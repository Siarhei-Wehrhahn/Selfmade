package de.jackson.speedcontrol;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class SpeedControlPlugin extends JavaPlugin implements CommandExecutor {

    private static final float DEFAULT_SPEED = 0.2f;

    @Override
    public void onEnable() {
        if (getCommand("setspeed") != null) {
            getCommand("setspeed").setExecutor(this);
        }
        if (getCommand("resetspeed") != null) {
            getCommand("resetspeed").setExecutor(this);
        }

        getLogger().info("SpeedControl wurde aktiviert");
    }

    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args
    ) {
        String name = command.getName().toLowerCase();

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Nur Spieler können diesen Befehl nutzen");
            return true;
        }

        if (!player.hasPermission("speedcontrol.set")) {
            player.sendMessage(ChatColor.RED + "Du darfst deine Geschwindigkeit nicht ändern");
            return true;
        }

        if (name.equals("setspeed")) {
            return handleSetSpeed(player, args);
        }

        if (name.equals("resetspeed")) {
            player.setWalkSpeed(DEFAULT_SPEED);
            player.sendMessage(ChatColor.GREEN + "Geschwindigkeit wurde zurückgesetzt auf "
                    + ChatColor.AQUA + DEFAULT_SPEED);
            return true;
        }

        return false;
    }

    private boolean handleSetSpeed(Player player, String[] args) {
        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Benutzung: /setspeed <wert>");
            player.sendMessage(ChatColor.GRAY + "Beispiele: /setspeed 0.2  oder  /setspeed 5");
            return true;
        }

        String input = args[0];
        float value;

        try {
            // Wenn es eine ganze Zahl 1 bis 10 ist, wie 5
            if (!input.contains(".")) {
                int intVal = Integer.parseInt(input);
                if (intVal >= 1 && intVal <= 10) {
                    value = intVal / 10.0f;
                } else {
                    value = Float.parseFloat(input);
                }
            } else {
                value = Float.parseFloat(input);
            }
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Das ist keine gültige Zahl");
            return true;
        }

        // Begrenzen auf sinnvolle Werte
        if (value <= 0 || value > 1.0f) {
            player.sendMessage(ChatColor.RED + "Erlaubter Bereich ist "
                    + ChatColor.YELLOW + "0.1 bis 1.0");
            return true;
        }

        player.setWalkSpeed(value);

        player.sendMessage(ChatColor.GREEN + "Deine Laufgeschwindigkeit wurde gesetzt auf "
                + ChatColor.AQUA + value);

        return true;
    }
}
