package com.martinbrook.DeathSwap;


public class MatchCountdown extends AbstractCountdown {

	public MatchCountdown(int countdownLength, DeathSwap plugin) {
		super(countdownLength, plugin);
	}

	@Override
	protected void complete() {
		plugin.startMatch();
	}

	@Override
	protected String getDescription() {
		return "The match will begin";
	}


}
