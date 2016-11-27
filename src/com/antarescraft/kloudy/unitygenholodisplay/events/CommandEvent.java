package com.antarescraft.kloudy.unitygenholodisplay.events;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.antarescraft.kloudy.hologuiapi.plugincore.command.CommandHandler;
import com.antarescraft.kloudy.hologuiapi.plugincore.command.CommandParser;
import com.antarescraft.kloudy.hologuiapi.plugincore.messaging.MessageManager;
import com.antarescraft.kloudy.unitygenholodisplay.UnityGenHoloDisplay;
import com.antarescraft.kloudy.unitygenholodisplay.datamodels.UnityPageModel;

public class CommandEvent implements CommandExecutor
{
	private UnityGenHoloDisplay plugin;
	
	public CommandEvent(UnityGenHoloDisplay plugin)
	{
		this.plugin = plugin;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) 
	{
		return CommandParser.parseCommand(plugin, this, "ugd", cmd.getName(), sender, args);
	}
	
	@CommandHandler(description = "Reloads the config values", 
			mustBePlayer = false, permission = "unitygen.admin", subcommands = "reload")
	public void reload(CommandSender sender, String[] args)
	{
		plugin.getHoloGUIApi().destroyGUIPages(plugin);
		plugin.loadGUIPages();
		plugin.reloadConfig();
		plugin.getConfigManager().loadConfigValues();
		
		MessageManager.success(sender, "Reloaded config values.");
	}
	
	@CommandHandler(description = "Opens the UnityGen HoloGUI menu", 
			mustBePlayer = true, permission = "unitygen.display", subcommands = "menu")
	public void openDisplay(CommandSender sender, String[] args)
	{
		Player player = (Player)sender;
				
		UnityPageModel model = new UnityPageModel(plugin, plugin.getGUIPage("unitygen-page"), player, plugin.getConfigManager().getTeleportMessage());
		plugin.getHoloGUIApi().openGUIPage(plugin, player, model);
	}
	
	@CommandHandler(description = "Closes the UnityGen HoloGUI menu", 
			mustBePlayer = false, permission = "unitygen.display", subcommands = "close")
	public void closeDisplay(CommandSender sender, String[] args)
	{
		Player player = (Player)sender;
		
		plugin.getHoloGUIApi().closeGUIPage(player);
	}
}