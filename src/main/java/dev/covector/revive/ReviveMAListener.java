package dev.covector.maplus.revive;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.garbagemule.MobArena.framework.Arena;
import com.garbagemule.MobArena.events.ArenaEndEvent;
import com.garbagemule.MobArena.events.ArenaPlayerDeathEvent;

public class ReviveMAListener implements Listener {
    private Reviver reviver;

    public ReviveMAListener(Reviver reviver) {
        this.reviver = reviver;
    }

    @EventHandler
    public void onPlayerDeath(ArenaPlayerDeathEvent event) {
        if (event.wasLastPlayerStanding()) {
            return;
        }
        reviver.saveDeathState(event.getArena(), event.getPlayer(), event.getArena().getWaveManager().getWaveNumber());
    }

    @EventHandler
    public void onArenaEnd(ArenaEndEvent event) {
        reviver.clearSavedStates(event.getArena());
    }

    public void unregister() {
        ArenaEndEvent.getHandlerList().unregister(this);
        ArenaPlayerDeathEvent.getHandlerList().unregister(this);
    }
}