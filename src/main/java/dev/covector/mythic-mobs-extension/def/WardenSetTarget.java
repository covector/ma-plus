package dev.covector.maplus.mmextension;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Warden;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;
import org.bukkit.Location;

public class WardenSetTarget extends Ability {
    private String syntax = "<warden-uuid> <target-uuid>";
    private String id = "wardenSetTarget";

    public String cast(String[] args) {
        if (args.length != 2) {
            return "args length must be 2";
        }

        Entity warden = MMExtUtils.parseUUID(args[0]);
        Entity target = MMExtUtils.parseUUID(args[1]);

        if (!(warden instanceof Warden)) {
            return "first uuid must be a warden";
        }

        if (!(target instanceof LivingEntity)) {
            return "target must be a living entity";
        }

        Warden wardenMob = (Warden) warden;
        LivingEntity livingMob = (LivingEntity) target;

        for (Player p : Bukkit.getOnlinePlayers()) {
            wardenMob.clearAnger(p);
        }
        wardenMob.setAnger(livingMob, 150);

        return null;
    }

    public String getSyntax() {
        return syntax;
    }

    public String getId() {
        return id;
    }
}
