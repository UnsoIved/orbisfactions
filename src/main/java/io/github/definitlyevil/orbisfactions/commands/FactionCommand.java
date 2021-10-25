package io.github.definitlyevil.orbisfactions.commands;

import io.github.definitlyevil.orbisfactions.commands.faction.*;

public class FactionCommand extends ComplexCommand {

    public FactionCommand() {
        super("obf.command.faction");

        register(new FactionMeCommand());
        register(new FactionInfoCommand());
        register(new FactionCreateCommand());
        register(new FactionLeaveCommand());
        register(new FactionListCommand());
        register(new FactionDepositCommand());
        register(new FactionWithdrawCommand());
        register(new FactionClaimCommand());
        register(new FactionUnclaimCommand());
        register(new FactionRoleCommand());
    }

}
