package dev.covector.maplus.mmextension.def;

import java.util.Collections;
import java.util.List;
import java.util.HashMap;
import java.util.Random;
import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import dev.covector.maplus.Utils;
import dev.covector.maplus.mmextension.Ability;
import dev.covector.maplus.mmextension.MMExtUtils;

public class BombDefuse extends DefaultParamAbility {
    private String syntax = "target:<target-uuid> bombCount:<int> timeLimitSeconds:<double> onSuccess:<success-mm-skill> onFail:<fail-mm-skill-callback> bombItem:<item-name> defusedItem:<item-name> emptyItem:<item-name>";

    private String id = "bombDefuse";
    private int GUISIZE = 6 * 9;
    private double HARDTIMELIMIT = 60D;
    private HashSet<DefuseProcess> processes = new HashSet<>();

    public BombDefuse() {
        setDefault("target", null, livingEntityTabOptions);
        setDefault("bombCount", "6");
        setDefault("timeLimitSeconds", "60");
        setDefault("onSuccess", null);
        setDefault("onFail", null);
        setDefault("bombItem", "RED_DYE");
        setDefault("defusedItem", "BLACK_DYE");
        setDefault("emptyItem", "GRAY_DYE");
    }

    private void removeProcess(DefuseProcess process) {
        processes.remove(process);
    }

    private boolean isProcessRunning(Player player) {
        for (DefuseProcess process : processes) {
            if (process.player == player) {
                return true;
            }
        }
        return false;
    }

    private class DefuseProcess extends BukkitRunnable implements Listener {
        private Player player;
        private String successCallback;
        private String failCallback;
        private HashMap<Integer, Boolean> defused;
        private Inventory inv;
        private BombDefuse bombDefuse;
        private Material bombItem;
        private Material defusedItem;
        private Material emptyItem;


        public DefuseProcess(Player player, int bombCount, String successCallback, String failCallback, Material bombItem, Material defusedItem, Material emptyItem, BombDefuse bombDefuse) {
            this.player = player;
            this.successCallback = successCallback;
            this.failCallback = failCallback;
            this.defused = new HashMap<>();
            this.bombItem = bombItem;
            this.defusedItem = defusedItem;
            this.emptyItem = emptyItem;
            this.bombDefuse = bombDefuse;
            bombDefuse.processes.add(this);

            Random rand = new Random();
            int i = 0;
            while (i < bombCount) {
                int slot = rand.nextInt(GUISIZE);
                if (!defused.containsKey(slot)) {
                    defused.put(slot, false);
                    i++;
                }
            }

            Bukkit.getPluginManager().registerEvents(this, Utils.getPlugin());
            inv = createInventory();
            player.openInventory(inv);
        }

        private boolean allDefused() {
            for (boolean defused : defused.values()) {
                if (!defused) {
                    return false;
                }
            }
            return true;
        }

        private void tryDefuse(int slot) {
            if (defused.containsKey(slot)) {
                defused.put(slot, true);
                inv.setItem(slot, getDefusedItem());
                if (allDefused()) {
                    end();
                    if (successCallback != null) {
                        MMExtUtils.castMMSkill(player, successCallback, player, null);
                    }
                }
            } else {
                end();
                if (failCallback != null) {
                    MMExtUtils.castMMSkill(player, failCallback, player, null);
                }
            }
        }

        private ItemStack getBombItem() {
            ItemStack redDye = new ItemStack(bombItem);
            ItemMeta meta = redDye.getItemMeta();
            meta.setDisplayName("Bomb");
            redDye.setItemMeta(meta);
            return redDye;
        }

        private ItemStack getDefusedItem() {
            ItemStack blackDye = new ItemStack(defusedItem);
            ItemMeta meta = blackDye.getItemMeta();
            meta.setDisplayName("Defused");
            blackDye.setItemMeta(meta);
            return blackDye;
        }

        private ItemStack getEmptyItem() {
            ItemStack grayDye = new ItemStack(emptyItem);
            return grayDye;
        }

        private Inventory createInventory() {
            Inventory inv = Bukkit.createInventory(null, GUISIZE, "Defuse the bomb!");
            for (int i = 0; i < GUISIZE; i++) {
                inv.setItem(i, getEmptyItem());
            }
            for (int slot : defused.keySet()) {
                inv.setItem(slot, defused.get(slot) ? getDefusedItem() : getBombItem());
            }
            return inv;
        }

        @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
        public void a(InventoryClickEvent e) {
            Player p = (Player) e.getWhoClicked();
            if (p != player) { return; }
            if (e.getClickedInventory() != inv) { return; }
            e.setCancelled(true);
            tryDefuse(e.getSlot());
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void b(InventoryCloseEvent e) {
            Player p = (Player) e.getPlayer();
            if (p != player) { return; }
            if (e.getInventory() != inv) { return; }
            new BukkitRunnable() {
                public void run() {
                    if (player.getOpenInventory().getTitle().equals("Defuse the bomb!")) {
                        return;
                    }
                    player.openInventory(inv);
                }
            }.runTaskLater(Utils.getPlugin(), 1L);
        }

        public void run() {
            end();
            MMExtUtils.castMMSkill(player, failCallback, player, null);
        }

        public void end() {
            InventoryClickEvent.getHandlerList().unregister(this);
            InventoryCloseEvent.getHandlerList().unregister(this);
            player.closeInventory();
            bombDefuse.removeProcess(this);
            cancel();
        }
    }

    public String cast(String[] args) {
        if (args.length < 1) {
            return "args length must at least be 1";
        }
        ParsedParam parsedParam;
        try {
            parsedParam = parse(args);
        } catch (Exception e) {
            return e.getMessage();
        }
        
        String targetUUID = getParam(parsedParam, "target");
        if (targetUUID == null) {
            return "target must be specified";
        }
        Entity target = MMExtUtils.parseUUID(targetUUID);
        int bombCount = getInt(parsedParam, "bombCount");
        double duration = getDouble(parsedParam, "timeLimitSeconds");
        String successCallback = getParam(parsedParam, "onSuccess");
        String failCallback = getParam(parsedParam, "onFail");
        String bombItemName = getParam(parsedParam, "bombItem");
        String defusedItemName = getParam(parsedParam, "defusedItem");        
        String emptyItemName = getParam(parsedParam, "emptyItem");

        // check if item exists
        Material bombItem = null;
        Material defusedItem = null;
        Material emptyItem = null;
        if (bombItemName != null) {
            bombItem = Material.getMaterial(bombItemName);
            if (bombItem == null) {
                return "bombItem must be a valid material";
            }
        }
        defusedItem = Material.getMaterial(defusedItemName);
        if (defusedItem == null) {
            return "defusedItem must be a valid material";
        }
        emptyItem = Material.getMaterial(emptyItemName);
        if (emptyItem == null) {
            return "emptyItem must be a valid material";
        }
        
        if (!(target instanceof Player)) {
            return "target must be a player";
        }

        if (bombCount > GUISIZE) {
            return "bomb count is too large";
        }

        Player targetPlayer = (Player) target;
        if (isProcessRunning(targetPlayer)) {
            return "target player is already defusing a bomb";
        }
        new DefuseProcess(targetPlayer, bombCount, successCallback, failCallback, bombItem, defusedItem, emptyItem, this).runTaskLater(Utils.getPlugin(), (long) (duration * 20D));

        return null;
    }

    public String getSyntax() {
        return syntax;
    }

    public String getId() {
        return id;
    }
}
