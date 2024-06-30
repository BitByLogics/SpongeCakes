package net.bitbylogic.spongecakes.checks.impl;

import me.ryanhamshire.GriefPrevention.GriefPrevention;
import net.bitbylogic.spongecakes.checks.ProtectionCheck;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class GriefPreventionCheck implements ProtectionCheck {

    @Override
    public boolean canBreak(Player player, Location location) {
        return GriefPrevention.instance.allowBreak(player, location.getBlock(), location).isEmpty();
    }

}
