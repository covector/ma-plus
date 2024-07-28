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
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.Pair;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import com.comphenix.protocol.wrappers.nbt.NbtWrapper;

import java.util.HashMap;
import java.util.UUID;

import dev.covector.maplus.Utils;

public class FakePumpkin {
    private static FakePumpkin instance;
    private HashMap<UUID, Integer> pumpkinPlayers = new HashMap<UUID, Integer>();
    private PacketListener packetListener;

    public void registerPacketListener() {
        ProtocolLibrary.getProtocolManager().addPacketListener(packetListener = new PacketAdapter(Utils.getPlugin(), PacketType.Play.Server.SET_SLOT) {
            @Override
            public void onPacketSending(PacketEvent event) {
                if (event.getPacketType() == PacketType.Play.Server.SET_SLOT) {
                    PacketContainer packet = event.getPacket();
                    Player receiver = event.getPlayer();
                    // int entityId = packet.getIntegers().read(0);
                    
                    int altBlurInd = hasPumpkin(receiver) ? pumpkinPlayers.get(receiver.getUniqueId()) : -1;
                    if (altBlurInd == -1) {
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

                    if (windowId != 0) {
                        return;
                    }

                    ItemStack item = slot == 5 ? createPumpkin(altBlurInd) : packet.getItemModifier().read(0);
                    // Bukkit.broadcastMessage("item: " + item.getType().toString());

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
            int altBlurInd = hasPumpkin(player) ? pumpkinPlayers.get(player.getUniqueId()) : -1;
            packet.getItemModifier().write(0, altBlurInd != -1 ? createPumpkin(altBlurInd) : player.getInventory().getHelmet());

            ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void unregisterPacketListener() {
        ProtocolLibrary.getProtocolManager().removePacketListener(packetListener);
    }

    public void applyPumpkin(Player player) {
        this.applyPumpkin(player, 0);
    }

    public void applyPumpkin(Player player, int altBlurInd) {
        pumpkinPlayers.put(player.getUniqueId(), altBlurInd);
        sendPacket(player);
    }

    public void removePumpkin(Player player) {
        pumpkinPlayers.remove(player.getUniqueId());
        sendPacket(player);
    }

    public boolean hasPumpkin(Player player) {
        return pumpkinPlayers.containsKey(player.getUniqueId());
    }

    private ItemStack createPumpkin(int altBlurInd) {
        ItemStack pumpkin = new ItemStack(Material.CARVED_PUMPKIN);
        if (altBlurInd > 0) {
            pumpkin = MinecraftReflection.getBukkitItemStack(pumpkin);
            NbtCompound compound = NbtFactory.ofCompound("tag");
            compound.put("AltBlur", altBlurInd);
            NbtFactory.setItemTag(pumpkin, compound);
        }
        return pumpkin;
    }

    public static FakePumpkin getInstance() {
        if (instance == null) {
            instance = new FakePumpkin();
        }
        return instance;
    }
}