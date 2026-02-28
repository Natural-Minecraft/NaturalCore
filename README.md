# NaturalCore v2.2.3 (The Mention Update)

Plugin inti (core) premium untuk server **NaturalSMP**, mengelola seluruh aspek penting server dengan stabilitas ultra-tinggi dan estetika premium.

## ✨ Fitur Baru v2.2.3 (The Mention Update)
*   **Modern Chat Mentions**: Tab completion untuk `@player` sekarang berfungsi penuh menggunakan client-side suggestions. Otomatis memunculkan dropdown daftar pemain saat kamu mengetik `@`.
*   **Cleanup**: Menghapus sistem tab completion lama yang sudah tidak kompatibel dengan client modern.

## ✨ Fitur Baru v2.2.2 (The Protection Update)
*   **SellAll Confirmation System**: Mencegah member salah menjual seluruh inventory. Sekarang `/sellall`, `/sellhand`, dan `/sellhandall` memerlukan konfirmasi (ketik 2x) dalam 10 detik.
*   **Accidental Sell Fix**: Menonaktifkan shortcut tombol "F" pada GUI Toko untuk mencegah penjualan tidak sengaja saat navigasi menu.

## ✨ Fitur Baru v2.2.1 (Stability Update)
*   **Dynamic Mob Stacker**: Sekarang fitur Mob Stacking bisa dinyalakan/matikan secara real-time via `/nacore reload` tanpa perlu restart server.
*   **Priority Join**: Limit display server menjadi 69, namun slot asli bisa menampung 100 pemain. Pemain dengan rank Nature atau prioritas bisa join, mengusir 1 pemain (non-prioritas) yang online paling lama secara acak dengan pesan kick khusus.
*   **Aesthetic Priority Message**: Pesan khusus 3 baris saat player prioritas join yang bisa dikustomisasi via `messages.yml`, disertai efek suara naga dan petir.
*   **Hologram Radius Fix**: Hologram entitas kini memiliki batas jarak pandang 5 blocks agar tidak menembus dinding dari jauh.
*   **Tips Actionbar Sync Fix**: Sinkronisasi sempurna antara teks marquee action bar dengan suara efek mengetik mesin tik.

## ✨ Fitur Baru v2.1.0 (The Mini-Games Update)
*   **1000 Chat Games**: Sistem mini-game chat interaktif baru (Trivia, Matematika, Susun Kata, Ketik Cepat) dengan 1000 pertanyaan yang tidak berulang.
*   **Customizable Games**: Sepenuhnya dapat dikonfigurasi melalui `chat-games.yml`.
*   **Dynamic Rewards**: Random reward berupa Iron Ingot, Diamond, uang (Rp), atau kombinasi (Diamond + Uang) untuk setiap pemenang game.
*   **Bug Fix**: Memperbaiki `ClassCastException` pada sistem `NaturalLagg` saat membersihkan entity item.
*   **Bug Fix**: Menghapus duplikasi pesan Join & Quit pada `GlobalNotificationListener`.

## ✨ Fitur Baru v2.0.9 (Hologram & Tips Update)
*   **Hologram System**: Sistem hologram baru berbasis `TextDisplay` untuk performa lebih ringan dan visual lebih tajam (menggantikan ArmorStand lama).
*   **Tips System**: Peningkatan visual pada pesan tips di Action Bar.
*   **Back Comand**: Logika command `/back` diperbaiki untuk akurasi lokasi kematian/teleport yang lebih baik.
*   **Interactive Tagging**: (Support) Dukungan sistem tagging pemain interaktif.

## ✨ Fitur Baru v2.0.8 (Maintenance Update)
*   **Build Fixes**: Perbaikan error kompilasi pada `SignMenu` dan `NaturalLagg`.
*   **Stability**: Peningkatan stabilitas pada sistem `First Join Kit` dan `NaturalLagg` mob stacking.

## ✨ Fitur Baru v2.0.6 (SQL & Performance Update)

### 🏦 NaturalBank SQL Migration
*   **Database Infrastructure**: Migrasi penuh dari flatfile (YAML) ke **MySQL/MariaDB** untuk integrasi website dan stabilitas data ekonomi tingkat lanjut.
*   **Cross-Server Ready**: Koneksi database yang dioptimasi untuk kebutuhan scaling masa depan.

### 📊 HUD Manager v2 (Performance Focus)
*   **Asynchronous Updates**: Animasi Action Bar dan Scoreboard kini diproses secara asinkron untuk CPU overhead yang minimal.
*   **Smart Refresh**: Sistem cerdas yang hanya mengupdate HUD saat ada perubahan data signifikan (Combat, Temperature, Balance).

### 🎬 Adaptive HUD Manager
*   **Context-Aware Transitions**: Transisi **Instant Pop-up** untuk combat, temperature, dan biome agar lebih responsif.
*   **Cinema Scroll**: Animasi scrolling khusus untuk **Tips** dan **ClearLagg** agar tetap bisa dibaca dengan nyaman.

### 🔨 Premium Shop & Infrastructure
*   **Rank Confirmation**: Menu konfirmasi pembelian rank otomatis dengan integrasi website saldo.
*   **API Stability**: Perbaikan total pada database timeout handling dan inventory synchronization.

## ✨ Fitur Baru v2.0.3 (The Trade & Home Fix)

### 🤝 Premium Trade Overhaul
*   **Custom Money Input**: Sekarang bisa mengetik jumlah nominal uang (Rp) langsung di chat saat bertransaksi.
*   **Small Caps UI**: Judul GUI Trade kini lebih elegan dengan font Small Caps.
*   **Interactive Design**: Layout baru dengan separator yang lebih jelas dan tombol konfirmasi yang lebih intuitif.

### 🏠 Home System Fix
*   **Persistent Data (PDC)**: Nama home kini disimpan secara internal (PDC), mengatasi masalah 'Home not found' akibat kode warna atau formatting.
*   **Prefix Consistency**: Semua pesan home kini menggunakan prefix `SMP »` yang konsisten.
*   **Small Caps GUI**: Judul GUI Home kini lebih seragam.

## ✨ Fitur Baru v2.0.2 (The Polish Update)

### 💎 Aesthetics & Bug Fixes
*   **Professional Small Caps**: Seluruh judul GUI (Profile, Ranks, Editor, Admin) kini menggunakan font Small Caps premium yang elegan.

### ⚙️ Rank Editor Advanced
*   **Permission Pagination**: List permissions di `/rankeditor` kini dikelompokkan per 7 baris untuk keterbacaan yang lebih baik.
*   **Weight Management**: Penambahan fungsionalitas untuk mengubah **Weight** rank secara langsung melalui GUI dengan input chat.

## ✨ Fitur Baru v2.0.0 (The Phoenix Update)

### 🧹 NaturalLagg (Visual Overhaul)
*   **Synced Action Bar**: Notifikasi hitung mundur kini muncul di Action Bar dengan animasi "Slide-In" yang halus.
*   **Synchronized Timer**: Waktu hitung mundur Action Bar tersinkronisasi sempurna dengan pesan chat (15 detik).
*   **Aesthetic Notifications**: Tampilan pesan pembersihan yang lebih modern dan konsisten dengan tema server.

### 🛠️ GUI System Refactor
*   **Modern API**: Seluruh GUI kini menggunakan sistem `GUIUtils` terpusat, meninggalkan metode lawas (deprecated).
*   **Adventure Component**: Mendukung format teks modern (MiniMessage/Component) untuk performa dan kompatibilitas masa depan.
*   **Stability**: Perbaikan bug pada Anvil dan Trash GUI.

### � TopUp Notification Systemanti
Fitur bagi owner/admin untuk mensimulasikan dan mengirim notifikasi donasi.
*   **GUI & Broadcast**: Tampilan GUI "Pembayaran Berhasil" dan broadcast global yang meriah.
*   **Admin Tools**: Tombol khusus di `/nacore admin` untuk simulasi cepat.

### �️ Environment & Seasons
*   **Musim & Suhu**: Suhu tubuh pemain berubah berdasarkan bioma, waktu (Pagi/Malam/Hujan), dan kedekatan dengan api/air.
*   **Personal Time/Weather**: Pemain dapat mengatur waktu/cuaca di client-side saja (`/ptime`, `/pweather`).

### 🏆 Natural Tier & Ranks
*   **Dynamic Rank GUI**: Menu `/ranks` kini otomatis menyesuaikan dengan `rank-config.yml`.
*   **Sorted Player List**: Daftar `/list` otomatis mengurutkan pemain berdasarkan ranking (Admin -> MVP -> Member).

---

## 📑 Full List Command & Permissions

### 👤 Profile & Social
| Command | Aliases | Description | Permission |
| :--- | :--- | :--- | :--- |
| `/menu` | - | Buka menu navigasi utama | - |
| `/profile` | `p`, `stats` | Buka menu profil player | - |
| `/tier` | - | Buka menu Tier / Ranking | - |
| `/tier top` | - | Buka leaderboard global | - |
| `/tags` | - | Buka koleksi tag chat | - |
| `/chatcolor` | `color` | Buka menu warna chat | `naturalsmp.chat.color` |
| `/nick` | `nickname` | Ubah nama tampilan | `naturalsmp.nick` |
| `/msg <player>` | `tell`, `whisper` | Kirim pesan pribadi | - |
| `/reply <msg>` | `r` | Balas pesan terakhir | - |
| `/emoji` | `emojis` | Lihat daftar emoji chat | `naturalsmp.emoji.use` |

### 🌍 Navigation & Teleport
| Command | Aliases | Description | Permission |
| :--- | :--- | :--- | :--- |
| `/start` | - | Buka menu pilih dunia awal | - |
| `/spawn` | - | Teleport ke Spawn utama | `naturalsmp.spawn` |
| `/setspawn` | - | Set lokasi Spawn | `naturalsmp.admin` |
| `/warp` | - | Teleport ke Warp point | - |
| `/warps` | - | Buka menu GUI Warp | - |
| `/setwarp` | - | Buat warp baru | `naturalsmp.admin` |
| `/delwarp` | - | Hapus warp | `naturalsmp.admin` |
| `/rtp` | `survival` | Teleport Random (Survival) | - |
| `/rsc` | `resource` | Teleport Random (Resource) | `naturalsmp.resource` |
| `/back` | - | Kembali ke lokasi mati/tp | `naturalsmp.back` |
| `/otp` | `offlinetp` | Admin TP ke player offline | `naturalsmp.otp` |
| `/tpa` | - | Request teleport ke player | - |
| `/tpahere` | - | Request player ke sini | - |

### 🌦️ Environment
| Command | Aliases | Description | Permission |
| :--- | :--- | :--- | :--- |
| `/ptime` | - | Atur waktu personal | `naturalsmp.ptime` |
| `/pweather` | - | Atur cuaca personal | `naturalsmp.pweather` |

### 🏠 Home System
| Command | Aliases | Description | Permission |
| :--- | :--- | :--- | :--- |
| `/home` | - | Teleport ke home | `naturalsmp.home.use` |
| `/homes` | - | List home yang dimiliki | `naturalsmp.home.use` |
| `/sethome` | - | Set titik home baru | `naturalsmp.home.use` |
| `/delhome` | - | Hapus home | `naturalsmp.home.use` |

### 💰 Economy
| Command | Aliases | Description | Permission |
| :--- | :--- | :--- | :--- |
| `/bal` | `money` | Cek saldo sendiri/lain | - |
| `/pay` | - | Kirim uang ke player | - |
| `/baltop` | - | Lihat top rich server | - |
| `/givebal` | - | Admin give money | `naturalcs.givebalance` |
| `/takebal` | - | Admin take money | `naturalsmp.economy.admin` |
| `/setbal` | - | Admin set money | `naturalsmp.economy.admin` |
| `/topupnotification`| - | Trigger notifikasi topup | `naturalsmp.admin` |

### 🛠️ Utilities & Perks
| Command | Aliases | Description | Permission |
| :--- | :--- | :--- | :--- |
| `/fly` | - | Mode terbang | `naturalsmp.fly` |
| `/hat` | - | Pakai item di kepala | `naturalsmp.hat` |
| `/feed` | - | Isi hunger bar | `naturalsmp.feed` |
| `/heal` | - | Isi HP & Hunger | `naturalsmp.heal` |
| `/repair` | `fix` | Perbaiki item di tangan | `naturalsmp.repair` |
| `/clean` | `ci` | Bersihkan inventory (Safe) | `naturalsmp.clean` |
| `/lagg` | `tps`, `mem` | Cek performa server (Stats) | `naturalsmp.lag` |
| `/lagg clear` | - | Bersihkan item di tanah (Admin) | `naturalsmp.admin` |
| `/craft` | `wb` | Buka Workbench virtual | `naturalsmp.craft` |
| `/anvil` | `av` | Buka Anvil virtual | `naturalsmp.anvil` |
| `/enderchest` | `ec` | Buka Enderchest virtual | `naturalsmp.enderchest` |
| `/trash` | - | Buka tempat sampah | `naturalsmp.trash` |

### 🛡️ Admin & Moderation
| Command | Aliases | Description | Permission |
| :--- | :--- | :--- | :--- |
| `/nacore` | - | Main Admin Command / Open Admin GUI | `naturalsmp.admin` |
| `/nacore admin reload` | `/nacore reload` | **Deep Reload** all systems & configs | `naturalsmp.admin` |
| `/nacore admin ranksync` | - | Sync ranks to LuckPerms | `naturalsmp.admin` |
| `/nacore admin status` | - | View detailed server health status | `naturalsmp.admin` |
| `/nacore admin backup` | - | Trigger manual database backup | `naturalsmp.admin` |
| `/gamemode` | `gm`, `gmc`, `gms` | Ganti mode permainan | `naturalsmp.gamemode` |
| `/invsee` | - | Cek isi tas player | `naturalsmp.invsee` |
| `/endersee` | - | Cek enderchest player | `naturalsmp.endersee` |
| `/vanish` | `v` | Mode menghilang | `naturalsmp.vanish` |
| `/god` | - | Mode kebal | `naturalsmp.god` |
| `/kickall` | - | Kick semua player | `naturalcs.kickall` |
| `/bc` | `broadcast` | Kirim pengumuman | `naturalcs.broadcast` |
| `/season` | - | Atur musim/suhu (Season) | `naturalsmp.season.admin` |
| `/banner` | - | Atur Interactive Board | `naturalsmp.admin.banner` |
| `/maintenance` | `mt` | Kontrol Mode Maintenance | `naturalsmp.maintenance.admin` |

---

## ⚙️ Deep Reload System
Perintah `/nacore reload` atau `/nacore admin reload` melakukan penyegaran sistem secara menyeluruh (**Deep Refresh**), meliputi:
*   **config.yml**: Settings utama (AFK, Mentions, dll).
*   **messages.yml**: Seluruh pesan dan notifikasi GUI.
*   **Module Configs**: Emojis, Chat Tags, Tiers, dan Rank logic.
*   **Dynamic Data**: Warps, Spawn, dan data Season.
*   **Visuals**: HUD (Action Bar/Boss Bar) dan Chat Color logic.
*   **NaturalLagg**: Konfigurasi pembersihan otomatis.

> [!IMPORTANT]
> Gunakan perintah ini setiap kali melakukan perubahan pada file `.yml` agar perubahan langsung diterapkan tanpa membutuhkan restart server.

---

## 💎 Special Permissions

### 🎨 Chat Color Ranks
Akses warna chat berdasarkan permission.
*   `naturalsmp.chat.color` : Akses dasar command `/chatcolor`.
*   `naturalsmp.color.midi` : Akses warna Midi.
*   `naturalsmp.color.vip` : Akses warna VIP (Gold, Yellow, dll).
*   `naturalsmp.color.mvp` : Akses warna MVP (Aqua, Pink, dll).
*   `naturalsmp.color.nature` : Akses **ALL** (Bold, Italic, Magic, Special Fonts).

### 🏠 Home Limits
Batas jumlah sethome per player.
*   `naturalsmp.home.limit.default` : Max 3 Home (Default)
*   `naturalsmp.home.limit.midi` : Max 5 Home
*   `naturalsmp.home.limit.vip` : Max 15 Home
*   `naturalsmp.home.limit.mvp` : Max 30 Home
*   `naturalsmp.home.limit.nature` : Max 50 Home
*   `naturalsmp.home.limit.admin` : Max 100 Home
*   `naturalsmp.home.limit.unlimited` : Unlimited Home

---

## 📁 Struktur Konfigurasi
*   `config.yml`: Pengaturan utama (AFK, Mention, Economy).
*   `tiers.yml`: Konfigurasi Rank Tier (Nama, Syarat, Suffix).
*   `tags.yml`: Konfigurasi list Chat Tags.
*   `messages.yml`: Pusat kustomisasi pesan.
*   `chatcolor.yml` & `chatemojis.yml`: Modul Chat.
*   `banners/`: Data Interactive Boards.
*   `rank-config.yml`: Konfigurasi Rank dan Permission Sync.

---
**© 2026 NaturalSMP Development Team**
