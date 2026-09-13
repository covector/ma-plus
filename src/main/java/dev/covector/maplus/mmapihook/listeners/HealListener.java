package dev.covector.maplus.mmapihook.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.potion.PotionEffectType;

import dev.covector.maplus.Utils;
import io.lumine.mythic.bukkit.events.MythicHealMechanicEvent;

public class HealListener implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onMythicHeal(MythicHealMechanicEvent event) {
        Bukkit.getServer().getPluginManager().callEvent(new EntityRegainHealthEvent(event.getTarget(), event.getHealAmount(), RegainReason.CUSTOM));
    }

    @EventHandler(ignoreCancelled = true)
    public void onAbsorption(EntityPotionEffectEvent event) {
        if (
            event.getEntity() instanceof Player &&
            event.getNewEffect() != null &&
            event.getNewEffect().getType().equals(PotionEffectType.ABSORPTION)
        )
            Bukkit.getServer().getPluginManager().callEvent(new EntityRegainHealthEvent(event.getEntity(), (event.getNewEffect().getAmplifier() + 1.0) * 4.0, RegainReason.CUSTOM));
    }

    public void register() {
        Bukkit.getPluginManager().registerEvents(this, Utils.getPlugin());
    }

    public void unregister() {
        MythicHealMechanicEvent.getHandlerList().unregister(this);
        EntityPotionEffectEvent.getHandlerList().unregister(this);
    }
}
