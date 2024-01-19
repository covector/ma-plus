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
        // packet.getLongs().write(1, currentTime());
        packet.getLongs().write(1, time);
    }

    // private long currentTime() {
    //     return System.currentTimeMillis() * 10 % 24000;
    // }
    private long time = 0; // avoid modulo

    public void sendPacket(Player player) {
        // sendPacket(player, currentTime());
        sendPacket(player, time);
    }

    public void sendPacket(Player player, long time) {
        PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.UPDATE_TIME);
        packet.getLongs().write(1, time);
        safeSendPacket(player, packet);
    }

    private BukkitRunnable timer;

    @Override
    public void onRegister() {
        timer = new BukkitRunnable() {
            @Override
            public void run() {
                if (activePlayers.size() == 0) {
                    return;
                }

                time += 1400;
                if (time >= 24000) {
                    time -= 24000;
                }

                for (UUID uuid : activePlayers) {
                    sendPacket(Bukkit.getPlayer(uuid), time);
                }
            }
        };
        timer.runTaskTimer(Utils.getPlugin(), 0, 1L);
    }

    @Override
    public void onUnregister() {
        if (timer != null)
            timer.cancel();
    }
}