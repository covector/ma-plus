package dev.covector.maplus.packetfucker.def;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.ChunkCoordIntPair;
import com.comphenix.protocol.wrappers.MultiBlockChangeInfo;
import com.comphenix.protocol.wrappers.WrappedBlockData;

import dev.covector.maplus.Utils;
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
    }

    public void sendPacket(Player player, Location location) {
        // if (!playerLocation.containsKey(player.getUniqueId())) {
        //     return;
        // }
        BlockPosition[] bp = new BlockPosition[] {
            createGhostBlockAt(location, 1, 0, 0),
            createGhostBlockAt(location, -1, 0, 0),
            createGhostBlockAt(location, 0, 0, 1),
            createGhostBlockAt(location, 0, 0, -1),
            createGhostBlockAt(location, 1, 1, 0),
            createGhostBlockAt(location, -1, 1, 0),
            createGhostBlockAt(location, 0, 1, 1),
            createGhostBlockAt(location, 0, 1, -1),
            createGhostBlockAt(location, 0, 2, 0)
        };
        ArrayList<MultiBlockChangePacket> mbcp = groupByChunk(bp);
        for (MultiBlockChangePacket multiBlockChangePacket : mbcp) {
            multiBlockChangePacket.sendPacket(player);
        }
    }

    private class MultiBlockChangePacket {
        public BlockPosition chunk;
        public ArrayList<Short> blockPosition = new ArrayList<Short>();
        public ArrayList<WrappedBlockData> blockData = new ArrayList<WrappedBlockData>();
        public MultiBlockChangePacket(BlockPosition position) {
            this.chunk = new BlockPosition(position.getX() >> 4, position.getY() >> 4, position.getZ() >> 4);
            addBlock(position);
        }
        public boolean sameChunk(BlockPosition position) {
            return this.chunk.getX() == (position.getX() >> 4) && this.chunk.getZ() == (position.getZ() >> 4) && this.chunk.getY() == (position.getY() >> 4);
        }
        public void addBlock(BlockPosition position) {
            blockPosition.add((short) ((position.getX() & 0xF) << 8 | (position.getZ() & 0xF) << 4 | (position.getY() & 0xF) << 0));
            blockData.add(WrappedBlockData.createData(Material.GLASS));
        }
        public void sendPacket(Player player) {
            PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.MULTI_BLOCK_CHANGE);
            
            WrappedBlockData[] blockDataA = blockData.toArray(new WrappedBlockData[0]);
            short[] blockLocations = new short[blockPosition.size()];
            for (int i = 0; i < blockLocations.length; i++) {
                blockLocations[i] = blockPosition.get(i);
            }
            
            packet.getSectionPositions().write(0, chunk);
            packet.getBlockDataArrays().write(0, blockDataA);
            packet.getShortArrays().write(0, blockLocations);

            safeSendPacket(player, packet);
        }
    }

    private BlockPosition createGhostBlockAt(Location location, int offsetX, int offsetY, int offsetZ) {
        return new BlockPosition(location.getBlockX() + offsetX, location.getBlockY() + offsetY, location.getBlockZ() + offsetZ);
    }

    private ArrayList<MultiBlockChangePacket> groupByChunk(BlockPosition[] blockPositions) {
        ArrayList<MultiBlockChangePacket> result = new ArrayList<MultiBlockChangePacket>();
        for (BlockPosition blockPosition : blockPositions) {
            boolean found = false;
            for (MultiBlockChangePacket mbcp : result) {
                if (mbcp.sameChunk(blockPosition)) {
                    found = true;
                    mbcp.addBlock(blockPosition);
                    break;
                }
            }
            if (!found) {
                result.add(new MultiBlockChangePacket(blockPosition));
            }
        }
        return result;
    }

    @Override
    public void addPlayer(Player player) {
        Location location = player.getLocation().getBlock().getLocation().clone();
        // playerLocation.put(player.getUniqueId(), location);
        player.teleport(location.clone().add(0.5, 0, 0.5).setDirection(player.getLocation().getDirection()));
        Bukkit.broadcastMessage(location.toString());
        sendPacket(player, location);
    }
}
