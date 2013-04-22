package com.martinbrook.DeathSwap;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;


public class MatchUtils {

	private static Method mAffectsSpawning = null;
	private static Method mCollidesWithEntities = null;

	static {
		try {
			mAffectsSpawning = HumanEntity.class.getDeclaredMethod("setAffectsSpawning", boolean.class);
			mCollidesWithEntities = Player.class.getDeclaredMethod("setCollidesWithEntities", boolean.class);
		}
		catch (Exception e) {  }
	}
	

	private MatchUtils() { }
	
	
	public static String formatDuration(long d, boolean precise) {
		if (precise) {
			long seconds = d % 60;
			d = d / 60;
			long minutes = d % 60;
			long hours = d / 60;
			
			// The string
			return String.format("%02d:%02d:%02d", hours, minutes, seconds);
		} else {
			long minutes = d / 60;
			return minutes + " minute" + (minutes != 1 ? "s" : "");
			
		}
		
	}
	
	public static String formatDuration(Calendar t1, Calendar t2, boolean precise) {
		// Convert to duration in seconds
		return formatDuration(getDuration(t1, t2), precise);
	}
	
	public static long getDuration(Calendar t1, Calendar t2) {
		return (t2.getTimeInMillis() - t1.getTimeInMillis()) / 1000;
	}


	
	/**
	 * Convert a string to a boolean.
	 * 
	 * true, on, yes, y, 1 => True
	 * false, off, no, n, 0 => False
	 * 
	 * @param s The string to check
	 * @return Boolean value, or null if not parsable
	 */
	public static Boolean stringToBoolean(String s) {
		if ("true".equalsIgnoreCase(s) || "on".equalsIgnoreCase(s) || "yes".equalsIgnoreCase(s) || "y".equalsIgnoreCase(s) || "1".equals(s))
			return true;
		if ("false".equalsIgnoreCase(s) || "off".equalsIgnoreCase(s) || "no".equalsIgnoreCase(s) || "n".equalsIgnoreCase(s) || "0".equals(s))
			return false;
		return null;
		
	}
	

	/**
	 * Gets a copy of a player's current inventory, including armor/health/hunger details.
	 *
	 * @author AuthorBlues
	 * @param player The player to be viewed
	 * @return inventory The player's inventory
	 *
	 */
	public static Inventory getInventoryView(Player player)
	{

		PlayerInventory pInventory = player.getInventory();
		Inventory inventoryView = Bukkit.getServer().createInventory(null,
			pInventory.getSize() + 9, player.getDisplayName() + "'s Inventory");

		ItemStack[] oldContents = pInventory.getContents();
		ItemStack[] newContents = inventoryView.getContents();

		for (int i = 0; i < oldContents.length; ++i)
			if (oldContents[i] != null) newContents[i] = oldContents[i];

		newContents[oldContents.length + 0] = pInventory.getHelmet();
		newContents[oldContents.length + 1] = pInventory.getChestplate();
		newContents[oldContents.length + 2] = pInventory.getLeggings();
		newContents[oldContents.length + 3] = pInventory.getBoots();

		newContents[oldContents.length + 7] = new ItemStack(Material.APPLE, player.getHealth());
		newContents[oldContents.length + 8] = new ItemStack(Material.COOKED_BEEF, player.getFoodLevel());

		for (int i = 0; i < oldContents.length; ++i)
			if (newContents[i] != null) newContents[i] = newContents[i].clone();

		inventoryView.setContents(newContents);
		return inventoryView;
	}
	


	/**
	 * Checks if the SportBukkit API is available
	 *
	 * @author AuthorBlues
	 * @return true if SportBukkit is installed, false otherwise
	 * @see http://www.github.com/rmct/SportBukkit
	 */
	public static boolean hasSportBukkitApi() {
		return mAffectsSpawning != null && mCollidesWithEntities != null;
	}

	/**
	 * Sets whether player affects spawning via natural spawn and mob spawners.
	 * Uses last_username's affects-spawning API from SportBukkit
	 *
	 * @author AuthorBlues
	 * @param affectsSpawning Set whether player affects spawning
	 * @see http://www.github.com/rmct/SportBukkit
	 */
	public static void setAffectsSpawning(Player player, boolean affectsSpawning) {
		if (mAffectsSpawning != null) try {
			mAffectsSpawning.invoke(player, affectsSpawning);
		} catch (Exception e) { }
	}

	/**
	 * Sets whether player collides with entities, including items and arrows.
	 * Uses last_username's collides-with-entities API from SportBukkit
	 *
	 * @author AuthorBlues
	 * @param collidesWithEntities Set whether player collides with entities
	 * @see http://www.github.com/rmct/SportBukkit
	 */
	public static void setCollidesWithEntities(Player player, boolean collidesWithEntities) {
		if (mCollidesWithEntities != null) try {
			mCollidesWithEntities.invoke(player, collidesWithEntities);
		}
		catch (Exception e) { }
	}

	/**
	 * Generate a list of radial locations
	 * 
	 * @param world World in which to generate locations
	 * @param count Number of starts to generate
	 * @param radius Radius of circle
	 * @return List of starts
	 */
	public static ArrayList<Location> calculateRadialStarts(World world, int count, int radius) {
		ArrayList<Location> locations = new ArrayList<Location>();
		
		double arc = (2*Math.PI) / count;
		
		for(int i = 0; i < count; i++) {
			int x = (int) (radius * Math.cos(i*arc));
			int z = (int) (radius * Math.sin(i*arc));
			
			int y = world.getHighestBlockYAt(x, z);
			locations.add(new Location(world,x,y,z));
		}
		return locations;
	
	}
}
