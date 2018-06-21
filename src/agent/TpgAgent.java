package agent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import javaclient.GymJavaHttpClient;
import javaclient.StepObject;
import sbbj_tpg.*;

// https://stackoverflow.com/questions/702415/how-to-know-if-other-threads-have-finished     do # 5

public class TpgAgent {

    public static enum LevelType{SingleLevel, FiveLevel};
    public static enum TrainType{DeathSequence, AllLevels, LevelPerGen, MultiGame};
    
    // default values
    public static int port = 5000;
    public static boolean debug = true;
    public static String game = "aliens";
    public static int generations = 10000;
    public static LevelType levelType = LevelType.SingleLevel;
    public static TrainType trainType = TrainType.MultiGame;
    public static int maxSteps = 1000; // max steps before game quits
    public static int maxStepRec = 20; // max amount of step recordings to take
    public static int bestStepRec = 10; // max amount of step recordings to take after fitness
    public static int stepRecDiff = 20; // number of frames to take off of step rec
    public static int defEps = 1; // default number of episodes per individual
    public static int defReps = 6; // default number of reps per set for sequences
    public static boolean quickie = true; // whether to do single frame episodes
    public static int threads = 1;
    public static int defLevel = 0;
    
    public static Random rand;

    public static void mainn(String[] args) {
        
        for(String arg : args) {
            if(arg.toLowerCase().startsWith("port=")) {
                port = Integer.parseInt(arg.substring(5));
            }else if(arg.toLowerCase().startsWith("debug=")) {
                debug = Boolean.parseBoolean(arg.substring(6));
            }else if(arg.toLowerCase().startsWith("game=")) {
                game = arg.substring(5);
            }else if(arg.toLowerCase().startsWith("gens=") || arg.toLowerCase().startsWith("generations=")) {
                generations = Integer.parseInt(arg.substring(arg.indexOf("=") + 1));
            }else if(arg.toLowerCase().startsWith("levellype=") || arg.toLowerCase().startsWith("ltype=")) {
                String val = arg.substring(arg.indexOf("=") + 1);
                if(val.equals("1") || val.toLowerCase().equals("single")) {
                    levelType = LevelType.SingleLevel;
                }else{
                    levelType = LevelType.FiveLevel;
                }
            }else if(arg.toLowerCase().startsWith("traintype=") || arg.toLowerCase().startsWith("ttype=")) {
                String val = arg.substring(arg.indexOf("=") + 1);
                if(val.toLowerCase().equals("mg") || val.toLowerCase().equals("multi") || val.toLowerCase().equals("multigame")) {
                    trainType = TrainType.MultiGame;
                }else{
                    trainType = TrainType.DeathSequence; // probably won't ever use anything else
                }
            }else if(arg.toLowerCase().startsWith("maxsteps=")) {
                maxSteps = Integer.parseInt(arg.substring(arg.indexOf("=") + 1));
            }else if(arg.toLowerCase().startsWith("maxsteprec=")) {
                maxStepRec = Integer.parseInt(arg.substring(arg.indexOf("=") + 1));
            }else if(arg.toLowerCase().startsWith("beststeprec=")) {
                bestStepRec = Integer.parseInt(arg.substring(arg.indexOf("=") + 1));
            }else if(arg.toLowerCase().startsWith("steprecdiff=")) {
                stepRecDiff = Integer.parseInt(arg.substring(arg.indexOf("=") + 1));
            }else if(arg.toLowerCase().startsWith("defeps=")) {
                defEps = Integer.parseInt(arg.substring(arg.indexOf("=") + 1));
            }else if(arg.toLowerCase().startsWith("defreps=")) {
                defReps = Integer.parseInt(arg.substring(arg.indexOf("=") + 1));
            }else if(arg.toLowerCase().startsWith("quickie=")) {
                debug = Boolean.parseBoolean(arg.substring(arg.indexOf("=") + 1));
            }else if(arg.toLowerCase().startsWith("threads=")) {
                threads = Integer.parseInt(arg.substring(arg.indexOf("=") + 1));
            }else if(arg.toLowerCase().startsWith("deflevel=")) {
                defLevel = Integer.parseInt(arg.substring(arg.indexOf("=") + 1));
            }
        }
        rand = new Random(55);
        
        System.out.println("Starting TPG on " + "port: " + port + ", debug: " + debug + ", game: " + game
                + ", generations: " + generations + ", LevelType: " + levelType.toString() + ", TrainType: " + trainType.toString());
        
        if(threads == 1) {
            runTpg();
        }else {
            runTpgMultiThread();
        }
    }
    
    public static void runTpgMultiThread() {
        boolean wtf; // write to file
        boolean render;
        
        if(debug) {
            wtf = false;
            render = true;
        }else {
            wtf = true;
            render = false;
        }
        
        // write to file instead of console to record stuff
        if(wtf) {
            if(trainType != TrainType.MultiGame) {
                wtf(game);
            }else {
                wtf("multigame");
            }
        }
        
        GymJavaHttpClient.baseUrl = "http://127.0.0.1:" + String.valueOf(port);
        
        // set up tpg agent
        TPGAlgorithm tpgAlg = new TPGAlgorithm("parameters.arg", "learn");
        TPGLearn tpg = setupTpgLearn(6, tpgAlg); // 6 actions in gvgai
        
        String[] games = game.split(",");
        HashMap<String,String[][]> lvlIds = getLevelsIdsMultiThread(games, levelType);
        HashMap<String,long[]> numsActions = getNumsActionsMapMultiThread(lvlIds);
        //runGensMultiThread(render, lvlIds, numsActions, tpg);
    }
    
    public static void runTpg() {
        
        boolean wtf; // write to file
        boolean render;
        
        if(debug) {
            wtf = false;
            render = true;
        }else {
            wtf = true;
            render = false;
        }
        
        // write to file instead of console to record stuff
        if(wtf) {
            if(trainType != TrainType.MultiGame) {
                wtf(game);
            }else {
                wtf("multigame");
            }
        }
        
        GymJavaHttpClient.baseUrl = "http://127.0.0.1:" + String.valueOf(port);
        
        // set up tpg agent
        TPGAlgorithm tpgAlg = new TPGAlgorithm("parameters.arg", "learn");
        TPGLearn tpg = setupTpgLearn(6, tpgAlg); // 6 actions in gvgai
        
        if(trainType == TrainType.MultiGame) {
            String[] games = game.split(",");
            HashMap<String,String[]> lvlIds = getLevelsIds(games, levelType);
            HashMap<String,long[]> numsActions = getNumsActionsMap(lvlIds);
            runGenerationsMultiGame(generations, render, lvlIds, numsActions, tpg);
        } else {
            String[] lvlIds = getLevelIds(game, levelType); // store IDs of create level environments
            long[] numsActions = getNumsActions(lvlIds); // number of action that can be performed in the environment
            if(trainType == TrainType.DeathSequence) {
                runGenerationsDeathSequence(generations, render, lvlIds, numsActions, tpg);
            }
        }
    }
    
    public static void wtf(String gameName) {
        try {
            // https://stackoverflow.com/questions/7488559/current-timestamp-as-filename-in-java
            String fileName = new SimpleDateFormat("yyyyMMddHHmm'.txt'").format(new Date());
            //throw new FileNotFoundException();
            PrintStream pToFile = new PrintStream(
                    new File("/home/ryan/tpg-playing-records/" + gameName + "/" + fileName));
            System.setOut(pToFile);
        } catch (FileNotFoundException e) {
            try {
                // https://stackoverflow.com/questions/7488559/current-timestamp-as-filename-in-java
                String fileName = new SimpleDateFormat("yyyyMMddHHmm'.txt'").format(new Date());
                //throw new FileNotFoundException();
                PrintStream pToFile = new PrintStream(
                        new File("/home/amaral/tpg-playing-records/" + gameName + "/" + fileName));
                System.setOut(pToFile);
            } catch (FileNotFoundException e1) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    
    public static String[] getLevelIds(String gameName, LevelType levelType) {
        String[] lvlIds = new String[] {};
        if(levelType == LevelType.FiveLevel) {
            lvlIds =  new String[] {
                GymJavaHttpClient.createEnv("gvgai-" + gameName + "-lvl0-v0"),
                GymJavaHttpClient.createEnv("gvgai-" + gameName + "-lvl1-v0"),
                GymJavaHttpClient.createEnv("gvgai-" + gameName + "-lvl2-v0"),
                GymJavaHttpClient.createEnv("gvgai-" + gameName + "-lvl3-v0"),
                GymJavaHttpClient.createEnv("gvgai-" + gameName + "-lvl4-v0")};
        }else if(levelType == LevelType.SingleLevel) {
            lvlIds =  new String[] {
                GymJavaHttpClient.createEnv("gvgai-" + gameName + "-lvl0-v0")};
        }
        
        return lvlIds;
    }
    
    public static HashMap<String,String[]> getLevelsIds(String[] gameNames, LevelType levelType) {
        HashMap<String,String[]> levelsIds = new HashMap<String,String[]>();
        if(levelType == LevelType.FiveLevel) {
            for(String game : gameNames) {
                levelsIds.put(game, 
                        new String[] {
                            GymJavaHttpClient.createEnv("gvgai-" + game + "-lvl0-v0"),
                            GymJavaHttpClient.createEnv("gvgai-" + game + "-lvl1-v0"),
                            GymJavaHttpClient.createEnv("gvgai-" + game + "-lvl2-v0"),
                            GymJavaHttpClient.createEnv("gvgai-" + game + "-lvl3-v0"),
                            GymJavaHttpClient.createEnv("gvgai-" + game + "-lvl4-v0")});
            }
        }else if(levelType == LevelType.SingleLevel) {
            for(String game : gameNames) {
                levelsIds.put(game, 
                        new String[] {
                            GymJavaHttpClient.createEnv("gvgai-" + game + "-lvl0-v0")});
            }
        }
        
        return levelsIds;
    }
    
    public static HashMap<String,String[][]> getLevelsIdsMultiThread(String[] gameNames, LevelType levelType) {
        HashMap<String,String[][]> levelsIds = new HashMap<String,String[][]>();
        if(levelType == LevelType.FiveLevel) {
            for(String game : gameNames) {
                String[][] lvls = new String[5][threads];
                for(int lvl = 0; lvl < 5; lvl++) {
                    for(int thrd = 0; thrd < threads; thrd++) {
                        lvls[lvl][thrd] = GymJavaHttpClient.createEnv("gvgai-" + game + "-lvl" + lvl + "-v0");
                    }
                }
                levelsIds.put(game, lvls);
            }
        }else if(levelType == LevelType.SingleLevel) {
            for(String game : gameNames) {
                String[][] lvls = new String[1][threads];
                for(int thrd = 0; thrd < threads; thrd++) {
                    lvls[0][thrd] = GymJavaHttpClient.createEnv("gvgai-" + game + "-lvl" + defLevel + "-v0");
                }
                levelsIds.put(game, lvls);
            }
        }
        
        return levelsIds;
    }
    
    public static long[] getNumsActions(String[] lvlIds) {
        long[] numsActions = new long[lvlIds.length];
        for(int i = 0; i < lvlIds.length; i++) {
            numsActions[i] = 
                    (long)GymJavaHttpClient.actionSpaceSize((JSONObject)GymJavaHttpClient.actionSpace(lvlIds[i]));
        }
        
        return numsActions;
    }
    
    public static HashMap<String,long[]> getNumsActionsMap(HashMap<String,String[]> lvlIds) {
        Set<String> games = lvlIds.keySet();
        HashMap<String,long[]> numsActions = new HashMap<String,long[]>();
        for(String game : games) {
            // get actionspace of first level in each game
            long[] actions = new long[GymJavaHttpClient.actionSpaceSize((JSONObject)GymJavaHttpClient.actionSpace(lvlIds.get(game)[0]))];
            for(int i = 0; i < actions.length; i++) {
                actions[i] = (long)i;
            }
            numsActions.put(game, actions);
        }
        // heyyyyyyyyyy
        return numsActions;
    }
    
    public static HashMap<String,long[]> getNumsActionsMapMultiThread(HashMap<String,String[][]> lvlIds) {
        Set<String> games = lvlIds.keySet();
        HashMap<String,long[]> numsActions = new HashMap<String,long[]>();
        for(String game : games) {
            // get actionspace of first level in each game
            long[] actions = new long[GymJavaHttpClient.actionSpaceSize((JSONObject)GymJavaHttpClient.actionSpace(lvlIds.get(game)[0][0]))];
            for(int i = 0; i < actions.length; i++) {
                actions[i] = (long)i;
            }
            numsActions.put(game, actions);
        }
        // heyyyyyyyyyy
        return numsActions;
    }
    
    public static TPGLearn setupTpgLearn(int numActions, TPGAlgorithm tpgAlg) {
        TPGLearn tpg = tpgAlg.getTPGLearn();
        long[] tpgActions = new long[numActions];
        for(long i = 0; i < numActions; i++) {
            tpgActions[(int) i] = i;
        }
        tpg.setActions(tpgActions);
        tpg.initialize();
        
        return tpg;
    }
    
    public static void runGenerations(int gens, TrainType trainType, 
            boolean render, String[] lvlIds, long[] numsActions, TPGLearn agent) {
        
        if(trainType == TrainType.DeathSequence) {
            runGenerationsDeathSequence(gens, render, lvlIds, numsActions, agent);
        }
    }
    
    public static void runGensMultiThread(boolean render, 
            HashMap<String, String[]> lvlIds, HashMap<String, long[]> numsActions, TPGLearn agent) {
        
    }
    
    public static void runGenerationsMultiGame(int gens, boolean render, 
            HashMap<String, String[]> lvlIds, HashMap<String, long[]> numsActions,TPGLearn agent) {
        
        String genSummaries = new String(); // performance of each generation (min, max, avg)
        
        // record steps (which are sequences)
        ArrayList<ArrayList<Integer>> stepSeqs = new ArrayList<ArrayList<Integer>>();
        
        ArrayList<String> gamesList = new ArrayList<String>(lvlIds.keySet());
        // order to choose games
        LinkedList<String> gameQueue = getGameQueue(gamesList);

        String game = "";
        int lvlIdx = 0;
        String lvl = "";
        long[] curActions = null;
        for(int gen = 0; gen < gens; gen++) {
            System.out.println("==================================================");
            System.out.println("Starting Generation #" + (gen + 1));
            int rep = gen % defReps;
            
            Float[] fitnesses = new Float[agent.remainingTeams()]; // track fitness of all individuals per gen
            
            // choose new game and level maybe
            if(rep == 0) { 
                if(gameQueue.isEmpty()) {
                    gameQueue = getGameQueue(gamesList);
                }
                game = gameQueue.removeFirst();
                curActions = numsActions.get(game); // actions for this game
                lvlIdx = rand.nextInt(lvlIds.get(game).length); // choose random level
                lvl = lvlIds.get(game)[lvlIdx];
            }
            System.out.println("On Game: " + game);
            System.out.println("On Level: " + lvlIdx);
            
            // used for  rep 1 of death sequence for point population fitness
            int[] epLosses = null;
            if(rep == 1) { // on rep 1 we use epVars to find fitness of sequences
                epLosses = new int[stepSeqs.size()];
            }
            
            // eps is number of sequences we do, or just 1 if rep 0
            int eps = 1;
            if(rep > 0 && stepSeqs.size() > 0) {
                eps = stepSeqs.size(); // 1 episode per sequence
            }
            
            while(agent.remainingTeams() > 0) { // iterate through teams
                System.out.println(" ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~");
                System.out.println("Remaining Teams: " + agent.remainingTeams());
                Team team = agent.getCurTeam();
                float reward = 0;
                long[] ac = new long[curActions.length]; // action count
                
                for(int ep = 0; ep < eps; ep++) { // iterate through episodes
                    float rwd = 0;
                    System.out.println("starting episode #" + (ep + 1) + " of " + eps);
                    Object obs = GymJavaHttpClient.resetEnv(lvl); // reset the environment
                    Boolean isDone = false; // whether current episode is done
                    int action; // action for agent to do
                    int stepC = 0; // stepCounter
                    boolean isAutopilot = true; // for following sequence
                    System.out.println("autopilot control");
                    Object info = null;
                    
                    while(!isDone) { // iterate through environment
                        stepC++;
                        if(rep == 0 || stepSeqs.size() == 0 
                                || stepC > stepSeqs.get(ep).size()) { // finished with autopilot, do tpg
                            if(isAutopilot) {
                                isAutopilot = false;
                                System.out.println("tpg control");
                            }
                            
                            action = (int) agent.participate(team, getFeatures(obs), curActions); // tpg chooses
                            
                            // record steps if rep 0, new sequence on first step
                            if(rep == 0) {
                                if(stepC == 1) {
                                    stepSeqs.add(new ArrayList<Integer>());
                                }
                                stepSeqs.get(stepSeqs.size()-1).add(action);
                            }
                        }else{ // do autopilot, until finish sequence
                            action = stepSeqs.get(ep).get(stepC-1);
                        }
                        ac[action] += 1; // track actions
                        
                        StepObject step = GymJavaHttpClient.stepEnv(lvl, action, true, render);
                        
                        obs = step.observation;
                        isDone = step.done;
                        if(!isAutopilot) {
                            rwd += step.reward;
                        }
                        info = step.info;
                        if(quickie || stepC >= maxSteps) {
                            break;
                        }
                        
                        // print progression level
                        if(stepC == Math.floor((maxSteps / 10)*1)){
                            System.out.println("10%");
                        }else if(stepC == Math.floor((maxSteps / 10)*2)){
                            System.out.println("20%");
                        }else if(stepC == Math.floor((maxSteps / 10)*3)){
                            System.out.println("30%");
                        }else if(stepC == Math.floor((maxSteps / 10)*4)){
                            System.out.println("40%");
                        }else if(stepC == Math.floor((maxSteps / 10)*5)){
                            System.out.println("50%");
                        }else if(stepC == Math.floor((maxSteps / 10)*6)){
                            System.out.println("60%");
                        }else if(stepC == Math.floor((maxSteps / 10)*7)){
                            System.out.println("70%");
                        }else if(stepC == Math.floor((maxSteps / 10)*8)){
                            System.out.println("80%");
                        }else if(stepC == Math.floor((maxSteps / 10)*9)){
                            System.out.println("90%");
                        }
                    } // episode done
                    reward += rwd;
                    System.out.println("Score: " + rwd);
                    agent.reward(team, "ep" + Integer.toString(ep), rwd); // apply reward
                    
                    if(rep == 0) { // In rep 0 take sequences of losses
                        fixSequence(stepSeqs, info);
                    }else if(rep == 1) { // record in win or lose this ep
                        if(!didIWin(info)) {
                            epLosses[ep]++;
                        }
                    }
                }
                reward = reward / eps; // average rewards over eps
                // print actions taken
                System.out.print("Action Count: ");
                for(int i = 0; i < ac.length; i++) {
                    System.out.print(ac[i] + " ");
                }
                System.out.println("\nAverage Score: " + reward); // print the score
                System.out.println();
                
                fitnesses[fitnesses.length - agent.remainingTeams() - 1] = reward;
            }
            
            if(rep == 0 && stepSeqs.size() > 20) {
                // take only up a certain amount
                try {
                    Collections.shuffle(stepSeqs); // randomize
                    stepSeqs = new ArrayList<ArrayList<Integer>>(stepSeqs.subList(0, maxStepRec));
                }catch(Exception e) {
                    System.out.println("Oof!");
                }
                System.out.println("Taking " + stepSeqs.size() + " step recordings into next generation");
            }else if(rep == 1) {
                // get fitness of the step sequences
                // first find max losses
                int max = 0;
                for(int i = 0; i < epLosses.length; i++) {
                    if(epLosses[i] > max) {
                        max = epLosses[i];
                    }
                }
                // now get fitnesses of each sequence
                float[] fitness = new float[epLosses.length];
                float lambda = 0.75f; // weight parameter for fitness
                for(int i = 0; i < epLosses.length; i++) {
                    fitness[i] = (float)((2*((float)epLosses[i]/max)/lambda) -
                            (Math.pow(((float)epLosses[i]/max)/lambda, 2)));
                }
                // bubble sort with indexes
                int[] idxs = new int[fitness.length]; // index of fitnesses
                for(int i = 0; i < idxs.length; i++) {
                    idxs[i] = i;
                }
                float swp;
                int swpi;
                for(int i = 0; i < fitness.length - 1; i++) {
                    for(int j = 0; j < fitness.length - i - 1; j++) {
                        if(fitness[j+1] > fitness[j]) {
                            swp = fitness[j];
                            swpi = idxs[j];
                            
                            fitness[j] = fitness[j+1];
                            idxs[j] = idxs[j+1];
                            
                            fitness[j+1] = swp;
                            idxs[j] = swpi;
                        }
                    }
                }
                //delet
                for(int i = 0; i < fitness.length; i++) {
                    System.out.print(fitness[i] + " ");
                }
                ArrayList<ArrayList<Integer>> stepSeqsCopy = stepSeqs;
                stepSeqs = new ArrayList<ArrayList<Integer>>();
                // keep only the sequences that are good enough
                int taken = 0;
                for(int j = 0; j < idxs.length; j++) {
                    int i = idxs[j];
                    if(fitnesses.length - epLosses[i] > 0 && taken < bestStepRec) {
                        stepSeqs.add(stepSeqsCopy.get(i));
                        taken++;
                    }else if(taken >= bestStepRec) {
                        break;
                    }
                }
            }
            
            genSummaries += game + ": " + getSummary(fitnesses);
            System.out.println("Fitness Summary:\n" + genSummaries);
            // prep next generation
            agent.selection();
            agent.generateNewTeams();
            agent.nextEpoch();
        }
    }

    public static void runGenerationsDeathSequence(int gens, boolean render, 
            String[] lvlIds, long[] numsActions, TPGLearn agent){
        
        String genSummaries = new String(); // performance of each generation (min, max, avg)
        // how many reps in a sequence (for death sequence training)
        // rep 0 : each individual plays full sequence, save those that agent loses in (not oot)
        // rep 1 : each individual plays all saved sequences to decide which are better for training
        // rep 2+: each individual plays all good training sequences
        int seqReps = defReps;
        
        // record steps (which are sequences)
        ArrayList<ArrayList<Integer>> stepSeqs = new ArrayList<ArrayList<Integer>>();
        
        int lvl = -1;
        for(int gen = 0; gen < gens; gen++) {
            System.out.println("==================================================");
            System.out.println("Starting Generation #" + gen);
            
            Float[] fitnesses = new Float[agent.remainingTeams()]; // track fitness of all individuals per gen
            
            int rep = gen % seqReps; // rep changes every generation
            if(rep == 0) {
                lvl = (lvl+1)%lvlIds.length;
            }
            String lvlId = lvlIds[lvl];
            System.out.println("On level: " + lvl);
            
            // used for  rep 1 of death sequence
            int[] epWins = null;
            int[] epLosses = null;
            
            if(rep == 1) { // on rep 1 we use epVars to find discrimination of sequnces
                epWins = new int[stepSeqs.size()];
                epLosses = new int[stepSeqs.size()];
            }
            
            int eps = defEps; // default number of episodes
            if(rep > 0 && stepSeqs.size() > 0) {
                eps = stepSeqs.size(); // 1 episode per sequence
            }
            
            while(agent.remainingTeams() > 0) { // iterate through teams
                Team team = agent.getCurTeam();
                System.out.println(" ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~");
                System.out.println("Remaining Teams: " + agent.remainingTeams());
                float reward = 0;
                long[] ac = new long[(int) numsActions[lvl]]; // action count
                
                for(int ep = 0; ep < eps; ep++) { // iterate through episodes
                    float rwd = 0;
                    System.out.println("starting episode #" + ep);
                    Object obs = GymJavaHttpClient.resetEnv(lvlId); // reset the environment
                    Boolean isDone = false; // whether current episode is done
                    int action; // action for agent to do
                    int stepC = 0; // stepCounter
                    boolean isAutopilot = true; // for following sequence
                    System.out.println("autopilot control");
                    Object info = null;
                    
                    while(!isDone) { // iterate through environment
                        stepC++;
                        if(rep == 0 || stepSeqs.size() == 0 
                                || stepC > stepSeqs.get(ep).size()) { // finished with autopilot, do tpg
                            if(isAutopilot) {
                                isAutopilot = false;
                                System.out.println("tpg control");
                            }
                            action = (int) agent.participate(team, getFeatures(obs), new long[] {0,1,2,3}); // tpg chooses
                            
                            // record steps if rep 0, new sequence on first step
                            if(rep == 0) {
                                if(stepC == 1) {
                                    stepSeqs.add(new ArrayList<Integer>());
                                }
                                stepSeqs.get(stepSeqs.size()-1).add(action);
                            }
                        }else{ // do autopilot, until finish sequence
                            action = stepSeqs.get(ep).get(stepC-1);
                        }
                        ac[action] += 1; // track actions
                        StepObject step = GymJavaHttpClient.stepEnv(lvlId, action, true, render);
                        obs = step.observation;
                        isDone = step.done;
                        if(!isAutopilot) {
                            rwd += step.reward;
                        }
                        info = step.info;
                        if(quickie || stepC >= maxSteps) {
                            break;
                        }
                    } // episode done
                    reward += rwd;
                    agent.reward(team, "ep" + Integer.toString(ep), rwd); // apply reward
                    
                    if(rep == 0) { // In rep 0 take sequences of losses
                        fixSequence(stepSeqs, info);
                    }else if(rep == 1) { // record in win or lose this ep
                        if(didIWin(info)) {
                            epWins[ep]++;
                        }else {
                            epLosses[ep]++;
                        }
                    }
                }
                reward = reward / eps; // average rewards over eps
                // print actions taken
                System.out.print("Action Count: ");
                for(int i = 0; i < ac.length; i++) {
                    System.out.print(ac[i] + " ");
                }
                System.out.println("\nScore: " + reward); // print the score
                System.out.println();
                
                fitnesses[fitnesses.length - agent.remainingTeams() - 1] = reward;
            }
            
            if(rep == 0 && stepSeqs.size() > 20) {
                // take only 20 up to
                try {
                    Collections.shuffle(stepSeqs); // randomize
                    stepSeqs = new ArrayList<ArrayList<Integer>>(stepSeqs.subList(0, 20));
                }catch(Exception e) {
                    System.out.println(e);
                    e.printStackTrace();
                }
            }else if(rep == 1) {
                // see which sequences are divisive enough
                for(int i = epWins.length - 1; i >= 0; i--) {
                    if(!(epWins[i] != 0 && 
                            ((float)epLosses[i]/(float)epWins[i] > 0.11 && 
                                    (float)epLosses[i]/(float)epWins[i] < epLosses[i] + epWins[i] - 1))) {
                        stepSeqs.remove(i); // not goldilocks
                    }
                }
            }
            
            genSummaries += getSummary(fitnesses);
            System.out.println("Fitness Summary:\n" + genSummaries);
            // prep next generation
            agent.selection();
            agent.generateNewTeams();
            agent.nextEpoch();
        }
    }
    
    public static void fixSequence(ArrayList<ArrayList<Integer>> stepSeqs, Object info) {
        int stepSeqEnd = stepSeqs.size()-1; // last index
        
        if(stepSeqs.get(stepSeqEnd).size() > stepRecDiff) { // chop off end from sequence
            stepSeqs.set(stepSeqEnd, new ArrayList<Integer>(
                    stepSeqs.get(stepSeqEnd).subList(
                            0, stepSeqs.get(stepSeqEnd).size()-stepRecDiff)));
        }else if(stepSeqs.get(stepSeqEnd).size() <= stepRecDiff) { // sequence from start
            stepSeqs.set(stepSeqEnd, new ArrayList<Integer>());
        }else if(stepSeqs.get(stepSeqEnd).size() == maxSteps || didIWin(info)) { // remove if win or end
            stepSeqs.remove(stepSeqEnd);
        }
    }
    
    public static boolean didIWin(Object info) {
        JSONObject jinfo = (JSONObject)info;
        try {
            return jinfo.getString("winner").equals("PLAYER_WINS");
        }finally {
            return false;
        }
    }
    
    public static double[] getFeatures(Object obs) {
        int res = 128; // x and y resolution of screen space
        double[] features = new double[res*res];
        JSONArray jObs = (JSONArray)obs;
        boolean first = true; // for getting column size first time only
        int fcounter = 0; // counter for feature array
        
        int rowSz = (int) Math.ceil((float)jObs.length()/(float)res); // reduced row size
        int columnSz = 1; // reduced column size (found in first row and column)
        
        // the pixel coords
        int pRow = -1;
        int pCol = -1;
        
        Iterator<Object> iterRow = jObs.iterator(); // row iterator
        while(iterRow.hasNext()) { // iterates rows
            pRow++;
            JSONArray theNext = (JSONArray) iterRow.next();
            if((int)(pRow - rowSz/2) % rowSz != 0) {
                // not in row with features
                continue;
            }
            if(first) {
                // get column size only first time
                columnSz = (int) Math.ceil((float)((JSONArray)theNext).length()/(float)res);
                first = false;
            }
            pCol = -1;
            Iterator<Object> iterCol = theNext.iterator();
            while(iterCol.hasNext()) { // iterates columns
                pCol++;
                Iterator<Object> iterPixel = ((JSONArray)iterCol.next()).iterator();
                if((int)(pCol-columnSz/2) % columnSz != 0) {
                    // not in column with feature
                    continue;
                }
                int r,g,b;
                r = (int)iterPixel.next();
                g = (int)iterPixel.next();
                b = (int)iterPixel.next();
                features[fcounter] = (r << 16) + (g << 8) + b;
                fcounter++;
            }
        }
        
        return features;
    }
    
    public static String getSummary(Float[] fitnesses) {
        return Collections.min(Arrays.asList(fitnesses)).toString() + ","
                + Collections.max(Arrays.asList(fitnesses)).toString() + ","
                + getAvg(fitnesses).toString() + "\n";
    }
    
    public static Float getAvg(Float[] arr) {
        float sum = 0;
        for(int i = 0; i < arr.length; i++) {
            sum += arr[i];
        }
        
        return sum/arr.length;
    }
    
    public static LinkedList<String> getGameQueue(ArrayList<String> keys) {
        Collections.shuffle(keys);
        return new LinkedList<String>(keys);
    }
}
