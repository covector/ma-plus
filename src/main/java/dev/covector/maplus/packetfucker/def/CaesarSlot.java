package dev.covector.maplus.packetfucker.def;

import java.util.List;

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
    private static PacketType[] processPacketTypes = new PacketType[] { PacketType.Play.Server.SET_SLOT, PacketType.Play.Server.WINDOW_ITEMS };
    public PacketType[] getPacketTypes() {
        return processPacketTypes;
    }

    public void modifyPacket(Player receiver, PacketContainer packet) {
        if (packet.getType() == PacketType.Play.Server.SET_SLOT) {
            int slot = packet.getIntegers().read(2);
            if (slot >= 36 && slot <= 44) {
                int targtSlot = slot == 44 ? 0 : slot - 35;
                packet.getItemModifier().write(0, airIfNull(receiver.getInventory().getItem(targtSlot)));
            }
        } else {
            List<ItemStack> items = packet.getItemListModifier().read(0);
            ItemStack temp = items.get(36);
            for (int i = 36; i <= 43; i++) {
                items.set(i, items.get(i+1));
            }
            items.set(44, temp);
            packet.getItemListModifier().write(0, items);
        }
    }

    public void sendPacket(Player player) {
        for (int i = 36; i <= 44; i++) {
            int targtSlot = hasPlayer(player) ? (i == 44 ? 0 : i - 35) : i - 36;
            PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.SET_SLOT);
            
            packet.getIntegers().write(0, 0);
            packet.getIntegers().write(1, 0);
            packet.getIntegers().write(2, i);
            packet.getItemModifier().write(0, airIfNull(player.getInventory().getItem(targtSlot)));

            safeSendPacket(player, packet);
        }
    }

    private ItemStack airIfNull(ItemStack item) {
        return item == null ? new ItemStack(Material.AIR) : item;
    }
}
