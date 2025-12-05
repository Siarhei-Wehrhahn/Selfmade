package de.jackson.warpstones;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.stream.Collectors;

public final class QuickWarpPlugin extends JavaPlugin implements Listener {

    private static final Set<Material> CENTER_BLOCKS = EnumSet.of(
            Material.GOLD_BLOCK,
            Material.COPPER_BLOCK,
            Material.DIAMOND_BLOCK,
            Material.EMERALD_BLOCK,
            Material.LAPIS_BLOCK,
            Material.REDSTONE_BLOCK,
            Material.NETHERITE_BLOCK
    );

    private final Map<Location, WarpStone> warpByLocation = new HashMap<>();
    private final Map<UUID, List<WarpStone>> warpsByOwner = new HashMap<>();
    private final Map<UUID, PendingWarp> pendingNaming = new HashMap<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);

        // Beim Start aus Config laden
        loadWarpStonesFromConfig();

        getLogger().info("WarpStonesPlugin aktiviert");
    }

    @Override
    public void onDisable() {
        saveWarpStonesToConfig();
        getLogger().info("WarpStonesPlugin deaktiviert");
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlockPlaced();
        Player player = event.getPlayer();
        Material type = block.getType();

        if (!CENTER_BLOCKS.contains(type)) {
            return;
        }

        if (!isValidWarpFormation(block)) {
            return;
        }

        Location centerLoc = cleanLocation(block.getLocation());

        if (warpByLocation.containsKey(centerLoc)) {
            player.sendMessage(ChatColor.RED + "Hier existiert bereits ein Warpstein");
            return;
        }

        PendingWarp pending = new PendingWarp(centerLoc, player.getUniqueId());
        pendingNaming.put(player.getUniqueId(), pending);

        player.sendMessage(ChatColor.GOLD + "Du hast einen Warpstein gebaut");
        player.sendMessage(ChatColor.YELLOW + "Bitte schreibe jetzt im Chat den Namen für diesen Warpstein");
        player.sendMessage(ChatColor.GRAY + "Schreibe 'abbrechen' um abzubrechen");
    }

    @EventHandler(ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        PendingWarp pending = pendingNaming.get(uuid);
        if (pending == null) {
            return;
        }

        event.setCancelled(true);
        String message = event.getMessage().trim();

        if (message.equalsIgnoreCase("abbrechen")) {
            pendingNaming.remove(uuid);
            player.sendMessage(ChatColor.RED + "Benennung des Warpsteins abgebrochen");
            return;
        }

        if (message.isEmpty()) {
            player.sendMessage(ChatColor.RED + "Der Name darf nicht leer sein");
            return;
        }

        if (message.length() > 32) {
            player.sendMessage(ChatColor.RED + "Der Name darf maximal 32 Zeichen haben");
            return;
        }

        Bukkit.getScheduler().runTask(this, () -> {
            createWarpStone(player, pending, message);
        });
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) {
            return;
        }

        switch (event.getAction()) {
            case RIGHT_CLICK_BLOCK:
                break;
            default:
                return;
        }

        Block block = event.getClickedBlock();
        Location loc = cleanLocation(block.getLocation());
        Player player = event.getPlayer();

        WarpStone warp = warpByLocation.get(loc);
        if (warp == null) {
            return;
        }

        // Jeder Spieler darf den Warpstein benutzen
        openWarpMenu(player, warp);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Location brokenLoc = cleanLocation(block.getLocation());

        WarpStone toRemove = null;

        for (WarpStone warp : warpByLocation.values()) {
            if (isPartOfFormation(brokenLoc, warp.getCenter())) {
                toRemove = warp;
                break;
            }
        }

        if (toRemove == null) {
            return;
        }

        removeWarpStone(toRemove);

        Player breaker = event.getPlayer();
        breaker.sendMessage(ChatColor.RED + "Warpstein '" + toRemove.getName() + "' wurde entfernt");

        if (!breaker.getUniqueId().equals(toRemove.getOwner())) {
            Player owner = Bukkit.getPlayer(toRemove.getOwner());
            if (owner != null && owner.isOnline()) {
                owner.sendMessage(ChatColor.RED + "Dein Warpstein '" + toRemove.getName()
                        + "' wurde von " + breaker.getName() + " abgebaut");
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory top = event.getView().getTopInventory();
        if (!(top.getHolder() instanceof WarpMenuHolder holder)) {
            return;
        }

        event.setCancelled(true);

        if (event.getCurrentItem() == null || event.getCurrentItem().getType().isAir()) {
            return;
        }

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        ItemStack clicked = event.getCurrentItem();
        ItemMeta meta = clicked.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return;
        }

        String rawName = ChatColor.stripColor(meta.getDisplayName());

        Location currentCenter = holder.getCenter();
        List<WarpStone> destinations = warpByLocation.values().stream()
                .filter(w -> !w.getCenter().equals(currentCenter))
                .collect(Collectors.toList());

        Optional<WarpStone> targetOpt = destinations.stream()
                .filter(w -> w.getName().equals(rawName))
                .findFirst();

        if (targetOpt.isEmpty()) {
            player.sendMessage(ChatColor.RED + "Dieser Warp existiert nicht mehr");
            player.closeInventory();
            return;
        }

        WarpStone target = targetOpt.get();
        Location targetLoc = target.getCenter().clone().add(0.5, 1.1, 0.5);

        if (targetLoc.getWorld() == null) {
            player.sendMessage(ChatColor.RED + "Die Welt dieses Warpsteins ist nicht geladen");
            player.closeInventory();
            return;
        }

        player.closeInventory();
        player.teleport(targetLoc);
        player.sendMessage(ChatColor.GREEN + "Teleportiert zu Warp '" + target.getName() + "'");
    }

    private void createWarpStone(Player player, PendingWarp pending, String name) {
        pendingNaming.remove(player.getUniqueId());

        Location center = pending.center();
        World world = center.getWorld();
        if (world == null) {
            player.sendMessage(ChatColor.RED + "Welt für diesen Warpstein ist nicht verfügbar");
            return;
        }

        if (!isValidWarpFormation(world.getBlockAt(center))) {
            player.sendMessage(ChatColor.RED + "Die Warpstein Struktur ist nicht mehr vollständig");
            return;
        }

        WarpStone warp = new WarpStone(
                player.getUniqueId(),
                name,
                center.getWorld().getName(),
                center.getBlockX(),
                center.getBlockY(),
                center.getBlockZ()
        );

        addWarpStone(warp);
        player.sendMessage(ChatColor.GREEN + "Warpstein '" + name + "' wurde gespeichert");
    }

    private void addWarpStone(WarpStone warp) {
        Location center = warp.getCenter();

        warpByLocation.put(center, warp);
        warpsByOwner.computeIfAbsent(warp.getOwner(), k -> new ArrayList<>()).add(warp);

        saveWarpStonesToConfig();
    }

    private void removeWarpStone(WarpStone warp) {
        Location center = warp.getCenter();

        warpByLocation.remove(center);

        List<WarpStone> list = warpsByOwner.get(warp.getOwner());
        if (list != null) {
            list.removeIf(w -> w.equals(warp));
            if (list.isEmpty()) {
                warpsByOwner.remove(warp.getOwner());
            }
        }

        saveWarpStonesToConfig();
    }

    private void openWarpMenu(Player player, WarpStone current) {
        List<WarpStone> allWarps = new ArrayList<>(warpByLocation.values());

        List<WarpStone> destinations = allWarps.stream()
                .filter(w -> !w.getCenter().equals(current.getCenter()))
                .collect(Collectors.toList());

        if (destinations.isEmpty()) {
            player.sendMessage(ChatColor.RED + "Es gibt keine anderen Warpsteine zu denen du reisen kannst");
            return;
        }

        int size = ((destinations.size() - 1) / 9 + 1) * 9;
        size = Math.min(Math.max(size, 9), 54);

        Inventory inv = Bukkit.createInventory(
                new WarpMenuHolder(current.getCenter()),
                size,
                ChatColor.DARK_PURPLE + "Warpsteine"
        );

        for (int i = 0; i < destinations.size() && i < size; i++) {
            WarpStone warp = destinations.get(i);

            ItemStack stack = new ItemStack(Material.ENDER_PEARL);
            ItemMeta meta = stack.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.AQUA + warp.getName());

                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + warp.getWorldName());
                lore.add(ChatColor.GRAY + "X " + warp.getX() + " Y " + warp.getY() + " Z " + warp.getZ());
                meta.setLore(lore);

                stack.setItemMeta(meta);
            }

            inv.setItem(i, stack);
        }

        player.openInventory(inv);
    }

    private boolean isValidWarpFormation(Block centerBlock) {
        if (!CENTER_BLOCKS.contains(centerBlock.getType())) {
            return false;
        }

        World world = centerBlock.getWorld();
        int cx = centerBlock.getX();
        int cy = centerBlock.getY();
        int cz = centerBlock.getZ();

        int[][] offsets = {
                {-1, -1},
                {-1, 0},
                {-1, 1},
                {0, -1},
                {0, 1},
                {1, -1},
                {1, 0},
                {1, 1}
        };

        for (int[] off : offsets) {
            Block b = world.getBlockAt(cx + off[0], cy, cz + off[1]);
            Material type = b.getType();
            if (!type.name().endsWith("_STAIRS")) {
                return false;
            }
        }

        return true;
    }

    private boolean isPartOfFormation(Location broken, Location center) {
        if (broken.getWorld() == null || center.getWorld() == null) {
            return false;
        }
        if (!broken.getWorld().getName().equals(center.getWorld().getName())) {
            return false;
        }
        if (broken.getBlockY() != center.getBlockY()) {
            return false;
        }

        int dx = Math.abs(broken.getBlockX() - center.getBlockX());
        int dz = Math.abs(broken.getBlockZ() - center.getBlockZ());

        return dx <= 1 && dz <= 1;
    }

    private Location cleanLocation(Location loc) {
        return new Location(
                loc.getWorld(),
                loc.getBlockX(),
                loc.getBlockY(),
                loc.getBlockZ()
        );
    }

    private void loadWarpStonesFromConfig() {
        warpByLocation.clear();
        warpsByOwner.clear();

        FileConfiguration cfg = getConfig();
        List<Map<?, ?>> list = cfg.getMapList("warpstones");
        if (list == null || list.isEmpty()) {
            return;
        }

        for (Map<?, ?> map : list) {
            try {
                UUID owner = UUID.fromString((String) map.get("owner"));
                String name = (String) map.get("name");
                String worldName = (String) map.get("world");
                int x = (int) map.get("x");
                int y = (int) map.get("y");
                int z = (int) map.get("z");

                World world = Bukkit.getWorld(worldName);
                if (world == null) {
                    getLogger().warning("Welt nicht gefunden für Warpstein " + name + " Welt " + worldName);
                    continue;
                }

                WarpStone warp = new WarpStone(owner, name, worldName, x, y, z);
                warpByLocation.put(warp.getCenter(), warp);
                warpsByOwner.computeIfAbsent(owner, k -> new ArrayList<>()).add(warp);
            } catch (Exception ex) {
                getLogger().warning("Konnte Warpstein aus Config nicht laden " + ex.getMessage());
            }
        }
    }

    private void saveWarpStonesToConfig() {
        FileConfiguration cfg = getConfig();
        List<Map<String, Object>> list = new ArrayList<>();

        for (WarpStone warp : warpByLocation.values()) {
            Map<String, Object> map = new HashMap<>();
            map.put("owner", warp.getOwner().toString());
            map.put("name", warp.getName());
            map.put("world", warp.getWorldName());
            map.put("x", warp.getX());
            map.put("y", warp.getY());
            map.put("z", warp.getZ());
            list.add(map);
        }

        cfg.set("warpstones", list);
        saveConfig();
    }

    private record PendingWarp(Location center, UUID owner) {}

    private static class WarpStone {
        private final UUID owner;
        private final String name;
        private final String worldName;
        private final int x;
        private final int y;
        private final int z;

        public WarpStone(UUID owner, String name, String worldName, int x, int y, int z) {
            this.owner = owner;
            this.name = name;
            this.worldName = worldName;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public UUID getOwner() {
            return owner;
        }

        public String getName() {
            return name;
        }

        public String getWorldName() {
            return worldName;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getZ() {
            return z;
        }

        public Location getCenter() {
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                return new Location(null, x, y, z);
            }
            return new Location(world, x, y, z);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof WarpStone other)) return false;
            return owner.equals(other.owner)
                    && worldName.equals(other.worldName)
                    && x == other.x
                    && y == other.y
                    && z == other.z;
        }

        @Override
        public int hashCode() {
            return Objects.hash(owner, worldName, x, y, z);
        }
    }

    private static class WarpMenuHolder implements InventoryHolder {
        private final Location center;

        public WarpMenuHolder(Location center) {
            this.center = center;
        }

        public Location getCenter() {
            return center;
        }

        @Override
        public Inventory getInventory() {
            return null;
        }
    }
}
