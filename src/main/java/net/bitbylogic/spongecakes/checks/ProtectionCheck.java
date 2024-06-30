package net.bitbylogic.spongecakes.checks;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface ProtectionCheck {

    boolean canBreak(Player player, Location location);

}
