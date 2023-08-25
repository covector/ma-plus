package dev.covector.maplus.leaderboardclear;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.entity.Player;

import com.garbagemule.MobArena.framework.Arena;
import com.garbagemule.MobArena.events.ArenaStartEvent;

import us.ajg0702.leaderboards.LeaderboardPlugin;

public class CLBListener implements Listener {
    private LeaderboardPlugin lbPlugin;

    public CLBListener(LeaderboardPlugin lbPlugin) {
        this.lbPlugin = lbPlugin;
    }

    public void clearLeaderboards() {
        String[] removingBoards = {"mobarena_player_kills", "mobarena_player_damage-done"};
        for (String removingBoard : removingBoards) {
            if(!lbPlugin.getCache().removeBoard(removingBoard)) {
                Bukkit.broadcastMessage("&cSomething went wrong while resetting " + removingBoard + ". Check the console for more info.");
                return;
            }
            if(!lbPlugin.getCache().createBoard(removingBoard)) {
                Bukkit.broadcastMessage("&cSomething went wrong while resetting " + removingBoard + ". Check the console for more info.");
                return;
            }
            // Bukkit.broadcastMessage("&aThe board &f" + removingBoard + "&a has been reset!");
        }
    }

    public void updateScoreboard(Arena arena) {
        String arenaName = arena.configName();
        for (Player player : arena.getAllPlayers()) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "simplescore force " + player.getName() + " " + arenaName);
        }
    }

    @EventHandler
    public void onArenaStart(ArenaStartEvent event) {
        clearLeaderboards();
        updateScoreboard(event.getArena());
    }

    public void unregister() {
        ArenaStartEvent.getHandlerList().unregister(this);
    }
}