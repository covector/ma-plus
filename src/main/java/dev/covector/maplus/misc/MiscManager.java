package dev.covector.maplus.misc;

import dev.covector.maplus.Utils;

public class MiscManager {
    MLSkillCooldownReset skillCooldownReset = new MLSkillCooldownReset();
    TPArenaMobsToSelf tpaMobsToSelf = new TPArenaMobsToSelf();

    public void register() {
        Utils.getPlugin().getCommand("mlcooldown").setExecutor(skillCooldownReset);
        Utils.getPlugin().getCommand("tparenamobstoself").setExecutor(tpaMobsToSelf);
    }

    public void unregister() {
        Utils.getPlugin().getCommand("mlcooldown").setExecutor(null);
        Utils.getPlugin().getCommand("tparenamobstoself").setExecutor(null);
    }
}
