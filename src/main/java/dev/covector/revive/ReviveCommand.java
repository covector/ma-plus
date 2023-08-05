package dev.covector.maplus.revive;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.garbagemule.MobArena.framework.Arena;

import dev.covector.maplus.Utils;

public class ReviveCommand implements CommandExecutor {
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

}