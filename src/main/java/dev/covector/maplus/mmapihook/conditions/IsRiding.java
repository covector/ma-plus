package dev.covector.maplus.mmapihook.conditions;

import java.util.UUID;

import org.bukkit.entity.Entity;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.conditions.IEntityComparisonCondition;

public class IsRiding implements IEntityComparisonCondition {

	public IsRiding(MythicLineConfig config) {
	    
	}

	@Override
	public boolean check(AbstractEntity entity, AbstractEntity target) {
		UUID entityUUID = entity.getBukkitEntity().getUniqueId();
		for (Entity passenger : target.getBukkitEntity().getPassengers()) {
			if (passenger.getUniqueId().equals(entityUUID)) return true;
		}
        return false;
    }
}