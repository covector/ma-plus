package dev.covector.maplus;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.Plugin;

import com.garbagemule.MobArena.MobArena;

import dev.covector.maplus.revive.*;

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

        Reviver reviver = new Reviver();
        this.getCommand("marevive").setExecutor(new ReviveCommand(reviver));
        Bukkit.getPluginManager().registerEvents(new ReviveMAListener(reviver), this);
        
        getLogger().info("Mob Arena Plus Plugin Activated!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Mob Arena Plus Plugin Deactivated!");
    }
}
