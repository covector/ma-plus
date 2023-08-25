package dev.covector.maplus.fakepumpkin;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.InternalStructure;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.Pair;

import java.util.HashSet;
import java.util.UUID;

import dev.covector.maplus.Utils;

public class FakePumpkin {
    private static FakePumpkin instance;
    private HashSet<UUID> pumpkinPlayers = new HashSet<>();
    private PacketListener packetListener;

    public void registerPacketListener() {
        ProtocolLibrary.getProtocolManager().addPacketListener(packetListener = new PacketAdapter(Utils.getPlugin(), PacketType.Play.Server.SET_SLOT) {
            @Override
            public void onPacketSending(PacketEvent event) {
                if (event.getPacketType() == PacketType.Play.Server.SET_SLOT) {
                    PacketContainer packet = event.getPacket();
                    Player receiver = event.getPlayer();
                    // int entityId = packet.getIntegers().read(0);
                    
                    if (!hasPumpkin(receiver)) {
                        return;
                    }
                    // Bukkit.broadcastMessage("has pumpkin");
                    // if (pumpkinPlayers.get(receiver.getUniqueId()) != entityId) {
                    //     return;
                    // }
                    // Bukkit.broadcastMessage("is pumpkin");

                    packet = event.getPacket().deepClone();
                    event.setPacket(packet);

                    int windowId = packet.getIntegers().read(0);
                    int stateId = packet.getIntegers().read(1);
                    int slot = packet.getIntegers().read(2);
                    // Bukkit.broadcastMessage("windowId: " + windowId);
                    // Bukkit.broadcastMessage("stateId: " + stateId);
                    // Bukkit.broadcastMessage("slot: " + slot);

                    ItemStack item = packet.getItemModifier().read(0);
                    // Bukkit.broadcastMessage("item: " + item.getType().toString());

                    if (slot == 5) {
                        item = new ItemStack(Material.PUMPKIN);
                    }

                    packet.getItemModifier().write(0, item);
                }
            }
        });
    }

    private void sendPacket(Player player) {
        try {
            PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.SET_SLOT);
            
            packet.getIntegers().write(0, 0); // window id
            packet.getIntegers().write(1, 0); // state id, idk if need to be correct, seems like using 0 works
            packet.getIntegers().write(2, 5); // slot
            packet.getItemModifier().write(0, hasPumpkin(player) ? new ItemStack(Material.PUMPKIN) : player.getInventory().getHelmet());

            ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void unregisterPacketListener() {
        ProtocolLibrary.getProtocolManager().removePacketListener(packetListener);
    }

    public void applyPumpkin(Player player) {
        // Bukkit.broadcastMessage("apply pumpkin");
        pumpkinPlayers.add(player.getUniqueId());
        sendPacket(player);
        // pumpkinPlayers.put(player.getUniqueId(), player.getEntityId());
    }

    public void removePumpkin(Player player) {
        // Bukkit.broadcastMessage("remove pumpkin");
        pumpkinPlayers.remove(player.getUniqueId());
        sendPacket(player);
    }

    public boolean hasPumpkin(Player player) {
        return pumpkinPlayers.contains(player.getUniqueId());
        // return pumpkinPlayers.containsKey(player.getUniqueId());
    }

    public static FakePumpkin getInstance() {
        if (instance == null) {
            instance = new FakePumpkin();
        }
        return instance;
    }
}