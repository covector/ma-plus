package dev.covector.maplus.mmextension.def;

import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import dev.covector.maplus.mmextension.Ability;
import dev.covector.maplus.mmextension.MMExtUtils;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;

public class FlashBlind extends Ability {
    private String syntax = "<flashBang-uuid> <target-uuid> <angle-margin> <callback-skill>";
    private String id = "flashBlind";

    public String cast(String[] args) {
        if (args.length != 4) {
            return "args length must be 4";
        }

        Entity flashBang = MMExtUtils.parseUUID(args[0]);
        Entity target = MMExtUtils.parseUUID(args[1]);
        double angleMarginHalf = Double.parseDouble(args[2]) / 2D;
        String callbackSkill = args[3];

        if (!(target instanceof LivingEntity)) {
            return "target must be a living entity";
        }

        if (!(flashBang instanceof LivingEntity)) {
            return "flashBang must be a living entity";
        }

        LivingEntity targetMob = (LivingEntity) target;
        LivingEntity flashBangMob = (LivingEntity) flashBang;

        Location loc = new Location(flashBang.getWorld(), 0D, 0D, 0D);
        Vector diffVec = targetMob.getLocation().subtract(flashBang.getLocation()).toVector().normalize();
        try {
            diffVec.checkFinite();
        } catch (IllegalArgumentException e) {
            // still counts as hit
            // targetPlayer.performCommand("mm test cast -s " + callbackSkill);
            MMExtUtils.castMMSkill(flashBangMob, callbackSkill, targetMob, null);
            return null;
        }

        loc.setDirection(diffVec);
        double angleDiff = Math.abs(loc.getYaw() - targetMob.getEyeLocation().getYaw());
        if (angleDiff > 180 ? angleDiff < 360 - angleMarginHalf : angleDiff > angleMarginHalf) {
            // success hit
            // targetPlayer.performCommand("mm test cast -s " + callbackSkill);
            MMExtUtils.castMMSkill(flashBangMob, callbackSkill, targetMob, null);
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
        if ((argsList.length == 1 || argsList.length == 2) && sender instanceof Player) {
            return MMExtUtils.getLivingEntityTabComplete(argsList[argsList.length-1], (Player) sender);
        }
        
        return Collections.emptyList();
    }
}
