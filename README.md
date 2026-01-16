# NaturalCore v1.8.0

Plugin inti (core) premium untuk server **NaturalSMP**, mengelola modul Ekonomi, Chat, Warp, Home, Banner interaktif, sistem Seasons, hingga fitur ChatColor berbasis Rank. **NaturalCore** difokuskan sebagai plugin Essentials yang ringan dan stabil.

## ✨ Fitur Utama v1.8 (The "Killer" Update)

### 👤 Natural Profile GUI
Dashboard personal interaktif untuk menggantikan command text membosankan.
*   **Stats Tracking**: KDR, Playtime, Mob Kills, Deaths.
*   **Economy Hub**: Menampilkan saldo Vault dan **NaturalCoin** (CoinsEngine Integration).
*   **Akses**: `/profile` atau `/profile <player>`.

### 🏆 Natural Tier System (Ranking)
Sistem progresi level ala Game MOBA (Warrior -> Mythic).
*   **Grinding Based**: Naik rank dengan memenuhi syarat **Money** & **Mob Kills**.
*   **Suffix Prestige**: Rank otomatis muncul di chat (misal: `[VIP] Dimas [Warrior I]: Halo`).
*   **Leaderboard**: GUI Top Global Seasons (`/tier top`) menggunakan kepala player asli.
*   **Configurable**: Atur nama rank dan syarat di `tiers.yml`.

### 🔔 Mentions & Tags System
*   **Mentions**: Panggil teman di chat (misal: "Halo adi") -> Player tersebut mendengar suara **"Ting!"** dan mendapat notifikasi Title di layar. Nama mereka di chat juga di-highlight.
*   **Tags**: Koleksi gelar kosmetik via GUI (`/tags`).

### 💤 Modern AFK System
*   **Visual Indikator**: Hologram (TextDisplay) **"💤 Sedang Bermimpi... 💤"** muncul di atas kepala player saat AFK.
*   **Tablist Status**: Nama di tablist berubah jadi abu-abu.
*   **Configurable**: Atur waktu timeout dan teks di `config.yml`.

### ✏️ In-Game Warp Editor
Admin tidak perlu menyentuh config untuk mengubah tampilan Warp.
*   **Cara Pakai**: Buka `/warps edit`, pegang item icon di tangan, lalu **Shift + Klik Kanan** pada warp tujuan. Icon berubah seketika!

---

## 🛠️ Daftar Perintah & Izin Baru

### 👤 Profile & Tier
| Perintah | Deskripsi | Permission |
| :--- | :--- | :--- |
| `/profile` | Buka menu profil | - |
| `/tier` | Buka menu ranking | - |
| `/tier top` | Buka leaderboard global | - |
| `/tags` | Buka koleksi tag chat | - |

### 🛠️ Editor & Admin
| Perintah | Deskripsi | Permission |
| :--- | :--- | :--- |
| `/warps` | Buka menu warp | - |
| `/warps edit` | Buka mode edit warp | `naturalsmp.admin` |
| `/nacore reload` | Reload semua config | `naturalsmp.admin` |

---

## 📁 Struktur Konfigurasi v1.8
*   `config.yml`: Pengaturan utama (AFK, Mention, Economy).
*   `tiers.yml`: Konfigurasi Rank Tier (Nama, Syarat, Suffix).
*   `tags.yml`: Konfigurasi list Chat Tags.
*   `messages.yml`: Pusat kustomisasi pesan.
*   `chatcolor.yml` & `chatemojis.yml`: Modul Chat.

---
**© 2026 NaturalSMP Development Team**
