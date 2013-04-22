package com.martinbrook.DeathSwap;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;


public class DeathSwapListener implements Listener {

	private DeathSwap plugin;
	
	public DeathSwapListener(DeathSwap plugin) {
		this.plugin = plugin;
	}
	
	
	/**
	 * Handle death events; add bonus items, if any.
	 * 
	 * @param pDeath
	 */
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e){
		DeathSwapPlayer dp = plugin.getDeathSwapPlayer(e.getEntity());
		
		if (dp != null && dp.isAlive() && plugin.matchInProgress())
			plugin.handlePlayerDeath(dp);

	}

	
	
	
	@EventHandler(ignoreCancelled = true)
	public void onRegainHealth(EntityRegainHealthEvent e) {
		// Only interested in players
		if (e.getEntityType() != EntityType.PLAYER) return;
		
		// Only interested if match is in progress.
		if (!plugin.matchInProgress()) return;

		// Only interested in actual players, not spectators
		DeathSwapPlayer dp = plugin.getDeathSwapPlayer((Player) e.getEntity());
		if (dp == null || !dp.isAlive() || !dp.isReady()) return;

		// Cancel event if it is a natural regen due to hunger being full, and UHC is enabled
		if (plugin.uhcEnabled() && e.getRegainReason() == RegainReason.SATIATED) {
			e.setCancelled(true);
			return;
		}
		
	}
	
}
