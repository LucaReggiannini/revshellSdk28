package org.android.settings

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class MainBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        MainService.startService(context)
    }
}