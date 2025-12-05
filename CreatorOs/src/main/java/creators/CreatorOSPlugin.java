package de.jackson.creatoros;

import de.jackson.creatoros.commands.CreatorOSCommand;
import de.jackson.creatoros.listeners.QuestProgressListener;
import de.jackson.creatoros.service.PlayerProgressService;
import de.jackson.creatoros.service.QuestService;
import de.jackson.creatoros.service.SeasonService;
import de.jackson.creatoros.storage.PlayerProgressStorage;
import de.jackson.creatoros.storage.SeasonStorage;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class CreatorOSPlugin extends JavaPlugin {

    private SeasonService seasonService;
    private PlayerProgressService progressService;
    private QuestService questService;

    @Override
    public void onEnable() {
        // Storage
        SeasonStorage seasonStorage = new SeasonStorage(this);
        PlayerProgressStorage progressStorage = new PlayerProgressStorage(this);

        // Services
        this.seasonService = new SeasonService(seasonStorage);
        this.progressService = new PlayerProgressService(progressStorage);
        this.questService = new QuestService(seasonService, progressService);

        // Daten laden
        seasonService.loadSeasons();
        progressService.load();

        // Kommando
        PluginCommand creatoros = getCommand("creatoros");
        if (creatoros != null) {
            CreatorOSCommand commandExecutor = new CreatorOSCommand(this, seasonService);
            creatoros.setExecutor(commandExecutor);
            creatoros.setTabCompleter(commandExecutor);
        } else {
            getLogger().warning("Kommando /creatoros konnte nicht registriert werden");
        }

        // Listener
        getServer().getPluginManager().registerEvents(
                new QuestProgressListener(questService, progressService),
                this
        );

        getLogger().info("CreatorOS wurde aktiviert. Seasons: "
                + seasonService.getSeasons().size());
    }

    @Override
    public void onDisable() {
        if (seasonService != null) {
            seasonService.saveSeasons();
        }
        if (progressService != null) {
            progressService.save();
        }
        getLogger().info("CreatorOS wurde deaktiviert");
    }

    public SeasonService getSeasonService() {
        return seasonService;
    }

    public PlayerProgressService getProgressService() {
        return progressService;
    }

    public QuestService getQuestService() {
        return questService;
    }
}
