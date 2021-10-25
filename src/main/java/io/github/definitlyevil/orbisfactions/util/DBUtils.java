package io.github.definitlyevil.orbisfactions.util;

import io.github.definitlyevil.orbisfactions.OBFRole;
import io.github.definitlyevil.orbisfactions.OrbisFactions;
import io.github.definitlyevil.orbisfactions.util.exceptions.FactionNotFoundException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.function.BiConsumer;

public class DBUtils {

    public static void getFactionRole(int playerId, String faction, BiConsumer<Exception, Object> callback) {
        executeCallbackTask(() -> {
            OBFRole role = OBFRole.OUTSIDER;
            try (Connection connection = OrbisFactions.getInstance().getConnection()) {
                final String sql =
                    "SELECT `faction_roles`.`role` AS `role_number` FROM `faction_roles` " +
                        "LEFT JOIN `players` ON `players`.`id`=`faction_roles`.`player_id` " +
                        "LEFT JOIN `factions` ON `factions`.`name`=?" +
                        "WHERE `faction_roles`.`player_id`=? AND `faction_roles`.`faction_id`=`factions`.`id` LIMIT 1";
                try (PreparedStatement stm = connection.prepareStatement(sql)) {
                    stm.setString(1, faction);
                    stm.setInt(2, playerId);
                    try(ResultSet rs = stm.executeQuery()) {
                        if(rs.next()) {
                            role = OBFRole.from(rs.getInt("role_number"));
                        }
                    }
                }
            } finally {
                return role;
            }
        }, callback);
    }

    public static void depositBank(String faction, double amount, BiConsumer<Exception, Object> callback) {
        executeCallbackTask(() -> {
            try (Connection connection = OrbisFactions.getInstance().getConnection()) {
                final String sql = "UPDATE `factions` SET `factions`.`bank`=`factions`.`bank`+(?) WHERE `factions`.`name`=? AND `factions`.`bank`+(?)>=0.0";
                try (PreparedStatement stm = connection.prepareStatement(sql)) {
                    stm.setDouble(1, amount);
                    stm.setString(2, faction);
                    stm.setDouble(3, amount);
                    stm.executeUpdate();
                    int changed = stm.getUpdateCount();
                    if(changed <= 0) throw new FactionNotFoundException(faction);
                }
                return null;
            }
        }, callback);
    }


    public static void executeCallbackTask(ExceptionRunnable runnable, BiConsumer<Exception, Object> callback) {
        OrbisFactions.getInstance().execute(() -> {
            try {
                Object ret = runnable.run();
                OrbisFactions.getInstance().primary(() -> callback.accept(null, ret));
            } catch (Exception ex) {
                ex.printStackTrace();
                OrbisFactions.getInstance().primary(() -> callback.accept(ex, null));
            }
        });
    }

}
