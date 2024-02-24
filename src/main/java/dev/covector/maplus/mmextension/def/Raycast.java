package dev.covector.maplus.mmextension.def;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import dev.covector.maplus.mmextension.MMExtUtils;

import org.bukkit.Location;
import org.bukkit.FluidCollisionMode;
import org.bukkit.util.RayTraceResult;
import org.bukkit.entity.ArmorStand;
import org.bukkit.ChatColor;

public abstract class Raycast extends DefaultParamAbility {
    private String syntax = "caster:<caster-uuid> onHitEntity:<entity-hit-callback-skill> onHeadShot:<headshot-callback-skill> onHitBlock:<block-hit-callback-skill> onHitNothing:<nothing-hit-callback-skill> hitBlockCenter:<boolean> hitAirOnMaxDistance:<boolean> hitBlockMode:<DEFAULT|EXCLUDE_ENTITY|ALWAYS> piercing:<int> raySize:<double> maxDistance:<double> fluidCollisionMode:<NEVER|SOURCE_ONLY|ALWAYS> ignorePassableBlocks:<boolean> sourceOffsetX:<double> sourceOffsetY:<double> sourceOffsetZ:<double> sourceOffsetGlobalY:<boolean> sourceOffsetXIgnorePitch:<boolean> sourceOffsetRelative:<boolean> filterMode:<ALL|PLAYER_ONLY|NOT_PLAYER> debug:<boolean>";

    public Raycast() {
        setDefault("caster", null, livingEntityTabOptions);
        setDefault("onHitEntity", null);
        setDefault("onHeadShot", null);
        setDefault("onHitBlock", null);
        setDefault("onHitNothing", null);
        setDefault("hitBlockCenter", "false", booleanTabOptions);
        setDefault("hitAirOnMaxDistance", "false", booleanTabOptions);
        setDefault("hitBlockMode", "DEFAULT", List.of("DEFAULT", "EXCLUDE_ENTITY", "ALWAYS"));
        setDefault("piercing", "0");
        setDefault("raySize", "0");
        setDefault("maxDistance", "20");
        setDefault("fluidCollisionMode", "NEVER", List.of("NEVER", "SOURCE_ONLY", "ALWAYS"));
        setDefault("ignorePassableBlocks", "true", booleanTabOptions);
        setDefault("hitEntityBehindWalls", "false", booleanTabOptions);
        setDefault("sourceOffsetX", "0");
        setDefault("sourceOffsetY", "0");
        setDefault("sourceOffsetZ", "0");
        setDefault("sourceOffsetGlobalY", "true", booleanTabOptions);
        setDefault("sourceOffsetXIgnorePitch", "false", booleanTabOptions);
        setDefault("sourceOffsetRelative", "true", booleanTabOptions);
        setDefault("filterMode", "ALL", List.of("ALL", "PLAYER_ONLY", "NOT_PLAYER"));
        // setDefault("headShotMargin", "0.5");
    }

    public String raycast(ParsedParam parsedParam, Vector direction) {
        Entity caster = getParam(parsedParam, "caster") == null ? null : MMExtUtils.parseUUID(getParam(parsedParam, "caster"));
        if (!(caster instanceof LivingEntity)) {
            return "caster must be a living entity";
        }
        LivingEntity casterMob = (LivingEntity) caster;
        HitBlockMode hitBlockMode = HitBlockMode.valueOf(getParam(parsedParam, "hitBlockMode"));
        boolean hitBlockCenter = getBoolean(parsedParam, "hitBlockCenter");
        boolean hitAirOnMaxDistance = getBoolean(parsedParam, "hitAirOnMaxDistance");
        int hitLimit = getInt(parsedParam, "piercing") + 1;
        double raySize = getDouble(parsedParam, "raySize");
        double maxDistance = getDouble(parsedParam, "maxDistance");
        FluidCollisionMode fluidCollisionMode = FluidCollisionMode.valueOf(getParam(parsedParam, "fluidCollisionMode"));
        boolean ignorePassableBlocks = getBoolean(parsedParam, "ignorePassableBlocks");
        boolean hitEntityBehindWalls = getBoolean(parsedParam, "hitEntityBehindWalls");
        double sourceOffsetX = getDouble(parsedParam, "sourceOffsetX");
        double sourceOffsetY = getDouble(parsedParam, "sourceOffsetY");
        double sourceOffsetZ = getDouble(parsedParam, "sourceOffsetZ");
        boolean sourceOffsetGlobalY = getBoolean(parsedParam, "sourceOffsetGlobalY");
        boolean sourceOffsetXIgnorePitch = getBoolean(parsedParam, "sourceOffsetXIgnorePitch");
        boolean sourceOffsetRelative = getBoolean(parsedParam, "sourceOffsetRelative");
        FilterMode filterMode = FilterMode.valueOf(getParam(parsedParam, "filterMode"));
        // double headShotMargin = getDouble(parsedParam, "headShotMargin");

        Location location = casterMob.getEyeLocation().clone();
        if (sourceOffsetRelative) {
            double cosYaw = Math.cos(casterMob.getEyeLocation().getYaw() * Math.PI / 180);
            double sinYaw = Math.sin(casterMob.getEyeLocation().getYaw() * Math.PI / 180);
            double cosNegPitch = Math.cos(-1 * casterMob.getEyeLocation().getPitch() * Math.PI / 180);
            double sinNegPitch = Math.sin(-1 * casterMob.getEyeLocation().getPitch() * Math.PI / 180);
            
            Vector newX = sourceOffsetXIgnorePitch ? (new Vector(-sinYaw, 0, cosYaw)).multiply(sourceOffsetZ) : casterMob.getEyeLocation().getDirection().multiply(sourceOffsetX);
            Vector newY = sourceOffsetGlobalY ? new Vector(0, sourceOffsetY, 0) : (new Vector(sinNegPitch * sinYaw, cosNegPitch, sinNegPitch * -1 * cosYaw)).multiply(sourceOffsetY);
            Vector newZ = (new Vector(cosYaw, 0, sinYaw)).multiply(sourceOffsetZ);

            location.add(newX).add(newY).add(newZ);
        } else {
            location.add(sourceOffsetX, sourceOffsetY, sourceOffsetZ);
        }

        boolean hitSomething = false;
        boolean hasPiercingLeft = false;
        Entity[] hitEntities = new Entity[hitLimit];
        for (int i = 0; i < hitLimit; i++) {

            RayTraceResult entityray = null;
            if (hitEntityBehindWalls) {
                entityray = casterMob.getWorld().rayTraceEntities(
                location,
                direction,
                maxDistance,
                raySize,
                e -> (e instanceof LivingEntity &&
                    !(e.getUniqueId().toString().equals(casterMob.getUniqueId().toString())) &&
                    !(e instanceof ArmorStand || isInArray(hitEntities, e)) &&
                    (filterMode == FilterMode.ALL || (filterMode == FilterMode.PLAYER_ONLY && e instanceof Player) || (filterMode == FilterMode.NOT_PLAYER && !(e instanceof Player))))
                );
            } else {
                entityray = casterMob.getWorld().rayTrace(
                location,
                direction,
                maxDistance,
                fluidCollisionMode,
                ignorePassableBlocks,
                raySize,
                e -> (e instanceof LivingEntity &&
                    !(e.getUniqueId().toString().equals(casterMob.getUniqueId().toString())) &&
                    !(e instanceof ArmorStand || isInArray(hitEntities, e)) &&
                    (filterMode == FilterMode.ALL || (filterMode == FilterMode.PLAYER_ONLY && e instanceof Player) || (filterMode == FilterMode.NOT_PLAYER && !(e instanceof Player))))
                );
            }
            if (entityray == null) {
                hasPiercingLeft = true;
                break;
            }
            Entity entity = entityray.getHitEntity();
            if (entity == null) {
                hasPiercingLeft = true;
                break;
            }
            hitEntities[i] = entity;
            hitSomething = true;

            LivingEntity livingEntity = (LivingEntity) entity;
            
            if (getParam(parsedParam, "onHeadShot") != null) {
                // if (entityray.getHitPosition().distance(livingEntity.getEyeLocation().toVector()) < headShotMargin) {
                HeadShotDetection.HeadShotResult headShot = HeadShotDetection.isHeadShot(livingEntity, entityray.getHitPosition(), raySize);
                // Bukkit.broadcastMessage("distance: " + headShot.getDistance());
                if (getBoolean(parsedParam, "debug") && casterMob instanceof Player) {
                    ((Player) casterMob).sendMessage(ChatColor.AQUA + "Headshot distance: " + headShot.getDistance());
                }
                if (headShot.isHeadShot()) {
                    // headshot
                    // Bukkit.broadcastMessage("headshot");
                    if (getBoolean(parsedParam, "debug") && casterMob instanceof Player) {
                        ((Player) casterMob).sendMessage(ChatColor.GREEN + "Headshot!");
                    }
                    MMExtUtils.castMMSkill(casterMob, getParam(parsedParam, "onHeadShot"), livingEntity, entityray.getHitPosition().toLocation(casterMob.getWorld()));
                } else {
                    // bodyshot
                    if (getParam(parsedParam, "onHitEntity") != null) {
                        MMExtUtils.castMMSkill(casterMob, getParam(parsedParam, "onHitEntity"), livingEntity, entityray.getHitPosition().toLocation(casterMob.getWorld()));
                    }

                }
            } else {
                // bodyshot
                if (getParam(parsedParam, "onHitEntity") != null) {
                    MMExtUtils.castMMSkill(casterMob, getParam(parsedParam, "onHitEntity"), livingEntity, entityray.getHitPosition().toLocation(casterMob.getWorld()));
                }
            }
        }

        switch (hitBlockMode) {
            case EXCLUDE_ENTITY:
                if (hitSomething) {
                    return null;
                }
            case DEFAULT:
                if (!hasPiercingLeft) {
                    if (!hitSomething && getParam(parsedParam, "onHitNothing") != null) {
                        MMExtUtils.castMMSkill(casterMob, getParam(parsedParam, "onHitNothing"), casterMob, null);
                    }
                    return null;
                }
        }

        RayTraceResult blockray = casterMob.getWorld().rayTraceBlocks(
            location,
            direction,
            maxDistance,
            fluidCollisionMode,
            ignorePassableBlocks
            );
        if (blockray == null) {
            if (hitAirOnMaxDistance) {
                MMExtUtils.castMMSkill(casterMob, getParam(parsedParam, "onHitBlock"), null, location.clone().add(direction.clone().multiply(maxDistance)));
            } else if (getParam(parsedParam, "onHitNothing") != null) {
                MMExtUtils.castMMSkill(casterMob, getParam(parsedParam, "onHitNothing"), casterMob, null);
            }
            return null;
        }
        
        if (blockray != null && getParam(parsedParam, "onHitBlock") != null) {
            MMExtUtils.castMMSkill(casterMob, getParam(parsedParam, "onHitBlock"), null, hitBlockCenter ?
                blockray.getHitBlock().getLocation().add(0.5, 0.5, 0.5) :
                blockray.getHitPosition().toLocation(casterMob.getWorld()));
        }
        return null;
    }

    private boolean isInArray(Entity[] entities, Entity entity) {
        String uuid = entity.getUniqueId().toString();
        for (Entity e : entities) {
            if (e == null) {
                return false;
            }
            if (uuid.equals(e.getUniqueId().toString())) {
                return true;
            }
        }
        return false;
    }

    public String getSyntax() {
        return syntax;
    }

    enum HitBlockMode {
        DEFAULT,
        EXCLUDE_ENTITY,
        ALWAYS
    }

    enum FilterMode {
        ALL,
        PLAYER_ONLY,
        NOT_PLAYER
    }
}
