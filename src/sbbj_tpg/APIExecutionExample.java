package sbbj_tpg;

public class APIExecutionExample {
	public static void main(String[] args) {
		// Example Code execution when interacting with any API:

		// Create a TPG instance with the parameters file and training flag
		TPGAlgorithm tpgAlgorithm = new TPGAlgorithm("parameters.arg", "learn");

		// Grab the TPG learning interface from the wrapper object
		TPGLearn tpg = tpgAlgorithm.getTPGLearn();

		// Get the action pool from the API and give it to TPG in the form of a long[]
		tpg.setActions(new long[] { 1L, 2L, 3L });

		// Run the initialize method to create Team/Learner populations and prep for
		// beginning learning
		tpg.initialize();

		// Create a variable for holding reward
		double reward = 0.0D;

		// Create an array for holding features
		double[] inputFeatures = null;

		// Create a variable for the number of iterations. 1000 is MASSIVE for
		// reinforcement learning in games. This is just for boundary testing.
		int numberOfIterations = 1000;

		// Keep a count of the number of games to play
		int gamesToPlay = 1;

		// Main Learning Loop
		for (int i = 0; i < numberOfIterations; i++) {
			for (int j = 0; j < gamesToPlay; j++) {
				// Let every Team play the current game once
				while (tpg.remainingTeams() > 0) {
					// For simulation only. See the big comment below.
					int count = 10;

					// Reset the reward to 0.
					reward = 0.0;
					
					Team team = tpg.getCurTeam();

					// This while loop would normally be while( game.episode_still_running() ), but
					// I don't have a game to simulate for you, so here I'm simply saying that each
					// game
					// runs for 10 "frames" before offering reward and moving to the next Team.
					while (count > 0) {
						// Convert the gameState to a double[] somehow. This is a 5 feature space. A
						// very small frame.
						inputFeatures = new double[] { 1.1, 2.1, 3.1, 4.1, 5.1 };

						// Accumulate the reward by getting TPG to play
						reward += tpg.participate(team, inputFeatures);

						// Counting down frames for testing purposes
						count--;
					}

					// Reward the current Team. This automatically rotates the current Team.
					// The "game" string should be unique to the game the Team just played.
					// In single-game learning just make it static, but when you move on to
					// playing multiple games, you'll need to make sure the labels are correct.
					tpg.reward(team, "game", reward);
				}
			}

			// Print the current top 10 Team population outcomes and some simple environment
			// values
			tpg.printStats(10);

			// Tell TPG to Perform Selection
			tpg.selection();

			// Tell TPG to Reproduce and Mutate with the current Teams
			tpg.generateNewTeams();

			// Reset TPG so it increases the generation count and finds the new Root Teams
			tpg.nextEpoch();
		}
	}
}
