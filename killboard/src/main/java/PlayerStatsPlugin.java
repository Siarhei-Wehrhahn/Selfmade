import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

public final class PlayerStatsPlugin extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        setupScoreboard();

        // Listener registrieren
        Bukkit.getPluginManager().registerEvents(this, this);

        // Für alle Spieler die schon online sind (bei /reload usw.)
        for (Player p : Bukkit.getOnlinePlayers()) {
            applyScoreboard(p);
        }

        getLogger().info("PlayerStats wurde aktiviert");
    }

    @Override
    public void onDisable() {
        getLogger().info("PlayerStats wurde deaktiviert");
    }

    private void setupScoreboard() {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager == null) {
            getLogger().warning("ScoreboardManager ist null");
            return;
        }

        Scoreboard board = manager.getMainScoreboard();

        // Kills im TAB (playerKillCount)
        Objective kills = board.getObjective("stats_kills");
        if (kills == null) {
            try {
                kills = board.registerNewObjective(
                        "stats_kills",
                        "playerKillCount",  // eingebautes Kriterium für Kills
                        ChatColor.GREEN + "Kills"
                );
            } catch (IllegalArgumentException e) {
                kills = board.getObjective("stats_kills");
            }
        }
        if (kills != null) {
            kills.setDisplaySlot(DisplaySlot.PLAYER_LIST);
            kills.setDisplayName(ChatColor.GREEN + "Kills");
        }

        // Tode unter dem Namen (deathCount)
        Objective deaths = board.getObjective("stats_deaths");
        if (deaths == null) {
            try {
                deaths = board.registerNewObjective(
                        "stats_deaths",
                        "deathCount",       // eingebautes Kriterium für Tode
                        ChatColor.RED + "Tode"
                );
            } catch (IllegalArgumentException e) {
                deaths = board.getObjective("stats_deaths");
            }
        }
        if (deaths != null) {
            deaths.setDisplaySlot(DisplaySlot.BELOW_NAME);
            deaths.setDisplayName(ChatColor.RED + "Tode");
        }
    }

    private void applyScoreboard(Player player) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager == null) {
            return;
        }
        Scoreboard board = manager.getMainScoreboard();
        player.setScoreboard(board);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        applyScoreboard(event.getPlayer());
    }
}
