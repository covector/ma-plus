package dev.covector.maplus.mmapihook;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import dev.covector.maplus.Utils;
import dev.covector.maplus.mmapihook.conditions.*;
import dev.covector.maplus.mmapihook.mechanics.*;
import io.lumine.mythic.bukkit.events.MythicConditionLoadEvent;
import io.lumine.mythic.bukkit.events.MythicDropLoadEvent;
import io.lumine.mythic.bukkit.events.MythicMechanicLoadEvent;
import io.lumine.mythic.bukkit.events.MythicTargeterLoadEvent;

public class MMApiHook implements Listener{
    public void register() {
        Bukkit.getPluginManager().registerEvents(this, Utils.getPlugin());
    }

    public void unregister() {
        MythicMechanicLoadEvent.getHandlerList().unregister(this);
        MythicConditionLoadEvent.getHandlerList().unregister(this);
        MythicDropLoadEvent.getHandlerList().unregister(this);
        MythicTargeterLoadEvent.getHandlerList().unregister(this);
    }

    @EventHandler
	public void onMythicMechanicLoad(MythicMechanicLoadEvent event)	{
		if (event.getMechanicName().equalsIgnoreCase("SUMMONCENTER") || event.getMechanicName().equalsIgnoreCase("SUMMONCENTRE")) {
			event.register(new SummonCenter(event.getConfig()));
		}
		if (event.getMechanicName().equalsIgnoreCase("SETTEMPBLOCK") || event.getMechanicName().equalsIgnoreCase("TEMPBLOCK")) {
			event.register(new SetTempBlock(event.getConfig()));
		}
		if (event.getMechanicName().equalsIgnoreCase("REMOVETEMPBLOCK")) {
			event.register(new RemoveTempBlock(event.getConfig()));
		}
		if (event.getMechanicName().equalsIgnoreCase("REMOVEALLTEMPBLOCK") || event.getMechanicName().equalsIgnoreCase("REMOVEALLTEMPBLOCKS")) {
			event.register(new RemoveAllTempBlock(event.getConfig()));
		}
	}
	
	@EventHandler
	public void onMythicConditionLoad(MythicConditionLoadEvent event)	{
		if (event.getConditionName().equalsIgnoreCase("ISCHILDFIX")) {
			event.register(new IsChildFix(event.getConfig()));
		}
	}
	

	// @EventHandler
	// public void onMythicDropLoad(MythicDropLoadEvent event)	{
	// }

    // @EventHandler
	// public void onMythicTargeterLoad(MythicTargeterLoadEvent event)	{
	// }
}
