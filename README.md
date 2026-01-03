# 🌿 NaturalCore Plugin

Core plugin untuk NaturalSMP Server dengan berbagai fitur custom untuk meningkatkan gameplay experience!

## 📋 Features

### 🛡️ Admin Commands
- **KickAll** - Kick semua player dengan alasan custom
- **RestartAlert** - Countdown otomatis sebelum restart
- **Broadcast** - Broadcast message dengan format menarik

### 💰 Economy System
- **GiveBalance** - Admin bisa give balance ke player
- **Vault Integration** - Terintegrasi dengan Vault untuk economy

### ⚔️ Altar System (NEW!)
- **Multi-Item Donation** - Support hingga 3 jenis item berbeda
- **DecentHolograms Integration** - Real-time progress hologram
- **Auto-Zone Teleport** - Player otomatis teleport saat masuk zona
- **Wand Tool** - Admin tool untuk setup zona dengan mudah
- Reward system otomatis
- Timer system dengan broadcast updates

### 📖 Quest System (NEW!)
- **NPC Integration** - Menggunakan Citizens API
- **Dialog System** - Realistic NPC conversations dengan delays
- **Item Protection** - Quest item tidak bisa dibuang/disimpan
- **Quest Stages** - Multiple stages (started, collected, done)
- **Death Protection** - Quest item kembali otomatis setelah respawn
- Economy rewards integration

### 🔨 Reforge System
- Upgrade stats item/weapon
- Random stats boost (Damage, Attack Speed, Crit, dll)
- GUI yang user-friendly
- Economy integration

### 🏪 Travelling Trader
- NPC trader yang muncul di jam tertentu
- Jadwal: 12:00-14:00 & 20:00-22:00
- Limited stock system
- Real-time schedule tracking

### 📍 TPA & Teleport System (NEW!)
- **TPA Request** - Player bisa request teleport ke player lain
- **Warmup System** - 3 detik countdown sebelum teleport
- **Movement Detection** - Teleport cancel jika player bergerak
- **Cooldown System** - Prevent spam dengan cooldown
- **Rank-based Cooldown** - Nature: 0s, MVP: 60s, Default: 120s
- **Clickable Messages** - Accept/Deny dengan 1 klik
- **Back Command** - Kembali ke lokasi death dengan warmup
- **Death Location Tracking** - Auto save lokasi death
- **Admin Override** - Admin bisa teleport player lain ke back location

### 📖 Guide System
- In-game guide untuk semua fitur
- Welcome message untuk new player
- Particle effects untuk visual appeal

## 🔧 Installation

1. Download plugin dari releases
2. Masukkan file `.jar` ke folder `plugins/`
3. Install dependencies:
   - **Required**: Vault, Economy Plugin (EssentialsX/CMI)
   - **Optional**: Citizens (untuk Quest), DecentHolograms (untuk Altar)
4. Restart server
5. Configure `config.yml` sesuai kebutuhan

## 📦 Dependencies

### Required
- **Spigot/Paper** 1.20.1+
- **Vault** - Economy integration

### Optional (Recommended)
- **Citizens** - NPC untuk quest system
- **DecentHolograms** - Hologram untuk altar progress
- **Multiverse-Core** - Untuk dungeon world teleport

## ⚙️ Commands

### Admin
```
/kickall [reason] - Kick semua player
/restartalert <detik> - Countdown restart
/broadcast <pesan> - Broadcast message
/givebal <player> <amount> - Give balance
```

### Altar (Admin)
```
/altarwand - Dapatkan altar wand
/altarsetpos1 - Set posisi 1 zona
/altarsetpos2 - Set posisi 2 zona
/altarsettrigger - Set lokasi donasi & hologram
/altarsetwarp - Set lokasi warp altar
/altarsetworld <world> - Set world tujuan dungeon
/altarstart <amount1> [amount2] [amount3] - Mulai altar
/altardelete - Reset altar data
```

### Quest (Admin)
```
/questnpc penagih - Set NPC penagih (yang kamu lihat)
/questnpc petani - Set NPC petani (yang kamu lihat)
/questnpc info - Lihat info quest NPCs
/questnpc reset <player> - Reset quest progress player
```

### Player
```
/dungeon - Teleport ke dungeon (jika altar selesai)
/reforge - Buka reforge menu
/trader <open|info> - Akses trader
/guide [topic] - Lihat panduan
/tpa <player> - Request teleport
/tpaccept (atau /tpyes, /yes) - Terima TPA
/tpdeny (atau /tpno, /no) - Tolak TPA
/back - Kembali ke lokasi death
```

## 🔐 Permissions

### Admin
```yaml
naturalcore.admin.* - Semua admin commands
naturalcore.admin.kickall - KickAll command
naturalcore.admin.restartalert - RestartAlert command
naturalcore.admin.broadcast - Broadcast command
naturalcore.admin.givebal - GiveBalance command
naturalcore.admin.bypass - Bypass restrictions
```

### Altar
```yaml
naturalcore.altar.admin - Altar admin commands
naturalcore.dungeon - Akses dungeon teleport
```

### Quest
```yaml
naturalcore.quest.admin - Quest admin commands
```

### Teleport
```yaml
naturalcore.tpa - TPA commands
naturalcore.tpa.bypass - Bypass TPA cooldown
naturalcore.back - Back command
naturalcore.back.others - Teleport player lain ke back location
```

### Rank-Based
```yaml
naturalcore.nature - Rank Nature (no dungeon cooldown)
naturalcore.mvp - Rank MVP (reduced cooldown)
```

### Player
```yaml
naturalcore.reforge - Reforge system
naturalcore.trader - Trader system
naturalcore.guide - Guide commands
```

## 📁 Project Structure

```
NaturalCore/
├── src/main/java/id/naturalsmp/naturalcore/
│   ├── NaturalCore.java (Main Class)
│   ├── admin/ (Admin Commands)
│   ├── economy/ (Economy & Vault)
│   ├── altar/ (Altar & Dungeon System)
│   ├── quest/ (Quest & NPC System) ⭐ NEW
│   ├── reforge/ (Reforge System)
│   ├── trader/ (Trader System)
│   ├── guide/ (Guide & Tutorial)
│   ├── teleport/ (TPA & Back System) ⭐ NEW
│   └── utils/ (Utility Classes)
└── src/main/resources/
    ├── plugin.yml
    └── config.yml
```

## 🛠️ Building

### Maven
```bash
mvn clean package
```

Output file akan ada di `target/NaturalCore-1.0.0.jar`

## 📝 Configuration

### Teleport Settings (config.yml)
```yaml
teleport:
  warmup-seconds: 3  # Warmup sebelum teleport
  tpa-cooldown-seconds: 60  # Cooldown TPA request
  request-timeout-seconds: 60  # Timeout TPA request
```

### Quest Settings
- Setup NPC dengan `/questnpc penagih` dan `/questnpc petani`
- Lihat NPC yang kamu inginkan, lalu run command
- UUID NPC akan auto-save di config

### Altar Settings
1. Get wand: `/altarwand`
2. Set zona: Klik kiri & kanan dengan wand
3. Set trigger: `/altarsettrigger` (lihat blok donasi)
4. Set warp: `/altarsetwarp` (lokasi player saat ini)
5. Set world: `/altarsetworld dungeon`
6. Start altar: `/altarstart 64 32 16` (pegang item di hotbar 1,2,3)

## ✨ New Features Highlight

### Quest System
- **Realistic Dialog**: NPC berbicara dengan delay natural
- **Anti-Spam**: Cooldown untuk prevent spam dialog
- **Item Protection**: Quest item tidak bisa hilang
- **Multiple Stages**: Quest progression dengan 4 stages
- **Rewards**: Integrasi dengan Vault economy

### TPA System
- **Clickable UI**: Accept/Deny dengan sekali klik
- **Smart Warmup**: Countdown dengan movement detection
- **Rank Benefits**: Nature rank tanpa cooldown
- **Back on Death**: Auto save & clickable back button
- **Admin Tools**: Admin bisa teleport player ke back location

### Improved Altar
- **Multi-Item**: Support hingga 3 item berbeda
- **Real-time Hologram**: Progress update otomatis
- **Auto Teleport**: Player masuk zona = auto teleport
- **Easy Setup**: Wand tool untuk admin

## 🤝 Contributing

Contributions are welcome! Silakan:
1. Fork repository
2. Create feature branch
3. Commit changes
4. Push ke branch
5. Create Pull Request

## 📞 Support

Untuk bug reports atau feature requests, silakan buat issue di repository ini.

## 📄 License

Copyright © 2024 NaturalSMP. All rights reserved.

---

**Made with ❤️ for NaturalSMP Community**

## 🎮 Credits

- **Original Skript**: NaturalSMP Team
- **Plugin Conversion**: Claude AI Assistant
- **APIs Used**: Vault, Citizens, DecentHolograms
