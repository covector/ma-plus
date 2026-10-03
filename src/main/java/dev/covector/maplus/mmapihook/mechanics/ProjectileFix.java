package dev.covector.maplus.mmapihook.mechanics;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.adapters.AbstractVector;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.ITargetedLocationSkill;
import io.lumine.mythic.api.skills.Skill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.utils.numbers.Numbers;
import io.lumine.mythic.core.logging.MythicLogger;
import io.lumine.mythic.core.skills.SkillExecutor;
import io.lumine.mythic.core.skills.placeholders.PlaceholderMeta;
import io.lumine.mythic.core.skills.projectiles.Projectile;
import io.lumine.mythic.core.skills.projectiles.ProjectileBulletType;
import io.lumine.mythic.core.skills.projectiles.ProjectileSurfaceMode;
import io.lumine.mythic.core.utils.BlockUtil;
import io.lumine.mythic.core.utils.MythicUtil;
import io.lumine.mythic.core.utils.annotations.MythicField;
import io.lumine.mythic.core.utils.annotations.MythicFields;
import io.lumine.mythic.core.utils.annotations.MythicMechanic;
import io.lumine.mythic.core.utils.physics.CollisionHelper;
import io.lumine.mythic.core.utils.physics.PhysicsCollision;
import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;

@MythicMechanic(
   author = "Ashijin",
   name = "projectilefix",
   aliases = {"pfix"},
   description = "Launches a custom projectile at the target"
)
public class ProjectileFix extends Projectile implements ITargetedEntitySkill, ITargetedLocationSkill {
   @MythicField(
      name = "type",
      description = "The type of the projectile, NORMAL or METEOR",
      defValue = "NORMAL"
   )
   protected ProjectileType type;
   @MythicField(
      name = "gravity",
      aliases = {"g"},
      description = "The gravity modifier the projectile uses",
      defValue = "0"
   )
   protected float projectileGravity;
   @MythicField(
      name = "bounces",
      aliases = {"bounce"},
      description = "The projectile can bounce",
      defValue = "false",
      premium = true
   )
   protected boolean bounce;
   @MythicField(
      name = "bounceVelocity",
      aliases = {"bv"},
      description = "The velocity modifier of the bounce",
      defValue = "0.9"
   )
   protected float bounceVelocityMod;
   @MythicField(
      name = "highaccuracymode",
      aliases = {"ham"},
      description = "Whether to use high-accuracy mode, which raytraces every tick to ensure the projectile cannot ever go anything",
      defValue = "PLAYERS_ONLY"
   )
   protected Projectile.ProjectileTriOption highAccuracyMode;
   @MythicFields({@MythicField(
   name = "hugSurface",
   aliases = {"hs"},
   description = "Whether the projectile will hug the surface",
   defValue = "false"
), @MythicField(
   name = "hugLiquid",
   aliases = {"hugWater", "hugLava"},
   description = "If hugSurface is set, determines whether the projectile will hug liquid",
   defValue = "false"
)})
   protected ProjectileSurfaceMode surfaceMode;
   @MythicField(
      name = "heightFromSurface",
      aliases = {"hfs"},
      description = "    The offset depends on the type of the projectile and if hugSurface is set.\n    NORMAL - how high above the surface the projectile will glide.\n    METEOR - how high above the surface the projectile starts above the target\n",
      defValue = "0.5"
   )
   protected float heightFromSurface;
   @MythicField(
      name = "maxClimbHeight",
      aliases = {"mch"},
      description = "The number of attempts the projectile will try to increase its y-location before terminating the projectile",
      defValue = "3"
   )
   protected float maxClimbHeight;
   @MythicField(
      name = "maxDropHeight",
      aliases = {"mdh"},
      description = "The number of attempts the projectile will try to decrease its y-location before terminating the projectile",
      defValue = "10"
   )
   protected float maxDropHeight;

   public ProjectileFix(SkillExecutor manager, File file, String skill, MythicLineConfig mlc) {
      super(manager, file, skill, mlc);
      this.surfaceMode = ProjectileSurfaceMode.NONE;
      String type = mlc.getString("type", "NORMAL");
      this.type = ProjectileFix.ProjectileType.valueOf(type.toUpperCase());
      this.projectileGravity = mlc.getFloat(new String[]{"gravity", "g"}, 0.0F);
      this.bounce = mlc.getBoolean(new String[]{"bounces", "bounce"}, false);
      this.bounceVelocityMod = mlc.getFloat(new String[]{"bouncevelocity", "bv"}, 0.9F);
      boolean hugSurface = mlc.getBoolean(new String[]{"hugsurface", "hs"}, false);
      this.heightFromSurface = mlc.getFloat(new String[]{"heightfromsurface", "hfs"}, 0.5F);
      this.maxClimbHeight = mlc.getFloat(new String[]{"maxclimbheight", "mch"}, 3.0F);
      this.maxDropHeight = mlc.getFloat(new String[]{"maxdropheight", "mdh"}, 10.0F);
      String highAccuracyDefault = "PLAYERS_ONLY";
      if (hugSurface) {
         highAccuracyDefault = "FALSE";
         boolean hugWater = mlc.getBoolean(new String[]{"hugliquid", "hugwater", "huglava"}, false);
         if (hugWater) {
            this.surfaceMode = ProjectileSurfaceMode.WATER;
         } else {
            this.surfaceMode = ProjectileSurfaceMode.SURFACE;
         }
      }

      String highAccuracyMode = mlc.getString(new String[]{"highaccuracymode", "ham"}, highAccuracyDefault);

      try {
         this.highAccuracyMode = Projectile.ProjectileTriOption.valueOf(highAccuracyMode.toUpperCase());
      } catch (Throwable var10) {
         MythicLogger.errorMechanic(this, "Invalid input for highAccuracyMode option '" + highAccuracyMode + "'");
         this.highAccuracyMode = Projectile.ProjectileTriOption.PLAYERS_ONLY;
      }

   }

   public SkillResult castAtLocation(SkillMetadata data, AbstractLocation target) {
      try {
         new ProjectileFixTracker(data, target.clone().add((double)0.0F, this.targetYOffset.get((PlaceholderMeta)data), (double)0.0F));
         return SkillResult.SUCCESS;
      } catch (Exception ex) {
         MythicLogger.error((String)"An error occurred executing a Projectile Mechanic", (Throwable)ex);
         return SkillResult.ERROR;
      }
   }

   public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
      return this.castAtLocation(data, target.getLocation().add((double)0.0F, target.getEyeHeight() / (double)2.0F, (double)0.0F));
   }

   public ProjectileSurfaceMode getSurfaceMode() {
      return this.surfaceMode;
   }

   protected static enum ProjectileType {
      NORMAL,
      METEOR;

      // $FF: synthetic method
      private static ProjectileType[] $values() {
         return new ProjectileType[]{NORMAL, METEOR};
      }
   }

   public class ProjectileFixTracker extends Projectile.ProjectileTracker {
      private float gravity = 0.0F;
      private float bounciness = 0.0F;
      private AbstractLocation target;

      public ProjectileFixTracker(SkillMetadata data, AbstractLocation target) {
         super(data, target);
         this.target = target;
         if (ProjectileFix.this.bounce) {
            this.bounciness = ProjectileFix.this.projectileVelocity.get((PlaceholderMeta)data);
         }

         this.start();
      }

      public void projectileStart() {
         if (ProjectileFix.this.type == ProjectileFix.ProjectileType.METEOR) {
            this.startLocation = this.target.clone();
            this.startLocation.add((double)0.0F, (double)ProjectileFix.this.heightFromSurface, (double)0.0F);
            if (ProjectileFix.this.projectileGravity <= 0.0F) {
               this.gravity = ProjectileFix.this.projectileVelocity.get((PlaceholderMeta)this.data);
               this.gravity = this.gravity > 0.0F ? this.gravity / ProjectileFix.this.ticksPerSecond : 0.0F;
            } else {
               this.gravity = ProjectileFix.this.projectileGravity > 0.0F ? ProjectileFix.this.projectileGravity / ProjectileFix.this.ticksPerSecond : 0.0F;
            }

            // use horizontal offset for spawn location noise
            if (ProjectileFix.this.projectileVelocityHorizNoise > 0.0F) {
               AbstractVector noise = new AbstractVector(0, 0, 0);
               noise.setX(((double)ProjectileFix.this.projectileVelocityHorizNoiseBase + Numbers.randomDouble() * (double)ProjectileFix.this.projectileVelocityHorizNoise));
               noise.setZ(((double)ProjectileFix.this.projectileVelocityHorizNoiseBase + Numbers.randomDouble() * (double)ProjectileFix.this.projectileVelocityHorizNoise));
               this.startLocation.add(noise);
            }

            this.velocityMagnitude = (double)0.0F;
         } else {
            if (ProjectileFix.this.sourceIsOrigin) {
               this.startLocation = this.data.getOrigin().clone();
            } else {
               this.startLocation = this.data.getCaster().getEntity().getLocation().clone();
            }

            this.velocityMagnitude = (double)(ProjectileFix.this.projectileVelocity.get((PlaceholderMeta)this.data) / ProjectileFix.this.ticksPerSecond);
            this.gravity = ProjectileFix.this.projectileGravity > 0.0F ? ProjectileFix.this.projectileGravity / ProjectileFix.this.ticksPerSecond : 0.0F;
            if (ProjectileFix.this.tickInterpolation > 0) {
               this.velocityMagnitude /= (double)(ProjectileFix.this.tickInterpolation + 1);
               this.gravity /= (float)(ProjectileFix.this.tickInterpolation + 1);
            }

            double syo = ProjectileFix.this.startYOffset.get((PlaceholderMeta)this.data);
            if (syo != (double)0.0F) {
               this.startLocation.setY(this.startLocation.getY() + syo);
            }

            double sfo = (double)(ProjectileFix.this.startForwardOffset.get((PlaceholderMeta)this.data) * -1.0F);
            if (sfo != (double)0.0F) {
               this.startLocation = MythicUtil.move(this.startLocation, sfo, (double)0.0F, (double)0.0F);
            }

            double sso = ProjectileFix.this.startSideOffset.get((PlaceholderMeta)this.data);
            if (sso != (double)0.0F) {
               this.startLocation = MythicUtil.move(this.startLocation, (double)0.0F, (double)0.0F, sso);
            }

            double eso = ProjectileFix.this.endSideOffset.get((PlaceholderMeta)this.data);
            if (eso != (double)0.0F) {
               this.target.setDirection(this.startLocation.getDirection());
               this.target = MythicUtil.move(this.target, (double)0.0F, (double)0.0F, eso);
            }
         }

         this.previousLocation = this.startLocation.clone();
         this.currentLocation = this.startLocation.clone();
         if (this.currentLocation != null) {
            this.currentVelocity = this.target.toVector().rotate(0.001F).subtract(this.currentLocation.toVector()).normalize();
            if (ProjectileFix.this.projectileVelocityHorizOffset.get((PlaceholderMeta)this.data) != 0.0F || ProjectileFix.this.projectileVelocityHorizNoise > 0.0F) {
               float noise = 0.0F;
               if (ProjectileFix.this.projectileVelocityHorizNoise > 0.0F) {
                  noise = (float)((double)ProjectileFix.this.projectileVelocityHorizNoiseBase + Numbers.randomDouble() * (double)ProjectileFix.this.projectileVelocityHorizNoise);
               }

               this.currentVelocity.rotate(ProjectileFix.this.projectileVelocityHorizOffset.get((PlaceholderMeta)this.data) + noise);
            }

            if (ProjectileFix.this.projectileVelocityVertOffset.get((PlaceholderMeta)this.data) != 0.0F || ProjectileFix.this.projectileVelocityVertNoise > 0.0F) {
               float noise = 0.0F;
               if (ProjectileFix.this.projectileVelocityVertNoise > 0.0F) {
                  noise = (float)((double)ProjectileFix.this.projectileVelocityVertNoiseBase + Numbers.randomDouble() * (double)ProjectileFix.this.projectileVelocityVertNoise);
               }

               this.currentVelocity.add(new AbstractVector(0.0F, ProjectileFix.this.projectileVelocityVertOffset.get((PlaceholderMeta)this.data) + noise, 0.0F)).normalize();
            }

            if (ProjectileFix.this.surfaceMode != ProjectileSurfaceMode.NONE) {
               this.currentLocation.setY((double)((float)((int)this.currentLocation.getY()) + ProjectileFix.this.heightFromSurface));
               this.currentVelocity.setY(0).normalize();
            }

            if (ProjectileFix.this.powerAffectsVelocity) {
               this.currentVelocity.multiply(this.power);
            }

            this.currentVelocity.multiply(this.velocityMagnitude);
            if (ProjectileFix.this.projectileGravity > 0.0F) {
               this.currentVelocity.setY(this.currentVelocity.getY() - (double)this.gravity);
            }

            if (ProjectileFix.this.bullet != null) {
               this.bullet = ProjectileFix.this.bullet.create(this, (AbstractEntity)null);
            }

         }
      }

      public void setVelocity(float value) {
         this.velocityMagnitude = (double)(value / ProjectileFix.this.ticksPerSecond);
         if (ProjectileFix.this.tickInterpolation > 0) {
            this.velocityMagnitude /= (double)(ProjectileFix.this.tickInterpolation + 1);
         }

         this.currentVelocity = this.currentVelocity.normalize().multiply(this.velocityMagnitude);
      }

      public void multiplyVelocity(float v) {
         this.velocityMagnitude *= (double)v;
         this.currentVelocity = this.currentVelocity.multiply(v);
      }

      public void addVelocity(float v) {
         if (ProjectileFix.this.tickInterpolation > 0) {
            v /= (float)(ProjectileFix.this.tickInterpolation + 1);
         }

         this.velocityMagnitude = (this.velocityMagnitude * (double)ProjectileFix.this.ticksPerSecond + (double)v) / (double)ProjectileFix.this.ticksPerSecond;
         if (this.currentVelocity.length() != (double)0.0F) {
            AbstractVector normalizedVelocity = this.currentVelocity.clone().normalize();
            AbstractVector flatAmountVector = normalizedVelocity.multiply(v);
            this.currentVelocity.add(flatAmountVector);
         }

      }

      public void setGravity(float p) {
         if (ProjectileFix.this.tickInterpolation > 0) {
            p /= (float)(ProjectileFix.this.tickInterpolation + 1);
         }

         this.gravity = p;
      }

      public void multiplyGravity(float p) {
         this.gravity *= p;
      }

      public void addGravity(float g) {
         if (ProjectileFix.this.tickInterpolation > 0) {
            g /= (float)(ProjectileFix.this.tickInterpolation + 1);
         }

         this.gravity += g;
      }

      private boolean isHighAccuracy() {
         return ProjectileFix.this.highAccuracyMode == Projectile.ProjectileTriOption.TRUE || ProjectileFix.this.highAccuracyMode == Projectile.ProjectileTriOption.PLAYERS_ONLY && this.data.getCaster().getEntity().isPlayer();
      }

      public void projectileMove() {
         this.previousLocation = this.currentLocation.clone();
         this.currentLocation.add(this.currentVelocity);
         if (this.isHighAccuracy()) {
            RayTraceResult traceResult = ProjectileFix.this.getPlugin().getVolatileCodeHandler().getWorldHandler().rayTraceBlock(this.previousLocation, this.currentLocation, FluidCollisionMode.NEVER, true);
            if (traceResult != null && traceResult.getHitBlock() != null && !traceResult.getHitBlock().isEmpty()) {
               AbstractLocation hitLocation = BukkitAdapter.adapt(traceResult.getHitPosition()).toLocation(this.currentLocation.getWorld());
               Location from = BukkitAdapter.adapt(this.previousLocation);
               Location to = BukkitAdapter.adapt(hitLocation);
               if (this.previousLocation.distanceSquared(hitLocation) <= this.previousLocation.distanceSquared(this.currentLocation)) {
                  if (ProjectileFix.this.stopOnHitGround) {
                     this.currentLocation = hitLocation;
                  } else if (ProjectileFix.this.onHitBlockSkill.isPresent() && ((Skill)ProjectileFix.this.onHitBlockSkill.get()).isUsable(this.data)) {
                     SkillMetadata sData = this.data.deepClone();
                     AbstractLocation location;
                     if (ProjectileFix.this.bulletType.isPresent() && ProjectileFix.this.bulletType.get() == ProjectileBulletType.ARROW) {
                        location = this.previousLocation.clone();
                     } else {
                        location = this.currentLocation.clone();
                     }

                     ((Skill)ProjectileFix.this.onHitBlockSkill.get()).execute(sData.setOrigin(location).setLocationTarget(location));
                  }
               }
            }
         }

         if (ProjectileFix.this.surfaceMode != ProjectileSurfaceMode.NONE) {
            if (this.currentLocation.getBlockX() != this.previousLocation.getBlockX() || this.currentLocation.getBlockZ() != this.previousLocation.getBlockZ()) {
               Block b = BukkitAdapter.adapt(this.currentLocation).subtract((double)0.0F, (double)ProjectileFix.this.heightFromSurface, (double)0.0F).getBlock();
               if (BlockUtil.isPathable(b, ProjectileFix.this.surfaceMode)) {
                  int attempts = 0;
                  boolean ok = false;

                  while((float)(attempts++) < ProjectileFix.this.maxDropHeight) {
                     b = b.getRelative(BlockFace.DOWN);
                     if (!BlockUtil.isPathable(b, ProjectileFix.this.surfaceMode)) {
                        ok = true;
                        break;
                     }

                     this.currentLocation.subtract((double)0.0F, (double)1.0F, (double)0.0F);
                  }

                  if (!ok) {
                     this.terminate();
                     return;
                  }
               } else {
                  int attempts = 0;
                  boolean ok = false;

                  while((float)(attempts++) < ProjectileFix.this.maxClimbHeight) {
                     b = b.getRelative(BlockFace.UP);
                     this.currentLocation.add((double)0.0F, (double)1.0F, (double)0.0F);
                     if (BlockUtil.isPathable(b)) {
                        ok = true;
                        break;
                     }
                  }

                  if (!ok) {
                     this.terminate();
                     return;
                  }
               }

               this.currentLocation.setY((double)((float)((int)this.currentLocation.getY()) + ProjectileFix.this.heightFromSurface));
            }
         } else if (ProjectileFix.this.projectileGravity != 0.0F) {
            this.currentVelocity.setY(this.currentVelocity.getY() - (double)(ProjectileFix.this.projectileGravity / ProjectileFix.this.ticksPerSecond));
         }

         if (ProjectileFix.this.bounce && MythicBukkit.isVolatile()) {
            try {
               if (this.handleBounce()) {
                  this.executeProjectileSkill(ProjectileFix.this.onBounceSkill, this.data, false);
               }
            } catch (IllegalArgumentException ex) {
               MythicLogger.errorMechanicConfig(ProjectileFix.this, ProjectileFix.this.config, "An error occurred while calculating projectile bounce (did you set the bounding box to zero?)");
               ex.printStackTrace();
            }
         } else if (ProjectileFix.this.stopOnHitGround && !BlockUtil.isPathable(BukkitAdapter.adapt(this.currentLocation).getBlock(), (Projectile.ProjectileTracker)this)) {
            if (ProjectileFix.this.onHitBlockSkill.isPresent() && ((Skill)ProjectileFix.this.onHitBlockSkill.get()).isUsable(this.data)) {
               SkillMetadata sData = this.data.deepClone();
               AbstractLocation location;
               if (ProjectileFix.this.bulletType.isPresent() && ProjectileFix.this.bulletType.get() == ProjectileBulletType.ARROW) {
                  location = this.previousLocation.clone();
               } else {
                  location = this.currentLocation.clone();
               }

               ((Skill)ProjectileFix.this.onHitBlockSkill.get()).execute(sData.setOrigin(location).setLocationTarget(location));
            }

            this.currentLocation = this.previousLocation;
            this.terminate();
            return;
         }

      }

      public void projectileTick() {
         if (ProjectileFix.this.onTickSkill.isPresent() && ((Skill)ProjectileFix.this.onTickSkill.get()).isUsable(this.data)) {
            SkillMetadata sData = this.data.deepClone();
            AbstractLocation location;
            if (ProjectileFix.this.bulletType.isPresent() && ProjectileFix.this.bulletType.get() == ProjectileBulletType.ARROW) {
               location = this.previousLocation.clone();
            } else {
               location = this.currentLocation.clone();
            }

            HashSet<AbstractLocation> targets = new HashSet();
            targets.add(location);
            sData.setLocationTargets(targets);
            sData.setOrigin(location);
            ((Skill)ProjectileFix.this.onTickSkill.get()).execute(sData);
         }

         this.evaluateTargetsInBB();
         if (!this.targets.isEmpty()) {
            this.doHit((Collection)this.targets.clone());
            if (ProjectileFix.this.stopOnHitEntity) {
               this.terminate();
            }
         }

         this.targets.clear();
      }

      private void doHit(Collection<AbstractEntity> targets) {
         if (ProjectileFix.this.onHitSkill.isPresent()) {
            SkillMetadata sData = this.data.deepClone();
            sData.setEntityTargets(targets);
            sData.setOrigin(this.currentLocation.clone());
            if (((Skill)ProjectileFix.this.onHitSkill.get()).isUsable(sData)) {
               ((Skill)ProjectileFix.this.onHitSkill.get()).execute(sData);
            }
         }

      }

      public void setCancelled() {
         this.terminate();
      }

      public boolean getCancelled() {
         return this.components.hasTerminated();
      }

      public boolean handleBounce() {
         BoundingBox bb = BoundingBox.of(BukkitAdapter.adapt(this.currentLocation), (double)ProjectileFix.this.hitRadius.get((PlaceholderMeta)this.data), (double)ProjectileFix.this.verticalHitRadius, (double)ProjectileFix.this.hitRadius.get((PlaceholderMeta)this.data));
         BlockFace collided = null;
         double bV = (double)0.0F;

         for(PhysicsCollision collision : CollisionHelper.getCollisions(this.currentVelocity, bb, this.previousLocation)) {
            switch (collision.getBlockFace()) {
               case NORTH:
                  double magnitude1 = Math.abs(this.currentVelocity.getZ());
                  if (magnitude1 > bV) {
                     bV = magnitude1;
                     collided = BlockFace.NORTH;
                  }
                  break;
               case EAST:
                  double magnitude2 = Math.abs(this.currentVelocity.getZ());
                  if (magnitude2 > bV) {
                     bV = magnitude2;
                     collided = BlockFace.EAST;
                  }
                  break;
               case SOUTH:
                  double magnitude3 = Math.abs(this.currentVelocity.getZ());
                  if (magnitude3 > bV) {
                     bV = magnitude3;
                     collided = BlockFace.SOUTH;
                  }
                  break;
               case WEST:
                  double magnitude4 = Math.abs(this.currentVelocity.getZ());
                  if (magnitude4 > bV) {
                     bV = magnitude4;
                     collided = BlockFace.WEST;
                  }
                  break;
               case UP:
                  double magnitude5 = Math.abs(this.currentVelocity.getY());
                  if (magnitude5 > bV) {
                     bV = magnitude5;
                     collided = BlockFace.UP;
                  }
                  break;
               case DOWN:
                  double magnitude6 = Math.abs(this.currentVelocity.getY());
                  if (magnitude6 > bV) {
                     bV = magnitude6;
                     collided = BlockFace.DOWN;
                  }
            }
         }

         if (collided == null) {
            return false;
         } else {
            switch (collided) {
               case NORTH:
                  if (this.currentVelocity.getZ() > (double)0.0F) {
                     this.currentVelocity.setZ(this.currentVelocity.getZ() * -0.995);
                  }
                  break;
               case EAST:
                  if (this.currentVelocity.getX() < (double)0.0F) {
                     this.currentVelocity.setX(this.currentVelocity.getX() * -0.995);
                  }
                  break;
               case SOUTH:
                  if (this.currentVelocity.getZ() < (double)0.0F) {
                     this.currentVelocity.setZ(this.currentVelocity.getZ() * -0.995);
                  }
                  break;
               case WEST:
                  if (this.currentVelocity.getX() > (double)0.0F) {
                     this.currentVelocity.setX(this.currentVelocity.getX() * -0.995);
                  }
                  break;
               case UP:
                  if (this.currentVelocity.getY() < (double)0.0F) {
                     if (!(this.currentVelocity.getY() <= (double)(this.gravity * -1.0F))) {
                        this.currentVelocity.setY(0);
                        return false;
                     }

                     this.currentVelocity.setY(this.currentVelocity.getY() * -0.8);
                  }
                  break;
               case DOWN:
                  if (this.currentVelocity.getY() > (double)0.0F) {
                     this.currentVelocity.setY(this.currentVelocity.getY() * -0.95);
                  }
            }

            this.currentVelocity.multiply(ProjectileFix.this.bounceVelocityMod);
            return true;
         }
      }
   }
}