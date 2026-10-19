package dev.covector.maplus.mmextension.def;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import dev.covector.maplus.misc.InventorySwapManager;
import dev.covector.maplus.mmextension.Ability;
import dev.covector.maplus.mmextension.MMExtUtils;

public class SwapInventory extends Ability {
    private String syntax = "<player1> <player2> OR reset";
    private String id = "swapInventory";

    public String cast(String[] args) {
        if (args.length != 1 && args.length != 2) {
            return "args length must be 1 or 2";
        }

        // reset all swapping
        if (args[0].equals("reset")) {
            InventorySwapManager.getInstance().reset();
            return null;
        }
        
        // get entities for swapping
        Entity entity1 = MMExtUtils.parseUUID(args[0]);
        if (!(entity1 instanceof Player)) {
            return "player1 must be a player";
        }
        Player player1 = (Player) entity1;

        Entity entity2 = MMExtUtils.parseUUID(args[1]);
        if (!(entity2 instanceof Player)) {
            return "player2 must be a player";
        }
        Player player2 = (Player) entity2;

        // swap
        InventorySwapManager.getInstance().swap(player1, player2);

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
            return Stream.concat(
                MMExtUtils.streamFilter(Stream.of("reset"), argsList[argsList.length-1]).stream(),
                MMExtUtils.getLivingEntityTabComplete(argsList[argsList.length-1]).stream())
            .collect(Collectors.toList());
        } else if (argsList.length == 2) {
            return MMExtUtils.getLivingEntityTabComplete(argsList[argsList.length-1]);
        }
        
        return super.getTabComplete(sender, argsList);
    }
}
