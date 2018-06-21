package sbbj_tpg;

import java.util.*;
import java.io.*;

public class TPGAlgorithm {
	// A map for holding arguments from the parameters file
	public HashMap<String, String> arguments = null;

	// A variable for holding a static Random Number Generator
	public static Random RNG = null;

	// TPG Framework Objects
	protected TPGLearn tpgLearn = null;
	protected TPGPlay tpgPlay = null;

	// Create a new TPGAlgorithm in Learn or Play mode
	public TPGAlgorithm(String inputFile, String type) {
		if (type.equals("learn")) {
			System.out.println("Starting TPG in Learning Mode.");
			startLearning(inputFile);
		} else if (type.equals("play")) {
			System.out.println("Starting TPG in Play Mode.");
			startPlaying(inputFile);
		} else
			throw new RuntimeException(
					"Uh, we had a slight input parameters malfunction, but uh... everything's perfectly all right now. We're fine. We're all fine here now, thank you. How are you?");
	}

	// Start a Learn session
	public void startLearning(String argumentsFile) {
		// Create new data structures for storage
		arguments = new HashMap<String, String>();

		// Set the procedure type to all before checking it
		arguments.put("procedureType", "all");

		// Get the arguments
		readArgumentsToMap(argumentsFile);

		// Set the seed for the RNG
		RNG = new Random(Integer.parseInt(arguments.get("seed")));
		if (Integer.valueOf(arguments.get("seed")) == 0)
			RNG = new Random(System.currentTimeMillis());

		// Create a new TPGLearn object to start the learning process
		tpgLearn = new TPGLearn(arguments);
	}

	// Start a Play session
	public void startPlaying(String modelFile) {
		// Creates a new TPGPlay object. Does not need arguments or RNG.
		tpgPlay = new TPGPlay(modelFile);
	}

	// Get the TPGLearn object. This returns null if TPGAlgorithm is in Play mode.
	public TPGLearn getTPGLearn() {
		return tpgLearn;
	}

	// Get the TPGPlay object. This returns null if TPGAlgorithm is in Learn mode.
	public TPGPlay getTPGPlay() {
		return tpgPlay;
	}

	// Read the arguments from a file and store them in an arguments map
	public void readArgumentsToMap(String fileName) {
		arguments.put("seed", "0");
		arguments.put("teamPopSize", "360");
		arguments.put("teamGap", "0.5");
		arguments.put("probLearnerDelete", "0.7");
		arguments.put("probLearnerAdd", "0.7");
		arguments.put("probMutateAction", "0.2");
		arguments.put("probActionIsTeam", "0.5");
		arguments.put("maximumTeamSize", "5");
		arguments.put("maximumProgramSize", "96");
		arguments.put("probProgramDelete", "0.5");
		arguments.put("probProgramAdd", "0.5");
		arguments.put("probProgramSwap", "1.0");
		arguments.put("probProgramMutate", "1.0");
	}
}