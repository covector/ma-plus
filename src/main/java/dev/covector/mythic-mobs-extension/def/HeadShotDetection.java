package dev.covector.maplus.mmextension;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Slime;
import org.bukkit.entity.MagmaCube;

import java.util.HashMap;

public class HeadShotDetection {
    private static final double defaultMargin = 0.6;
    private static final HashMap<EntityType, Double> hitBoxWidth;
    static {
        hitBoxWidth = new HashMap<>();
        hitBoxWidth.put(EntityType.RABBIT, 0.4);
        hitBoxWidth.put(EntityType.CHICKEN, 0.4);
        hitBoxWidth.put(EntityType.PIG, 0.9);
        hitBoxWidth.put(EntityType.SHEEP, 0.9);
        hitBoxWidth.put(EntityType.COW, 0.9);
        hitBoxWidth.put(EntityType.RAVAGER, 1.95);
        hitBoxWidth.put(EntityType.LLAMA, 0.9);
        hitBoxWidth.put(EntityType.ENDERMITE, 0.4);
        hitBoxWidth.put(EntityType.SILVERFISH, 0.4);
        hitBoxWidth.put(EntityType.CAVE_SPIDER, 0.7);
        hitBoxWidth.put(EntityType.PHANTOM, 0.8);
        hitBoxWidth.put(EntityType.VEX, 0.4);
        hitBoxWidth.put(EntityType.GUARDIAN, 0.85);
        hitBoxWidth.put(EntityType.SPIDER, 1.4);
        hitBoxWidth.put(EntityType.GHAST, 4.0);
        hitBoxWidth.put(EntityType.WITHER_SKELETON, 0.7);
        hitBoxWidth.put(EntityType.IRON_GOLEM, 1.4);
        hitBoxWidth.put(EntityType.BEE, 0.7);
        hitBoxWidth.put(EntityType.STRIDER, 0.9);
        hitBoxWidth.put(EntityType.WARDEN, 0.9);
        hitBoxWidth.put(EntityType.ZOGLIN, 1.4);
        hitBoxWidth.put(EntityType.HOGLIN, 1.4);
    }
    private static final HashMap<EntityType, Double> babyMargin;
    static {
        babyMargin = new HashMap<>();
        babyMargin.put(EntityType.ZOMBIE, 0.3);
        babyMargin.put(EntityType.HUSK, 0.3);
        babyMargin.put(EntityType.DROWNED, 0.3);
        babyMargin.put(EntityType.ZOMBIFIED_PIGLIN, 0.3);
        babyMargin.put(EntityType.PIGLIN, 0.3);
        babyMargin.put(EntityType.PIGLIN_BRUTE, 0.3);
        babyMargin.put(EntityType.ZOGLIN, 0.7);
        babyMargin.put(EntityType.HOGLIN, 0.7);
    }

    public static HeadShotResult isHeadShot(Entity entity, Vector hitLocation, double extraMargin) {
        if (!(entity instanceof LivingEntity)) {
            return null;
        }
        LivingEntity livingEntity = (LivingEntity) entity;

        // Crawling scheme
        if (
            entity.getType() == EntityType.RAVAGER ||
            entity.getType() == EntityType.SPIDER ||
            entity.getType() == EntityType.CAVE_SPIDER ||
            entity.getType() == EntityType.PHANTOM ||
            entity.getType() == EntityType.VEX ||
            entity.getType() == EntityType.GUARDIAN ||
            entity.getType() == EntityType.GHAST ||
            entity.getType() == EntityType.RABBIT ||
            entity.getType() == EntityType.ENDERMITE ||
            entity.getType() == EntityType.SILVERFISH ||
            entity.getType() == EntityType.SHEEP ||
            entity.getType() == EntityType.COW ||
            entity.getType() == EntityType.PIG ||
            entity.getType() == EntityType.CHICKEN ||
            entity.getType() == EntityType.BEE ||
            entity.getType() == EntityType.HOGLIN ||
            entity.getType() == EntityType.ZOGLIN || 
            entity.getType() == EntityType.SLIME ||
            entity.getType() == EntityType.MAGMA_CUBE
        ) {
            return schemeCrawling(livingEntity, hitLocation, extraMargin);
        }

        // Y only scheme as livingEntity
        return schemeYOnly(livingEntity, hitLocation, extraMargin);
    }

    private static HeadShotResult schemeYOnly(LivingEntity entity, Vector hitLocation, double extraMargin) {
        double distance = Math.max(0, Math.abs(entity.getEyeLocation().getY() - hitLocation.getY()) - extraMargin);
        return new HeadShotResult(distance < getMargin(entity) / 2, distance);
    }

    private static HeadShotResult schemeCrawling(LivingEntity entity, Vector hitLocation, double extraMargin) {
        double width = getMargin(entity) / 2;
        Location newHitBox = entity.getEyeLocation().clone().add(entity.getEyeLocation().getDirection().multiply(width));
        double distance = Math.max(newHitBox.toVector().distance(hitLocation) - extraMargin, 0);
        return new HeadShotResult(distance < width, distance);
    }

    private static double getMargin(LivingEntity entity) {
        double width = hitBoxWidth.containsKey(entity.getType()) ? hitBoxWidth.get(entity.getType()) : defaultMargin;
        if (entity instanceof Ageable) {
            Ageable ageable = (Ageable) entity;
            if (!ageable.isAdult()) {
                width = babyMargin.containsKey(entity.getType()) ? babyMargin.get(entity.getType()) : width;
            }
        }
        if (entity instanceof Slime) {
            Slime slime = (Slime) entity;
            width = slime.getSize() * 0.4;
        }
        if (entity instanceof MagmaCube) {
            MagmaCube magmaCube = (MagmaCube) entity;
            width = magmaCube.getSize() * 0.4;
        }
        return width;
    }

    public static class HeadShotResult {
        private final boolean isHeadShot;
        private final double distance;

        public HeadShotResult(boolean isHeadShot, double distance) {
            this.isHeadShot = isHeadShot;
            this.distance = distance;
        }

        public boolean isHeadShot() {
            return isHeadShot;
        }

        public double getDistance() {
            return distance;
        }
    }
}