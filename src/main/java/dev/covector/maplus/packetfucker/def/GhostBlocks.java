package dev.covector.maplus.packetfucker.def;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.WrappedBlockData;

import dev.covector.maplus.packetfucker.PacketHandler;

public class GhostBlocks extends PacketHandler {
    private static PacketType[] processPacketTypes = new PacketType[] { PacketType.Play.Server.BLOCK_CHANGE };
    public PacketType[] getPacketTypes() {
        return processPacketTypes;
    }

    public void modifyPacket(Player receiver, PacketContainer packet) {
        BlockPosition position = packet.getBlockPositionModifier().read(0);

        if (position.getX() != -44 || position.getY() != 65 || position.getZ() != -49) {
            return;
        }

        packet.getBlockData().write(0, WrappedBlockData.createData(Material.GLASS));
    }

    public void sendPacket(Player player) {
    }
}