package dev.covector.maplus.packetfucker.def;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers.WorldBorderAction;

import dev.covector.maplus.packetfucker.PacketHandler;

public class WorldBorder extends PacketHandler {
    private static PacketType[] processPacketTypes = null;
    public PacketType[] getPacketTypes() {
        return processPacketTypes;
    }

    private HashMap<UUID, Location> playerLocation = new HashMap<UUID, Location>();

    public void modifyPacket(Player receiver, PacketContainer packet) {
    }

    public void sendPacket(Player player) {
        if (!playerLocation.containsKey(player.getUniqueId())) {
            return;
        }
        Location location = playerLocation.get(player.getUniqueId());

        PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.INITIALIZE_BORDER);
        packet.getDoubles().write(0, location.getX());
        packet.getDoubles().write(1, location.getZ());
        packet.getDoubles().write(2, 10D);
        packet.getDoubles().write(3, 1D);
        packet.getLongs().write(0, 10000L);
        packet.getIntegers().write(0, 29999984);
        packet.getIntegers().write(1, 2);
        packet.getIntegers().write(2, 2);
        safeSendPacket(player, packet);

        // PacketContainer borderCenterPacket = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.SET_BORDER_CENTER);
        // borderCenterPacket.getDoubles().write(0, location.getX());
        // borderCenterPacket.getDoubles().write(1, location.getZ());
        // safeSendPacket(player, borderCenterPacket);
        // // broadcast border center location
        // Bukkit.broadcastMessage("border at " + location.getX() + " " + location.getZ());
        
        // PacketContainer borderLerpPacket = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.SET_BORDER_LERP_SIZE);
        // borderLerpPacket.getDoubles().write(0, 10D);
        // borderLerpPacket.getDoubles().write(1, 10D);
        // borderLerpPacket.getLongs().write(0, 0L);
        // safeSendPacket(player, borderLerpPacket);

        // PacketContainer borderSizePacket = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.SET_BORDER_SIZE);
        // borderSizePacket.getDoubles().write(0, 10D);

        // PacketContainer warningDelayPacket = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.SET_BORDER_WARNING_DELAY);
        // warningDelayPacket.getIntegers().write(0, 2);
        // safeSendPacket(player, warningDelayPacket);
        // PacketContainer warningDistancePacket = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.SET_BORDER_WARNING_DISTANCE);
        // warningDistancePacket.getIntegers().write(0, 2);
        // safeSendPacket(player, warningDistancePacket);
    }

    @Override
    public void addPlayer(Player player) {
        playerLocation.put(player.getUniqueId(), player.getLocation().getBlock().getLocation().clone());
        sendPacket(player);
        Bukkit.broadcastMessage("test");
    }
}