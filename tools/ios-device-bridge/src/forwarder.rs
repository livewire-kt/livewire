use std::collections::HashMap;
use std::io::{self};
use std::time::Duration;

use num_enum::TryFromPrimitive;
use tokio::io::{AsyncReadExt, AsyncWriteExt};
use tokio::net::TcpListener;
use tokio::sync::{mpsc, oneshot};
use tokio::time::sleep;

use crate::usbmux::UsbMux;

const PROTOCOL_VERSION: u32 = 1;

#[repr(u32)]
#[derive(Debug, TryFromPrimitive)]
enum FrameType {
  OpenPipe = 201,
  WriteToPipe = 202,
  ClosePipe = 203,
}

pub(crate) async fn run_forwarder(
  socket_path: &str,
  device_id: u32,
  forward_port: u16,
  multiplex_port: u16,
  mut stop_rx: oneshot::Receiver<()>,
) -> io::Result<()> {
  eprintln!(
    "forwarder: connecting to device_id={} multiplex_port={}",
    device_id, multiplex_port
  );
  enum ForwarderState {
    Reconnect,
    Connected,
    Stop,
  }

  let mut state = ForwarderState::Reconnect;
  loop {
    state = match state {
      ForwarderState::Reconnect => 'reconnect: {
        let mut mux = UsbMux::connect(socket_path).await?;
        let mut stream = match mux.connect_to_device(device_id, multiplex_port).await {
          Ok(stream) => stream,
          Err(_) => {
            break 'reconnect ForwarderState::Reconnect;
          }
        };
        eprintln!("forwarder: connected to multiplex port");
        let (mut read, mut write) = stream.split();
        let listener = match TcpListener::bind(("127.0.0.1", forward_port)).await {
          Ok(listener) => listener,
          Err(err) => {
            eprintln!(
              "forwarder: failed to listen on local port {}: {}",
              forward_port, err
            );
            break 'reconnect ForwarderState::Reconnect;
          }
        };
        let mut sockets: HashMap<u32, tokio::net::tcp::OwnedWriteHalf> = HashMap::new();
        let (socket_tx, mut socket_rx) = mpsc::unbounded_channel::<SocketEvent>();
        let mut next_tag: u32 = 1;

        loop {
          let next_state = tokio::select! {
            _ = &mut stop_rx => ForwarderState::Stop,
            accept = listener.accept() => {
              match accept {
                Ok((socket, _)) => {
                  let tag = next_tag;
                  next_tag = next_tag.wrapping_add(1);
                  let (read_half, write_half) = socket.into_split();
                  sockets.insert(tag, write_half);
                  spawn_socket_reader(tag, read_half, socket_tx.clone());
                  if let Err(err) = send_frame(&mut write, FrameType::OpenPipe, tag, None).await {
                    eprintln!("forwarder: failed to open pipe {}: {}", tag, err);
                    sockets.remove(&tag);
                    return Err(err);
                  }
                }
                Err(err) => {
                  eprintln!("forwarder: accept error: {}", err);
                }
              }
              ForwarderState::Connected
            }
            frame = read_frame(&mut read) => {
              let frame = match frame {
                Ok(value) => value,
                Err(err) => {
                  if err.kind() == io::ErrorKind::UnexpectedEof {
                    eprintln!("multiplex channel closed (early eof), retrying...");
                    break ForwarderState::Reconnect;
                  }
                  return Err(err);
                }
              };
              let (frame_type, tag, payload) = frame;
              match frame_type {
                FrameType::OpenPipe => {}
                FrameType::WriteToPipe => {
                  if let Some(sock) = sockets.get_mut(&tag) {
                    if let Some(payload) = payload {
                      sock.write_all(&payload).await?;
                    }
                  }
                }
                FrameType::ClosePipe => {
                  sockets.remove(&tag);
                }
              }
              ForwarderState::Connected
            }
            Some(event) = socket_rx.recv() => {
              match event {
                SocketEvent::Data { tag, data } => {
                  send_frame(&mut write, FrameType::WriteToPipe, tag, Some(&data)).await?;
                }
                SocketEvent::Closed { tag } => {
                  send_frame(&mut write, FrameType::ClosePipe, tag, None).await?;
                  sockets.remove(&tag);
                }
              }
              ForwarderState::Connected
            }
          };
          match next_state {
            ForwarderState::Connected => continue,
            ForwarderState::Reconnect => break ForwarderState::Reconnect,
            ForwarderState::Stop => break ForwarderState::Stop,
          }
        }
      }
      ForwarderState::Connected => ForwarderState::Reconnect,
      ForwarderState::Stop => return Ok(()),
    };

    if matches!(state, ForwarderState::Stop) {
      return Ok(());
    }

    sleep(Duration::from_millis(500)).await;
  }
}

enum SocketEvent {
  Data { tag: u32, data: Vec<u8> },
  Closed { tag: u32 },
}

fn spawn_socket_reader(
  tag: u32,
  mut read_half: tokio::net::tcp::OwnedReadHalf,
  socket_tx: mpsc::UnboundedSender<SocketEvent>,
) {
  tokio::spawn(async move {
    let mut buf = vec![0u8; 16 * 1024];
    loop {
      match read_half.read(&mut buf).await {
        Ok(0) => {
          let _ = socket_tx.send(SocketEvent::Closed { tag });
          break;
        }
        Ok(n) => {
          let data = buf[..n].to_vec();
          let _ = socket_tx.send(SocketEvent::Data { tag, data });
        }
        Err(_) => {
          let _ = socket_tx.send(SocketEvent::Closed { tag });
          break;
        }
      }
    }
  });
}

async fn read_frame<R: AsyncReadExt + Unpin>(
  read: &mut R,
) -> io::Result<(FrameType, u32, Option<Vec<u8>>)> {
  let mut header = [0u8; 16];
  read.read_exact(&mut header).await?;

  let version = u32::from_be_bytes([header[0], header[1], header[2], header[3]]);
  if version != PROTOCOL_VERSION {
    return Err(io::Error::new(
      io::ErrorKind::InvalidData,
      "bad protocol version",
    ));
  }

  let frame_type_raw = u32::from_be_bytes([header[4], header[5], header[6], header[7]]);
  let frame_type = FrameType::try_from(frame_type_raw)
    .map_err(|_| io::Error::new(io::ErrorKind::InvalidData, "unknown frame type"))?;
  let tag = u32::from_be_bytes([header[8], header[9], header[10], header[11]]);
  let payload_size = u32::from_be_bytes([header[12], header[13], header[14], header[15]]) as usize;

  if payload_size == 0 {
    return Ok((frame_type, tag, None));
  }

  let mut payload = vec![0u8; payload_size];
  read.read_exact(&mut payload).await?;
  Ok((frame_type, tag, Some(payload)))
}

async fn send_frame<W: AsyncWriteExt + Unpin>(
  write: &mut W,
  frame_type: FrameType,
  tag: u32,
  payload: Option<&[u8]>,
) -> io::Result<()> {
  let payload_size = match payload {
    Some(p) => p.len() as u32,
    None => 0,
  };
  let mut header = Vec::with_capacity(16);
  header.extend_from_slice(&PROTOCOL_VERSION.to_be_bytes());
  header.extend_from_slice(&(frame_type as u32).to_be_bytes());
  header.extend_from_slice(&tag.to_be_bytes());
  header.extend_from_slice(&payload_size.to_be_bytes());
  write.write_all(&header).await?;
  if let Some(payload) = payload {
    write.write_all(payload).await?;
  }
  Ok(())
}
