# BerapaRupiah - Aplikasi Deteksi dan Klasifikasi Nominal Uang Rupiah berbasis Text-to-Speech

**BerapaRupiah** adalah aplikasi Android yang dirancang untuk mendeteksi dan mengenali nominal uang Rupiah emisi 2016 menggunakan teknologi deep dan transfer learning. Aplikasi ini menggabungkan dua model AI canggih, yaitu **YOLO11n** untuk deteksi objek dan **MobileNetV2** untuk klasifikasi gambar. Dengan fitur berbasis **Text-to-Speech (TTS)**, aplikasi ini memberikan kemudahan bagi pengguna dalam mengenali nominal uang dengan cepat dan akurat. 

**Versi APK BerapaRupiah yang siap untuk diunduh dalam smartphone bisa Anda akses pada link berikut:**
https://drive.google.com/drive/folders/1SY_K4Dn4dlIhMTDHc46Bb09N_zlgDVw7?usp=drive_link 

## 🎯 Tujuan Aplikasi

Tujuan utama aplikasi ini adalah untuk membantu seseorang yang mengalami gangguan penglihatan, seperti lansia atau penderita katarak dan glaukoma, untuk memudahkan mereka dalam mengenali nominal uang dan menghindari kesalahan ketika bertransaksi. Dengan aplikasi ini, mereka dapat dengan mudah mengetahui nominal uang yang mereka pegang, sehingga dapat meningkatkan kemandirian mereka dalam bertransaksi dan mengurangi risiko kesalahan dalam penggunaan uang.

Secara teknis aplikasi ini dirancang untuk:
- Mendeteksi nominal uang Rupiah dengan menggunakan input gambar dari galeri maupun kamera Android.
- Menggunakan model YOLO untuk mendeteksi posisi uang (bounding box).
- Menggunakan model MobileNetV2 untuk mengklasifikasikan nominal uang.
- Menampilkan hasil dalam bentuk teks dan suara (TTS).
- Menyimpan riwayat deteksi secara lokal.
<br><br><br>

## 📢 Cara Melakukan Training Model

Langkah training model ini bisa Anda lewati jika Anda hanya ingin menjalankan aplikasi di **Android Studio** tanpa melakukan pelatihan model terlebih dahulu. Namun, jika Anda ingin **melatih model deteksi dan klasifikasi nominal uang** dari awal atau memperbaiki akurasi model, Anda dapat mengikuti langkah-langkah berikut:

### 📍 Persiapan Sebelum Melatih Model
1. **Kode Training**: Unduh seluruh Source Code dari link di bawah ini dan simpan pada local disk `D:\`
   
   https://drive.google.com/drive/folders/1v2ZsCerCFuzcyIneNSBmHmaQgSzNN5Qc

2. **Dataset**: Unduh dataset dari link berikut dan simpan dalam direktori `D:\Projek_Deteksi_Nominal_Uang_Kelompok7\MobileNetV2\dataset_no_cropping`
   
   https://www.kaggle.com/datasets/anidwiastuti/rupiah-banknotes-dataset 

3. **Lingkungan Pengembangan**: Projek ini membutuhkan **python versi 3.10+** dan pastikan semua **library Python** yang dibutuhkan telah terinstal dengan menjalankan:
   ```bash
   pip install -r requirements.txt
   ```
4. **Jupyter Notebook**: Untuk melatih model, kami menggunakan Jupyter Notebook. Anda perlu menginstal Jupyter jika belum terpasang:
   ```bash
   pip install jupyter
   ```


### 💡 Langkah-Langkah untuk Melatih Model
1. Buka jupyter notebook pada path projek (`D:\Projek_Deteksi_Nominal_Uang_Kelompok7\`)
2. Masuk ke folder `YOLO`
3. Disarankan untuk langsung mengeksekusi kode yang ada dalam folder `Training_Final_dengan_Full_Dataset` karena folder `Training_Awal_dengan_529_Gambar` berisi training model YOLO11n dengan hanya sebagian dataset. Menggunakan dataset yang lebih besar di `Training_Final_dengan_Full_Dataset` akan memberikan hasil pelatihan yang lebih baik dan model yang lebih akurat.
4. Urutan eksekusi file dapat dilakukan mengikuti keterangan angka yang terdapat dalam nama file di folder tersebut.
5. Jika proses training YOLO11n telah selesai, maka selanjutnya Anda dapat mulai melakukan training MobileNetV2. 
6. Buka folder `MobileNetV2` kemudian eksekusi file training sesuai keterangan angka yang terdapat dalam nama file di folder tersebut.
7. Jika terjadi error saat mengeksekusi kode, pastikan untuk memeriksa dan menyesuaikan path penyimpanan atau path pengambilan file agar sesuai dengan struktur direktori proyek di sistem Anda.
8. Setelah model YOLO11n dan MobileNetV2 telah selesai di-training, Anda dapat mengkonversi format kedua model tersebut menjadi format `.tflite` agar file model tersebut kompatibel dengan kebutuhan deployment pada sistem Android.
9. Untuk melakukan konversi format Anda dapat mengeksekusi file pada folder `Convert_Model`. Disarankan untuk mengeksekusi file tersebut menggunakan Google Collab untuk performa eksekusi yang lebih baik dan cepat.<br>
Setelah selesai melakukan training dan konversi model, Anda siap untuk deploy aplikasi Android menggunakan model yang telah dilatih. Jika Anda menemui kesulitan atau memerlukan bantuan lebih lanjut, jangan ragu untuk menghubungi tim pengembang atau membuka Issues di repositori ini. Semoga sukses!

<br><br>

## 📲 Cara Menjalankan Aplikasi BerapaRupiah di Android Studio

Aplikasi Deteksi Nominal Uang Kertas (BerapaRupiah) dikembangkan dan dideploy sebagai aplikasi Android menggunakan Android Studio. Proses deployment dilakukan dengan memperhatikan kesesuaian lingkungan pengembangan (development environment) agar aplikasi dapat berjalan dengan baik.

### 1. Lingkungan Pengembangan

- **IDE**: Android Studio
- **Bahasa Pemrograman**: Kotlin / Java
- **Machine Learning Model**: TensorFlow Lite (`.tflite`)
- **Authentication**: Firebase Authentication
- **Minimum Android Version**: Android 7.0 (API Level 24)

### 2. Konfigurasi JDK Android Studio (JDK 17)

Aplikasi ini memerlukan Java Development Kit (JDK) versi 17 untuk proses build dan deployment. Oleh karena itu, konfigurasi JDK harus dipastikan sudah sesuai sebelum menjalankan aplikasi.
## A. Mengatur JDK di Android Studio
1. Buka Android Studio
2. Pilih menu File → Settings
3. Masuk ke Build, Execution, Deployment → Build Tools → Gradle
4. Pada bagian Gradle JDK pilih Embedded JDK (17) atau JDK 17 (jika tersedia)
5. Klik Apply → OK 

## B. Mengunduh JDK 17 (Jika Tidak Tersedia di Android Studio)

Jika pada Android Studio tidak tersedia JDK 17, lakukan instalasi manual dengan langkah berikut.

### Langkah 1: Unduh JDK 17
Buka tautan resmi berikut: 
https://adoptium.net/temurin/releases/?version=17

### Langkah 2: Pilih Versi Windows
* Pilih sistem operasi Windows
* Unduh file installer `.msi` (direkomendasikan karena lebih mudah)
* Jika `.msi` tidak tersedia, file `.zip` juga dapat digunakan

### Langkah 3: Instal JDK 17
1. Klik dua kali file `.msi`
2. Pilih Next → Next → Install
3. Tunggu hingga proses instalasi selesai
4. Klik Finish (Gunakan pengaturan default, tidak perlu mengubah konfigurasi)

### Langkah 4: Verifikasi Instalasi JDK
Secara default, JDK akan terinstal di `C:\Program Files\Eclipse Adoptium\`.
Pastikan terdapat folder dengan nama `jdk-17.x.x`.
Jika folder tersebut ada, maka instalasi JDK berhasil.

## C. Menghubungkan JDK 17 ke Android Studio
1. Buka Android Studio
2. Masuk ke File → Settings → Build, Execution, Deployment → Build Tools → Gradle
3. Pada Gradle JDK:
   * Klik dropdown → pilih Add JDK…
   * Arahkan ke folder `C:\Program Files\Eclipse Adoptium\jdk-17.x.x`
4. Klik OK → Apply → OK

## D. Build Ulang Project
Untuk memastikan konfigurasi berjalan dengan baik:
1. Tutup Android Studio
2. Hapus folder cache Gradle `C:\Users\lenov\.gradle`
3. Buka kembali Android Studio
4. Pilih menu:
   Build → Clean Project
   Build → Make / Assemble Project
Setelah langkah ini, project siap dijalankan.


## 3. Konfigurasi Firebase SHA Fingerprint

Aplikasi ini menggunakan Firebase Authentication untuk proses login. Firebase memerlukan SHA fingerprint dari setiap lingkungan pengembang yang digunakan untuk menjalankan aplikasi.
### Pengambilan SHA dari Laptop Pengembang Lain
Jika project dijalankan di laptop yang berbeda, SHA dari laptop tersebut harus ditambahkan ke Firebase. Langkah pengambilan SHA dilakukan melalui Command Prompt (CMD) sebagai berikut:
1. Buka CMD
2. Masuk ke direktori `.android`: cd C:\Users\NamaPengguna\.android\
    Catatan: `NamaPengguna` disesuaikan dengan nama user masing-masing laptop (dapat dilihat pada folder `C:\Users\`)
3. Jalankan perintah berikut:
   ```bash
   keytool -list -v -keystore debug.keystore -alias androiddebugkey -storepass android -keypass android
   ```
5. Setelah perintah dijalankan, sistem akan menampilkan SHA-1 dan SHA-256
6. SHA tersebut kemudian ditambahkan ke Firebase Console oleh pemilik project
Setelah SHA ditambahkan dan file `google-services.json` diperbarui, fitur login aplikasi akan berfungsi dengan normal.

## 4. Deployment ke Perangkat Android
Setelah konfigurasi JDK berhasil:
1. Hubungkan perangkat Android atau gunakan emulator
2. Jalankan aplikasi melalui Android Studio (`Run`)
3. Aplikasi akan terinstal dan siap digunakan pada perangkat target

## 5. Cara Build
1. Clone repository
2. Buka dengan Android Studio
3. Sync Gradle
4. **Setup Firebase**: Minta **`google-services.json`** dari pengembang utama atau Anda dapat membuat **`google-services.json`** sendiri dengan mengikuti langkah-langkah berikut:
   - Masuk ke [Firebase Console](https://console.firebase.google.com/).
   - Pilih proyek Firebase yang sesuai atau buat proyek baru.
   - Ikuti petunjuk di Firebase untuk menambahkan aplikasi Android dan mendapatkan **`google-services.json`**.
   - Setelah file **`google-services.json`** diperoleh, salin file tersebut ke dalam folder `app/` pada proyek Android Studio.

5. Run aplikasi

## 6. Catatan Deployment
* Kendala yang muncul selama deployment tidak disebabkan oleh kesalahan kode
* Masalah yang terjadi berkaitan dengan:
  * Ketidaksesuaian versi JDK
  * Perbedaan konfigurasi Firebase (SHA fingerprint)
* Setelah konfigurasi disesuaikan, aplikasi dapat berjalan dengan baik di berbagai lingkungan pengembangan
