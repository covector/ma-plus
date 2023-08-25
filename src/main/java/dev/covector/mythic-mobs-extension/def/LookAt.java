package dev.covector.maplus.mmextension;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;
import org.bukkit.Location;

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
}
