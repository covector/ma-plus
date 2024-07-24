package dev.covector.maplus.mmapihook.conditions;

import java.util.UUID;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.conditions.IEntityComparisonCondition;
import io.lumine.mythic.api.skills.conditions.IEntityCondition;
import io.lumine.mythic.api.skills.conditions.ISkillMetaComparisonCondition;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;

public class IsChildFix implements IEntityComparisonCondition {

	public IsChildFix(MythicLineConfig config) {
	    
	}

	@Override
	public boolean check(AbstractEntity entity, AbstractEntity target) {
		if (MythicBukkit.inst().getMobManager().isActiveMob(target)) {
			ActiveMob am = MythicBukkit.inst().getMobManager().getMythicMobInstance(target);
			UUID playerUUID = am.getOwner().isPresent() ? am.getOwner().get() : null;
			if (entity.getUniqueId().equals(playerUUID)) return true;
		}
        return false;
    }
}