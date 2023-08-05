package dev.covector.maplus;

import org.bukkit.Bukkit;
import com.garbagemule.MobArena.MobArena;
import com.garbagemule.MobArena.framework.Arena;
import org.bukkit.entity.Player;

public class Utils {
    public static MobArena mobarena;

    public static void setMobArena(MobArena mobarena) {
        Utils.mobarena = mobarena;
    }

    public static Arena getArena(String name) {
        return mobarena.getArenaMaster().getArenaWithName(name);
    }

    public static Arena getArenaWithPlayer(Player player) {
        return mobarena.getArenaMaster().getArenaWithPlayer(player);
    }
}