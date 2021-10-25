package io.github.definitlyevil.orbisfactions.commands.faction;

import io.github.definitlyevil.orbisfactions.OBFRole;
import io.github.definitlyevil.orbisfactions.OrbisFactions;
import io.github.definitlyevil.orbisfactions.commands.ComplexCommand;
import io.github.definitlyevil.orbisfactions.util.OBFPlayerMeta;
import io.github.definitlyevil.orbisfactions.util.Patterns;
import io.github.definitlyevil.orbisfactions.util.exceptions.OBFException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

public class FactionLeaveCommand implements ComplexCommand.SubCommand {
    @Override
    public String name() {
        return "leave";
    }

    @Override
    public String usage() {
        return "Leave your faction. ";
    }

    @Override
    public String description() {
        return "<faction>";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!Player.class.isAssignableFrom(sender.getClass())) return;
        if(args.length != 1) {
            sender.sendMessage("\u00a7cInvalid parameters! ");
            return;
        }
        final String faction = args[0];
        if(!Patterns.FACTION_NAME.matcher(faction).matches()) {
            sender.sendMessage("\u00a7cInvalid faction name! ");
            return;
        }

        Player player = (Player) sender;
        OBFPlayerMeta meta = OBFPlayerMeta.get(player);
        if(meta == null) {
            player.sendMessage("\u00a7cCan't do that, because the profile for you is not loaded! Please try to rejoin. ");
            return;
        }

        OrbisFactions.getInstance().execute(() -> {
            try(Connection connection = OrbisFactions.getInstance().getConnection()) {
                // check requisites
                final int factionId;
                final OBFRole role;
                try(PreparedStatement stm = connection.prepareStatement(
                    "SELECT `factions`.`id` AS `faction_id`,`faction_roles`.`role` FROM `faction_roles` LEFT JOIN `factions` on faction_roles.faction_id = factions.id WHERE `faction_roles`.`player_id`=? AND `factions`.`name`=?"
                )) {
                    stm.setInt(1, meta.playerId);
                    stm.setString(2, faction);
                    try(ResultSet rs = stm.executeQuery()) {
                        if(!rs.next()) throw new OBFException("Not in that faction or faction not found! ");
                        factionId = rs.getInt("faction_id");
                        role = OBFRole.from(rs.getInt("role"));
                    }
                }
                if(role == OBFRole.OWNER || role == OBFRole.OUTSIDER) {
                    // should == OUTSIDER but meh shit can happen
                    throw new OBFException("Owners can't leave the faction, please disband it! ");
                }
                player.sendMessage(String.format("\u00a7bAll checks were passed, now leaving the faction #%d:%s... ", factionId, faction));
                try(PreparedStatement stm = connection.prepareStatement("DELETE FROM `faction_roles` WHERE `faction_id`=? AND `player_id`=?")) {
                    stm.setInt(1, factionId);
                    stm.setInt(2, meta.playerId);
                    stm.executeUpdate();
                    if(stm.getUpdateCount() <= 0) throw new OBFException("Database operation failed! Probable due to not a member! ");
                }
                player.sendMessage("\u00a7aYou left your faction! ");
            } catch (Exception ex) {
                ex.printStackTrace();
                player.sendMessage(String.format("\u00a7cCommand failed to execute<%s>: %s", ex.getClass().getSimpleName(), ex.getMessage()));
            }
        });
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String label, String[] args) {
        return null;
    }
}
