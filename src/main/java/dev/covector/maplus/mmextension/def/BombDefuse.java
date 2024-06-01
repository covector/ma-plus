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

public class BombDefuse extends Ability {
    private String syntax = "<target-uuid> <bomb-count> <time-limit-seconds> <success-mm-skill-callback> <fail-mm-skill-callback>";
    private String id = "bombDefuse";
    private int GUISIZE = 6 * 9;
    private double HARDTIMELIMIT = 60D;
    private HashSet<DefuseProcess> processes = new HashSet<>();

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


        public DefuseProcess(Player player, int bombCount, String successCallback, String failCallback, BombDefuse bombDefuse) {
            this.player = player;
            this.successCallback = successCallback;
            this.failCallback = failCallback;
            this.defused = new HashMap<>();
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
                    MMExtUtils.castMMSkill(player, successCallback, player, null);
                }
            } else {
                end();
                MMExtUtils.castMMSkill(player, failCallback, player, null);
            }
        }

        private ItemStack getBombItem() {
            ItemStack redDye = new ItemStack(Material.RED_DYE);
            ItemMeta meta = redDye.getItemMeta();
            meta.setDisplayName("Bomb");
            redDye.setItemMeta(meta);
            return redDye;
        }

        private ItemStack getDefusedItem() {
            ItemStack blackDye = new ItemStack(Material.BLACK_DYE);
            ItemMeta meta = blackDye.getItemMeta();
            meta.setDisplayName("Defused");
            blackDye.setItemMeta(meta);
            return blackDye;
        }

        private ItemStack getEmptyItem() {
            ItemStack grayDye = new ItemStack(Material.GRAY_DYE);
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
        if (args.length != 5) {
            return "args length must be 5";
        }

        Entity target = MMExtUtils.parseUUID(args[0]);
        int bombCount = Integer.parseInt(args[1]);
        double duration = Double.parseDouble(args[2]);
        
        if (!(target instanceof Player)) {
            return "target must be a player";
        }

        if (duration > HARDTIMELIMIT) {
            return "time limit is too long";
        }

        if (bombCount > GUISIZE) {
            return "bomb count is too large";
        }

        Player targetPlayer = (Player) target;
        if (isProcessRunning(targetPlayer)) {
            return "target player is already defusing a bomb";
        }
        new DefuseProcess(targetPlayer, bombCount, args[3], args[4], this).runTaskLater(Utils.getPlugin(), (long) (duration * 20D));

        return null;
    }

    public String getSyntax() {
        return syntax;
    }

    public String getId() {
        return id;
    }

    public List<String> getTabComplete(CommandSender sender, String[] argsList) {
        if (argsList.length == 1 && sender instanceof Player) {
            return MMExtUtils.getLivingEntityTabComplete(argsList[0], (Player) sender);
        }
        
        return super.getTabComplete(sender, argsList);
    }
}
