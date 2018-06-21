package sbbj_tpg;

import java.util.*;

public class TPGPlay {
	// Create a variable for holding a Team
	protected Team team = null;

	// Given a file name as an input, read in a model and prepare it to play
	public TPGPlay(String modelFile) {
		System.out.println("You can't play yet. I'll fix this soon.");
	}

	// Given an input feature set, produce an action
	public long participate(double[] inputFeatures) {
		// If we don't have a Team, it can't play
		if (team == null)
			return -1;

		// Give the team the input features to find an action
		return team.getAction(new HashSet<Team>(), inputFeatures, 0);
	}

	// Print the current status of the TPG algorithm
	public void printStats(int teamCount) {
		// Input whatever stats readout you want. There's very little to look at,
		// but you could include outcomes as they're generated.
	}
}
