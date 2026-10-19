package dev.covector.maplus.mmextension.def;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import dev.covector.maplus.misc.InventorySwapManager;
import dev.covector.maplus.mmextension.Ability;
import dev.covector.maplus.mmextension.MMExtUtils;

public class ShuffleInventory extends Ability {
    private String syntax = "add <target-player> OR reset";
    private String id = "shuffleInventory";

    public List<Player> shuffledPlayers = new ArrayList<Player>();

    public String cast(String[] args) {
        if (args.length != 1 && args.length != 2) {
            return "args length must be 1 or 2";
        }

        // reset all swapping
        if (args[0].equals("reset")) {
            shuffledPlayers.clear();
            InventorySwapManager.getInstance().reset();
            return null;
        } else if (!args[0].equals("add")) {
            return "args[0] must be add or reset";
        } else if (args.length != 2) {
            return "args length must be 2";
        }
        
        // get player for shuffling
        Entity entity = MMExtUtils.parseUUID(args[1]);
        if (!(entity instanceof Player)) {
            return "target player must be a player";
        }
        Player player = (Player) entity;

        // add to shuffle list
        if (shuffledPlayers.contains(player)) {
            return "player is already shuffled";
        }
        shuffledPlayers.add(player);
        // ... and shuffle
        InventorySwapManager.getInstance().shuffle(shuffledPlayers);

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
            return List.of("reset", "add");
        } else if (argsList.length == 2 && argsList[0].equals("add")) {
            return MMExtUtils.getLivingEntityTabComplete(argsList[argsList.length-1]);
        }
        
        return super.getTabComplete(sender, argsList);
    }
}
