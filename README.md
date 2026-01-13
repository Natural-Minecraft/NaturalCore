# NaturalCore v1.6.2

Plugin inti (core) untuk server **NaturalSMP**, menangani berbagai modul mulai dari Ekonomi, Chat, Warp, hingga fitur Banners interaktif dan sistem Seasons.

## 🚀 Fitur Baru & Perbaikan (v1.6.2)

### 🖼️ Interactive Banners (Refactored)
*   **Precision Rendering**: Menggunakan `ItemDisplay` dengan offset `0.02` dan skala `1.005` untuk hasil yang menempel sempurna (fit) di permukaan blok tanpa celah.
*   **Anti-Ghosting System**: Mekanisme pembersihan agresif berbasis *Scoreboard Tags* untuk menghapus entitas yang tertinggal meskipun plugin sempat mati mendadak.
*   **Recovery Tools**: Admin dapat melakukan pembersihan total manual dengan perintah `/banner purge`.
*   **Interactive URL**: Klik banner URL memicu prompt browser Minecraft yang modern dengan efek suara.

### 📍 Modular RTP System
*   **Configurable Worlds**: Nama dunia untuk `/rtp` (Survival) dan `/resource` kini dapat diatur melalui `config.yml`.
*   **BetterRTP Integration**: Menjalankan perintah melalui konsol agar lebih stabil dan bypass sistem permission internal yang tidak perlu.
*   **Permission Control**: `/rtp` terbuka untuk semua pemain, sementara `/resource` dapat dibatasi dengan izin `naturalsmp.resource`.

### 🛡️ Core Stability & Persistence
*   **onDisable Hooks**: Menjamin semua data (termasuk status Banner) disimpan dengan aman saat server dimatikan.
*   **Enhanced Utils**: Perbaikan pada `ImageUtils` untuk performa scaling gambar yang lebih cepat dan hemat memori.

---

## 🛠️ Perintah & Izin

| Perintah | Deskripsi | Permission |
| :--- | :--- | :--- |
| `/rtp` | Random Teleport ke dunia Survival | - |
| `/resource` | Random Teleport ke dunia Resource | `naturalsmp.resource` |
| `/banner create` | Membuat banner baru | `naturalsmp.admin.banner` |
| `/banner purge` | Menghapus semua entitas banner hantu | `naturalsmp.admin.banner.purge` |
| `/nacore reload`| Muat ulang semua konfigurasi | `naturalsmp.admin` |

---

## 📁 Struktur Konfigurasi
*   `config.yml`: Pengaturan utama dan world RTP.
*   `messages.yml`: Semua pesan teks (Support HEX Colors).
*   `chatemojis.yml`: Pengaturan emoji chat.
*   `banners/`: Data banner yang disimpan secara dinamis.

---
**© 2026 NaturalSMP Team**