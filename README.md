# Diskusiku - AI Discussion Knowledge Base & Archive

Aplikasi Android modern dengan **Material Design 3 Expressive (2026)** untuk mencatat, mengarsipkan, dan menelusuri riwayat diskusi riset bersama **OpenRouter AI** & Gemini secara lokal di perangkat Anda (Offline-first via Room Database).

---

## 🚀 Fitur Utama
1. **Dukungan OpenRouter Multi-Model**:
   - Pilihan model gratis tanpa batas (**Free Tier**): `meta-llama/llama-3.3-70b-instruct:free`, `deepseek/deepseek-r1:free`, `google/gemini-2.0-flash-lite-preview-02-05:free`, `qwen/qwen-2.5-coder-32b-instruct:free`, `mistralai/mistral-7b-instruct:free`.
   - Dukungan model tingkat lanjut (**$100 Tier / Kredit**): `google/gemini-2.5-flash`, `openai/gpt-4o-mini`, `anthropic/claude-3.5-sonnet`.
   - Pemilihan model fleksibel langsung dari aplikasi via dialog konfigurasi.
2. **Material Design 3 Expressive (2026)**:
   - `NavigationSuiteScaffold` adaptif (Mobile, Tablet, Layar Lipat).
   - Bentuk dinamis *shape-morphing* & tombol *hug-content*.
3. **Pencarian Cerdas Gaya Google**:
   - Pencarian instan teks lengkap terhadap judul, kueri, tag, dan transkrip dialog AI.
4. **Penyimpanan Lokal Penuh (Offline-First via Room Database)**:
   - Data tersimpan aman di database SQLite lokal perangkat (`diskusiku_database`).

---

## 🌐 Setup di Dashboard OpenRouter

Berdasarkan halaman dashboard OpenRouter Anda (`openrouter.ai`):
1. **Menu Routing**: **Tidak perlu diubah (Skip / Biarkan Default)**.
2. **Membuat Kunci API**:
   - Di sidebar kiri, klik menu **API Keys**.
   - Klik tombol **Create Key**.
   - Beri nama (misalnya: `Diskusiku-App`).
   - Pada kolom *Credit limit*: Kosongkan atau biarkan default.
   - Klik **Create** dan salin kuncinya yang berawalan `sk-or-v1-...`.

---

## 🔑 Cara Setting di APK & GitHub Secrets

### Opsi A: Setting Langsung di Aplikasi (Paling Cepat untuk Prototype)
1. Pasang APK hasil build di smartphone Anda.
2. Buka tab **Diskusi AI**, lalu klik tombol icon **Tune / Setelan** (atau banner oranye di bagian atas).
3. Tempelkan OpenRouter API Key Anda (`sk-or-v1-...`).
4. Pilih model AI yang Anda inginkan (misal **Llama 3.3 70B** atau **DeepSeek R1**).
5. Klik **Simpan & Terapkan**. Kunci dan model tersimpan secara lokal dan langsung aktif.

### Opsi B: Setting via GitHub Secrets (Otomatis Tertanam di APK)
1. Buka repositori Anda di GitHub.
2. Masuk ke **Settings** > **Secrets and variables** > **Actions**.
3. Klik **New repository secret**.
4. Isi data berikut:
   - **Name**: `OPENROUTER_API_KEY`
   - **Value**: Tempelkan kunci `sk-or-v1-...` dari OpenRouter Anda.
5. *(Opsional)* Jika Anda juga memiliki Gemini API key, Anda dapat menambahkan secret `GEMINI_API_KEY`.
6. Klik **Add secret**.
7. Masuk ke tab **Actions** di GitHub, pilih **Build Android APK** lalu klik **Run workflow**. File APK di tab **Artifacts** akan otomatis memiliki kunci OpenRouter yang tertanam.
