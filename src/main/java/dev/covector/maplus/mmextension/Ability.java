package dev.covector.maplus.mmextension;

import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public abstract class Ability {
    public abstract String getSyntax();

    public abstract String getId();

    public abstract String cast(String[] args);

    public List<String> getTabComplete(CommandSender sender, String[] argsList) {
        String[] argSyntax = getSyntax().split(" ");
        if (argsList.length <= argSyntax.length && argsList[argsList.length - 1].isEmpty()) {
            return Collections.singletonList(argSyntax[argsList.length - 1]);
        }
        return Collections.emptyList();
    }
}