package io.github.definitlyevil.orbisfactions.commands.faction;

import io.github.definitlyevil.orbisfactions.OrbisFactions;
import io.github.definitlyevil.orbisfactions.commands.ComplexCommand;
import io.github.definitlyevil.orbisfactions.util.DBUtils;
import io.github.definitlyevil.orbisfactions.util.Patterns;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class FactionDepositCommand implements ComplexCommand.SubCommand {
    @Override
    public String name() {
        return "deposit";
    }

    @Override
    public String usage() {
        return "<faction> <amount>";
    }

    @Override
    public String description() {
        return "Deposit to a faction. ";
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
            sender.sendMessage("\u00a7cCan not deposit a negative amount! ");
            return;
        }
        Economy eco = OrbisFactions.getInstance().getEconomy();
        if(!eco.has(player, amount)) {
            sender.sendMessage("\u00a7cNot enough money in your account! ");
            return;
        }
        if(eco.withdrawPlayer(player, amount).transactionSuccess()) {
            DBUtils.depositBank(faction, amount, (ex, ret) -> {
                if(ex == null) {
                    sender.sendMessage("\u00a7aDeposit complete! ");
                } else {
                    eco.depositPlayer(player, amount);
                    sender.sendMessage("\u00a7cDeposit failed, funds have been returned to your account! ");
                }
            });
        } else {
            sender.sendMessage("\u00a7cTransaction failed, caused by economy plugin, not OrbisFactions! ");
        }
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
