package com.droidtune.suresim

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.telephony.euicc.DownloadableSubscription
import android.telephony.euicc.EuiccManager
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam

/**
 * SureSIM Plus — eSIM support module.
 *
 * Based on SureSIM by thatmarcel (AGPL-3.0). This fork only ever spoofs
 * eSIM support (EuiccManager.isEnabled) and, optionally, copies the eSIM
 * activation code to the clipboard. It does NOT spoof any other device
 * identity or capability: no model, brand, Android version, IMEI or serial.
 *
 * Every hook is registered defensively: the method is looked up first and
 * the registration is wrapped in try/catch, so on an Android version where
 * a method does not exist the module simply does nothing instead of
 * crashing the target app.
 */
class ModuleMain : XposedModule() {

    companion object {
        var application: Application? = null
    }

    /**
     * Reads the settings the user chose in the SureSIM Plus app.
     * Runs on every hook call so a toggle change takes effect without a
     * reboot (after the first reboot that loads the module).
     *
     * The settings are read through the framework's RemotePreferences
     * (XSharedPreferences does not exist in the libxposed API): the
     * module app's XposedProvider serves its own SharedPreferences
     * file to the framework, so no world-readable file is needed.
     *
     * If the preferences cannot be read at all, the module falls back
     * to its original behaviour — eSIM on, copy on — so it still works
     * out of the box. An explicit "off" in the app is always respected.
     */
    private fun readPrefs(): Pair<Boolean, Boolean> {
        return try {
            val prefs = getRemotePreferences(Prefs.FILE_NAME)
            val esimOn = prefs.getBoolean(
                Prefs.KEY_ESIM_ENABLED,
                Prefs.DEFAULT_ESIM_ENABLED
            )
            val copyOn = prefs.getBoolean(
                Prefs.KEY_COPY_CODE,
                Prefs.DEFAULT_COPY_CODE
            )
            esimOn to copyOn
        } catch (e: Throwable) {
            // Preferences unreadable: fall back to the original
            // always-on behaviour, never crash.
            Prefs.DEFAULT_ESIM_ENABLED to Prefs.DEFAULT_COPY_CODE
        }
    }

    /** Hook a method only if it exists on this Android version. */
    private fun hookIfExists(clazz: Class<*>, name: String, vararg params: Class<*>) =
        try {
            val method = clazz.getDeclaredMethod(name, *params)
            hook(method)
        } catch (e: Throwable) {
            // NoSuchMethodException / ClassNotFoundException / anything
            // else: this Android version does not have the method, so
            // there is nothing to hook. Fail silently.
            null
        }

    override fun onPackageReady(param: PackageReadyParam) {
        super.onPackageReady(param)

        if (!param.isFirstPackage) {
            return
        }

        // Capture the target app's Application so the clipboard hook can
        // reach the ClipboardManager.
        hookIfExists(
            Application::class.java,
            "onCreate"
        )?.intercept { chain ->
            try {
                application = chain.thisObject as? Application
            } catch (e: Throwable) {
                // Never crash the target app.
            }
            chain.proceed()
        }

        // 1. eSIM support: make the app think eSIM is supported.
        //    Only when the user left the toggle on in the app.
        hookIfExists(
            EuiccManager::class.java,
            "isEnabled"
        )?.intercept { chain ->
            val (esimOn, _) = readPrefs()
            if (!esimOn) {
                // Toggle off: let the real result through untouched.
                return@intercept chain.proceed()
            }
            chain.proceed()
            true
        }

        // 2. Activation code: when an app prepares an eSIM profile,
        //    copy its activation code to the clipboard (if enabled) and
        //    tell our own app the time it happened — never the code
        //    itself, which is never logged or broadcast.
        hookIfExists(
            DownloadableSubscription::class.java,
            "forActivationCode",
            String::class.java
        )?.intercept { chain ->
            val returnValue = chain.proceed()
            val (_, copyOn) = readPrefs()
            if (!copyOn) {
                return@intercept returnValue
            }

            try {
                val app = application ?: return@intercept returnValue
                val subscription =
                    returnValue as? DownloadableSubscription
                        ?: return@intercept returnValue
                val code = subscription.encodedActivationCode
                    ?: return@intercept returnValue

                val clipboardManager =
                    app.getSystemService(Context.CLIPBOARD_SERVICE)
                        as? ClipboardManager
                clipboardManager?.setPrimaryClip(
                    ClipData.newPlainText(
                        "Encoded eSIM activation code",
                        code
                    )
                )

                // Tell the SureSIM Plus app (only the time, never the
                // code) so its status line can show the last copy time.
                val broadcast = Intent(Prefs.ACTION_CODE_COPIED).apply {
                    setPackage("com.droidtune.suresim")
                    putExtra(Prefs.EXTRA_COPY_TIME, System.currentTimeMillis())
                }
                app.sendBroadcast(broadcast)
            } catch (e: Throwable) {
                // Clipboard / broadcast failure must never break the app
                // that is installing the eSIM profile.
            }

            returnValue
        }
    }
}
