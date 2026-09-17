package dev.covector.maplus.revive;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import com.garbagemule.MobArena.framework.Arena;

import dev.covector.maplus.Utils;
import dev.covector.maplus.mmextension.MMExtUtils;

/**
 * /revive random <arena> <success-callback?>
 * /revive player <player> <success-callback?>
 */
public class ReviveCommand implements CommandExecutor, TabCompleter {
    private Reviver reviver;

    public ReviveCommand(Reviver reviver) {
        this.reviver = reviver;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Arena arena;
        switch(args.length) {
            case 2:
            case 3:
                if (args[0].equals("random")) {
                    // random revive
                    arena = Utils.getArena(args[1]);
                    if (arena == null || !arena.isRunning()) {
                        sender.sendMessage(ChatColor.RED + "Active arena not found!");
                        return true;
                    }
                    if (args.length == 2) {
                        reviver.reviveRandomPlayer(arena);
                    } else {
                        reviver.reviveRandomPlayer(arena, args[2]);
                    }
                    return true;
                } else if (args[0].equals("player")) {
                    // revive player
                    Player player = Bukkit.getServer().getPlayer(args[1]);
                    if (player == null) {
                        sender.sendMessage(ChatColor.RED + "Player not found!");
                        return true;
                    }
                    arena = Utils.getArenaWithPlayer(player);
                    if (arena == null || !arena.isRunning()) {
                        sender.sendMessage(ChatColor.RED + "Player not in any active arena!");
                        return true;
                    }
                    if (!reviver.canRevivePlayer(arena, player)) {
                        sender.sendMessage(ChatColor.RED + "Only spectating players can be revived!");
                        return true;
                    }
                    if (args.length == 2) {
                        reviver.revivePlayer(arena, player);
                    } else {
                        reviver.revivePlayer(arena, player, args[2]);
                    }
                    return true;
                }
        }
        return false;      
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        switch (args.length) {
            case 1:
                Stream<String> options = Stream.of("random", "player");
                return MMExtUtils.streamFilter(options, args[0]);
            case 2:
                if (args[0].equals("random")) { 
                    return MMExtUtils.getArenaTabComplete(args[args.length-1]);
                } else if (args[0].equals("player")) {
                    return MMExtUtils.getLivingEntityTabComplete(args[args.length-1]);
                }
                break;
            case 3:
                return List.of("<success-callback-skill>");
        }

        return Collections.emptyList();
    }

}