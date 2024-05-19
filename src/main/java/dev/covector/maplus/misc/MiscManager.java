package dev.covector.maplus.misc;

import dev.covector.maplus.Utils;

public class MiscManager {
    MLSkillCooldownReset skillCooldownReset = new MLSkillCooldownReset();
    TPArenaMobsToSelf tpaMobsToSelf = new TPArenaMobsToSelf();
    UnsocketGems unsocketGems = new UnsocketGems();

    public void register() {
        Utils.getPlugin().getCommand("mlcooldown").setExecutor(skillCooldownReset);
        Utils.getPlugin().getCommand("tparenamobstoself").setExecutor(tpaMobsToSelf);
        Utils.getPlugin().getCommand("unsocketgems").setExecutor(unsocketGems);
    }

    public void unregister() {
        Utils.getPlugin().getCommand("mlcooldown").setExecutor(null);
        Utils.getPlugin().getCommand("tparenamobstoself").setExecutor(null);
        Utils.getPlugin().getCommand("unsocketgems").setExecutor(null);
    }
}
