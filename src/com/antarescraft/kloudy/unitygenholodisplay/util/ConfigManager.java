package com.antarescraft.kloudy.unitygenholodisplay.util;

import org.bukkit.configuration.file.FileConfiguration;

import com.antarescraft.kloudy.unitygenholodisplay.UnityGenHoloDisplay;

/*
 * Manager class to manage configuration logic
 */
public class ConfigManager 
{
	private UnityGenHoloDisplay plugin;
	
	private String teleportMessage;
	private String noGensMessage;
	
	public ConfigManager(UnityGenHoloDisplay plugin)
	{
		this.plugin = plugin;
		
		loadConfigValues();
	}
	
	/*
	 * Load config values from config.yml
	 */
	public void loadConfigValues()
	{
		FileConfiguration root = plugin.getConfig();
		
		teleportMessage = root.getString("teleport-message", "");
		noGensMessage = root.getString("no-unity-gens-msg", "");
	}
	
	/*
	 * Getter Functions
	 */
	
	public String getTeleportMessage()
	{
		return teleportMessage;
	}
	public String getNoGensMessage(){ return noGensMessage; }
}