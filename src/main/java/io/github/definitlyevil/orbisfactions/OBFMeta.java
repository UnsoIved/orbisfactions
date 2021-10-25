package io.github.definitlyevil.orbisfactions;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

public class OBFMeta {
    private static final String KEY = "OrbisFactions::Meta";

    public static final String CHAT_PREFIX = "\u00a77[\u00a76Claim\u00a77]";

    public ChunkCache.CachedChunk lastStanding = null;


    public void updateLastStanding(Player player, World newWorld, int newX, int newZ) {
        ChunkCache.CachedChunk c = OrbisFactions.getInstance().getChunkCache().getChunk(newWorld, newX, newZ);
        if(c == null) return;
        if(ChunkCache.CachedWildernessChunk.class.isAssignableFrom(c.getClass())) {
            // wilderness
            if(lastStanding != null) {
                // entered wilderness
                lastStanding = null;
                // send msg
                player.sendMessage(String.format("%s \u00a7aEntered wilderness", CHAT_PREFIX));
            }
        } else {
            // not wilderness
            if(lastStanding == null || ((ChunkCache.CachedFactionChunk) lastStanding).id != ((ChunkCache.CachedFactionChunk) c).id) {
                lastStanding = c;
                // send msg
                player.sendMessage(String.format("%s \u00a7eEntered %s", CHAT_PREFIX, ((ChunkCache.CachedFactionChunk) c).name));
            }
        }
    }


    public static OBFMeta get(Player player) {
        OBFMeta m;
        if(player.hasMetadata(KEY)) {
            m = (OBFMeta) player.getMetadata(KEY).get(0).value();
        } else {
            m = new OBFMeta();
            player.setMetadata(KEY, new FixedMetadataValue(OrbisFactions.getInstance(), m));
        }
        return m;
    }

}
