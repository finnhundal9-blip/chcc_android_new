# Build the Carl Heftye Android APK without Android Studio

1. Sign in to https://github.com and create a new **private** repository, e.g. `carl-heftye-android`. Do not initialize with a README.
2. Extract this ZIP on your Windows PC. Open the `CarlHeftyeAndroid_GitHub` folder.
3. In the GitHub repository, choose **Add file > Upload files** and upload the **contents** of that folder. Important: GitHub's web uploader may not include hidden `.github` folders; if `.github/workflows/build-apk.yml` is missing after upload, create it using **Add file > Create new file** and paste the workflow contents from your extracted folder.
4. Commit the files to `main`. Make sure `app/`, `build.gradle.kts`, `settings.gradle.kts`, and `.github/workflows/build-apk.yml` are at the repository root (not nested in another folder).
5. Open the repository's **Actions** tab, choose **Build Android APK**, and click **Run workflow** if it did not run automatically.
6. When the workflow is green, open the run and download the artifact named **CarlHeftye-debug-apk**.
7. Unzip the artifact and transfer `app-debug.apk` to your Samsung phone. Open it and allow installation from that source when prompted. Install only your own APK.

## Live Google Sheets offers
The app currently has an empty ENDPOINT in `app/src/main/java/no/carlheftye/tilbud/MainActivity.kt`. Until a Google Apps Script web app is deployed and its `/exec` URL inserted, Tilbud shows a configuration message. `Code.gs` contains the sample read-only endpoint. In Google Sheets select **Extensions > Apps Script**, paste `Code.gs`, then **Deploy > New deployment > Web app**, execute as your account, access `Anyone` **only if the sheet contains approved public product data**. Paste the `/exec` URL into ENDPOINT and commit to trigger a new APK build. Do not put any private API tokens into the Android source.

## Notes
This is a debug APK for testing, not a signed release package for Play Store distribution. The source has not been compiled in this environment; GitHub Actions will report any build errors. Product image links must be accessible from the phone over HTTPS. Google Sheets is the Android data source; Glide Free's internal tables do not automatically synchronize with it.
