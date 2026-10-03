# Diskusiku - AI Discussion Knowledge Base & Archive

Aplikasi Android modern dengan **Material Design 3 Expressive (2026)** untuk mencatat, mengarsipkan, dan menelusuri riwayat diskusi riset bersama Gemini AI secara lokal di perangkat Anda.

## 🚀 Fitur Utama
1. **Material Design 3 Expressive**:
   - `NavigationSuiteScaffold` adaptif untuk ponsel, tablet, dan layar lipat.
   - Bilah navigasi modern bergaya short bottom bar.
   - Komponen dinamis *shape-morphing* & tombol *hug-content*.
   - Kartu asimetris dan kartu obrolan ekspresif.
2. **Pencarian Cerdas Gaya Google**:
   - Pencarian instan teks lengkap terhadap judul, kueri awal, tag, dan transkrip dialog AI.
   - Chip filter kategori (*Semua, UI/UX, Android, AI & ML, Ide Produk*).
3. **Penyimpanan Lokal Penuh (Offline-First via Room Database)**:
   - Seluruh data diskusi, sintesis ringkasan, dan status penanda buku (*bookmark*) disimpan di database lokal SQLite Android melalui Room ORM.
   - Aman dan dapat diakses kapan saja tanpa perlu bergantung pada koneksi internet.
4. **Integrasi Gemini 3.5 Flash API**:
   - Respon dialog cerdas dan auto-sintesis ringkasan 2–3 kalimat langsung dari transkrip percakapan.
   - Penanganan API key aman menggunakan Secrets Gradle Plugin dan `BuildConfig`.

---

## 🔑 Langkah Implementasi Gemini API saat Build ke APK

### 1. Mendapatkan API Key
1. Buka [Google AI Studio](https://aistudio.google.com/).
2. Buat API Key baru pada menu **Get API key**.

### 2. Cara Kerja Injeksi Kunci API
Kunci API **TIDAK PERNAH** ditulis langsung dalam kode sumber. Proyek ini menggunakan **Secrets Gradle Plugin**:
- Template mendefinisikan placeholder di `.env.example`: `GEMINI_API_KEY=MY_GEMINI_API_KEY`.
- Saat build, file `.env` dibaca dan menghasilkan variabel aman `BuildConfig.GEMINI_API_KEY`.
- Pada runtime, `GeminiRepository` memvalidasi kunci tersebut sebelum memanggil endpoint resmi `gemini-3.5-flash`.

### 3. Konfigurasi di AI Studio (Pengujian Langsung)
- Buka panel **Secrets** di AI Studio UI.
- Masukkan variabel `GEMINI_API_KEY` beserta kunci asli Anda.

### 4. Konfigurasi di GitHub Repository (Untuk GitHub Actions)
Saat Anda mem-push proyek ini ke GitHub:
1. Buka repositori GitHub Anda.
2. Masuk ke **Settings** > **Secrets and variables** > **Actions**.
3. Klik **New repository secret**.
4. Isi **Name**: `GEMINI_API_KEY`
5. Isi **Value**: Kunci API Google AI Studio Anda.
6. Klik **Add secret**.

Workflow GitHub Actions (`.github/workflows/build-apk.yml`) akan otomatis menyuntikkan secret ini ke dalam file `.env` saat mengompilasi APK.

---

## 🤖 GitHub Actions CI/CD (Build APK Otomatis)

Workflow `.github/workflows/build-apk.yml` telah disediakan dan bekerja secara otomatis:
1. **Otomasi Keystore**: Menghasilkan keystore penandatanganan release RSA 2048-bit secara mandiri di runner CI (`keytool`).
2. **Kompilasi APK**: Menjalankan `./gradlew assembleDebug assembleRelease`.
3. **Penyimpanan Artifact**: Mengunggah APK Debug dan Release langsung ke tab **Actions > Artifacts** repositori GitHub Anda, siap diunduh dan dipasang pada perangkat Android.

---

## 🛠️ Build Manual Secara Lokal

```bash
# Salin template environment dan isi API key Anda
cp .env.example .env
echo "GEMINI_API_KEY=KUNCI_API_ANDA" > .env

# Build APK Debug
./gradlew assembleDebug

# Build APK Release
./gradlew assembleRelease
```
File APK akan berada di folder `app/build/outputs/apk/`.
