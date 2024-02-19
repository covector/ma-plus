package dev.covector.maplus.mmextension.def;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;
import org.bukkit.Location;

import org.inventivetalent.glow.GlowAPI;

import dev.covector.maplus.mmextension.Ability;
import dev.covector.maplus.mmextension.MMExtUtils;

public class SetGlow extends Ability {
    private String syntax = "<target-uuid> <observer-uuid> <chat-color>";
    private String id = "setGlow";

    public String cast(String[] args) {
        if (args.length != 3) {
            return "args length must be 3";
        }
        
        Entity target = MMExtUtils.parseUUID(args[0]);
        Entity observer = MMExtUtils.parseUUID(args[1]);
        GlowAPI.Color color = args[2].toUpperCase().equals("NULL") ? null : GlowAPI.Color.valueOf(args[2]);

        if (!(observer instanceof Player)) {
            return "observer must be a player";
        }
        Player observerPlayer = (Player) observer;

        GlowAPI.setGlowing(target, color, observerPlayer);

        return null;
    }

    public String getSyntax() {
        return syntax;
    }

    public String getId() {
        return id;
    }
}
