package de.jackson.creatoros;

import de.jackson.creatoros.service.SeasonService;
import de.jackson.creatoros.storage.SeasonStorage;
import org.bukkit.plugin.java.JavaPlugin;

public final class CreatorOSPlugin extends JavaPlugin {

    private SeasonService seasonService;

    @Override
    public void onEnable() {
        // Storage und Service initialisieren
        SeasonStorage seasonStorage = new SeasonStorage(this);
        this.seasonService = new SeasonService(seasonStorage);

        // Seasons beim Start laden
        seasonService.loadSeasons();

        getLogger().info("CreatorOS wurde aktiviert. Geladene Seasons: " 
                + seasonService.getSeasons().size());
    }

    @Override
    public void onDisable() {
        // Seasons beim Stop speichern
        if (seasonService != null) {
            seasonService.saveSeasons();
        }

        getLogger().info("CreatorOS wurde deaktiviert.");
    }

    public SeasonService getSeasonService() {
        return seasonService;
    }
}
