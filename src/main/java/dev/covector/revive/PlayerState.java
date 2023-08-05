package dev.covector.maplus.revive;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.garbagemule.MobArena.framework.Arena;
import com.garbagemule.MobArena.ArenaClass;
import com.garbagemule.MobArena.waves.WaveManager;
import com.garbagemule.MobArena.waves.Wave;
import com.garbagemule.MobArena.waves.enums.WaveType;
import com.garbagemule.MobArena.waves.types.UpgradeWave;

import java.lang.reflect.Field;
import java.util.TreeSet;

public class PlayerState {
    public ItemStack[] inventory;
    public int exp;
    public int deathWave;
    private static Field singleWaves = null;
    private static int[] majorUpgrades = { 19, 49 };

    public PlayerState(Player player, int deathWave) {
        // cloning player inventory
        this.inventory = new ItemStack[player.getInventory().getSize()];
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item != null) {
                this.inventory[i] = item.clone();
            }
        }
        this.exp = player.getTotalExperience();
        this.deathWave = deathWave;
        if (singleWaves == null) {
            ModifyArenaPlayerAccessibility();
        }
    }

    public void restore(Arena arena, Player player) {
        player.getInventory().setContents(this.inventory);
        player.setTotalExperience(this.exp);
        ArenaClass arenaClass = arena.getArenaPlayer(player).getArenaClass();
        arenaClass.grantPotionEffects(player);
        arenaClass.grantPermissions(player);
        arenaClass.grantLobbyPermissions(player);
        applyMajorUpgrades(arena, player, arenaClass);
    }

    
    private static void ModifyArenaPlayerAccessibility() {
        try {
            singleWaves = WaveManager.class.getDeclaredField("singleWaves");
            singleWaves.setAccessible(true);
        } catch (NoSuchFieldException e) {
            Bukkit.broadcastMessage("Error: NoSuchFieldException");
        }
    }

    public static TreeSet<Wave> getSingleWaves(Arena arena) {
        try {
            return (TreeSet<Wave>)singleWaves.get(arena.getWaveManager());
        } catch (IllegalAccessException e) {
            Bukkit.broadcastMessage("Error: IllegalAccessException");
            return null;
        }
    }

    private void applyMajorUpgrades(Arena arena, Player player, ArenaClass arenaClass) {
        int current = arena.getWaveManager().getWaveNumber();
        for (Wave w : getSingleWaves(arena)) {
            for (int i : majorUpgrades) {
                if (deathWave < i && i <= current && w.getFirstWave() == i) {
                    if (w.getType() == WaveType.UPGRADE) {
                        UpgradeWave uw = (UpgradeWave)w;
                        uw.grantItems(player, arenaClass.getSlug());
                        uw.grantItems(player, "all");
                    }
                }
            }
        }
        
    }
}