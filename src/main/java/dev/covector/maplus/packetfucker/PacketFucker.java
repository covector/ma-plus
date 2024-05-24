package dev.covector.maplus.packetfucker;

import java.util.HashMap;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;

import dev.covector.maplus.Utils;
import dev.covector.maplus.packetfucker.def.CaesarSlot;
import dev.covector.maplus.packetfucker.def.ChunkUnload;
import dev.covector.maplus.packetfucker.def.ClearInv;
import dev.covector.maplus.packetfucker.def.FakeDamage;
import dev.covector.maplus.packetfucker.def.FakeDeath;
import dev.covector.maplus.packetfucker.def.FakeRain;
import dev.covector.maplus.packetfucker.def.FastTimeCycle;
import dev.covector.maplus.packetfucker.def.GhostCage;
import dev.covector.maplus.packetfucker.def.SineFloat;
import dev.covector.maplus.packetfucker.def.SineFloatIndv;
import dev.covector.maplus.packetfucker.def.WorldBorder;

public class PacketFucker {
    private static PacketFucker instance;
    private HashMap<String, PacketHandler> packetHandlers;
    
    public PacketFucker() {
        init();
    }

    public void init() {
        packetHandlers = new HashMap<String, PacketHandler>();
        packetHandlers.put("ghostCage", new GhostCage());
        packetHandlers.put("fastTime", new FastTimeCycle());
        packetHandlers.put("caesarSlot", new CaesarSlot());
        // packetHandlers.put("chunkUnload", new ChunkUnload());
        packetHandlers.put("worldBorder", new WorldBorder());
        packetHandlers.put("sineFloat", new SineFloat());
        packetHandlers.put("fakeDeath", new FakeDeath());
        packetHandlers.put("clearInv", new ClearInv());
        packetHandlers.put("fakeDamage", new FakeDamage());
        packetHandlers.put("fakeRain", new FakeRain());
        packetHandlers.put("sineFloatINDV", new SineFloatIndv());
    }
    public boolean hasHandler(String packetHandlerName) {
        return packetHandlers.containsKey(packetHandlerName);
    }
    public Set<String> getAllHandlerNames() {
        return packetHandlers.keySet();
    }

    public void registerPacketListener() {
        for (PacketHandler packetHandler : packetHandlers.values()) {
            packetHandler.registerPacketListener();
        }
    }

    public void unregisterPacketListener() {
        for (PacketHandler packetHandler : packetHandlers.values()) {
            packetHandler.unregisterPacketListener();
        }
    }

    public boolean addPlayer(Player player, String packetHandlerName) {
        if (!packetHandlers.containsKey(packetHandlerName)) {
            return false;
        }
        packetHandlers.get(packetHandlerName).addPlayer(player);
        return true;
    }

    public boolean removePlayer(Player player, String packetHandlerName) {
        if (!packetHandlers.containsKey(packetHandlerName)) {
            return false;
        }
        packetHandlers.get(packetHandlerName).removePlayer(player);
        return true;
    }

    public boolean hasPlayer(Player player, String packetHandlerName) {
        if (!packetHandlers.containsKey(packetHandlerName)) {
            return false;
        }
        return packetHandlers.get(packetHandlerName).hasPlayer(player);
    }

    public PacketHandler getPacketHandler(String packetHandlerName) {
        if (!packetHandlers.containsKey(packetHandlerName)) {
            return null;
        }
        return packetHandlers.get(packetHandlerName);
    }

    public void clearForPlayer(Player player) {
        for (PacketHandler packetHandler : packetHandlers.values()) {
            packetHandler.removePlayer(player);
        }
    }
    
    public static PacketFucker getInstance() {
        if (instance == null) {
            instance = new PacketFucker();
        }
        return instance;
    }
}
