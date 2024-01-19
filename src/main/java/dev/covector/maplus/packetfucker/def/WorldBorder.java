package dev.covector.maplus.packetfucker.def;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;

import dev.covector.maplus.packetfucker.PacketHandler;

public class WorldBorder extends PacketHandler {
    private static PacketType[] processPacketTypes = null;
    public PacketType[] getPacketTypes() {
        return processPacketTypes;
    }

    public void modifyPacket(Player receiver, PacketContainer packet) {
    }

    public void sendPacket(Player player) {
        if (hasPlayer(player)) {
            sendPresetBorderPacket(player);
        } else {
            sendResetPacket(player);
        }
    }

    public void sendPacket(Player player, double x, double z, double oldDiameter, double newDiameter, long speed, int portalTeleportBoundary, int warningBlocks, int warningTime) {
        PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.INITIALIZE_BORDER);
        packet.getDoubles().write(0, x);
        packet.getDoubles().write(1, z);
        packet.getDoubles().write(2, oldDiameter);
        packet.getDoubles().write(3, newDiameter);
        packet.getLongs().write(0, speed);
        packet.getIntegers().write(0, portalTeleportBoundary);
        packet.getIntegers().write(1, warningBlocks);
        packet.getIntegers().write(2, warningTime);
        safeSendPacket(player, packet);
    }

    public void sendPacket(Player player, double oldDiameter, double newDiameter, long speed, int portalTeleportBoundary, int warningBlocks, int warningTime) {
        Location location = player.getLocation().getBlock().getLocation().clone();
        sendPacket(player, location.getX(), location.getZ(), oldDiameter, newDiameter, speed, portalTeleportBoundary, warningBlocks, warningTime);
    }

    private void sendPresetBorderPacket(Player player) {
        Location location = player.getLocation().getBlock().getLocation().clone();
        sendPacket(player, location.getX(), location.getZ(), 10D, 5D, 4000L, 29999984, 2, 2);
    }

    public void sendResetPacket(Player player) {
        sendPacket(player, 0D, 0D, 60000000D, 60000000D, 0L, 29999984, 0, 0);
    }
}