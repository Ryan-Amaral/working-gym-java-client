package agent;
import javaclient.*;

import java.util.Iterator;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONObject;

public class SampleAgent {

	public static void main(String[] args) {
	    
		GymJavaHttpClient.baseUrl = "http://127.0.0.1:5000"; // this is the default value, but just showing that you can change it
		
		String id = GymJavaHttpClient.createEnv("CartPole-v0"); // create an environment
		
		Object actionSpace = GymJavaHttpClient.actionSpace(id);
		
		// Do this if not a standard attribute
		System.out.println(actionSpace.getClass().getName()); // helpful to know how to deal with
		System.out.println(actionSpace); // helpful to see format of object
		//int numActions = ((JSONObject)actionSpace).getInt("n");
		
		// but we have method to get action space size from action space object
		int numActions = GymJavaHttpClient.actionSpaceSize((JSONObject) actionSpace);

        int action; // action for agent to do
		Object obs = GymJavaHttpClient.resetEnv(id); // reset the environment (get initial observation)
		System.out.println(obs.getClass().getName());// see what observation looks like to work with it
		System.out.println(obs.toString());
		
        Boolean isDone = false; // whether current episode is done
        float reward = 0;
        int counter = 0;
        
        while(!isDone && counter < 2000) { // do steps
            action = obsToAction(obs);
            StepObject step = GymJavaHttpClient.stepEnv(id, action, true, true);
            obs = step.observation;
            isDone = step.done;
            reward += step.reward;
            counter++;
        }
        
        System.out.println("The agent got reward: " + reward);
	}
	
	/**
	 * Do a policy where you add the values in observation and return 1 or 0 based on the sum.
	 * @param obs
	 * @return
	 */
	public static int obsToAction(Object obs) {
	    Iterator<Object> iter = ((JSONArray)obs).iterator();
	    double sum = 0;
	    while(iter.hasNext()) {
	        sum += (double)iter.next();
	    }
	    
	    if(sum > 0) {
	        return 1;
	    } else {
	        return 0;
	    }
	}
}
