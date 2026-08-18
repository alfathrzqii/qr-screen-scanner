# 🤖 AGENTS.md — Development Guidelines & Architecture Reference

Dokumen ini berisi panduan teknis, aturan operasional, dan arsitektur aplikasi **QR Screen Scanner** untuk setiap AI Agent yang bekerja di repositori ini.

---

## ⚠️ ATURAN UTAMA & WAJIB DIPATUHI (CRITICAL RULE)

> [!CAUTION]
> **DILARANG MENJALANKAN GRADLE BUILD SECARA LOKAL**
> - Laptop/sistem pengguna memiliki spesifikasi terbatas (CPU/RAM rendah).
> - **JANGAN PERNAH** menjalankan `./gradlew build`, `./gradlew assemble`, `gradle`, atau perintah kompilasi Gradle lokal lainnya di terminal pengguna.
> - **SEMUA PROSES BUILD & PACKAGING DIJALANKAN DI GITHUB ACTIONS CLOUD CI/CD**.
> - Tugas AI: Menulis kode yang benar, menjaga arsitektur, melakukan commit ke Git, dan meminta pengguna untuk melakukan `git push` atau `git tag`.

---

## 📱 Ringkasan Proyek

- **Nama Aplikasi**: QR Screen Scanner
- **Application ID / Namespace**: `dev.alfathrzqii.qrscreenscanner`
- **Tujuan**: Memindai kode QR yang muncul langsung di layar smartphone (Instagram post, TikTok, WhatsApp, web browser) via shortcut **Quick Settings Tile (Control Center)** tanpa perlu screenshot atau menyimpan gambar secara manual.
- **Bahasa & Framework**: Kotlin + Jetpack Compose (Material 3 Expressive)
- **Komponen Utama**:
  - Offline ML: `com.google.mlkit:barcode-scanning`
  - Database: Room Database (Local History)
  - Screen Capture: `MediaProjection` API + `VirtualDisplay` + `ImageReader`
  - Desain: Material You / Dynamic Color (Android 12+) + Material 3 Expressive components

---

## 🏛️ Arsitektur & Aturan Komponen Penting

### 1. MediaProjection & Foreground Service (Wajib untuk Android 14+ / API 34+)
- Android 14 melarang pemanggilan `MediaProjectionManager.getMediaProjection(...)` di luar Foreground Service yang aktif.
- Seluruh lifecycle tangkapan layar diatur di [`MediaProjectionService.kt`](file:///app/src/main/java/dev/alfathrzqii/qrscreenscanner/service/MediaProjectionService.kt) setelah `startForeground(..., ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION)` aktif.
- Callback `mediaProjection.registerCallback(...)` **wajib** didaftarkan sebelum membuat `VirtualDisplay`.
- Hasil scan dikirim kembali ke UI melalui Kotlin `SharedFlow` (`MediaProjectionService.scanEvents`).

### 2. ScreenCaptureManager & Buffer Safety
- Wajib memberikan jeda singkat (~350ms) sebelum menangkap frame agar animasi penutupan dialog perizinan OS (*system scrim*) selesai dan tidak menutupi layar.
- Wajib memanggil `buffer.rewind()` pada `Image.Plane.buffer` sebelum `copyPixelsFromBuffer(buffer)` untuk mencegah buffer underflow / blank bitmap.
- Menggunakan filter frame hitam transisi (`isBitmapAllBlank`) agar frame awal yang kosong tidak diproses ke ML Kit.

### 3. Smart Action Recognition ([`QrTypeParser.kt`](file:///app/src/main/java/dev/alfathrzqii/qrscreenscanner/util/QrTypeParser.kt))
Aplikasi mendukung pengenalan cerdas (*smart actions*):
- **QRIS / Digital Payment**: Mem-parsing payload standar EMVCo / QRIS Indonesia (Merchant name, city, nominal dinamis/statis) ➔ Tombol aksi buka app pembayaran digital.
- **WhatsApp Direct**: Mem-parsing format URL `wa.me`, `api.whatsapp.com`, `whatsapp://` ➔ Tombol aksi kirim pesan langsung via WhatsApp.
- **Wi-Fi Auto Connect**: Mem-parsing string `WIFI:S:...;T:...;P:...;;` ➔ Tombol koneksi otomatis via `WifiNetworkSuggestion` / `WifiManager` + toggle lihat password.
- **Contact Card**: Mem-parsing vCard / MeCard ➔ Tombol simpan kontak langsung ke buku telepon Android.
- **Web URL, Telepon, SMS, Geo Lokasi, Teks Biasa**.

### 4. Desain & Tema (Material 3 Expressive)
- Selalu gunakan `QrScreenScannerTheme` dengan dukungan `dynamicDarkColorScheme` / `dynamicLightColorScheme` pada Android 12+.
- Gunakan surface container bertingkat (`surfaceContainerLow`, `surfaceContainer`, `surfaceContainerHigh`, `surfaceContainerHighest`).
- Gunakan kontur asimetris ekspresif (`RoundedCornerShape(topStart = 28.dp, bottomEnd = 28.dp)`), status pill berindikator titik aktif, dan hindari template generic / AI slop.

---

## 🚀 CI/CD & Alur Rilis GitHub Actions

| File Workflow | Trigger | Tanggung Jawab |
|---|---|---|
| [`.github/workflows/build-apk.yml`](file:///e:/alfath/dev/qr-screen-scanner/.github/workflows/build-apk.yml) | Push ke branch `main`, PR | Memvalidasi kompilasi debug dan meng-upload artifact debug untuk pengujian dev. **TIDAK membuat GitHub release.** |
| [`.github/workflows/release.yml`](file:///e:/alfath/dev/qr-screen-scanner/.github/workflows/release.yml) | Push tag `v*` (contoh: `v1.0.0`) atau `workflow_dispatch` | Men-decode keystore rahasia dari `KEYSTORE_BASE64`, membuat signed production APK (`QR-Screen-Scanner-v1.0.0.apk`), dan mempublikasikan **GitHub Release resmi** (hanya melampirkan file release APK). |

---

## 🔒 Folder `temp/` & Pengelolaan Keystore
- Folder `temp/` **wajib 100% di-ignore** di [`.gitignore`](file:///e:/alfath/dev/qr-screen-scanner/.gitignore) dan tidak boleh ter-push ke GitHub.
- Folder ini digunakan secara lokal untuk:
  - `temp/release.jks` (Production signing keystore).
  - `temp/keystore_base64.txt` (Base64 string untuk secret GitHub).
  - `temp/release_notes.md` (Draft catatan rilis markdown untuk copy-paste manual ke GitHub Releases).

---

## 📜 Checklist Saat Menambah Fitur / Memperbaiki Bug

1. **Edit/Tambah Kode**: Pastikan kode mematuhi Android modern (Android 14+, Coroutines, Jetpack Compose M3).
2. **Cek Lint / Dependensi**: Jika ada dependensi baru, pastikan didaftarkan di `gradle/libs.versions.toml` dan `app/build.gradle.kts`.
3. **Commit ke Git**: Buat commit pesan yang jelas mengikuti Conventional Commits (`feat: ...`, `fix: ...`, `ci: ...`).
4. **Push ke GitHub**: Minta user menjalankan `git push` (atau `git tag vX.X.X && git push origin vX.X.X` jika rilis) agar build berjalan di GitHub Actions cloud.
