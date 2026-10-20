# Openly Catholic

An Android companion for daily Catholic prayer and spiritual life.

Openly Catholic brings prayer, daily readings, and practical tools for preparing for Confession together in one focused app.

## Features

- **Rosary:** Guided prayers, daily mystery selection, mystery artwork, and optional playback using your own audio files.
- **Daily Missal:** Readings for the day, including the responsorial psalm and Gospel.
- **Examination of conscience:** Full and quick examinations, custom entries, and confession history.
- **Confession guide:** A step-by-step guide for the Sacrament of Reconciliation.
- **Reminders:** Optional confession and penance reminders.
- **App updates:** Check for published releases from the app's Settings screen.

## Requirements

- Android Studio with Android SDK Platform 33
- JDK 17
- Android 5.0 (API 21) or later

## Build

Open this project folder in Android Studio, or build from a terminal:

**Windows**

```powershell
.\gradlew.bat :app:assembleDebug
```

**macOS / Linux**

```sh
./gradlew :app:assembleDebug
```

The debug APK is generated at `app/build/outputs/apk/debug/`.

To build a signed release APK, configure the release keystore and passwords locally. Do not commit the keystore or signing credentials. Release signing expects:

- `~/android-signing/OpenlyCatholic-release.jks`
- Gradle user properties `OPENLY_CATHOLIC_STORE_PASSWORD` and `OPENLY_CATHOLIC_KEY_PASSWORD`

Then run `:app:assembleRelease` from Android Studio or the Gradle wrapper. Release builds require the original signing key to update an existing installation.

## Project layout

```text
app/src/main/
├── assets/       Bundled reading data
├── java/         Android application code
├── res/          Layouts, images, icons, and themes
└── AndroidManifest.xml
```

The app is written in Java and uses Android XML layouts and fragments. Settings and local records are stored on-device; WorkManager schedules reminders.

## Audio Rosary

Audio Rosary uses files you select from your device. Place the supported MP3 files in one folder and select it from the Rosary audio settings:

| Prayer or step | File name |
| --- | --- |
| Sign of the Cross | `sign_of_cross.mp3` |
| Apostles' Creed | `apostles_creed.mp3` |
| Our Father | `our_father.mp3` |
| Hail Mary | `hail_mary.mp3` |
| Glory Be | `glory_be.mp3` |
| Fatima Prayer | `fatima.mp3` |
| Hail Holy Queen | `hail_holy_queen.mp3` |
| Mystery announcements | `mystery_1.mp3` through `mystery_5.mp3` |

Common prayers are reused across the decades; one copy of each file is sufficient.

## License and third-party materials

Project code authored for Openly Catholic is licensed under the GNU General Public License, version 3 or (at your option) any later version. See [LICENSE](LICENSE).

Bundled Bible readings, translation datasets, artwork, and other third-party materials are not relicensed by the project license. Their respective copyright notices, permissions, and terms continue to apply; check the source and licensing information for each item before redistributing it.
