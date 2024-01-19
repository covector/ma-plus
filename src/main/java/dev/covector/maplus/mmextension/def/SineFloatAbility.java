package dev.covector.maplus.mmextension.def;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import dev.covector.maplus.mmextension.Ability;
import dev.covector.maplus.mmextension.MMExtUtils;
import dev.covector.maplus.packetfucker.PacketFucker;
import dev.covector.maplus.packetfucker.def.SineFloatIndv;

public class SineFloatAbility extends Ability {
    private String syntax = "<observer-uuid> <target-uuid> <toggle>?";
    private String id = "sineFloat";

    public String cast(String[] args) {
        if (args.length != 2 && args.length != 3) {
            return "args length must be 2 or 3";
        }

        Entity observer = MMExtUtils.parseUUID(args[0]);
        // Entity observer;
        // try {
        //     observer = Bukkit.getEntity(UUID.fromString(args[0]));
        // } catch (IllegalArgumentException e) {
        //     // testing on players
        //     observer =  Bukkit.getPlayer(args[0]);
        // }
        if (!(observer instanceof Player)) {
            return "observer must be a player";
        }
        Player player = (Player) observer;

        Entity target = MMExtUtils.parseUUID(args[1]);
        // Entity target;
        // try {
        //     target = Bukkit.getEntity(UUID.fromString(args[1]));
        // } catch (IllegalArgumentException e) {
        //     // testing on players
        //     target =  Bukkit.getPlayer(args[1]);
        // }
        if (!(target instanceof LivingEntity)) {
            return "target must be a living entity";
        }
        LivingEntity targetEntity = (LivingEntity) target;

        SineFloatIndv sinFloatIndv = (SineFloatIndv) PacketFucker.getInstance().getPacketHandler("sineFloatINDV");

        boolean toggle = args.length == 4 ? Boolean.parseBoolean(args[3]) : !sinFloatIndv.hasPairs(player, targetEntity);
        
        if (toggle) {
            sinFloatIndv.addPairs(player, targetEntity);
        } else {
            sinFloatIndv.removePairs(player, targetEntity);
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
