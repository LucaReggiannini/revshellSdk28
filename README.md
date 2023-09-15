# revshellSdk28
This is my Android Reverse Shell with dropping capabilities.

The idea was to create a Termux-like environment with a main APK with core functionality, capable of dropping additional executables and scripts.

## Features
- **Secure TLS shell**
- **Persistent** over time and reboots
- **Hidden** and camouflaged with Google apps
- **Drops additional statically linked executable and scripts** based on CPU architecture

Executable and scripts integrated by default:
- `socat` (aarch64 and x86/x64)
- `curl` (aarch64 and x86/x64)
- custom script for file exfiltration over HTTPS (`uplaod`)
- custom script to get device public IP (`ipinfo`)
- custom script to override `whoami` and set a custom "victim id" (`whoami`)
