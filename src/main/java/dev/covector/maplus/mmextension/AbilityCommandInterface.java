package dev.covector.maplus.mmextension;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.HumanEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AbilityCommandInterface implements CommandExecutor, TabCompleter {
    private AbilityRegistry registry;

    public AbilityCommandInterface() {
        this.registry = new AbilityRegistry();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            return false;
        }

        Ability ab = registry.getAbility(args[0]);
        boolean silent = false;
        if (args.length > 1 && args[1].equals("-s")) {
            silent = true;
        }
        if (!silent && ab == null) {
            Bukkit.broadcastMessage("Ability " + args[0] + " not found!");
            Bukkit.broadcastMessage("Available abilities: " + registry.getAbilityIds().toString());
            return true;
        }

        String[] argsList = Arrays.copyOfRange(args, silent ? 2 : 1, args.length);
        String error = ab.cast(argsList);
        if (!silent && error != null) {
            Bukkit.broadcastMessage("Failed to cast " + args[0]);
            Bukkit.broadcastMessage(error);
            Bukkit.broadcastMessage("Correct Syntax: /mmability " + args[0] + " " + ab.getSyntax());
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return MMExtUtils.streamFilter(registry.getAbilityIds(), args[0]);
        }

        if (args.length > 2 && args[1].equals("-s")) {
            Ability ab = registry.getAbility(args[0]);
            if (ab != null) {
                String[] argsList = Arrays.copyOfRange(args, 2, args.length);
                return ab.getTabComplete(sender, argsList);
            }
            return Collections.emptyList();
        }
        
        if (args.length == 2) {
            Ability ab = registry.getAbility(args[0]);
            if (ab != null) {
                String[] argsList = Arrays.copyOfRange(args, 1, args.length);
                return Stream.concat(
                    ab.getTabComplete(sender, argsList).stream(),
                    Stream.of("-s").filter(n -> "-s".startsWith(args[1]))
                ).collect(Collectors.toList());
            }
            return Collections.emptyList();
        }

        if (args.length > 2) {
            Ability ab = registry.getAbility(args[0]);
            if (ab != null) {
                String[] argsList = Arrays.copyOfRange(args, 1, args.length);
                return ab.getTabComplete(sender, argsList);
            }
            return Collections.emptyList();
        }

        return Collections.emptyList();
    }
}