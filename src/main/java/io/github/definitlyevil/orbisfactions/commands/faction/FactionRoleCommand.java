package io.github.definitlyevil.orbisfactions.commands.faction;

import io.github.definitlyevil.orbisfactions.OBFRole;
import io.github.definitlyevil.orbisfactions.OrbisFactions;
import io.github.definitlyevil.orbisfactions.commands.ComplexCommand;
import io.github.definitlyevil.orbisfactions.util.DBUtils;
import io.github.definitlyevil.orbisfactions.util.OBFPlayerMeta;
import io.github.definitlyevil.orbisfactions.util.Patterns;
import io.github.definitlyevil.orbisfactions.util.exceptions.FactionNotFoundException;
import io.github.definitlyevil.orbisfactions.util.exceptions.OBFException;
import io.github.definitlyevil.orbisfactions.util.exceptions.PlayerNotFoundException;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FactionRoleCommand implements ComplexCommand.SubCommand {
    private static final List<String> USAGE_1 = Collections.singletonList("<faction> <player> <role>");
    private static final List<String> ROLES = new ArrayList<>(OBFRole.values().length);
    static {
        for(OBFRole role : OBFRole.values()) {
            ROLES.add(role.name().toLowerCase());
        }
    }

    @Override
    public String name() {
        return "role";
    }

    @Override
    public String usage() {
        return "<faction> <player> <role>";
    }

    @Override
    public String description() {
        return "Change a player's role. ";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!Player.class.isAssignableFrom(sender.getClass())) return;
        if(args.length != 3) {
            sender.sendMessage("\u00a76Invalid parameters. ");
            return;
        }

        final String faction = args[0];
        if(!Patterns.FACTION_NAME.matcher(faction).matches()) {
            sender.sendMessage("\u00a7cInvalid faction name! \n\u00a76Faction name can only begin with letters, and can include numbers, underscores in the middle or at the end. ");
            return;
        }

        final String targetUsername = args[1];
        if(!Patterns.USERNAME.matcher(targetUsername).matches()) {
            sender.sendMessage("\u00a7cInvalid username! ");
            return;
        }

        final OBFRole targetRole;
        try {
            targetRole = OBFRole.valueOf(args[2].toUpperCase());
        } catch (Exception ex) {
            sender.sendMessage("\u00a7cInvalid role name, use tab complete to find out all of them! ");
            return;
        }

        if(targetRole == OBFRole.OWNER) {
            sender.sendMessage("\u00a7cYou can not make someone owner! ");
            return;
        }

        Player player = (Player) sender;
        OBFPlayerMeta meta = OBFPlayerMeta.get(player);
        if(meta == null) {
            player.sendMessage("\u00a7cProfile not loaded, please try to rejoin! ");
            return;
        }

        final Player targetPlayer = Bukkit.getPlayer(targetUsername);
        OBFPlayerMeta targetMeta = null;
        if(targetPlayer != null) {
            targetMeta = OBFPlayerMeta.get(targetPlayer);
        }
        final OBFPlayerMeta fTargetMeta = targetMeta;

        sender.sendMessage("\u00a7aChecking permissions... ");
        DBUtils.getFactionRole(meta.playerId, faction, (ex, r) -> {
            if(ex != null) {
                player.sendMessage(String.format("\u00a7cError<%s>: %s", ex.getClass().getSimpleName(), ex.getMessage()));
                return;
            }
            OBFRole role = (OBFRole) r;
            if(role.getRoleNumber() >= targetRole.getRoleNumber()) {
                // not permitted
                player.sendMessage("\u00a7cNot enough permission to do it! ");
                return;
            }

            OrbisFactions.getInstance().execute(() -> {
                try(Connection connection = OrbisFactions.getInstance().getConnection()) {
                    final int targetPlayerId;
                    final int targetFactionId;

                    // lookup player ID
                    try(PreparedStatement stm = connection.prepareStatement("SELECT `id` FROM `players` WHERE `username`=?")) {
                        stm.setString(1, targetUsername);
                        try(ResultSet rs = stm.executeQuery()) {
                            if(!rs.next()) throw new PlayerNotFoundException(targetUsername);
                            targetPlayerId = rs.getInt("id");
                        }
                    }

                    if(targetPlayerId == meta.playerId && targetRole == OBFRole.OUTSIDER) {
                        if(role == OBFRole.OWNER) {
                            throw new OBFException("Owners can not remove themselves from the faction, must use \u00a76/f disband \u00a7c! ");
                        }
                    }

                    // lookup faction ID
                    try(PreparedStatement stm = connection.prepareStatement("SELECT `id` FROM `factions` WHERE `name`=?")) {
                        stm.setString(1, faction);
                        try(ResultSet rs = stm.executeQuery()) {
                            if(!rs.next()) throw new FactionNotFoundException(faction);
                            targetFactionId = rs.getInt("id");
                        }
                    }

                    // detect add or not
                    boolean is_add = false;
                    if(targetRole != OBFRole.OUTSIDER) {
                        try(PreparedStatement stm = connection.prepareStatement("SELECT `id` FROM `faction_roles` WHERE `faction_id`=? AND `player_id`=? LIMIT 1")) {
                            stm.setInt(1, targetFactionId);
                            stm.setInt(2, targetPlayerId);
                            try(ResultSet rs = stm.executeQuery()) {
                                if(!rs.next()) {
                                    is_add = true;
                                }
                            }
                        }
                    }

                    // check existing faction players
                    if(is_add) {
                        try(PreparedStatement stm = connection.prepareStatement("SELECT COUNT(`id`) AS `member_count` FROM `faction_roles` WHERE `faction_id`=?")) {
                            stm.setInt(1, targetFactionId);
                            try(ResultSet rs = stm.executeQuery()) {
                                if(!rs.next()) throw new OBFException("Failed getting existing members! ");
                                int existing_members = rs.getInt("member_count");
                                final int max_allowed = OrbisFactions.getInstance().getConfig().getInt("limits.faction-max-members", 15);
                                if(existing_members >= max_allowed) {
                                    throw new OBFException(String.format("Faction reached max player limit(%d)! ", existing_members));
                                }
                            }
                        }
                        try(PreparedStatement stm = connection.prepareStatement("SELECT `id` FROM `faction_roles` WHERE `player_id`=?")) {
                            stm.setInt(1, targetPlayerId);
                            try(ResultSet rs = stm.executeQuery()) {
                                if(rs.next()) {
                                    throw new OBFException("That player is already in another faction! ");
                                }
                            }
                        }
                    }

                    if(targetRole == OBFRole.OUTSIDER) {
                        // delete
                        try (PreparedStatement stm = connection.prepareStatement("DELETE FROM `faction_roles` WHERE `faction_id`=? AND `player_id`=?")) {
                            stm.setInt(1, targetFactionId);
                            stm.setInt(2, targetPlayerId);
                            stm.executeUpdate();
                            if(stm.getUpdateCount() <= 0) throw new OBFException("Failed changing role! ");
                            player.sendMessage("\u00a7aPlayer removed from faction! ");
                        }
                        if(fTargetMeta != null) OrbisFactions.getInstance().primary(() -> {
                            fTargetMeta.factionRoles.remove(targetFactionId);
                            fTargetMeta.factions.remove(faction);
                            if(targetPlayer != null) targetPlayer.sendMessage(String.format("\u00a76You have been removed from faction %s! ", faction));
                        });
                    } else {
                        // change
                        try(PreparedStatement stm = connection.prepareStatement("REPLACE INTO `faction_roles` (`player_id`,`faction_id`,`role`) VALUES (?,?,?)")) {
                            stm.setInt(1, targetPlayerId);
                            stm.setInt(2, targetFactionId);
                            stm.setInt(3, targetRole.getRoleNumber());
                            stm.executeUpdate();
                            if(stm.getUpdateCount() <= 0) throw new OBFException("Failed changing role! ");
                            player.sendMessage(String.format("\u00a7aPlayer role changed to %s! ", targetRole.name()));
                        }
                        if(fTargetMeta != null) OrbisFactions.getInstance().primary(() -> {
                            fTargetMeta.factionRoles.put(targetFactionId, targetRole);
                            if(!fTargetMeta.factions.contains(faction)) fTargetMeta.factions.add(faction);
                            if(targetPlayer != null) targetPlayer.sendMessage(String.format("\u00a76Your role in faction %s have been changed to %s! ", faction, targetRole.name().toLowerCase()));
                        });
                    }
                } catch (Exception exSQL) {
                    exSQL.printStackTrace();
                    OrbisFactions.getInstance().getLogger().severe(String.format("Failed changing player's role<%s>: %s", exSQL.getClass().getSimpleName(), exSQL.getMessage()));
                    player.sendMessage(String.format("\u00a7cError<%s>: %s", exSQL.getClass().getSimpleName(), exSQL.getMessage()));
                }
            });
        });
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String label, String[] args) {
        if(args.length == 1) {
            return USAGE_1;
        } else if(args.length == 2) {
            return null;
        } else if(args.length == 3) {
            return ROLES;
        }
        return Collections.emptyList();
    }
}
