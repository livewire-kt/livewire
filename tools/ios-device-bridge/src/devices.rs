use std::collections::HashMap;
use std::io::{self};
use std::process::Command;

use serde::{Deserialize, Serialize};

#[derive(Clone, Debug, Serialize)]
#[serde(rename_all = "snake_case")]
pub(crate) enum DeviceType {
  Physical,
  Simulator,
}

#[derive(Clone, Debug, Serialize)]
pub(crate) struct DeviceInfo {
  pub(crate) udid: String,
  pub(crate) name: String,
  pub(crate) product_type: String,
  pub(crate) device_type: DeviceType,
  pub(crate) os_version: String,
}

#[derive(Clone, Debug)]
pub(crate) struct PhysicalDevice {
  pub(crate) device_id: u32,
  pub(crate) udid: String,
}

pub(crate) fn load_physical_info(udid: &str) -> DeviceInfo {
  let mut name: Option<String> = None;
  let mut product_type: Option<String> = None;
  let mut os_version: Option<String> = None;

  // Attempt ideviceinfo first - primarily for linux support, but could be installed on macos too
  if let Ok(output) = Command::new("ideviceinfo").arg("-u").arg(udid).output()
    && output.status.success()
    && let Ok(text) = String::from_utf8(output.stdout)
  {
    for line in text.lines() {
      if let Some((key, value)) = line.split_once(": ") {
        match key.trim() {
          "DeviceName" => name = Some(value.trim().to_string()),
          "ProductType" => product_type = Some(value.trim().to_string()),
          "ProductVersion" => os_version = Some(value.trim().to_string()),
          _ => {}
        }
      }
    }
  }

  // Fallback to xctrace if needed
  if name.is_none() || os_version.is_none() {
    if let Some((fallback_name, fallback_version)) = xctrace_device_info(udid) {
      name.get_or_insert(fallback_name);
      os_version.get_or_insert(fallback_version);
    }
  }

  DeviceInfo {
    udid: udid.to_string(),
    name: name.unwrap_or_else(|| udid.to_string()),
    product_type: product_type.unwrap_or_else(|| "unknown".to_string()),
    device_type: DeviceType::Physical,
    os_version: os_version.unwrap_or_else(|| "unknown".to_string()),
  }
}

fn xctrace_device_info(udid: &str) -> Option<(String, String)> {
  if !cfg!(target_os = "macos") {
    return None;
  }

  let output = Command::new("xcrun")
    .arg("xctrace")
    .arg("list")
    .arg("devices")
    .output()
    .ok()?;

  if !output.status.success() {
    return None;
  }

  let text = String::from_utf8_lossy(&output.stdout);
  for line in text.lines() {
    if !line.contains(udid) {
      continue;
    }

    let idx = line.find(udid)?;
    let prefix = line[..idx]
      .trim()
      .trim_end_matches(')')
      .trim_end_matches('(')
      .trim();
    let last_open = prefix.rfind('(')?;
    let name = prefix[..last_open].trim().to_string();
    let mut version = prefix[last_open + 1..].trim().to_string();
    if let Some(stripped) = version.strip_prefix("iOS ") {
      version = stripped.to_string();
    }
    if name.is_empty() || version.is_empty() {
      return None;
    }
    return Some((name, version));
  }
  None
}

#[derive(Debug, Deserialize)]
struct SimctlList {
  devices: HashMap<String, Vec<SimctlDevice>>,
}

#[derive(Debug, Deserialize)]
struct SimctlDevice {
  state: String,
  #[serde(rename = "isAvailable")]
  is_available: Option<serde_json::Value>,
  name: String,
  udid: String,
}

pub(crate) fn query_simulators() -> io::Result<Vec<DeviceInfo>> {
  let output = Command::new("xcrun")
    .arg("simctl")
    .arg("list")
    .arg("devices")
    .arg("--json")
    .output()
    .or_else(|err| {
      if err.kind() == io::ErrorKind::NotFound {
        Command::new("/usr/bin/xcrun")
          .arg("simctl")
          .arg("list")
          .arg("devices")
          .arg("--json")
          .output()
      } else {
        Err(err)
      }
    })?;

  if !output.status.success() {
    let stderr = String::from_utf8_lossy(&output.stderr);
    eprintln!(
      "simctl failed: status={} stderr={}",
      output.status,
      stderr.trim()
    );
    return Ok(Vec::new());
  }

  let list: SimctlList = match serde_json::from_slice(&output.stdout) {
    Ok(value) => value,
    Err(err) => {
      eprintln!("simctl parse failed: {}", err);
      return Ok(Vec::new());
    }
  };
  let mut result = Vec::new();

  for (runtime, devices) in list.devices {
    let os_version = runtime
      .split(".SimRuntime.iOS-")
      .nth(1)
      .map(|s| s.replace('-', "."))
      .unwrap_or_else(|| "unknown".to_string());

    for device in devices {
      if !sim_is_available(&device) {
        continue;
      }
      if device.state.to_lowercase() != "booted" {
        continue;
      }
      result.push(DeviceInfo {
        udid: device.udid,
        name: device.name,
        product_type: "simulator".to_string(),
        device_type: DeviceType::Simulator,
        os_version: os_version.clone(),
      });
    }
  }

  Ok(result)
}

fn sim_is_available(device: &SimctlDevice) -> bool {
  if let Some(value) = &device.is_available {
    match value {
      serde_json::Value::String(s) => s.as_str() == "YES",
      serde_json::Value::Bool(b) => *b,
      _ => false,
    }
  } else {
    true
  }
}
