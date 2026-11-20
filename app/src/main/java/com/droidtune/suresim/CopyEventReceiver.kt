package com.droidtune.suresim

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Receives the hook's "an activation code was copied" broadcast and
 * records only the time it happened. The activation code itself is never
 * sent in the broadcast, never logged and never stored here.
 */
class CopyEventReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Prefs.ACTION_CODE_COPIED) return
        val time = intent.getLongExtra(Prefs.EXTRA_COPY_TIME, 0L)
        if (time <= 0L) return
        try {
            context.getSharedPreferences(Prefs.FILE_NAME, Context.MODE_PRIVATE)
                .edit()
                .putLong(Prefs.KEY_LAST_COPY_TIME, time)
                .apply()
        } catch (e: Throwable) {
            // Recording the time is best-effort only.
        }
    }
}
