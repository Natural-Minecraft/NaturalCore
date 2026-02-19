import yaml
import random

def generate_games():
    games = []
    
    # 1. TRIVIA (Base)
    trivia_pool = [
        ("Berapa max level enchant di Minecraft?", "30"),
        ("Blok apa yang paling keras di Minecraft?", "BEDROCK"),
        ("Berapa HP max player tanpa armor?", "20"),
        ("Dimensi apa yang punya Ender Dragon?", "END"),
        ("Berapa jumlah slot hotbar?", "9"),
        ("Mob apa yang drop Gunpowder?", "CREEPER"),
        ("Berapa blok 1 chunk?", "16"),
        ("Tool apa untuk menambang Diamond?", "IRON PICKAXE"),
        ("Berapa banyak obsidian untuk Nether Portal?", "10"),
        ("Item apa untuk menjinakkan kucing?", "RAW COD"),
        ("Biome apa yang ada Mooshroom?", "MUSHROOM"),
        ("Berapa max stack item biasa?", "64"),
        ("Blok apa yang digunakan untuk craft Beacon?", "GLASS"),
        ("Berapa Eye of Ender untuk End Portal?", "12"),
        ("Villager bertukar pakai mata uang apa?", "EMERALD")
    ]
    
    for _ in range(250):
        q, a = random.choice(trivia_pool)
        games.append({
            "type": "TRIVIA",
            "title": "Trivia Minecraft",
            "question": q,
            "answer": a
        })
        
    # 2. MATH
    for _ in range(250):
        op = random.randint(0, 3)
        if op == 0:
            a, b = random.randint(10, 100), random.randint(10, 100)
            ans, sym = a + b, "+"
        elif op == 1:
            a = random.randint(20, 100)
            b = random.randint(5, a)
            ans, sym = a - b, "-"
        elif op == 2:
            a, b = random.randint(2, 13), random.randint(2, 13)
            ans, sym = a * b, "×"
        else:
            b, ans = random.randint(2, 13), random.randint(2, 13)
            a, sym = b * ans, "÷"
            
        games.append({
            "type": "MATH",
            "title": "Matematika",
            "question": f"Berapa {a} {sym} {b} ?",
            "answer": str(ans)
        })

    # 3. UNSCRAMBLE
    unscramble_words = [
        ("DIAMOND", "BERLIAN"), ("EMERALD", "ZAMRUD"), ("CREEPER", "MOB HIJAU"),
        ("REDSTONE", "BATU MERAH"), ("OBSIDIAN", "BATU HITAM"), ("ENDERMAN", "MOB TINGGI"),
        ("VILLAGER", "PENDUDUK"), ("NETHERITE", "ORE TERKUAT"), ("SKELETON", "MOB TULANG"),
        ("ENCHANT", "SIHIR"), ("POTION", "RAMUAN"), ("BEACON", "SUAR"),
        ("TRIDENT", "TRISULA"), ("FURNACE", "TUNGKU"), ("PICKAXE", "BELIUNG"),
        ("IRON", "Besi"), ("GOLD", "Emas"), ("LAPIS", "Biru"), 
        ("APPLE", "Buah"), ("SWORD", "Pedang"), ("SHIELD", "Perisai")
    ]
    for _ in range(250):
        word, hint = random.choice(unscramble_words)
        scrambled = "".join(random.sample(word, len(word)))
        while scrambled == word:
            scrambled = "".join(random.sample(word, len(word)))
            
        games.append({
            "type": "UNSCRAMBLE",
            "title": "Susun Kata",
            "question": f"Susun huruf ini: &f&l{scrambled} &7(Hint: {hint})",
            "answer": word
        })
        
    # 4. TYPE_RACE
    phrases = [
        "NaturalSMP Server Terbaik", "Minecraft Survival Multiplayer",
        "Diamond Pickaxe Unbreaking", "Ender Dragon telah dikalahkan",
        "Jangan lupa tidur malam ini", "Creeper oh man jangan meledak",
        "Villager Trading Iron Golem", "Nether Fortress Blaze Spawner",
        "Enchanting Table Bookshelf Level", "Redstone Repeater Comparator Piston",
        "Bermain bersama teman sangat seru", "Ayo bangun rumah yang bagus",
        "Beli barang di server shop", "Temukan diamond di gua", "Jangan lupa vote hari ini"
    ]
    for _ in range(250):
        phrase = random.choice(phrases)
        games.append({
            "type": "TYPE_RACE",
            "title": "Ketik Cepat",
            "question": f"Ketik: &f&l{phrase}",
            "answer": phrase
        })

    # Add Rewards and shuffle
    random.shuffle(games)
    
    formatted_games = {}
    for i, game in enumerate(games):
        roll = random.randint(0, 99)
        if roll < 40:
            amt = random.randint(3, 8)
            reward = {"type": "ITEM", "material": "IRON_INGOT", "amount": amt, "displayText": f"&#AAAAAA&l{amt}x Iron Ingot"}
        elif roll < 70:
            money = random.randint(5, 15) * 100
            reward = {"type": "MONEY", "amount": float(money), "displayText": f"&#55FF55&lRp{money:,}"}
        elif roll < 90:
            reward = {"type": "ITEM", "material": "DIAMOND", "amount": 1, "displayText": "&#55FFFF&l1x Diamond"}
        else:
            reward = {"type": "MONEY_AND_ITEM", "material": "DIAMOND", "itemAmount": 2, "moneyAmount": 1000.0, "displayText": "&#55FFFF&l2x Diamond &7+ &#55FF55&lRp1.000"}
            
        game["reward"] = reward
        formatted_games[f"game_{i+1}"] = game

    with open("src/main/resources/chat-games.yml", "w", encoding='utf-8') as f:
        yaml.dump({"games": formatted_games}, f, allow_unicode=True, sort_keys=False)

if __name__ == "__main__":
    generate_games()
    print("Generated 1000 games in src/main/resources/chat-games.yml")
