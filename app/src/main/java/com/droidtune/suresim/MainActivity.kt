package com.droidtune.suresim

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * SureSIM Plus settings UI.
 *
 * Two toggles, saved in SharedPreferences and read by the hook in the
 * target app's process through the framework's RemotePreferences
 * (the module app's XposedProvider serves the settings file):
 *  - eSIM support on/off
 *  - copy activation code to clipboard on/off
 *
 * The status line shows whether the module is on and the last time an
 * activation code was copied — never the code itself.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SettingsScreen()
                }
            }
        }
    }
}

private fun loadPrefs(context: Context) =
    context.getSharedPreferences(Prefs.FILE_NAME, Context.MODE_PRIVATE)

@Composable
private fun SettingsScreen() {
    val context = LocalContext.current
    val prefs = loadPrefs(context)

    var esimOn by mutableStateOf(
        prefs.getBoolean(Prefs.KEY_ESIM_ENABLED, Prefs.DEFAULT_ESIM_ENABLED)
    )
    var copyOn by mutableStateOf(
        prefs.getBoolean(Prefs.KEY_COPY_CODE, Prefs.DEFAULT_COPY_CODE)
    )
    var lastCopy by mutableLongStateOf(
        prefs.getLong(Prefs.KEY_LAST_COPY_TIME, 0L)
    )

    fun save() {
        prefs.edit()
            .putBoolean(Prefs.KEY_ESIM_ENABLED, esimOn)
            .putBoolean(Prefs.KEY_COPY_CODE, copyOn)
            .apply()
    }

    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    val statusText = if (esimOn) {
        "Module ON — apps will see eSIM as supported."
    } else {
        "Module OFF — apps see the real eSIM support."
    }
    val lastCopyText = if (lastCopy > 0L) {
        "Last activation code copied: ${dateFormat.format(Date(lastCopy))}"
    } else {
        "Last activation code copied: never"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "SureSIM Plus",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "eSIM support for LSPosed / Vector — nothing else is spoofed.",
            style = MaterialTheme.typography.bodyMedium
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Status", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = statusText)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = lastCopyText)
            }
        }

        ToggleRow(
            title = "eSIM support",
            subtitle = "Make apps think eSIM is supported on this phone.",
            checked = esimOn,
            onCheckedChange = {
                esimOn = it
                save()
            }
        )

        ToggleRow(
            title = "Copy activation code",
            subtitle = "When an app prepares an eSIM profile, copy its activation code to the clipboard.",
            checked = copyOn,
            onCheckedChange = {
                copyOn = it
                save()
            }
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "How to use it", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "1. Install this APK.\n" +
                        "2. In LSPosed / Vector Manager, enable SureSIM Plus for the apps that need eSIM.\n" +
                        "3. Reboot your phone.\n" +
                        "4. Open this app and turn eSIM support on.\n" +
                        "Changes to the switches above take effect without another reboot."
                )
            }
        }

        Text(
            text = "Based on SureSIM by thatmarcel (AGPL-3.0). " +
                "This version only spoofs eSIM support — no phone model, " +
                "brand, Android version, IMEI or serial is faked.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun ToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold)
                Text(text = subtitle, style = MaterialTheme.typography.bodyMedium)
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}
