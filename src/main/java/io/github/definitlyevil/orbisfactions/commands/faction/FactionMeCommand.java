package io.github.definitlyevil.orbisfactions.commands.faction;

import io.github.definitlyevil.orbisfactions.OBFRole;
import io.github.definitlyevil.orbisfactions.OrbisFactions;
import io.github.definitlyevil.orbisfactions.commands.ComplexCommand;
import io.github.definitlyevil.orbisfactions.util.OBFPlayerMeta;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collections;
import java.util.List;

public class FactionMeCommand implements ComplexCommand.SubCommand {
    @Override
    public String name() {
        return "me";
    }

    @Override
    public String usage() {
        return "";
    }

    @Override
    public String description() {
        return "See your status. ";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!Player.class.isAssignableFrom(sender.getClass())) return;
        Player player = (Player) sender;
        OBFPlayerMeta meta = OBFPlayerMeta.get(player);
        if(meta == null) {
            player.sendMessage("\u00a7cProfile not loaded! Please try to rejoin. ");
            return;
        }

        OrbisFactions.getInstance().execute(() -> {
            // get factions the player is in
            try(Connection connection = OrbisFactions.getInstance().getConnection()) {
                final String sql =
                        "SELECT `factions`.`id` AS `faction_id`,`factions`.`name` AS `faction_name`,`factions`.`description` AS `faction_description`,`factions`.`bank` AS `faction_bank`,`faction_roles`.`role` AS `faction_role` " +
                        "FROM `faction_roles` " +
                        "LEFT JOIN `factions` on faction_roles.faction_id = factions.id " +
                        "WHERE `faction_roles`.`player_id`=?";
                try(PreparedStatement stm = connection.prepareStatement(sql)) {
                    stm.setInt(1, meta.playerId);
                    try(ResultSet rs = stm.executeQuery()) {
                        int counter = 0;
                        while(rs.next()) {
                            counter ++;

                            player.sendMessage(String.format("\u00a7e%s \u00a77in #%d:\u00a76%s\u00a77(\u00a7d%.2f\u00a77) \u00a7a- \u00a7b%s", OBFRole.from(rs.getInt("faction_role")).name(), rs.getInt("faction_id"), rs.getString("faction_name"), rs.getDouble("faction_bank"), rs.getString("faction_description")));
                        }
                        if(counter <= 0) {
                            player.sendMessage("\u00a7cYou're not in a faction! ");
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                player.sendMessage(String.format("\u00a7cError<%s>: %s", ex.getClass().getSimpleName(), ex.getMessage()));
            }
        });
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String label, String[] args) {
        return Collections.emptyList();
    }
}
