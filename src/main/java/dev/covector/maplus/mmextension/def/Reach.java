package dev.covector.maplus.mmextension.def;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import dev.covector.maplus.Utils;
import dev.covector.maplus.mmextension.Ability;
import dev.covector.maplus.mmextension.MMExtUtils;

public class Reach extends Ability implements Utils.Destructor {
    private String syntax = "<target-player> <blocks> or <target-player> reset";
    private String id = "reach";

    private final StolenReach reach = new StolenReach();

    public Reach() {
        Utils.addDestructor(this);
    }

    public String cast(String[] args) {
        if (args.length != 2) {
            return "args length must be 2";
        }
        
        Entity target = MMExtUtils.parseUUID(args[0]);
        
        if (!(target instanceof Player)) {
            return "target must be a player";
        }
        Player targetPlayer = (Player) target;

        double reachBlocks;
        
        try {
            reachBlocks = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            reach.resetReach(targetPlayer);
            return null;
        }

        if (reachBlocks < 0) {
            return "reach must be a positive number";
        }

        if (reachBlocks <= 3) {
            reach.resetReach(targetPlayer);
            return null;
        }

        reach.setReach(targetPlayer, reachBlocks);
        
        return null;
    }

    public String getSyntax() {
        return syntax;
    }

    public String getId() {
        return id;
    }

    public void destroy() {
        reach.unregisterListeners();
    }

    class StolenReach implements Listener {
        private final Set<Entity> exclude = new HashSet<>();
        private final Set<UUID> throwExclude = new HashSet<>();
        private final Map<UUID, Double> playerReach = new HashMap<>();

        public StolenReach() {
            Bukkit.getPluginManager().registerEvents(this, Utils.getPlugin());
        }

        public void unregisterListeners() {
            PlayerInteractEvent.getHandlerList().unregister((Listener) this);
            EntityDamageByEntityEvent.getHandlerList().unregister((Listener) this);
            PlayerDropItemEvent.getHandlerList().unregister((Listener) this);
        }
        
        @EventHandler
        public void onClick(PlayerInteractEvent e) {
            if (!(e.getAction().equals(Action.LEFT_CLICK_AIR) || e.getAction().equals(Action.LEFT_CLICK_BLOCK))) return;
            if (throwExclude.contains(e.getPlayer().getUniqueId())) {
                throwExclude.remove(e.getPlayer().getUniqueId());
                return;
            }
            if (!hasReach(e.getPlayer().getUniqueId())) return;
            Player player = e.getPlayer();
            Location eyeLocation = player.getEyeLocation();
            Vector direction = eyeLocation.getDirection();
            Predicate<Entity> filter = (entity) -> !entity.equals(player);
            RayTraceResult result = player.getWorld().rayTrace(eyeLocation, direction,
                    getReach(player.getUniqueId()),
                    FluidCollisionMode.NEVER, false, 0, filter);
            if (result == null) return;
            Entity entity = result.getHitEntity();
            if (entity == null) return;
            if (exclude.contains(entity)) {
                exclude.remove(entity);
                return;
            }
            if (entity.getPassengers().contains(player)) return;
            player.attack(entity);
        }

        @EventHandler
        public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
            exclude.add(e.getEntity());
            Bukkit.getScheduler().scheduleSyncDelayedTask(Utils.getPlugin(), () -> exclude.remove(e.getEntity()));
        }

        @EventHandler
        public void onThrow(PlayerDropItemEvent e) {
            throwExclude.add(e.getPlayer().getUniqueId());
        }

        public void setReach(Player player, double reach) {
            playerReach.put(player.getUniqueId(), reach);
        }

        public void resetReach(Player player) {
            playerReach.remove(player.getUniqueId());
        }

        public Map<UUID, Double> getPlayerReach() {
            return playerReach;
        }

        public boolean hasReach(UUID uuid) {
            return playerReach.containsKey(uuid);
        }

        public double getReach(UUID uuid) {
            return playerReach.getOrDefault(uuid, 0D);
        }
    }

    public List<String> getTabComplete(CommandSender sender, String[] argsList) {
        if (argsList.length == 1 && sender instanceof Player) {
            return MMExtUtils.getLivingEntityTabComplete(argsList[0], (Player) sender);
        }

        if (argsList.length == 2) {
            return MMExtUtils.streamFilter(Stream.of("reset"), argsList[1]);
        }

        return super.getTabComplete(sender, argsList);
    }
}
