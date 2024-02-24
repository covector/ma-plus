package dev.covector.maplus.mmextension;

import java.util.List;

import org.bukkit.command.CommandSender;

public abstract class Ability {
    public abstract String getSyntax();

    public abstract String getId();

    public abstract String cast(String[] args);

    public abstract List<String> getTabComplete(CommandSender sender, String[] argsList);
}