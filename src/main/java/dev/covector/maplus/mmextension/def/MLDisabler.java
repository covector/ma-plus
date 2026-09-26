package dev.covector.maplus.mmextension.def;

import java.util.Collections;
import java.util.List;
import java.util.Arrays;
import java.util.HashSet;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import dev.covector.maplus.mmextension.Ability;
import dev.covector.maplus.mmextension.MMExtUtils;
import dev.covector.maplus.Utils;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.player.cooldown.CooldownMap;
import io.lumine.mythic.lib.api.event.skill.PlayerCastSkillEvent;

public class MLDisabler extends Ability implements AutoCloseable, Listener {
    private String syntax = "<target-player> <true|false>?";
    private String id = "mlDisabler";
    private HashSet<String> disabledPlayers = new HashSet<String>();

    public MLDisabler() {
        Bukkit.getPluginManager().registerEvents(this, Utils.getPlugin());
    }

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
        boolean toggle = args.length == 2 ? Boolean.parseBoolean(args[1]) : !disabledPlayers.contains(targetUUID);
        if (toggle) {
            disabledPlayers.add(targetUUID);
        } else {
            disabledPlayers.remove(targetUUID);
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
            return MMExtUtils.getLivingEntityTabComplete(argsList[0]);
        } else if (argsList.length == 2) {
            return Arrays.asList("true", "false");
        }
        
        return super.getTabComplete(sender, argsList);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void cancelSKill(PlayerCastSkillEvent event) {
        if (disabledPlayers.contains(event.getPlayer().getUniqueId().toString())) {
            event.setCancelled(true);
            return;
        }
    }

    @Override
    public void close() {
        PlayerCastSkillEvent.getHandlerList().unregister(this);
    }
}
