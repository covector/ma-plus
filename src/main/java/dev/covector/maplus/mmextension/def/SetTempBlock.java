package dev.covector.maplus.mmextension.def;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import dev.covector.maplus.mmapihook.mechanics.TempBlockManager;

public class SetTempBlock extends DefaultParamAbility {
    private String id = "setTempBlock";
    private String syntax = "x:<int> y:<int> z:<int> world:<string> material:<string> replacePolicy:<FIRST|LAST|LONGER> duration:<int,0-for-inf> priority:<int> debug:<boolean>";

    public SetTempBlock() {
        setDefault("material", "DIRT");
        setDefault("replacePolicy", "LAST", List.of("FIRST", "LAST", "LONGER"));
        setDefault("duration", "0");
        setDefault("priority", "0");
    }

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
        Material material;
        try {
            material = Material.valueOf(getParam(parsedParam, "material"));
        } catch (Exception ex) {
            return "'" + getParam(parsedParam, "material") + "' is not a valid block material.";
        }
        if (material == null) {
            return "Invalid material";
        }
        int duration = getInt(parsedParam, "duration");
        int priority = getInt(parsedParam, "priority");
        TempBlockManager.ReplacePolicy replacePolicy = TempBlockManager.ReplacePolicy.valueOf(getParam(parsedParam, "replacePolicy"));

        TempBlockManager.setBlock(material, new Location(world, x, y, z), replacePolicy, duration, priority);
        return null;
    }

    public String getSyntax() {
        return syntax;
    }

    public String getId() {
        return id;
    }
}
