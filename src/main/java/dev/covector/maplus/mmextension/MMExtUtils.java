package dev.covector.maplus.mmextension;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.Location;

import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.api.skills.Skill;
import io.lumine.mythic.core.skills.SkillExecutor;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.mobs.GenericCaster;
import io.lumine.mythic.api.skills.SkillCaster;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.core.skills.SkillMetadataImpl;
import io.lumine.mythic.core.skills.SkillTriggers;


public class MMExtUtils {
    public static Entity parseUUID(String uuid) {
        try {
            return Bukkit.getEntity(UUID.fromString(uuid));
        } catch (IllegalArgumentException e) {
            // testing on players
            return Bukkit.getPlayer(uuid);
        }
    }

    public static List<String> getLivingEntityTabComplete(String arg) {
        return streamFilter(Bukkit.getOnlinePlayers().stream()
            .map(HumanEntity::getName)
            , arg);
    }

    public static List<String> getLivingEntityTabComplete(String arg, Player player) {
        // also return uuid of closest non-player living entity
        return streamFilter(Stream.concat(
            Bukkit.getOnlinePlayers().stream()
            .map(HumanEntity::getName),
            player.getWorld().getNearbyEntities(player.getLocation(), 10, 10, 10).stream()
                .filter(e -> e instanceof LivingEntity && !(e.getUniqueId().toString().equals(player.getUniqueId().toString())))
                .sorted((e1, e2) -> {
                    return (int) (e1.getLocation().distanceSquared(player.getLocation()) - e2.getLocation().distanceSquared(player.getLocation()));
                })
                .map(Entity::getUniqueId)
                .map(UUID::toString)
        ), arg);
    }

    public static List<String> streamFilter(Stream<String> list, String arg) {
        return list
            .filter(n -> n.toLowerCase().startsWith(arg.toLowerCase()))
            .collect(Collectors.toList());
    }

    public static List<String> streamFilter(Set<String> list, String arg) {
        return streamFilter(list.stream(), arg);
    }

    public static void castMMSkill(LivingEntity caster, String skillName, LivingEntity targetEntity, Location targetLoc) {
        if (caster == null) {
            throw new IllegalArgumentException("caster cannot be null");
        }
        if (skillName == null) {
            throw new IllegalArgumentException("skillName cannot be null");
        }
        if (targetEntity == null && targetLoc == null) {
            throw new IllegalArgumentException("targetEntity and targetLoc cannot both be null");
        }

        SkillExecutor skillManager = MythicBukkit.inst().getSkillManager();

        Optional<Skill> opt = skillManager.getSkill(skillName);
        if (!opt.isPresent()) {
            throw new IllegalArgumentException("skillName is not a valid skill");
        }
        Skill skill = opt.get();
        
        AbstractEntity mmTrigger = BukkitAdapter.adapt(caster);
        SkillCaster mmCaster = new GenericCaster(mmTrigger);

        HashSet<AbstractEntity> targetEntities = new HashSet<>();
        HashSet<AbstractLocation> targetLocations = new HashSet<>();

        if (targetEntity != null)
            targetEntities.add(BukkitAdapter.adapt(targetEntity));
        if (targetLoc != null)
            targetLocations.add(BukkitAdapter.adapt(targetLoc));

        SkillMetadataImpl mmSkillMeta = new SkillMetadataImpl(SkillTriggers.API, mmCaster, mmTrigger, BukkitAdapter.adapt(caster.getEyeLocation()), targetEntities, targetLocations, 1);

        skill.execute(mmSkillMeta);
    }
}