package dev.covector.maplus.mmapihook;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import dev.covector.maplus.Utils;
import dev.covector.maplus.mmapihook.conditions.*;
import dev.covector.maplus.mmapihook.mechanics.*;
import dev.covector.maplus.mmapihook.placeholder.PlaceholderRegister;
import dev.covector.maplus.mmapihook.listeners.*;
import dev.covector.maplus.mmapihook.targeters.*;
import io.lumine.mythic.api.config.MythicConfig;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.events.MythicConditionLoadEvent;
import io.lumine.mythic.bukkit.events.MythicDropLoadEvent;
import io.lumine.mythic.bukkit.events.MythicMechanicLoadEvent;
import io.lumine.mythic.bukkit.events.MythicTargeterLoadEvent;
import io.lumine.mythic.core.skills.SkillExecutor;

import java.io.File;

public class MMApiHook implements Listener{
    public void register() {
        Bukkit.getPluginManager().registerEvents(this, Utils.getPlugin());
		registerListeners();
    }

    public void unregister() {
        MythicMechanicLoadEvent.getHandlerList().unregister(this);
        MythicConditionLoadEvent.getHandlerList().unregister(this);
        MythicDropLoadEvent.getHandlerList().unregister(this);
        MythicTargeterLoadEvent.getHandlerList().unregister(this);
		unregisterListeners();
    }

    @EventHandler
	public void onMythicMechanicLoad(MythicMechanicLoadEvent event)	{
		String skillNString = event.getMechanicName();
		SkillExecutor manager = MythicBukkit.inst().getSkillManager();
		MythicLineConfig config = event.getConfig();
		if (event.getMechanicName().equalsIgnoreCase("SUMMONCENTER") || event.getMechanicName().equalsIgnoreCase("SUMMONCENTRE")) {
			event.register(new SummonCenter(event.getConfig()));
		}
		if (event.getMechanicName().equalsIgnoreCase("SETTEMPBLOCK") || event.getMechanicName().equalsIgnoreCase("TEMPBLOCK")) {
			event.register(new SetTempBlock(manager, null, skillNString, config));
		}
		if (event.getMechanicName().equalsIgnoreCase("REMOVETEMPBLOCK")) {
			event.register(new RemoveTempBlock(manager, null, skillNString, config));
		}
		if (event.getMechanicName().equalsIgnoreCase("REMOVEALLTEMPBLOCK") || event.getMechanicName().equalsIgnoreCase("REMOVEALLTEMPBLOCKS")) {
			event.register(new RemoveAllTempBlock(manager, null, skillNString, config));
		}
		if (event.getMechanicName().equalsIgnoreCase("BARCREATEUNI")) {
			event.register(new BarCreateUni(event.getConfig()));
		}
		if (event.getMechanicName().equalsIgnoreCase("FIXBARUNICODE")) {
			event.register(new FixBarUnicode(event.getConfig()));
		}
	}
	
	@EventHandler
	public void onMythicConditionLoad(MythicConditionLoadEvent event)	{
		if (event.getConditionName().equalsIgnoreCase("ISCHILDFIX")) {
			event.register(new IsChildFix(event.getConfig()));
		}
		if (event.getConditionName().equalsIgnoreCase("ISSOLID")) {
			event.register(new IsSolid(event.getConfig()));
		}
		if (event.getConditionName().equalsIgnoreCase("BLOCKSABOVEGROUND")) {
			event.register(new BlocksAboveGround(event.getConfig(), event.getArgument()));
		}
		if (event.getConditionName().equalsIgnoreCase("ISSNEAKING")) {
			event.register(new IsSneaking(event.getConfig()));
		}
		if (event.getConditionName().equalsIgnoreCase("ISRIDING") || event.getConditionName().equalsIgnoreCase("ISRIDDENON")) {
			event.register(new IsRiding(event.getConfig()));
		}
	}
	

	// @EventHandler
	// public void onMythicDropLoad(MythicDropLoadEvent event)	{
	// }

    @EventHandler
	public void onMythicTargeterLoad(MythicTargeterLoadEvent event)	{
		SkillExecutor manager = MythicBukkit.inst().getSkillManager();
		if (event.getTargeterName().equalsIgnoreCase("NEARESTPLAYERFROMCURSOR") || event.getTargeterName().equalsIgnoreCase("NPFC")) {
			event.register(new NearestPlayerFromCursor(manager, event.getConfig()));
		}
	}

	HealListener healListener = new HealListener();
	// PlaceholderRegister placeholderRegister = new PlaceholderRegister();

	private void registerListeners() {
		healListener.register();

		// placeholderRegister.load();
	}

	private void unregisterListeners() {
		healListener.unregister();

		// placeholderRegister.unload();
	}
}
