package dev.covector.maplus.mmextension.def;

import java.util.List;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import dev.covector.maplus.mlskillsrecorder.MLSkillsRecorder;
import dev.covector.maplus.mmextension.Ability;
import dev.covector.maplus.mmextension.MMExtUtils;

import org.bukkit.command.CommandSender;

public class MLRecast extends Ability {
    private String syntax = "<target-uuid> <except-skill-1>,<except-skill-2>,<except-skill-3>,...";
    private String id = "mlrecast";

    public String cast(String[] args) {
        if (args.length != 1 && args.length != 2) {
            return "args length must be 1 or 2";
        }

        Entity target = MMExtUtils.parseUUID(args[0]);
        
        if (!(target instanceof Player)) {
            return "target must be a player";
        }

        Player player = (Player) target;

        MLSkillsRecorder skillsRecorder = MLSkillsRecorder.GetInstance();
        if (args.length == 2) {
            String[] skills = args[1].split(",");
            for (String skill : skills) {
                skillsRecorder.castLastSkill(player, List.of(skill));
            }
        } else {
            skillsRecorder.castLastSkill(player);
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
            return MMExtUtils.getLivingEntityTabComplete(argsList[argsList.length-1], (Player) sender);
        }
        
        return super.getTabComplete(sender, argsList);
    }
}
