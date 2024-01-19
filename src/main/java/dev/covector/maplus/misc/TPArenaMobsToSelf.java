package dev.covector.maplus.misc;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.garbagemule.MobArena.framework.Arena;

import dev.covector.maplus.Utils;

public class TPArenaMobsToSelf  implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // syntax: /tparenamobstoself
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "You must be a player to execute this command!");
            return false;
        }

        Player player = (Player) sender;
        Arena arena = Utils.getFirstActiveArena();
        if (arena == null) {
            sender.sendMessage(ChatColor.RED + "No active arena found!");
            return false;
        }

        arena.getMonsterManager().getMonsters().forEach(m -> {
            m.teleport(player.getLocation());
        });
        sender.sendMessage(ChatColor.GREEN + "Teleported all arena mobs to you!");

        return true;
    }
}
