package dev.covector.maplus.mmapihook.mechanics;

import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedLocationSkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.bukkit.BukkitAdapter;

// removetempblock @selflocation

public class RemoveTempBlock
implements ITargetedLocationSkill
{
    public RemoveTempBlock(MythicLineConfig mlc) {
    }

    @Override
    public SkillResult castAtLocation(SkillMetadata data, AbstractLocation target) {
        TempBlockManager.removeBlock(BukkitAdapter.adapt(target));
        return SkillResult.SUCCESS;
    }
}
