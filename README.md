# Little Cloud

*Ecce nubecula parva quasi vestigium hominis.* (3 Kings 18:44)

A quiet prayer companion for traditional Catholics and Carmelite tertiaries.
It has the Little Office of the Blessed Virgin Mary, the day's collect, a prayer book in Latin, English and Spanish, bells, and a traditional calendar.
The default calendar follows the pre-1955 (Divino Afflatu) rubrics, and the 1962 rubrics are available as an option.
The app works fully offline and has no internet permission.

## Status
- **Android:** 0.6.1, built with Kotlin and Jetpack Compose in the `app/` module.
- **iOS:** planned. The next version splits the shared code into a Kotlin Multiplatform core with Compose Multiplatform.

## Building (Android)
Open the project in Android Studio and click Run. You can also build from the command line with `./gradlew :app:assembleDebug`.

`tools/` contains the scripts and public-domain sources that generated the bundled calendar, Office and prayer data.
