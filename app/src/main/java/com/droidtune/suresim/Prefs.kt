package com.droidtune.suresim

/**
 * Shared constants for eSIM Enabler.
 *
 * The settings UI (MainActivity) writes these SharedPreferences; the hook
 * (ModuleMain) reads them from the target app's process through the
 * framework's RemotePreferences (getRemotePreferences). Only eSIM support
 * is ever spoofed — nothing else.
 */
object Prefs {
    const val FILE_NAME = "suresim_plus"
    const val KEY_ESIM_ENABLED = "esim_enabled"
    const val KEY_COPY_CODE = "copy_activation_code"
    const val KEY_LAST_COPY_TIME = "last_copy_time"

    const val DEFAULT_ESIM_ENABLED = true
    const val DEFAULT_COPY_CODE = true

    /** Explicit broadcast sent by the hook when an activation code is copied. */
    const val ACTION_CODE_COPIED = "com.droidtune.suresim.ACTION_CODE_COPIED"
    const val EXTRA_COPY_TIME = "copy_time"
}
