package dev.covector.maplus.mmapihook.mechanics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Rotation;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.CommandBlock;
import org.bukkit.block.Container;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Bed;
import org.bukkit.block.data.type.BigDripleaf;
import org.bukkit.block.data.type.Door;
import org.bukkit.block.data.type.Dripleaf;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.block.data.type.TrapDoor;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.entity.Entity;
import org.bukkit.entity.GlowItemFrame;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import dev.covector.maplus.Utils;

// NOT SUPPORTED
// banner, scaffolding, drip leaf

public class TempBlockManager {
    private static HashMap<Location, TempBlock> blocks = new HashMap<>();

    public enum ReplacePolicy {
        FIRST,
        LAST,
        LONGER
    }
    public static void setBlock(Material blockType, Location location, ReplacePolicy policy, int duration, int priority) {
        location = location.getBlock().getLocation();
        TempBlock block = blocks.containsKey(location) ? blocks.get(location) : null;
        if (block != null && block.priority > priority) { return; }
        if (policy == ReplacePolicy.FIRST && block != null && block.priority == priority) { return; }
        if (policy == ReplacePolicy.LONGER && block != null && block.priority == priority && block.getTimeLeft() > duration) { return; }

        boolean clump = false;
        if (duration > 0 && timerClumper.shouldClump(location, duration)) {
            duration = 0;
            clump = true;
        }

        ArrayList<Location> connectedBlocks = new ArrayList<>();
        Block blockAbove = location.getBlock().getRelative(BlockFace.UP);
        int height = 0;
        if (FLOWERS.contains(blockAbove.getType()) || LARGE_FLOWERS.contains(blockAbove.getType())) {
            height = 1;
        } else if (TALL_FLOWERS.contains(blockAbove.getType())) {
            height = 1;
            Block blockTemp = blockAbove.getRelative(BlockFace.UP);
            while (TALL_FLOWERS.contains(blockTemp.getType())) {
                height++;
                blockTemp = blockTemp.getRelative(BlockFace.UP);
            }
        }
        if (height > 0){
            BigDripleaf b1 = null;
            for (int i = 0; i < height; i++) {
                if (i == 0 && blockAbove.getType() == Material.BIG_DRIPLEAF_STEM) {
                    b1 = (BigDripleaf) Bukkit.createBlockData(Material.BIG_DRIPLEAF);
                    Dripleaf b2 = (Dripleaf) blockAbove.getBlockData();
                    b1.setFacing(b2.getFacing());
                }
                connectedBlocks.add(blockAbove.getLocation());
                setBlockSingle(blocks.get(blockAbove.getLocation()), Material.AIR, blockAbove.getLocation(), policy, duration, priority);
                TempBlock tb = blocks.get(blockAbove.getLocation());
                tb.hasParent = true;
                if (i == height - 1 && b1 != null) {
                    tb.originalBlockType = Material.BIG_DRIPLEAF;
                    tb.originalBlockData = b1;
                }
                blockAbove = blockAbove.getRelative(BlockFace.UP);
            }
        }

        setBlockSingle(block, blockType, location, policy, duration, priority);

        if (clump) {
            blocks.get(location).inBatch = true;
        }

        if (connectedBlocks.size() > 0) {
            blocks.get(location).setConnectedBlocks(connectedBlocks);
        }
    }

    private static void setBlockSingle(TempBlock block, Material blockType, Location location, ReplacePolicy policy, int duration, int priority) {
        if (block != null) {
            block.cancelTimer();
            blocks.put(location, new TempBlock(location, block.originalBlockType, block.originalBlockData, block.originalBlockState, blockType, duration, priority));
        } else {
            blocks.put(location, new TempBlock(location, location.getBlock(), blockType, duration, priority));
        }
        ItemFrame frame = getItemFrame(location);
        if (frame != null) {
            blocks.get(location).setItemFrameData(frame);
            frame.remove();
        }
    }

    public static void removeBlock(Location location) {
        location = location.getBlock().getLocation();
        if (blocks.containsKey(location)) {
            TempBlock block = blocks.get(location);
            if (!block.hasParent) {
                block.restoreBlock();
                block.cleanConnectedBlocks();
            }
            block.cancelTimer();
            blocks.remove(location);
        }
    }

    public static void removeAllBlocks() {
        timerClumper.removeAll();
        Collection<Location> locations = new ArrayList<>(blocks.keySet());
        for (Location location : locations) {
            TempBlock block = blocks.get(location);
            if (block != null) {
                if (!block.hasParent) {
                    block.restoreBlock();
                }
                block.cancelTimer();
            }
        }
        blocks.clear();
    }

    private static ItemFrame getItemFrame(Location location) {
        Collection<Entity> frames = location.getWorld().getNearbyEntities(location.clone().add(0.5, 0.5, 0.5), 0.5, 0.5, 0.5, e -> e instanceof ItemFrame);
        if (frames.size() > 0) {
            return (ItemFrame) frames.iterator().next();
        }
        return null;
    }

    private static class TempBlock {
        public int duration;
        private RestoreBlock timer = null;
        private long startTime;
        private int priority;
        public Material originalBlockType;
        public BlockData originalBlockData;
        public BlockState originalBlockState;
        private Location location;
        private List<Location> connectedBlocks = new ArrayList<>();
        private ItemFrameData itemFrameData = null;
        public boolean hasParent = false;
        public boolean inBatch = false;
        private class ItemFrameData {
            public ItemStack item;
            public BlockFace face;
            public boolean glowing;
            public Rotation rotation;
            public ItemFrameData(ItemFrame frame) {
                this.item = frame.getItem();
                this.face = frame.getFacing();
                this.glowing = frame instanceof GlowItemFrame;
                this.rotation = frame.getRotation();
            }
            public void spawn(Location location) {
                ItemFrame frame = (ItemFrame) (this.glowing ?
                    location.getWorld().spawn(location.getBlock().getLocation(), GlowItemFrame.class) :
                    location.getWorld().spawn(location.getBlock().getLocation(), ItemFrame.class)
                );
                frame.setItem(this.item);
                frame.setFacingDirection(this.face, true);
                frame.setRotation(this.rotation);
            }
        }

        public TempBlock(Location location, Block block, Material newBlockType, int duration, int priority) {
            this(block.getLocation(), block.getType(), block.getBlockData(), block.getState(), newBlockType, duration, priority);
        }

        public TempBlock(Location location, Material oldBlockType, BlockData oldBlockData, BlockState oldBlockState, Material newBlockType, int duration, int priority) {
            this.duration = duration;
            this.priority = priority;
            this.location = location;
            this.startTime = System.currentTimeMillis();
            this.originalBlockType = oldBlockType;
            this.originalBlockData = oldBlockData;
            this.originalBlockState = oldBlockState;
            this.preventDrop();
            location.getBlock().setType(newBlockType);
            if (duration > 0) {
                this.timer = new RestoreBlock(location, this);
                timer.runTaskLater(Utils.getPlugin(), duration);
            }
        }
        public int getTimeLeft() {
            return timer != null ? duration - (int) ((System.currentTimeMillis() - startTime) / 50) : 0;
        }
        public void cancelTimer() {
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
        }
        public void restoreBlock() {
            if (timer != null) {
                timer.run();
                timer = null;
            } else {
                setBlock(location);
                if (this.connectedBlocks.size() > 0) {
                    restoreConnectedBlocks();
                }
            }
        }

        public void cleanConnectedBlocks() {
            for (Location connectedBlock : this.connectedBlocks) {
                blocks.remove(connectedBlock);
            }
        }

        public void setConnectedBlocks(List<Location> connectedBlocks) {
            this.connectedBlocks = connectedBlocks;
            for (Location connectedBlock : connectedBlocks) {
                connectedBlock.getBlock().setType(Material.AIR);
            }
        }

        public void setItemFrameData(ItemFrame frame) {
            this.itemFrameData = new ItemFrameData(frame);
        }

        private void restoreConnectedBlocks() {
            for (Location connectedBlock : this.connectedBlocks) {
                TempBlock block = blocks.get(connectedBlock);
                if (block == null) { continue; }
                block.restoreBlock();
                block.cancelTimer();
            }
        }

        private void setBlock(Location location) {
            location.getBlock().setType(originalBlockType);
            location.getBlock().setBlockData(originalBlockData);

            // sign blockstates
            if (originalBlockState instanceof Sign) {
                Sign sign = (Sign) location.getBlock().getState();
                Sign originalSign = (Sign) originalBlockState;
                for (Side side : Side.values()) {
                    SignSide originalSignSide = originalSign.getSide(side);
                    SignSide signSide = sign.getSide(side);
                    String[] lines = originalSignSide.getLines();
                    for (int i = 0; i < lines.length; i++) {
                        signSide.setLine(i, lines[i]);
                    }
                    signSide.setGlowingText(originalSignSide.isGlowingText());
                    signSide.setColor(originalSignSide.getColor());
                }
                sign.setWaxed(originalSign.isWaxed());
                sign.update();
            }

            // lockable blockstates
            if (originalBlockState instanceof Container) {
                Container lockable = (Container) location.getBlock().getState();
                Container originalLockable = (Container) originalBlockState;
                if (originalLockable.isLocked()) {
                    lockable.setLock(originalLockable.getLock());
                    lockable.update();
                }
            }
            
            // door blockdata
            if (originalBlockData instanceof Bisected && !(originalBlockData instanceof TrapDoor) && !(originalBlockData instanceof Stairs)) {
                Bisected bisect = (Bisected) originalBlockData;
                Location bottom = location.clone().add(0, bisect.getHalf() == Bisected.Half.BOTTOM ? 0 : -1, 0);
                Location other = location.clone().add(0, bisect.getHalf() == Bisected.Half.BOTTOM ? 1 : -1, 0);
                TempBlockManager.blocks.remove(other);
                
                if (bisect instanceof Door) {
                    Door door = (Door) bisect;
                    Door d1 = (Door) Bukkit.createBlockData(originalBlockType);
                    d1.setHinge(door.getHinge());
                    d1.setOpen(door.isOpen());
                    d1.setPowered(door.isPowered());
                    d1.setFacing(door.getFacing());
                    d1.setHalf(Bisected.Half.BOTTOM);
                    location.getWorld().setBlockData(bottom.getBlockX(), bottom.getBlockY(), bottom.getBlockZ(), d1);
                    d1.setHalf(Bisected.Half.TOP);
                    location.getWorld().setBlockData(bottom.getBlockX(), bottom.getBlockY() + 1, bottom.getBlockZ(), d1);
                } else {
                    Bisected b1 = (Bisected) Bukkit.createBlockData(originalBlockType);
                    b1.setHalf(Bisected.Half.BOTTOM);
                    location.getWorld().setBlockData(bottom.getBlockX(), bottom.getBlockY(), bottom.getBlockZ(), b1);
                    b1.setHalf(Bisected.Half.TOP);
                    location.getWorld().setBlockData(bottom.getBlockX(), bottom.getBlockY() + 1, bottom.getBlockZ(), b1);
                }
            }

            // command block blockstate
            if (originalBlockState instanceof CommandBlock) {
                CommandBlock commandBlock = (CommandBlock) originalBlockState;
                CommandBlock cb = (CommandBlock) location.getBlock().getState();
                cb.setCommand(commandBlock.getCommand());
                cb.setName(commandBlock.getName());
                cb.update();
            }

            // container blockstate
            // if (originalBlockState instanceof Container) {
            //     Container container = (Container) originalBlockState;
            //     Container c = (Container) location.getBlock().getState();
            //     c.getInventory().setContents(container.getSnapshotInventory().getContents().clone());
            //     c.update();
            // }

            // big drip leaf blockdata
            if (originalBlockData instanceof BigDripleaf) {
                BigDripleaf bigDripleaf = (BigDripleaf) originalBlockData;
                BigDripleaf b1 = (BigDripleaf) Bukkit.createBlockData(originalBlockType);
                b1.setTilt(bigDripleaf.getTilt());
                location.getBlock().setBlockData(b1);
            }

            // bed blockdata
            if (originalBlockData instanceof Bed) {
                Bed bed = (Bed) originalBlockData;
                Location foot;
                Location head;
                if (bed.getPart() == Bed.Part.FOOT) {
                    foot = location.clone();
                    head = location.clone().add(bed.getFacing().getDirection());
                } else {
                    foot = location.clone().add(bed.getFacing().getOppositeFace().getDirection());
                    head = location.clone();
                }
                TempBlockManager.blocks.remove(bed.getPart() == Bed.Part.FOOT ? head : foot);
                Block b1 = foot.getBlock();
                b1.setType(originalBlockType);
                Bed bedState = (Bed) b1.getBlockData();
                bedState.setPart(Bed.Part.FOOT);
                bedState.setFacing(bed.getFacing());
                b1.setBlockData(bedState);
                b1 = head.getBlock();
                bedState.setPart(Bed.Part.HEAD);
                b1.setBlockData(bedState);
            }

            // item frame
            if (itemFrameData != null) {
                itemFrameData.spawn(location);
            }
        }

        private void preventDrop() {
            if (this.originalBlockData instanceof Bisected) {
                Bisected bisect = (Bisected) this.originalBlockData;
                Location bottom = location.clone().add(0, bisect.getHalf() == Bisected.Half.BOTTOM ? 0 : -1, 0);
                Location top = location.clone().add(0, bisect.getHalf() == Bisected.Half.BOTTOM ? 1 : 0, 0);
                bottom.getBlock().setType(Material.AIR);
                top.getBlock().setType(Material.AIR);
            }
            if (this.originalBlockData instanceof Bed) {
                Bed bed = (Bed) this.originalBlockData;
                Location foot = bed.getPart() == Bed.Part.FOOT ? location : location.clone().add(bed.getFacing().getOppositeFace().getDirection());
                Location head = bed.getPart() == Bed.Part.FOOT ? location.clone().add(bed.getFacing().getDirection()) : location;
                head.getBlock().setType(Material.AIR);
                foot.getBlock().setType(Material.AIR);
            }
        }

        private class RestoreBlock extends BukkitRunnable implements Listener {
            private Location location;
            private TempBlock tempBlock;
            public RestoreBlock(Location location, TempBlock tempBlock) {
                this.location = location;
                this.tempBlock = tempBlock;
            }
            public void run() {
                this.tempBlock.setBlock(location);
                TempBlockManager.blocks.remove(location);
                this.tempBlock.restoreConnectedBlocks();
                this.tempBlock.cleanConnectedBlocks();
                cancel();
            }
        }
    }

    private static final Set<Material> FLOWERS = new HashSet<Material>() {{
        add(Material.BROWN_MUSHROOM);
        add(Material.RED_MUSHROOM);
        add(Material.AZALEA);
        add(Material.FLOWERING_AZALEA);
        add(Material.CRIMSON_FUNGUS);
        add(Material.WARPED_FUNGUS);
        add(Material.GRASS);
        add(Material.FERN);
        add(Material.DEAD_BUSH);
        add(Material.DANDELION);
        add(Material.POPPY);
        add(Material.BLUE_ORCHID);
        add(Material.ALLIUM);
        add(Material.AZURE_BLUET);
        add(Material.RED_TULIP);
        add(Material.ORANGE_TULIP);
        add(Material.WHITE_TULIP);
        add(Material.PINK_TULIP);
        add(Material.OXEYE_DAISY);
        add(Material.CORNFLOWER);
        add(Material.LILY_OF_THE_VALLEY);
        add(Material.TORCHFLOWER);
        add(Material.WITHER_ROSE);
        add(Material.PINK_PETALS);
        add(Material.SUGAR_CANE);
        add(Material.SWEET_BERRY_BUSH);
        add(Material.CRIMSON_ROOTS);
        add(Material.WARPED_ROOTS);
        add(Material.PITCHER_CROP);
        add(Material.LILY_PAD);
        add(Material.BAMBOO_SAPLING);
    }};
    private static final Set<Material> LARGE_FLOWERS = new HashSet<Material>() {{
        add(Material.SUNFLOWER);
        add(Material.LILAC);
        add(Material.ROSE_BUSH);
        add(Material.PEONY);
        add(Material.TALL_GRASS);
        add(Material.LARGE_FERN);
    }};
    private static final Set<Material> TALL_FLOWERS = new HashSet<Material>() {{
        add(Material.CACTUS);
        add(Material.BIG_DRIPLEAF);
        add(Material.BIG_DRIPLEAF_STEM);
        add(Material.SCAFFOLDING);
        add(Material.BAMBOO);
    }};

    private static class TimerClumper {
        private long lastTime = 0;
        public Location parentTempBlock = null;
        private BatchRestoreBlock batchRestoreBlock = null;
        private HashMap<UUID, BatchRestoreBlock> batchRestoreBlocks = new HashMap<>();
        public boolean shouldClump(Location location, int duration) {
            if (parentTempBlock != null && System.currentTimeMillis() - this.lastTime < 50) {
                TempBlock tb = TempBlockManager.blocks.get(parentTempBlock);
                if (tb != null && tb.duration == duration) {
                    this.lastTime = System.currentTimeMillis();
                    if (batchRestoreBlock == null) {
                        batchRestoreBlock = new BatchRestoreBlock(this, parentTempBlock);
                        batchRestoreBlock.runTaskLater(Utils.getPlugin(), duration);
                        batchRestoreBlocks.put(batchRestoreBlock.uuid, batchRestoreBlock);
                    }
                    batchRestoreBlock.addLocation(location);
                    return true;
                }
            }
            this.lastTime = System.currentTimeMillis();
            this.parentTempBlock = location;
            batchRestoreBlock = null;
            return false;
        }
        public void removeAll() {
            for (BatchRestoreBlock brb : batchRestoreBlocks.values()) {
                brb.cancel();
            }
            batchRestoreBlocks.clear();
        }
        private class BatchRestoreBlock extends BukkitRunnable implements Listener {
            private ArrayList<Location> locations = new ArrayList<>();
            private TimerClumper clumper;
            public UUID uuid = UUID.randomUUID();
            public BatchRestoreBlock(TimerClumper clumper, Location location) {
                this.clumper = clumper;
                this.locations.add(location);
                TempBlock tb = TempBlockManager.blocks.get(location);
                if (tb != null) {
                    tb.cancelTimer();
                    tb.inBatch = true;
                }
            }
            public void addLocation(Location location) {
                this.locations.add(location);
            }
            public void run() {
                for (Location location : this.locations) {
                    TempBlock block = TempBlockManager.blocks.get(location);
                    if (block != null && block.inBatch) {
                        block.setBlock(location);
                        TempBlockManager.blocks.remove(location);
                        block.restoreConnectedBlocks();
                        block.cleanConnectedBlocks();
                    }
                }
                removeSelf();
                cancel();
            }
            public void removeSelf() {
                clumper.batchRestoreBlock = null;
            }
        }
    }
    private static TimerClumper timerClumper = new TimerClumper();
}
