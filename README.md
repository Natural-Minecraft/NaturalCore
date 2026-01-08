# 🍃 NaturalCore v1.4 (The Social Update)

**NaturalCore** adalah plugin "All-in-One" untuk server Minecraft Survival/SMP. Plugin ini menggantikan kebutuhan akan banyak plugin dasar dengan satu solusi yang ringan, estetik, dan terintegrasi.

Versi **1.4** membawa pembaruan besar pada sistem **Chat**, **Social**, dan **Moderation**.

---

## ✨ Fitur Utama v1.4
* **💬 Chat Formatting:** Integrasi penuh dengan Vault/LuckPerms (Prefix, Suffix, & Hex Color).
* **👋 Custom Join/Quit:** Pesan sambutan (MOTD) dan notifikasi join yang mendukung Placeholder.
* **🎒 Essentials Split:** Pemisahan command inventory (`/ec` untuk pemain, `/endersee` untuk admin).
* **🛡️ Vanish V2:** Sistem menghilang yang lebih aman (Hidden from Tablist & Login Glitch fixed).
* **📊 Dynamic Limits:** Batas sethome yang bisa diatur per-rank tanpa batas via Config.
* **🧩 PlaceholderAPI:** Support variable custom untuk Scoreboard/TAB.

---

## 🛠️ Daftar Command & Permission

### 💬 Social & Fun (New!)
| Command | Permission | Deskripsi |
| :--- | :--- | :--- |
| `/gg` | - | Broadcast "GG" dengan format rank (Cooldown 30s). |
| `/noob` | - | Broadcast "NOOB" dengan format rank (Cooldown 30s). |
| **Chat Color** | `naturalsmp.chat.color` | Izin menggunakan kode warna/hex di chat. |

### 🎒 Essentials & Inventory
| Command | Permission | Deskripsi |
| :--- | :--- | :--- |
| `/ec` | `naturalsmp.enderchest` | Membuka Enderchest sendiri. |
| `/endersee <player>` | `naturalsmp.endersee` | Intip Enderchest pemain lain (Admin). |
| `/invsee <player>` | `naturalsmp.invsee` | Intip/Edit inventory pemain lain. |
| `/trash` | - | Membuka tempat sampah. |
| `/craft` / `/wb` | `naturalsmp.craft` | Membuka Crafting Table portable. |
| `/fly` | `naturalsmp.fly` | Mode terbang. |
| `/heal` | `naturalsmp.heal` | Mengisi darah & saturation. |
| `/feed` | `naturalsmp.feed` | Mengisi rasa lapar. |
| `/gmc`, `/gms`, `/gmsp` | `naturalsmp.gamemode` | Ganti gamemode cepat. |

### 🛡️ Moderation (Lite)
| Command | Permission | Deskripsi |
| :--- | :--- | :--- |
| `/god` | `naturalsmp.god` | Mode kebal (Invulnerable). |
| `/vanish` / `/v` | `naturalsmp.vanish` | Menghilang dari game & tablist. |
| `/whois <player>` | `naturalsmp.whois` | Cek info detail (IP, UUID, God/Vanish Status). |
| **See Vanish** | `naturalsmp.vanish.see` | Melihat admin yang sedang vanish. |

### 💰 Economy
| Command | Permission | Deskripsi |
| :--- | :--- | :--- |
| `/bal` | - | Cek saldo sendiri/orang lain. |
| `/pay <player>` | - | Kirim uang ke pemain lain. |
| `/baltop` | - | Menu GUI Top 10 pemain terkaya. |
| `/setbal`, `/takebal` | `naturalsmp.economy.admin` | Atur saldo pemain (Admin). |
| `/givebal` | `naturalcs.givebalance` | Give balance dual currency. |

### 🏠 Home, Warp & Teleport
| Command | Permission | Deskripsi |
| :--- | :--- | :--- |
| `/sethome <nama>` | `naturalsmp.home.use` | Set rumah. |
| `/home`, `/homes` | `naturalsmp.home.use` | Buka GUI daftar home. |
| `/tpa`, `/tpahere` | - | Request teleport. |
| `/spawn` | `naturalsmp.spawn` | Teleport ke spawn utama. |
| `/setspawn` | `naturalsmp.admin` | Set lokasi spawn. |
| `/warps` | - | Buka GUI Warp. |
| `/setwarp`, `/delwarp` | `naturalsmp.admin` | Atur lokasi warp. |
| `/rtp`, `/resource` | `naturalsmp.resource` | Teleport Random (via BetterRTP). |

---

## 🧩 Placeholders
Gunakan placeholder ini di **Scoreboard (TAB)** atau plugin lain (Memerlukan PlaceholderAPI).

| Placeholder | Deskripsi |
| :--- | :--- |
| `%naturalcore_homes%` | Jumlah home yang dimiliki player saat ini. |
| `%naturalcore_maxhomes%` | Batas maksimal home player (sesuai rank). |

**Config Placeholder:**
Gunakan `%displayname%` di dalam `config.yml` (pesan join/quit/motd) untuk menampilkan Nama Lengkap + Prefix/Suffix dari LuckPerms.

---

## 📂 Instalasi & Requirements

**Requirements:**
1.  **Java 21** (Minimum).
2.  **Paper 1.21+**.
3.  **Vault** (Wajib untuk Economy & Chat).
4.  **LuckPerms** (Direkomendasikan untuk Prefix/Suffix).
5.  **Citizens** (Opsional, untuk fitur NPC Trader).

**Cara Install:**
1.  Matikan server.
2.  Hapus folder `NaturalCore` lama (Sangat disarankan saat update ke v1.4 karena perubahan Config).
3.  Masukkan `NaturalCore-1.4.jar` ke folder plugins.
4.  Nyalakan server.
5.  Edit `config.yml` sesuai kebutuhan.

---

**Developed with ❤️ by NaturalSMP Team**