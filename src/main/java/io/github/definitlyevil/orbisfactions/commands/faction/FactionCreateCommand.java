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
import java.sql.Statement;
import java.util.Collections;
import java.util.List;

public class FactionCreateCommand implements ComplexCommand.SubCommand {
    @Override
    public String name() {
        return "create";
    }

    @Override
    public String usage() {
        return "<name>";
    }

    @Override
    public String description() {
        return "Create a new faction. ";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!Player.class.isAssignableFrom(sender.getClass())) return;
        if(args.length != 1) {
            sender.sendMessage("\u00a76Invalid faction name to create. ");
            return;
        }

        final String faction = args[0];
        if(!Patterns.FACTION_NAME.matcher(faction).matches()) {
            sender.sendMessage("\u00a7cInvalid faction name! \n\u00a76Faction name can only begin with letters, and can include numbers, underscores in the middle or at the end. ");
            return;
        }

        Player player = (Player) sender;
        OBFPlayerMeta meta = OBFPlayerMeta.get(player);
        if(meta == null) {
            player.sendMessage("\u00a7cCan't do that, because the profile for you is not loaded! Please try to rejoin. ");
            return;
        }
        player.sendMessage("\u00a7aCreating a faction... ");
        OrbisFactions.getInstance().execute(() -> {
            try(Connection connection = OrbisFactions.getInstance().getConnection()) {
                boolean savedAutoCommit = connection.getAutoCommit();
                connection.setAutoCommit(false);
                try {
                    // check for existing roles
                    try (PreparedStatement stm = connection.prepareStatement("SELECT COUNT(`id`) AS `count` FROM `faction_roles` WHERE `faction_roles`.`player_id`=?")) {
                        stm.setInt(1, meta.playerId);
                        try (ResultSet rs = stm.executeQuery()) {
                            if (!rs.next()) throw new OBFException("Failed to lookup existing roles in other factions! ");
                            if (rs.getInt("count") > 0) throw new OBFException("Already in another faction! ");
                        }
                    }
                    int factionId;
                    try (PreparedStatement stm = connection.prepareStatement(
                        "INSERT INTO `factions` (`name`,`description`,`bank`) VALUES(?,'',0.0)",
                        Statement.RETURN_GENERATED_KEYS
                    )) {
                        stm.setString(1, faction);
                        stm.execute();
                        try(ResultSet rs = stm.getGeneratedKeys()) {
                            if(!rs.next()) throw new OBFException("Failed creating a new faction, faction ID is missing! ");
                            factionId = rs.getInt(1);
                        }
                    }
                    try (PreparedStatement stm = connection.prepareStatement(
                        "INSERT INTO `faction_roles` (`faction_id`,`player_id`,`role`) VALUE(?,?,?)"
                    )) {
                        stm.setInt(1, factionId);
                        stm.setInt(2, meta.playerId);
                        stm.setInt(3, OBFRole.OWNER.getRoleNumber());
                        stm.executeUpdate();
                    }
                    connection.commit();

                    meta.factionRoles.put(factionId, OBFRole.OWNER);
                    if(!meta.factions.contains(faction)) meta.factions.add(faction);

                    player.sendMessage(String.format("\u00a7aFaction <%s> created with ID #%d! ", faction, factionId));
                } finally {
                    connection.setAutoCommit(savedAutoCommit);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                player.sendMessage(String.format("\u00a7cError: %s", ex.getMessage()));
            }
        });
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String label, String[] args) {
        return Collections.emptyList();
    }
}
