package io.github.definitlyevil.orbisfactions.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;

import java.util.*;
import java.util.regex.Pattern;

public abstract class ComplexCommand implements TabExecutor {

    private static final Pattern COMMAND_NAME_PATTERN = Pattern.compile("^[a-z0-9\\-]+$");

    private final Map<String, SubCommand> commands = new HashMap<>();

    private List<String> commandListToSend = null;

    private final String permissionPrefix;

    public ComplexCommand(String permissionPrefix) {
        this.permissionPrefix = permissionPrefix;
    }

    protected void register(SubCommand command) {
        if(command == null || command.name() == null || !COMMAND_NAME_PATTERN.matcher(command.name()).matches()) throw new IllegalArgumentException("Invalid subcommand");
        if(commands.containsKey(command.name())) throw new IllegalArgumentException("Already registered! ");
        commands.put(command.name(), command);
        if(commandListToSend != null) commandListToSend = null;
    }

    private static final String USAGE_FORMAT =
        "\u00a76/%s %s \u00a7d%s \u00a7a- \u00a7b%s";
    public void sendUsages(String label, CommandSender sender) {
        sender.sendMessage("\u00a77\u00a7l\u00a7m========================");
        for(Map.Entry<String, SubCommand> entry : commands.entrySet()) {
            String name = entry.getKey();
            SubCommand cmd = entry.getValue();
            sender.sendMessage(
                String.format(USAGE_FORMAT, label, name, cmd.usage(), cmd.description())
            );
        }
        sender.sendMessage("\u00a77\u00a7l\u00a7m========================");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length == 0) {
            sendUsages(label, sender);
            return true;
        }
        String name = args[0].toLowerCase();
        SubCommand sub = commands.get(name);
        if(sub == null) {
            sendUsages(label, sender);
            return true;
        }
        if(
                (permissionPrefix != null && !permissionPrefix.isEmpty()) ||
                (sub.subPermission() != null && !sub.subPermission().isEmpty())
        ) {
            final String perm_needed = (permissionPrefix != null ? permissionPrefix : "") + (sub.subPermission() != null ? sub.subPermission() : "");
            if(!sender.hasPermission(perm_needed)) {
                sender.sendMessage(String.format("\u00a7cNo permission! \u00a77(%s)", perm_needed));
                return true;
            }
        }
        sub.execute(sender, Arrays.copyOfRange(args, 1, args.length));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String label, String[] args) {
        if (args.length == 1) {
            if(commandListToSend == null) commandListToSend = new ArrayList<>(new ArrayList<>(commands.keySet()));
            return commandListToSend;
        } else if (args.length > 1) {
            String n = args[0].toLowerCase();
            SubCommand sub = commands.get(n);
            if(sub == null) return Collections.emptyList();
            return sub.tabComplete(commandSender, label, Arrays.copyOfRange(args, 1, args.length));
        } else return Collections.emptyList();
    }

    public void setup(PluginCommand pc) {
        pc.setExecutor(this);
        pc.setTabCompleter(this);
    }

    public interface SubCommand {
        String name();
        String usage();
        String description();
        default String subPermission() { return null; }
        void execute(CommandSender sender, String[] args);
        List<String> tabComplete(CommandSender sender, String label, String[] args);
    }
}
