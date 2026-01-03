package id.naturalsmp.naturalcore.quest;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class QuestManager {

    private final NaturalCore plugin;
    private final Map<UUID, String> questProgress; // player UUID -> quest stage
    private final Map<UUID, Long> dialogCooldown; // player UUID -> last dialog time
    private UUID npcPenagihUUID;
    private UUID npcPetaniUUID;
    
    // Quest item
    private ItemStack questItem;
    private final int rewardMoney = 20000;
    
    public QuestManager(NaturalCore plugin) {
        this.plugin = plugin;
        this.questProgress = new HashMap<>();
        this.dialogCooldown = new HashMap<>();
        
        // Create quest item
        this.questItem = new ItemBuilder(Material.PAPER)
                .setName("&e&lAmplop Hutang")
                .setLore(
                    "&7Berikan kepada &bHaikalMabrur",
                    "&c&lITEM QUEST (Tidak bisa dibuang)"
                )
                .build();
                
        loadNPCData();
    }
    
    private void loadNPCData() {
        // Load from config
        if (plugin.getConfig().contains("quest.npc.penagih")) {
            String uuidStr = plugin.getConfig().getString("quest.npc.penagih");
            npcPenagihUUID = UUID.fromString(uuidStr);
        }
        if (plugin.getConfig().contains("quest.npc.petani")) {
            String uuidStr = plugin.getConfig().getString("quest.npc.petani");
            npcPetaniUUID = UUID.fromString(uuidStr);
        }
    }
    
    public void saveNPCData() {
        if (npcPenagihUUID != null) {
            plugin.getConfig().set("quest.npc.penagih", npcPenagihUUID.toString());
        }
        if (npcPetaniUUID != null) {
            plugin.getConfig().set("quest.npc.petani", npcPetaniUUID.toString());
        }
        plugin.saveConfig();
    }
    
    public void setNPCPenagih(UUID uuid) {
        this.npcPenagihUUID = uuid;
        saveNPCData();
    }
    
    public void setNPCPetani(UUID uuid) {
        this.npcPetaniUUID = uuid;
        saveNPCData();
    }
    
    public UUID getNPCPenagihUUID() {
        return npcPenagihUUID;
    }
    
    public UUID getNPCPetaniUUID() {
        return npcPetaniUUID;
    }
    
    public String getQuestStage(Player player) {
        return questProgress.getOrDefault(player.getUniqueId(), "none");
    }
    
    public void setQuestStage(Player player, String stage) {
        questProgress.put(player.getUniqueId(), stage);
    }
    
    public boolean isInDialog(Player player) {
        if (!dialogCooldown.containsKey(player.getUniqueId())) {
            return false;
        }
        long lastDialog = dialogCooldown.get(player.getUniqueId());
        return (System.currentTimeMillis() - lastDialog) < 10000; // 10 seconds cooldown
    }
    
    public void startDialog(Player player) {
        dialogCooldown.put(player.getUniqueId(), System.currentTimeMillis());
    }
    
    public void endDialog(Player player) {
        dialogCooldown.remove(player.getUniqueId());
    }
    
    public ItemStack getQuestItem() {
        return questItem.clone();
    }
    
    public boolean isQuestItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        String displayName = ChatUtils.stripColor(item.getItemMeta().getDisplayName());
        return displayName.equals("Amplop Hutang");
    }
    
    // Dialog dengan NPC Penagih (Haikal)
    public void handlePenagihDialog(Player player) {
        if (isInDialog(player)) {
            return;
        }
        
        startDialog(player);
        String stage = getQuestStage(player);
        
        // Quest sudah selesai
        if (stage.equals("done")) {
            player.sendMessage(ChatUtils.color("&fHaikalMabrur &e»&f Ah, kamu. Makasih ya udah bantuin waktu itu!"));
            new BukkitRunnable() {
                @Override
                public void run() {
                    player.sendMessage(ChatUtils.color("&fHaikalMabrur &e»&f Aku betah disini, pemandangannya bagus."));
                    endDialog(player);
                }
            }.runTaskLater(plugin, 40L);
            return;
        }
        
        // Player sudah collect amplop, mau report balik
        if (stage.equals("collected")) {
            if (hasQuestItem(player)) {
                player.sendMessage(ChatUtils.color("&b" + player.getName() + " &8»&f Nih bang amplopnya, sesuai pesanan."));
                
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        removeQuestItem(player);
                        player.playSound(player.getLocation(), "ENTITY_EXPERIENCE_ORB_PICKUP", 1f, 1f);
                        player.sendMessage(ChatUtils.color("&fHaikalMabrur &e»&f Wah, akhirnya bayar juga tuh orang tua."));
                        
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                player.sendMessage(ChatUtils.color("&fHaikalMabrur &e»&f Makasih banyak bro! Sesuai janji, ini jatah lu."));
                                
                                // Give reward
                                if (plugin.getVaultManager().hasEconomy()) {
                                    plugin.getVaultManager().getEconomy().depositPlayer(player, rewardMoney);
                                    player.sendMessage(ChatUtils.color("&6&l📜 &x&F&F&C&E&4&7&lS&x&F&F&B&C&4&2&lT&x&F&F&A&B&3&D&lO&x&F&F&9&9&3&8&lR&x&F&F&8&8&3&3&lY &8» &a+ Rp " + rewardMoney + " ditambahkan ke akunmu."));
                                }
                                
                                setQuestStage(player, "done");
                                player.getWorld().spawnParticle(org.bukkit.Particle.VILLAGER_HAPPY, player.getLocation().add(0, 2, 0), 5, 0.5, 0.5, 0.5, 0);
                                endDialog(player);
                            }
                        }.runTaskLater(plugin, 40L);
                    }
                }.runTaskLater(plugin, 40L);
            } else {
                player.sendMessage(ChatUtils.color("&fHaikalMabrur &e»&f Sudah belum bro? Mana amplopnya?"));
                endDialog(player);
            }
            return;
        }
        
        // Player sedang dalam quest, belum ketemu farmer
        if (stage.equals("started")) {
            player.sendMessage(ChatUtils.color("&fHaikalMabrur &e»&f Duh, buruan cari si &aFarmer&f itu. Gue butuh duitnya nih."));
            endDialog(player);
            return;
        }
        
        // Mulai quest pertama kali
        player.playSound(player.getLocation(), "ENTITY_VILLAGER_NO", 1f, 1f);
        player.sendMessage(ChatUtils.color("&fHaikalMabrur &e»&f Aduhh, ini si farmer kemana sih?"));
        
        new BukkitRunnable() {
            int step = 0;
            @Override
            public void run() {
                step++;
                switch(step) {
                    case 1:
                        player.sendMessage(ChatUtils.color("&b" + player.getName() + " &8»&f Kenapa bang? Kok bingung gitu?"));
                        break;
                    case 2:
                        player.sendMessage(ChatUtils.color("&fHaikalMabrur &e»&f Eh lu liat si Farmer ga?"));
                        break;
                    case 3:
                        player.sendMessage(ChatUtils.color("&fHaikalMabrur &e»&f Dia ada utang &c40k &fke gw, dari minggu lalu ngilang mulu."));
                        break;
                    case 4:
                        player.sendMessage(ChatUtils.color("&b" + player.getName() + " &8»&f Wah parah juga. Mau dibantuin nagih ga?"));
                        break;
                    case 5:
                        player.sendMessage(ChatUtils.color("&fHaikalMabrur &e»&f Boleh tuh! Tolong tagihin ya, ntar gw kasih lu &a20k &fdeh."));
                        player.playSound(player.getLocation(), "UI_TOAST_CHALLENGE_COMPLETE", 1f, 1f);
                        player.sendActionBar(ChatUtils.color("&e&lQUEST STARTED: &fTagih Utang Petani"));
                        setQuestStage(player, "started");
                        endDialog(player);
                        cancel();
                        break;
                }
            }
        }.runTaskTimer(plugin, 40L, 40L);
    }
    
    // Dialog dengan NPC Petani
    public void handlePetaniDialog(Player player) {
        if (isInDialog(player)) {
            return;
        }
        
        String stage = getQuestStage(player);
        
        // Hanya bisa interact kalau quest aktif
        if (!stage.equals("started")) {
            return; // Biarkan Citizens handle normal interaction
        }
        
        startDialog(player);
        setQuestStage(player, "collected");
        
        player.sendMessage(ChatUtils.color("&b" + player.getName() + " &8»&f Woi pak, dicariin Haikal tuh. Bayar utangnya!"));
        
        new BukkitRunnable() {
            int step = 0;
            @Override
            public void run() {
                step++;
                switch(step) {
                    case 1:
                        player.playSound(player.getLocation(), "ENTITY_VILLAGER_TRADE", 1f, 1f);
                        player.sendMessage(ChatUtils.color("&a&lFarmer &e»&f Eh? Oh iya iya, astaga saya lupaa..."));
                        break;
                    case 2:
                        player.sendMessage(ChatUtils.color("&a&lFarmer &e»&f Maklum nak, saya sudah tua, pikun."));
                        break;
                    case 3:
                        player.sendMessage(ChatUtils.color("&a&lFarmer &e»&f Ini, tolong sampaikan maaf saya ke &bHaikal &fya."));
                        player.getInventory().addItem(getQuestItem());
                        player.playSound(player.getLocation(), "ENTITY_ITEM_PICKUP", 1f, 1f);
                        player.sendActionBar(ChatUtils.color("&a&lITEM GET: &fAmplop Hutang"));
                        endDialog(player);
                        cancel();
                        break;
                }
            }
        }.runTaskTimer(plugin, 40L, 40L);
    }
    
    private boolean hasQuestItem(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (isQuestItem(item)) {
                return true;
            }
        }
        return false;
    }
    
    private void removeQuestItem(Player player) {
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (isQuestItem(item)) {
                player.getInventory().setItem(i, null);
                return;
            }
        }
    }
    
    public void returnQuestItem(Player player) {
        String stage = getQuestStage(player);
        if (stage.equals("collected") && !hasQuestItem(player)) {
            player.getInventory().addItem(getQuestItem());
            player.sendMessage(ChatUtils.color("&6&l📜 &x&F&F&C&E&4&7&lS&x&F&F&B&C&4&2&lT&x&F&F&A&B&3&D&lO&x&F&F&9&9&3&8&lR&x&F&F&8&8&3&3&lY &8» &aItem Quest &f'Amplop Hutang' &adikembalikan ke tasmu."));
        }
    }
}
