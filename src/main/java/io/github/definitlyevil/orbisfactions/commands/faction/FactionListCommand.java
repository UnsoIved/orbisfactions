package io.github.definitlyevil.orbisfactions.commands.faction;

import com.google.common.collect.Lists;
import io.github.definitlyevil.orbisfactions.OrbisFactions;
import io.github.definitlyevil.orbisfactions.commands.ComplexCommand;
import io.github.definitlyevil.orbisfactions.util.Patterns;
import org.bukkit.command.CommandSender;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class FactionListCommand implements ComplexCommand.SubCommand {

    public static final int PAGE_MAX_RECORDS = 10;

    private static final Pattern PATTERN_INTEGER = Pattern.compile("^[0-9]+$");

    @Override
    public String name() {
        return "list";
    }

    @Override
    public String usage() {
        return "[search] [page]";
    }

    @Override
    public String description() {
        return "List or lookup factions";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        String search = null;
        int page = 1;
        if(args.length == 1) {
            if(PATTERN_INTEGER.matcher(args[0]).matches()) {
                page = Integer.parseInt(args[0]);
                if(page <= 0) {
                    sender.sendMessage("\u00a7cPage must be a positive integer! ");
                    return;
                }
            } else if (Patterns.FACTION_NAME.matcher(args[0]).matches()) {
                search = args[0];
            } else {
                sender.sendMessage(String.format("\u00a7cNot a valid page number or faction name! \"%s\"", args[0]));
                return;
            }
        } else if (args.length == 2) {
            boolean wrong = false;
            if (!Patterns.FACTION_NAME.matcher(args[0]).matches()) {
                sender.sendMessage(String.format("\u00a7c<%s> is not a valid faction name! ", args[0]));
                wrong = true;
            }
            if (!PATTERN_INTEGER.matcher(args[1]).matches()) {
                sender.sendMessage(String.format("\u00a7c<%s> is not a valid page number! ", args[1]));
                wrong = true;
            }
            if (wrong) return;
            search = args[0];
            page = Integer.parseInt(args[1]);
        } else if (args.length != 0) {
            sender.sendMessage("\u00a7cInvalid parameters! ");
            return;
        }
        sender.sendMessage("\u00a7aSearching... ");
        final String fSearch = search;
        final int fPage = page;
        OrbisFactions.getInstance().execute(() -> {
            try(Connection connection = OrbisFactions.getInstance().getConnection()) {
                int max_records;
                {   // Find numbers
                    String sql = "SELECT COUNT(`id`) AS `count` FROM `factions`";
                    if (fSearch != null) {
                        sql += " WHERE `name` LIKE CONCAT(?,'%')";
                    }
                    try (PreparedStatement stm = connection.prepareStatement(sql)) {
                        if (fSearch != null) {
                            stm.setString(1, fSearch);
                        }
                        try(ResultSet rs = stm.executeQuery()) {
                            if(!rs.next()) {
                                sender.sendMessage("\u00a7cDatabase error, no records found! ");
                                return;
                            }
                            max_records = rs.getInt("count");
                        }
                    }
                }

                {   // Actual search
                    String sql = "SELECT * FROM `factions` ";
                    if (fSearch != null) {
                        sql += "WHERE `name` LIKE CONCAT(?,'%') ";
                    }
                    sql += "LIMIT ?,?";
                    try (PreparedStatement stm = connection.prepareStatement(sql)) {
                        if (fSearch != null) {
                            stm.setString(1, fSearch);
                            stm.setInt(2, (fPage-1)*PAGE_MAX_RECORDS);
                            stm.setInt(3, PAGE_MAX_RECORDS);
                        } else {
                            stm.setInt(1, (fPage-1)*PAGE_MAX_RECORDS);
                            stm.setInt(2, PAGE_MAX_RECORDS);
                        }
                        try (ResultSet rs = stm.executeQuery()) {
                            int len = 0;
                            while (rs.next()) {
                                sender.sendMessage(String.format("\u00a79- \u00a7e[%s]\u00a77 (bank: \u00a7d%.2f\u00a77) \u00a7a- \u00a7b%s", rs.getString("name"), rs.getDouble("bank"), rs.getString("description")));
                                len ++;
                            }
                            sender.sendMessage(String.format("\u00a7aFound %d records. ", len));
                            sender.sendMessage(String.format("\u00a76Page %d / %d", fPage, Math.max(max_records/PAGE_MAX_RECORDS, 1)));
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                sender.sendMessage("\u00a7cError: " + ex.getMessage());
            }
        });
    }

    private static List<String> SUGGESTION_A = Arrays.asList("[faction]", "[page]", "[faction] [page]");
    private static List<String> SUGGESTION_B = Collections.singletonList("[page]");
    @Override
    public List<String> tabComplete(CommandSender sender, String label, String[] args) {
        if(args.length == 1) {
            return SUGGESTION_A;
        } else if (args.length == 2) {
            return SUGGESTION_B;
        } else return Collections.emptyList();
    }
}
