package dev.covector.maplus.packetfucker;

import java.util.HashSet;
import java.util.UUID;

import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;

import dev.covector.maplus.Utils;

public abstract class PacketHandler {
    protected HashSet<UUID> activePlayers = new HashSet<>();
    private PacketListener packetListener;
    public PacketHandler() {
        packetListener = new PacketAdapter(Utils.getPlugin(), getPacketTypes()) {
            @Override
            public void onPacketSending(PacketEvent event) {
                handlePacket(event);
            }
        };
    }
    public void registerPacketListener() {
        ProtocolLibrary.getProtocolManager().addPacketListener(packetListener);
        // send initial packet to all players online (temporary)
        for (Player player : Utils.getPlugin().getServer().getOnlinePlayers()) {
            addPlayer(player);
        }
        onRegister();
    }
    public void unregisterPacketListener() {
        ProtocolLibrary.getProtocolManager().removePacketListener(packetListener);
        onUnregister();
    }
    public void addPlayer(Player player) {
        activePlayers.add(player.getUniqueId());
        sendPacket(player);
    }
    public boolean hasPlayer(Player player) {
        return activePlayers.contains(player.getUniqueId());
    }
    public void removePlayer(Player player) {
        activePlayers.remove(player.getUniqueId());
        sendPacket(player);
    }
    private void handlePacket(PacketEvent event) {
        PacketContainer packet = event.getPacket();
        Player receiver = event.getPlayer();
        if (!hasPlayer(receiver)) {
            // return;
        }

        packet = event.getPacket().deepClone();
        event.setPacket(packet);
        modifyPacket(receiver, packet);
    }
    protected void safeSendPacket(Player player, PacketContainer packet) {
        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public abstract PacketType[] getPacketTypes();
    public abstract void modifyPacket(Player receiver, PacketContainer packet);
    public abstract void sendPacket(Player player);
    public void onRegister() {
    }
    public void onUnregister() {
    }
}
