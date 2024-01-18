package dev.covector.maplus.packetfucker.def;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;

import dev.covector.maplus.Utils;
import dev.covector.maplus.packetfucker.PacketHandler;

public class ChunkUnload extends PacketHandler {
    private static PacketType[] processPacketTypes = null;
    public PacketType[] getPacketTypes() {
        return processPacketTypes;
    }

    private HashMap<UUID, ChunkLocation> playerChunk = new HashMap<UUID, ChunkLocation>();
    private class ChunkLocation {
        public int x;
        public int z;
        public ChunkLocation(int x, int z) {
            this.x = x;
            this.z = z;
        }
    }

    public void modifyPacket(Player receiver, PacketContainer packet) {
    }

    public void sendPacket(Player player) {
        if (!playerChunk.containsKey(player.getUniqueId())) {
            return;
        }
        ChunkLocation chunkLocation = playerChunk.get(player.getUniqueId());
        PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.UNLOAD_CHUNK);
        packet.getIntegers().write(0, chunkLocation.x);
        packet.getIntegers().write(1, chunkLocation.z);
        Bukkit.broadcastMessage(chunkLocation.x + " " + chunkLocation.z);
        safeSendPacket(player, packet);
    }

    @Override
    public void addPlayer(Player player) {
        Chunk chunk = player.getLocation().getChunk();
        playerChunk.put(player.getUniqueId(), new ChunkLocation(chunk.getX(), chunk.getZ()));
        sendPacket(player);
    }
}
