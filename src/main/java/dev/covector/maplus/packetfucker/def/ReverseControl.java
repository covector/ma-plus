package dev.covector.maplus.packetfucker.def;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;

import dev.covector.maplus.packetfucker.PacketHandler;

public class ReverseControl extends PacketHandler {
    private static PacketType[] processPacketTypes = new PacketType[] { PacketType.Play.Client.POSITION, PacketType.Play.Client.POSITION_LOOK };
    public PacketType[] getPacketTypes() {
        return processPacketTypes;
    }

    public void modifyPacket(Player receiver, PacketContainer packet) {
        
        int entityId = packet.getIntegers().read(0);
        Bukkit.broadcastMessage(String.valueOf(receiver.getEntityId()) + " and " + String.valueOf(entityId));
        if (receiver.getEntityId() != entityId) {
            return;
        }
        Bukkit.broadcastMessage("changing packet");
        packet.getDoubles().write(0, -44D);
        packet.getDoubles().write(1, 65D);
        packet.getDoubles().write(2, -51D);
    }

    public void sendPacket(Player player) {
    }
}
