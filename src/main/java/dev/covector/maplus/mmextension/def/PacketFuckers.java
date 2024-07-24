package dev.covector.maplus.mmextension.def;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import dev.covector.maplus.mmextension.Ability;
import dev.covector.maplus.mmextension.MMExtUtils;
import dev.covector.maplus.packetfucker.PacketFucker;

public class PacketFuckers extends Ability {
    private String syntax = "<handler-name> <target-uuid> <toggle>? OR clear <target-uuid>";
    private String id = "packetFuckers";

    public String cast(String[] args) {
        if (args.length != 3 && args.length != 2) {
            return "args length must be 2 or 3";
        }

        if (!args[0].equalsIgnoreCase("clear") && !PacketFucker.getInstance().hasHandler(args[0])) {
            return "handler " + args[0] + " does not exist. Available handlers: " + PacketFucker.getInstance().getAllHandlerNames().toString();
        }

        Entity target = MMExtUtils.parseUUID(args[1]);

        // Entity target;
        // try {
        //     target = Bukkit.getEntity(UUID.fromString(args[1]));
        // } catch (IllegalArgumentException e) {
        //     // testing on players
        //     target =  Bukkit.getPlayer(args[1]);
        // }

        if (!(target instanceof Player)) {
            return "target must be a player";
        }

        Player player = (Player) target;

        if (args[0].equalsIgnoreCase("clear")) {
            PacketFucker.getInstance().clearForPlayer(player);
            return null;
        }

        boolean toggle = args.length == 3 ? Boolean.parseBoolean(args[2].toLowerCase()) : !PacketFucker.getInstance().hasPlayer(player, args[0]);

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

    public List<String> getTabComplete(CommandSender sender, String[] argsList) {
        if (argsList.length == 1) {
            return MMExtUtils.streamFilter(
                Stream.concat(
                    PacketFucker.getInstance().getAllHandlerNames().stream(),
                    Stream.of("clear")
                )
            , argsList[0]);
        }

        if (argsList.length == 2 && sender instanceof Player) {
            return MMExtUtils.getLivingEntityTabComplete(argsList[1], (Player) sender);
        }

        if (argsList.length == 3 && !argsList[0].equalsIgnoreCase("clear")) {
            return MMExtUtils.streamFilter(Stream.of("true", "false"), argsList[2]);
        }

        return Collections.emptyList();
    }
}
