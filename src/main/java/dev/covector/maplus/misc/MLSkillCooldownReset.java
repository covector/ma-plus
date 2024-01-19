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

import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.player.cooldown.CooldownInfo;
import io.lumine.mythic.lib.player.cooldown.CooldownMap;

public class MLSkillCooldownReset  implements CommandExecutor {
    private Field coolDownMap;

    public MLSkillCooldownReset() {
        try {
            coolDownMap = MMOPlayerData.class.getDeclaredField("map");
            coolDownMap.setAccessible(true);
        } catch (NoSuchFieldException e) {
            Bukkit.broadcastMessage("Error: NoSuchFieldException");
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "You must be a player to execute this command!");
            return false;
        }   

        Player player = (Player) sender;
        CooldownMap cdm = MMOPlayerData.get(player.getUniqueId()).getCooldownMap();

        try {
            Map<String, CooldownInfo> map = (HashMap<String, CooldownInfo>)coolDownMap.get(cdm);
            map.clear();
        } catch (IllegalAccessException e) {
            Bukkit.broadcastMessage("Error: IllegalAccessException");
            return false;
        }

        sender.sendMessage(ChatColor.GREEN + "Skill cooldowns reset!");

        return true;
    }
}
