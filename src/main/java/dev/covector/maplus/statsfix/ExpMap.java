package dev.covector.maplus.statsfix;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Slime;

public class ExpMap {
    private static final HashMap<String, HashSet<EntityType>> mobExpMap;
    private static final Random random = new Random();

    static {
        mobExpMap = new HashMap<>();

        HashSet<EntityType> one2three = new HashSet<>();
        one2three.add(EntityType.AXOLOTL);
        one2three.add(EntityType.CHICKEN);
        one2three.add(EntityType.COW);
        one2three.add(EntityType.PIG);
        one2three.add(EntityType.RABBIT);
        one2three.add(EntityType.SHEEP);
        one2three.add(EntityType.SKELETON_HORSE);
        one2three.add(EntityType.STRIDER);
        one2three.add(EntityType.BEE);
        one2three.add(EntityType.LLAMA);
        one2three.add(EntityType.WOLF);
        mobExpMap.put("1-3", one2three);

        HashSet<EntityType> five = new HashSet<>();
        five.add(EntityType.CAVE_SPIDER);
        five.add(EntityType.ENDERMAN);
        five.add(EntityType.PIGLIN);
        five.add(EntityType.SPIDER);
        five.add(EntityType.ZOMBIFIED_PIGLIN);
        five.add(EntityType.ZOMBIE);
        five.add(EntityType.DROWNED);
        five.add(EntityType.HUSK);
        five.add(EntityType.CREEPER);
        five.add(EntityType.GHAST);
        five.add(EntityType.HOGLIN);
        five.add(EntityType.ILLUSIONER);
        five.add(EntityType.PILLAGER);
        five.add(EntityType.SILVERFISH);
        five.add(EntityType.SKELETON);
        five.add(EntityType.STRAY);
        five.add(EntityType.VEX);
        five.add(EntityType.WITCH);
        five.add(EntityType.ZOGLIN);
        five.add(EntityType.VINDICATOR);
        five.add(EntityType.WARDEN);
        five.add(EntityType.WITHER_SKELETON);
        five.add(EntityType.ZOMBIE_VILLAGER);
        mobExpMap.put("5", five);

        HashSet<EntityType> ten = new HashSet<>();
        ten.add(EntityType.BLAZE);
        ten.add(EntityType.EVOKER);
        ten.add(EntityType.GUARDIAN);
        mobExpMap.put("10", ten);

        HashSet<EntityType> twelve = new HashSet<>();
        twelve.add(EntityType.ZOMBIE);
        twelve.add(EntityType.HUSK);
        twelve.add(EntityType.DROWNED);
        twelve.add(EntityType.ZOMBIFIED_PIGLIN);
        twelve.add(EntityType.PIGLIN);
        twelve.add(EntityType.ZOGLIN);
        twelve.add(EntityType.HOGLIN);
        mobExpMap.put("12", twelve);
    }

    public static int getExp(Entity entity) {
        EntityType entityType = entity.getType();

        // baby mobs
        if (entity instanceof Ageable && !((Ageable) entity).isAdult()) {
            return mobExpMap.get("12").contains(entityType) ? 12 : 0;
        }

        // 1-3
        if (mobExpMap.get("1-3").contains(entityType)) {
            return 1 + random.nextInt(3);
        }
        
        // 5
        if (mobExpMap.get("5").contains(entityType)) {
            return 5;
        }

        // 10
        if (mobExpMap.get("10").contains(entityType)) {
            return 10;
        }

        // endermite
        if (entityType == EntityType.ENDERMITE) {
            return 3;
        }

        // ravager and piglin brute
        if (entityType == EntityType.RAVAGER || entityType == EntityType.PIGLIN_BRUTE) {
            return 20;
        }

        // slime and magma cube
        if (entityType == EntityType.SLIME || entityType == EntityType.MAGMA_CUBE) {
            return ((Slime) entity).getSize();
        }

        return 0;
    }
}
