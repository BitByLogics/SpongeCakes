package net.bitbylogic.spongecakes;

import com.jeff_media.updatechecker.UpdateCheckSource;
import com.jeff_media.updatechecker.UpdateChecker;
import com.jeff_media.updatechecker.UserAgentBuilder;
import lombok.Getter;
import net.bitbylogic.spongecakes.checks.ProtectionCheck;
import net.bitbylogic.spongecakes.checks.impl.GriefPreventionCheck;
import net.bitbylogic.spongecakes.checks.impl.WorldGuardCheck;
import net.bitbylogic.spongecakes.command.SpongeCakesCommand;
import net.bitbylogic.spongecakes.listener.SpongeListener;
import net.bitbylogic.spongecakes.util.SpongeUtils;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.ServerOperator;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static net.bitbylogic.spongecakes.util.SpongeUtils.color;

@Getter
public class SpongeCakes extends JavaPlugin {

    @Getter
    private static SpongeCakes instance;

    private final List<ProtectionCheck> protectionChecks = new ArrayList<>();
    private final List<NamespacedKey> recipeKeys = new ArrayList<>();

    private ItemStack condensedSponge;
    private ItemStack spongeCake;
    private ItemStack condensedSpongeCake;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        loadRecipes();

        getCommand("spongecakes").setExecutor(new SpongeCakesCommand(this));
        getServer().getPluginManager().registerEvents(new SpongeListener(this), this);

        if (getServer().getPluginManager().isPluginEnabled("GriefPrevention")) {
            protectionChecks.add(new GriefPreventionCheck());
        }

        if (getServer().getPluginManager().isPluginEnabled("WorldGuard")) {
            protectionChecks.add(new WorldGuardCheck());
        }

        new Metrics(this, 22474);

        new UpdateChecker(this, UpdateCheckSource.SPIGOT, "117722")
                .setNotifyRequesters(false)
                .setNotifyOpsOnJoin(false)
                .setUserAgent(UserAgentBuilder.getDefaultUserAgent())
                .checkEveryXHours(12)
                .onSuccess((commandSenders, latestVersion) -> {
                    String messagePrefix = "&8[&eSponge Cakes&8] ";
                    String currentVersion = getDescription().getVersion();

                    if (currentVersion.equalsIgnoreCase(latestVersion)) {
                        String updateMessage = color(messagePrefix + "&aYou are using the latest version of SpongeCakes!");

                        Bukkit.getConsoleSender().sendMessage(updateMessage);
                        Bukkit.getOnlinePlayers().stream().filter(ServerOperator::isOp).forEach(player -> player.sendMessage(updateMessage));
                        return;
                    }

                    List<String> updateMessages = List.of(
                            color(messagePrefix + "&cYour version of SpongeCakes is outdated!"),
                            color(String.format(messagePrefix + "&cYou are using %s, latest is %s!", currentVersion, latestVersion)),
                            color(messagePrefix + "&cDownload latest here:"),
                            color("&6https://www.spigotmc.org/resources/spongecakes.117722/")
                    );

                    Bukkit.getConsoleSender().sendMessage(updateMessages.toArray(new String[]{}));
                    Bukkit.getOnlinePlayers().stream().filter(ServerOperator::isOp).forEach(player -> player.sendMessage(updateMessages.toArray(new String[]{})));
                })
                .onFail((commandSenders, e) -> {
                }).checkNow();
    }

    @Override
    public void onDisable() {

    }

    public void loadRecipes() {
        recipeKeys.clear();

        loadRecipe("Condensed-Sponge").ifPresent(item -> condensedSponge = item);
        loadRecipe("Sponge-Cake").ifPresent(item -> spongeCake = item);
        loadRecipe("Condensed-Sponge-Cake").ifPresent(item -> condensedSpongeCake = item);
    }

    private Optional<ItemStack> loadRecipe(String recipeId) {
        ConfigurationSection keySection = getConfig().getConfigurationSection(recipeId);

        if (keySection == null) {
            return Optional.empty();
        }

        ConfigurationSection recipeSection = keySection.getConfigurationSection("Recipe");

        if (recipeSection == null) {
            return Optional.empty();
        }

        ConfigurationSection itemSection = keySection.getConfigurationSection("Item");

        if (itemSection == null) {
            return Optional.empty();
        }

        ItemStack recipeResult = SpongeUtils.getItemStackFromConfig(itemSection);

        if (recipeResult == null) {
            return Optional.empty();
        }

        ItemMeta meta = recipeResult.getItemMeta();

        if (meta == null) {
            return Optional.empty();
        }

        NamespacedKey recipeKey = new NamespacedKey(this, recipeId);

        if (Bukkit.getRecipe(recipeKey) != null) {
            Bukkit.removeRecipe(recipeKey);
        }

        List<String> lore = new ArrayList<>();
        meta.getLore().forEach(line -> lore.add(
                line.replace("%max%", getConfig().getInt("Condensed-Sponge.Max-Absorbed", 576) + "")
                        .replace("%base-radius%", getConfig().getInt("Sponge-Cake.Absorb-Radius", 5) + "")
                        .replace("%condensed-radius%", getConfig().getInt("Condensed-Sponge-Cake.Absorb-Radius", 10) + "")
        ));
        meta.setLore(lore);
        meta.getPersistentDataContainer().set(recipeKey, PersistentDataType.BOOLEAN, true);
        recipeResult.setItemMeta(meta);

        if (!recipeSection.getBoolean("Enabled")) {
            return Optional.empty();
        }

        ConfigurationSection ingredientsSection = recipeSection.getConfigurationSection("Ingredients");
        String recipeType = recipeSection.getString("Type", "SHAPELESS");

        if (ingredientsSection == null) {
            return Optional.empty();
        }

        switch (recipeType) {
            case "SHAPED":
                List<String> shape = recipeSection.getStringList("Shape");
                ShapedRecipe shapedRecipe = new ShapedRecipe(recipeKey, recipeResult).shape(shape.toArray(new String[]{}));

                for (String ingredientKey : ingredientsSection.getKeys(false)) {
                    ConfigurationSection ingredientItemSection = ingredientsSection.getConfigurationSection(ingredientKey);

                    if (ingredientItemSection == null) {
                        continue;
                    }

                    ItemStack ingredient = SpongeUtils.getItemStackFromConfig(ingredientItemSection);

                    if (ingredient == null) {
                        continue;
                    }

                    shapedRecipe.setIngredient(ingredientKey.charAt(0), new RecipeChoice.ExactChoice(ingredient));
                }

                Bukkit.addRecipe(shapedRecipe);
                break;
            case "SHAPELESS":
                ShapelessRecipe shapelessRecipe = new ShapelessRecipe(recipeKey, recipeResult);

                for (String ingredientKey : ingredientsSection.getKeys(false)) {
                    ConfigurationSection ingredientItemSection = ingredientsSection.getConfigurationSection(ingredientKey);

                    if (ingredientItemSection == null) {
                        continue;
                    }

                    ItemStack ingredient = SpongeUtils.getItemStackFromConfig(ingredientItemSection);

                    if (ingredient == null) {
                        continue;
                    }

                    shapelessRecipe.addIngredient(new RecipeChoice.ExactChoice(ingredient));
                }

                Bukkit.addRecipe(shapelessRecipe);
                break;
            default:
                break;
        }

        recipeKeys.add(recipeKey);
        return Optional.of(recipeResult);
    }

}
