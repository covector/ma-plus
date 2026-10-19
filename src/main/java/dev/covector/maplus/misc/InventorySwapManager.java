package dev.covector.maplus.misc;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import com.garbagemule.MobArena.events.ArenaEndEvent;
import com.garbagemule.MobArena.events.ArenaPlayerDeathEvent;

public class InventorySwapManager implements Listener {
    // singleton
    private static InventorySwapManager instance;
    public static InventorySwapManager getInstance() {
        if (instance == null) {
            instance = new InventorySwapManager();
        }
        return instance;
    }

    private HashMap<String, String> hasInvOf = new HashMap<>();

    private ItemStack[] getInventory(Player player) {
        return player.getInventory().getContents();
    }

    private void setInventory(Player player, ItemStack[] inventory) {
        player.getInventory().setContents(inventory);
    }

    public boolean isSwapping(Player player) {
        return isSwapping(player.getUniqueId().toString());
    }
    public boolean isSwapping(String playerUUID) {
        return hasInvOf.containsKey(playerUUID);
    }

    private void swapHasInvOfMap(String a, String b) {
        String aHasInvOf = hasInvOf.get(a);
        String bHasInvOf = hasInvOf.get(b);
        
        // if b is actually holding the inventory of another player c
        // a should now hold that of c
        // else just hold that of b
        hasInvOf.put(a, bHasInvOf == null ? b : bHasInvOf);
        // same the other way around
        hasInvOf.put(b, aHasInvOf == null ? a : aHasInvOf);

        // remove if hasInvOf points to self
        if (a.equals(hasInvOf.get(a))) hasInvOf.remove(a);
        if (b.equals(hasInvOf.get(b))) hasInvOf.remove(b);
    }

    private void swapInventory(Player player1, Player player2) {
        ItemStack[] inventory1 = getInventory(player1);
        ItemStack[] inventory2 = getInventory(player2);

        setInventory(player1, inventory2);
        setInventory(player2, inventory1);
    }

    public void swap(Player player1, Player player2) {
        String player1UUID = player1.getUniqueId().toString();
        String player2UUID = player2.getUniqueId().toString();
        
        // cannot swap with self
        if (player1UUID.equals(player2UUID)) return;

        swapHasInvOfMap(player1UUID, player2UUID);
        swapInventory(player1, player2);
    }

    public void shuffle(List<Player> players) {
        // fair shuffle using Fisher-Yates shuffle
        for (int i = players.size() - 1; i > 0; i--) {
            int j = ThreadLocalRandom.current().nextInt(i + 1);
            swap(players.get(j), players.get(i));
        }
    }

    public void reset() {
        // copy all inventory into a new temp hashmap
        HashMap<String, ItemStack[]> currentInv = new HashMap<>();
        for (String uuid : hasInvOf.keySet()) {
            // get player
            Entity entity = Bukkit.getEntity(UUID.fromString(uuid));
            if (!(entity instanceof Player)) return;
            Player player = (Player) entity;

            // save inventory of player
            currentInv.put(uuid, getInventory(player));
        }

        // loop through hasInvOf and give back the original inventory to the player
        for (String uuid : hasInvOf.keySet()) {
            String originalHolderUUID = hasInvOf.get(uuid);

            // get original holder
            Entity entity = Bukkit.getEntity(UUID.fromString(originalHolderUUID));
            if (!(entity instanceof Player)) return;
            Player originalHolder = (Player) entity;

            // give back inventory
            setInventory(originalHolder, currentInv.get(uuid));
        }

        // clear hasInvOf
        hasInvOf.clear();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDeath(ArenaPlayerDeathEvent event) {
        // reset on player death
        reset();
    }

    public void unregister() {
        ArenaEndEvent.getHandlerList().unregister(this);
        reset();
    }
}
