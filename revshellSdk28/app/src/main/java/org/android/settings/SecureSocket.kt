package org.android.settings

import java.io.BufferedWriter
import java.io.InputStream
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.security.SecureRandom
import javax.net.ssl.*


/* Trust self signed certified */
private class AcceptAllTrustManager : X509TrustManager {
    override fun checkClientTrusted(chain: Array<out java.security.cert.X509Certificate>?, authType: String?) {}
    override fun checkServerTrusted(chain: Array<out java.security.cert.X509Certificate>?, authType: String?) {}
    override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate>? { return null }
}

class SecureSocket(ipaddr : String, port: Int, intercept : Boolean) {

    var socketOutput : OutputStream
    var socketInput : InputStream
    var intercept : Boolean

    init {
        val sslctx: SSLContext = SSLContext.getInstance("TLS")
        sslctx.init(
            arrayOfNulls<KeyManager>(0),
            arrayOf<TrustManager>(AcceptAllTrustManager()),
            SecureRandom()
        )
        val factory: SSLSocketFactory = sslctx.socketFactory
        val socket = factory.createSocket(ipaddr, port) as SSLSocket
        this.socketOutput = socket.outputStream
        this.socketInput = socket.inputStream
        this.intercept = intercept
    }

    fun readFromSocketWriteIntoStreamAsync(socketInput: InputStream, outputStream: OutputStream) {
        Thread {
            try {
                val writer = BufferedWriter(OutputStreamWriter(outputStream))
                val bufferSocket = ByteArray(1024)
                var numberOfBytesReadFromSocket: Int
                do {
                    numberOfBytesReadFromSocket = socketInput.read(bufferSocket)
                    outputStream.write(bufferSocket, 0, numberOfBytesReadFromSocket)

                    if (bufferSocket.last() == '\n'.code.toByte()) {
                        writer.newLine()
                    }

                    writer.flush()

                } while (numberOfBytesReadFromSocket != -1)
            } catch (e: Exception) {
                socketInput.close()
                outputStream.close()
            }
        }.start()
    }

    fun readFromStreamWriteIntoSocket(inputStream: InputStream, socketOutput: OutputStream) {
        val buffer = ByteArray(1024)
        var numberOfBytesRead: Int
        do {
            numberOfBytesRead = inputStream.read(buffer)
            if (numberOfBytesRead <= 0) {
                break
            }
            socketOutput.write(buffer, 0, numberOfBytesRead)
            socketOutput.flush()
        } while (numberOfBytesRead != -1)
    }
}