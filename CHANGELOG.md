# Changelog - NaturalCore 👑

Dokumentasi riwayat pembaruan, perbaikan bug, dan rilis fitur untuk plugin **NaturalCore** (Core Plugin NaturalSMP).

---

## [v2.3.0] - IQ HUD & Maintenance Sync Update
### ✨ Fitur Baru
- **IQ HUD Action Bar**: Mengubah visualisasi HUD Action Bar dari indikator **Mana** ke **IQ** menggunakan placeholder `%naturalschool_iq%`.
- **Bedrock GUI Redirection**: Menambahkan pintasan menu telepon (phone menu) khusus bagi player Bedrock GUI demi peningkatan aksesibilitas.

### ⚡ Peningkatan & Refactor
- **Decoupled Maintenance**: Menghapus fitur maintenance bawaan secara total dari NaturalCore karena pengaturan ini telah didelegasikan dan dikelola terpusat oleh **NaturalVelocity** di level proxy.
- **Maintenance Whitelist Database**: Normalisasi tabel database whitelist maintenance untuk sinkronisasi dengan tabel `nvelo_mt` milik NaturalVelocity.
- **Dungeon Cleanup**: Pembersihan data dan optimasi sistem dungeon lama yang sudah tidak aktif.
- **Custom Model X Close Button**: Mengganti seluruh ikon penutup/batal berupa Barrier merah pada GUI menu menjadi item Model X kustom yang lebih modern dan premium.
- **Media Link Add-on**: Penambahan item tautan pendaftaran media sosial (+ icon) pada Media GUI.

### 🐛 Perbaikan Bug
- **Vote Command Conflict Fix**: Menghapus command `/vote` internal agar tidak konflik dengan plugin **NaturalVote**, dan mengalihkan fungsinya ke proxy `/nacore vote`.
- **Shift-Click Double Trigger**: Memperbaiki bug yang memicu aksi ganda saat player melakukan shift-click pada slot item tertentu di inventory GUI.
- **Media Target Actionbar**: Notifikasi target media kini dialihkan secara anggun ke Action Bar daripada membanjiri chat box player.

---

## [v2.2.3] - The Mention Update
### ✨ Fitur Baru
- **Modern Chat Mentions**: Tab completion suggestions berbasis client-side untuk format `@player`. Dropdown otomatis muncul seketika saat mengetik simbol `@`.

### ⚡ Peningkatan & Refactor
- **Legacy Cleanup**: Menghapus parser tab completion lama yang sudah usang dan tidak kompatibel dengan client Minecraft modern.

---

## [v2.2.2] - The Protection Update
### ✨ Fitur Baru
- **SellAll Confirmation System**: Mencegah player tidak sengaja menjual seluruh isi tas. Command `/sellall`, `/sellhand`, dan `/sellhandall` kini mewajibkan konfirmasi penulisan ulang dalam waktu 10 detik.
- **Accidental Sell Prevention**: Menonaktifkan input shortcut tombol "F" (Offhand) saat player sedang membuka Toko GUI guna mencegah penjualan tidak disengaja.

---

## [v2.2.1] - Stability Update
### ✨ Fitur Baru
- **Dynamic Mob Stacker**: Modul Mob Stacking kini sepenuhnya dinamis dan dapat dimuat ulang secara instan via `/nacore reload` tanpa restart server.
- **Priority Join Slots**: visual slots diset ke 69 namun real slot menampung hingga 100. Player VIP/Nature memiliki hak prioritas untuk masuk dan mengeluarkan player non-prioritas terlama secara acak dengan broadcast gemuruh petir.

### 🐛 Perbaikan Bug
- **Hologram Radius Fix**: Menambahkan batas visibilitas 5 block pada entity TextDisplay hologram agar tidak terlihat menembus dinding tebal dari kejauhan.
- **ActionBar Typing Sync**: Sinkronisasi ketukan audio mesin tik dengan kecepatan scroll teks tips.

---

## [v2.2.0] - Media Ranks & UI Overhaul
### ✨ Fitur Baru
- **Media Creator Rank**: Rank kustom baru untuk Konten Kreator YouTube/TikTok dengan perizinan terbang `/fly`, integrasi LuckPerms, dan sinkronisasi otomatis status media.
- **Level-Based Chat Color**: Migrasi warna chat dari berbasis rank LuckPerms ke sistem perizinan level 1-4 (`naturalsmp.color.levelX`).
- **Kit Preview GUI**: Klik kanan pada info Rank untuk melihat pratinjau kit item secara visual.
- **HUD Pass Integration**: Integrasi event perolehan XP NaturalPass ke siklus HUD utama.
- **Screeneffects Dimensions**: Efek filter dimensi visual baru saat player berganti dimensi.
