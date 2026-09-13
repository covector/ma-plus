package dev.covector.maplus.mmapihook.conditions;

import org.bukkit.entity.Player;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.conditions.IEntityCondition;

public class IsSneaking implements IEntityCondition {

	public IsSneaking(MythicLineConfig config) {
	}

	@Override
	public boolean check(AbstractEntity entity) {
        if (entity.getBukkitEntity() instanceof Player) {
            Player p = (Player) entity.getBukkitEntity();
            return p.isSneaking();
        }
        return false;
    }
}