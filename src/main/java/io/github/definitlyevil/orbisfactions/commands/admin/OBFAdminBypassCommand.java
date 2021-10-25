package io.github.definitlyevil.orbisfactions.commands.admin;

import io.github.definitlyevil.orbisfactions.OrbisFactions;
import io.github.definitlyevil.orbisfactions.commands.ComplexCommand;
import io.github.definitlyevil.orbisfactions.listeners.ClaimProtectionListener;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Collections;
import java.util.List;

public class OBFAdminBypassCommand implements ComplexCommand.SubCommand {
    @Override
    public String name() {
        return "bypass";
    }

    @Override
    public String usage() {
        return "";
    }

    @Override
    public String description() {
        return "Bypass all protections. ";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!Player.class.isAssignableFrom(sender.getClass())) return;
        Player player = (Player) sender;
        if(player.hasMetadata(ClaimProtectionListener.BYPASS_META_KEY)) {
            player.removeMetadata(ClaimProtectionListener.BYPASS_META_KEY, OrbisFactions.getInstance());
            player.sendMessage("\u00a7bBypass disabled! ");
        } else {
            player.setMetadata(ClaimProtectionListener.BYPASS_META_KEY, new FixedMetadataValue(OrbisFactions.getInstance(), true));
            player.sendMessage("\u00a7aBypass enabled, now ignoring all claim protections! ");
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String label, String[] args) {
        return Collections.emptyList();
    }
}
