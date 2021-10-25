package io.github.definitlyevil.orbisfactions.commands.admin;

import io.github.definitlyevil.orbisfactions.OrbisFactions;
import io.github.definitlyevil.orbisfactions.commands.ComplexCommand;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class OBFAdminReloadCommand implements ComplexCommand.SubCommand {
    @Override
    public String name() {
        return "reload";
    }

    @Override
    public String usage() {
        return "";
    }

    @Override
    public String description() {
        return "Reload the configurations. ";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        sender.sendMessage("\u00a76Reloading OrbisFactions... ");
        OrbisFactions.getInstance().reloadConfig();
        sender.sendMessage("\u00a7aPlugin reloaded! ");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String label, String[] args) {
        return Collections.emptyList();
    }
}
