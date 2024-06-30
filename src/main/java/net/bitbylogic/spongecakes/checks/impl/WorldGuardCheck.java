package net.bitbylogic.spongecakes.checks.impl;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import net.bitbylogic.spongecakes.checks.ProtectionCheck;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class WorldGuardCheck implements ProtectionCheck {

    @Override
    public boolean canBreak(Player player, Location location) {
        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();

        return query.testState(BukkitAdapter.adapt(location), localPlayer, Flags.BLOCK_BREAK) ||
                WorldGuard.getInstance().getPlatform().getSessionManager().hasBypass(localPlayer, localPlayer.getWorld());
    }

}
