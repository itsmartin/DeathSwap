package com.martinbrook.DeathSwap;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class DeathSwapPlayer {
	private String name;
	boolean ready;
	boolean alive;
	DeathSwap plugin;

	public DeathSwapPlayer(String name, DeathSwap plugin) {
		this.name = name;
		this.alive=true;
		this.plugin = plugin;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setReady() {
		ready = true;
	}
	
	public void setDead() {
		alive = false;
	}
	
	public boolean isReady() {
		return ready;
	}
	
	public boolean isAlive() {
		return alive;
	}
	
	public Player getPlayer() { return plugin.getServer().getPlayerExact(name); }
	
	public boolean giveResistance(int duration) {
		Player p = getPlayer();
		if (p==null) return false;
		p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, duration * 20, 10));
		return true;
	}
	
	public boolean renew() {
		return (heal() && feed() && clearXP() && clearPotionEffects() && clearInventory());
	}
	
	/**
	 * Heal the player
	 */
	public boolean heal() {
		Player p = getPlayer();
		if (p==null) return false;
		p.setHealth(20);
		return true;
	}

	/**
	 * Feed the player
	 */
	public boolean feed() {
		Player p = getPlayer();
		if (p==null) return false;
		p.setFoodLevel(20);
		p.setExhaustion(0.0F);
		p.setSaturation(5.0F);
		return true;
	}

	/**
	 * Reset XP of the given player
	 */
	public boolean clearXP() {
		Player p = getPlayer();
		if (p==null) return false;
		p.setTotalExperience(0);
		p.setExp(0);
		p.setLevel(0);
		return true;
	}

	/**
	 * Clear potion effects of the given player
	 */
	public boolean clearPotionEffects() {
		Player p = getPlayer();
		if (p==null || !p.isOnline()) return false;
		for (PotionEffect pe : p.getActivePotionEffects()) {
			p.removePotionEffect(pe.getType());
		}
		return true;
	}

	/**
	 * Clear inventory and ender chest of the given player
	 */
	public boolean clearInventory() {
		Player p = getPlayer();
		if (p==null) return false;
		PlayerInventory i = p.getInventory();
		i.clear();
		i.setHelmet(null);
		i.setChestplate(null);
		i.setLeggings(null);
		i.setBoots(null);
		
		p.getEnderChest().clear();
		return true;
		
	}
	
	public boolean setGameMode(GameMode g) {
		Player p = getPlayer();
		if (p==null) return false;
		p.setGameMode(g);
		return true;
	}
	
	public boolean isOnline() {
		Player p = getPlayer();
		return (p != null && p.isOnline());
	}

	public boolean teleport(Location location) {
		Player p = getPlayer();
		if (p==null) return false;
		p.teleport(location);
		return true;
	}

}
