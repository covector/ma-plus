package dev.covector.maplus.mmextension.def;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import dev.covector.maplus.mmextension.Ability;
import dev.covector.maplus.mmextension.MMExtUtils;
import dev.covector.maplus.packetfucker.PacketFucker;

public class PacketFuckers extends Ability {
    private String syntax = "<handler-name> <target-uuid> <toggle>?";
    private String id = "packetFuckers";

    public String cast(String[] args) {
        if (args.length != 3 && args.length != 2) {
            return "args length must be 2 or 3";
        }

        if (!PacketFucker.getInstance().hasHandler(args[0])) {
            return "handler " + args[0] + " does not exist. Available handlers: " + PacketFucker.getInstance().getAllHandlerNames().toString();
        }

        Entity target = MMExtUtils.parseUUID(args[1]);

        if (!(target instanceof Player)) {
            return "target must be a player";
        }

        Player player = (Player) target;

        boolean toggle = args.length == 3 ? Boolean.parseBoolean(args[2]) : !PacketFucker.getInstance().hasPlayer(player, args[0]);

        if (toggle) {
            PacketFucker.getInstance().addPlayer(player, args[0]);
        } else {
            PacketFucker.getInstance().removePlayer(player, args[0]);
        }
        return null;
    }

    public String getSyntax() {
        return syntax;
    }

    public String getId() {
        return id;
    }
}
