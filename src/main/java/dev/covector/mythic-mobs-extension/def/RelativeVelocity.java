package dev.covector.maplus.mmextension;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import java.util.UUID;

public class RelativeVelocity extends Ability {
    private String syntax = "<mob-uuid> <vx> <vy> <vz> <SET|ADD> <global-y>";
    private String id = "relativeVelocity";

    public String cast(String[] args) {
        if (args.length != 6) {
            return "args length must be 6";
        }

        String mobUUID = args[0];
        double vx = Double.parseDouble(args[1]);
        double vy = Double.parseDouble(args[2]);
        double vz = Double.parseDouble(args[3]);
        String mode = args[4].toUpperCase();
        boolean globalY = args[5].equals("true") || args[5].equals("1");
        Entity entity = MMExtUtils.parseUUID(mobUUID);

        if (!(entity instanceof LivingEntity)) {
            return "entity must be a living entity";
        }
        LivingEntity mob = (LivingEntity) entity;

        double cosYaw = Math.cos(mob.getEyeLocation().getYaw() * Math.PI / 180);
        double sinYaw = Math.sin(mob.getEyeLocation().getYaw() * Math.PI / 180);
        double cosNegPitch = Math.cos(-1 * mob.getEyeLocation().getPitch() * Math.PI / 180);
        double sinNegPitch = Math.sin(-1 * mob.getEyeLocation().getPitch() * Math.PI / 180);
        
        Vector newX = mob.getEyeLocation().getDirection().multiply(vx);
        Vector newY = globalY ? new Vector(0, vy, 0) : (new Vector(sinNegPitch * sinYaw, cosNegPitch, sinNegPitch * -1 * cosYaw)).multiply(vy);
        Vector newZ = (new Vector(cosYaw, 0, sinYaw)).multiply(vz);

        switch(mode) {
            case "ADD":
                mob.setVelocity(mob.getVelocity().add(newX).add(newY).add(newZ));
                break;
            case "SET":
                mob.setVelocity(newX.add(newY).add(newZ));
                break;
            default:
                return "mode must be ADD or SET";
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
