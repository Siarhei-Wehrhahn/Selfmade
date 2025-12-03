package de.jackson.chickenparty;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public final class ChickenPartyPlugin extends JavaPlugin implements CommandExecutor {

    @Override
    public void onEnable() {
        if (getCommand("chickenparty") != null) {
            getCommand("chickenparty").setExecutor(this);
        }

        getLogger().info("ChickenParty wurde aktiviert");
    }

    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args
    ) {
        if (!command.getName().equalsIgnoreCase("chickenparty")) {
            return false;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Nur Spieler können diese Party starten");
            return true;
        }

        if (!player.hasPermission("chickenparty.use")) {
            player.sendMessage(ChatColor.RED + "Du hast keine Erlaubnis für diese Party");
            return true;
        }

        World world = player.getWorld();
        Location center = player.getLocation();

        int count = 20;
        double radius = 3.0;

        for (int i = 0; i < count; i++) {
            double angle = (2 * Math.PI / count) * i;
            double xOffset = Math.cos(angle) * radius;
            double zOffset = Math.sin(angle) * radius;

            Location spawnLoc = center.clone().add(xOffset, 0.5, zOffset);

            Chicken chicken = (Chicken) world.spawnEntity(spawnLoc, EntityType.CHICKEN);

            // Langsam fallend und glühend
            chicken.addPotionEffect(new PotionEffect(
                    PotionEffectType.SLOW_FALLING,
                    20 * 10,
                    0,
                    false,
                    false
            ));
            chicken.addPotionEffect(new PotionEffect(
                    PotionEffectType.GLOWING,
                    20 * 10,
                    0,
                    false,
                    false
            ));

            // Partikel um das Huhn
            world.spawnParticle(
                    Particle.HEART,
                    spawnLoc.getX(),
                    spawnLoc.getY() + 0.5,
                    spawnLoc.getZ(),
                    5,
                    0.3,
                    0.3,
                    0.3,
                    0
            );
        }

        world.spawnParticle(
                Particle.CLOUD,
                center.getX(),
                center.getY(),
                center.getZ(),
                30,
                1,
                1,
                1,
                0.01
        );

        player.sendMessage(ChatColor.LIGHT_PURPLE + "Die Hühnerparty hat begonnen");

        return true;
    }
}
