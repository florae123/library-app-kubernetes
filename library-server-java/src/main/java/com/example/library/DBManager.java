package com.example.library;

import com.cloudant.client.api.ClientBuilder;
import com.cloudant.client.api.CloudantClient;
import com.cloudant.client.api.Database;
//import com.cloudant.client.org.lightcouch.CouchDbException;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.cloudant.*;


public class DBManager {

	private static CloudantClient cloudant = null;
	private static Database db = null;

	private static final String FILENAME = "C:\\dev\\src\\demo\\library-server-java\\vcap-env.json";

	private static String user;
	private static String password;


	public static void getCred(){
		String VCAP_SERVICES = System.getenv("VCAP_SERVICES");
		String dockeruser = System.getenv("username");
		String dockerpw = System.getenv("password");
		System.out.println("dockeruser: "+dockeruser);
		System.out.println("dockerpassword: "+dockerpw);
		System.out.println("cloudant-local: "+System.getenv("CLOUDANT_DEVELOPER"));
		JSONObject vcap = new JSONObject();
		//if cloud foundry app
		if (VCAP_SERVICES != null) {
			vcap = new JSONObject(VCAP_SERVICES);
			JSONArray cloudant = vcap.getJSONArray("cloudantNoSQLDB");
			JSONObject cred = cloudant.getJSONObject(0).getJSONObject("credentials");
			user = cred.getString("username");
			password = cred.getString("password");
		//if running in docker container
		} else if (dockeruser!=null && dockerpw!=null) {
			user = dockeruser;
			password = dockerpw;
		//if running locally
		} else {
			try {
				String content = new String(Files.readAllBytes(Paths.get(FILENAME)));
				System.out.println(content);
				vcap = new JSONObject(content);
				JSONArray cloudant = vcap.getJSONArray("cloudantNoSQLDB");
				JSONObject cred = cloudant.getJSONObject(0).getJSONObject("credentials");
				user = cred.getString("username");
				password = cred.getString("password");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("db user: "+user);
		System.out.println("db password: "+password);
	}



	public static CloudantClient createClient(){
		getCred();
		try {
			String cloudantdeveloper="";
			try{
				cloudantdeveloper = System.getenv("CLOUDANT_DEVELOPER");
			} catch (Exception e){
				cloudantdeveloper = "nothing";
				System.out.println("no cloudant-developer db");
			}
			System.out.println("cloudantdeveloper: "+cloudantdeveloper);
			System.out.println("cloudantdeveloper is null: "+(cloudantdeveloper==null));
			String kubernetes_cloudant_host = "";
			kubernetes_cloudant_host=System.getenv("CLOUDANT_DEVELOPER_SERVICE_HOST");
			System.out.println("cloudant-test-serivce-host: "+kubernetes_cloudant_host);
			if(cloudantdeveloper==null || !cloudantdeveloper.equals("1")) {
				//System.out.println("Cloudant developer: "+System.getenv("CLOUDANT_DEVELOPER"));
				//System.out.println(System.getenv("CLOUDANT_DEVELOPER").equals("1"));
				System.out.println("Connecting to Cloudant : " + user);
				CloudantClient client = ClientBuilder.account(user)
						.username(user)
						.password(password)
						.build();
				return client;
			} else {
				if(kubernetes_cloudant_host==null) {
					System.out.println("Connecting to Cloudant : " + "admin");
					System.out.println("Using other url http://admin:pass@cloudant instead of url ibm/com/cloudant-developer://cloudant");
					CloudantClient client = ClientBuilder.url(new URL("http://admin:pass@cloudant"))
						 .username("admin")
						 .password("pass") //default values
						 .build();
					return client;
				} else {
					System.out.println("Connecting to Cloudant : " + "admin");
					System.out.println("Using other url http://admin:pass@kubernetes_cloudant_host");
					CloudantClient client = ClientBuilder.url(new URL("http://admin:pass@"+kubernetes_cloudant_host+":"+System.getenv("CLOUDANT_DEVELOPER_SERVICE_PORT")))
						 .username("admin")
						 .password("pass") //default values
						 .build();
					return client;
				}
			}
		//} catch (CouchDbException e) {
		} catch (Exception e){
			throw new RuntimeException("Unable to connect to repository", e);
		}
	}

	public static void initClient() {
		if (cloudant == null) {
			cloudant = createClient();
			return;
		}else{
			return;
		}
	}

	public static Database getDB(String databaseName) {
		if (cloudant == null) {
			initClient();
		}

		try {
			db = cloudant.database(databaseName, false);
		} catch (Exception e) {
			throw new RuntimeException("DB Not found", e);
		}

		return db;
	}

}
