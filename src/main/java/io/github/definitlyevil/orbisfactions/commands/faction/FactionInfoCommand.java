package io.github.definitlyevil.orbisfactions.commands.faction;

import io.github.definitlyevil.orbisfactions.commands.ComplexCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class FactionInfoCommand implements ComplexCommand.SubCommand {
    @Override
    public String name() {
        return "info";
    }

    @Override
    public String usage() {
        return "[other faction]";
    }

    @Override
    public String description() {
        return "Get faction info of yours or another. ";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        sender.sendMessage("\u00a7eWhoops! This feature is under construction! ");
        // if(!Player.class.isAssignableFrom(sender.getClass())) return;
        // Player p = (Player) sender;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String label, String[] args) {
        return Collections.emptyList();
    }
}
