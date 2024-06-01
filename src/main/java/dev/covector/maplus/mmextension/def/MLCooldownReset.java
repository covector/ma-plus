package dev.covector.maplus.mmextension.def;

import java.util.Collections;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import dev.covector.maplus.mmextension.Ability;
import dev.covector.maplus.mmextension.MMExtUtils;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.player.cooldown.CooldownMap;

public class MLCooldownReset extends Ability {
    private String syntax = "<target-player> <skill-name-1>,<skill-name-2>,<skill-name-3>,... <success-mm-skill-callback>?";
    private String id = "mlCooldown";

    public String cast(String[] args) {
        if (args.length != 2 && args.length != 3) {
            return "args length must be 2 or 3";
        }
        
        Entity target = MMExtUtils.parseUUID(args[0]);
        String[] skills = args[1].split(",");
        
        if (!(target instanceof Player)) {
            return "target must be a player";
        }
        Player targetPlayer = (Player) target;

        CooldownMap cdm = MMOPlayerData.get(targetPlayer.getUniqueId()).getCooldownMap();
        for (String skill : skills) {
            if (cdm.getCooldown(skill) == 0) {
                continue;
            }
            
            cdm.resetCooldown(skill);
            if (args.length == 3) {
                MMExtUtils.castMMSkill(targetPlayer, args[2], targetPlayer, null);
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
        if (argsList.length == 1 && sender instanceof Player) {
            return MMExtUtils.getLivingEntityTabComplete(argsList[0], (Player) sender);
        }
        
        return super.getTabComplete(sender, argsList);
    }
}
