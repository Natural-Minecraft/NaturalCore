# 🍃 NaturalCore v1.6 (The Interaction & Climate Update)

![Version](https://img.shields.io/badge/version-1.6-green) ![Minecraft](https://img.shields.io/badge/minecraft-1.21+-blue) ![Java](https://img.shields.io/badge/java-21-orange)

**NaturalCore** adalah plugin "All-in-One" untuk server Minecraft Survival/SMP modern. Plugin ini menggantikan kebutuhan akan Essentials dengan solusi yang ringan, estetik, dan terintegrasi penuh.

Versi **1.6** menghadirkan pengalaman dunia yang lebih hidup dengan sistem **Musim (Seasons)** serta fitur visual **Interactive Banners** untuk dekorasi server yang fungsional.

---

## ✨ Apa yang Baru di v1.6?

* **🍂 Dynamic Seasons:** Siklus 4 musim (Spring, Summer, Autumn, Winter) yang mengubah warna dunia (foliage/grass) secara otomatis.
* **🌡️ Temperature System:** Suhu dinamis berbasis Celsius yang dipengaruhi oleh waktu, cuaca, dan lingkungan (api, lava, air).
* **🖼️ Interactive Banners:** Pasang gambar kustom di dunia menggunakan Map & ItemDisplay. Mendukung klik aksi (Buka URL atau Jalankan Command).
* **⚡ Modern Action Bar:** Tampilan informatif yang menggabungkan Status Musim, Suhu, dan **Mana AuraSkills** secara estetik.
* **📨 Private Messaging:** Sistem `/msg` dan `/reply` dengan format modern.
* **🏠 Home System V2:** Penyimpanan Home kini berbasis **UUID Flat-File** (Folder `homes/`), aman dari pergantian nama player.
* **🏠 Home GUI Pagination:** Tampilan GUI Home baru yang minimalis (9-slot) dengan dukungan halaman tak terbatas.

---

## 🛠️ Daftar Command & Permission

### 🌍 Interactive & Climate (New in v1.6!)
| Command | Permission | Deskripsi |
| :--- | :--- | :--- |
| `/season set <nama>` | `naturalsmp.season.admin` | Ubah musim secara manual (Refresh visuals). |
| `/banner wand` | `naturalsmp.admin.banner` | Ambil alat seleksi untuk pasang gambar. |
| `/banner create <n> <img>` | `naturalsmp.admin.banner` | Pasang banner interaktif di area terseleksi. |
| `/banner delete <nama>` | `naturalsmp.admin.banner` | Hapus banner yang sudah ada. |

### 📨 Chat & Social
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
3.  Masukkan `NaturalCore-1.6.jar`.
4.  Pastikan plugin dependensi terinstall:
    * **Vault** (Wajib)
    * **DecentHolograms** (Wajib - untuk beberapa fitur tampilan)
    * **PlaceholderAPI** (Recommended - untuk Action Bar Mana)
    * **Citizens** (Opsional - untuk NPC)
5.  **Start Server**.

---

**Developed with ❤️ by NaturalSMP Team**