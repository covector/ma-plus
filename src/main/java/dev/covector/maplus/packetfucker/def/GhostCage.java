package dev.covector.maplus.packetfucker.def;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.ChunkCoordIntPair;
import com.comphenix.protocol.wrappers.MultiBlockChangeInfo;
import com.comphenix.protocol.wrappers.WrappedBlockData;

import dev.covector.maplus.packetfucker.PacketHandler;

public class GhostCage extends PacketHandler {
    private static PacketType[] processPacketTypes = null;
    public PacketType[] getPacketTypes() {
        return processPacketTypes;
    }

    private HashMap<UUID, Location> playerLocation = new HashMap<UUID, Location>();

    public void modifyPacket(Player receiver, PacketContainer packet) {
    }

    public void sendPacket(Player player) {
        if (!playerLocation.containsKey(player.getUniqueId())) {
            return;
        }
        Location location = playerLocation.get(player.getUniqueId());
        PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.MULTI_BLOCK_CHANGE);
        Chunk chunk = location.getWorld().getChunkAt(location);
        packet.getChunkCoordIntPairs().write(0, new ChunkCoordIntPair(chunk.getX(), chunk.getZ()));
        packet.getMultiBlockChangeInfoArrays().write(0, new MultiBlockChangeInfo[] {
            createGhostBlockAt(location, 1, 0, 0),
            createGhostBlockAt(location, -1, 0, 0),
            createGhostBlockAt(location, 0, 0, 1),
            createGhostBlockAt(location, 0, 0, -1),
            createGhostBlockAt(location, 1, 1, 0),
            createGhostBlockAt(location, -1, 1, 0),
            createGhostBlockAt(location, 0, 1, 1),
            createGhostBlockAt(location, 0, 1, -1),
            createGhostBlockAt(location, 0, 2, 0)
        });
        safeSendPacket(player, packet);
    }

    private MultiBlockChangeInfo createGhostBlockAt(Location location, int offsetX, int offsetY, int offsetZ) {
        return new MultiBlockChangeInfo(location.clone().add(offsetX, offsetY, offsetZ), WrappedBlockData.createData(Material.GLASS));
    }

    @Override
    public void addPlayer(Player player) {
        activePlayers.add(player.getUniqueId());
        sendPacket(player);
        playerLocation.put(player.getUniqueId(), player.getLocation().getBlock().getLocation().clone());
        // need to tp to middle?
    }
}
