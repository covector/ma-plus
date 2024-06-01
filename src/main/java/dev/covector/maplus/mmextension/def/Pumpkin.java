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
    private String syntax = "<target-uuid> <toggle>?";
    private String id = "fakePumpkin";

    public String cast(String[] args) {
        if (args.length != 2 && args.length != 1) {
            return "args length must be 2 or 1";
        }

        Entity target = MMExtUtils.parseUUID(args[0]);

        if (!(target instanceof Player)) {
            return "target must be a player";
        }

        Player player = (Player) target;

        boolean toggle = args.length == 2 ? args[1].equals("true") || args[1].equals("1") : !FakePumpkin.getInstance().hasPumpkin(player);

        if (toggle) {
            FakePumpkin.getInstance().applyPumpkin(player);
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

        return super.getTabComplete(sender, argsList);
    }
}
