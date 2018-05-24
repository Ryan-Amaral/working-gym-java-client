package agent;
import javaclient.*;
import java.util.Random;

import org.json.JSONObject;

public class SampleAgent {

	public static void main(String[] args) {
		GymJavaHttpClient.baseUrl = "http://127.0.0.1:5000"; // this is the default value, but just showing that you can change it
		String id = GymJavaHttpClient.createEnv("CartPole-v0"); // create an environment
		GymJavaHttpClient.resetEnv(id); // reset the environment
		Object actionSpace = GymJavaHttpClient.actionSpace(id);
		System.out.println(actionSpace.getClass().getName()); // helpful to know how to deal with
		System.out.println(actionSpace); // helpful to see format of object
		int numActions = ((JSONObject)actionSpace).getInt("n");
	}
}
