package de.jackson.creatoros.storage;

import de.jackson.creatoros.domain.PlayerProgress;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PlayerProgressStorage {

    private final Plugin plugin;
    private final File file;

    public PlayerProgressStorage(Plugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "progress.yml");
    }

    public Map<UUID, PlayerProgress> loadAll() {
        Map<UUID, PlayerProgress> result = new HashMap<>();

        if (!file.exists()) {
            plugin.getDataFolder().mkdirs();
            return result;
        }

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection root = yaml.getConfigurationSection("players");
        if (root == null) {
            return result;
        }

        for (String key : root.getKeys(false)) {
            UUID uuid;
            try {
                uuid = UUID.fromString(key);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Ungültige UUID in progress.yml: " + key);
                continue;
            }

            ConfigurationSection pSec = root.getConfigurationSection(key);
            if (pSec == null) {
                continue;
            }

            PlayerProgress progress = new PlayerProgress(uuid);

            // questProgress
            ConfigurationSection qpSec = pSec.getConfigurationSection("questProgress");
            if (qpSec != null) {
                for (String questId : qpSec.getKeys(false)) {
                    int value = qpSec.getInt(questId, 0);
                    progress.setProgress(questId, value);
                }
            }

            // completedQuests
            List<String> completed = pSec.getStringList("completedQuests");
            if (completed != null) {
                for (String qId : completed) {
                    progress.markCompleted(qId);
                }
            }

            result.put(uuid, progress);
        }

        return result;
    }

    public void saveAll(Map<UUID, PlayerProgress> players) {
        plugin.getDataFolder().mkdirs();

        YamlConfiguration yaml = new YamlConfiguration();
        ConfigurationSection root = yaml.createSection("players");

        for (Map.Entry<UUID, PlayerProgress> entry : players.entrySet()) {
            UUID uuid = entry.getKey();
            PlayerProgress progress = entry.getValue();

            ConfigurationSection pSec = root.createSection(uuid.toString());

            // questProgress
            ConfigurationSection qpSec = pSec.createSection("questProgress");
            for (Map.Entry<String, Integer> qp : progress.getAllProgress().entrySet()) {
                qpSec.set(qp.getKey(), qp.getValue());
            }

            // completedQuests
            pSec.set("completedQuests", new ArrayList<>(progress.getCompletedQuests()));
        }

        try {
            yaml.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Konnte progress.yml nicht speichern: " + e.getMessage());
        }
    }
}
