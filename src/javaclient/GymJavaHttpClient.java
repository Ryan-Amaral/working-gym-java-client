package javaclient;


import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import java.util.Set;
import org.json.JSONObject;

//much based on: https://www.mkyong.com/java/how-to-send-http-request-getpost-in-java/
public class GymJavaHttpClient {
	
	private String baseUrl;
	private HttpURLConnection con;
	
	public GymJavaHttpClient(String url) {
		baseUrl = url;
	}
	
	public Set<String> listEnvs(){
		connect("/v1/envs/", "GET", null);
		return getJson().getJSONObject("all_envs").keySet();
	}
	
	public String createEnv(String envId) {
		connect("/v1/envs/", 
				"POST", 
				"{\"env_id\":\"" + envId + "\"}");
		return getJson().getString("instance_id");
	}
	
	/**
	 * Update to return Observation.
	 * @param instanceId
	 */
	public void resetEnv(String instanceId) {
		connect("/v1/envs/" + instanceId + "/reset/", 
				"POST", 
				"{\"instance_id\":\"" + instanceId + "\"}");
		//return getJson().getSomething("observation");
	}
	
	/**
	 * Return info and stuff later.
	 * @param instanceId
	 */
	public void stepEnv(String instanceId, double action, boolean isDiscreteSpace) {
		if(isDiscreteSpace) {
			connect("/v1/envs/" + instanceId + "/step/", 
					"POST", 
					"{\"instance_id\":\"" + instanceId + "\", \"action\":" + (int)action + "}");
		}else {
			connect("/v1/envs/" + instanceId + "/step/", 
					"POST", 
					"{\"instance_id\":\"" + instanceId + "\", \"action\":" + action + "}");
		}
		
		// return stuff
	}
	
	/**
	 * return info later.
	 * @param instanceId
	 */
	public void actionSpace(String instanceId) {
		connect("/v1/envs/" + instanceId + "/action_space/", 
				"GET", 
				"{\"instance_id\":\"" + instanceId + "\"}");
		//return info
	}
	
	/**
	 * return info later.
	 * @param instanceId
	 */
	public void observationSpace(String instanceId) {
		connect("/v1/envs/" + instanceId + "/observation_space/", 
				"GET", 
				"{\"instance_id\":\"" + instanceId + "\"}");
		//return info
	}
	
	public void startMonitor(String instanceId, boolean force, boolean resume) {
		connect("/v1/envs/" + instanceId + "/monitor/start/", 
				"POST", 
				"{\"instance_id\":\"" + instanceId + "\", \"force\":" + Boolean.toString(force) + 
				", \"resume\":" + Boolean.toString(resume) + "}");
	}
	
	public void closeMonitor(String instanceId) {
		connect("/v1/envs/" + instanceId + "/monitor/close/", 
				"POST", 
				"{\"instance_id\":\"" + instanceId + "\"}");
	}
	
	public void flushMonitorToDisk(String trainingDir, String apiKey, String algId) {
		connect("/v1/upload/", 
				"POST", 
				"{\"training_dir\":\"" + trainingDir + "\"," +
				"\"api_key\":\"" + apiKey + "\"," +
				"\"algorithm_id\":\"" + algId + "\"}");
	}
	
	public void shutdownServer() {
		connect("/v1/shutdown/", 
				"POST", 
				null);
	}
	
	// from: https://stackoverflow.com/questions/11901831/how-to-get-json-object-from-http-request-in-java
	private JSONObject getJson() {
		JSONObject json = null;
		try {
			Scanner scanner = new Scanner(con.getInputStream());
			String response = scanner.useDelimiter("\\Z").next();
			json = new JSONObject(response);
			scanner.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return json;
	}

	// https://www.mkyong.com/java/how-to-send-http-request-getpost-in-java/
	private void connect(String urlEx, String mthd, String args) {
		try {
			URL url = new URL(baseUrl + urlEx);
			con = (HttpURLConnection)(url).openConnection();
			con.setRequestMethod(mthd);
			if(mthd.equals("GET")) {
				int responseCode = con.getResponseCode();
				System.out.println("\nSending 'GET' request to URL : " + url);
				System.out.println("Response Code : " + responseCode);
			}else { // post
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
	
	public static void main(String args[]) {
		GymJavaHttpClient clnt = new GymJavaHttpClient("http://127.0.0.1:5000");
		System.out.println(clnt.createEnv("CartPole-v0"));
		System.out.println(clnt.listEnvs().toString());
	}
	
}
