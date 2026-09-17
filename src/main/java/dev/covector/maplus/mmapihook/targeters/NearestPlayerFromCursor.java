package dev.covector.maplus.mmapihook.targeters;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.adapters.AbstractPlayer;
import io.lumine.mythic.api.adapters.AbstractVector;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.SkillCaster;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.skills.SkillExecutor;
import io.lumine.mythic.core.skills.targeters.IEntitySelector;
import java.util.Collection;
import java.util.HashSet;

public class NearestPlayerFromCursor extends IEntitySelector {
    private double radius;

    public NearestPlayerFromCursor(SkillExecutor manager, MythicLineConfig mlc) {
        super(manager, mlc);
        this.radius = mlc.getDouble(new String[]{"radius", "r"}, 50.0);
    }

    @Override
    public Collection<AbstractEntity> getEntities(SkillMetadata data) {
        SkillCaster am = data.getCaster();
        HashSet<AbstractEntity> targets = new HashSet<AbstractEntity>();
        if (!(am.getEntity().isPlayer())) {
            return targets;
        }
        AbstractPlayer nearest = null;
        double maxCos = -2.0;
        for (AbstractPlayer p : MythicBukkit.inst().getEntityManager().getPlayers(am.getEntity().getWorld())) {
            if (p.getUniqueId().toString().equals(am.getEntity().getUniqueId().toString()) || !p.getWorld().equals(am.getEntity().getWorld())) continue;
            AbstractVector diff = p.getLocation().subtract(am.getEntity().getLocation()).toVector();
            double length = diff.length();
            if (length == 0.0) {
                nearest = p;
                break;
            }
            double cosSim = am.getEntity().getEyeLocation().getDirection().dot(diff.divide(new AbstractVector(length, length, length)));
            if (cosSim > maxCos && cosSim > 0.0 && length <= this.radius) {
                maxCos = cosSim;
                nearest = p;
            }
        }
        if (nearest != null) {
            targets.add(nearest);
        }
        return targets;
    }
}
