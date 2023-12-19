package dev.covector.maplus.mmextension;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Arrays;

public class AbilityCommandInterface implements CommandExecutor {
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
}