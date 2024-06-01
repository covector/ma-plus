package dev.covector.maplus.statsfix;

import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.entity.Entity;
import org.bukkit.projectiles.ProjectileSource;

import com.garbagemule.MobArena.ArenaPlayer;
import com.garbagemule.MobArena.framework.Arena;

import dev.covector.maplus.Utils;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;

public class StatsFixListener implements Listener {
    NamespacedKey allyKey = new NamespacedKey(Utils.getMobArena(), "ally-monster");

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDamage(EntityDamageEvent event) {
        EntityDamageByEntityEvent edbe = (event instanceof EntityDamageByEntityEvent) ? (EntityDamageByEntityEvent) event : null;
        Entity damager = null;
        if (edbe != null) {
            damager = edbe.getDamager();
            if (damager instanceof Projectile) {
                ProjectileSource shooter = ((Projectile) damager).getShooter();
                if (shooter instanceof Entity) {
                    damager = (Entity) shooter;
                }
            }
        }
        Entity damagee = event.getEntity();

        if (isFriendlyFire(damager, damagee)) { return; }
        ArenaPlayer ap = resolveOwner(damager);
        if (ap != null) { 
            ap.getStats().add("dmgDone", event.getDamage());
            // Bukkit.broadcastMessage(ap.getPlayer().getDisplayName() + " dealt " + event.getDamage());
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onEntityDeath(EntityDeathEvent event) {
        // fix exp
        if (event.getDroppedExp() == 0) { 
            event.setDroppedExp(ExpMap.getExp(event.getEntity()));
        }


        EntityDamageEvent e1 = event.getEntity().getLastDamageCause();
        EntityDamageByEntityEvent e2 = (e1 instanceof EntityDamageByEntityEvent) ? (EntityDamageByEntityEvent) e1 : null;
        Entity damager = (e2 != null) ? e2.getDamager() : null;
        Entity damagee = event.getEntity();

        if (isFriendlyFire(damager, damagee)) { return; }
        ArenaPlayer ap = resolveOwner(damager);
        if (ap != null) { 
            ap.getStats().inc("kills");
            // Bukkit.broadcastMessage(ap.getPlayer().getDisplayName() + " killed something");
        }
    }

    private boolean isFriendlyFire(Entity damager, Entity damagee) {
        if (damager == null || damagee == null) { return false; }
        if (!MythicBukkit.inst().getAPIHelper().isMythicMob(damager) || !MythicBukkit.inst().getAPIHelper().isMythicMob(damagee)) { return false; }
        return damager.getPersistentDataContainer().has(allyKey, PersistentDataType.BYTE) && damagee.getPersistentDataContainer().has(allyKey, PersistentDataType.BYTE);
    }

    private ArenaPlayer getClassInArena(String slug) {
        Arena arena = Utils.getFirstActiveArena();
        if (arena == null) { return null; }
        for (Player player : arena.getPlayersInArena()) {
            ArenaPlayer ap = arena.getArenaPlayer(player);
            if (ap.getArenaClass().getConfigName().equals(slug)) {
                return ap;
            }
        }
        return null;
    }

    private ArenaPlayer getMythicOwner(ActiveMob am) {
        Arena arena = Utils.getFirstActiveArena();
        if (arena == null) { return null; }
        UUID playerUUID = am.getOwner().isPresent() ? am.getOwner().get() : null;
        if (playerUUID == null) { return null; }
        return arena.getArenaPlayer(Bukkit.getPlayer(playerUUID));
    }

    // private HashSet<String> emperorMinions = new HashSet<>(Arrays.asList("melee_summon1", "melee_summon2", "melee_summon3", "melee_summon4", "archer_summon1", "creeper_summon", "mage_summon", "mage_summon2"));
    private ArenaPlayer resolveOwner(Entity entity) {
        if (!MythicBukkit.inst().getAPIHelper().isMythicMob(entity)) { return null; }
        ActiveMob am = MythicBukkit.inst().getAPIHelper().getMythicMobInstance(entity);

        // if (am.getMobType().equals("Shadow") ||
        //     emperorMinions.contains(am.getMobType()) ||
        //     am.getMobType().equals("gunner") || am.getMobType().equals("tiger")
        // ) {
        //     return getMythicOwner(am);
        // }

        return getMythicOwner(am);
    }

    public void register() {
        Bukkit.getPluginManager().registerEvents(this, Utils.getPlugin());
    }

    public void unregister() {
        EntityDamageEvent.getHandlerList().unregister(this);
        EntityDeathEvent.getHandlerList().unregister(this);
    }
}
