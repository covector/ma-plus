package dev.covector.maplus.mmextension;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import net.Indyuce.mmocore.api.player.PlayerData;

import java.util.UUID;

public class ResetStamina extends Ability {
    private String syntax = "<player-uuid> [<stamina> <ADD|SET>]?";
    private String id = "resetStamina";

    public String cast(String[] args) {
        if (args.length != 1 || args.length != 3) {
            return "args length must be 1 or 3";
        }

        String playerUUID = args[0];
        Entity entity = MMExtUtils.parseUUID(playerUUID);
        if (!(entity instanceof Player)) {
            return "entity must be a player";
        }
        
        PlayerData playerData = PlayerData.get(UUID.fromString(playerUUID));
        
        double stamina = args.length == 2 ? Double.parseDouble(args[1]) : playerData.getStats().getStat("MAX_STAMINA");
        
        if (args.length == 3) {
            String mode = args[2].toUpperCase();
            switch(mode) {
                case "ADD":
                    stamina += playerData.getStamina();
                    break;
                case "SET":
                    break;
                default:
                    return "mode must be ADD or SET";
            }
        }
        
        playerData.setStamina(stamina);

        return null;
    }

    public String getSyntax() {
        return syntax;
    }

    public String getId() {
        return id;
    }
}
