package dev.covector.maplus.mmextension.def;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import dev.covector.maplus.Utils;
import dev.covector.maplus.mmextension.Ability;
import dev.covector.maplus.mmextension.MMExtUtils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;

public class CompassTrack extends Ability implements Utils.Destructor {
    private String syntax = "<targeter-uuid> <targetee-uuid> OR clear";
    private String id = "compassTrack";
    private HashMap<UUID, UUID> compassTrackers = new HashMap<UUID, UUID>();
    private CompassTrack.Tracking trackingTimer;

    public CompassTrack() {
        this.trackingTimer = new Tracking(this);
        this.trackingTimer.runTaskTimer(Utils.getPlugin(), 0, 20);
        Utils.addDestructor(this);
    }
    @Override
    public void destroy() {
        for (UUID playerUUID : compassTrackers.keySet()) {
            Player player = Bukkit.getPlayer(playerUUID);
            if (player != null) {
                CompassTrack.clearCompass(player);
            }
        }
        trackingTimer.cancel();
    }

    public String cast(String[] args) {
        if (args.length != 2) {
            return "args length must be 2";
        }

        Entity targeter = MMExtUtils.parseUUID(args[0]);
        if (!(targeter instanceof Player)) {
            return "targeter must be a player";
        }
        Player player = (Player) targeter;

        if (args[1].equals("clear")) {
            CompassTrack.clearCompass(player);
            compassTrackers.remove(player.getUniqueId());
        } else {
            Entity targetee = MMExtUtils.parseUUID(args[1]);
            if (targetee == null) {
                return "targetee not found";
            }
            compassTrackers.put(player.getUniqueId(), targetee.getUniqueId());
        }

        return null;
    }

    private class Tracking extends BukkitRunnable implements Listener {
        private CompassTrack ability;
        public Tracking(CompassTrack ability) {
            this.ability = ability;
        }
        public void run() {
            for (UUID playerUUID : ability.compassTrackers.keySet()) {
                Player player = Bukkit.getPlayer(playerUUID);
                if (player == null) {
                    continue;
                }
                Entity targetee = Bukkit.getEntity(ability.compassTrackers.get(playerUUID));
                if (targetee == null) {
                    CompassTrack.clearCompass(player);
                    ability.compassTrackers.remove(playerUUID);
                    continue;
                }

                CompassTrack.setCompass(player, targetee.getLocation());
            }
        }
    }

    private static void setCompass(Player player, Location loc) {
        player.setCompassTarget(loc);
        // for (int i = 0; i < 9; i++) {
        //     ItemStack item = player.getInventory().getItem(i);
        //     if (item != null && item.getType().equals(Material.COMPASS)) {
        //         CompassMeta compassMeta = (CompassMeta) item.getItemMeta();
        //         Bukkit.broadcastMessage("is lodestone tracked: " + compassMeta.isLodestoneTracked());
        //         compassMeta.setLodestoneTracked(false);
        //         compassMeta.setLodestone(loc);
        //         item.setItemMeta(compassMeta);
        //         player.getInventory().setItem(i, item);
        //     }
        // }
    }

    private static void clearCompass(Player player) {
        player.setCompassTarget(player.getWorld().getSpawnLocation());
        // for (int i = 0; i < 9; i++) {
        //     ItemStack item = player.getInventory().getItem(i);
        //     if (item != null && item.getType().equals(Material.COMPASS)) {
        //         CompassMeta compassMeta = (CompassMeta) item.getItemMeta();
        //         compassMeta.setLodestoneTracked(false);
        //         compassMeta.setLodestone(null);
        //         item.setItemMeta(compassMeta);
        //         player.getInventory().setItem(i, item);
        //     }
        // }
    }

    public String getSyntax() {
        return syntax;
    }

    public String getId() {
        return id;
    }

    public List<String> getTabComplete(CommandSender sender, String[] argsList) {
        if (argsList.length == 1 && sender instanceof Player) {
            return MMExtUtils.getLivingEntityTabComplete(argsList[argsList.length-1], (Player) sender);
        }

        if (argsList.length == 2 && sender instanceof Player) {
            return Stream.concat(
                MMExtUtils.streamFilter(Stream.of("clear"), argsList[1]).stream(),
                MMExtUtils.getLivingEntityTabComplete(argsList[argsList.length-1], (Player) sender).stream())
            .collect(Collectors.toList());
        }
        
        return super.getTabComplete(sender, argsList);
    }
}
