package dev.covector.maplus.mmextension;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.bukkit.Location;

import java.util.Random;

public class RaycastDirection extends Raycast {
    private String syntax = "relative:<boolean> offsetYaw:<float> offsetPitch:<float> inaccuracy:<float>";
    private String id = "raycastDirection";
    private static Random random = new Random();

    public RaycastDirection() {
        super();
        setDefault("relative", "true");
        setDefault("offsetYaw", "0");
        setDefault("offsetPitch", "0");
        setDefault("inaccuracy", "0");
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
        float yawOffset = 0;
        float pitchOffset = 0;
        float inaccuracy = getFloat(parsedParam, "inaccuracy");
        if (inaccuracy > 0) {
            yawOffset = random.nextFloat() * inaccuracy;
            pitchOffset = random.nextFloat() * inaccuracy;
        }
        loc.setYaw(Location.normalizeYaw(casterMob.getEyeLocation().getYaw() + getFloat(parsedParam, "offsetYaw")) + yawOffset);
        loc.setPitch(Location.normalizePitch(casterMob.getEyeLocation().getPitch() + getFloat(parsedParam, "offsetPitch")) + pitchOffset);

        return raycast(parsedParam, loc.getDirection());
    }

    public String getSyntax() {
        return syntax + " " + super.getSyntax();
    }

    public String getId() {
        return id;
    }
}
