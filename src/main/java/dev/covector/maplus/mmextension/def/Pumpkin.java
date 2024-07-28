package dev.covector.maplus.mmextension.def;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;

import dev.covector.maplus.fakepumpkin.FakePumpkin;
import dev.covector.maplus.mmextension.Ability;
import dev.covector.maplus.mmextension.MMExtUtils;
import dev.covector.maplus.packetfucker.PacketFucker;

public class Pumpkin extends Ability {
    private String syntax = "<target-uuid> <alt-blur-index>? <toggle>?";
    private String id = "fakePumpkin";

    public String cast(String[] args) {
        if (args.length != 3 && args.length != 2 && args.length != 1) {
            return "args length must be between 1 to 3";
        }

        Entity target = MMExtUtils.parseUUID(args[0]);

        if (!(target instanceof Player)) {
            return "target must be a player";
        }

        Player player = (Player) target;

        int altBlurInd = -1;
        if (args.length >= 2) {
            try {
                altBlurInd = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
            }

        }
        boolean toggle = (args.length == 3 || (args.length == 2 && altBlurInd == -1)) ?
            (args.length == 3 ? args[2] : args[1]).equalsIgnoreCase("true") :
            !FakePumpkin.getInstance().hasPumpkin(player);
        altBlurInd = altBlurInd == -1 ? 0 : altBlurInd; 

        if (toggle) {
            FakePumpkin.getInstance().applyPumpkin(player, altBlurInd);
        } else {
            FakePumpkin.getInstance().removePumpkin(player);
        }
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
            return MMExtUtils.getLivingEntityTabComplete(argsList[0], (Player) sender);
        }

        if (argsList.length == 2) {
            return MMExtUtils.streamFilter(Stream.of("true", "false"), argsList[1]);
        }

        if (argsList.length == 3) {
            try {
                Integer.parseInt(argsList[1]);
                return MMExtUtils.streamFilter(Stream.of("true", "false"), argsList[2]);
            } catch (NumberFormatException e) {
            }
        }

        return super.getTabComplete(sender, argsList);
    }
}
