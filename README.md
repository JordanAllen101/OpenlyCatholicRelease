# Openly Catholic

Openly Catholic is an Android app for daily prayer and Catholic spiritual practices.

## ✨ Features

### 📿 Rosary
- Complete Rosary prayers with all mysteries
- Daily mystery selection based on the day of the week
- Easy-to-follow guided prayers
- Beautiful, distraction-free interface
- **Audio Rosary**: Add custom MP3 audio files to pray along with

### 📖 Daily Missal
- Access to daily Mass readings
- Responsorial psalms with proper formatting
- Alleluia verses
- Gospel readings
- Liturgical calendar integration

### ✝️ Examination of Conscience
- **Full Examination**: Comprehensive self-reflection across all Ten Commandments
- **Quick Examination**: Fast checklist for serious sins
- **Custom Sins**: Add your own custom sins if predefined ones aren't sufficient
- **Confession Guide**: Step-by-step guide through the sacrament of confession
- **Confession History**: Track your past confessions

### ⏰ Confession Reminders
- Optional weekly reminder notifications
- Customizable settings
- Gentle prompts to prepare for confession
- Fully integrated with examination of conscience
- **Penance Reminders**: Separate reminders for completing penance

## 🎵 Audio Rosary Guide

The Audio Rosary feature allows you to pray along with custom MP3 audio files. Here's how to set it up:

### Setting Up Audio Rosary

1. **Prepare Your Audio Files**
   - Gather or record MP3 files for each rosary prayer
   - Name them according to the naming convention below
   - Place all files in a single folder on your device

2. **Enable Audio Rosary**
   - Open the Rosary screen
   - Tap the 🎵 (music note) button in the top-right
   - Tap "Select Folder" to choose your audio files folder
   - Enable the "Enable Audio Rosary" toggle
   - Tap "Save Settings"

3. **Using Audio Playback**
   - When audio is enabled, a play/pause button appears below the prayer text
   - Tap "▶ Play" to start audio for the current step
   - Tap "⏸ Pause" to stop playback
   - Audio automatically advances as you move through rosary steps

### Audio File Naming Convention

Your MP3 files must be named exactly as follows:

| Step | File Name | Description |
|------|-----------|-------------|
| 1 | `sign_of_cross.mp3` | Sign of the Cross |
| 2 | `apostles_creed.mp3` | Apostles' Creed |
| 3 | `our_father.mp3` | Our Father (used multiple times) |
| 4 | `hail_mary.mp3` | Hail Mary (used multiple times) |
| 5 | `glory_be.mp3` | Glory Be (used multiple times) |
| 6 | `mystery_1.mp3` | First Mystery |
| 10 | `mystery_2.mp3` | Second Mystery |
| 15 | `mystery_3.mp3` | Third Mystery |
| 20 | `mystery_4.mp3` | Fourth Mystery |
| 25 | `mystery_5.mp3` | Fifth Mystery |
| 9, 14, 19, 24, 29 | `fatima.mp3` | Fatima Prayer (after each decade) |
| 30 | `hail_holy_queen.mp3` | Hail Holy Queen |

**Note**: Some files are reused multiple times (Our Father, Hail Mary, Glory Be, Fatima). You only need one copy of each.

### Tips

- Use high-quality MP3 files (128kbps or higher recommended)
- Keep audio files relatively short (30 seconds to 2 minutes per prayer)
- Ensure consistent volume levels across all files
- Test playback before using in prayer
- The app uses Android's Storage Access Framework to access your files securely

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK 33
- Minimum SDK: 21 (Android 5.0 Lollipop)

### Building the App

Open the project directory in Android Studio, or build from a terminal.

Windows:
   ```bash
   .\gradlew.bat :app:assembleDebug
   ```

macOS/Linux:
   ```bash
   ./gradlew :app:assembleDebug
   ```

The debug APK is written to `app/build/outputs/apk/debug/`.

## 🏗️ Architecture

- **Language**: Java
- **UI Framework**: Android XML layouts with Fragments
- **Navigation**: Fragment transactions with back stack management
- **Data Storage**: SharedPreferences and JSON assets
- **Background Tasks**: WorkManager for scheduled reminders
- **Build System**: Gradle

### Project Structure

```
app/
├── src/main/
│   ├── java/com/openlycatholic/app/
│   │   ├── examination/       # Examination of conscience features
│   │   ├── home/              # Home screen navigation
│   │   ├── missal/            # Daily readings and missal
│   │   ├── rosary/            # Rosary prayers and mysteries
│   │   └── MainActivity.java  # Main activity
│   ├── res/
│   │   ├── layout/            # XML layouts
│   │   ├── values/            # Colors, strings, etc.
│   │   └── mipmap/            # App icons
│   └── AndroidManifest.xml
└── build.gradle
```

## 🎨 Customization

### Colors
The app uses a dark theme with gold accents. Colors can be customized in `res/values/colors.xml`:
- Primary: `#D4AF37` (Gold)
- Background: `#1a1a2e` (Dark blue)
- Text: White and secondary gray

### Features
Each feature is modular and can be extended:
- Add new examination categories in `ExaminationData.java`
- Customize rosary mysteries in the rosary package
- Modify reminder frequency in `ConfessionReminderFragment.java`

## 📝 License

This checkout does not include a `LICENSE` file. Confirm the project's license with its maintainer before redistributing the source.
