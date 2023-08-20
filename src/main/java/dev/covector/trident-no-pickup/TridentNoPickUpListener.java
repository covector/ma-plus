package dev.covector.maplus.tridentnopickup;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.entity.Trident;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.AbstractArrow.PickupStatus;

import com.garbagemule.MobArena.framework.Arena;

import dev.covector.maplus.Utils;

public class TridentNoPickUpListener implements Listener {

    @EventHandler
    public void onTridentPickup(PlayerPickupArrowEvent event) {

        if (!(event.getArrow() instanceof Trident)) {
            return;
        }
        Trident trident = (Trident) event.getArrow();
        
        if (!(trident.getShooter() instanceof Player)) {
            return;
        }

        Player player = (Player) trident.getShooter();
        Arena arena = Utils.getArenaWithPlayer(player);

        if (arena == null || !arena.isRunning()) {
            return;
        }

        if (trident.getItem().getEnchantmentLevel(Enchantment.LOYALTY) == 0) {
            event.setCancelled(true);
        }
    }
}