package com.martinbrook.DeathSwap;

public class DeathSwapPlayer {
	private String name;
	boolean ready;
	boolean alive;

	public DeathSwapPlayer(String name) {
		this.name = name;
		this.alive=true;
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
	

}
