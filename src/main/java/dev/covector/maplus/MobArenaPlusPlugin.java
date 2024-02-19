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
import dev.covector.maplus.packetfucker.PacketFucker;
import dev.covector.maplus.fakepumpkin.*;
import dev.covector.maplus.misc.*;

public class MobArenaPlusPlugin extends JavaPlugin
{
    private ReviveMAListener reviverListener;
    private ReviveCommand reviveCommand;
    private CLBListener clbListener;
    private TridentNoPickUpListener tridentNoPickUpListener;
    private AbilityCommandInterface abilityCommandInterface;
    private MiscManager miscManager;

    @Override
    public void onEnable() {
        if (true) {
            Utils.setPlugin(this);
            this.getCommand("mmability").setExecutor(abilityCommandInterface = new AbilityCommandInterface());
            PacketFucker.getInstance().registerPacketListener();
            return;
        }

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
        this.getCommand("marevive").setExecutor(reviveCommand = new ReviveCommand(reviver));
        Bukkit.getPluginManager().registerEvents(reviverListener = new ReviveMAListener(reviver), this);

        // LEADERBOARD CLEAR
        Plugin ajlplugin = getServer().getPluginManager().getPlugin("ajLeaderboards");
        if (ajlplugin != null) {
            LeaderboardPlugin lbPlugin = (LeaderboardPlugin) ajlplugin;
            Bukkit.getPluginManager().registerEvents(clbListener = new CLBListener(lbPlugin), this);
        }

        // TRIDENT NO PICKUP
        Bukkit.getPluginManager().registerEvents(tridentNoPickUpListener = new TridentNoPickUpListener(), this);

        // MYTHIC MOBS EXTENSION
        this.getCommand("mmability").setExecutor(abilityCommandInterface = new AbilityCommandInterface());

        // FAKE PUMPKIN
        FakePumpkin.getInstance().registerPacketListener();

        // PACKET FUCKER
        PacketFucker.getInstance().registerPacketListener();

        // MISCELLANEOUS
        miscManager = new MiscManager();
        miscManager.register();
        
        getLogger().info("Mob Arena Plus Plugin Activated!");
    }

    @Override
    public void onDisable() {
        // if (true) {
        //     PacketFucker.getInstance().unregisterPacketListener();
        //     this.getCommand("mmability").setExecutor(null);
        //     return;
        // }

        // REVIVE
        this.getCommand("marevive").setExecutor(null);
        reviverListener.unregister();

        // LEADERBOARD CLEAR
        if (clbListener != null) {
            clbListener.unregister();
        }

        // TRIDENT NO PICKUP
        tridentNoPickUpListener.unregister();

        // MYTHIC MOBS EXTENSION
        this.getCommand("mmability").setExecutor(null);


        // FAKE PUMPKIN
        FakePumpkin.getInstance().unregisterPacketListener();

        // PACKET FUCKER
        PacketFucker.getInstance().unregisterPacketListener();

        // MISCELLANEOUS
        miscManager.unregister();

        getLogger().info("Mob Arena Plus Plugin Deactivated!");
    }
}
