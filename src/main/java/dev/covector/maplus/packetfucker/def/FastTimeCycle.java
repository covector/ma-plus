package dev.covector.maplus.packetfucker.def;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;

import dev.covector.maplus.Utils;
import dev.covector.maplus.packetfucker.PacketHandler;

public class FastTimeCycle extends PacketHandler {
    private static PacketType[] processPacketTypes = new PacketType[] { PacketType.Play.Server.UPDATE_TIME };
    public PacketType[] getPacketTypes() {
        return processPacketTypes;
    }

    public void modifyPacket(Player receiver, PacketContainer packet) {
        packet.getLongs().write(1, currentTime());
    }

    private long currentTime() {
        return System.currentTimeMillis() * 10 % 24000;
    }

    public void sendPacket(Player player) {
        sendPacket(player, currentTime());
    }

    public void sendPacket(Player player, long time) {
        PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.UPDATE_TIME);
        packet.getLongs().write(1, time);
        safeSendPacket(player, packet);
    }

    private BukkitRunnable timer;

    @Override
    public void onRegister() {
        BukkitRunnable timer = new BukkitRunnable() {
            @Override
            public void run() {
                long time = currentTime();
                for (UUID uuid : activePlayers) {
                    sendPacket(Bukkit.getPlayer(uuid), time);
                }
            }
        };
        timer.runTaskTimer(Utils.getPlugin(), 0, 2L);
    }

    @Override
    public void onUnregister() {
        timer.cancel();
    }
}