package dev.covector.maplus;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import com.garbagemule.MobArena.MobArena;
import com.garbagemule.MobArena.framework.Arena;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;

public class Utils {
    private static MobArena mobarena;
    private static MobArenaPlusPlugin plugin;
    private static ArrayList<Destructor> destructors = new ArrayList<>();
    public static Random random = new Random();

    public static void setMobArena(MobArena mobarena) {
        Utils.mobarena = mobarena;
    }

    public static void setPlugin(MobArenaPlusPlugin plugin) {
        Utils.plugin = plugin;
    }

    public static MobArena getMobArena() {
        return mobarena;
    }

    public static Arena getArena(String name) {
        return mobarena.getArenaMaster().getArenaWithName(name);
    }

    public static List<Arena> getAllArenas() {
        return mobarena.getArenaMaster().getEnabledArenas();
    }

    public static Arena getFirstActiveArena() {
        return mobarena.getArenaMaster().getArenas().stream().filter(arena -> arena.getPlayersInArena().size() > 0).findFirst().orElse(null);
    }

    public static Arena getArenaWithPlayer(Player player) {
        return mobarena.getArenaMaster().getArenaWithPlayer(player);
    }

    public static Arena getArenaWithMonster(Entity entity) {
        return mobarena.getArenaMaster().getArenaWithMonster(entity);
    }

    public static MobArenaPlusPlugin getPlugin() {
        return plugin;
    }

    public static void addDestructor(Destructor destructor) {
        destructors.add(destructor);
    }

    public static void destroyAll() {
        for (Destructor destructor : destructors) {
            destructor.destroy();
        }
    }

    public static String[] parseSpaces(String[] args) {
        ArrayList<String> newArgs = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder currentArg = new StringBuilder();
        for (String arg : args) {
            if (inQuotes) {
                if (arg.endsWith("\"")) {
                    // end of string
                    inQuotes = false;
                    currentArg.append(" " + arg.substring(0, arg.length() - 1));
                    newArgs.add(currentArg.toString());
                    currentArg.setLength(0);
                } else {
                    // continue string
                    currentArg.append(" " + arg);
                }
            } else {
                if (arg.contains("\"")) {
                    // get word without first quote
                    int firstQuoteInd = arg.indexOf("\"");
                    currentArg.append(arg).deleteCharAt(firstQuoteInd);

                    // start of string
                    if (currentArg.length() > 0 && currentArg.charAt(currentArg.length() - 1) == '"') {
                        // no spaces
                        currentArg.deleteCharAt(currentArg.length() - 1); // remove ending quote
                        newArgs.add(currentArg.toString());
                        currentArg.setLength(0);
                    } else {
                        inQuotes = true;
                    }
                } else {
                    // normal parse
                    newArgs.add(arg);
                }
            }
        }

        // leftover
        if (inQuotes) {
            newArgs.add(currentArg.toString());
        }

        return newArgs.toArray(new String[0]);
    }

    public interface Destructor {
        void destroy();
    }
}