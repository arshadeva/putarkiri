# Ocalix Android — GitHub Actions build

Repository ini hanya untuk aplikasi Android Kotlin + WebView. Node.js backend dikelola terpisah di server panel.

## Build APK otomatis
1. Buat repository GitHub baru dan upload seluruh isi folder ini ke root repository.
2. Pastikan default branch bernama `main` atau `master`.
3. (Direkomendasikan) Buka Settings → Secrets and variables → Actions → Variables.
4. Tambahkan variable `OCALIX_BACKEND_URL`, misalnya `https://music.domainkamu.com` tanpa slash di akhir.
5. Buka tab Actions → `Ocalix Android APK` → Run workflow.
6. Setelah job hijau, buka ringkasan run dan download artifact `Ocalix-Android-debug-...`.

Workflow juga berjalan otomatis ketika perubahan pada source Android atau workflow di-push ke `main` / `master`.

## Koneksi backend
- Jika `OCALIX_BACKEND_URL` diset saat build, URL tersebut menjadi alamat default yang disimpan pada first launch.
- Jika variable kosong, aplikasi meminta URL backend ketika pertama dibuka. URL disimpan pada perangkat.
- Android Emulator lokal: `http://10.0.2.2:7777`.
- HP fisik tanpa domain: gunakan `http://IP-PUBLIK:PORT` dari Allocation server panel. Pastikan port tersebut dialokasikan dan dapat diakses dari internet. Jangan gunakan alamat IP lokal seperti `127.0.0.1`, `10.x.x.x`, atau `192.168.x.x` untuk server publik.

## Versi build
- Application ID: `com.ocalix.music`
- Version: `2.0.0` (versionCode 2)
- Minimum Android: API 26 / Android 8.0
- Compile/Target SDK: 35
- JDK: 17
- Gradle: 8.9

Artifact workflow adalah **Debug APK** yang otomatis ditandatangani debug key untuk instalasi dan pengujian. Untuk rilis Play Store atau distribusi produksi, siapkan signing keystore dan workflow release terpisah.

## Yang ada di paket Android
- Kotlin `MainActivity` dan JavaScript bridge ke service Android.
- `PlaybackKeepAliveService` untuk foreground notification saat pemutaran.
- WebView, izin internet, notifikasi, dan media playback service.
- Web player assets lokal sebagai fallback/source bundle; aplikasi saat ini membuka URL backend server agar UI dan API tetap satu origin.
- GitHub Actions workflow untuk build dan upload APK artifact.
