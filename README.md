# Little Cloud

*Ecce nubecula parva quasi vestigium hominis.* (3 Kings 18:44)

A quiet prayer companion for any Catholic who wants a more contemplative life of prayer, modeled on the traditional rule of the Third Order of Carmel.
It works fully offline, has no internet permission, no ads and no accounts, and collects nothing. It is free.

## What it has
- **The Little Office of the Blessed Virgin Mary:** every hour from Matins to Compline in Latin, English or both, with its seasonal forms. Directions are marked in red, each hour can be marked as said, and any hour can be printed or saved as a PDF.
- **A traditional calendar** from 2026 to 2050: the feast of the day with its rank, colour and commemorations; fast and abstinence under the Code of 1917 or the American rules of 1951; Our Lady of Guadalupe; and an optional Carmelite proper.
- **The day's Mass:** the collect, Epistle and Gospel in Latin and English, which can be printed or saved as a PDF.
- **A prayer book** of traditional prayers that you can search and edit, with room for your own, and a rule of morning and evening prayer you build and order yourself.
- **Mental prayer** in the Carmelite way, with a timer that ends with a bell.
- **Bells** for the Angelus (the Regina Cæli in Eastertide), the hours and night prayers, with the wooden clapper in the Sacred Triduum.

## Rubrics
By default it follows the rubrics of Pope Pius XII (the simplification of 1955 and the restored Holy Week of 1956), as kept by the CMRI. The rubrics before 1955 (Divino Afflatu) are available in Settings.
The calendar, collects, readings and Little Office all follow the rubrics chosen.
A 1962 option has been set aside for now. Its calendar code remains in the source, but it is not offered in the app.

## Languages
Latin and English. Spanish is in the code but switched off (`SPANISH_ENABLED` in `Store.kt`) until a traditional public-domain Spanish translation is on file.

## Status
- **Android:** 1.0-beta, in closed testing before release on Google Play.
- **iPhone:** the same screens run on iOS from the shared Kotlin Multiplatform code. On iPhone the bells come as notifications, so they cannot ring through silent mode.

## Sources
- **Calendar, collects, Little Office, Mass readings (Latin):** the Divinum Officium Project.
- **Readings (English):** the Douay-Rheims Bible (Challoner), Project Gutenberg #8300, taken by each citation.
- **Prayers:** public-domain manuals, listed in `tools/`.

## Layout
- `app/src/commonMain`: the calendar, the prayers, the Little Office and every screen, shared by both phones.
- `app/src/androidMain`: Android only. This has MainActivity, the bells (exact alarms, so battery savers do not hold them back, plus notifications and the ring screen) and printing.
- `app/src/iosMain`: iPhone only. This has the entry point, the bells as notifications, AirPrint and settings storage.
- `app/src/commonMain/composeResources`: the bundled data (calendars, Office, collects, readings), the icons and the Forum font.
- `iosApp/`: the Xcode project for the iPhone app.
- `tools/`: scripts and public-domain sources that generated the bundled data.
- `site/`: the project's web pages (home, privacy policy, support).
- `store/`: the store listing text and graphics.

## Building
**Android:** open the project in Android Studio and click Run. From the command line, use `./gradlew :app:assembleDebug`.

**iPhone (on a Mac):**
1. Install Xcode and Android Studio. Gradle needs Android Studio's Java and Android SDK even for the iPhone build.
2. Open `iosApp/iosApp.xcodeproj` in Xcode.
3. Choose a simulator and click Run. The "Compile Kotlin" step builds the shared code first, which takes a few minutes the first time.
4. If you add or remove Swift files, regenerate the project with `cd iosApp && xcodegen`.

**CI:** every push builds the Android APK and the iPhone app on GitHub Actions, runs the tests and takes screenshots on both simulators. When the upload-key secrets are set, it also builds the signed release bundle for Google Play. Logs and screenshots go to the `ci-logs/android` and `ci-logs/ios` branches.

## License
- **Code:** GNU General Public License v3.0 or later. See [`LICENSE`](LICENSE).
- **Original writing** (descriptions, help text, translations made for this project): [Creative Commons Attribution-ShareAlike 4.0](https://creativecommons.org/licenses/by-sa/4.0/).
- **Public-domain texts** (the Little Office, the Douay-Rheims, the older prayer manuals) remain in the public domain. The calendar, collect and Office data drawn from the [Divinum Officium Project](https://github.com/DivinumOfficium/divinum-officium) is used under the MIT License; its notice is in [`licenses/divinum-officium-MIT.txt`](licenses/divinum-officium-MIT.txt).

## Name and icon
"Little Cloud" and its icon (the little cloud rising over the sea) identify this app and are not covered by the licenses above.
You are free to fork and change the code, but please give your version a different name and icon, so that no one mistakes a changed text or calendar for this one.

## Contact
LittleCloudProject@proton.me
