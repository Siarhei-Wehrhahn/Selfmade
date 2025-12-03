package de.jackson.simplehud;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class SimpleHudPlugin extends JavaPlugin implements CommandExecutor {

    // Spieler, bei denen die HUD aktiv ist
    private final Set<UUID> enabled = new HashSet<>();

    // BossBars pro Spieler
    private final Map<UUID, BossBar> playerBars = new HashMap<>();

    @Override
    public void onEnable() {
        if (getCommand("hud") != null) {
            getCommand("hud").setExecutor(this);
        }

        startHudTask();

        getLogger().info("SimpleHud (BossBar) wurde aktiviert");
    }

    @Override
    public void onDisable() {
        // Aufräumen
        for (BossBar bar : playerBars.values()) {
            bar.removeAll();
        }
        playerBars.clear();
    }

    private void startHudTask() {
        // Alle 20 Ticks (ca. 1x pro Sekunde)
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!enabled.contains(player.getUniqueId())) {
                    continue;
                }

                updateBossBar(player);
            }
        }, 20L, 20L);
    }

    private void updateBossBar(Player player) {
        UUID id = player.getUniqueId();

        BossBar bar = playerBars.get(id);
        if (bar == null) {
            bar = Bukkit.createBossBar(
                    ChatColor.AQUA + "Koordinaten werden geladen...",
                    BarColor.BLUE,
                    BarStyle.SOLID
            );
            bar.setProgress(1.0); // Volle Leiste
            bar.addPlayer(player);
            playerBars.put(id, bar);
        }

        Location loc = player.getLocation();
        World world = player.getWorld();

        long fullTime = world.getFullTime();
        long day = fullTime / 24000L;
        long timeOfDay = world.getTime(); // 0–23999
        double progress = (timeOfDay % 24000L) / 24000.0;

        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();

        String title = ChatColor.AQUA + "XYZ "
                + ChatColor.WHITE + "X: " + x
                + " Y: " + y
                + " Z: " + z
                + ChatColor.GRAY + " | Tag: " + day;

        bar.setTitle(title);

        // Fortschritt der Bossbar = Tagesfortschritt (optional, sieht ganz nice aus)
        bar.setProgress(Math.max(0.0, Math.min(1.0, progress)));
    }

    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args
    ) {
        if (!command.getName().equalsIgnoreCase("hud")) {
            return false;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Dieser Befehl ist nur im Spiel nutzbar.");
            return true;
        }

        UUID id = player.getUniqueId();

        if (enabled.contains(id)) {
            // HUD aus
            enabled.remove(id);

            BossBar bar = playerBars.remove(id);
            if (bar != null) {
                bar.removeAll();
            }

            player.sendMessage(ChatColor.RED + "HUD (Bossbar) ausgeschaltet.");
        } else {
            // HUD an
            enabled.add(id);
            player.sendMessage(ChatColor.GREEN + "HUD (Bossbar) eingeschaltet.");

            // Direkt initial updaten
            updateBossBar(player);
        }

        return true;
    }
}
