package dev.covector.maplus.mmextension.def;

import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import dev.covector.maplus.Utils;
import dev.covector.maplus.mmextension.Ability;
import dev.covector.maplus.mmextension.MMExtUtils;

public class RightClickTridentReturn extends Ability implements AutoCloseable, Listener {
    private String syntax = "<target-player> <can-hit-on-return:(true|false)> <return-speed(only-for-can-hit-on-return-true)>";
    private String id = "rightClickTridentReturn";
    private HashMap<String, TridentReturnParams> checkPlayers = new HashMap<String, TridentReturnParams>();

    public RightClickTridentReturn() {
        Bukkit.getPluginManager().registerEvents(this, Utils.getPlugin());
    }

    public String cast(String[] args) {
        if (args.length != 2 && args.length != 3) {
            return "args length must be 2 or 3";
        }

        Entity target = MMExtUtils.parseUUID(args[0]);
        if (!(target instanceof Player)) {
            return "target must be a player";
        }
        Player player = (Player) target;

        boolean canHitOnReturn = Boolean.parseBoolean(args[1]);
        double returnSpeed = args.length == 3 ? Double.parseDouble(args[2]) : 1;

        checkPlayers.put(player.getUniqueId().toString(), new TridentReturnParams(canHitOnReturn, returnSpeed));

        return null;
    }

    protected class TridentReturnParams {
        public boolean canHitOnReturn;
        public double returnSpeed;
        public TridentReturnParams(boolean canHitOnReturn, double returnSpeed) {
            this.canHitOnReturn = canHitOnReturn;
            this.returnSpeed = returnSpeed;
        }
    }

    public String getSyntax() {
        return syntax;
    }

    public String getId() {
        return id;
    }

    public List<String> getTabComplete(CommandSender sender, String[] argsList) {
        if (argsList.length == 1) {
            return MMExtUtils.getLivingEntityTabComplete(argsList[argsList.length-1]);
        }
        
        return super.getTabComplete(sender, argsList);
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if ((event.getAction() != Action.LEFT_CLICK_AIR)) {
            return;
        }

        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        String playerUUID = player.getUniqueId().toString();
        if (!checkPlayers.containsKey(playerUUID)) {
            return;
        }

        TridentReturnParams params = checkPlayers.get(playerUUID);
        TridentReturn.resetTrident(player, params.canHitOnReturn, params.returnSpeed);

        checkPlayers.remove(playerUUID);
     }

    @Override
    public void close() {
        PlayerInteractEvent.getHandlerList().unregister(this);
    }
}
