package de.jackson.chickenparty;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class ChickenPartyPlugin extends JavaPlugin implements CommandExecutor {

    // Cooldown pro Spieler
    private final Map<UUID, Long> lastPartyUse = new HashMap<>();

    @Override
    public void onEnable() {
        // Config mit Standardwerten anlegen, falls nicht vorhanden
        saveDefaultConfig();

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

        // reload Befehl
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("chickenparty.reload")) {
                sender.sendMessage(ChatColor.RED + "Du hast keine Erlaubnis zum Reload");
                return true;
            }

            reloadConfig();
            sender.sendMessage(ChatColor.GREEN + "ChickenParty Konfiguration neu geladen");
            return true;
        }

        // Zielspieler ermitteln
        Player target;

        if (args.length >= 1) {
            // /chickenparty <player>
            target = Bukkit.getPlayerExact(args[0]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Spieler " + args[0] + " wurde nicht gefunden");
                return true;
            }
        } else {
            // /chickenparty ohne Argumente, nur für Spieler
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Nur Spieler können eine Party um sich selbst starten");
                return true;
            }
            target = player;
        }

        // Permission für Party
        if (!sender.hasPermission("chickenparty.use")) {
            sender.sendMessage(ChatColor.RED + "Du hast keine Erlaubnis für diese Party");
            return true;
        }

        // Cooldown check
        int cooldownSeconds = getConfig().getInt("cooldown-seconds", 30);
        if (cooldownSeconds > 0) {
            long now = System.currentTimeMillis();
            long cooldownMillis = cooldownSeconds * 1000L;

            Long last = lastPartyUse.get(target.getUniqueId());
            if (last != null && now - last < cooldownMillis) {
                long remaining = (cooldownMillis - (now - last) + 999) / 1000;
                if (sender.equals(target)) {
                    sender.sendMessage(ChatColor.YELLOW + "Du musst noch " + remaining + " Sekunden warten");
                } else {
                    sender.sendMessage(ChatColor.YELLOW + "Dieser Spieler kann erst in " + remaining + " Sekunden wieder eine Party bekommen");
                }
                return true;
            }

            lastPartyUse.put(target.getUniqueId(), now);
        }

        startChickenParty(target, sender);
        return true;
    }

    private void startChickenParty(Player target, CommandSender trigger) {
        World world = target.getWorld();
        Location center = target.getLocation();

        int count = getConfig().getInt("spawn-count", 20);
        double radius = getConfig().getDouble("radius", 3.0);
        int durationTicks = getConfig().getInt("duration-ticks", 20 * 10);

        boolean enableHeartParticles = getConfig().getBoolean("enable-heart-particles", true);
        boolean enableCloudParticles = getConfig().getBoolean("enable-cloud-particles", true);
        boolean enableSound = getConfig().getBoolean("enable-sound", true);

        List<Chicken> spawnedChickens = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            double angle = (2 * Math.PI / count) * i;
            double xOffset = Math.cos(angle) * radius;
            double zOffset = Math.sin(angle) * radius;

            Location spawnLoc = center.clone().add(xOffset, 0.5, zOffset);

            Chicken chicken = (Chicken) world.spawnEntity(spawnLoc, EntityType.CHICKEN);

            // Langsam fallend und glühend
            chicken.addPotionEffect(new PotionEffect(
                    PotionEffectType.SLOW_FALLING,
                    durationTicks,
                    0,
                    false,
                    false
            ));
            chicken.addPotionEffect(new PotionEffect(
                    PotionEffectType.GLOWING,
                    durationTicks,
                    0,
                    false,
                    false
            ));

            if (enableHeartParticles) {
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

            spawnedChickens.add(chicken);
        }

        if (enableCloudParticles) {
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
        }

        if (enableSound) {
            world.playSound(center, Sound.ENTITY_CHICKEN_EGG, 1.0f, 1.2f);
        }

        target.sendMessage(ChatColor.LIGHT_PURPLE + "Die Hühnerparty hat begonnen");
        if (!trigger.equals(target)) {
            trigger.sendMessage(ChatColor.LIGHT_PURPLE + "Du hast eine Hühnerparty für " + target.getName() + " gestartet");
        }

        // Hühner nach Ablauf entfernen
        int durationTicksFinal = durationTicks;
        Bukkit.getScheduler().runTaskLater(this, () -> {
            for (Chicken chicken : spawnedChickens) {
                if (chicken != null && !chicken.isDead()) {
                    chicken.remove();
                }
            }
        }, durationTicksFinal);
    }
}
