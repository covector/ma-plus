package dev.covector.maplus.mmapihook.mechanics;

import io.lumine.mythic.api.adapters.AbstractBossBar;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.INoTargetSkill;
import io.lumine.mythic.api.skills.SkillCaster;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderDouble;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import io.lumine.mythic.core.utils.annotations.MythicField;

public class BarCreateUni
implements INoTargetSkill
{
    @MythicField(name="name", aliases={"n"}, description="The name of the bossbar", defValue="infobar")
    protected String barName;
    @MythicField(name="display", aliases={"d"}, description="The text displayed on the bar", defValue="<caster.name>")
    protected PlaceholderString barDisplay;
    @MythicField(name="value", aliases={"v"}, description="How filled the bossbar is. Must be between 0.0 and 1.0", defValue="1.0")
    protected PlaceholderDouble value;
    @MythicField(name="color", aliases={"c"}, description="The color of the bossbar", defValue="RED")
    protected AbstractBossBar.BarColor barTimerColor;
    @MythicField(name="style", aliases={"s"}, description="The style of the bossbar", defValue="SOLID")
    protected AbstractBossBar.BarStyle barTimerStyle;

    public BarCreateUni(MythicLineConfig mlc) {
        this.barName = mlc.getString(new String[]{"name", "n"}, "infobar", new String[0]);
        this.barDisplay = mlc.getPlaceholderString(new String[]{"display", "d", "bartimerdisplay", "bartimertext"}, "<caster.name>", new String[0]);
        this.value = mlc.getPlaceholderDouble(new String[]{"value", "v"}, 1.0, new String[0]);
        String barTimerColor = mlc.getString(new String[]{"color", "c", "bartimercolor"}, "RED", new String[0]);
        String barTimerStyle = mlc.getString(new String[]{"style", "s", "bartimerstyle"}, "SOLID", new String[0]);
        try {
            this.barTimerColor = AbstractBossBar.BarColor.valueOf(barTimerColor);
        } catch (Exception ex) {
            this.barTimerColor = AbstractBossBar.BarColor.RED;
        }
        try {
            this.barTimerStyle = AbstractBossBar.BarStyle.valueOf(barTimerStyle);
        } catch (Exception ex) {
            this.barTimerStyle = AbstractBossBar.BarStyle.SOLID;
        }
    }

    @Override
    public SkillResult cast(SkillMetadata data) {
        SkillCaster skillCaster = data.getCaster();
        if (!(skillCaster instanceof ActiveMob)) {
            return SkillResult.INVALID_TARGET;
        }
        ActiveMob am = (ActiveMob)skillCaster;
        AbstractBossBar bar = ((MythicBukkit)this.getPlugin()).getBootstrap().createBossBar(this.barDisplay.get(data), this.barTimerColor, this.barTimerStyle);
        if (this.barDisplay.get(data).startsWith("\\u")) {
            bar.setTitle(Character.toString(Integer.parseInt(this.barDisplay.get(data).substring(2), 16)));
        }
        bar.setProgress(this.value.get(data));
        am.addBar(this.barName, bar);
        return SkillResult.SUCCESS;
    }
}
