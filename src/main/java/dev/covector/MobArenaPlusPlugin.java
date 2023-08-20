package dev.covector.maplus;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.Plugin;

import com.garbagemule.MobArena.MobArena;

import us.ajg0702.leaderboards.LeaderboardPlugin;

import dev.covector.maplus.revive.*;
import dev.covector.maplus.leaderboardclear.*;
import dev.covector.maplus.tridentnopickup.*;
import dev.covector.maplus.mmextension.*;

public class MobArenaPlusPlugin extends JavaPlugin
{
    @Override
    public void onEnable() {
        Plugin maplugin = getServer().getPluginManager().getPlugin("MobArena");
        if (maplugin == null) {
            getLogger().warning("MobArena Not Loaded!");
            return;
        }
        MobArena mobarena = (MobArena) maplugin;

        Utils.setMobArena(mobarena);
        Utils.setPlugin(this);

        // REVIVE
        Reviver reviver = new Reviver();
        this.getCommand("marevive").setExecutor(new ReviveCommand(reviver));
        Bukkit.getPluginManager().registerEvents(new ReviveMAListener(reviver), this);

        // LEADERBOARD CLEAR
        Plugin ajlplugin = getServer().getPluginManager().getPlugin("ajLeaderboards");
        if (ajlplugin != null) {
            LeaderboardPlugin lbPlugin = (LeaderboardPlugin) ajlplugin;
            Bukkit.getPluginManager().registerEvents(new CLBListener(lbPlugin), this);
        }

        // TRIDENT NO PICKUP
        Bukkit.getPluginManager().registerEvents(new TridentNoPickUpListener(), this);

        // MYTHIC MOBS EXTENSION
        this.getCommand("mmability").setExecutor(new AbilityCommandInterface());
        
        getLogger().info("Mob Arena Plus Plugin Activated!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Mob Arena Plus Plugin Deactivated!");
    }
}
