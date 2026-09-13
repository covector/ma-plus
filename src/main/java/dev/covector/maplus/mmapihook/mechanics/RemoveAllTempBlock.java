package dev.covector.maplus.mmapihook.mechanics;

import java.io.File;

import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.INoTargetSkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.ThreadSafetyLevel;
import io.lumine.mythic.core.skills.SkillExecutor;
import io.lumine.mythic.core.skills.SkillMechanic;

// removealltempblocks

public class RemoveAllTempBlock
extends SkillMechanic
implements INoTargetSkill
{
    public RemoveAllTempBlock(SkillExecutor manager, File file, String skill, MythicLineConfig mlc) {
        super(manager, file, skill, mlc);
        this.threadSafetyLevel = ThreadSafetyLevel.SYNC_ONLY;
    }

    @Override
    public SkillResult cast(SkillMetadata data) {
        TempBlockManager.removeAllBlocks();
        return SkillResult.SUCCESS;
    }
}
