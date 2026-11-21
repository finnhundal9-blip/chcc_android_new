# CHCC Fresh — Android

Native Kotlin / Jetpack Compose app for Carl Heftye Cash & Carry.

## Features
- Hjem: CHCC logo, green welcome card, offer button, opening hours.
- Tilbud: two-column catalogue with live Google Apps Script JSON feed, product images, prices, search, dynamic category filters and refresh.
- Om Carl Heftye: company information, certification statement, hours, tap-to-dial contact numbers.

## Build without Android Studio
1. Extract this ZIP.
2. Upload the **contents** of this folder to the root of your existing GitHub repository. In particular replace `app/src/main/java/no/carlheftye/tilbud/MainActivity.kt` and ensure `app/src/main/res/drawable/chcc_logo.png` exists.
3. Replace root `build.gradle.kts`, `settings.gradle.kts` and `.github/workflows/build-apk.yml` with these versions if yours differ. Do not upload the files inside an extra nested directory.
4. Commit changes to `main`. Go to **Actions > Build Android APK**. If needed click **Run workflow**.
5. On successful build download artifact `CHCC-Fresh-debug-apk`, extract, install `app-debug.apk` on Android.

## Data
The existing Google Apps Script /exec URL is set in MainActivity.kt. It returns `Produkt`, `Beskrivelse`, `Pris`, `BildeUrl`, `Gruppe`, `Aktiv`. The app reads active offers only. Image URLs must be public HTTPS. Data feed is public; never include private customer or business data.

## Notes
- The logo is bundled as an Android PNG drawable.
- Network refresh happens when entering Tilbud and on tapping Oppdater.
- No offline persistence; last loaded offers remain visible while the app process is alive.
- Source code has been checked structurally, but no Android SDK is available here to compile an APK.
