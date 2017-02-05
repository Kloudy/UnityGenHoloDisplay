package com.antarescraft.kloudy.unitygenholodisplay.datamodels;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.antarescraft.kloudy.hologuiapi.handlers.HoverHandler;
import com.antarescraft.kloudy.hologuiapi.handlers.HoverOutHandler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.antarescraft.kloudy.hologuiapi.HoloGUIPlugin;
import com.antarescraft.kloudy.hologuiapi.guicomponentproperties.ItemButtonComponentProperties;
import com.antarescraft.kloudy.hologuiapi.guicomponentproperties.LabelComponentProperties;
import com.antarescraft.kloudy.hologuiapi.guicomponents.ButtonComponent;
import com.antarescraft.kloudy.hologuiapi.guicomponents.ComponentPosition;
import com.antarescraft.kloudy.hologuiapi.guicomponents.GUIComponentFactory;
import com.antarescraft.kloudy.hologuiapi.guicomponents.GUIPage;
import com.antarescraft.kloudy.hologuiapi.guicomponents.ItemButtonComponent;
import com.antarescraft.kloudy.hologuiapi.guicomponents.LabelComponent;
import com.antarescraft.kloudy.hologuiapi.handlers.ClickHandler;
import com.antarescraft.kloudy.hologuiapi.handlers.GUIPageLoadHandler;
import com.antarescraft.kloudy.hologuiapi.playerguicomponents.PlayerGUIPage;
import com.antarescraft.kloudy.hologuiapi.playerguicomponents.PlayerGUIPageModel;

import me.charlie.unitygen.UnityGenAPI;
import net.md_5.bungee.api.ChatColor;

/*
 * Data model for the UnityGen page
 */
public class UnityPageModel extends PlayerGUIPageModel
{
	private PlayerGUIPage playerGUIPage;
	private ArrayList<ItemButtonComponent> playerUnityGenBtns;
	private int page = 0;//index of the current page of UnitGen buttons the player is looking at
	private int totalPages;

	private LabelComponent instructionsLabel;
	private ButtonComponent nextPageBtn;
	private ButtonComponent prevPageBtn;
	private LabelComponent pageLabel;
	
	public UnityPageModel(final HoloGUIPlugin plugin, GUIPage guiPage, final Player player, final String teleportMessage, final String noGensMessage)
	{
		super(plugin, guiPage, player);
		
		playerUnityGenBtns = new ArrayList<ItemButtonComponent>();

		instructionsLabel = (LabelComponent)guiPage.getComponent("instructions-label");
		nextPageBtn = (ButtonComponent)guiPage.getComponent("next-page-btn");
		prevPageBtn = (ButtonComponent)guiPage.getComponent("prev-page-btn");
		pageLabel = (LabelComponent)guiPage.getComponent("page-label");
		
		nextPageBtn.registerClickHandler(player, new ClickHandler()
		{
			@Override
			public void onClick()
			{
				playerGUIPage.renderComponent(prevPageBtn);
				
				page++;
				if(page >= totalPages)//last page
				{
					playerGUIPage.removeComponent("next-page-btn");
				}
				
				renderButtons();
			}
		});
		
		prevPageBtn.registerClickHandler(player, new ClickHandler()
		{
			@Override
			public void onClick()
			{
				playerGUIPage.renderComponent(nextPageBtn);
				
				page--;
				if(page <= 0)//first page
				{
					playerGUIPage.removeComponent("prev-page-btn");
				}
				
				renderButtons();
			}
		});
		
		guiPage.registerPageLoadHandler(player, new GUIPageLoadHandler()
		{
			@Override
			public void onPageLoad(PlayerGUIPage _playerGUIPage)
			{
				playerGUIPage = _playerGUIPage;

				player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1);
				
				//parse player UnityGen strings
				List<String> playerGenStrings = UnityGenAPI.getPlayerGens(player.getUniqueId());
				for(int i = 0; i < playerGenStrings.size(); i++)
				{
					//unityGenString format: <world_name>,x,y,z,<gen_name>
					String unityGenString = playerGenStrings.get(i);
					String[] unityGenTokens = unityGenString.split(",");
					
					final World world = Bukkit.getWorld(unityGenTokens[0]);
					final int x = Integer.parseInt(unityGenTokens[1]);
					final int y = Integer.parseInt(unityGenTokens[2]);
					final int z = Integer.parseInt(unityGenTokens[3]);
					final Location unityGenLocation = new Location(world, x, y, z);
					
					String unityGenName = unityGenTokens[4];
					
					final ItemButtonComponentProperties itemBtnProperties = new ItemButtonComponentProperties();
					itemBtnProperties.setId("unity-btn-" + i);
					itemBtnProperties.setAlwaysShowLabel(true);
					itemBtnProperties.setLabel(ChatColor.GOLD + "" + ChatColor.BOLD + unityGenName);
					itemBtnProperties.setOnclickSound(Sound.BLOCK_PORTAL_TRAVEL);
					itemBtnProperties.setItem(new ItemStack(unityGenLocation.getBlock().getType(), 1));
					itemBtnProperties.setPosition(new ComponentPosition(0, 0));
					itemBtnProperties.setRotation(new Vector(0, 0, 0));
					
					final ItemButtonComponent unityGenBtn = GUIComponentFactory.createItemButtonComponent(plugin, itemBtnProperties);

					//UnityGen button click handler
					unityGenBtn.registerClickHandler(player, new ClickHandler()
					{
						@Override
						public void onClick()
						{
							plugin.getHoloGUIApi().closeGUIPage(player);
							
							Location tpLocation = unityGenLocation.clone();
							tpLocation.setY(tpLocation.getY() + 1);
							player.teleport(tpLocation);
							
							player.sendMessage(ChatColor.translateAlternateColorCodes('&', teleportMessage));
						}
					});

					//UnityGen button hover handler
					unityGenBtn.registerHoverHandler(player, new HoverHandler()
					{
						@Override
						public void onHover()
						{
							player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BASS, 0.5f, 1);

							ComponentPosition btnPosition = unityGenBtn.getProperties().getPosition();
							ComponentPosition labelPosition = new ComponentPosition(btnPosition.getX(), btnPosition.getY() - 0.22);
							String[] text = new String[]{ "&e&l" + String.format("%s: (%d, %d, %d)", world.getName(), x, y, z) };

							LabelComponentProperties labelProperties = new LabelComponentProperties();
							labelProperties.setId(unityGenBtn.getProperties().getId() + "-label");
							labelProperties.setPosition(labelPosition);
							labelProperties.setLines((ArrayList<String>) Arrays.asList(text));
							labelProperties.setLabelDistance(3.75);

							LabelComponent infoLabel = GUIComponentFactory.createLabelComponent(plugin, labelProperties);
							playerGUIPage.renderComponent(infoLabel);
						}
					});

					//UnityGen button hover out handler
					unityGenBtn.registerHoverOutHandler(player, new HoverOutHandler()
					{
						@Override
						public void onHoverOut()
						{
							playerGUIPage.removeComponent( unityGenBtn.getProperties().getId() + "-label");
						}
					});

					playerUnityGenBtns.add(unityGenBtn);
				}
				
				totalPages = (playerUnityGenBtns.size() / 8) + 1;
				
				if(playerUnityGenBtns.size() > 8)//>1 pages of UnityGen buttons
				{
					playerGUIPage.renderComponent(nextPageBtn);
					playerGUIPage.renderComponent(pageLabel);
				}

				if(playerUnityGenBtns.size() == 0)//The player hasn't placed any UnityGen blocks, display the no unity gens message
				{
					//playerGUIPage.removeComponent("instructions-label");//remove the instructions

					//LabelComponent noGensLabel = instructionsLabel.clone();
					instructionsLabel.setLines((ArrayList<String>)Arrays.asList(new String[]{ ChatColor.translateAlternateColorCodes('&', noGensMessage) }));

					//playerGUIPage.renderComponent(noGensLabel);//render the label
				}
										
				renderButtons();
			}
		});
	}
	
	public String page()
	{
		return Integer.toString(page + 1);
	}
	
	public String totalPages()
	{
		return Integer.toString(totalPages + 1);
	}
	
	//removes all buttons
	private void removeButtons()
	{
		for(ItemButtonComponent button : playerUnityGenBtns)
		{
			playerGUIPage.removeComponent(button.getProperties().getId());
		}
	}
	
	//renders the current page of UnityGen buttons
	private void renderButtons()
	{
		removeButtons();
		
		//render 4x2 grid of buttons
		int index = page * 4;
		for(int i = 0; i < 2; i++)
		{
			for(int j = 0; j < 4; j++)
			{
				if(index >= playerUnityGenBtns.size()) break;
				
				ComponentPosition position = new ComponentPosition(0.65 - (j * 0.5), 0.2 - (i * 0.6));
				
				ItemButtonComponent unityGenBtn = playerUnityGenBtns.get(index);
				unityGenBtn.getProperties().setPosition(position);
				
				playerGUIPage.renderComponent(unityGenBtn);//render the button
				
				index++;
			}
		}
	}
}