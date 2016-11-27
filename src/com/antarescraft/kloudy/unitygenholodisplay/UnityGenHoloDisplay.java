package com.antarescraft.kloudy.unitygenholodisplay;

import com.antarescraft.kloudy.hologuiapi.HoloGUIPlugin;
import com.antarescraft.kloudy.unitygenholodisplay.events.CommandEvent;
import com.antarescraft.kloudy.unitygenholodisplay.util.ConfigManager;

public class UnityGenHoloDisplay extends HoloGUIPlugin
{
	private ConfigManager configManager;
	
	@Override
	public void onEnable()
	{
		saveDefaultConfig();
		
		configManager = new ConfigManager(this);
		configManager.loadConfigValues();
		
		getHoloGUIApi().hookHoloGUIPlugin(this);//hook this plugin into HoloGUIApi
		loadGUIPages();//load the yaml files in the plugin folder into HoloGUIApi
		
		getCommand("ugd").setExecutor(new CommandEvent(this));
	}
	
	@Override
	public void onDisable()
	{
		getHoloGUIApi().destroyGUIPages(this);//destroy all gui pages that players may have been looking when the plugin got disabled
		getHoloGUIApi().unhookHoloGUIPlugin(this);//unhook the plugin from HoloGUIApi
	}
	
	public ConfigManager getConfigManager()
	{
		return configManager;
	}
}