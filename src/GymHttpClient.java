

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;
import java.util.Set;
import org.json.JSONObject;

//much based on: https://www.mkyong.com/java/how-to-send-http-request-getpost-in-java/
public class GymHttpClient {
	
	private String baseUrl;
	private HttpURLConnection con;
	
	public GymHttpClient(String url) {
		baseUrl = url;
	}
	
	public Set<String> listEnvs(){
		connect("/v1/envs/", "GET", null);
		return getJson().getJSONObject("all_envs").keySet();
	}
	
	public String createEnv(String envId) {
		connect("/v1/envs/", "POST", "{\"env_id\":\"" + envId + "\"}");
		return getJson().getString("instance_id");
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
		GymHttpClient clnt = new GymHttpClient("http://127.0.0.1:5000");
		System.out.println(clnt.createEnv("CartPole-v0"));
		System.out.println(clnt.listEnvs().toString());
	}
	
}
