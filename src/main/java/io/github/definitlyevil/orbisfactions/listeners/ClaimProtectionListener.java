package io.github.definitlyevil.orbisfactions.listeners;

import io.github.definitlyevil.orbisfactions.ChunkCache;
import io.github.definitlyevil.orbisfactions.OrbisFactions;
import io.github.definitlyevil.orbisfactions.util.OBFPlayerMeta;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class ClaimProtectionListener implements Listener {

    public static final String BYPASS_META_KEY = "OBF::Admin::Bypass";

    private final OrbisFactions plugin;
    private final ChunkCache chunkCache;

    public ClaimProtectionListener(OrbisFactions plugin, ChunkCache chunkCache) {
        this.plugin = plugin;
        this.chunkCache = chunkCache;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if(!canEdit(event.getPlayer(), event.getBlock().getChunk())) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if(!canEdit(event.getPlayer(), event.getBlock().getChunk())) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockClick(PlayerInteractEvent event) {
        if(!event.getAction().name().endsWith("_CLICK_BLOCK")) return;
        boolean can = canEdit(event.getPlayer(), event.getClickedBlock().getChunk());
        if(!plugin.getConfig().getBoolean("protections.interact", false)) {
            if(!can) event.setUseItemInHand(Event.Result.DENY);
            return;
        }
        if(!can) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHangingPlace(HangingPlaceEvent event) {
        if(!canEdit(event.getPlayer(), event.getEntity().getLocation().getChunk())) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHangingBreak(HangingBreakByEntityEvent event) {
        if(!Player.class.isAssignableFrom(event.getEntity().getClass())) return;
        if(!canEdit((Player) event.getEntity(), event.getEntity().getLocation().getChunk())) event.setCancelled(true);
    }

    public boolean canEdit(Player player, Chunk chunk) { return canEdit(player, new ChunkCache.ChunkKey(chunk));  }
    public boolean canEdit(Player player, ChunkCache.ChunkKey ck) {
        if(player.hasMetadata(BYPASS_META_KEY)) return true; // BYPASS
        OBFPlayerMeta meta = OBFPlayerMeta.get(player);
        if(meta == null) {
            player.sendMessage("\u00a7cProfile not loaded yet! ");
            return false;
        }
        ChunkCache.CachedChunk chunk = chunkCache.getChunk(ck);
        if(chunk == null) {
            player.sendMessage("\u00a7cChunk permission is still loading... ");
            return false;
        }
        if(ChunkCache.CachedWildernessChunk.class.isAssignableFrom(chunk.getClass())) return true;
        if(ChunkCache.CachedFactionChunk.class.isAssignableFrom(chunk.getClass())) {
            ChunkCache.CachedFactionChunk factionChunk = (ChunkCache.CachedFactionChunk) chunk;
            if(!meta.factionRoles.containsKey(factionChunk.id) || !meta.factionRoles.get(factionChunk.id).isMember()) {
                player.sendMessage(String.format("\u00a7cNot a member of faction %s! ", factionChunk.name));
                return false;
            }
            return true;
        }
        player.sendMessage("\u00a7cUnknown claim chunk status! ");
        return false;
    }

}
