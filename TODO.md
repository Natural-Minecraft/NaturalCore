# Fix sendActionBar Compilation Errors

## Files to Update
- [x] src/main/java/id/naturalsmp/naturalcore/altar/AltarManager.java
- [x] src/main/java/id/naturalsmp/naturalcore/quest/QuestManager.java
- [x] src/main/java/id/naturalsmp/naturalcore/teleport/TPACommand.java
- [x] src/main/java/id/naturalsmp/naturalcore/teleport/TPAAcceptCommand.java
- [x] src/main/java/id/naturalsmp/naturalcore/teleport/BackCommand.java

## Changes Needed
- Add import: `import net.md_5.bungee.api.ChatMessageType;`
- Replace `player.sendActionBar(message)` with `player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message))`
