package dev.covector.maplus.mmapihook.conditions;

import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.util.RayTraceResult;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.conditions.IEntityCondition;
import io.lumine.mythic.bukkit.utils.numbers.RangedDouble;
import io.lumine.mythic.core.utils.annotations.MythicField;

public class BlocksAboveGround 
implements IEntityCondition {
    @MythicField(name = "height", aliases = {"h"}, description = "The height to check for")
    private RangedDouble height;
  
    public BlocksAboveGround(MythicLineConfig mlc, String conditionVar) {
        this.height = new RangedDouble(mlc.getString(new String[] { "height", "h" }, conditionVar, new String[0]));
    }
 
    public boolean check(AbstractEntity e) {
        Location loc = e.getBukkitEntity().getLocation();
        RayTraceResult ray = loc.getWorld().rayTraceBlocks(loc, BlockFace.DOWN.getDirection(), 10, FluidCollisionMode.NEVER, true);
        if (ray == null) return false;
        return this.height.equals(Double.valueOf(loc.getY() - ray.getHitPosition().getY()));
    }
}
