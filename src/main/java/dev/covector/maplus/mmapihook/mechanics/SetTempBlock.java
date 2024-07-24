package dev.covector.maplus.mmapihook.mechanics;

import org.bukkit.Material;

import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedLocationSkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.core.logging.MythicLogger;

// settempblock{m=DIRT;rp=LAST;d=0;p=0} @selflocation

public class SetTempBlock
implements ITargetedLocationSkill
 {
    private Material blockType;
    private TempBlockManager.ReplacePolicy replacePolicy;
    private int duration;
    private int priority;

    public SetTempBlock(MythicLineConfig mlc) {
        String bt = mlc.getString(new String[]{"types", "type", "t", "material", "mat", "m"}, "DIRT", new String[0]);
        try {
            this.blockType = Material.valueOf(bt.toUpperCase());
        } catch (Exception ex) {
            MythicLogger.error("'" + bt + "' is not a valid block material.");
        }
        String rp = mlc.getString(new String[]{"replacepolicy", "policy", "rp"}, "LAST", new String[0]);
        try {
            this.replacePolicy = TempBlockManager.ReplacePolicy.valueOf(rp.toUpperCase());
        } catch (Exception ex) {
            MythicLogger.error("'" + rp + "' is not a valid replace policy.");
        }
        this.duration = mlc.getInteger(new String[]{"duration", "d"}, 0);
        this.priority = mlc.getInteger(new String[]{"priority", "p"}, 0);
    }

    @Override
    public SkillResult castAtLocation(SkillMetadata data, AbstractLocation target) {
        TempBlockManager.setBlock(this.blockType, BukkitAdapter.adapt(target), this.replacePolicy, this.duration, this.priority);
        return SkillResult.SUCCESS;
    }
}
