package dev.covector.maplus.mmextension.def;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import dev.covector.maplus.mmextension.Ability;
import dev.covector.maplus.mmextension.MMExtUtils;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.player.cooldown.CooldownMap;

public class MLCooldownReset extends Ability {
    private String syntax = "<target-player> <skill-name-1>,<skill-name-2>,<skill-name-3>,...";
    private String id = "mlCooldown";

    public String cast(String[] args) {
        if (args.length != 2) {
            return "args length must be 2";
        }
        
        Entity target = MMExtUtils.parseUUID(args[0]);
        String[] skills = args[1].split(",");
        
        if (!(target instanceof Player)) {
            return "target must be a player";
        }
        Player targetPlayer = (Player) target;

        CooldownMap cdm = MMOPlayerData.get(targetPlayer.getUniqueId()).getCooldownMap();
        for (String skill : skills) {
            cdm.resetCooldown(skill);
        }
        
        return null;
    }

    public String getSyntax() {
        return syntax;
    }

    public String getId() {
        return id;
    }
}
