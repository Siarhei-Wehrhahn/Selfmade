import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class FlyBotPlugin extends JavaPlugin implements CommandExecutor {

    private final Map<UUID, BukkitTask> activeTasks = new HashMap<>();

    @Override
    public void onEnable() {
        if (getCommand("flybot") != null) {
            getCommand("flybot").setExecutor(this);
        }
        if (getCommand("flyboat") != null) {
            getCommand("flyboat").setExecutor(this);
        }

        getLogger().info("FlyBot (flying boat) wurde aktiviert");
    }

    @Override
    public void onDisable() {
        for (BukkitTask task : activeTasks.values()) {
            if (task != null) {
                task.cancel();
            }
        }
        activeTasks.clear();
    }

    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args
    ) {
        String name = command.getName().toLowerCase();

        if (!name.equals("flybot") && !name.equals("flyboat")) {
            return false;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Nur Spieler können diesen Befehl nutzen");
            return true;
        }

        if (!player.hasPermission("flybot.use")) {
            player.sendMessage(ChatColor.RED + "Du darfst den FlyBot nicht benutzen");
            return true;
        }

        UUID id = player.getUniqueId();

        // Wenn schon aktiv -> ausschalten
        if (activeTasks.containsKey(id)) {
            stopFlyingBoat(player);
            player.sendMessage(ChatColor.YELLOW + "Boot Flugmodus ausgeschaltet");
            return true;
        }

        // Sonst aktivieren
        startFlyingBoat(player);
        return true;
    }

    private void startFlyingBoat(Player player) {
        Entity vehicle = player.getVehicle();

        if (vehicle == null || !(vehicle instanceof Boat)) {
            player.sendMessage(ChatColor.RED + "Du musst in einem Boot sitzen");
            player.sendMessage(ChatColor.GRAY + "Setz dich zuerst in ein Boot und nutze dann /flybot");
            return;
        }

        Boat boat = (Boat) vehicle;
        boat.setGravity(false);
        boat.setInvulnerable(true);

        player.sendMessage(ChatColor.GREEN + "Boot Flugmodus eingeschaltet");
        player.sendMessage(ChatColor.GRAY + "Steuere mit deiner Blickrichtung");

        BukkitTask task = getServer().getScheduler().runTaskTimer(this, () -> {
            if (!player.isOnline()) {
                stopFlyingBoat(player);
                return;
            }

            Entity currentVehicle = player.getVehicle();
            if (currentVehicle == null || currentVehicle != boat || boat.isDead()) {
                stopFlyingBoat(player);
                return;
            }

            // Richtung aus Sicht des Spielers
            Vector dir = player.getLocation().getDirection().normalize();

            double speed = 0.7;
            Vector velocity = dir.multiply(speed);

            boat.setVelocity(velocity);

        }, 1L, 1L);

        activeTasks.put(player.getUniqueId(), task);
    }

    private void stopFlyingBoat(Player player) {
        UUID id = player.getUniqueId();
        BukkitTask task = activeTasks.remove(id);
        if (task != null) {
            task.cancel();
        }

        Entity vehicle = player.getVehicle();
        if (vehicle instanceof Boat) {
            Boat boat = (Boat) vehicle;
            boat.setGravity(true);
            boat.setInvulnerable(false);
        }

        if (player.isOnline()) {
            player.setFallDistance(0f);
        }
    }
}
