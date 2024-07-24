package dev.covector.maplus.mmextension.def;

import java.util.List;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import dev.covector.maplus.Utils;
import dev.covector.maplus.mmextension.Ability;
import dev.covector.maplus.mmextension.MMExtUtils;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;

public class Disorient extends Ability {
    private String syntax = "<target-uuid>";
    private String id = "disorient";

    public String cast(String[] args) {
        if (args.length != 1) {
            return "args length must be 1";
        }

        Entity target = MMExtUtils.parseUUID(args[0]);
        
        if (!(target instanceof Player)) {
            return "target must be a player";
        }

        Player player = (Player) target;

        Location loc = player.getLocation();
        loc.setYaw(loc.getYaw() + 45 + Utils.random.nextInt(270));
        player.teleport(loc);

        return null;
    }

    public String getSyntax() {
        return syntax;
    }

    public String getId() {
        return id;
    }

    public List<String> getTabComplete(CommandSender sender, String[] argsList) {
        if (argsList.length == 1 && sender instanceof Player) {
            return MMExtUtils.getLivingEntityTabComplete(argsList[argsList.length-1], (Player) sender);
        }
        
        return super.getTabComplete(sender, argsList);
    }
}
