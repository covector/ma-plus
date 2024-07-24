package dev.covector.maplus.mmapihook.mechanics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Rotation;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Bed;
import org.bukkit.block.data.type.Door;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.entity.GlowItemFrame;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import dev.covector.maplus.Utils;

// TODO
// grass, item frame, bed, banner, door, tall grass

public class TempBlockManager {
    private static HashMap<Location, TempBlock> blocks = new HashMap<>();

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
        add(Material.BAMBOO);
        add(Material.CACTUS);
        add(Material.SUGAR_CANE);
        add(Material.SWEET_BERRY_BUSH);
        add(Material.CRIMSON_ROOTS);
        add(Material.WARPED_ROOTS);
        add(Material.PITCHER_CROP);
        add(Material.BIG_DRIPLEAF);
        add(Material.LILY_PAD);
    }};

    private static final Set<Material> LARGE_FLOWERS = new HashSet<Material>() {{
        add(Material.SUNFLOWER);
        add(Material.LILAC);
        add(Material.ROSE_BUSH);
        add(Material.PEONY);
        add(Material.TALL_GRASS);
        add(Material.LARGE_FERN);
    }};

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

        ArrayList<Location> connectedBlocks = new ArrayList<>();
        BlockData currentBlockData = location.getBlock().getBlockData();
        Block blockAbove = location.getBlock().getRelative(BlockFace.UP);
        if (FLOWERS.contains(blockAbove.getType()) || LARGE_FLOWERS.contains(blockAbove.getType())){
            connectedBlocks.add(blockAbove.getLocation());
            setBlockSingle(blocks.get(blockAbove.getLocation()), Material.AIR, blockAbove.getLocation(), policy, duration, priority);
        }

        setBlockSingle(block, blockType, location, policy, duration, priority);

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
    }

    public static void removeBlock(Location location) {
        location = location.getBlock().getLocation();
        if (blocks.containsKey(location)) {
            TempBlock block = blocks.get(location);
            block.restoreBlock(true);
            block.cancelTimer();
            blocks.remove(location);
        }
    }

    public static void removeAllBlocks() {
        for (TempBlock block : blocks.values()) {
            block.restoreBlock(false);
            block.cancelTimer();
        }
        blocks.clear();
    }

    private static class TempBlock {
        private int duration;
        private RestoreBlock timer = null;
        private long startTime;
        private int priority;
        public Material originalBlockType;
        public BlockData originalBlockData;
        public BlockState originalBlockState;
        private Location location;
        private List<Location> connectedBlocks = new ArrayList<>();
        private ItemFrameData itemFrameData = null;
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
            public ItemFrameData(ItemStack item, BlockFace face, boolean glowing, Rotation rotation) {
                this.item = item;
                this.face = face;
                this.glowing = glowing;
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
            }
        }
        public void restoreBlock(boolean connected) {
            if (timer != null) {
                timer.run();
            } else {
                setBlock(location);
                if (connected) {
                    restoreConnectedBlocks();
                }
            }
        }

        public void setConnectedBlocks(List<Location> connectedBlocks) {
            this.connectedBlocks = connectedBlocks;
        }

        public void setItemFrameData(ItemFrame frame) {
            this.itemFrameData = new ItemFrameData(frame);
        }

        private void restoreConnectedBlocks() {
            for (Location connectedBlock : this.connectedBlocks) {
                Bukkit.broadcastMessage("Connected block: " + connectedBlock);
                TempBlock block = blocks.get(connectedBlock);
                if (block == null) { continue; }
                Bukkit.broadcastMessage("Restoring connected block");
                block.restoreBlock(false);
                block.cancelTimer();
                blocks.remove(connectedBlock);
            }
        }

        private void setBlock(Location location) {
            location.getBlock().setType(originalBlockType);
            location.getBlock().setBlockData(originalBlockData);

            // BlockState state = location.getBlock().getState();
            // state.setData(originalBlockState.getData());
            // state.update();

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
            if (originalBlockData instanceof Door) {
                Door door = (Door) originalBlockData;
                Location bottom = location.clone().add(0, door.getHalf() == Door.Half.BOTTOM ? 0 : -1, 0);
                Location other = location.clone().add(0, door.getHalf() == Door.Half.BOTTOM ? 1 : -1, 0);
                TempBlockManager.blocks.remove(other);
                Door d1 = (Door) Bukkit.createBlockData(originalBlockType);
                d1.setHinge(door.getHinge());
                d1.setOpen(door.isOpen());
                d1.setPowered(door.isPowered());
                d1.setFacing(door.getFacing());
                d1.setHalf(Bisected.Half.BOTTOM);
                location.getWorld().setBlockData(bottom.getBlockX(), bottom.getBlockY(), bottom.getBlockZ(), d1);
                d1.setHalf(Bisected.Half.TOP);
                location.getWorld().setBlockData(bottom.getBlockX(), bottom.getBlockY() + 1, bottom.getBlockZ(), d1);
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
                restoreConnectedBlocks();
                cancel();
            }
        }
    }
}
