# 🍃 NaturalCore v1.5 (The Utility Update)

![Version](https://img.shields.io/badge/version-1.5-green) ![Minecraft](https://img.shields.io/badge/minecraft-1.21+-blue) ![Java](https://img.shields.io/badge/java-21-orange)

**NaturalCore** adalah plugin "All-in-One" untuk server Minecraft Survival/SMP modern. Plugin ini menggantikan kebutuhan akan Essentials dengan solusi yang ringan, estetik, dan terintegrasi penuh.

Versi **1.5** berfokus pada **Utilities & Modern Aesthetics**, menambahkan fitur pesan pribadi, pengaturan dunia, dan tampilan GUI yang lebih segar.

---

## ✨ Apa yang Baru di v1.5?

* **📨 Private Messaging:** Sistem `/msg` dan `/reply` dengan format modern.
* **🎨 Hex Color Support:** Dukungan penuh warna Hex (`&#RRGGBB`) di chat dan config.
* **🔨 Virtual Utilities:** Akses `/anvil`, `/trash`, dan `/craft` di mana saja.
* **🏠 Home System V2:** Penyimpanan Home kini berbasis **UUID Flat-File** (Folder `homes/`), aman dari pergantian nama player.
* **📖 Home GUI Pagination:** Tampilan GUI Home baru yang minimalis (9-slot) dengan dukungan halaman tak terbatas.
* **🌤️ World Tools:** Kontrol waktu dan cuaca instan (`/day`, `/rain`, dll).

---

## 🛠️ Daftar Command & Permission

### 📨 Chat & Social (New!)
| Command | Permission | Deskripsi |
| :--- | :--- | :--- |
| `/msg <player>` | - | Kirim pesan pribadi (Aliases: `/tell`, `/w`). |
| `/reply <pesan>` | - | Balas pesan terakhir (Alias: `/r`). |
| `/gg`, `/noob` | - | Broadcast pesan seru (Cooldown 30s). |
| **Color Chat** | `naturalsmp.chat.color` | Izin menggunakan kode warna & Hex di chat. |

### 🎒 Essentials & Inventory
| Command | Permission | Deskripsi |
| :--- | :--- | :--- |
| `/anvil` | `naturalsmp.anvil` | **(Baru)** Buka Anvil virtual. |
| `/trash` | - | Buka tempat sampah. |
| `/craft` | `naturalsmp.craft` | Buka Crafting Table portable. |
| `/ec` | `naturalsmp.enderchest` | Buka Enderchest sendiri. |
| `/endersee` | `naturalsmp.endersee` | Intip Enderchest pemain lain (Admin). |
| `/invsee` | `naturalsmp.invsee` | Intip inventory pemain lain. |
| `/repair` | `naturalsmp.repair` | **(Baru)** Perbaiki item di tangan. |
| `/hat` | `naturalsmp.hat` | **(Baru)** Pakai item di tangan sebagai topi. |
| `/nick` | `naturalsmp.nick` | **(Baru)** Ganti nama panggilan (Support Hex). |

### 🌍 World & Teleport
| Command | Permission | Deskripsi |
| :--- | :--- | :--- |
| `/day`, `/night` | `naturalsmp.time` | **(Baru)** Ubah waktu dunia. |
| `/sun`, `/rain` | `naturalsmp.time` | **(Baru)** Ubah cuaca dunia. |
| `/rtp` | `naturalsmp.resource` | Teleport random (Resource World). |
| `/tpa`, `/tpahere` | - | Request teleport ke player. |
| `/spawn` | `naturalsmp.spawn` | Teleport ke spawn utama. |
| `/setspawn` | `naturalsmp.admin` | Set lokasi spawn. |

### 🏠 Home System
| Command | Permission | Deskripsi |
| :--- | :--- | :--- |
| `/sethome <nama>` | `naturalsmp.home.use` | Set rumah (Limit sesuai rank). |
| `/home` | `naturalsmp.home.use` | Buka GUI Home (Pagination). |
| `/delhome <nama>` | `naturalsmp.home.use` | Hapus rumah. |

### 💰 Economy
| Command | Permission | Deskripsi |
| :--- | :--- | :--- |
| `/bal` | - | Cek saldo. |
| `/pay <player>` | - | Kirim uang. |
| `/baltop` | - | GUI Top Player terkaya. |
| `/givebal` | `naturalcs.givebalance` | Give balance (Dual Currency Support). |

---

## 🎨 Configuration & Placeholders

### Config Placeholders
Gunakan variabel ini di `config.yml` (Pesan Join, Quit, Chat Format, MOTD):
* `%displayname%` : Menampilkan Prefix + Nama + Suffix (Integrasi LuckPerms).
* `%player%` : Menampilkan nama asli player.

### PlaceholderAPI (PAPI)
Gunakan di Scoreboard/TAB:
* `%naturalcore_homes%` : Jumlah home yang dimiliki.
* `%naturalcore_maxhomes%` : Batas maksimal home player.

---

## 📂 Instalasi

1.  **Stop Server**.
2.  **Hapus folder `NaturalCore` lama** (Wajib jika upgrade dari v1.4 ke v1.5 karena perubahan struktur Home).
3.  Masukkan `NaturalCore-1.5.jar`.
4.  Pastikan plugin dependensi terinstall:
    * **Vault** (Wajib)
    * **LuckPerms** (Recommended untuk Prefix)
    * **PlaceholderAPI** (Opsional)
5.  **Start Server**.

---

**Developed with ❤️ by NaturalSMP Team**