package io.github.definitlyevil.orbisfactions;

import io.github.definitlyevil.orbisfactions.util.OBFPlayerMeta;
import me.clip.placeholderapi.PlaceholderHook;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashSet;

public class PAPIHook extends PlaceholderHook {

    @Override
    public String onPlaceholderRequest(Player p, String params) {
        if(p == null) return "-PLAYER-OFFLINE-";
        OBFPlayerMeta meta = OBFPlayerMeta.get(p);
        if(params.equalsIgnoreCase("star")) {
            if(meta == null) return "?";
            if(new HashSet<>(meta.factionRoles.values()).contains(OBFRole.OWNER)) {
                return "⭐";
            } else return "";
        }
        if(params.equalsIgnoreCase("role")) {
            if(meta == null) return "?";
            if(meta.factionRoles.size() <= 0) return "";
            return new ArrayList<>(meta.factionRoles.values()).get(0).getDisplayText();
        }
        if(params.equalsIgnoreCase("faction")) {
            if(meta == null) return "?";
            if(meta.factions.size() <= 0) return "";
            return meta.factions.get(0);
        }
        return "_invalid_param_";
    }
}
