# 📱 QR Screen Scanner for Android

A sleek, offline-first Android utility app that scans QR codes directly from your smartphone screen via the **Quick Settings Tile (Control Center)** without taking manual screenshots or leaving your current app.

---

## ✨ Key Features

- ⚡ **One-Tap Quick Settings Tile**: Access instant screen scanning from the Android Control Center / Notification Shade anytime.
- 🚀 **100% Offline Google ML Kit**: Lightning-fast barcode & QR recognition without requiring an active internet connection.
- 🎯 **Multi-QR Visual Highlight Overlay**: If a screen contains multiple QR codes (e.g. social media feeds or split screen), clickable bounding boxes let you choose which QR to open.
- 📋 **Smart Action Bottom Sheet**: Automatically detects URLs, Wi-Fi networks, Phone numbers, Emails, and plain text with one-click actions (*Open in Browser, Copy, Share*).
- 📳 **Haptic Feedback**: Tactile feedback on scan completion and error states.
- 🗄️ **Local Room History**: Searchable and manageable log of all past scanned QR codes.
- ☁️ **GitHub Actions Cloud CI/CD**: Automatically compiles the app and generates ready-to-install `.apk` files directly in GitHub Actions.

---

## 🏗️ Architecture & Tech Stack

- **UI**: Jetpack Compose + Material 3 (Dark Theme & Dynamic Color)
- **Language**: Kotlin 1.9 + Coroutines & StateFlow
- **ML / AI**: Ultra-lightweight Google ML Kit Barcode Scanning (`play-services-mlkit-barcode-scanning`)
- **Screen Capture**: Android `MediaProjectionManager`, `VirtualDisplay`, `ImageReader`
- **Background Service**: `MediaProjection` Foreground Service (Android 14/15 compliant)
- **System Integration**: `android.service.quicksettings.TileService`
- **Database**: Jetpack Room Database

---

## 🚀 How to Build & Install via GitHub Actions (Zero Local Load)

Since this repository is equipped with GitHub Actions CI/CD, you don't need Android Studio or heavy local Gradle builds.

### 1. Push to your GitHub Repository:
```bash
git remote add origin https://github.com/<your-username>/<your-repo-name>.git
git branch -M main
git push -u origin main
```

### 2. Download APK:
1. Open your GitHub Repository in your browser or phone.
2. Navigate to the **Actions** tab.
3. Click on the latest workflow run: **"Build Android APK"**.
4. Scroll down to the **Artifacts** section and download `QR-Screen-Scanner-Debug-APK`.
5. Install the APK on your Android smartphone!

---

## 📲 How to Use

1. Open the **QR Screen Scanner** app once to ensure permissions are ready.
2. Swipe down from the top of your phone screen twice to expand the Quick Settings / Control Center.
3. Tap the **Edit (Pencil)** icon to customize your tiles.
4. Drag the **"Scan Screen QR"** tile into your active tiles list.
5. Whenever you see a QR code on Instagram, TikTok, Twitter, or WhatsApp, simply pull down Control Center and tap **"Scan Screen QR"**!
