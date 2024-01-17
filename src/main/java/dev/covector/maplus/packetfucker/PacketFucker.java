package dev.covector.maplus.packetfucker;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.WrappedBlockData;

import dev.covector.maplus.Utils;
import dev.covector.maplus.packetfucker.def.CaesarSlot;
import dev.covector.maplus.packetfucker.def.ChunkUnload;
import dev.covector.maplus.packetfucker.def.FastTimeCycle;
import dev.covector.maplus.packetfucker.def.GhostBlocks;
import dev.covector.maplus.packetfucker.def.GhostCage;
import dev.covector.maplus.packetfucker.def.ReverseControl;
import dev.covector.maplus.packetfucker.def.TestHandler;

public class PacketFucker {
    private static PacketFucker instance;
    private PacketListener packetListener;
    private HashMap<String, PacketHandler> packetHandlers;
    
    public PacketFucker() {
        init();
    }

    public void init() {
        packetHandlers = new HashMap<String, PacketHandler>();
        packetHandlers.put("ghostCage", new GhostCage());
        packetHandlers.put("fastTime", new FastTimeCycle());
        packetHandlers.put("caesarSlot", new CaesarSlot());
        packetHandlers.put("chunkUnload", new ChunkUnload());
        // shrinking border
        // sine wave float
    }

    public void registerPacketListener() {
        // packetListeners = new PacketListener[] {
        //     new PacketAdapter(Utils.getPlugin(), PacketType.Play.Server.ENTITY_LOOK, PacketType.Play.Server.REL_ENTITY_MOVE_LOOK, PacketType.Play.Server.REL_ENTITY_MOVE, PacketType.Play.Server.ENTITY_TELEPORT) {
        //         @Override
        //         public void onPacketSending(PacketEvent event) {
        //             PacketContainer packet = event.getPacket();
        //             Player receiver = event.getPlayer();
        //             int entityId = packet.getIntegers().read(0);

        //             if (receiver.getEntityId() == entityId) {
        //                 return;
        //             }

        //             // Bukkit.broadcastMessage("changing packet");

        //             packet = event.getPacket().deepClone();
        //             event.setPacket(packet);

        //             packet.getBytes().write(1, (byte) (90 * 256.0F / 360.0F));

        //             // Serializer serializer = Registry.get(Byte.class);
        //             // List<WrappedDataValue> dataValues = packet.getDataValueCollectionModifier().read(0);
        //             // for (WrappedDataValue dataValue : dataValues) {
        //             //     if (dataValue.getValue() == null) {
        //             //         continue;
        //             //     }
        //             //     if (dataValue.getIndex() == 0) {
        //             //         dataValues.set(0, new WrappedDataValue(0, serializer, (byte) 0x08));
        //             //         try {
        //             //             packet.getDataValueCollectionModifier().write(0, dataValues);
        //             //         } catch (Exception e) {
        //             //             e.printStackTrace();
        //             //         }
        //             //     }
        //             // }

        //             // for (PacketHandler ph : packetHandlers) {
        //             //     for (PacketType pt : ph.getPacketTypes()) {
        //             //         if (event.getPacketType() == pt) {
        //             //             ph.onPacketSending(event);
        //             //         }
        //             //     }
        //             // }
        //         }
        //     },
        //     new PacketAdapter(Utils.getPlugin(), PacketType.Play.Server.BLOCK_CHANGE) {
        //         @Override
        //         public void onPacketSending(PacketEvent event) {
        //             PacketContainer packet = event.getPacket();
        //             Player receiver = event.getPlayer();
        //             BlockPosition position = packet.getBlockPositionModifier().read(0);

        //             Bukkit.broadcastMessage("x: " + position.getX() + ", y: " + position.getY() + ", z: " + position.getZ());

        //             if (position.getX() != -44 || position.getY() != 65 || position.getZ() != -49) {
        //                 return;
        //             }

        //             Bukkit.broadcastMessage("changing packet");

        //             packet = event.getPacket().deepClone();
        //             event.setPacket(packet);

        //             packet.getBlockData().write(0, WrappedBlockData.createData(Material.AIR));
        //         }
        //     }
        // };
        packetListener = new PacketAdapter(Utils.getPlugin(), new PacketType[] { PacketType.Play.Server.WINDOW_ITEMS }) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                // PacketContainer packet = event.getPacket();
                // Player receiver = event.getPlayer();
                // int entityId = packet.getIntegers().read(0);
                // Bukkit.broadcastMessage(String.valueOf(receiver.getEntityId()) + " and " + String.valueOf(entityId));
                // if (receiver.getEntityId() != entityId) {
                //     return;
                // }
                // packet = event.getPacket().deepClone();
                // event.setPacket(packet);
                // Bukkit.broadcastMessage(String.valueOf(packet.getDoubles().read(0)) + ", " + String.valueOf(packet.getDoubles().read(1)) + ", " + String.valueOf(packet.getDoubles().read(2)));
                // packet.getDoubles().write(0, -44D);
                // packet.getDoubles().write(1, 65D);
                // packet.getDoubles().write(2, -51D);
            }
        };
        
        for (PacketHandler packetHandler : packetHandlers.values()) {
            if (packetHandler.getPacketTypes() == null) {
                continue;
            }
            packetHandler.registerPacketListener();
        }
        ProtocolLibrary.getProtocolManager().addPacketListener(packetListener);
    }

    public void unregisterPacketListener() {
        for (PacketHandler packetHandler : packetHandlers.values()) {
            if (packetHandler.getPacketTypes() == null) {
                continue;
            }
            packetHandler.unregisterPacketListener();
        }
        // ProtocolLibrary.getProtocolManager().removePacketListener(packetListener);
    }

    public boolean addPlayer(Player player, String packetHandlerName) {
        if (!packetHandlers.containsKey(packetHandlerName)) {
            return false;
        }
        packetHandlers.get(packetHandlerName).addPlayer(player);
        return true;
    }

    public boolean removePlayer(Player player, String packetHandlerName) {
        if (!packetHandlers.containsKey(packetHandlerName)) {
            return false;
        }
        packetHandlers.get(packetHandlerName).removePlayer(player);
        return true;
    }

    public boolean hasPlayer(Player player, String packetHandlerName) {
        if (!packetHandlers.containsKey(packetHandlerName)) {
            return false;
        }
        return packetHandlers.get(packetHandlerName).hasPlayer(player);
    }

    public static PacketFucker getInstance() {
        if (instance == null) {
            instance = new PacketFucker();
        }
        return instance;
    }
}
