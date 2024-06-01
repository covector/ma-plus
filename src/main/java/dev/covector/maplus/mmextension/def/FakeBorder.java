package dev.covector.maplus.mmextension.def;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import dev.covector.maplus.mmextension.Ability;
import dev.covector.maplus.mmextension.MMExtUtils;
import dev.covector.maplus.packetfucker.PacketFucker;
import dev.covector.maplus.packetfucker.def.WorldBorder;

public class FakeBorder extends Ability {
    private String syntax = "<target-uuid> <old-radius> <new-radius> <seconds-to-shrink> or <target-uuid> false";
    private String id = "fakeBorder";

    public String cast(String[] args) {
        if (args.length != 4 && args.length != 2) {
            return "args length must be 4 or 2";
        }

        Entity target = MMExtUtils.parseUUID(args[0]);

        // Entity target;
        // try {
        //     target = Bukkit.getEntity(UUID.fromString(args[0]));
        // } catch (IllegalArgumentException e) {
        //     // testing on players
        //     target =  Bukkit.getPlayer(args[0]);
        // }

        if (!(target instanceof Player)) {
            return "target must be a player";
        }

        Player player = (Player) target;
        WorldBorder worldBorder = (WorldBorder) PacketFucker.getInstance().getPacketHandler("worldBorder");
        
        if (args.length == 4) {
            worldBorder.sendPacket(
                player,
                Double.parseDouble(args[1]),
                Double.parseDouble(args[2]),
                (long) (Double.parseDouble(args[3]) * 1000),
                29999984, 2, 2
            );
        } else {
            worldBorder.sendResetPacket(player);
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
            return MMExtUtils.streamFilter(Stream.of("false"), argsList[1]);
        }

        return super.getTabComplete(sender, argsList);
    }
}
