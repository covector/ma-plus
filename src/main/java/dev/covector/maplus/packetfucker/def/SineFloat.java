package dev.covector.maplus.packetfucker.def;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;

import dev.covector.maplus.Utils;
import dev.covector.maplus.packetfucker.PacketHandler;

public class SineFloat extends PacketHandler{
    private static PacketType[] processPacketTypes = new PacketType[] { PacketType.Play.Server.POSITION, PacketType.Play.Server.ENTITY_TELEPORT };
    public PacketType[] getPacketTypes() {
        return processPacketTypes;
    }

    public void modifyPacket(Player receiver, PacketContainer packet) {
        // Bukkit.broadcastMessage("changing packet");
        int entityId = packet.getIntegers().read(0);
        if (receiver.getEntityId() == entityId) {
            return;
        }
        packet.getDoubles().write(1, packet.getDoubles().read(1) + Math.sin(System.currentTimeMillis() / 500D + entityId / 10D) * 2D);
    }

    public void sendPacket(Player player) {
        for (Entity entity : player.getNearbyEntities(20D, 10D, 20D)) {
            if (entity instanceof LivingEntity) {
                sendPacketOfEntity(player, (LivingEntity) entity);
            }
        }
    }

    public void sendPacketOfEntity(Player player, LivingEntity livingEntity) {
        PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_TELEPORT);
        packet.getIntegers().write(0, livingEntity.getEntityId());
        packet.getDoubles().write(0, livingEntity.getLocation().getX());
        packet.getDoubles().write(1, livingEntity.getLocation().getY());
        packet.getDoubles().write(2, livingEntity.getLocation().getZ());
        packet.getBytes().write(0, (byte) (livingEntity.getLocation().getYaw() * 256.0F / 360.0F));
        packet.getBytes().write(1, (byte) (livingEntity.getLocation().getPitch() * 256.0F / 360.0F));
        packet.getBooleans().write(0, livingEntity.isOnGround());

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

                for (UUID uuid : activePlayers) {
                    Player player = Bukkit.getPlayer(uuid);
                    for (Entity entity : player.getNearbyEntities(20D, 10D, 20D)) {
                        if (entity instanceof LivingEntity) {
                            sendPacketOfEntity(player, (LivingEntity) entity);
                        }
                    }
                }
            }
        };
        timer.runTaskTimer(Utils.getPlugin(), 0, 1L);
    }

    @Override
    public void onUnregister() {
        timer.cancel();
    }
}
