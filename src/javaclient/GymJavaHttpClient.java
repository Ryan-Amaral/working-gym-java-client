package javaclient;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import java.util.Set;

import org.json.*;

/**
 * Contains methods that correspond with the OpenAI Gym HTTP API 
 * (https://github.com/openai/gym-http-api), check there for more details about the methods.
 * @author Ryan Amaral - (ryan-amaral on GitHub)
 */
public class GymJavaHttpClient {

    private String baseUrl; // probably "http://127.0.0.1:5000"
    private HttpURLConnection con; // object to use to create and do stuff with connection

    /**
     * Creates a client with the default base url ("http://127.0.0.1:5000").
     */
    public GymJavaHttpClient() {
    	this.baseUrl = "http://127.0.0.1:5000";
    }
    
    /**
     * Creates a client with selected baseUrl.
     * @param baseUrl Ex: "http://127.0.0.1:5000"
     */
    public GymJavaHttpClient(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * List all of the environments you started that are currently running on the server.
     * @return A set of the environments' instance Id's.
     */
    public Set<String> listEnvs() {
        connect("/v1/envs/", "GET", null);
        return getJson().getJSONObject("all_envs").keySet();
    }

    /**
     * Creates a new environment of the type specified.
     * @param envId The id of the environment to create (ex: "CartPole-v0").
     * @return The instance id of the created environment.
     */
    public String createEnv(String envId) {
        connect("/v1/envs/", "POST", "{\"env_id\":\"" + envId + "\"}");
        return getJson().getString("instance_id");
    }

    /**
     * Resets the selected environment.
     * @param instanceId The id of the environment.
     * @return Whatever the observation of the environment is. Probably JSONArray.
     */
    public Object resetEnv(String instanceId) {
        connect("/v1/envs/" + instanceId + "/reset/", "POST", "{\"instance_id\":\"" + instanceId + "\"}");
        return getJson().get("observation"); // probably of type JSONArray
    }

    /**
     * Steps the environment.
     * @param instanceId The id of the environment.
     * @param action The action to do in the step.
     * @param isDiscreteSpace Whether space in the environment is discrete or not.
     * @return A StepObject, check out that class.
     */
    public StepObject stepEnv(String instanceId, double action, boolean isDiscreteSpace, boolean render) {
        if (isDiscreteSpace) {
        	connect("/v1/envs/" + instanceId + "/step/", "POST",
                    "{\"instance_id\":\"" + instanceId + "\", \"action\":" + (int) action + 
                    ", \"render\":" + Boolean.toString(render) + "}");
        } else {
            connect("/v1/envs/" + instanceId + "/step/", "POST",
                    "{\"instance_id\":\"" + instanceId + "\", \"action\":" + action + 
                    ", \"render\":" + Boolean.toString(render) + "}");
        }
        JSONObject jobj = getJson();
        
        return new StepObject(
        		jobj.get("observation"), jobj.getFloat("reward"), 
        		jobj.getBoolean("done"), jobj.get("info"));
    }

    /**
     * Gets the name and the dimensions of the environment's action space.
     * @param instanceId The id of the environment.
     * @return Whatever the action space of the environment is. Probably JSONObject.
     */
    public Object actionSpace(String instanceId) {
        connect("/v1/envs/" + instanceId + "/action_space/", "GET", "{\"instance_id\":\"" + instanceId + "\"}");
        return getJson().get("info");
    }
    
    /**
     * Gets the dimension from the JSONObject obtained from actionSpace.
     * @param jobj JSONObject from actionSpace.
     * @return Whether the space is discrete.
     */
    public boolean isActionSpaceDiscrete(JSONObject jobj) {
        String name = jobj.getString("name");
        if(name.equals("Discrete")) {
            return true;
        }else {
            return false;
        }
    }
    
    public int actionSpaceSize(JSONObject jobj) {
        return jobj.getInt("n");
    }

    /**
     * *** I COULDN'T ACTUALLY GET THIS ONE TO WORK, MAYBE MY TEST ENVIRONMENT DOESN'T USE THIS? ***
     * Gets the name and the dimensions of the environment's observation space.
     * @param instanceId The id of the environment.
     * @return Whatever the observation space of the environment is.
     */
    public void observationSpace(String instanceId) {
        connect("/v1/envs/" + instanceId + "/observation_space/", "GET", "{\"instance_id\":\"" + instanceId + "\"}");
        System.out.println(getJson().toString());
    }

    /**
     * *** DIDN'T TEST! ***
     * Start monitoring.
     * @param instanceId The id of the environment.
     * @param force Whether to clear existing training data.
     * @param resume Keep data that's already in.
     */
    public void startMonitor(String instanceId, boolean force, boolean resume) {
        connect("/v1/envs/" + instanceId + "/monitor/start/", "POST", "{\"instance_id\":\"" + instanceId
                + "\", \"force\":" + Boolean.toString(force) + ", \"resume\":" + Boolean.toString(resume) + "}");
    }

    /**
     * *** DIDN'T TEST! ***
     * Flush all monitor data to disk.
     * @param instanceId The id of the environment.
     */
    public void closeMonitor(String instanceId) {
        connect("/v1/envs/" + instanceId + "/monitor/close/", "POST", "{\"instance_id\":\"" + instanceId + "\"}");
    }

    /**
     * *** DIDN'T TEST! ***
     * Probably uploads your thing to OpenAI? The method just said "Flush all monitor data to disk"
     * on the Gym HTTP API GitHub page, but it seems to do something different, I'm new to gym so I
     * don't really know.
     * @param trainingDir
     * @param apiKey
     * @param algId
     */
    public void upload(String trainingDir, String apiKey, String algId) {
        connect("/v1/upload/", "POST", "{\"training_dir\":\"" + trainingDir + "\"," + "\"api_key\":\"" + apiKey + "\","
                + "\"algorithm_id\":\"" + algId + "\"}");
    }

    /**
     * *** COULDN'T GET IT TO WORK! ***
     * Attempts to shutdown the server.
     */
    public void shutdownServer() {
        connect("/v1/shutdown/", "POST", null);
    }

    /**
     * Get JSON from the connection: technique from: 
     * https://stackoverflow.com/questions/11901831/how-to-get-json-object-from-http-request-in-java
     * @return The JSON obtained from the connection, first line only, which should hopefully
     * contain all JSON.
     */
    private JSONObject getJson() {
        JSONObject json = null;
        try {
            Scanner scanner = new Scanner(con.getInputStream());
            String response = scanner.useDelimiter("\\Z").next();
            json = new JSONObject(response);
            scanner.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return json;
    }

    /**
     * Does either a post or get request on the base url + urlEx. Learned from:
     * https://www.mkyong.com/java/how-to-send-http-request-getpost-in-java/ .
     * @param urlEx The extension to add onto base url.
     * @param mthd POST or GET.
     * @param args What to pass for a Post request, make null if not used.
     */
    private void connect(String urlEx, String mthd, String args) {
        try {
            URL url = new URL(baseUrl + urlEx);
            con = (HttpURLConnection) (url).openConnection();
            con.setRequestMethod(mthd);
            if (mthd.equals("POST")) {
                con.setDoOutput(true);
                con.setRequestProperty("Content-Type", "application/json");
                con.setRequestProperty("Accept", "application/json");
                DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                wr.writeBytes(args);
                wr.flush();
                wr.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Same as connect method but with prints for debugging. 
     */
    @SuppressWarnings("unused")
	private void connectDebug(String urlEx, String mthd, String args) {
        try {
            URL url = new URL(baseUrl + urlEx);
            con = (HttpURLConnection) (url).openConnection();
            con.setRequestMethod(mthd);
            if (mthd.equals("GET")) {
                int responseCode = con.getResponseCode();
                System.out.println("\nSending 'GET' request to URL : " + url);
                System.out.println("Response Code : " + responseCode);
            } else { // post
                con.setDoOutput(true);
                con.setRequestProperty("Content-Type", "application/json");
                con.setRequestProperty("Accept", "application/json");
                DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                wr.writeBytes(args);
                wr.flush();
                wr.close();

                int responseCode = con.getResponseCode();
                System.out.println("\nSending 'POST' request to URL : " + url);
                System.out.println("Post parameters : " + args);
                System.out.println("Response Code : " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Test simple stuff out here by running this file.
     * @param args
     */
    
    public static void main(String args[]) {
        GymJavaHttpClient clnt = new GymJavaHttpClient();
        String instId = clnt.createEnv("CartPole-v0");
        clnt.listEnvs().toString();
        clnt.resetEnv(instId);
        clnt.actionSpace(instId);
        float rwdSum = 0;
        for(int i = 0; i < 1000; i++) {
        	StepObject sobj = clnt.stepEnv(instId, i%2, true, true);
        	rwdSum += sobj.reward;
        	System.out.println(rwdSum);
        }
    }

}
