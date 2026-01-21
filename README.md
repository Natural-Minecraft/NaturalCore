# NaturalCore v1.9.0

Plugin inti (core) premium untuk server **NaturalSMP**, mengelola modul Ekonomi, Chat, Warp, Home, Banner interaktif, sistem Seasons, hingga fitur ChatColor berbasis Rank. **NaturalCore** difokuskan sebagai plugin Essentials yang ringan dan stabil.

## ✨ Fitur Baru v1.9 (The "Experience" Update)

### ❂ Central Menu (`/menu`)
Hub navigasi pusat dengan tampilan gradient premium untuk memudahkan akses ke semua fitur utama (Profile, Warps, Homes, Shop, ChatColor, dll).

### ❂ Start Selection (`/start`)
GUI 9x1 khusus untuk pemain baru memilih dunia awal petualangan (**Survival**, **Dungeon**, atau **Resource**).

### 🌦️ Personal Environment (`/ptime` & `/pweather`)
Kontrol penuh atas waktu dan cuaca di layar masing-masing pemain tanpa mempengaruhi server-side.
*   **Pesan Estetik**: Menggunakan format prefix `&#00AAFF❂` yang sudah dipoles.
*   **Akses**: `/ptime <time>`, `/pweather <sun/rain>`.

### 👤 Enhanced Profile GUI
Dashboard personal kini terintegrasi dengan plugin **AuraSkills**.
*   **AuraSkills Display**: Menampilkan **Total Power** dan level skill utama (Mining, Fighting, Foraging, Archery).
*   **Akses**: `/profile` atau `/profile <player>`.

### 🏆 Natural Tier System (Ranking)
Sistem progresi level ala Game MOBA (Warrior -> Mythic).
*   **Grinding Based**: Naik rank dengan memenuhi syarat **Money** & **Mob Kills**.
*   **Suffix Prestige**: Rank otomatis muncul di chat (misal: `[VIP] Dimas [Warrior I]: Halo`).
*   **Leaderboard**: GUI Top Global Seasons (`/tier top`) menggunakan kepala player asli.

### 💤 Modern AFK System
*   **Visual Indikator**: Hologram (TextDisplay) **"💤 Sedang Bermimpi... 💤"** muncul di atas kepala player saat AFK.
*   **Tablist Status**: Nama di tablist berubah jadi abu-abu.

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

### 🛠️ Utilities & Perks
| Command | Aliases | Description | Permission |
| :--- | :--- | :--- | :--- |
| `/fly` | - | Mode terbang | `naturalsmp.fly` |
| `/hat` | - | Pakai item di kepala | `naturalsmp.hat` |
| `/feed` | - | Isi hunger bar | `naturalsmp.feed` |
| `/heal` | - | Isi HP & Hunger | `naturalsmp.heal` |
| `/repair` | `fix` | Perbaiki item di tangan | `naturalsmp.repair` |
| `/clean` | `ci` | Bersihkan inventory (Safe) | `naturalsmp.clean` |
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

---
**© 2026 NaturalSMP Development Team**
