package org.android.settings

import java.io.*

/* Start an interactive SH session */

class Shell(command : String, enableStderr : Boolean = true) {
    var stdin : OutputStream
    var stdout : InputStream
    private var process : Process

    init {
        val pb = ProcessBuilder()
        pb.redirectErrorStream(enableStderr)
        pb.redirectInput(ProcessBuilder.Redirect.PIPE)
        pb.redirectOutput(ProcessBuilder.Redirect.PIPE)
        this.process = pb.command("/bin/sh", "-i").start()
        this.stdin = process.outputStream
        this.stdout = process.inputStream

        val writer = BufferedWriter(OutputStreamWriter(this.stdin))
        this.stdin.write(command.toByteArray())
        writer.newLine()
        writer.flush()
    }

    fun exit() {
        this.process.destroy()
    }

    fun processOutput() : String {
        return this.stdout.bufferedReader().use {
            it.readText()
        }
    }
}