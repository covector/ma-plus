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

public class LookAt extends Ability {
    private String syntax = "<view-target-uuid> <target-uuid>";
    private String id = "lookAt";

    public String cast(String[] args) {
        if (args.length != 2) {
            return "args length must be 2";
        }

        Entity viewTarget = MMExtUtils.parseUUID(args[0]);
        Entity target = MMExtUtils.parseUUID(args[1]);

        if (!(target instanceof LivingEntity)) {
            return "target must be a living entity";
        }

        if (!(viewTarget instanceof LivingEntity)) {
            return "viewTarget must be a living entity";
        }

        LivingEntity targetMob = (LivingEntity) target;
        LivingEntity viewTargetMob = (LivingEntity) viewTarget;

        Location loc = targetMob.getLocation().clone();
        Vector diffVec = viewTargetMob.getEyeLocation().subtract(targetMob.getEyeLocation()).toVector().normalize();
        loc.setDirection(diffVec);
        try {
            diffVec.checkFinite();
        } catch (IllegalArgumentException e) {
            return null;
        }

        targetMob.teleport(loc);
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
        
        return super.getTabComplete(sender, argsList);
    }
}
