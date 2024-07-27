package dev.covector.maplus.mmapihook.mechanics;

import java.lang.reflect.Field;

import org.bukkit.Bukkit;

import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.INoTargetSkill;
import io.lumine.mythic.api.skills.SkillCaster;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import io.lumine.mythic.core.mobs.ActiveMob;
import io.lumine.mythic.core.mobs.MobType;
public class FixBarUnicode
implements INoTargetSkill
{
    private Field activeMobMobType;
    private Field mobTypeBossBarTitle;

    public FixBarUnicode(MythicLineConfig mlc) {
        try {
            this.activeMobMobType = ActiveMob.class.getDeclaredField("type");
            this.activeMobMobType.setAccessible(true);
            this.mobTypeBossBarTitle = MobType.class.getDeclaredField("bossBarTitle");
            this.mobTypeBossBarTitle.setAccessible(true);
        } catch (NoSuchFieldException e) {
            Bukkit.broadcastMessage("Error: NoSuchFieldException");
        }
    }

    private void setBossBarTitle(ActiveMob am, String title) {
        try {
            this.mobTypeBossBarTitle.set((MobType)this.activeMobMobType.get(am), PlaceholderString.of(title));
        } catch (IllegalAccessException e) {
            Bukkit.broadcastMessage("Error: IllegalAccessException");
        }
    }

    private String getBossBarTitle(ActiveMob am) {
        try {
            return ((PlaceholderString)this.mobTypeBossBarTitle.get(this.activeMobMobType.get(am))).toString();
        } catch (IllegalAccessException e) {
            Bukkit.broadcastMessage("Error: IllegalAccessException");
            return null;
        }
    }

    @Override
    public SkillResult cast(SkillMetadata data) {
        SkillCaster skillCaster = data.getCaster();
        if (!(skillCaster instanceof ActiveMob)) {
            return SkillResult.INVALID_TARGET;
        }
        ActiveMob am = (ActiveMob)skillCaster;
        if (this.getBossBarTitle(am) == null) {
            return SkillResult.CONDITION_FAILED;
        }
        String title = am.getType().getBossBarTitle().toString();
        if (title.startsWith("\\u")) {
            this.setBossBarTitle(am, Character.toString(Integer.parseInt(title.substring(2), 16)));
        }
        return SkillResult.SUCCESS;
    }

    public boolean getRunAsync() {
        return false;
    }
}
