package dev.covector.maplus.mmapihook.mechanics;

import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.INoTargetSkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;

// removealltempblocks

public class RemoveAllTempBlock
implements INoTargetSkill
{
    public RemoveAllTempBlock(MythicLineConfig mlc) {
    }

    @Override
    public SkillResult cast(SkillMetadata data) {
        TempBlockManager.removeAllBlocks();
        return SkillResult.SUCCESS;
    }
}
