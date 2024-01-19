package dev.covector.maplus.packetfucker.def;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;

import dev.covector.maplus.packetfucker.PacketHandler;

public class ClearInv extends PacketHandler{
    private static PacketType[] processPacketTypes = new PacketType[] { PacketType.Play.Server.SET_SLOT, PacketType.Play.Server.WINDOW_ITEMS };
    public PacketType[] getPacketTypes() {
        return processPacketTypes;
    }

    public void modifyPacket(Player receiver, PacketContainer packet) {
        if (packet.getType() == PacketType.Play.Server.SET_SLOT) {
            packet.getItemModifier().write(0, new ItemStack(Material.AIR));
        } else {
            packet.getItemListModifier().write(0, createEmptyInventory());
        }
    }

    public void sendPacket(Player player) {
        PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.WINDOW_ITEMS);
        
        packet.getIntegers().write(0, 0);
        if (hasPlayer(player)) {
            packet.getItemListModifier().write(0, createEmptyInventory());
        } else {
            packet.getItemListModifier().write(0, createNormalInventory(player));
        }

        safeSendPacket(player, packet);
    }

    private List<ItemStack> createEmptyInventory() {
        List<ItemStack> items = new ArrayList<ItemStack>();
        for (int i = 0; i <= 45; i++) {
            items.add(new ItemStack(Material.AIR));
        }
        return items;
    }

    private List<ItemStack> createNormalInventory(Player player) {
        List<ItemStack> items = new ArrayList<ItemStack>();
        for (int i = 0; i <= 4; i++) {
            items.add(new ItemStack(Material.AIR));
        }
        for (int i = 5; i <= 8; i++) {
            items.add(airIfNull(player.getInventory().getArmorContents()[8 - i]));
        }
        for (int i = 9; i <= 35; i++) {
            items.add(airIfNull(player.getInventory().getItem(i)));
        }
        for (int i = 36; i <= 44; i++) {
            items.add(airIfNull(player.getInventory().getItem(i - 36)));
        }
        return items;
    }

    private ItemStack airIfNull(ItemStack item) {
        return item == null ? new ItemStack(Material.AIR) : item;
    }
}