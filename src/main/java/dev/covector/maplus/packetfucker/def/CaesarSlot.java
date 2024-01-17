package dev.covector.maplus.packetfucker.def;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.WrappedBlockData;

import dev.covector.maplus.packetfucker.PacketHandler;

public class CaesarSlot extends PacketHandler{
    private static PacketType[] processPacketTypes = new PacketType[] { PacketType.Play.Server.SET_SLOT };
    public PacketType[] getPacketTypes() {
        return processPacketTypes;
    }

    public void modifyPacket(Player receiver, PacketContainer packet) {
        int slot = packet.getIntegers().read(2);
        if (slot >= 36 && slot <= 44) {
            int targtSlot = slot == 44 ? 0 : slot - 35;
            packet.getItemModifier().write(0, receiver.getInventory().getItem(targtSlot));
        }
    }

    public void sendPacket(Player player) {
        for (int i = 36; i <= 44; i++) {
            int targtSlot = i == 44 ? 0 : i - 35;
            PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.SET_SLOT);
            
            packet.getIntegers().write(0, 0);
            packet.getIntegers().write(1, 0);
            packet.getIntegers().write(2, i);
            packet.getItemModifier().write(0, player.getInventory().getItem(targtSlot));

            ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
        }
        
    }
}
