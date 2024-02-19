package dev.covector.maplus.mmextension.def;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import dev.covector.maplus.mmextension.Ability;
import dev.covector.maplus.mmextension.MMExtUtils;
import net.Indyuce.mmocore.api.player.PlayerData;

import java.util.UUID;

public class ResetMana extends Ability {
    private String syntax = "<player-uuid> [<mana> <ADD|SET>]?";
    private String id = "resetMana";

    public String cast(String[] args) {
        if (args.length != 1 && args.length != 3) {
            return "args length must be 1 or 3";
        }

        String playerUUID = args[0];
        Entity entity = MMExtUtils.parseUUID(playerUUID);
        if (!(entity instanceof Player)) {
            return "entity must be a player";
        }
        
        PlayerData playerData = PlayerData.get(entity.getUniqueId());
        
        double mana = args.length == 2 ? Double.parseDouble(args[1]) : playerData.getStats().getStat("MAX_MANA");
        
        if (args.length == 3) {
            String mode = args[2].toUpperCase();
            switch(mode) {
                case "ADD":
                    mana += playerData.getMana();
                    break;
                case "SET":
                    break;
                default:
                    return "mode must be ADD or SET";
            }
        }
        
        playerData.setMana(mana);

        return null;
    }

    public String getSyntax() {
        return syntax;
    }

    public String getId() {
        return id;
    }
}
