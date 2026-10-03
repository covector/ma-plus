package dev.covector.maplus.mmextension.def;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;

import dev.covector.maplus.mmextension.Ability;
import dev.covector.maplus.mmextension.MMExtUtils;

public class setHostileTarget extends Ability {
    private String syntax = "<ability-target> <hostile-target>";
    private String id = "setHostileTarget";

    public String cast(String[] args) {
        if (args.length != 1 && args.length != 2) {
            return "args length must be 1 or 2";
        }

        Entity abilityTarget = MMExtUtils.parseUUID(args[0]);
        if (!(abilityTarget instanceof Mob)) {
            return "ability target must be a mob";
        }

        Entity hostileTarget = MMExtUtils.parseUUID(args[1]);
        if (!(abilityTarget instanceof LivingEntity)) {
            return "hostile target must be a living entity";
        }
        
        // set hostile target
        ((Mob) abilityTarget).setTarget((LivingEntity) hostileTarget);

        return null;
    }

    public String getSyntax() {
        return syntax;
    }

    public String getId() {
        return id;
    }

    public List<String> getTabComplete(CommandSender sender, String[] argsList) {
        if ((argsList.length == 1 || argsList.length == 2) && sender instanceof Player) {
            return MMExtUtils.getLivingEntityTabComplete(argsList[argsList.length - 1], (Player) sender);
        }

        return super.getTabComplete(sender, argsList);
    }
}
