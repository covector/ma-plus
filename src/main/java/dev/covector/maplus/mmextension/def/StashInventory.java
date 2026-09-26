package dev.covector.maplus.mmextension.def;

import java.util.Collections;
import java.util.List;
import java.util.Arrays;
import java.util.HashMap;
import java.util.ArrayList;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import dev.covector.maplus.mmextension.Ability;
import dev.covector.maplus.mmextension.MMExtUtils;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.player.cooldown.CooldownMap;

public class StashInventory extends Ability {
    private String syntax = "<target-player> <true|false>?";
    private String id = "stashInventory";
    private HashMap<String, List<ItemStack>> stashedInv = new HashMap<String, List<ItemStack>>();

    public String cast(String[] args) {
        if (args.length != 1 && args.length != 2) {
            return "args length must be 1 or 2";
        }
        
        Entity target = MMExtUtils.parseUUID(args[0]);
        if (!(target instanceof Player)) {
            return "target must be a player";
        }
        Player targetPlayer = (Player) target;

        String targetUUID = targetPlayer.getUniqueId().toString();
        boolean toggle = args.length == 2 ? Boolean.parseBoolean(args[1]) : !stashedInv.containsKey(targetUUID);
        
        if (toggle && stashedInv.containsKey(targetUUID)) {
            return "player's inventory has already been stashed";
        } else if (!toggle && !stashedInv.containsKey(targetUUID)) {
            return "player's inventory has not been stashed";
        }

        if (toggle) {
            push(targetPlayer);
        } else {
            pop(targetPlayer);
        }

        return null;
    }

    protected void push(Player player) {
        // store inventory
        List<ItemStack> inv = new ArrayList<ItemStack>();
        for (int i = 0; i <= 35; i++) {
            inv.add(player.getInventory().getItem(i));
        }
        // offhand
        inv.add(player.getInventory().getItemInOffHand());
        stashedInv.put(player.getUniqueId().toString(), inv);

        // clear inventory
        for (int i = 0; i <= 35; i++) {
            player.getInventory().setItem(i, new ItemStack(Material.AIR));
        }
        player.getInventory().setItemInOffHand(new ItemStack(Material.AIR));
    }

    protected void pop(Player player) {
        // restore inventory
        List<ItemStack> inv = stashedInv.get(player.getUniqueId().toString());
        for (int i = 0; i <= 35; i++) {
            player.getInventory().setItem(i, inv.get(i));
        }
        // restore offhand
        player.getInventory().setItemInOffHand(inv.get(inv.size() - 1));
        stashedInv.remove(player.getUniqueId().toString());
    }

    public String getSyntax() {
        return syntax;
    }

    public String getId() {
        return id;
    }

    public List<String> getTabComplete(CommandSender sender, String[] argsList) {
        if (argsList.length == 1) {
            return MMExtUtils.getLivingEntityTabComplete(argsList[0]);
        } else if (argsList.length == 2) {
            return Arrays.asList("true", "false");
        }
        
        return super.getTabComplete(sender, argsList);
    }
}
