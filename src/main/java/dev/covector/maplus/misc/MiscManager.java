package dev.covector.maplus.misc;

import org.bukkit.Bukkit;

import dev.covector.maplus.Utils;

public class MiscManager {
    MLSkillCooldownReset skillCooldownReset = new MLSkillCooldownReset();
    TPArenaMobsToSelf tpaMobsToSelf = new TPArenaMobsToSelf();
    UnsocketGems unsocketGems = new UnsocketGems();

    public void register() {
        Utils.getPlugin().getCommand("mlcooldown").setExecutor(skillCooldownReset);
        Utils.getPlugin().getCommand("tparenamobstoself").setExecutor(tpaMobsToSelf);
        Utils.getPlugin().getCommand("unsocketgems").setExecutor(unsocketGems);
        Bukkit.getPluginManager().registerEvents(InventorySwapManager.getInstance(), Utils.getPlugin());
    }

    public void unregister() {
        Utils.getPlugin().getCommand("mlcooldown").setExecutor(null);
        Utils.getPlugin().getCommand("tparenamobstoself").setExecutor(null);
        Utils.getPlugin().getCommand("unsocketgems").setExecutor(null);
        InventorySwapManager.getInstance().unregister();
    }
}
