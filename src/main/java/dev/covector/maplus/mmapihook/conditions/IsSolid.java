package dev.covector.maplus.mmapihook.conditions;

import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.conditions.ILocationCondition;
import io.lumine.mythic.bukkit.BukkitAdapter;

public class IsSolid implements ILocationCondition  {

	public IsSolid(MythicLineConfig config) {
	}

@Override
    public boolean check(AbstractLocation location) {
        return BukkitAdapter.adapt(location).getBlock().getType().isSolid();
    }
}