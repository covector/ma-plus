package dev.covector.maplus.mmextension.def;

import java.lang.reflect.Field;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.util.Vector;

import dev.covector.maplus.mmextension.Ability;
import dev.covector.maplus.mmextension.MMExtUtils;

public class TridentReturn extends Ability {
    private String syntax = "<target-player> <can-hit-on-return:(true|false)> <return-speed(only-for-can-hit-on-return-true)>";
    private String id = "tridentReturn";

    public String cast(String[] args) {
        if (args.length != 2 && args.length != 3) {
            return "args length must be 2 or 3";
        }

        Entity target = MMExtUtils.parseUUID(args[0]);
        if (!(target instanceof Player)) {
            return "target must be a player";
        }
        Player player = (Player) target;

        boolean canHitOnReturn = Boolean.parseBoolean(args[1]);
        double returnSpeed = args.length == 3 ? Double.parseDouble(args[2]) : 1;

        resetTrident(player, canHitOnReturn, returnSpeed);

        return null;
    }

    public static void resetTrident(Player player, boolean canHitOnReturn, double returnSpeed) {
        // loop through all trident
        World world = player.getWorld();
        for (Entity entity : world.getEntitiesByClass(Trident.class)) {
            Trident trident = (Trident) entity;

            // check if trident belongs to player
            if (!(trident.getShooter() instanceof Player)) return;
            Player shooter = (Player) trident.getShooter();
                        
            if (!(shooter.getUniqueId().equals(player.getUniqueId()))) return;

            int loyaltyLevel = trident.getItem().getItemMeta().getEnchantLevel(Enchantment.LOYALTY);
            if (trident.isValid() && !trident.isDead() && loyaltyLevel > 0) {
                // cannot hit on return
                if (!canHitOnReturn) {
                    try {
                        // get nms handle of the trident
                        Object nmsTrident = trident.getClass().getMethod("getHandle").invoke(trident);
                        
                        // set "dealtDamage" to true
                        // for 1.20.1 "dealtDamage" is obfuscated as "j"
                        Field dealtDamageField = nmsTrident.getClass().getDeclaredField("j");
                        dealtDamageField.setAccessible(true);
                        dealtDamageField.setBoolean(nmsTrident, true);
                        
                        return;
                    } catch (Exception e) {
                        Bukkit.broadcastMessage("Error: Could not set trident to dealtDamage");
                    }
                }
                
                // can hit on return or fallback

                trident.setGravity(false);

                // point trident back at player's head
                Vector distance = player.getLocation().toVector().add(new Vector(0, 2, 0)).subtract(trident.getLocation().toVector());

                // set velocity to distance * returnSpeed
                trident.setVelocity(distance.multiply(returnSpeed * 0.1D));
            }
        }
    }

    public String getSyntax() {
        return syntax;
    }

    public String getId() {
        return id;
    }

    public List<String> getTabComplete(CommandSender sender, String[] argsList) {
        if (argsList.length == 1) {
            return MMExtUtils.getLivingEntityTabComplete(argsList[argsList.length-1]);
        }
        
        return super.getTabComplete(sender, argsList);
    }
}
