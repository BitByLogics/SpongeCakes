package net.bitbylogic.spongecakes.command;

import lombok.RequiredArgsConstructor;
import net.bitbylogic.spongecakes.SpongeCakes;
import net.bitbylogic.spongecakes.util.SpongeUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

@RequiredArgsConstructor
public class SpongeCakesCommand implements CommandExecutor {

    private final SpongeCakes plugin;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!sender.hasPermission("spongecakes.reload")) {
            sender.sendMessage(SpongeUtils.color(plugin.getConfig().getString("Messages.Command.No-Permission", "&cYou cannot use this command")));
            return true;
        }

        plugin.reloadConfig();
        plugin.loadRecipes();
        sender.sendMessage(SpongeUtils.color(plugin.getConfig().getString("Messages.Command.Reloaded", "&aSuccessfully reloaded configuration!")));
        return true;
    }

}
