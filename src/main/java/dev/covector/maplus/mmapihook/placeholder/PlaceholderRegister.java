package dev.covector.maplus.mmapihook.placeholder;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import dev.covector.maplus.Utils;
import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.events.MythicPostReloadedEvent;
import io.lumine.mythic.core.skills.placeholders.Placeholder;
import io.lumine.mythic.core.skills.placeholders.PlaceholderExecutor;

public class PlaceholderRegister implements Listener{
    public void registerPlaceholder() {
        PlaceholderExecutor manager = MythicBukkit.inst().getPlaceholderManager();

        // manager.register("target.block.x", (Placeholder)Placeholder.meta((meta, arg) -> {
        //     if (meta instanceof SkillMetadata data) {
        //        if (!data.getLocationTargets().isEmpty()) {
        //           return String.valueOf(((AbstractLocation[])data.getLocationTargets().toArray())[0].getX());
        //        }
        //     }
        //     return "null";

        // }));
        // manager.register("target.block.y", (Placeholder)Placeholder.location((location, arg) -> String.valueOf(BukkitAdapter.adapt(location).getY())));
        // manager.register("target.block.z", (Placeholder)Placeholder.location((location, arg) -> String.valueOf(location.getZ())));
        // manager.register("target.block.w", (Placeholder)Placeholder.location((location, arg) -> location.getWorld().getName()));
    }

    @EventHandler
    public void onPostReload(MythicPostReloadedEvent event) {
        registerPlaceholder();
    }

    public void load() {
        if (!Bukkit.getServer().getPluginManager().isPluginEnabled("MythicMobs")) {
            Utils.getPlugin().getLogger().warning("MobArena Not Loaded!");
            return;
        }

        registerPlaceholder();
        Bukkit.getPluginManager().registerEvents(this, Utils.getPlugin());
    }

    public void unload() {
        MythicPostReloadedEvent.getHandlerList().unregister(this);
    }
}
