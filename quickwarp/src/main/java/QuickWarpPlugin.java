package de.jackson.quickwarp;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;

public final class QuickWarpPlugin extends JavaPlugin implements CommandExecutor {

    @Override
    public void onEnable() {
        // Config mit Standardwerten laden
        saveDefaultConfig();

        if (getCommand("setwarp") != null) {
            getCommand("setwarp").setExecutor(this);
        }
        if (getCommand("delwarp") != null) {
            getCommand("delwarp").setExecutor(this);
        }
        if (getCommand("warp") != null) {
            getCommand("warp").setExecutor(this);
        }
        if (getCommand("warps") != null) {
            getCommand("warps").setExecutor(this);
        }

        getLogger().info("QuickWarp wurde aktiviert");
    }

    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args
    ) {
        String name = command.getName().toLowerCase();

        if (name.equals("setwarp")) {
            return handleSetWarp(sender, args);
        }
        if (name.equals("delwarp")) {
            return handleDelWarp(sender, args);
        }
        if (name.equals("warp")) {
            return handleWarp(sender, args);
        }
        if (name.equals("warps")) {
            return handleWarps(sender);
        }

        return false;
    }

    private boolean handleSetWarp(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Nur Spieler können diesen Befehl nutzen");
            return true;
        }

        if (!player.hasPermission("quickwarp.set")) {
            player.sendMessage(ChatColor.RED + "Du darfst keine Warps setzen");
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Benutzung: /setwarp <name>");
            return true;
        }

        String warpName = args[0].toLowerCase();

        Location loc = player.getLocation();
        World world = loc.getWorld();
        if (world == null) {
            player.sendMessage(ChatColor.RED + "Deine Welt konnte nicht ermittelt werden");
            return true;
        }

        String base = "warps." + warpName;

        getConfig().set(base + ".world", world.getName());
        getConfig().set(base + ".x", loc.getX());
        getConfig().set(base + ".y", loc.getY());
        getConfig().set(base + ".z", loc.getZ());
        getConfig().set(base + ".yaw", loc.getYaw());
        getConfig().set(base + ".pitch", loc.getPitch());

        saveConfig();

        player.sendMessage(ChatColor.GREEN + "Warp " + ChatColor.AQUA + warpName
                + ChatColor.GREEN + " wurde gespeichert");
        return true;
    }

    private boolean handleDelWarp(CommandSender sender, String[] args) {
        if (!sender.hasPermission("quickwarp.del")) {
            sender.sendMessage(ChatColor.RED + "Du darfst keine Warps löschen");
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Benutzung: /delwarp <name>");
            return true;
        }

        String warpName = args[0].toLowerCase();
        String base = "warps." + warpName;

        if (!getConfig().isConfigurationSection(base)) {
            sender.sendMessage(ChatColor.RED + "Warp " + warpName + " existiert nicht");
            return true;
        }

        getConfig().set(base, null);
        saveConfig();

        sender.sendMessage(ChatColor.YELLOW + "Warp " + warpName + " wurde gelöscht");
        return true;
    }

    private boolean handleWarp(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Nur Spieler können diesen Befehl nutzen");
            return true;
        }

        if (!player.hasPermission("quickwarp.warp")) {
            player.sendMessage(ChatColor.RED + "Du darfst keine Warps benutzen");
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Benutzung: /warp <name>");
            return true;
        }

        String warpName = args[0].toLowerCase();
        String base = "warps." + warpName;

        if (!getConfig().isConfigurationSection(base)) {
            player.sendMessage(ChatColor.RED + "Warp " + warpName + " existiert nicht");
            return true;
        }

        ConfigurationSection sec = getConfig().getConfigurationSection(base);
        if (sec == null) {
            player.sendMessage(ChatColor.RED + "Warp " + warpName + " ist beschädigt");
            return true;
        }

        String worldName = sec.getString("world");
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            player.sendMessage(ChatColor.RED + "Welt " + worldName + " existiert nicht");
            return true;
        }

        double x = sec.getDouble("x");
        double y = sec.getDouble("y");
        double z = sec.getDouble("z");
        float yaw = (float) sec.getDouble("yaw");
        float pitch = (float) sec.getDouble("pitch");

        Location target = new Location(world, x, y, z, yaw, pitch);
        player.teleport(target);

        player.sendMessage(ChatColor.GREEN + "Teleportiert zu " + ChatColor.AQUA + warpName);
        return true;
    }

    private boolean handleWarps(CommandSender sender) {
        if (!sender.hasPermission("quickwarp.warps")) {
            sender.sendMessage(ChatColor.RED + "Du darfst die Warps nicht sehen");
            return true;
        }

        ConfigurationSection section = getConfig().getConfigurationSection("warps");
        if (section == null) {
            sender.sendMessage(ChatColor.YELLOW + "Es sind keine Warps gespeichert");
            return true;
        }

        Set<String> keys = section.getKeys(false);
        if (keys.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "Es sind keine Warps gespeichert");
            return true;
        }

        sender.sendMessage(ChatColor.AQUA + "Verfügbare Warps:");
        for (String key : keys) {
            sender.sendMessage(ChatColor.GRAY + "- " + ChatColor.WHITE + key);
        }

        return true;
    }
}
