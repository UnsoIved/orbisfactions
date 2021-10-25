package io.github.definitlyevil.orbisfactions.commands.faction;

import io.github.definitlyevil.orbisfactions.OBFRole;
import io.github.definitlyevil.orbisfactions.OrbisFactions;
import io.github.definitlyevil.orbisfactions.commands.ComplexCommand;
import io.github.definitlyevil.orbisfactions.util.DBUtils;
import io.github.definitlyevil.orbisfactions.util.OBFPlayerMeta;
import io.github.definitlyevil.orbisfactions.util.Patterns;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class FactionWithdrawCommand implements ComplexCommand.SubCommand {
    @Override
    public String name() {
        return "withdraw";
    }

    @Override
    public String usage() {
        return "<faction> <amount>";
    }

    @Override
    public String description() {
        return "Withdraw from a faction. ";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!Player.class.isAssignableFrom(sender.getClass())) return;
        if(args.length != 2) {
            sender.sendMessage("\u00a7cNo faction and amount specified! ");
            return;
        }
        Player player = (Player) sender;
        String faction = args[0];
        if(!Patterns.FACTION_NAME.matcher(faction).matches()) {
            sender.sendMessage("\u00a7cInvalid faction name! ");
            return;
        }
        double amount = Double.parseDouble(args[1]);
        if(amount < 0.0d) {
            sender.sendMessage("\u00a7cCan not withdraw a negative amount! ");
            return;
        }
        OBFPlayerMeta meta = OBFPlayerMeta.get(player);
        if(meta == null) {
            player.sendMessage("\u00a7cProfile not loaded, please try to rejoin! ");
            return;
        }
        player.sendMessage("\u00a7aChecking permission... ");
        DBUtils.getFactionRole(meta.playerId, faction, (ex, r) -> {
            if(ex != null || r == null || !OBFRole.class.isAssignableFrom(r.getClass())) {
                if(ex != null) player.sendMessage(String.format("\u00a7cError<%s>: %s", ex.getClass().getSimpleName(), ex.getMessage()));
                player.sendMessage("\u00a7cFailed executing command! ");
                return;
            }
            OBFRole role = (OBFRole) r;
            if (!role.checkRole(OBFRole.MOD)) {
                player.sendMessage("\u00a7cNot enough permission! You need to be at least a faction mod. ");
                return;
            }
            DBUtils.depositBank(faction, -amount, (exDeposit, ret) -> {
                if(exDeposit != null) {
                    player.sendMessage(String.format("\u00a7cError<%s>: %s", exDeposit.getClass().getSimpleName(), exDeposit.getMessage()));
                    return;
                }
                Economy eco = OrbisFactions.getInstance().getEconomy();
                if(!eco.depositPlayer(player, amount).transactionSuccess()) {
                    DBUtils.depositBank(faction, amount, (ignoredEx, ignoredObj) -> { });
                    player.sendMessage("\u00a7cDeposit to your account failed, due to a economy plugin issue, not by OrbisFactions. ");
                } else {
                    player.sendMessage("\u00a7aWithdraw complete! ");
                }
            });
        });
    }

    private static final List<String> USAGE_1 = Collections.singletonList("<faction> <amount>");
    private static final List<String> USAGE_2 = Collections.singletonList("<amount>");
    @Override
    public List<String> tabComplete(CommandSender sender, String label, String[] args) {
        if (args.length == 1) {
            return USAGE_1;
        } else if (args.length == 2) {
            return USAGE_2;
        } else return Collections.emptyList();
    }
}
