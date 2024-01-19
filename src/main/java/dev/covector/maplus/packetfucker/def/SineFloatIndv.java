package dev.covector.maplus.packetfucker.def;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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

public class SineFloatIndv extends PacketHandler{
    private static PacketType[] processPacketTypes = new PacketType[] { PacketType.Play.Server.ENTITY_TELEPORT };
    public PacketType[] getPacketTypes() {
        return processPacketTypes;
    }

    private HashMap<UUID, HashSet<Integer>> activePairs = new HashMap<UUID, HashSet<Integer>>();
    private HashMap<Integer, LivingEntity> entityIdEntityMap = new HashMap<Integer, LivingEntity>();

    public void modifyPacket(Player receiver, PacketContainer packet) {
        int entityId = packet.getIntegers().read(0);
        if (!hasPairs(receiver, entityId)) {
            return;
        }
        packet.getDoubles().write(1, packet.getDoubles().read(1) + Math.sin(System.currentTimeMillis() / 500D + entityId / 10D) * 2D);
    }

    public boolean hasPairs(Player receiver, int entityId) {
        return activePairs.containsKey(receiver.getUniqueId()) && activePairs.get(receiver.getUniqueId()).contains(entityId);
    }

    public boolean hasPairs(Player receiver, LivingEntity livingEntity) {
        return hasPairs(receiver, livingEntity.getEntityId());
    }

    public void sendPacket(Player player) {
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
            private int ti = 0;

            @Override
            public void run() {
                // if (Bukkit.getPlayer("PeeToo") != null) {
                //     for (Entity entity : Bukkit.getPlayer("PeeToo").getNearbyEntities(1D, 1D, 1D)) {
                //         if (entity instanceof LivingEntity) {
                //             Bukkit.broadcastMessage(entity.getUniqueId().toString());
                //         }
                //     }
                // }

                if (ti > 600) {
                    ti = 0;
                    int count = cleanMemory();
                    // Bukkit.broadcastMessage("cleaned " + count + " entities from memory");
                }
                ti++;
                
                if (activePairs.isEmpty()) {
                    return;
                }

                for (UUID uuid : activePairs.keySet()) {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player == null) {
                        continue;
                    }
                    for (Integer entityId : activePairs.get(uuid)) {
                        LivingEntity livingEntity = entityIdEntityMap.get(entityId);
                        if (livingEntity == null) {
                            continue;
                        }
                        sendPacketOfEntity(player, livingEntity);
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

    public void addPairs(Player player, LivingEntity livingEntity) {
        if (!activePairs.containsKey(player.getUniqueId())) {
            activePairs.put(player.getUniqueId(), new HashSet<Integer>());
        }
        activePairs.get(player.getUniqueId()).add(livingEntity.getEntityId());
        entityIdEntityMap.put(livingEntity.getEntityId(), livingEntity);
        addPlayer(player);
    }

    public void removePairs(Player player, LivingEntity livingEntity) {
        if (!activePairs.containsKey(player.getUniqueId())) {
            return;
        }
        activePairs.get(player.getUniqueId()).remove(livingEntity.getEntityId());
        if (activePairs.get(player.getUniqueId()).isEmpty()) {
            activePairs.remove(player.getUniqueId());
            removePlayer(player);
            sendPacketOfEntity(player, livingEntity);
        }
    }

    public void removeAllFromPlayer(Player player) {
        if (!activePairs.containsKey(player.getUniqueId())) {
            return;
        }
        removePlayer(player);
        for (LivingEntity livingEntity: entityIdEntityMap.values()) {
            sendPacketOfEntity(player, livingEntity);
        }
        activePairs.remove(player.getUniqueId());
    }

    private int cleanMemory() {
        ArrayList<Integer> toRemove = new ArrayList<>();
        int count = 0;
        for (int entityId: entityIdEntityMap.keySet()) {
            LivingEntity entity = entityIdEntityMap.get(entityId);
            if (entity == null || entity.isDead()) {
                toRemove.add(entityId);
                count++;
            }
        }
        for (int entityId: toRemove) {
            entityIdEntityMap.remove(entityId);
        }
        for (UUID uuid: activePairs.keySet()) {
            HashSet<Integer> entityIds = activePairs.get(uuid);
            for (int entityId: toRemove) {
                entityIds.remove(entityId);
            }
            if (entityIds.isEmpty()) {
                activePairs.remove(uuid);
                Player player = Bukkit.getPlayer(uuid);
                if (player != null)
                    removePlayer(player);
            }
        }
        return count;
    }
}
