package agent;
import javaclient.*;
import java.util.Random;

import org.json.JSONObject;

public class SampleAgent {

	public static void main(String[] args) {
		GymJavaHttpClient client = new GymJavaHttpClient(); // create the client object
		String id = client.createEnv("CartPole-v0"); // create an environment
		client.resetEnv(id); // reset the environment
		Object actionSpace = client.actionSpace(id);
		System.out.println(actionSpace.getClass().getName()); // helpful to know how to deal with
		System.out.println(actionSpace); // helpful to see format of object
		int numActions = ((JSONObject)actionSpace).getInt("n");
	}
}
