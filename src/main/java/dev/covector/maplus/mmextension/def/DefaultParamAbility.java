package dev.covector.maplus.mmextension.def;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import dev.covector.maplus.mmextension.Ability;
import dev.covector.maplus.mmextension.MMExtUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class DefaultParamAbility extends Ability {
    private HashMap<String, String> defaultValues = new HashMap<String, String>();
    private HashMap<String, List<String>> tabComplete = new HashMap<String, List<String>>();

    protected ParsedParam parse(String[] args) {
        ParsedParam parsedParam = new ParsedParam();
        for (int i = 0; i < args.length; i++) {
            String[] split = args[i].split(":");
            if (split.length != 2) {
                split = args[i].split("=");
            }
            if (split.length != 2) {
                throw new IllegalArgumentException("Invalid argument: " + args[i]);
            }
            parsedParam.put(split[0], split[1]);
        }
        if (getParam(parsedParam, "debug") != null && getBoolean(parsedParam, "debug")) {
            parsedParam.DebugLog();
        }
        return parsedParam;
    }

    protected String getParam(ParsedParam params, String key) {
        String value = params.getParam(key);
        return value == null ? defaultValues.get(key) : value;
    }

    protected boolean getBoolean(ParsedParam params, String key) {
        if (getParam(params, key) == null) {
            return false;
        }
        String value = getParam(params, key);
        return value.equals("1") || Boolean.parseBoolean(value);
    }

    protected int getInt(ParsedParam params, String key) {
        return Integer.parseInt(getParam(params, key));
    }

    protected double getDouble(ParsedParam params, String key) {
        return Double.parseDouble(getParam(params, key));
    }

    protected float getFloat(ParsedParam params, String key) {
        return Float.parseFloat(getParam(params, key));
    }


    protected void setDefault(String key, String value) {
        defaultValues.put(key, value);
    }

    protected void setDefault(String key, String value, List<String> tabCompleteList) {
        defaultValues.put(key, value);
        tabComplete.put(key, tabCompleteList);
    }

    protected class ParsedParam {
        private HashMap<String, String> params = new HashMap<String, String>();

        protected void put(String key, String value) {
            params.put(key, value);
        }

        protected String getParam(String key) {
            return params.get(key);
        }
        
        protected void DebugLog() {
            Bukkit.broadcastMessage("Debug parameters: ");
            for (String key : params.keySet()) {
                Bukkit.broadcastMessage(key + ": " + params.get(key));
            }
        }
    }

    public List<String> getTabComplete(CommandSender sender, String[] argsList) {
        String arg = argsList[argsList.length - 1];

        if (arg.contains(":") || arg.contains("=")) {
            String delimiter = arg.contains(":") ? ":" : "=";
            String[] split;
            if (arg.endsWith(delimiter)) {
                split = new String[] {arg.substring(0, arg.length() - 1), ""};
            } else {
                split = arg.split(delimiter);
            }

            if (split.length != 2) {
                return Collections.emptyList();
            }
            List<String> tabCompList = tabComplete.getOrDefault(split[0], Collections.emptyList());
            
            if (tabCompList.size() == 1 && tabCompList.get(0) == "@e") {
                // living entity tab complete
                if (sender instanceof Player) {
                    return MMExtUtils.getLivingEntityTabComplete(split[1], (Player) sender).stream().map(
                        n -> split[0] + delimiter + n
                    ).collect(Collectors.toList());
                } else {
                    return Collections.emptyList();
                }
            }

            if (split[0].equals("debug")) {
                return MMExtUtils.streamFilter(
                    Stream.of("true", "false")
                , split[1]).stream().map(
                    n -> split[0] + delimiter + n
                ).collect(Collectors.toList());
            }

            return MMExtUtils.streamFilter(tabCompList.stream()
                .map(n -> split[0] + delimiter + n)
            , arg);
        } else {
            return MMExtUtils.streamFilter(
                Stream.concat(
                    defaultValues.keySet().stream()
                    .map(n -> n + ":"),
                    Stream.of("debug:")
                )
            , argsList[argsList.length - 1]);
        }
    }

    protected static List<String> booleanTabOptions = List.of("true", "false");
    protected static List<String> livingEntityTabOptions = List.of("@e");
}
