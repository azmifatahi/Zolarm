# Zolarm

Hardcore Habit & Task Reminder / Alarm app for Android.

## Features

- Exact alarm scheduling with boot reschedule
- Full-screen lock-screen overlay (no easy dismiss)
- Dismissal challenges: **Steps** or **Camera**
- Foreground service + max alarm volume + DND bypass helpers
- Jetpack Compose Material 3 UI (neon dark theme)
- Room database for alarms
- GitHub Actions CI to build debug/release APK

## Requirements

- Android Studio Ladybug / Koala or newer (or JDK 17 + Gradle 8.7)
- minSdk 26 / targetSdk 34
- CameraX, Room, Compose BOM 2024.06.00

## Build (local)

```bash
# Generate wrapper if missing
gradle wrapper --gradle-version 8.7

./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

## Build (GitHub Actions)

Push to `main` / `master` / `develop` or run the **Build Zolarm APK** workflow manually.
Download the artifact **Zolarm-debug-apk**.

## Permissions you must grant on device

1. Notifications  
2. Camera  
3. Physical activity (steps)  
4. Draw over other apps  
5. Exact alarms  
6. Ignore battery optimizations  
7. (Optional) Do Not Disturb access — so the alarm can escape silent mode  

## Project structure

```
Zolarm/
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/zolarm/app/
│       │   ├── MainActivity.kt
│       │   ├── core/          # scheduler, permissions, extras
│       │   ├── data/          # Room entity / dao / repo
│       │   ├── receiver/      # alarm + boot
│       │   ├── service/       # foreground service + player
│       │   ├── challenge/     # steps + camera
│       │   └── ui/            # compose screens, theme, overlay
│       └── res/
└── .github/workflows/build.yml
```

## License

MIT — use freely.
