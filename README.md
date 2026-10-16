# Little Cloud

*Ecce nubecula parva quasi vestigium hominis.* (3 Kings 18:44)

A quiet prayer companion for traditional Catholics and Carmelite tertiaries.
It has the Little Office of the Blessed Virgin Mary, the day's collect, a prayer book in Latin, English and Spanish, bells, and a traditional calendar.
The default calendar follows the pre-1955 (Divino Afflatu) rubrics, and the 1962 rubrics are available as an option.
The app works fully offline and has no internet permission.

## Status
- **Android:** 0.7, built from the shared Kotlin Multiplatform code.
- **iPhone:** the same screens run on iOS. Bells are not on iPhone yet.

## Layout
- `app/src/commonMain`: the calendar, the prayers, the Little Office and every screen, shared by both phones.
- `app/src/androidMain`: Android only. This has MainActivity, the bells (alarms, notifications, the ring screen) and printing.
- `app/src/iosMain`: iPhone only. This has the entry point, AirPrint, the prayer-timer notification and settings storage.
- `app/src/commonMain/composeResources`: the bundled data (calendars, Office, collects), the icons and the Forum font.
- `iosApp/`: the Xcode project for the iPhone app.
- `tools/`: scripts and public-domain sources that generated the bundled data.

## Building
**Android:** open the project in Android Studio and click Run. From the command line, use `./gradlew :app:assembleDebug`.

**iPhone (on a Mac):**
1. Install Xcode and Android Studio. Gradle needs Android Studio's Java and Android SDK even for the iPhone build.
2. Open `iosApp/iosApp.xcodeproj` in Xcode.
3. Choose a simulator and click Run. The "Compile Kotlin" step builds the shared code first, which takes a few minutes the first time.
4. If you add or remove Swift files, regenerate the project with `cd iosApp && xcodegen`.

**CI:** every push builds the Android APK and the iPhone app on GitHub Actions. It also runs the tests and takes screenshots on both simulators. Logs and screenshots go to the `ci-logs/android` and `ci-logs/ios` branches.
