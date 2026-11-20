# eSIM Enabler

**LSPosed / Vector module to spoof eSIM support — eSIM only, nothing else.**

Based on SureSIM by thatmarcel (https://github.com/thatmarcel/suresim),
AGPL-3.0. Modified version.

This module makes every app it's enabled for think eSIM is supported
(`EuiccManager.isEnabled()` returns true) and, if you leave the second
switch on, copies the activation code to the clipboard when the app is
preparing to install a profile. That is all it does.

## What it does NOT do

- No fake phone model, brand, manufacturer or Android version.
- No fake IMEI, serial number or any other device identity.
- No unlocking of paid apps.
- The activation code is never displayed in the app, never logged and
  never sent anywhere. The app only shows the *time* the last code was
  copied.

## What's new in this version (1.0.3)

- New name: **eSIM Enabler**. Same app, same eSIM-only features — the
  launcher name, in-app title and release file now say eSIM Enabler.
  The package stays `com.droidtune.suresim`, so it still installs as
  an update over 1.0.1 / 1.0.2.

## What's new in version 1.0.2

- Scope fix: `staticScope` was removed from `module.prop`. With it, the
  LSPosed manager showed "The module declared static scope" and an empty
  app list, and no app could be selected. Now the scope is dynamic: tick
  the apps in LSPosed / Vector Manager as described in Install below.

## What's new in version 1.0.1

- Fix for AGP 9 / Gradle 9: the `org.jetbrains.kotlin.android` plugin is no
  longer required since AGP 9.0, so it has been removed from both build
  files. This fixes the "Failed to apply plugin 'org.jetbrains.kotlin.android'"
  build error on AndroidIDE / Code On the Go.

## What's new in version 1.0

- A real settings app: open **eSIM Enabler** and use the two switches.
  - **eSIM support** — turn the spoof on or off.
  - **Copy activation code** — turn the clipboard copy on or off.
- Status line: shows whether the module is on, and the last time an
  activation code was copied.
- Switch changes take effect without another reboot (after the first
  reboot that loads the module).
- Hooks are version-safe: every hook is registered only if the method
  exists on that Android version, and any failure is silent — the module
  never crashes the app it is hooked into.
- New package `com.droidtune.suresim`, new name and icon.

## Install

1. Install the APK (see Building below for the GitHub Actions build).
2. Open **LSPosed / Vector Manager** and enable **eSIM Enabler** for the
   apps that need eSIM (for example your carrier's app). Enabling it for
   Android System is not enough — eSIM is checked inside each app.
3. Reboot your phone.
4. Open **eSIM Enabler** and turn **eSIM support** on.

## Compatibility

- Android 12 and up (`minSdk 31`), libxposed API 101, LSPosed / Vector.
- **Android 17 compatibility is untested.** The module uses no
  Android-17-only APIs, and every hook is guarded so a missing method
  simply does nothing, but no build has been run or tested on Android 17
  yet. Please report what happens on Android 17 before relying on it.
- Settings are shared with the hook through the framework's
  RemotePreferences, so the switches in the app take effect in the
  hooked apps. If the framework cannot read them, the module falls
  back to its original always-on behaviour, so it still works.

## Building

Use Android Studio (or gradle directly) to generate an APK file, or use
the GitHub Actions workflow in `.github/workflows/build-apk.yml`:
push this project to a GitHub repo, open the **Actions** tab, run the
workflow, then download the `android-release` artifact and install
`eSIM-Enabler-v1.0.3-Android.apk`. The build signs with the pinned
debug key in `app/debug.keystore`, so future updates install over it.

## Licence

AGPL-3.0 — see `LICENSE`. Based on SureSIM by thatmarcel; this modified
version is distributed under the same licence, with the original credit
above.
