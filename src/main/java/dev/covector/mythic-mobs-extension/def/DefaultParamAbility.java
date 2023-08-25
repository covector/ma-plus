package dev.covector.maplus.mmextension;

import org.bukkit.Bukkit;

import java.util.HashMap;

public abstract class DefaultParamAbility extends Ability {
    private HashMap<String, String> defaultValues = new HashMap<String, String>();

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
}
