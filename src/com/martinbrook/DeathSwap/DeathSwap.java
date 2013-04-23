package com.martinbrook.DeathSwap;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;


public class DeathSwap extends JavaPlugin {
	
	private HashMap<String, DeathSwapPlayer> allPlayers = new HashMap<String, DeathSwapPlayer>();
	private ArrayList<DeathSwapPlayer> survivors = new ArrayList<DeathSwapPlayer>();
	private Server server;
	private Location spawn;
	private boolean matchRunning = false;
	private Calendar matchStartTime;
	private int readyCount = 0;
	private static int MINIMUM_SWAP_TIME = 22;
	private static int SWAP_CHECK_TIME = 6;
	private static int SWAP_PROBABILITY = 14;
	private static int COUNTDOWN_DURATION = 15;
	private static int INITIAL_RESISTANCE_DURATION = 10;
	private static int SWAP_RESISTANCE_DURATION = 5;
	private boolean uhcMode = false;
	private static int START_RADIUS = 1000;
	ArrayList<Location> startPoints;
	private MatchCountdown matchCountdown = null;
	
    @Override
    public void onEnable(){
        server = getServer();
        spawn = getServer().getWorlds().get(0).getSpawnLocation();
        getServer().getPluginManager().registerEvents(new DeathSwapListener(this), this);
		
    }
 
    
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
    	String c = cmd.getName().toLowerCase();
    	String response = null;

		if (c.equals("join") && sender instanceof Player) {
			response = cJoin((Player) sender);
		} else if (c.equals("leave") && sender instanceof Player) {
			response = cLeave((Player) sender);
		} else if (c.equals("ready") && sender instanceof Player) {
			response = cReady((Player) sender);
		} else if (c.equals("ds")) {
			response = cDs(args);
		}
		
		
		if (response != null)
			sender.sendMessage(response);
		
		return true;
    }

	/**
	 * Handle the /ds command
	 * 
	 * @param args The command arguments
	 * @return Response to be sent back to the sender
	 */
	private String cDs(String[] args) {
		if (args.length < 1) return ChatColor.GRAY + "/ds command options:\n" +
				"  /ds reset   - Abort the current match and teleport players back to spawn\n" +
				"  /ds uhc     - Toggle UHC mode";
		String cmd = args[0].toLowerCase();
		
		if (cmd.equals("reset")) {
			return cDsReset();
		}
		
		if (cmd.equals("uhc")) {
			uhcMode = !uhcMode;
			return ChatColor.AQUA + "UHC mode is now " + (uhcMode? "enabled" : "disabled") + "!";
		}
		
		return ChatColor.RED + "/ds command not understood. Type /ds for options.";
	}

	/**
	 * Handle the /ds reset command
	 * 
	 * @return Response to be sent back to the sender
	 */
	private String cDsReset() {
		// Teleports all players to spawn, aborts the current match.
		
		for (DeathSwapPlayer dp : allPlayers.values()) dp.teleport(spawn);
		resetMatch();

		broadcast(ChatColor.GOLD + "DeathSwap has been reset.");
		return null;
	}

	/**
	 * Handle the /join command
	 * 
	 * @param sender The player who wants to join
	 * @return Response to be sent back to the sender
	 */
	private String cJoin(Player sender) {
		if (getDeathSwapPlayer(sender) != null) return ChatColor.RED + "You have already joined this match! Type /ready when ready to begin.";
		
		if (matchInProgress() || matchCountdown != null) return ChatColor.RED + "Unable to join, the match has already started!";
		DeathSwapPlayer dp = new DeathSwapPlayer(sender.getName(), this);
		allPlayers.put(sender.getName().toLowerCase(), dp);
		survivors.add(dp);
		return ChatColor.GREEN + "You have joined the match! Type /ready when ready to begin.";
	}
	
	/**
	 * Handle the /leave command
	 * 
	 * @param sender The player who wants to leave
	 * @return Response to be sent back to the sender
	 */
	private String cLeave(Player sender) {
		DeathSwapPlayer dp = getDeathSwapPlayer(sender);
		if (dp == null) return ChatColor.RED + "Unable to leave - you have not joined this match!";
		
		if (matchInProgress() || matchCountdown != null) return ChatColor.RED + "Unable to leave - the match has already started!";
		
		allPlayers.remove(sender.getName().toLowerCase());
		survivors.remove(dp);
		return ChatColor.GREEN + "You have left the match!";
		
	}


	/**
	 * Handle the /ready command
	 * 
	 * @param sender The player who is ready
	 * @return Response to be sent back to the sender
	 */
	private String cReady(Player sender) {
		DeathSwapPlayer p = getDeathSwapPlayer(sender);
		
		if (p == null) return ChatColor.RED + "You have not joined this match! Type /join to join.";
		if (p.isReady()) return ChatColor.RED + "You are already marked as ready!";
	
		p.setReady();
		broadcast(ChatColor.GREEN + sender.getDisplayName() + " is ready!");
		readyCount++;
		if (readyCount == allPlayers.size()) startMatchCountdown();
		return null;
	}


	/**
	 * Pregenerate chunks and begin the match countdown
	 */
	private void startMatchCountdown() {
		matchCountdown = new MatchCountdown(COUNTDOWN_DURATION, this);
		broadcast(ChatColor.GRAY + "Generating chunks, prepare for possible lag...");
		World w = server.getWorlds().get(0);
		int playerCount = survivors.size();
		startPoints = MatchUtils.calculateRadialStarts(w, playerCount, START_RADIUS);
		
		 
	}


	/**
	 * Start the match, teleport players, and vanish spectators
	 */
	protected void startMatch() {
		matchCountdown = null;
		startMatchTimer();
		startSwapTimer();
		matchRunning = true;
		for (DeathSwapPlayer dp : survivors) {
			dp.renew();
			dp.giveResistance(INITIAL_RESISTANCE_DURATION);
		}
		server.getWorlds().get(0).setTime(0);
		launchPlayers();
		setPlayerVisibility();
		broadcast(ChatColor.AQUA + "GO!");
	}


	/**
	 * Teleport players to their start points, ensuring chunks are loaded
	 */
	private void launchPlayers() {
		int playerCount = survivors.size();
				
		for (int i = 0; i < playerCount; i++) {
			DeathSwapPlayer dp = survivors.get(i);
			if (dp.isOnline()) {
				// Ensure chunk is loaded
				startPoints.get(i).getChunk().load();
				dp.teleport(startPoints.get(i));
			}
		}
	}


	/**
	 * Broadcast a message to all online players
	 * 
	 * @param message The message to be sent
	 */
	public void broadcast(String message) {
		server.broadcastMessage(message);
	}
	
	/**
	 * Look up a player by Player object
	 * 
	 * @param p The Player object
	 * @return The player, or null if not found
	 */
	public DeathSwapPlayer getDeathSwapPlayer(Player p) {
		return getDeathSwapPlayer(p.getName());
	}
	
	/**
	 * Look up a player by name, case insensitive
	 * 
	 * @param name The name
	 * @return The player, or null if not found
	 */
	public DeathSwapPlayer getDeathSwapPlayer(String name) {
		return allPlayers.get(name.toLowerCase());
	}


	/**
	 * Start a timer to swap players
	 */
	private void startSwapTimer() {
		server.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			public void run() {
				checkForSwap();
			}
		}, MINIMUM_SWAP_TIME * 20);
		
		
		
	}


	/**
	 * Periodic check for player swap, according to the defined probability
	 */
	private void checkForSwap() {
		Random r = new Random();
		if (r.nextInt(100) < SWAP_PROBABILITY) {
			swapPlayers();
		} else {
			server.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
				public void run() {
					checkForSwap();
				}
			}, SWAP_CHECK_TIME * 20);
		}
	}


	/**
	 * Initiate the player swap and set a timer for the next one.
	 */
	private void swapPlayers() {
		broadcast(ChatColor.GOLD + "Swapping now!");
		doPlayerSwap();
		
		startSwapTimer();
		
	}

	/**
	 * Carry out the player swap, teleporting all online surviving players
	 */
	private void doPlayerSwap() {
		ArrayList<DeathSwapPlayer> players = new ArrayList<DeathSwapPlayer>();
		ArrayList<Location> locations = new ArrayList<Location>();
		
		for (DeathSwapPlayer dp : survivors) {
			if (dp.isOnline()) {
				players.add(dp);
				locations.add(dp.getPlayer().getLocation());
			}
		}
		
		// Cycle locations
		locations.add(locations.remove(0));
		
		for(int i = 0; i < players.size(); i++) {
			DeathSwapPlayer dp = players.get(i);
			dp.giveResistance(SWAP_RESISTANCE_DURATION);
			dp.teleport(locations.get(i));
		}

	}


	/**
	 * Starts the match timer
	 */
	private void startMatchTimer() {
		matchStartTime = Calendar.getInstance();
		server.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			public void run() {
				doMatchProgressAnnouncement();
			}
		}, 1200L, 1200L);
	}
	
	/**
	 * Display the current match time if it is a multiple of 30.
	 */
	private void doMatchProgressAnnouncement() {
		long matchTime = MatchUtils.getDuration(matchStartTime, Calendar.getInstance()) / 60;
		if (matchTime % 10 == 0 && matchTime > 0) {
			broadcast(matchTimeAnnouncement(false));
		}
	}
	
	/**
	 * Get the text of a match time announcement
	 * 
	 * @param precise Whether to give a precise time (00:00:00) instead of (xx minutes)
	 * @return Current match time as a nicely-formatted string
	 */
	private String matchTimeAnnouncement(boolean precise) {
		if (this.matchRunning == false)
			return ChatColor.AQUA + "Match time: " + ChatColor.GOLD + MatchUtils.formatDuration(0, precise);
		else
			return ChatColor.AQUA + "Match time: " + ChatColor.GOLD + MatchUtils.formatDuration(matchStartTime, Calendar.getInstance(), precise);

	}


	/**
	 * Process the death of a player in the match
	 * 
	 * @param dp The player who died
	 */
	protected void handlePlayerDeath(DeathSwapPlayer dp) {
		dp.setDead();
		survivors.remove(dp);
		setPlayerVisibility(dp.getPlayer()); // Make them a spectator
		if (survivors.size() == 1) {
			handleVictory(getDeathSwapPlayer(survivors.get(0).getName()));
		}
		
	}


	/**
	 * Called when all players but one have been eliminated
	 * 
	 * @param winner The winning player
	 */
	private void handleVictory(DeathSwapPlayer winner) {
		broadcast(ChatColor.AQUA + winner.getName() + " is the winner!");
		broadcast(matchTimeAnnouncement(true));
		
		resetMatch();
	}


	/**
	 * Reset all match parameters to default settings, and end the match in progress
	 */
	private void resetMatch() {
		allPlayers.clear();
		survivors.clear();
		
		matchRunning = false;
		uhcMode = false;
		readyCount = 0;

		matchCountdown = null;
		server.getScheduler().cancelTasks(this);
		setPlayerVisibility();
		
	}


	/**
	 * Set the correct vanish status for all players on the server
	 */
	public void setPlayerVisibility() {
		for(Player p : server.getOnlinePlayers()) setPlayerVisibility(p);
	}
	
	/**
	 * Set the correct visibility of a specific player on the server
	 * 
	 * @param viewed The player whose visibility is to be updated
	 */
	public void setPlayerVisibility(Player viewed) {
		DeathSwapPlayer viewedDp = this.getDeathSwapPlayer(viewed);
		boolean viewedIsSpectator = (!matchInProgress() || viewedDp == null || !viewedDp.isAlive());
		
		MatchUtils.setAffectsSpawning(viewed, !viewedIsSpectator);
		MatchUtils.setCollidesWithEntities(viewed, !viewedIsSpectator);

		for (Player viewer : server.getOnlinePlayers()) {
			DeathSwapPlayer dp = this.getDeathSwapPlayer(viewer);

			// Figure out if viewer is a spectator
			if (!matchInProgress() || dp == null || !dp.isAlive()) {
				// Viewer is a spectator; they can see the player.
				viewer.showPlayer(viewed);
			} else {
				// Viewer is a player; they can only see other players.
				if (viewedIsSpectator) viewer.hidePlayer(viewed);
				else viewer.showPlayer(viewed);
			}
		}
	}


	/**
	 * @return Whether a match is currently in progress
	 */
	public boolean matchInProgress() {
		return matchRunning;
	}


	/**
	 * @return Whether UHC mode is currently active
	 */
	public boolean uhcEnabled() {
		return uhcMode;
	}
}
