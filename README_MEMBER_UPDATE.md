# CHCC +Medlem update

The Hjem page now has a **+Medlem – fordeler og innmelding** button that opens an in-app information page. The page contains the supplied Norwegian membership terms and two external browser buttons:

- Opprett kundekonto: https://www.chcc.no/bli-kunde/
- Bli +Medlem: https://diller.app/CARLHEFTYEAS

The existing three bottom tabs, live Google Sheets offers, logo and Om Carl Heftye remain intact. The Kunde barcode page from the newer five-screen mockup is **not yet implemented** in this project.

To build: upload the extracted project contents to the repository root (preserve `.github/workflows/build-apk.yml` and `app/src/main/...` paths). GitHub Actions -> Build Android APK -> download the debug artifact. Do not upload signing keys to GitHub source control.

This project has been statically checked, not compiled in this environment.
