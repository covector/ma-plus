package dev.covector.maplus;

import org.bukkit.Bukkit;
import com.garbagemule.MobArena.MobArena;
import com.garbagemule.MobArena.framework.Arena;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;

public class Utils {
    private static MobArena mobarena;
    private static MobArenaPlusPlugin plugin;

    public static void setMobArena(MobArena mobarena) {
        Utils.mobarena = mobarena;
    }

    public static void setPlugin(MobArenaPlusPlugin plugin) {
        Utils.plugin = plugin;
    }

    public static Arena getArena(String name) {
        return mobarena.getArenaMaster().getArenaWithName(name);
    }

    public static Arena getFirstActiveArena() {
        return mobarena.getArenaMaster().getArenas().stream().filter(arena -> arena.getPlayersInArena().size() > 0).findFirst().orElse(null);
    }

    public static Arena getArenaWithPlayer(Player player) {
        return mobarena.getArenaMaster().getArenaWithPlayer(player);
    }

    public static Arena getArenaWithMonster(Entity entity) {
        return mobarena.getArenaMaster().getArenaWithMonster(entity);
    }

    public static MobArenaPlusPlugin getPlugin() {
        return plugin;
    }
}