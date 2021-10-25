package io.github.definitlyevil.orbisfactions.listeners;

import io.github.definitlyevil.orbisfactions.ChunkCache;
import io.github.definitlyevil.orbisfactions.OrbisFactions;
import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ChunkListener implements Listener {

    private final ChunkCache.CacheModifier cacheModifier;

    public ChunkListener(ChunkCache.CacheModifier cacheModifier) {
        this.cacheModifier = cacheModifier;
    }

    private final Set<ChunkCache.ChunkKey> loading = Collections.synchronizedSet(new HashSet<>());

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChunkLoad(ChunkLoadEvent event) {
        final Chunk chunk = event.getChunk();
        final ChunkCache.ChunkKey ck = new ChunkCache.ChunkKey(chunk);
        if(!loading.add(ck)) return;
        OrbisFactions.getInstance().execute(() -> {
            try (Connection connection = OrbisFactions.getInstance().getConnection()) {
                final String sql = "SELECT `f`.`id` AS `faction_id`,`f`.`name` AS `faction_name` FROM `faction_chunks` LEFT JOIN `factions` AS `f` ON faction_chunks.faction_id = f.id WHERE `faction_chunks`.`world`=? AND `faction_chunks`.`x`=? AND `faction_chunks`.`z`=? LIMIT 1";
                try (PreparedStatement stm = connection.prepareStatement(sql)) {
                    stm.setString(1, ck.world);
                    stm.setInt(2, ck.cx);
                    stm.setInt(3, ck.cz);
                    try (ResultSet rs = stm.executeQuery()) {
                        final ChunkCache.CachedChunk status;
                        if(rs.next()) {
                            // claimed
                            status = new ChunkCache.CachedFactionChunk(rs.getInt("faction_id"), rs.getString("faction_name"));
                        } else {
                            // wilderness
                            status = ChunkCache.CachedWildernessChunk.instance;
                        }
                        OrbisFactions.getInstance().primary(() -> {
                            cacheModifier.setCache(ck, status);
                            // OrbisFactions.getInstance().getLogger().info(String.format("loaded chunk(%s): %s", ck.toString(), status.toString()));
                        });
                    }
                }
            } catch (Exception exception) {
                exception.printStackTrace();
                OrbisFactions.getInstance().getLogger().severe(String.format("Chunk load error<%s>: %s", exception.getClass().getSimpleName(), exception.getMessage()));
            } finally {
                loading.remove(ck);
            }
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChunkUnload(ChunkUnloadEvent event) {
        cacheModifier.remove(new ChunkCache.ChunkKey(event.getChunk()));
    }
}
