package dev.covector.maplus.packetfucker.def;

import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;

import dev.covector.maplus.packetfucker.PacketHandler;

public class FakeDamage extends PacketHandler {
    private static PacketType[] processPacketTypes = null;
    public PacketType[] getPacketTypes() {
        return processPacketTypes;
    }

    public void modifyPacket(Player receiver, PacketContainer packet) {
    }

    public void sendPacket(Player player) {
    }

    public void sendDamagePacket(Player player) {
        PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.HURT_ANIMATION);
        packet.getIntegers().write(0, player.getEntityId());
        packet.getFloat().write(0, 0F);
        safeSendPacket(player, packet);
    }

    @Override
    public void addPlayer(Player player) {
        sendDamagePacket(player);
    }
}
