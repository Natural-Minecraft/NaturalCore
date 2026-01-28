# 🧪 Panduan Testing NaturalCore v2.0 (Ultimate Features)

Ikuti langkah-langkah di bawah ini untuk memastikan seluruh fitur premium berjalan dengan sempurna di server kamu.

---

## 🛡️ 1. Staff Suite (Staff Mode)
**Tujuan**: Memastikan staff bisa memantau server tanpa terlihat dan mengelola player dengan cepat.

- [ ] **Vanish Test**: Ketik `/staffmode` atau `/sm`. Pastikan kamu menghilang dari tab list player lain dan muncul pesan join/quit palsu.
- [ ] **Staff Chat**: Ketik `/sc Halo tim!`. Pastikan hanya pemain dengan permission `naturalsmp.staff` yang bisa melihat pesan tersebut.
- [ ] **Dashboard GUI**: Ketik `/staff`.
    - Klik kiri kepala player untuk Teleport.
    - Klik kanan untuk cek info lengkap (Whois).
    - Shift+Klik untuk buka menu Moderasi.
- [ ] **Staff Tools**: Saat di Staff Mode, coba gunakan Compass (TP), Clock (Random TP), dan Book (Inspect Inv).

---

## 📊 2. Server Health Dashboard
**Tujuan**: Memeriksa performa server secara visual.

- [ ] **Status Command**: Ketik `/nacore status`.
- [ ] **Metrics Check**: Pastikan bar statis (TPS, RAM, Entities) muncul dengan warna yang sesuai (Hijau = OK, Merah = Lag).
- [ ] **Live Charts**: Pastikan grafik unicode di bawah GUI bergerak dan menunjukkan history performa.

---

## 🤝 3. Secure Trading System (v2.0 Vault Migrated)
**Tujuan**: Memastikan transaksi item dan uang (Rp) aman.

- [ ] **Trade Request**: Ketik `/trade <nama_player>`. Player lain harus menerima dengan `/trade accept`.
- [ ] **Uang (Rp) Trading**: 
    - Klik icon emas. Pastikan saldo Rp kamu berkurang di GUI dan bertambah di sisi lawan.
    - Coba tambahkan uang melebihi saldo. Pastikan muncul pesan error "Saldo Rp tidak cukup".
- [ ] **Item Trading**: Taruh item di slot kiri (sisi kamu). Pastikan lawan melihat item tersebut di sisi kanan mereka.
- [ ] **Lock System**: Klik kaca merah untuk Konfirmasi. Status harus berubah jadi Hijau. Trade hanya akan selesai jika KEDUA player sudah klik Konfirmasi.
- [ ] **Cancel Test**: Coba tutup GUI saat trade berlangsung. Pastikan semua item kembali ke inventory masing-masing.

---

## ⚔️ 4. Visual Combat Tag
**Tujuan**: Mencegah player kabur saat PvP.

- [ ] **Tagging**: Pukul player lain. Pastikan muncul BossBar merah di atas layar dengan countdown 15 detik.
- [ ] **Command Block**: Saat sedang Combat Tag, coba ketik `/spawn`. Pastikan command dibatalkan dengan pesan "Command dilarang saat bertarung".
- [ ] **Combat Log**: Coba diskonek (Alt+F4 atau Quit) saat BossBar masih ada. Saat login kembali, pastikan player tersebut dalam keadaan mati (Combat Penalty).

---

## 🎁 5. Playtime & Milestones (v2.0 Vault Migrated)
**Tujuan**: Memberikan reward otomatis berdasarkan lama bermain.

- [ ] **Playtime Check**: Ketik `/playtime` atau `/pt`. Pastikan waktu yang muncul akurat.
- [ ] **Reward Test**: (Untuk testing cepat, kamu bisa edit `playtime_data.yml` untuk reset milestone atau ganti required seconds di code).
    - Pastikan saat mencapai target jam, muncul pesan "Selamat!" dan saldo Rp bertambah otomatis.
    - Pastikan bonus item (ExcellentCrates) diberikan melalui console.

---

## 📢 6. Smart Global Broadcast
**Tujuan**: Pengumuman otomatis yang estetik.

- [ ] **Auto Broadcast**: Tunggu selama 5 menit (default) atau gunakan `/nacore reload` untuk refresh.
- [ ] **Formatting**: Pastikan pesan muncul dengan prefix yang cantik dan warna gradient hex.
- [ ] **Config Check**: Cek file `announcements.yml` di folder plugin. Coba tambah pesan baru dan reload.

---

### 🚨 Troubleshooting
Jika ada fitur yang tidak jalan:
1. Cek `/plugins` apakah **NaturalCore** berwarna Hijau.
2. Pastikan dependencies terinstal: **Vault**, **LuckPerms**, **PlaceholderAPI**, dan **ExcellentCrates**.
3. Cek log console untuk error merah, jika ada langsung kirimkan ke saya.
