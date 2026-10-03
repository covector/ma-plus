package dev.covector.maplus.mmextension.def;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import dev.covector.maplus.mmextension.Ability;
import dev.covector.maplus.mmextension.MMExtUtils;
import dev.covector.maplus.packetfucker.PacketFucker;
import dev.covector.maplus.packetfucker.def.MobVision;

public class SetMobVision extends Ability {
    private String syntax = "<player-target> <spectate-target> OR <player-target> clear";
    private String id = "setMobVision";

    public String cast(String[] args) {
        if (args.length != 2) {
            return "args length must be 2";
        }

        Entity playerTarget = MMExtUtils.parseUUID(args[0]);
        if (!(playerTarget instanceof Player)) {
            return "player target must be a player";
        }

        Entity spectateTarget;
        if (args[1].equalsIgnoreCase("clear")) {
            // set spectate target to self will clear
            spectateTarget = playerTarget;
        } else {
            spectateTarget = MMExtUtils.parseUUID(args[1]);
            if (!(spectateTarget instanceof LivingEntity)) {
                return "spectate target must be a living entity";
            }
        }        
        
        // send packet
        MobVision mobVision = (MobVision) PacketFucker.getInstance().getPacketHandler("mobVision");
        mobVision.sendPacket((Player) playerTarget, ((LivingEntity) spectateTarget).getEntityId());

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
            return MMExtUtils.getLivingEntityTabComplete(argsList[argsList.length - 1]);
        } else if ((argsList.length == 2) && sender instanceof Player) {
            List<String> uuidList = new ArrayList<>(MMExtUtils.getLivingEntityTabComplete(argsList[argsList.length - 1], (Player) sender));
            uuidList.add(0, "clear");
            return uuidList;
        }

        return super.getTabComplete(sender, argsList);
    }
}
