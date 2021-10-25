package io.github.definitlyevil.orbisfactions.listeners;

import io.github.definitlyevil.orbisfactions.OBFRole;
import io.github.definitlyevil.orbisfactions.OrbisFactions;
import io.github.definitlyevil.orbisfactions.util.OBFPlayerMeta;
import io.github.definitlyevil.orbisfactions.util.exceptions.OBFException;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class PlayerProfileInitializerListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        if(player.hasMetadata(OBFPlayerMeta.META_KEY)) return;
        try(Connection connection = OrbisFactions.getInstance().getConnection()) {
            boolean found = false;
            int playerId = -1;
            OBFPlayerMeta meta = null;
            try(PreparedStatement stm = connection.prepareStatement("SELECT * FROM `players` WHERE `uuid`=?")) {
                stm.setString(1, player.getUniqueId().toString());
                try(ResultSet rs = stm.executeQuery()) {
                    if(!rs.next()) {
                        player.sendMessage("\u00a7aCreating new OrbisFactions player profile... ");
                        OrbisFactions.getInstance().getLogger().info(String.format("Initializing player profile for new player %s<%s>. ", player.getName(), player.getUniqueId().toString()));
                    } else {
                        playerId = rs.getInt("id");
                        found = true;
                        meta = new OBFPlayerMeta(playerId);
                        final OBFPlayerMeta fMeta = meta;
                        final int fPlayerId = playerId;
                        OrbisFactions.getInstance().primary(() -> {
                            player.setMetadata(OBFPlayerMeta.META_KEY, new FixedMetadataValue(OrbisFactions.getInstance(), fMeta));
                            player.sendMessage(String.format("\u00a7aOrbisFactions player profile has been loaded! \u00a77#%d", fPlayerId));
                        });
                    }
                }
            }
            if(found) {
                try (PreparedStatement stm = connection.prepareStatement("UPDATE `players` SET `username`=? WHERE `uuid`=?")) {
                    stm.setString(1, player.getName());
                    stm.setString(2, player.getUniqueId().toString());
                    stm.executeUpdate();
                }
                final Map<Integer, OBFRole> roles = new HashMap<>();
                final List<String> factions = new LinkedList<>();
                try (PreparedStatement stm = connection.prepareStatement("SELECT `faction_roles`.`faction_id`,`factions`.`name`,`faction_roles`.`role` FROM `faction_roles` LEFT JOIN `factions` on faction_roles.faction_id = `factions`.`id` WHERE `faction_roles`.`player_id`=?")) {
                    stm.setInt(1, playerId);
                    try(ResultSet rs = stm.executeQuery()) {
                        while(rs.next()) {
                            roles.put(rs.getInt("faction_id"), OBFRole.from(rs.getInt("role")));
                            factions.add(rs.getString("name"));
                        }
                    }
                }
                if(roles.size() > 0) {
                    final OBFPlayerMeta fMeta = meta;
                    OrbisFactions.getInstance().primary(() -> {
                        fMeta.factionRoles.putAll(roles);
                        fMeta.factions.addAll(factions);
                        player.sendMessage(String.format("\u00a7aLoaded %d faction roles! ", roles.size()));
                    });
                }
            } else {
                try (PreparedStatement stm = connection.prepareStatement("INSERT INTO `players` (`uuid`,`username`) VALUES(?,?)", Statement.RETURN_GENERATED_KEYS)) {
                    stm.setString(1, player.getUniqueId().toString());
                    stm.setString(2, player.getName());
                    stm.executeUpdate();
                    try(ResultSet rs = stm.getGeneratedKeys()) {
                        if(!rs.next()) throw new OBFException("Failed creating new profile, no ID has been returned! ");
                        int id = rs.getInt(1);
                        OrbisFactions.getInstance().primary(() -> {
                            player.setMetadata(OBFPlayerMeta.META_KEY, new FixedMetadataValue(OrbisFactions.getInstance(), new OBFPlayerMeta(id)));
                            player.sendMessage(String.format("\u00a7aOrbisFactions player profile has been created! \u00a77#%d", id));
                        });
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            player.sendMessage("\u00a7cFailed to load your OrbisFactions profile, you might want to rejoin to fix that! ");
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        event.getPlayer().removeMetadata(OBFPlayerMeta.META_KEY, OrbisFactions.getInstance());
    }

}
