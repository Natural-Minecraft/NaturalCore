# 🍃 NaturalCore v1.3 (Fun & Essentials Update)

**NaturalCore** adalah plugin "All-in-One" untuk server Minecraft Survival/SMP. Plugin ini menggantikan kebutuhan akan plugin dasar seperti Essentials, Economy, dan RTP system dengan fitur yang lebih ringan, estetik, dan terintegrasi.

---

## ✨ Fitur Utama
* **Modular System:** Fitur dipisah per kategori (Economy, Fun, Essentials, dll).
* **Dual Economy:** Support Vault (Rupiah) dan CoinsEngine (NaturalCoin).
* **Fun & RTP:** Broadcast GG/Noob, Warden Logger, dan RTP System.
* **GUI Based:** Hampir semua fitur memiliki menu GUI (Baltop, Home, Warp, Admin).
* **Hex Color Support:** Mendukung kode warna gradasi (contoh: `&#00AAFF`).

---

## 🛠️ Daftar Command & Permission

### 🎮 Fun & RTP (New!)
| Command | Permission | Deskripsi |
| :--- | :--- | :--- |
| `/gg` | - | Broadcast "GG" (Cooldown 30s). |
| `/noob` | - | Broadcast "NOOB" (Cooldown 30s). |
| `/resource`, `/rsc` | `naturalsmp.resource` | RTP ke dunia Resource (via BetterRTP). |
| `/survival`, `/rtp` | - | RTP Random di dunia Survival (via BetterRTP). |
| **Auto Lobby** | - | Player otomatis dipindah ke Spawn jika login di world terlarang. |
| **Warden Log** | - | Logger koordinat & XP saat membunuh Warden. |

### 🎒 Essentials & Utilities
| Command | Permission | Deskripsi |
| :--- | :--- | :--- |
| `/nacore <cmd>` | `naturalsmp.admin` | Main command untuk akses fitur admin. |
| `/gmc`, `/gms`, `/gmsp` | `naturalsmp.gamemode` | Ganti gamemode cepat. |
| `/fly` | `naturalsmp.fly` | Mode terbang. |
| `/heal`, `/feed` | `naturalsmp.heal/feed` | Isi darah dan lapar. |
| `/invsee <player>` | `naturalsmp.invsee` | Intip isi tas player. |
| `/enderchest <player>` | `naturalsmp.enderchest` | Intip enderchest player. |
| `/trash` | - | Membuka tempat sampah. |
| `/craft` | `naturalsmp.craft` | Membuka crafting table portable. |

### 👮 Moderation (Lite)
*Note: Gunakan AdvancedBan untuk fitur Ban/Kick/Mute.*
| Command | Permission | Deskripsi |
| :--- | :--- | :--- |
| `/god` | `naturalsmp.god` | Mode kebal (Invulnerable). |
| `/vanish` / `/v` | `naturalsmp.vanish` | Menghilang dari list player. |
| `/whois <player>` | `naturalsmp.whois` | Cek info detail player (IP, UUID, dll). |

### 💰 Economy
| Command | Permission | Deskripsi |
| :--- | :--- | :--- |
| `/bal`, `/money` | - | Cek saldo sendiri/orang lain. |
| `/pay <player>` | - | Kirim uang ke pemain lain. |
| `/baltop` | - | Menu GUI Top 10 pemain terkaya. |
| `/setbal`, `/takebal` | `naturalsmp.economy.admin` | Atur saldo pemain (Admin). |
| `/givebal` | `naturalcs.givebalance` | Give balance dual currency. |

### 🏠 Home & Teleport
| Command | Permission | Deskripsi |
| :--- | :--- | :--- |
| `/sethome <nama>` | `naturalsmp.home.use` | Set rumah. |
| `/home`, `/homes` | `naturalsmp.home.use` | Buka GUI daftar home. |
| `/tpa <player>` | `naturalsmp.tpa` | Request teleport ke pemain. |
| `/tpahere <player>` | `naturalsmp.tpa` | Request tarik pemain. |
| `/tp`, `/tphere` | `naturalsmp.tp` | Teleport paksa (Admin). |

### 📍 Warp & Spawn
| Command | Permission | Deskripsi |
| :--- | :--- | :--- |
| `/spawn` | `naturalsmp.spawn` | Teleport ke spawn utama. |
| `/setspawn` | `naturalsmp.admin` | Set lokasi spawn. |
| `/warps` | - | Buka GUI Warp. |
| `/setwarp`, `/delwarp` | `naturalsmp.admin` | Atur lokasi warp. |

### ⛩️ NPC Trader
| Command | Permission | Deskripsi |
| :--- | :--- | :--- |
| `/wt` | `naturalsmp.trader.admin` | Spawn Wandering Trader Custom. |
| `/settrader` | `naturalsmp.trader.admin` | Setup item Trader. |

---

## 📂 Konfigurasi
Semua pesan bisa diedit di `config.yml`.
**Dependencies:**
- Java 21+
- Paper 1.21+
- Vault & Economy Plugin (EssentialsEco/CoinsEngine)
- BetterRTP (Untuk fitur RTP)