package net.bitbylogic.spongecakes.listener;

import net.bitbylogic.spongecakes.SpongeCakes;
import net.bitbylogic.spongecakes.util.Pair;
import net.bitbylogic.spongecakes.util.SpongeUtils;
import net.bitbylogic.spongecakes.util.TimeConverter;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.block.data.type.Cake;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class SpongeListener implements Listener {

    // Yeah yeah, it's ugly. Explained: Player UUID. Pair: Absorb Radius, Expire Time
    private final HashMap<UUID, Pair<Integer, Long>> absorbTime = new HashMap<>();

    private final SpongeCakes plugin;
    private final NamespacedKey spongeCakeKey;
    private final NamespacedKey condensedCakeKey;

    public SpongeListener(SpongeCakes plugin) {
        this.plugin = plugin;
        this.spongeCakeKey = new NamespacedKey(plugin, "sponge_cake");
        this.condensedCakeKey = new NamespacedKey(plugin, "condensed_cake");

        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            absorbTime.forEach((uuid, pair) -> {
                Player player = Bukkit.getPlayer(uuid);

                if (player == null || pair.getValue() - System.currentTimeMillis() <= 0) {
                    return;
                }

                int condenseRadius = plugin.getConfig().getInt("Condensed-Sponge-Cake.Absorb-Radius", 10);

                if (pair.getKey() == condenseRadius) {
                    String timeMessage = plugin.getConfig().getString("Messages.Time-Display.Condensed-Time-Left", "&e%time% &aleft of condensed sponge absorb time!");
                    long timeLeft = pair.getValue() - System.currentTimeMillis();

                    timeMessage = timeMessage.replace("%time%", TimeConverter.convertToReadableTime(timeLeft));

                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy(SpongeUtils.color(timeMessage)));
                    return;
                }

                String timeMessage = plugin.getConfig().getString("Messages.Time-Display.Time-Left", "&e%time% &aleft of sponge absorb time!");
                long timeLeft = pair.getValue() - System.currentTimeMillis();

                timeMessage = timeMessage.replace("%time%", TimeConverter.convertToReadableTime(timeLeft));

                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy(SpongeUtils.color(timeMessage)));
            });
        }, 0, 20);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void clearPlaced(BlockPlaceEvent event) {
        if (!SpongeUtils.hasPersistentData(event.getBlock().getLocation())) {
            return;
        }

        SpongeUtils.deletePersistentData(event.getBlock().getLocation());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        ItemStack item = event.getItemInHand();

        if (item.isSimilar(plugin.getCondensedSponge())) {
            if (event.getBlockReplacedState().getType() != Material.WATER) {
                event.getPlayer().sendMessage(SpongeUtils.color(plugin.getConfig().getString("Condensed-Error-Placing",
                        "&cYou must place a condensed sponge in water to use it!")));
                event.setCancelled(true);
                return;
            }

            int drainedBlocks = SpongeUtils.vanillaDrain(event.getPlayer(), block.getLocation(), plugin.getConfig().getInt("Condensed-Sponge.Max-Absorbed", 576));

            if (drainedBlocks <= 0) {
                event.setCancelled(true);
                return;
            }

            event.setCancelled(true);
            block.getWorld().spawnParticle(Particle.BLOCK, block.getLocation().add(0.5, 0.5, 0.5), 10, 0.2, 0.2, 0.2, 1, Material.SPONGE.createBlockData());
            event.getItemInHand().setAmount(event.getItemInHand().getAmount() - 1);
            Bukkit.getScheduler().runTaskLater(plugin, () -> event.getBlock().setType(Material.AIR), 1);
            return;
        }

        if (item.isSimilar(plugin.getSpongeCake())) {
            PersistentDataContainer blockContainer = SpongeUtils.getPersistentData(block.getLocation(), true);
            blockContainer.set(spongeCakeKey, PersistentDataType.BOOLEAN, true);
            SpongeUtils.savePersistentData(block.getLocation(), blockContainer);
            return;
        }

        if (!item.isSimilar(plugin.getCondensedSpongeCake())) {
            return;
        }

        PersistentDataContainer blockContainer = SpongeUtils.getPersistentData(block.getLocation(), true);
        blockContainer.set(condensedCakeKey, PersistentDataType.BOOLEAN, true);
        SpongeUtils.savePersistentData(block.getLocation(), blockContainer);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onEat(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null || event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (event.getClickedBlock().getType() != Material.CAKE) {
            return;
        }

        PersistentDataContainer container = SpongeUtils.getPersistentData(event.getClickedBlock().getLocation(), false);

        if (container == null || (!container.has(spongeCakeKey) && !container.has(condensedCakeKey))) {
            return;
        }

        Cake cake = (Cake) event.getClickedBlock().getBlockData();

        if (event.getPlayer().getFoodLevel() >= 20 && (event.getPlayer().getGameMode() == GameMode.SURVIVAL
                || event.getPlayer().getGameMode() == GameMode.ADVENTURE)) {
            return;
        }

        if (cake.getBites() == cake.getMaximumBites()) {
            SpongeUtils.deletePersistentData(event.getClickedBlock().getLocation());
        }

        Player player = event.getPlayer();
        boolean condensed = container.has(condensedCakeKey);

        if (condensed) {
            int condenseRadius = plugin.getConfig().getInt("Condensed-Sponge-Cake.Absorb-Radius", 10);
            long condenseDuration = TimeConverter.convert(plugin.getConfig().getString("Condensed-Sponge-Cake.Absorb-Time-Per-Slice", "30s"));

            absorbTime.entrySet().stream().filter(entry -> entry.getKey().equals(player.getUniqueId()) && entry.getValue().getKey() == condenseRadius).findAny()
                    .ifPresentOrElse(entry -> absorbTime.put(entry.getKey(), new Pair<>(entry.getValue().getKey(), absorbTime.get(entry.getKey()).getValue() + condenseDuration)),
                            () -> absorbTime.put(player.getUniqueId(), new Pair<>(condenseRadius, System.currentTimeMillis() + condenseDuration)));
            return;
        }

        int radius = plugin.getConfig().getInt("Sponge-Cake.Absorb-Radius", 5);
        long duration = TimeConverter.convert(plugin.getConfig().getString("Sponge-Cake.Absorb-Time-Per-Slice", "10s"));

        absorbTime.entrySet().stream().filter(entry -> entry.getKey().equals(player.getUniqueId()) && entry.getValue().getKey() == radius).findAny()
                .ifPresentOrElse(entry -> absorbTime.put(entry.getKey(), new Pair<>(entry.getValue().getKey(), absorbTime.get(entry.getKey()).getValue() + duration)),
                        () -> absorbTime.put(player.getUniqueId(), new Pair<>(radius, System.currentTimeMillis() + duration)));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onMove(PlayerMoveEvent event) {
        // Ensure we're moving to a new block
        if (event.getTo() == null || (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
                event.getFrom().getBlockY() == event.getTo().getBlockY() &&
                event.getFrom().getBlockZ() == event.getTo().getBlockZ())) {
            return;
        }

        if (!absorbTime.containsKey(event.getPlayer().getUniqueId())) {
            return;
        }

        if (absorbTime.get(event.getPlayer().getUniqueId()).getValue() <= System.currentTimeMillis()) {
            absorbTime.remove(event.getPlayer().getUniqueId());
            return;
        }

        List<Block> blocks = SpongeUtils.drainInRadius(event.getPlayer().getLocation(), absorbTime.get(event.getPlayer().getUniqueId()).getKey());

        blocks.forEach(block -> {
            if (plugin.getProtectionChecks().stream().anyMatch(check -> !check.canBreak(event.getPlayer(), block.getLocation()))) {
                return;
            }

            if (block.getBlockData() instanceof Waterlogged blockData) {
                blockData.setWaterlogged(false);
                block.setBlockData(blockData);
                block.getState().update(true, true);
                return;
            }

            block.setType(Material.AIR);
        });
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onMilkDrank(PlayerItemConsumeEvent event) {
        if (!absorbTime.containsKey(event.getPlayer().getUniqueId()) || event.getItem().getType() != Material.MILK_BUCKET) {
            return;
        }

        absorbTime.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!SpongeUtils.hasPersistentData(event.getBlock().getLocation())) {
            return;
        }

        SpongeUtils.deletePersistentData(event.getBlock().getLocation());
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        Player player = (Player) event.getWhoClicked();
        ItemStack result = event.getRecipe().getResult();

        if (result.isSimilar(plugin.getCondensedSponge())) {
            String permission = plugin.getConfig().getString("Condensed-Sponge.Permission", "spongecakes.craft.condensed");

            if (permission.isEmpty() || player.hasPermission(permission)) {
                return;
            }

            player.sendMessage(SpongeUtils.color(plugin.getConfig().getString("Messages.Craft-No-Permission", "&cYou cannot craft this item!")));
            event.setCancelled(true);
            return;
        }

        if (result.isSimilar(plugin.getSpongeCake())) {
            String permission = plugin.getConfig().getString("Sponge-Cake.Permission", "spongecakes.craft.spongecake");

            if (permission.isEmpty() || player.hasPermission(permission)) {
                return;
            }

            player.sendMessage(SpongeUtils.color(plugin.getConfig().getString("Messages.Craft-No-Permission", "&cYou cannot craft this item!")));
            event.setCancelled(true);
            return;
        }

        if (!result.isSimilar(plugin.getCondensedSpongeCake())) {
            return;
        }

        String permission = plugin.getConfig().getString("Condensed-Sponge-Cake.Permission", "spongecakes.craft.condensedspongecake");

        if (permission.isEmpty() || player.hasPermission(permission)) {
            return;
        }

        player.sendMessage(SpongeUtils.color(plugin.getConfig().getString("Messages.Craft-No-Permission", "&cYou cannot craft this item!")));
        event.setCancelled(true);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        plugin.getRecipeKeys().forEach(key -> event.getPlayer().discoverRecipe(key));
    }

}
