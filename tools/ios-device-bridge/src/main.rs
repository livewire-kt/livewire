use std::collections::{HashMap, HashSet};
use std::io::{self};
use std::sync::Arc;
use std::time::Duration;

use base64::{Engine as _, engine::general_purpose::STANDARD as BASE64};
use clap::Parser;
use rand::RngCore;
use serde::{Deserialize, Serialize};
use tokio::io::{AsyncBufReadExt, AsyncWriteExt, BufReader};
use tokio::net::{TcpListener, TcpStream};
use tokio::sync::{RwLock, broadcast, mpsc, oneshot};
use tokio::time::sleep;

mod devices;
mod forwarder;
mod usbmux;

use crate::devices::{DeviceInfo, DeviceType, PhysicalDevice, load_physical_info, query_simulators};
use crate::forwarder::run_forwarder;
use crate::usbmux::{UsbMux, UsbMuxEvent};

const PROTOCOL_VERSION: u32 = 1;
const USBMUXD_SOCKET_PATH: &str = "/var/run/usbmuxd";

#[derive(Debug, Deserialize)]
#[serde(tag = "type")]
enum ClientMessage {
  /// Initial handshake - includes a provided auth token to prevent other local devices from using our socket - TODO: replace with real security?
  #[serde(rename = "hello")]
  Hello { token: String },
  /// Start usb port forwarding for the given physical device udid
  #[serde(rename = "activate")]
  Activate { udid: String },
  /// Stop any active usb port forwarding session
  #[serde(rename = "deactivate")]
  Deactivate,
}

#[derive(Debug, Serialize)]
#[serde(tag = "type")]
enum ServerMessage<'a> {
  /// Handshake response, including auth success or failure
  #[serde(rename = "hello")]
  Hello { ok: bool, version: u32 },
  /// Current device list snapshot
  #[serde(rename = "devices")]
  Devices { devices: &'a [DeviceInfo] },
  /// Acknowledge that activation was requested
  #[serde(rename = "activated")]
  Activated { ok: bool, udid: Option<&'a str> },
  /// Acknowledge that deactivation was requested
  #[serde(rename = "deactivated")]
  Deactivated { ok: bool },
  /// Protocol-level error
  #[serde(rename = "error")]
  Error { message: &'a str },
}

#[derive(Debug, Serialize)]
struct ServerStarted {
  r#type: &'static str,
  version: u32,
  port: u16,
  token: String,
}

#[derive(Debug)]
struct ForwarderHandle {
  stop: oneshot::Sender<()>,
  _task: tokio::task::JoinHandle<()>,
}

type SharedState = Arc<RwLock<DeviceState>>;

#[derive(Default)]
struct DeviceState {
  devices: HashMap<String, DeviceInfo>,
  physical: HashMap<String, PhysicalDevice>,
  by_id: HashMap<u32, String>,
}

#[derive(Parser)]
struct Args {
  #[arg(long)]
  forward_port: u16,

  #[arg(long)]
  multiplex_port: u16,
}

#[tokio::main]
async fn main() -> io::Result<()> {
  let args = Args::parse();

  let token = generate_token();

  let listener = TcpListener::bind(("127.0.0.1", 0)).await?;
  let port = listener.local_addr()?.port();

  let started = ServerStarted {
    r#type: "server_started",
    version: PROTOCOL_VERSION,
    port,
    token: token.clone(),
  };
  println!("{}", serde_json::to_string(&started)?);

  let state: SharedState = Arc::new(RwLock::new(DeviceState::default()));

  let (device_tx, _device_rx) = broadcast::channel::<Vec<DeviceInfo>>(16);

  let (activate_tx, activate_rx) = mpsc::unbounded_channel::<Option<String>>();
  let forwarder_state = Arc::new(RwLock::new(None::<ForwarderHandle>));
  let active_udid: Arc<RwLock<Option<String>>> = Arc::new(RwLock::new(None));

  spawn_usbmux_listener(state.clone(), device_tx.clone());
  spawn_simulator_poll(state.clone(), device_tx.clone());

  spawn_forwarder_manager(
    activate_rx,
    state.clone(),
    forwarder_state.clone(),
    active_udid.clone(),
    args.forward_port,
    args.multiplex_port,
  );

  loop {
    let (stream, _) = listener.accept().await?;
    let token = token.clone();
    let state = state.clone();
    let device_tx = device_tx.clone();
    let activate_tx = activate_tx.clone();
    tokio::spawn(async move {
      if let Err(err) = handle_client(stream, token, state, device_tx, activate_tx).await {
        eprintln!("client error: {err}");
      }
    });
  }
}

fn generate_token() -> String {
  let mut bytes = [0u8; 24];
  rand::thread_rng().fill_bytes(&mut bytes);
  BASE64.encode(bytes)
}

async fn handle_client(
  stream: TcpStream,
  token: String,
  state: SharedState,
  device_tx: broadcast::Sender<Vec<DeviceInfo>>,
  activate_tx: mpsc::UnboundedSender<Option<String>>,
) -> io::Result<()> {
  let (read, mut write) = stream.into_split();
  let mut reader = BufReader::new(read);

  let mut line = String::new();
  let read = reader.read_line(&mut line).await?;
  if read == 0 {
    return Ok(());
  }

  let hello: ClientMessage = match serde_json::from_str(line.trim()) {
    Ok(msg) => msg,
    Err(_) => {
      let msg = ServerMessage::Error {
        message: "invalid hello",
      };
      write_json(&mut write, &msg).await?;
      return Ok(());
    }
  };

  match hello {
    ClientMessage::Hello { token: t } if t == token => {
      let msg = ServerMessage::Hello {
        ok: true,
        version: PROTOCOL_VERSION,
      };
      write_json(&mut write, &msg).await?;
    }
    _ => {
      let msg = ServerMessage::Hello {
        ok: false,
        version: PROTOCOL_VERSION,
      };
      write_json(&mut write, &msg).await?;
      return Ok(());
    }
  }

  let mut device_rx = device_tx.subscribe();
  let mut current_devices = snapshot_devices(&state).await;
  write_json(
    &mut write,
    &ServerMessage::Devices {
      devices: &current_devices,
    },
  )
    .await?;

  let mut line = String::new();
  loop {
    tokio::select! {
      result = reader.read_line(&mut line) => {
        let n = result?;
        if n == 0 {
          return Ok(());
        }
        if let Ok(msg) = serde_json::from_str::<ClientMessage>(line.trim()) {
          match msg {
            ClientMessage::Activate { udid } => {
              let _ = activate_tx.send(Some(udid.clone()));
              write_json(&mut write, &ServerMessage::Activated { ok: true, udid: Some(&udid) }).await?;
            }
            ClientMessage::Deactivate => {
              let _ = activate_tx.send(None);
              write_json(&mut write, &ServerMessage::Deactivated { ok: true }).await?;
            }
            ClientMessage::Hello { .. } => {}
          }
        }
        line.clear();
      }
      Ok(updated) = device_rx.recv() => {
        current_devices = updated;
        write_json(&mut write, &ServerMessage::Devices { devices: &current_devices }).await?;
      }
    }
  }
}

async fn write_json<T: Serialize>(
  write: &mut tokio::net::tcp::OwnedWriteHalf,
  value: &T,
) -> io::Result<()> {
  let mut buf = serde_json::to_vec(value).unwrap();
  buf.push(b'\n');
  write.write_all(&buf).await
}

async fn snapshot_devices(state: &SharedState) -> Vec<DeviceInfo> {
  let guard = state.read().await;
  guard.devices.values().cloned().collect()
}

fn spawn_usbmux_listener(state: SharedState, device_tx: broadcast::Sender<Vec<DeviceInfo>>) {
  tokio::spawn(async move {
    let socket_path = USBMUXD_SOCKET_PATH;

    loop {
      match UsbMux::connect(socket_path).await {
        Ok(mut mux) => {
          eprintln!("usbmux listen started");
          let initial_events = match mux.listen().await {
            Ok(events) => events,
            Err(err) => {
              eprintln!("usbmux listen error: {err}");
              sleep(Duration::from_secs(2)).await;
              continue;
            }
          };

          for evt in initial_events {
            handle_usbmux_event(&state, &device_tx, evt).await;
          }

          while let Some(evt) = mux.next_event().await {
            handle_usbmux_event(&state, &device_tx, evt).await;
          }
        }
        Err(err) => {
          eprintln!("usbmux connect error: {err}");
          sleep(Duration::from_secs(2)).await;
        }
      }
    }
  });
}

async fn handle_usbmux_event(
  state: &SharedState,
  device_tx: &broadcast::Sender<Vec<DeviceInfo>>,
  evt: UsbMuxEvent,
) {
  match evt {
    UsbMuxEvent::Attach {
      device_id,
      properties,
    } => {
      if let Some(udid) = properties.get("SerialNumber").and_then(|v| v.as_string()) {
        let info = load_physical_info(udid);
        let physical = PhysicalDevice {
          device_id,
          udid: udid.to_string(),
        };
        {
          let mut write = state.write().await;
          write.physical.insert(udid.to_string(), physical);
          write.by_id.insert(device_id, udid.to_string());
          write.devices.insert(udid.to_string(), info);
        }
        eprintln!("usbmux attach: device_id={} udid={}", device_id, udid);
        let _ = device_tx.send(snapshot_devices(state).await);
      } else {
        let keys: Vec<&String> = properties.keys().collect();
        eprintln!(
          "usbmux attach: device_id={} missing SerialNumber, keys={:?}",
          device_id, keys
        );
      }
    }
    UsbMuxEvent::Detach { device_id, udid } => {
      eprintln!(
        "usbmux detach: device_id={} udid={:?}",
        device_id, udid
      );
      if let Some(udid) = udid {
        {
          let mut write = state.write().await;
          write.physical.remove(&udid);
          write.by_id.remove(&device_id);
          write.devices.remove(&udid);
        }
      } else {
        {
          let mut write = state.write().await;
          if let Some(udid) = write.by_id.remove(&device_id) {
            write.physical.remove(&udid);
            write.devices.remove(&udid);
          }
        }
      }
      let _ = device_tx.send(snapshot_devices(state).await);
    }
  }
}

fn spawn_simulator_poll(state: SharedState, device_tx: broadcast::Sender<Vec<DeviceInfo>>) {
  if !cfg!(target_os = "macos") {
    return;
  }

  tokio::spawn(async move {
    let mut last_count: usize = 0;
    loop {
      if let Ok(simulators) = query_simulators() {
        let mut current: HashSet<String> = HashSet::new();
        for sim in simulators.iter() {
          current.insert(sim.udid.clone());
        }

        let mut changed = false;
        {
          let mut write = state.write().await;
          for sim in simulators {
            if write.devices.insert(sim.udid.clone(), sim).is_none() {
              changed = true;
            }
          }
          let remove: Vec<String> = write
            .devices
            .iter()
            .filter_map(|(udid, info)| {
              if matches!(info.device_type, DeviceType::Simulator) && !current.contains(udid) {
                Some(udid.clone())
              } else {
                None
              }
            })
            .collect();
          if !remove.is_empty() {
            changed = true;
            for udid in remove {
              write.devices.remove(&udid);
            }
          }
        }

        if current.len() != last_count {
          eprintln!("simctl: found {} booted simulator(s)", current.len());
          last_count = current.len();
        }

        if changed {
          let _ = device_tx.send(snapshot_devices(&state).await);
        }
      }

      sleep(Duration::from_secs(3)).await;
    }
  });
}

fn spawn_forwarder_manager(
  mut activate_rx: mpsc::UnboundedReceiver<Option<String>>,
  state: SharedState,
  forwarder_state: Arc<RwLock<Option<ForwarderHandle>>>,
  active_udid: Arc<RwLock<Option<String>>>,
  forward_port: u16,
  multiplex_port: u16,
) {
  tokio::spawn(async move {
    let socket_path = USBMUXD_SOCKET_PATH;

    while let Some(udid) = activate_rx.recv().await {
      if let Some(handle) = forwarder_state.write().await.take() {
        let _ = handle.stop.send(());
      }

      let Some(udid) = udid else {
        *active_udid.write().await = None;
        continue;
      };

      let device = state.read().await.physical.get(&udid).cloned();
      let Some(device) = device else {
        *active_udid.write().await = None;
        continue;
      };
      eprintln!(
        "activating device: udid={} device_id={}",
        device.udid, device.device_id
      );

      let (stop_tx, stop_rx) = oneshot::channel();
      let task = tokio::spawn(async move {
        if let Err(err) = run_forwarder(
          socket_path,
          device.device_id,
          forward_port,
          multiplex_port,
          stop_rx,
        ).await
        {
          eprintln!("forwarder error: {err}");
        }
      });

      *forwarder_state.write().await = Some(ForwarderHandle {
        stop: stop_tx,
        _task: task,
      });
      *active_udid.write().await = Some(udid);
    }
  });
}
