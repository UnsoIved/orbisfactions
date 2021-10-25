package io.github.definitlyevil.orbisfactions.util;

import io.github.definitlyevil.orbisfactions.OBFRole;
import org.bukkit.entity.Player;

import java.util.*;

public class OBFPlayerMeta {

    public static final String META_KEY = "OrbisFactions::PlayerMeta";

    public final int playerId;

    public final Map<Integer, OBFRole> factionRoles = new HashMap<>();
    public final List<String> factions = new ArrayList<>();

    public OBFPlayerMeta(int playerId) {
        this.playerId = playerId;
    }

    public static OBFPlayerMeta get(Player player) {
        if(!player.hasMetadata(META_KEY)) return null;
        return (OBFPlayerMeta) player.getMetadata(META_KEY).get(0).value();
    }


}
