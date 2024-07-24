package dev.covector.maplus.mmapihook.mechanics;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.adapters.AbstractVector;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.api.mobs.entities.SpawnReason;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.ITargetedLocationSkill;
import io.lumine.mythic.api.skills.SkillCaster;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.ThreadSafetyLevel;
import io.lumine.mythic.api.skills.placeholders.PlaceholderDouble;
import io.lumine.mythic.api.skills.placeholders.PlaceholderFloat;
import io.lumine.mythic.api.skills.placeholders.PlaceholderInt;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.adapters.BukkitEntityType;
import io.lumine.mythic.bukkit.utils.numbers.Numbers;
import io.lumine.mythic.core.logging.MythicLogger;
import io.lumine.mythic.core.mobs.ActiveMob;
import io.lumine.mythic.core.mobs.MobExecutor;
import io.lumine.mythic.core.skills.SkillExecutor;
import io.lumine.mythic.core.skills.SkillMechanic;
import java.io.File;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;


public class SummonCenter
implements ITargetedEntitySkill,
ITargetedLocationSkill {
    protected MythicMob mm;
    protected BukkitEntityType me;
    protected PlaceholderString strType;
    protected PlaceholderInt amount;
    protected PlaceholderDouble level;
    protected PlaceholderFloat yaw;
    protected PlaceholderFloat pitch;
    protected int noise;
    protected int yNoise;
    protected double force;
    protected double yForce;
    protected boolean summonerIsOwner;
    protected boolean summonerIsParent;
    protected boolean summonerIsFaction;
    protected boolean yUpOnly;
    protected boolean onSurface;
    protected boolean inheritFaction;
    protected boolean inheritThreatTable;
    protected boolean copyThreatTable;

    public SummonCenter(MythicLineConfig mlc) {
        // this.threadSafetyLevel = ThreadSafetyLevel.SYNC_ONLY;
        this.amount = mlc.getPlaceholderInteger(new String[]{"amount", "a"}, 1, new String[0]);
        this.level = mlc.getPlaceholderDouble(new String[]{"level", "l"}, -1.0, new String[0]);
        this.yaw = mlc.getPlaceholderFloat(new String[]{"yaw"}, 0.0f, new String[0]);
        this.pitch = mlc.getPlaceholderFloat(new String[]{"pitch"}, 0.0f, new String[0]);
        this.noise = mlc.getInteger(new String[]{"noise", "n", "radius", "r"}, 0);
        this.yNoise = mlc.getInteger(new String[]{"ynoise", "yn", "yradius", "yr"}, this.noise);
        this.yUpOnly = mlc.getBoolean(new String[]{"yradiusuponly", "yradiusonlyup", "yruo", "yu"}, false);
        this.force = mlc.getDouble(new String[]{"force", "f", "velocity", "v"}, 0.0);
        this.yForce = mlc.getDouble(new String[]{"yforce", "yf", "yvelocity", "yv"}, this.force);
        this.onSurface = mlc.getBoolean(new String[]{"onsurface", "os", "s"}, false);
        this.copyThreatTable = mlc.getBoolean(new String[]{"copythreattable", "ctt"}, false);
        this.inheritFaction = mlc.getBoolean(new String[]{"inheritfaction", "if"}, true);
        this.inheritThreatTable = mlc.getBoolean(new String[]{"inheritthreattable", "itt"}, false);
        this.summonerIsOwner = mlc.getBoolean(new String[]{"summonerisowner", "sio"}, true);
        this.summonerIsParent = mlc.getBoolean(new String[]{"summonerisparent", "sip"}, true);
        this.summonerIsFaction = mlc.getBoolean(new String[]{"summonerisfaction", "sif"}, false);
        if (this.yaw.isStaticallyEqualTo(0.0f)) {
            this.yaw = null;
        }
        if (this.pitch.isStaticallyEqualTo(0.0f)) {
            this.pitch = null;
        }
        this.strType = mlc.getPlaceholderString(new String[]{"type", "t", "mob", "m"}, "SKELETON", new String[0]);
        if (this.strType.isStatic()) {
            ((MythicBukkit)this.getPlugin()).getSkillManager().queueSecondPass(() -> {
                this.mm = ((MythicBukkit)this.getPlugin()).getMobManager().getMythicMob(this.strType.get()).orElse(null);
                if (this.mm == null) {
                    this.me = BukkitEntityType.getMythicEntity(this.strType.get());
                    // if (this.me == null) {
                    //     MythicLogger.errorMechanicConfig(this, mlc, "The 'type' attribute must be a valid MythicMob or MythicEntity type.");
                    // }
                }
            });
        } else {
            this.mm = null;
            this.me = null;
        }
    }

    @Override
    public SkillResult castAtLocation(SkillMetadata data, AbstractLocation target) {
        target = this.roundOff(target);
        int amount = this.amount.get(data);
        double level = this.level.get(data);
        double d = level = level == -1.0 ? data.getCaster().getLevel() : level;
        if (this.mm == null) {
            this.mm = ((MythicBukkit)this.getPlugin()).getMobManager().getMythicMob(this.strType.get(data)).orElse(null);
            if (this.mm == null) {
                this.me = BukkitEntityType.getMythicEntity(this.strType.get(data));
                if (this.me == null) {
                    MythicLogger.error("The 'type' attribute must be a valid MythicMob or MythicEntity type. Spawned from a placeholder summon mechanic, no MLC available. Mob String: " + this.strType.get());
                }
            }
        }
        if (this.mm != null) {
            if (this.noise > 0) {
                for (int i = 1; i <= amount; ++i) {
                    ActiveMob ams2;
                    int height = this.mm.getMythicEntity() == null ? 2 : this.mm.getMythicEntity().getHeight();
                    AbstractLocation l = MobExecutor.findSafeSpawnLocation(target, this.noise, this.yNoise, height, this.yUpOnly, this.onSurface);
                    l = this.roundOff(l);
                    l.setDirection(data.getCaster().getLocation().getDirection());
                    if (this.yaw != null) {
                        l.setYaw(this.yaw.get(data));
                    }
                    if (this.pitch != null) {
                        l.setPitch(this.pitch.get(data));
                    }
                    if ((ams2 = this.mm.spawn(l, level, SpawnReason.SUMMON)) == null) continue;
                    ams2.getEntity().setVelocity(new AbstractVector((Numbers.randomDouble() - 0.5) * this.force, (Numbers.randomDouble() - 0.5) * this.yForce, (Numbers.randomDouble() - 0.5) * this.force));
                    MythicLogger.debug(MythicLogger.DebugLevel.MECHANIC, "Summoning {0} at {1}", this.strType, l.toString());
                    ((MythicBukkit)this.getPlugin()).getEntityManager().registerMob(ams2.getEntity().getWorld(), ams2.getEntity());
                    SkillCaster skillCaster = data.getCaster();
                    if (skillCaster instanceof ActiveMob) {
                        ActiveMob am = (ActiveMob)skillCaster;
                        if (this.summonerIsOwner) {
                            ams2.setParent(am);
                            ams2.setOwner(data.getCaster().getEntity().getUniqueId());
                        }
                        if (this.inheritFaction) {
                            ams2.setFaction(am.getFaction());
                        }
                        if (!am.hasThreatTable()) continue;
                        if (this.copyThreatTable) {
                            try {
                                ams2.importThreatTable(am.getThreatTable().clone());
                                ams2.getThreatTable().targetHighestThreat();
                            } catch (CloneNotSupportedException e1) {
                                e1.printStackTrace();
                            }
                            continue;
                        }
                        if (!this.inheritThreatTable) continue;
                        ams2.importThreatTable(am.getThreatTable());
                        ams2.getThreatTable().targetHighestThreat();
                        continue;
                    }
                    if (!this.summonerIsOwner) continue;
                    ams2.setParent(data.getCaster());
                    ams2.setOwner(data.getCaster().getEntity().getUniqueId());
                }
            } else {
                for (int i = 1; i <= amount; ++i) {
                    ActiveMob ams;
                    target.setDirection(data.getCaster().getLocation().getDirection());
                    if (this.yaw != null) {
                        target.setYaw(this.yaw.get(data));
                    }
                    if (this.pitch != null) {
                        target.setPitch(this.pitch.get(data));
                    }
                    if ((ams = this.mm.spawn(target, level, SpawnReason.SUMMON)) == null) continue;
                    ams.getEntity().setVelocity(new AbstractVector((Numbers.randomDouble() - 0.5) * this.force, (Numbers.randomDouble() - 0.5) * this.yForce, (Numbers.randomDouble() - 0.5) * this.force));
                    MythicLogger.debug(MythicLogger.DebugLevel.MECHANIC, "Summoning {0} at {1}", this.strType, target.toString());
                    ((MythicBukkit)this.getPlugin()).getEntityManager().registerMob(ams.getEntity().getWorld(), ams.getEntity());
                    SkillCaster ams2 = data.getCaster();
                    if (ams2 instanceof ActiveMob) {
                        ActiveMob am = (ActiveMob)ams2;
                        if (this.summonerIsOwner) {
                            ams.setParent(am);
                            ams.setOwner(data.getCaster().getEntity().getUniqueId());
                        }
                        if (this.inheritFaction) {
                            ams.setFaction(am.getFaction());
                        }
                        if (this.copyThreatTable) {
                            try {
                                ams.importThreatTable(am.getThreatTable().clone());
                                ams.getThreatTable().targetHighestThreat();
                            } catch (CloneNotSupportedException e1) {
                                e1.printStackTrace();
                            }
                            continue;
                        }
                        if (!this.inheritThreatTable) continue;
                        ams.importThreatTable(am.getThreatTable());
                        ams.getThreatTable().targetHighestThreat();
                        continue;
                    }
                    if (!this.summonerIsOwner) continue;
                    ams.setParent(data.getCaster());
                    ams.setOwner(data.getCaster().getEntity().getUniqueId());
                }
            }
            return SkillResult.SUCCESS;
        }
        if (this.me != null) {
            if (this.noise > 0) {
                for (int i = 1; i <= amount; ++i) {
                    AbstractLocation l = MobExecutor.findSafeSpawnLocation(target, this.noise, this.yNoise, this.me.getHeight(), this.yUpOnly, this.onSurface);
                    l = this.roundOff(l);
                    Entity entity = this.me.spawn(BukkitAdapter.adapt(l), SpawnReason.SUMMON);
                    entity.setVelocity(new Vector((Numbers.randomDouble() - 0.5) * this.force, (Numbers.randomDouble() - 0.5) * this.yForce, (Numbers.randomDouble() - 0.5) * this.force));
                    MythicLogger.debug(MythicLogger.DebugLevel.MECHANIC, "Summoning {0} at {1} with noise", this.strType, l.toString());
                }
            } else {
                for (int i = 1; i <= amount; ++i) {
                    Entity entity = this.me.spawn(BukkitAdapter.adapt(target), SpawnReason.SUMMON);
                    entity.setVelocity(new Vector((Numbers.randomDouble() - 0.5) * this.force, (Numbers.randomDouble() - 0.5) * this.yForce, (Numbers.randomDouble() - 0.5) * this.force));
                    MythicLogger.debug(MythicLogger.DebugLevel.MECHANIC, "Summoning {0} at {1}", this.strType, target.toString());
                }
            }
            return SkillResult.SUCCESS;
        }
        MythicLogger.error("SummonCenterMechanic: Mob Type {0} not found", this.strType);
        return SkillResult.SUCCESS;
    }

    @Override
    public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
        this.castAtLocation(data, target.getLocation());
        return SkillResult.SUCCESS;
    }

    private AbstractLocation roundOff(AbstractLocation l) {
        return new AbstractLocation(l.getWorld(), Math.floor(l.getX()) + 0.5D, Math.floor(l.getY()) + 0.5D, Math.floor(l.getZ()) + 0.5D, l.getYaw(), l.getPitch());
    }
}

