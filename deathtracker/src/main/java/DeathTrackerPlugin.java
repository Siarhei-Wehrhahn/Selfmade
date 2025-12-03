package de.jackson.deathtracker;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class DeathTrackerPlugin extends JavaPlugin implements Listener, CommandExecutor {

    // Speichert die letzte Todesposition pro Spieler
    private final Map<UUID, Location> lastDeaths = new HashMap<>();

    @Override
    public void onEnable() {
        // Events registrieren
        Bukkit.getPluginManager().registerEvents(this, this);

        // Commands registrieren
        if (getCommand("lastdeath") != null) {
            getCommand("lastdeath").setExecutor(this);
        }
        if (getCommand("lastdeathfollow") != null) {
            getCommand("lastdeathfollow").setExecutor(this);
        }

        getLogger().info("DeathTracker wurde aktiviert");
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Location loc = player.getLocation().clone();

        lastDeaths.put(player.getUniqueId(), loc);

        World world = loc.getWorld();
        String worldName = world != null ? world.getName() : "unbekannt";

        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();

        player.sendMessage(ChatColor.DARK_RED + "Du bist gestorben bei:");
        player.sendMessage(ChatColor.GOLD + "Welt: " + ChatColor.YELLOW + worldName);
        player.sendMessage(ChatColor.GOLD + "XYZ: "
                + ChatColor.YELLOW + x + ChatColor.GOLD + " / "
                + ChatColor.YELLOW + y + ChatColor.GOLD + " / "
                + ChatColor.YELLOW + z);
        player.sendMessage(ChatColor.GRAY + "Nutze "
                + ChatColor.AQUA + "/lastdeath" + ChatColor.GRAY
                + " oder "
                + ChatColor.AQUA + "/lastdeathfollow");
    }

    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args
    ) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Nur Spieler können diesen Befehl nutzen");
            return true;
        }

        UUID id = player.getUniqueId();
        Location loc = lastDeaths.get(id);

        if (loc == null) {
            player.sendMessage(ChatColor.RED + "Es ist keine Todesposition gespeichert");
            return true;
        }

        World world = loc.getWorld();
        String worldName = world != null ? world.getName() : "unbekannt";

        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();

        if (command.getName().equalsIgnoreCase("lastdeath")) {
            // Nur anzeigen
            player.sendMessage(ChatColor.GREEN + "Letzte Todesposition:");
            player.sendMessage(ChatColor.GOLD + "Welt: " + ChatColor.YELLOW + worldName);
            player.sendMessage(ChatColor.GOLD + "XYZ: "
                    + ChatColor.YELLOW + x + ChatColor.GOLD + " / "
                    + ChatColor.YELLOW + y + ChatColor.GOLD + " / "
                    + ChatColor.YELLOW + z);

            return true;
        }

        if (command.getName().equalsIgnoreCase("lastdeathfollow")) {
            // Kompass setzen
            if (world == null) {
                player.sendMessage(ChatColor.RED + "Die Welt deiner Todesposition existiert nicht mehr");
                return true;
            }

            player.setCompassTarget(loc);

            player.sendMessage(ChatColor.LIGHT_PURPLE + "Dein Kompass zeigt jetzt auf deinen Todespunkt");
            player.sendMessage(ChatColor.GOLD + "Welt: " + ChatColor.YELLOW + worldName);
            player.sendMessage(ChatColor.GOLD + "XYZ: "
                    + ChatColor.YELLOW + x + ChatColor.GOLD + " / "
                    + ChatColor.YELLOW + y + ChatColor.GOLD + " / "
                    + ChatColor.YELLOW + z);

            return true;
        }

        return false;
    }
}