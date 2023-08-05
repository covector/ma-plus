package dev.covector.maplus.revive;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.garbagemule.MobArena.framework.Arena;
import com.garbagemule.MobArena.ArenaImpl;

import java.util.HashMap;
import java.util.Set;
import java.lang.reflect.Field;
import java.util.Random;

import dev.covector.maplus.Utils;

public class Reviver {
    private Field arenaPlayers;
    private Field specPlayers;
    private HashMap<String, HashMap<String, PlayerState>> deathStates = new HashMap<String, HashMap<String, PlayerState>>();
    private Random rand = new Random(); 

    public Reviver() {
        ModifyArenaPlayerAccessibility();
    }

    private void ModifyArenaPlayerAccessibility() {
        try {
            this.arenaPlayers = ArenaImpl.class.getDeclaredField("arenaPlayers");
            this.arenaPlayers.setAccessible(true);
            this.specPlayers = ArenaImpl.class.getDeclaredField("specPlayers");
            this.specPlayers.setAccessible(true);
        } catch (NoSuchFieldException e) {
            Bukkit.broadcastMessage("Error: NoSuchFieldException");
        }
    }

    public Set<Player> getArenaPlayers(Arena arena) {
        try {
            return (Set<Player>)arenaPlayers.get((ArenaImpl) arena);
        } catch (IllegalAccessException e) {
            Bukkit.broadcastMessage("Error: IllegalAccessException");
            return null;
        }
    }

    public Set<Player> getSpecPlayers(Arena arena) {
        try {
            return (Set<Player>)specPlayers.get((ArenaImpl) arena);
        } catch (IllegalAccessException e) {
            Bukkit.broadcastMessage("Error: IllegalAccessException");
            return null;
        }
    }

    public void revivePlayer(Arena arena, Player player) {
        getArenaPlayers(arena).add(player);
        getSpecPlayers(arena).remove(player);
        player.teleport(arena.getRegion().getArenaWarp());
        restoreDeathState(arena, player);
    }

    public void revivePlayer(Player player) {
        Arena arena = Utils.getArenaWithPlayer(player);
        revivePlayer(arena, player);
    }

    public void revivePlayer(Arena arena, Player player, String callbackSkill) {
        revivePlayer(arena, player);

        // chain with mythic lib ability
        player.performCommand("ml cast " + callbackSkill);
    }

    public void reviveRandomPlayer(Arena arena) {
        Set<Player> specPlayers = getSpecPlayers(arena);
        if (specPlayers.size() == 0) {
            return;
        }
        Player player = (Player)specPlayers.toArray()[rand.nextInt(specPlayers.size())];
        revivePlayer(arena, player);
    }

    public void reviveRandomPlayer(Arena arena, String callbackSkill) {
        Set<Player> specPlayers = getSpecPlayers(arena);
        if (specPlayers.size() == 0) {
            return;
        }
        Player player = (Player)specPlayers.toArray()[rand.nextInt(specPlayers.size())];
        revivePlayer(arena, player, callbackSkill);
    }

    public boolean canRevivePlayer(Arena arena, Player player) {
        return getSpecPlayers(arena).contains(player);
    }

    public void saveDeathState(Arena arena, Player player, int deathWave) {
        if (!deathStates.containsKey(arena.configName())) {
            deathStates.put(arena.configName(), new HashMap<String, PlayerState>());
        }
        deathStates.get(arena.configName()).put(player.getUniqueId().toString(), new PlayerState(player, deathWave));
    }

    public void restoreDeathState(Arena arena, Player player) {
        if (!deathStates.containsKey(arena.configName())) {
            return;
        }
        PlayerState state = deathStates.get(arena.configName()).get(player.getUniqueId().toString());
        if (state == null) {
            return;
        }
        state.restore(arena, player);
        deathStates.get(arena.configName()).remove(player.getUniqueId().toString());
    }

    public void clearSavedStates(Arena arena) {
        if (deathStates.containsKey(arena.configName())) {
            deathStates.remove(arena.configName());
        }
    }
}