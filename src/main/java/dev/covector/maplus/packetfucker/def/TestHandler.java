package dev.covector.maplus.packetfucker.def;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;

import dev.covector.maplus.packetfucker.PacketHandler;

public class TestHandler extends PacketHandler{
    private static PacketType[] processPacketTypes = new PacketType[] { PacketType.Play.Server.ENTITY_LOOK, PacketType.Play.Server.REL_ENTITY_MOVE_LOOK, PacketType.Play.Server.REL_ENTITY_MOVE, PacketType.Play.Server.ENTITY_TELEPORT };
    public PacketType[] getPacketTypes() {
        return processPacketTypes;
    }

    public void modifyPacket(Player receiver, PacketContainer packet) {
        // Bukkit.broadcastMessage("changing packet");
        int entityId = packet.getIntegers().read(0);
        if (receiver.getEntityId() == entityId) {
            return;
        }
        packet.getBytes().write(1, (byte) (90 * 256.0F / 360.0F));
    }

    public void sendPacket(Player player) {
    }
}
