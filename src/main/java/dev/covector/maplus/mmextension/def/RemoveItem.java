package dev.covector.maplus.mmextension.def;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import dev.covector.maplus.mmextension.Ability;
import dev.covector.maplus.mmextension.MMExtUtils;

public class RemoveItem extends Ability {
    private String syntax = "<player-target> <item-name> <count?>";
    private String id = "removeItem";

    public String cast(String[] args) {
        if (args.length != 2 && args.length != 3) {
            return "args length must be 2 or 3";
        }

        Entity playerTarget = MMExtUtils.parseUUID(args[0]);
        if (!(playerTarget instanceof Player)) {
            return "player target must be a player";
        }
        Player player = (Player) playerTarget;

        String targetName = args[1];
        int count = 1;
        if (args.length == 3) {
            count = Integer.parseInt(args[2]);
        }

        Inventory inventory = player.getInventory();
    
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            
            // get item name
            if (item == null) continue;
            String displayName = item.getType().name();
            if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                displayName = ChatColor.stripColor(item.getItemMeta().getDisplayName());
            }
            
            // if matches
            if (displayName.equals(targetName)) {
                int currentAmount = item.getAmount();
                
                // remove x amount
                if (currentAmount > count) {
                    item.setAmount(currentAmount - count);
                    break;
                } else {
                    inventory.setItem(i, null);
                    count -= currentAmount;
                }
                
                if (count <= 0) break;
            }
        }

        return null;
    }

    public String getSyntax() {
        return syntax;
    }

    public String getId() {
        return id;
    }

    public List<String> getTabComplete(CommandSender sender, String[] argsList) {
        if (argsList.length == 1) {
            return MMExtUtils.getLivingEntityTabComplete(argsList[argsList.length - 1]);
        }

        return super.getTabComplete(sender, argsList);
    }
}