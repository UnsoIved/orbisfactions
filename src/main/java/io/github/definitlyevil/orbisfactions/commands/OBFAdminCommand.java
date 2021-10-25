package io.github.definitlyevil.orbisfactions.commands;

import io.github.definitlyevil.orbisfactions.commands.admin.OBFAdminBypassCommand;
import io.github.definitlyevil.orbisfactions.commands.admin.OBFAdminReloadCommand;

public class OBFAdminCommand extends ComplexCommand {
    public OBFAdminCommand() {
        super("obf.admin");

        register(new OBFAdminReloadCommand());
        register(new OBFAdminBypassCommand());
    }
}
