package dev.covector.maplus.mmextension.def;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.Bukkit;

import dev.covector.maplus.mmextension.Ability;
import dev.covector.maplus.mmextension.MMExtUtils;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.player.cooldown.CooldownMap;

public class SetBossBar extends Ability {
    private String syntax = "<target-player> set <title> <color:(" + availableColors() + ")> <style:(" + availableStyles() + ")> <progress:(0-1)> OR <target-player> remove <title> OR <target-player> clear";
    private String id = "setBossBar";
    private HashMap<String, HashMap<String, BossBar>> bossBar = new HashMap<String, HashMap<String, BossBar>>();

    public String cast(String[] args) {
        if (args.length != 2 && args.length != 3 && args.length != 6) {
            return "args length must be 2 or 3 or 6";
        }
        
        Entity target = MMExtUtils.parseUUID(args[0]);
        if (!(target instanceof Player)) {
            return "target must be a player";
        }
        Player targetPlayer = (Player) target;

        String action = args[1];
        if (action.equals("clear")) {
            // clear bossbar
            clearTitles(targetPlayer);
        } else if (action.equals("remove")) {
            // remove bossbar
            if (args.length < 3) {
                return "title to be removed must be specified";
            }
            removeTitle(targetPlayer, args[2]);
        } else if (action.equals("set")) {
            // set bossbar
            if (args.length < 6) {
                return "title, color, style, progress must all be specified";
            }
            // set bossbar
            setTitle(targetPlayer, args[2], BarColor.valueOf(args[3]), BarStyle.valueOf(args[4]), Double.parseDouble(args[5]));
        } else {
            return "action must be set, remove, or clear";
        }

        return null;
    }

    protected BossBar getBossBar(Player player, String title) {
        String uuid = player.getUniqueId().toString();
        if (bossBar.containsKey(uuid)) {
            if (bossBar.get(uuid).containsKey(title)) {
                return bossBar.get(uuid).get(title);
            }
        }
        return null;
    }

    protected BossBar putBossBar(Player player, String title, BossBar bar) {
        String uuid = player.getUniqueId().toString();
        if (!bossBar.containsKey(uuid)) {
            bossBar.put(uuid, new HashMap<String, BossBar>());
        }
        bossBar.get(uuid).put(title, bar);
        return bar;
    }

    protected String parseTitle(String title) {
        return title.replaceAll("-", " ");
    }

    protected void setTitle(Player player, String title, BarColor color, BarStyle style, double progress) {
        // edit existing bossbar if it exists
        BossBar existing = getBossBar(player, title);
        if (existing != null) {
            existing.setProgress(progress);
            existing.setColor(color);
            existing.setStyle(style);
            return;
        }

        // create new bossbar
        BossBar bar = Bukkit.createBossBar(parseTitle(title), color, style);
        bar.setProgress(progress);
        putBossBar(player, title, bar);
        bar.addPlayer(player);
    }

    protected void removeTitle(Player player, String title) {
        BossBar bar = getBossBar(player, title);
        if (bar != null) {
            bar.removePlayer(player);
            if (bar.getPlayers().size() == 0) {
                bar.removeAll();
                bossBar.get(player.getUniqueId().toString()).remove(title);
            }
        }
    }

    protected void clearTitles(Player player) {
        String uuid = player.getUniqueId().toString();
        if (bossBar.containsKey(uuid)) {
            List<String> titles = new ArrayList<>(bossBar.get(uuid).keySet());
            for (String title : titles) {
                removeTitle(player, title);
            }
        }
    }

    private String availableColors() {
        String available = "";
        for (BarColor color : BarColor.values()) {
            available += color.name() + "|";
        }
        available = available.substring(0, available.length() - 1);
        return available;
    }

    private String availableStyles() {
        String available = "";
        for (BarStyle style : BarStyle.values()) {
            available += style.name() + "|";
        }
        available = available.substring(0, available.length() - 1);
        return available;
    }

    public String getSyntax() {
        return syntax;
    }

    public String getId() {
        return id;
    }

    public List<String> getAvailableBossBars(String uuid) {
        Entity target = MMExtUtils.parseUUID(uuid);
        String targetUUID = target.getUniqueId().toString();
        if (bossBar.containsKey(targetUUID)) {
            return bossBar.get(targetUUID).keySet().stream().collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    public List<String> getTabComplete(CommandSender sender, String[] argsList) {
        if (argsList.length == 1) {
            return MMExtUtils.getLivingEntityTabComplete(argsList[0]);
        } else if (argsList.length == 2) {
            return Arrays.asList("set", "remove", "clear");
        } else if (argsList.length >= 3) {
            if (argsList[1].equals("set")) {
                if (argsList.length == 3) {
                    // title
                    List<String> titles = getAvailableBossBars(argsList[0]);
                    if (titles.size() > 0) {
                        return titles;
                    }
                } else if (argsList.length == 4) {
                    // color
                    return Arrays.asList(BarColor.values())
                        .stream()
                        .map(Enum::name)
                        .collect(Collectors.toList());
                } else if (argsList.length == 5) {
                    // style
                    return Arrays.asList(BarStyle.values())
                        .stream()
                        .map(Enum::name)
                        .collect(Collectors.toList());
                } else if (argsList.length == 6) {
                    // progress
                }
            } else if (argsList[1].equals("remove")) {
                if (argsList.length == 3) {
                    // title
                    List<String> titles = getAvailableBossBars(argsList[0]);
                    if (titles.size() > 0) {
                        return titles;
                    }
                }
            }
        }
        
        return super.getTabComplete(sender, argsList);
    }
}
