package io.github.definitlyevil.orbisfactions.commands.faction;

import io.github.definitlyevil.orbisfactions.OBFRole;
import io.github.definitlyevil.orbisfactions.OrbisFactions;
import io.github.definitlyevil.orbisfactions.commands.ComplexCommand;
import io.github.definitlyevil.orbisfactions.util.DBUtils;
import io.github.definitlyevil.orbisfactions.util.OBFPlayerMeta;
import io.github.definitlyevil.orbisfactions.util.Patterns;
import io.github.definitlyevil.orbisfactions.util.exceptions.FactionNotFoundException;
import io.github.definitlyevil.orbisfactions.util.exceptions.OBFException;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collections;
import java.util.List;

public class FactionUnclaimCommand implements ComplexCommand.SubCommand {
    @Override
    public String name() {
        return "unclaim";
    }

    @Override
    public String usage() {
        return "<faction>";
    }

    @Override
    public String description() {
        return "Unclaim a chunk. ";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!Player.class.isAssignableFrom(sender.getClass())) return;
        if (args.length != 1) {
            sender.sendMessage("\u00a7cNot given a faction name. ");
            return;
        }
        final String faction = args[0];
        if (!Patterns.FACTION_NAME.matcher(faction).matches()) {
            sender.sendMessage("\u00a7cInvalid faction name! ");
            return;
        }

        Player player = (Player) sender;
        OBFPlayerMeta meta = OBFPlayerMeta.get(player);
        if (meta == null) {
            player.sendMessage("\u00a7cProfile not loaded yet! Please try to rejoin. ");
            return;
        }
        Location location = player.getLocation();
        String w = location.getWorld().getName();
        int cx = location.getBlockX() >> 4;
        int cz = location.getBlockZ() >> 4;
        player.sendMessage(String.format("\u00a76Trying to unclaim at chunk (%d, %d) in world <%s>... ", cx, cz, w));
        DBUtils.getFactionRole(meta.playerId, faction, (ex, r) -> {
            if (ex != null) {
                player.sendMessage(String.format("\u00a7cError<%s>: %s", ex.getClass().getSimpleName(), ex.getMessage()));
                return;
            }
            OBFRole role = (OBFRole) r;
            if (!role.checkRole(OBFRole.MOD)) {
                player.sendMessage("\u00a7cInsufficient permission to claim chunks! ");
                return;
            }
            player.sendMessage("\u00a7dTransaction in process... ");

            OrbisFactions.getInstance().execute(() -> {
                try (Connection connection = OrbisFactions.getInstance().getConnection()) {
                    final int faction_id;
                    try (PreparedStatement stm = connection.prepareStatement("SELECT `id` FROM `factions` WHERE `name`=?")) {
                        stm.setString(1, faction);
                        try (ResultSet rs = stm.executeQuery()) {
                            if (!rs.next()) throw new FactionNotFoundException(faction);
                            faction_id = rs.getInt("id");
                        }
                    }
                    try (PreparedStatement stm = connection.prepareStatement(
                        "DELETE FROM `faction_chunks` WHERE `faction_id`=? AND `world`=? AND `x`=? AND `z`=?"
                    )) {
                        stm.setInt(1, faction_id);
                        stm.setString(2, w);
                        stm.setInt(3, cx);
                        stm.setInt(4, cz);
                        stm.executeUpdate();
                        if (stm.getUpdateCount() <= 0) throw new OBFException("Failed claiming chunk! ");

                        // Update cache
                        OrbisFactions.getInstance().getChunkCache().getModifier().markWilderness(w, cx, cz);
                    }
                } catch (Exception exFin) {
                    exFin.printStackTrace();
                    player.sendMessage(String.format("\u00a7cClaim failed<%s>: %s", exFin.getClass().getSimpleName(), exFin.getMessage()));
                }
            });
        });
    }

    private static final List<String> USAGE = Collections.singletonList("<faction>");

    @Override
    public List<String> tabComplete(CommandSender sender, String label, String[] args) {
        return USAGE;
    }
}
