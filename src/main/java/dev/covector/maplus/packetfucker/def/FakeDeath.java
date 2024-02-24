package dev.covector.maplus.packetfucker.def;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;

import dev.covector.maplus.packetfucker.PacketHandler;

public class FakeDeath extends PacketHandler {
    private static PacketType[] processPacketTypes = null;
    public PacketType[] getPacketTypes() {
        return processPacketTypes;
    }

    public void modifyPacket(Player receiver, PacketContainer packet) {
    }

    public void sendPacket(Player player) {
    }

    public void sendDeathPacket(Player player) {
        PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.PLAYER_COMBAT_KILL);
        packet.getIntegers().write(0, player.getEntityId());
        packet.getChatComponents().write(0, WrappedChatComponent.fromText("Title Screen > Respawn"));
        safeSendPacket(player, packet);
    }

    @Override
    public void addPlayer(Player player) {
        sendDeathPacket(player);
    }
}
