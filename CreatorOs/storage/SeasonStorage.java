package de.jackson.creatoros.storage;

import de.jackson.creatoros.domain.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SeasonStorage {

    private final Plugin plugin;
    private final File file;

    public SeasonStorage(Plugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "seasons.yml");
    }

    public List<Season> loadSeasons() {
        List<Season> result = new ArrayList<>();

        if (!file.exists()) {
            plugin.getDataFolder().mkdirs();
            return result;
        }

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        List<?> seasonList = yaml.getList("seasons");

        if (seasonList == null) {
            return result;
        }

        for (Object obj : seasonList) {
            if (!(obj instanceof ConfigurationSection section)) {
                continue;
            }
        }

        // Alternativer Weg: wir gehen über die Keys
        ConfigurationSection root = yaml.getConfigurationSection("seasons");
        if (root == null) {
            return result;
        }

        for (String key : root.getKeys(false)) {
            ConfigurationSection section = root.getConfigurationSection(key);
            if (section == null) {
                continue;
            }

            String id = section.getString("id", key);
            String name = section.getString("name", "Unnamed Season");
            String description = section.getString("description", "");
            String startDate = section.getString("startDate", "");
            String endDate = section.getString("endDate", "");
            String statusStr = section.getString("status", "PLANNED");

            SeasonStatus status;
            try {
                status = SeasonStatus.valueOf(statusStr.toUpperCase());
            } catch (IllegalArgumentException ex) {
                status = SeasonStatus.PLANNED;
            }

            Season season = new Season(id, name, description, startDate, endDate, status);

            // Quests
            ConfigurationSection questsSection = section.getConfigurationSection("quests");
            if (questsSection != null) {
                for (String qKey : questsSection.getKeys(false)) {
                    ConfigurationSection qSec = questsSection.getConfigurationSection(qKey);
                    if (qSec == null) {
                        continue;
                    }

                    String qId = qSec.getString("id", qKey);
                    String qName = qSec.getString("name", "Unnamed Quest");
                    String qDesc = qSec.getString("description", "");
                    String typeStr = qSec.getString("type", "GATHER");

                    QuestType type;
                    try {
                        type = QuestType.valueOf(typeStr.toUpperCase());
                    } catch (IllegalArgumentException ex) {
                        type = QuestType.GATHER;
                    }

                    de.jackson.creatoros.domain.Quest quest =
                            new de.jackson.creatoros.domain.Quest(qId, qName, qDesc, type);

                    quest.setTargetAmount(qSec.getInt("targetAmount", 0));
                    quest.setTargetId(qSec.getString("targetId", ""));
                    quest.setRepeatable(qSec.getBoolean("repeatable", false));

                    // Rewards
                    ConfigurationSection rewardsSection = qSec.getConfigurationSection("rewards");
                    if (rewardsSection != null) {
                        for (String rKey : rewardsSection.getKeys(false)) {
                            ConfigurationSection rSec = rewardsSection.getConfigurationSection(rKey);
                            if (rSec == null) {
                                continue;
                            }

                            String rTypeStr = rSec.getString("type", "ITEM");
                            RewardType rType;
                            try {
                                rType = RewardType.valueOf(rTypeStr.toUpperCase());
                            } catch (IllegalArgumentException ex) {
                                rType = RewardType.ITEM;
                            }

                            Reward reward = new Reward(rType);
                            reward.setItemMaterial(rSec.getString("itemMaterial"));
                            reward.setItemAmount(rSec.getInt("itemAmount", 0));
                            reward.setMoneyAmount(rSec.getDouble("moneyAmount", 0.0));
                            reward.setText(rSec.getString("text"));

                            quest.getRewards().add(reward);
                        }
                    }

                    season.getQuests().add(quest);
                }
            }

            result.add(season);
        }

        return result;
    }

    public void saveSeasons(List<Season> seasons) {
        YamlConfiguration yaml = new YamlConfiguration();
        ConfigurationSection root = yaml.createSection("seasons");

        int index = 0;
        for (Season season : seasons) {
            ConfigurationSection section = root.createSection(String.valueOf(index++));

            section.set("id", season.getId());
            section.set("name", season.getName());
            section.set("description", season.getDescription());
            section.set("startDate", season.getStartDate());
            section.set("endDate", season.getEndDate());
            section.set("status", season.getStatus().name());

            ConfigurationSection questsSection = section.createSection("quests");
            int qIndex = 0;
            for (de.jackson.creatoros.domain.Quest quest : season.getQuests()) {
                ConfigurationSection qSec = questsSection.createSection(String.valueOf(qIndex++));
                qSec.set("id", quest.getId());
                qSec.set("name", quest.getName());
                qSec.set("description", quest.getDescription());
                qSec.set("type", quest.getType().name());
                qSec.set("targetAmount", quest.getTargetAmount());
                qSec.set("targetId", quest.getTargetId());
                qSec.set("repeatable", quest.isRepeatable());

                ConfigurationSection rewardsSection = qSec.createSection("rewards");
                int rIndex = 0;
                for (Reward reward : quest.getRewards()) {
                    ConfigurationSection rSec = rewardsSection.createSection(String.valueOf(rIndex++));
                    rSec.set("type", reward.getType().name());
                    rSec.set("itemMaterial", reward.getItemMaterial());
                    rSec.set("itemAmount", reward.getItemAmount());
                    rSec.set("moneyAmount", reward.getMoneyAmount());
                    rSec.set("text", reward.getText());
                }
            }
        }

        try {
            yaml.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Konnte seasons.yml nicht speichern: " + e.getMessage());
        }
    }
}
