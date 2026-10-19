package dev.covector.maplus.autorevive;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.garbagemule.MobArena.framework.Arena;

import dev.covector.maplus.Utils;
import dev.covector.maplus.revive.Reviver;

import com.garbagemule.MobArena.events.ArenaEndEvent;
import com.garbagemule.MobArena.events.ArenaPlayerDeathEvent;
import com.garbagemule.MobArena.events.ArenaPlayerLeaveEvent;
import com.garbagemule.MobArena.events.NewWaveEvent;

public class AutoRevive implements Listener {
    protected HashMap<String, HashMap<String, Integer>> playerDeathWave = new HashMap<>();
    protected HashMap<String, HashMap<String, Integer>> playerDeathCount = new HashMap<>();
    protected HashMap<String, HashMap<String, Double>> playerMaxHealth = new HashMap<>();

    public AutoRevive() {
    }

    protected Reviver getReviver() {
        return Utils.getPlugin().reviver;
    }

    protected int getDeadWaveBeforeRevive() {
        return Utils.getPlugin().getConfig().getInt("auto-revive.dead-wave-before-revive");
    }

    protected double getHealthReduction() {
        return Utils.getPlugin().getConfig().getDouble("auto-revive.punishment-health-reduction");
    }

    protected boolean isEnabled() {
        return Utils.getPlugin().getConfig().getBoolean("auto-revive.enabled");
    }

    protected double getPlayerMaxHealth(Player player) {
        return player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
    }

    protected void setPlayerMaxHealth(Player player, double maxHealth) {
        player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth);
    }

    protected void registerDeath(Arena arena, Player player, int deathWave) {
        String arenaName = arena.configName();
        String playerID = player.getUniqueId().toString();

        // register death wave
        if (!playerDeathWave.containsKey(arenaName)) {
            playerDeathWave.put(arenaName, new HashMap<String, Integer>());
        }
        playerDeathWave.get(arenaName).put(playerID, deathWave);

        // register death count
        if (!playerDeathCount.containsKey(arenaName)) {
            playerDeathCount.put(arenaName, new HashMap<String, Integer>());
        }
        int deathCount = playerDeathCount.get(arenaName).getOrDefault(playerID, 0);
        playerDeathCount.get(arenaName).put(playerID, deathCount + 1);

        // register max health
        if (deathCount == 0) {
            if (!playerMaxHealth.containsKey(arenaName)) {
                playerMaxHealth.put(arenaName, new HashMap<String, Double>());
            }
            playerMaxHealth.get(arenaName).put(playerID, getPlayerMaxHealth(player));
        }
    }

    protected void registerRevive(Arena arena, Player player) {
        String arenaName = arena.configName();
        String playerID = player.getUniqueId().toString();

        // register death wave
        if (!playerDeathWave.containsKey(arenaName)) {
            return;
        }
        playerDeathWave.get(arenaName).remove(playerID);
    }

    protected double getPlayerHealthReduction(Arena arena, Player player) {
        String arenaName = arena.configName();
        String playerID = player.getUniqueId().toString();
        
        if (!playerDeathCount.containsKey(arenaName)) {
            return 0;
        }
        int deathCount = playerDeathCount.get(arenaName).get(playerID);
        double healthReduction = getHealthReduction() * deathCount;
        return healthReduction;
    }

    protected boolean canBeRevived(Arena arena, Player player) {
        return getPlayerHealthReduction(arena, player) < 1;
    }

    @EventHandler
    public void onPlayerDeath(ArenaPlayerDeathEvent event) {
        if (!isEnabled()) {
            return;
        }

        // areana ended
        if (event.wasLastPlayerStanding()) {
            return;
        }

        // save death wave
        Player player = event.getPlayer();
        Arena arena = event.getArena();
        int deathWave = event.getArena().getWaveManager().getWaveNumber();
        registerDeath(arena, player, deathWave);

        // tell player when they will be revived
        int maxWave = arena.getWaveManager().getFinalWave();
        int reviveWave = deathWave + getDeadWaveBeforeRevive();
        if (reviveWave <= maxWave && canBeRevived(arena, player)) {
            player.sendMessage(
                ChatColor.GOLD + "lumine: " + 
                ChatColor.WHITE + "How did you fucking die lmao, fine, just this once, you will be " +
                ChatColor.YELLOW + "revived " + ChatColor.WHITE + 
                "in wave " + 
                ChatColor.LIGHT_PURPLE + String.valueOf(reviveWave));
            
            // tell player how much health they will lose
            double healthReduction = getPlayerHealthReduction(arena, player);
            player.sendMessage(
                ChatColor.GOLD + "lumine: " + 
                ChatColor.WHITE + "However, you will lose " + 
                ChatColor.YELLOW + String.valueOf(healthReduction * 100) + "%" + ChatColor.WHITE + 
                " of your health");
        }
    }

    @EventHandler
    public void onNewWave(NewWaveEvent event) {
        if (!isEnabled()) {
            return;
        }

        // loop through dead players
        String arenaName = event.getArena().configName();
        if (playerDeathWave.containsKey(arenaName)) {
            for (String playerUUIDString : playerDeathWave.get(arenaName).keySet()) {
                // get player
                Entity entity = Bukkit.getEntity(UUID.fromString(playerUUIDString));
                if (!(entity instanceof Player)) {
                    continue;
                }
                Player player = (Player) entity;

                // calculate how long ago the player died
                int deathWave = playerDeathWave.get(arenaName).get(playerUUIDString);
                int currentWave = event.getWaveNumber();
                int waveSinceDeath = currentWave - deathWave;

                // if the player died for longer than the dead-wave-before-revive config, revive
                int deadWaveBeforeRevive = getDeadWaveBeforeRevive();
                if (waveSinceDeath >= deadWaveBeforeRevive && canBeRevived(event.getArena(), player)) {
                    getReviver().revivePlayer(event.getArena(), player);
                    Bukkit.broadcastMessage(player.getName() + " was revived by " + ChatColor.GOLD + "lumine" + ChatColor.WHITE + "!");
                    player.sendTitle(
                        ChatColor.GREEN + "REVIVED BY " + ChatColor.GOLD + "LUMINE",
                        ChatColor.RED + "REJOICE",
                        10, 70, 20
                    );
                    player.playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 1.0f, 1.0f);
                    registerRevive(event.getArena(), player);

                    // give punishment
                    double healthReduction = getPlayerHealthReduction(event.getArena(), player);
                    double originalMaxHealth = playerMaxHealth.get(arenaName).getOrDefault(playerUUIDString, getPlayerMaxHealth(player));
                    setPlayerMaxHealth(player, originalMaxHealth * (1 - healthReduction));
                }
            }
        }
    }

    @EventHandler
    public void onArenaEnd(ArenaEndEvent event) {
        String arenaName = event.getArena().configName();

        // clear on arena end
        playerDeathWave.remove(arenaName);
        playerDeathCount.remove(arenaName);

        // restore max health
        if (playerMaxHealth.containsKey(arenaName)) {
            for (String playerUUIDString : playerMaxHealth.get(arenaName).keySet()) {
                // get player
                Entity entity = Bukkit.getEntity(UUID.fromString(playerUUIDString));
                if (!(entity instanceof Player)) {
                    continue;
                }
                Player player = (Player) entity;

                // restore max health
                setPlayerMaxHealth(player, 20D);
            }
        }
        playerMaxHealth.remove(arenaName);
    }

    public void onPlayerLeave(ArenaPlayerLeaveEvent event) {
        String arenaName = event.getArena().configName();
        String playerID = event.getPlayer().getUniqueId().toString();

        // restore max health
        if (playerMaxHealth.containsKey(arenaName)) {
            setPlayerMaxHealth(event.getPlayer(), 20D);
            playerMaxHealth.get(arenaName).remove(playerID);
        }
    }

    public void unregister() {
        ArenaEndEvent.getHandlerList().unregister(this);
        NewWaveEvent.getHandlerList().unregister(this);
        ArenaPlayerDeathEvent.getHandlerList().unregister(this);
    }
}