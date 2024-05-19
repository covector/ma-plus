package dev.covector.maplus.misc;

import java.util.List;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.api.item.mmoitem.LiveMMOItem;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.item.mmoitem.VolatileMMOItem;
import net.Indyuce.mmoitems.stat.data.GemSocketsData;
import net.Indyuce.mmoitems.stat.data.GemstoneData;
import net.Indyuce.mmoitems.stat.type.StatHistory;
import net.Indyuce.mmoitems.util.Pair;
import io.lumine.mythic.lib.api.util.ui.SilentNumbers;

public class UnsocketGems implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // syntax: /unsocketgems
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "You must be a player to execute this command!");
            return false;
        }

        Player player = (Player) sender;
        NBTItem target = NBTItem.get(player.getInventory().getItemInMainHand());

        MMOItem mmoVol = new VolatileMMOItem(target);
        if (!mmoVol.hasData(ItemStats.GEM_SOCKETS)) { return true; }
        GemSocketsData mmoGems = (GemSocketsData) mmoVol.getData(ItemStats.GEM_SOCKETS);
        if (mmoGems == null || mmoGems.getGemstones().size() == 0) { return true; }

        MMOItem mmo = new LiveMMOItem(target);
        List<Pair<GemstoneData, MMOItem>> mmoGemStones = mmo.extractGemstones();
        if (mmoGemStones.isEmpty()) {
            return true;
        }

        ArrayList<ItemStack> items2Drop = new ArrayList<>();
        while (!mmoGemStones.isEmpty()) {

            final Pair<GemstoneData, MMOItem> pair = mmoGemStones.get(0);
            final MMOItem gem = pair.getValue();
            final GemstoneData gemData = pair.getKey();
            mmoGemStones.remove(0);

            try {
                ItemStack builtGem = gem.newBuilder().build();

                if (!SilentNumbers.isAir(builtGem)) {
                    items2Drop.add(builtGem);
                    String chosenColor;
                    if (gemData.getSocketColor() != null) {
                        chosenColor = gemData.getSocketColor();
                    } else {
                        chosenColor = GemSocketsData.getUncoloredGemSlot();
                    }

                    mmo.removeGemStone(gemData.getHistoricUUID(), chosenColor);
                }

            } catch (Throwable e) {}
        }
        mmo.setData(ItemStats.GEM_SOCKETS, StatHistory.from(mmo, ItemStats.GEM_SOCKETS).recalculate(mmo.getUpgradeLevel()));
        player.getInventory().setItemInMainHand(mmo.newBuilder().build());
        for (ItemStack drop : player.getInventory().addItem(items2Drop.toArray(new ItemStack[0])).values()) player.getWorld().dropItem(player.getLocation(), drop);

        return true;
    }
}