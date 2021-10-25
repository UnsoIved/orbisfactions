package io.github.definitlyevil.orbisfactions.tasks;

import io.github.definitlyevil.orbisfactions.OBFMeta;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class UpdateStandingChunkTask implements Runnable {
    @Override
    public void run() {
        Bukkit.getOnlinePlayers().forEach(p -> {
            OBFMeta m = OBFMeta.get(p);
            Location l = p.getLocation();
            m.updateLastStanding(p, l.getWorld(), l.getBlockX() >> 4, l.getBlockZ() >> 4);
        });
    }
}
