package com.antarescraft.kloudy.unitygenholodisplay.datamodels;

import java.util.ArrayList;
import java.util.List;

import com.antarescraft.kloudy.hologuiapi.handlers.HoverHandler;
import com.antarescraft.kloudy.hologuiapi.handlers.HoverOutHandler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.antarescraft.kloudy.hologuiapi.HoloGUIPlugin;
import com.antarescraft.kloudy.hologuiapi.guicomponents.ButtonComponent;
import com.antarescraft.kloudy.hologuiapi.guicomponents.ComponentPosition;
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
	private ItemButtonComponent genBtnTemplate;//clones of this component are used to create each UnityGen button
	private LabelComponent hoverLabelTemplate;//clones of this component are used to create each UnitGen button label
	private ButtonComponent nextPageBtn;
	private ButtonComponent prevPageBtn;
	private LabelComponent pageLabel;
	
	public UnityPageModel(final HoloGUIPlugin plugin, GUIPage guiPage, final Player player, final String teleportMessage, final String noGensMessage)
	{
		super(plugin, guiPage, player);
		
		playerUnityGenBtns = new ArrayList<ItemButtonComponent>();

		instructionsLabel = (LabelComponent)guiPage.getComponent("instructions-label");
		genBtnTemplate = (ItemButtonComponent)guiPage.getComponent("unitygen-btn-template");
		hoverLabelTemplate = (LabelComponent)guiPage.getComponent("hover-label-template");
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
					
					final ItemButtonComponent unityGenBtn = genBtnTemplate.clone();
					unityGenBtn.setId("unity-btn-" + i);
					unityGenBtn.setLabel(ChatColor.GOLD + "" + ChatColor.BOLD + unityGenName);
					unityGenBtn.setItem(new ItemStack(unityGenLocation.getBlock().getType()));
					
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

							ComponentPosition btnPosition = unityGenBtn.getPosition();
							ComponentPosition labelPosition = new ComponentPosition(btnPosition.getX(), btnPosition.getY() - 0.22);
							String[] text = new String[]{ "&e&l" + String.format("%s: (%d, %d, %d)", world.getName(), x, y, z) };

							LabelComponent infoLabel = hoverLabelTemplate.clone();
							infoLabel.setId(unityGenBtn.getId() + "-label");
							infoLabel.setPosition(labelPosition);
							infoLabel.setLines(text);
							infoLabel.setLabelDistance(3.75);

							playerGUIPage.renderComponent(infoLabel);
						}
					});

					//UnityGen button hover out handler
					unityGenBtn.registerHoverOutHandler(player, new HoverOutHandler()
					{
						@Override
						public void onHoverOut()
						{
							playerGUIPage.removeComponent( unityGenBtn.getId() + "-label");
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
					playerGUIPage.removeComponent("instructions-label");//remove the instructions

					LabelComponent noGensLabel = instructionsLabel.clone();
					noGensLabel.setLines(new String[]{ ChatColor.translateAlternateColorCodes('&', noGensMessage) });

					playerGUIPage.renderComponent(noGensLabel);//render the label
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
			playerGUIPage.removeComponent(button.getId());
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
				unityGenBtn.setPosition(position);
				
				playerGUIPage.renderComponent(unityGenBtn);//render the button
				
				index++;
			}
		}
	}
}