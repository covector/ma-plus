package dev.covector.maplus.mmextension;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.bukkit.Location;

public class RaycastDirection extends Raycast {
    private String syntax = "relative:<boolean> offsetYaw:<float> offsetPitch:<float>";
    private String id = "raycastDirection";

    public RaycastDirection() {
        super();
        setDefault("relative", "true");
        setDefault("offsetYaw", "0");
        setDefault("offsetPitch", "0");
    }

    public String cast(String[] args) {
        if (args.length < 2) {
            return "args length must at least be 2";
        }
        ParsedParam parsedParam;
        try {
            parsedParam = parse(args);
        } catch (Exception e) {
            return e.getMessage();
        }

        Entity caster = getParam(parsedParam, "caster") == null ? null : MMExtUtils.parseUUID(getParam(parsedParam, "caster"));
        if (!(caster instanceof LivingEntity)) {
            return "caster must be a living entity";
        }
        LivingEntity casterMob = (LivingEntity) caster;
        Location loc = getBoolean(parsedParam, "relative") ? casterMob.getEyeLocation().clone() : new Location(casterMob.getWorld(), 0, 0, 0, 0, 0);
        loc.setYaw(Location.normalizeYaw(casterMob.getEyeLocation().getYaw() + getFloat(parsedParam, "offsetYaw")));
        loc.setPitch(Location.normalizePitch(casterMob.getEyeLocation().getPitch() + getFloat(parsedParam, "offsetPitch")));

        return raycast(parsedParam, loc.getDirection());
    }

    public String getSyntax() {
        return syntax + " " + super.getSyntax();
    }

    public String getId() {
        return id;
    }
}
