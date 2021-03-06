package me.BadBones69.CrazyCrates;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.BadBones69.CrazyCrates.API.CrateType;
import me.BadBones69.CrazyCrates.API.KeyType;
import me.BadBones69.CrazyCrates.CrateTypes.CSGO;
import me.BadBones69.CrazyCrates.CrateTypes.Cosmic;
import me.BadBones69.CrazyCrates.CrateTypes.QCC;
import me.BadBones69.CrazyCrates.CrateTypes.Roulette;
import me.BadBones69.CrazyCrates.CrateTypes.Wheel;
import me.BadBones69.CrazyCrates.CrateTypes.Wonder;

public class GUI implements Listener{
	
	public static HashMap<Player, String> Crate = new HashMap<Player, String>();
	
	public static void openGUI(Player player){
		Inventory inv = Bukkit.createInventory(null, Main.settings.getConfig().getInt("Settings.InventorySize"), Methods.color(Main.settings.getConfig().getString("Settings.InventoryName")));
		for(String crate : Main.settings.getAllCratesNames()){
			if(!Main.settings.getFile(crate).contains("Crate.InGUI")){
				Main.settings.getFile(crate).set("Crate.InGUI", true);
				Main.settings.saveAll();
			}
			if(Main.settings.getFile(crate).getBoolean("Crate.InGUI")){
				String path = "Crate.";
				int slot = Main.settings.getFile(crate).getInt(path+"Slot")-1;
				String ma = Main.settings.getFile(crate).getString(path+"Item");
				String name = Main.settings.getFile(crate).getString(path+"Name");
				ArrayList<String> lore = new ArrayList<String>();
				String keys = NumberFormat.getNumberInstance().format(Methods.getKeys(player, crate));
				for(String i : Main.settings.getFile(crate).getStringList(path+"Lore")){
					lore.add(i
							.replaceAll("%Keys%", keys).replaceAll("%keys%", keys)
							.replaceAll("%Player%", player.getName()).replaceAll("%player%", player.getName()));
				}
				inv.setItem(slot, Methods.makeItem(ma, 1, name, lore));
			}
		}
		player.openInventory(inv);
	}
	
	public static void openPreview(Player player, String crate){
		int slots = 9;
		for(int size = Main.settings.getFile(crate).getConfigurationSection("Crate.Prizes").getKeys(false).size(); size > 9 && slots < 54; size -= 9){
			slots += 9;
		}
		Inventory inv = Bukkit.createInventory(null, slots, Methods.color(Main.settings.getFile(crate).getString("Crate.Name")));
		for(String reward : Main.settings.getFile(crate).getConfigurationSection("Crate.Prizes").getKeys(false)){
			String id = Main.settings.getFile(crate).getString("Crate.Prizes."+reward+".DisplayItem");
			String name = Main.settings.getFile(crate).getString("Crate.Prizes."+reward+".DisplayName");
			List<String> lore = Main.settings.getFile(crate).getStringList("Crate.Prizes."+reward+".Lore");
			HashMap<Enchantment, Integer> enchantments = new HashMap<Enchantment, Integer>();
			Boolean glowing = false;
			int amount = 1;
			if(Main.settings.getFile(crate).contains("Crate.Prizes."+reward+".Glowing")){
				glowing = Main.settings.getFile(crate).getBoolean("Crate.Prizes."+reward+".Glowing");
			}
			if(Main.settings.getFile(crate).contains("Crate.Prizes."+reward+".DisplayAmount")){
				amount = Main.settings.getFile(crate).getInt("Crate.Prizes."+reward+".DisplayAmount");
			}
			if(Main.settings.getFile(crate).contains("Crate.Prizes."+reward+".DisplayEnchantments")){
				for(String enchant : Main.settings.getFile(crate).getStringList("Crate.Prizes."+reward+".DisplayEnchantments")){
					String[] b = enchant.split(":");
					enchantments.put(Enchantment.getByName(b[0]), Integer.parseInt(b[1]));
				}
			}
			try{
				if(enchantments.size() > 0){
					inv.setItem(inv.firstEmpty(), Methods.makeItem(id, amount, name, lore, enchantments, glowing));
				}else{
					inv.setItem(inv.firstEmpty(), Methods.makeItem(id, amount, name, lore, glowing));
				}
			}catch(Exception e){
				inv.addItem(Methods.makeItem(Material.STAINED_CLAY, 1, 14, "&c&lERROR", Arrays.asList("&cThere is an error","&cFor the reward: &c"+reward)));
			}
		}
		player.openInventory(inv);
	}
	
	@EventHandler
	public void onInvClick(InventoryClickEvent e){
		Player player = (Player) e.getWhoClicked();
		Inventory inv = e.getInventory();
		FileConfiguration config = Main.settings.getConfig();
		if(inv!=null){
			for(String crate : Methods.getCrates()){
				if(inv.getName().equals(Methods.color(Main.settings.getFile(crate).getString("Crate.Name")))){
					e.setCancelled(true);
					return;
				}
			}
			if(inv.getName().equals(Methods.color(config.getString("Settings.InventoryName")))){
				e.setCancelled(true);
				if(e.getCurrentItem() != null){
					ItemStack item = e.getCurrentItem();
					if(item.hasItemMeta()){
						if(item.getItemMeta().hasDisplayName()){
							for(String crate : Main.settings.getAllCratesNames()){
								String path = "Crate.";
								if(item.getItemMeta().getDisplayName().equals(Methods.color(Main.settings.getFile(crate).getString(path+"Name")))){
									if(e.getAction() == InventoryAction.PICKUP_HALF){
										if(config.getBoolean("Settings.Show-Preview")){
											player.closeInventory();
											openPreview(player, crate);
										}
										return;
									}
									if(Crate.containsKey(player)){
										player.sendMessage(Methods.color(Methods.getPrefix()+config.getString("Settings.Crate-Already-Opened")));
										return;
									}
									if(Methods.getKeys(player, crate)<1){
										String msg = config.getString("Settings.NoVirtualKeyMsg");
										player.sendMessage(Methods.color(Methods.getPrefix()+msg));
										return;
									}
									for(String world : getDisabledWorlds()){
										if(world.equalsIgnoreCase(player.getWorld().getName())){
											player.sendMessage(Methods.color(Methods.getPrefix() + config.getString("Settings.WorldDisabledMsg")
											.replaceAll("%World%", player.getWorld().getName()).replaceAll("%world%", player.getWorld().getName())));
											return;
										}
									}
									if(Methods.isInvFull(player)){
										if(config.contains("Settings.Inventory-Full")){
											player.sendMessage(Methods.color(Methods.getPrefix() + config.getString("Settings.Inventory-Full")));
										}else{
											player.sendMessage(Methods.color(Methods.getPrefix() + "&cYour inventory is full, please make room before opening a crate."));
										}
										return;
									}
									switch(CrateType.getFromName(Main.settings.getFile(crate).getString("Crate.CrateType"))){
										case COSMIC:
											Crate.put(player, crate);
											CrateControl.Crate.put(player, crate);
											Methods.Key.put(player, KeyType.VIRTUAL_KEY);
											Cosmic.openCosmic(player);
											break;
										case CRATE_ON_THE_GO:
											player.sendMessage(Methods.color(Methods.getPrefix() + config.getString("Settings.Cant-Be-Virtual-Crate")));
											break;
										case CSGO:
											Crate.put(player, crate);
											CrateControl.Crate.put(player, crate);
											Methods.Key.put(player, KeyType.VIRTUAL_KEY);
											CSGO.openCSGO(player);
											if(Main.settings.getFile(GUI.Crate.get(player)).getBoolean("Crate.OpeningBroadCast")){
												Bukkit.broadcastMessage(Methods.color(Main.settings.getFile(GUI.Crate.get(player)).getString("Crate.BroadCast")
														.replaceAll("%Prefix%", Methods.getPrefix()).replaceAll("%prefix%", Methods.getPrefix())
														.replaceAll("%Player%", player.getName()).replaceAll("%player%", player.getName())));
											}
											break;
										case FIRE_CRACKER:
											player.sendMessage(Methods.color(Methods.getPrefix() + config.getString("Settings.Cant-Be-Virtual-Crate")));
											break;
										case MENU:
											break;
										case QUAD_CRATE:
											player.sendMessage(Methods.color(Methods.getPrefix() + config.getString("Settings.Cant-Be-Virtual-Crate")));
											break;
										case QUICK_CRATE:
											Crate.put(player, crate);
											CrateControl.Crate.put(player, crate);
											Methods.Key.put(player, KeyType.VIRTUAL_KEY);
											QCC.startBuild(player, player.getLocation(), Material.CHEST);
											break;
										case ROULETTE:
											Crate.put(player, crate);
											CrateControl.Crate.put(player, crate);
											Methods.Key.put(player, KeyType.VIRTUAL_KEY);
											Roulette.openRoulette(player);
											if(Main.settings.getFile(GUI.Crate.get(player)).getBoolean("Crate.OpeningBroadCast")){
												Bukkit.broadcastMessage(Methods.color(Main.settings.getFile(GUI.Crate.get(player)).getString("Crate.BroadCast")
														.replaceAll("%Prefix%", Methods.getPrefix()).replaceAll("%prefix%", Methods.getPrefix())
														.replaceAll("%Player%", player.getName()).replaceAll("%player%", player.getName())));
											}
											break;
										case WHEEL:
											Crate.put(player, crate);
											CrateControl.Crate.put(player, crate);
											Methods.Key.put(player, KeyType.VIRTUAL_KEY);
											Wheel.startWheel(player);
											break;
										case WONDER:
											Crate.put(player, crate);
											CrateControl.Crate.put(player, crate);
											Methods.Key.put(player, KeyType.VIRTUAL_KEY);
											Wonder.startWonder(player);
											break;
									}
									return;
								}
							}
						}
					}
				}
			}
		}
	}
	
	private ArrayList<String> getDisabledWorlds(){
		ArrayList<String> worlds = new ArrayList<String>();
		for(String world : Main.settings.getConfig().getStringList("Settings.DisabledWorlds")){
			worlds.add(world);
		}
		return worlds;
	}
	
}