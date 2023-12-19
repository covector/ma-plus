package dev.covector.maplus.misc;

import dev.covector.maplus.Utils;
import dev.covector.maplus.misc.MLSkillCooldownReset;

public class MiscManager {
    MLSkillCooldownReset skillCooldownReset = new MLSkillCooldownReset();

    public void register() {
        Utils.getPlugin().getCommand("mlcooldown").setExecutor(skillCooldownReset);
    }

    public void unregister() {
        Utils.getPlugin().getCommand("mlcooldown").setExecutor(null);
    }
}
