package de.jackson.creatoros.service;

import de.jackson.creatoros.domain.PlayerProgress;
import de.jackson.creatoros.domain.Quest;
import de.jackson.creatoros.domain.QuestType;
import de.jackson.creatoros.domain.Reward;
import de.jackson.creatoros.domain.RewardType;
import de.jackson.creatoros.domain.Season;
import de.jackson.creatoros.domain.SeasonStatus;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class QuestService {

    private final SeasonService seasonService;
    private final PlayerProgressService progressService;

    public QuestService(SeasonService seasonService, PlayerProgressService progressService) {
        this.seasonService = seasonService;
        this.progressService = progressService;
    }

    public List<Quest> getAllQuests() {
        List<Quest> quests = new ArrayList<>();
        for (Season season : seasonService.getSeasons()) {
            quests.addAll(season.getQuests());
        }
        return quests;
    }

    public List<Quest> getActiveQuests() {
        List<Quest> quests = new ArrayList<>();
        for (Season season : seasonService.getSeasons()) {
            if (season.getStatus() == SeasonStatus.ACTIVE) {
                quests.addAll(season.getQuests());
            }
        }
        return quests;
    }

    /**
     * Aufruf durch Listener wenn ein Block abgebaut wurde
     */
    public void handleBlockBreak(Player player, Material material) {
        UUID uuid = player.getUniqueId();
        PlayerProgress progress = progressService.getOrCreate(uuid);

        String materialName = material.name().toUpperCase(Locale.ROOT);

        for (Quest quest : getActiveQuests()) {
            if (quest.getType() != QuestType.GATHER) {
                continue;
            }

            if (quest.getTargetId() == null || quest.getTargetId().isEmpty()) {
                continue;
            }

            if (!quest.getTargetId().equalsIgnoreCase(materialName)) {
                continue;
            }

            if (progress.isCompleted(quest.getId())) {
                continue;
            }

            int target = quest.getTargetAmount();
            if (target <= 0) {
                continue;
            }

            int current = progress.getProgress(quest.getId());
            current = current + 1;
            progress.setProgress(quest.getId(), current);

            // kleine Info Nachricht
            player.sendActionBar(ChatColor.AQUA + quest.getName()
                    + ChatColor.GRAY + " " + current + "/" + target);

            if (current >= target) {
                progress.markCompleted(quest.getId());
                grantRewards(player, quest);
                player.sendMessage(ChatColor.GREEN + "Quest abgeschlossen: "
                        + ChatColor.YELLOW + quest.getName());
            }
        }
    }

    private void grantRewards(Player player, Quest quest) {
        for (Reward reward : quest.getRewards()) {
            if (reward.getType() == RewardType.ITEM) {
                giveItemReward(player, reward);
            } else if (reward.getType() == RewardType.MONEY) {
                // Vault kommt später
                player.sendMessage(ChatColor.GRAY
                        + "(Geld Belohnung noch nicht implementiert)");
            } else if (reward.getType() == RewardType.TITLE
                    || reward.getType() == RewardType.TAG) {
                // Titel / Tags kommen später
                player.sendMessage(ChatColor.GRAY
                        + "(Titel oder Tag Belohnung noch nicht implementiert)");
            }
        }
    }

    private void giveItemReward(Player player, Reward reward) {
        if (reward.getItemMaterial() == null || reward.getItemMaterial().isEmpty()) {
            return;
        }

        Material mat;
        try {
            mat = Material.valueOf(reward.getItemMaterial().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            Bukkit.getLogger().warning("Ungültiges Item Material in Reward: "
                    + reward.getItemMaterial());
            return;
        }

        int amount = Math.max(1, reward.getItemAmount());
        ItemStack stack = new ItemStack(mat, amount);
        player.getInventory().addItem(stack);
        player.sendMessage(ChatColor.GOLD + "Du hast eine Belohnung erhalten: "
                + ChatColor.YELLOW + amount + "x " + mat.name());
    }
}
