package dev.covector.maplus.mmextension.def;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import dev.covector.maplus.mmapihook.mechanics.TempBlockManager;

public class RemoveTempBlock extends DefaultParamAbility {
    private String id = "removeTempBlock";
    private String syntax = "x:<int> y:<int> z:<int> world:<string> debug:<boolean>";

    public String cast(String[] args) {
        if (args.length < 4) {
            return "args length must at least be 4";
        }
        ParsedParam parsedParam;
        try {
            parsedParam = parse(args);
        } catch (Exception e) {
            return e.getMessage();
        }

        int x = getInt(parsedParam, "x");
        int y = getInt(parsedParam, "y");
        int z = getInt(parsedParam, "z");
        World world = Bukkit.getWorld(getParam(parsedParam, "world"));
        if (world == null) {
            return "Invalid world: " + getParam(parsedParam, "world");
        }
        
        TempBlockManager.removeBlock(new Location(world, x, y, z));
        return null;
    }

    public String getSyntax() {
        return syntax;
    }

    public String getId() {
        return id;
    }
}
