# NaturalCore v1.9.0

Plugin inti (core) premium untuk server **NaturalSMP**, mengelola modul Ekonomi, Chat, Warp, Home, Banner interaktif, sistem Seasons, hingga fitur ChatColor berbasis Rank. **NaturalCore** difokuskan sebagai plugin Essentials yang ringan dan stabil.

## ✨ Fitur Baru v1.9 (The "Experience" Update)

### 🧹 NaturalLagg (New Optimizer)
Sistem pembersih item dan mob yang lebih cerdas dan estetik.
*   **Smart Mob Merging**: Otomatis menggabungkan mob yang menumpuk (misal: `Chicken x50`) dengan hologram di atasnya. Mengurangi lag entity cramming.
*   **Prioritized Action Bar**: Animasi scrolling "sliding" saat pembersihan terjadi. Bar Mana/Suhu akan bergeser untuk menampilkan hitung mundur ClearLagg.
*   **Low-TPS Unloader**: Otomatis unload chunk kosong jika TPS drop di bawah 10.0.

### � TopUp Notification System
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
| `/clean ground` | - | Bersihkan item di tanah (Admin) | `naturalsmp.admin` |
| `/craft` | `wb` | Buka Workbench virtual | `naturalsmp.craft` |
| `/anvil` | `av` | Buka Anvil virtual | `naturalsmp.anvil` |
| `/enderchest` | `ec` | Buka Enderchest virtual | `naturalsmp.enderchest` |
| `/trash` | - | Buka tempat sampah | `naturalsmp.trash` |

### 🛡️ Admin & Moderation
| Command | Aliases | Description | Permission |
| :--- | :--- | :--- | :--- |
| `/nacore` | - | Main Admin Command | `naturalsmp.admin` |
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

## 💎 Special Permissions

### 🎨 Chat Color Ranks
Akses warna chat berdasarkan permission.
*   `naturalsmp.chat.color` : Akses dasar command `/chatcolor`.
*   `naturalsmp.color.vip` : Akses warna VIP (Gold, Yellow, dll).
*   `naturalsmp.color.mvp` : Akses warna MVP (Aqua, Pink, dll).
*   `naturalsmp.color.midi` : Akses warna Midi.
*   `naturalsmp.color.nature` : Akses **ALL** (Bold, Italic, Magic, Special Fonts).

### 🏠 Home Limits
Batas jumlah sethome per player.
*   `naturalsmp.home.limit.default` : Max 3 Home (Default)
*   `naturalsmp.home.limit.vip` : Max 5 Home
*   `naturalsmp.home.limit.mvp` : Max 15 Home
*   `naturalsmp.home.limit.nature` : Max 30 Home
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
