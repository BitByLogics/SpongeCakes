package net.bitbylogic.spongecakes.util;

import com.google.common.collect.Lists;
import net.bitbylogic.spongecakes.SpongeCakes;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Borrowed from:
// https://github.com/BitByLogics/APIByLogic/blob/master/Spigot/src/main/java/net/bitbylogic/apibylogic/util/item/ItemStackUtil.java#L50
public class SpongeUtils {

    private static final Pattern hexColorExtractor = Pattern.compile("#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})");

    /**
     * Create an ItemStack object from a configuration
     * section.
     *
     * @param section The configuration section.
     * @return New ItemStack instance.
     */
    public static ItemStack getItemStackFromConfig(ConfigurationSection section) {
        // Yes I know, another hacky solution, will fix later
        if (section.getString("Material", "BARRIER").equalsIgnoreCase("CONDENSED_SPONGE")) {
            return SpongeCakes.getInstance().getCondensedSponge();
        }

        int amount = section.getInt("Amount", 1);
        ItemStack stack = new ItemStack(Material.valueOf(section.getString("Material", "BARRIER")), amount);
        ItemMeta meta = stack.getItemMeta();

        if (meta == null) {
            return null;
        }

        // Define the items name
        if (section.getString("Name") != null) {
            meta.setDisplayName(color(section.getString("Name")));
        }

        List<String> lore = Lists.newArrayList();

        // Define the items lore
        section.getStringList("Lore").forEach(string ->
                lore.add(color(string)));

        meta.setLore(lore);

        // Add flags to hide potion effects/attributes
        section.getStringList("Flags").forEach(flag -> {
            meta.addItemFlags(ItemFlag.valueOf(flag.toUpperCase()));
        });

        if (section.getBoolean("Unbreakable")) {
            meta.setUnbreakable(true);
        }

        // Make the item glow
        if (section.getBoolean("Glow")) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        stack.setItemMeta(meta);

        // If leather armor, apply dye color if defined
        if (stack.getType().name().startsWith("LEATHER_") && section.getString("Dye-Color") != null) {
            LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) stack.getItemMeta();
            java.awt.Color color = ChatColor.of(section.getString("Dye-Color")).getColor();
            leatherArmorMeta.setColor(Color.fromRGB(color.getRed(), color.getGreen(), color.getBlue()));
            stack.setItemMeta(leatherArmorMeta);
        }

        // If the item is a potion, apply potion data
        if (stack.getType() == Material.SPLASH_POTION || stack.getType() == Material.POTION) {
            ConfigurationSection potionSection = section.getConfigurationSection("Potion-Data");

            if (potionSection != null) {
                boolean vanilla = potionSection.getBoolean("Vanilla", false);
                PotionMeta potionMeta = (PotionMeta) stack.getItemMeta();
                String potionType = potionSection.getString("Type", "POISON");

                if (vanilla) {
                    potionMeta.setBasePotionType(PotionType.valueOf(potionType));
                } else {
                    potionMeta.addCustomEffect(new PotionEffect(PotionEffectType.getByName(potionType), potionSection.getInt("Duration", 20), potionSection.getInt("Amplifier", 1) - 1), true);
                }

                stack.setItemMeta(potionMeta);
            }
        }

        if (stack.getType() == Material.TIPPED_ARROW) {
            PotionMeta potionMeta = (PotionMeta) stack.getItemMeta();
            potionMeta.setBasePotionType(PotionType.valueOf(section.getString("Arrow-Type", "POISON")));
            stack.setItemMeta(potionMeta);
        }

        // If the item is a player head, apply skin
        if (section.getString("Skull-Name") != null && stack.getType() == Material.PLAYER_HEAD) {
            SkullMeta skullMeta = (SkullMeta) stack.getItemMeta();
            skullMeta.setOwner(section.getString("Skull-Name", "Notch"));
            stack.setItemMeta(skullMeta);
        }

        // Used for resourcepacks, to display custom models
        if (section.getInt("Model-Data") != 0) {
            ItemMeta updatedMeta = stack.getItemMeta();
            updatedMeta.setCustomModelData(section.getInt("Model-Data"));
            stack.setItemMeta(updatedMeta);
        }

        ItemMeta updatedMeta = stack.getItemMeta();

        // Apply enchantments
        section.getStringList("Enchantments").forEach(enchant -> {
            String[] data = enchant.split(":");
            NamespacedKey key = NamespacedKey.minecraft(data[0].trim());
            Enchantment enchantment = Enchantment.getByKey(key);
            int level = 0;

            try {
                level = Integer.parseInt(data[1]);
            } catch (NumberFormatException e) {
                Bukkit.getLogger().warning(String.format("[SpongeCakes]: Skipped enchantment '%s', invalid level.", enchant));
                return;
            }

            if (enchantment == null) {
                Bukkit.getLogger().warning(String.format("[SpongeCakes]: Skipped enchantment '%s', invalid enchant.", enchant));
                return;
            }

            updatedMeta.addEnchant(enchantment, level, true);
        });

        stack.setItemMeta(updatedMeta);

        return stack;
    }

    public static String color(String message) {
        String coloredMessage = ChatColor.translateAlternateColorCodes('&', message);

        Matcher matcher = hexColorExtractor.matcher(coloredMessage);

        while (matcher.find()) {
            String hexColor = matcher.group();
            coloredMessage = coloredMessage.replace(hexColor, ChatColor.of(hexColor).toString());
        }

        return coloredMessage;
    }

    public static String locationToString(Location location, String separator) {
        return location.getWorld().getName() + separator +
                location.getBlockX() + separator +
                location.getBlockY() + separator +
                location.getBlockZ();
    }

    public static boolean hasPersistentData(Location location) {
        return location.getChunk().getPersistentDataContainer().has(new NamespacedKey(SpongeCakes.getInstance(), locationToString(location, "._.")), PersistentDataType.TAG_CONTAINER);
    }

    public static PersistentDataContainer getPersistentData(Location location, boolean create) {
        NamespacedKey locationKey = new NamespacedKey(SpongeCakes.getInstance(), locationToString(location, "._."));
        Chunk chunk = location.getChunk();

        if (!chunk.getPersistentDataContainer().has(locationKey, PersistentDataType.TAG_CONTAINER)) {
            if (!create) {
                return null;
            }

            chunk.getPersistentDataContainer().set(locationKey, PersistentDataType.TAG_CONTAINER,
                    chunk.getPersistentDataContainer().getAdapterContext().newPersistentDataContainer());
        }

        return chunk.getPersistentDataContainer().get(locationKey, PersistentDataType.TAG_CONTAINER);
    }

    public static void deletePersistentData(Location location) {
        NamespacedKey locationKey = new NamespacedKey(SpongeCakes.getInstance(), locationToString(location, "._."));
        Chunk chunk = location.getChunk();

        if (!chunk.getPersistentDataContainer().has(locationKey, PersistentDataType.TAG_CONTAINER)) {
            return;
        }

        chunk.getPersistentDataContainer().remove(locationKey);
    }

    public static void savePersistentData(Location location, PersistentDataContainer container) {
        location.getChunk().getPersistentDataContainer().set(new NamespacedKey(SpongeCakes.getInstance(),
                locationToString(location, "._.")), PersistentDataType.TAG_CONTAINER, container);
    }

    /**
     * Sponge drain code borrowed & refactored from Minecraft source.
     * This was done to keep sponge logic exactly like vanilla.
     *
     * @param player    The player draining the water
     * @param location  The initial location of the area
     * @param maxBlocks The max amount of blocks to drain
     * @return The amount of blocks drained.
     */
    public static int vanillaDrain(Player player, Location location, int maxBlocks) {
        Queue<Block> queue = Lists.newLinkedList();
        queue.add(location.getBlock());
        int i = 0;

        while (!queue.isEmpty()) {
            Block originalBlock = queue.poll();

            for (BlockFace face : BlockFace.values()) {
                if (face == BlockFace.SELF || i > maxBlocks) {
                    break;
                }

                Block relativeBlock = originalBlock.getRelative(face);
                BlockData blockData = relativeBlock.getBlockData();

                if (SpongeCakes.getInstance().getProtectionChecks().stream().anyMatch(check -> !check.canBreak(player, relativeBlock.getLocation()))) {
                    continue;
                }

                if (!isDrainableBlock(relativeBlock)) {
                    continue;
                }

                if (blockData instanceof Waterlogged waterloggedData && waterloggedData.isWaterlogged()) {
                    waterloggedData.setWaterlogged(false);
                    relativeBlock.setBlockData(waterloggedData);
                    relativeBlock.getState().update(false, true);

                    ++i;

                    queue.add(relativeBlock);
                    continue;
                }

                relativeBlock.setType(Material.AIR);

                ++i;

                queue.add(relativeBlock);
            }

            if (i > maxBlocks) {
                break;
            }
        }

        return i;
    }

    public static List<Block> drainInRadius(Location location, int radius) {
        List<Block> blocks = new ArrayList<>();

        if (location == null || location.getWorld() == null) {
            return blocks;
        }

        World world = location.getWorld();
        int bx = location.getBlockX();
        int by = location.getBlockY();
        int bz = location.getBlockZ();

        for (int x = bx - radius; x <= bx + radius; x++) {
            for (int y = by - radius; y <= by + radius; y++) {
                for (int z = bz - radius; z <= bz + radius; z++) {
                    double distance = ((bx - x) * (bx - x) + (bz - z) * (bz - z) + (by - y) * (by - y));

                    if (distance >= radius * radius && distance >= (radius - 1) * (radius - 1)) {
                        continue;
                    }

                    Block block = world.getBlockAt(x, y, z);

                    if (!isDrainableBlock(block)) {
                        continue;
                    }

                    blocks.add(block);
                }
            }
        }

        return blocks;
    }

    private static boolean isDrainableBlock(Block block) {
        return block.getType() == Material.WATER || (block.getBlockData() instanceof Waterlogged waterlogged && waterlogged.isWaterlogged())
                || (block.getType() == Material.SEAGRASS || block.getType() == Material.TALL_SEAGRASS || block.getType() == Material.KELP
                || block.getType() == Material.KELP_PLANT);
    }

}
