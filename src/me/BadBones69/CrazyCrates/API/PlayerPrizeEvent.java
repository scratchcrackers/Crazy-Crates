package me.BadBones69.CrazyCrates.API;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerPrizeEvent extends Event{
	
	private Player player;
	private CrateType crateType;
	private String prize;
	private String crateName;
	
	private static final HandlerList handlers = new HandlerList();
	
	public HandlerList getHandlers() {
	    return handlers;
	}
	
	public static HandlerList getHandlerList() {
	    return handlers;
	}
	
	public PlayerPrizeEvent(Player player, CrateType crateType, String crateName, String prize){
		this.player = player;
		this.crateType = crateType;
		this.prize = prize;
		this.crateName = crateName;
	}
	
	public Player getPlayer(){
		return player;
	}
	
	public CrateType getCrateType(){
		return crateType;
	}
	
	public String getCrateName(){
		return crateName;
	}
	
	public String getPrize(){
		return prize;
	}
	
}