package dev.covector.maplus.mmextension.def;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import dev.covector.maplus.Utils;
import dev.covector.maplus.mmextension.Ability;
import dev.covector.maplus.mmextension.MMExtUtils;

public class EffectCleanse extends Ability {
    private String syntax = "<target-uuid> <last-duration-tick>?";
    private String id = "effectCleanse";

    private static final PotionEffectType[] badEffects = {
            PotionEffectType.BLINDNESS,
            PotionEffectType.CONFUSION,
            PotionEffectType.DARKNESS,
            PotionEffectType.HUNGER,
            PotionEffectType.POISON,
            PotionEffectType.SLOW,
            PotionEffectType.SLOW_DIGGING,
            PotionEffectType.WEAKNESS,
            PotionEffectType.WITHER
    };

    private static void cleanseEffect(LivingEntity entity) {
        for (PotionEffectType type : badEffects) {
            entity.removePotionEffect(type);
        }
    }

    private static boolean isBadEffect(PotionEffectType type) {
        for (PotionEffectType badType : badEffects) {
            if (type == badType) {
                return true;
            }
        }
        return false;
    }

    private class CleanseContinuous extends BukkitRunnable implements Listener {
        private LivingEntity entity;

        public CleanseContinuous(LivingEntity entity) {
            this.entity = entity;
        }

        @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
        public void a(EntityPotionEffectEvent e) {
            if (
                e.getEntity() == entity &&
                e.getAction() == EntityPotionEffectEvent.Action.ADDED &&
                isBadEffect(e.getNewEffect().getType())
            ) {
                e.setCancelled(true);
            }
        }

        public void run() {
            EntityPotionEffectEvent.getHandlerList().unregister(this);
            cancel();
        }
    }

    public String cast(String[] args) {
        if (args.length != 2 && args.length != 1) {
            return "args length must be 1 or 2";
        }

        Entity target = MMExtUtils.parseUUID(args[0]);

        if (!(target instanceof LivingEntity)) {
            return "target must be a living entity";
        }

        LivingEntity targetMob = (LivingEntity) target;
        
        cleanseEffect(targetMob);

        if (args.length == 2) {
            int duration = Integer.parseInt(args[1]);
            new CleanseContinuous(targetMob).runTaskLater(Utils.getPlugin(), duration);
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
