use std::collections::HashMap;
use std::io::{self};
use std::path::{Path, PathBuf};

use plist::Value as PlistValue;
use tokio::io::{AsyncReadExt, AsyncWriteExt};
use tokio::net::UnixStream;

pub(crate) enum UsbMuxEvent {
  Attach {
    device_id: u32,
    properties: HashMap<String, PlistValue>,
  },
  Detach {
    device_id: u32,
    udid: Option<String>,
  },
}

pub(crate) struct UsbMux {
  stream: UnixStream,
  socket_path: PathBuf,
  next_tag: u32,
}

impl UsbMux {
  pub(crate) async fn connect<P: AsRef<Path>>(path: P) -> io::Result<Self> {
    let path_ref = path.as_ref();
    let stream = UnixStream::connect(path_ref.to_path_buf()).await?;
    Ok(Self {
      stream,
      socket_path: path_ref.to_path_buf(),
      next_tag: 1,
    })
  }

  pub(crate) async fn listen(&mut self) -> io::Result<Vec<UsbMuxEvent>> {
    let packet = plist_packet("Listen", None);
    let tag = self.next_tag();
    send_usbmux_plist(&mut self.stream, tag, &packet).await?;
    let mut initial_events = Vec::new();
    loop {
      let (packet_tag, payload) = read_usbmux_packet(&mut self.stream).await?;
      if packet_tag == tag {
        break;
      }
      if packet_tag == 0 {
        if let Some(evt) = parse_usbmux_event(payload) {
          initial_events.push(evt);
        }
      }
    }
    Ok(initial_events)
  }

  pub(crate) async fn next_event(&mut self) -> Option<UsbMuxEvent> {
    loop {
      match read_usbmux_packet(&mut self.stream).await {
        Ok((tag, payload)) => {
          if tag != 0 {
            continue;
          }
          if let Some(evt) = parse_usbmux_event(payload) {
            return Some(evt);
          }
        }
        Err(_) => return None,
      }
    }
  }

  pub(crate) async fn connect_to_device(&mut self, device_id: u32, port: u16) -> io::Result<UnixStream> {
    let port_swapped = ((port << 8) & 0xFF00) | (port >> 8);
    match self.connect_with_port(device_id, port_swapped).await {
      Ok(stream) => Ok(stream),
      Err(_) => self.connect_with_port(device_id, port).await,
    }
  }

  async fn connect_with_port(
    &mut self,
    device_id: u32,
    port_number: u16,
  ) -> io::Result<UnixStream> {
    let mut payload = HashMap::new();
    payload.insert(
      "DeviceID".to_string(),
      PlistValue::Integer((device_id as i64).into()),
    );
    payload.insert(
      "PortNumber".to_string(),
      PlistValue::Integer((port_number as i64).into()),
    );
    let packet = plist_packet("Connect", Some(payload));

    let mut stream = UnixStream::connect(self.socket_path.clone()).await?;
    let tag = self.next_tag();
    send_usbmux_plist(&mut stream, tag, &packet).await?;
    let response = read_usbmux_plist(&mut stream, tag).await?;
    if let PlistValue::Dictionary(dict) = response {
      if let Some(PlistValue::Integer(number)) = dict.get("Number") {
        let code = number.as_signed().unwrap_or(0);
        if code != 0 {
          return Err(io::Error::new(
            io::ErrorKind::Other,
            "usbmux connect failed",
          ));
        }
      }
    }
    Ok(stream)
  }

  fn next_tag(&mut self) -> u32 {
    let tag = self.next_tag;
    self.next_tag += 1;
    tag
  }
}

fn parse_usbmux_event(payload: Option<PlistValue>) -> Option<UsbMuxEvent> {
  if let Some(PlistValue::Dictionary(dict)) = payload {
    if let Some(PlistValue::String(msg)) = dict.get("MessageType") {
      if msg.as_str() == "Attached" {
        if let Some(PlistValue::Integer(device_id)) = dict.get("DeviceID") {
          let mut props = HashMap::new();
          if let Some(PlistValue::Dictionary(p)) = dict.get("Properties") {
            for (k, v) in p.iter() {
              props.insert(k.clone(), v.clone());
            }
          }
          let device_id = device_id.as_signed().unwrap_or(0) as u32;
          return Some(UsbMuxEvent::Attach {
            device_id,
            properties: props,
          });
        }
      } else if msg.as_str() == "Detached" {
        let device_id = dict
          .get("DeviceID")
          .and_then(|v| v.as_signed_integer())
          .unwrap_or(0) as u32;
        let udid = dict
          .get("SerialNumber")
          .and_then(|v| v.as_string())
          .map(|s| s.to_string());
        return Some(UsbMuxEvent::Detach { device_id, udid });
      }
    }
  }
  None
}

async fn send_usbmux_plist(
  stream: &mut UnixStream,
  tag: u32,
  payload: &PlistValue,
) -> io::Result<()> {
  let mut buf = Vec::new();
  payload
    .to_writer_xml(&mut buf)
    .map_err(|_| io::Error::new(io::ErrorKind::Other, "plist encode failed"))?;
  send_usbmux_packet(stream, tag, &buf).await
}

async fn read_usbmux_plist(stream: &mut UnixStream, tag: u32) -> io::Result<PlistValue> {
  loop {
    let (packet_tag, payload) = read_usbmux_packet(stream).await?;
    if packet_tag != tag {
      continue;
    }
    return Ok(payload.unwrap_or(PlistValue::Dictionary(Default::default())));
  }
}

async fn send_usbmux_packet(stream: &mut UnixStream, tag: u32, payload: &[u8]) -> io::Result<()> {
  let size = (16 + payload.len()) as u32;
  let mut buf = Vec::with_capacity(size as usize);
  buf.extend_from_slice(&size.to_le_bytes());
  buf.extend_from_slice(&1u32.to_le_bytes());
  buf.extend_from_slice(&8u32.to_le_bytes());
  buf.extend_from_slice(&tag.to_le_bytes());
  buf.extend_from_slice(payload);
  stream.write_all(&buf).await
}

async fn read_usbmux_packet(stream: &mut UnixStream) -> io::Result<(u32, Option<PlistValue>)> {
  let mut size_buf = [0u8; 4];
  stream.read_exact(&mut size_buf).await?;
  let size = u32::from_le_bytes(size_buf) as usize;
  let mut buf = vec![0u8; size - 4];
  stream.read_exact(&mut buf).await?;

  if buf.len() < 12 {
    return Err(io::Error::new(
      io::ErrorKind::UnexpectedEof,
      "short usbmux packet",
    ));
  }

  let tag = u32::from_le_bytes([buf[8], buf[9], buf[10], buf[11]]);
  let payload = if buf.len() > 12 {
    let raw = &buf[12..];
    let cursor = io::Cursor::new(raw);
    plist::Value::from_reader(cursor).ok()
  } else {
    None
  };
  Ok((tag, payload))
}

fn plist_packet(message_type: &str, payload: Option<HashMap<String, PlistValue>>) -> PlistValue {
  let mut dict = plist::Dictionary::new();
  dict.insert(
    "MessageType".into(),
    PlistValue::String(message_type.to_string()),
  );
  dict.insert(
    "ProgName".into(),
    PlistValue::String("livewire-ios-bridge".to_string()),
  );
  dict.insert(
    "ClientVersionString".into(),
    PlistValue::String("1".to_string()),
  );

  if let Some(payload) = payload {
    for (k, v) in payload {
      dict.insert(k, v);
    }
  }
  PlistValue::Dictionary(dict)
}
