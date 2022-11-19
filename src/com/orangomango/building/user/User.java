package com.orangomango.building.user;

import java.io.*;
import org.json.*;
import java.util.*;

public class User{
	private String username;
	private JSONObject json;
	private Inventory inventory;
	
	private static final String USER_HOME = System.getProperty("user.home");
	private static final String APP_HOME = USER_HOME+File.separator+".buildingsim";
	
	public User(String username){
		this.username = username;
		setupDirectory();
	}
	
	private void setupDirectory(){
		createDirectory(new File(APP_HOME));
		try {
			File userData = new File(APP_HOME, "data.json");
			if (!userData.exists()){
				userData.createNewFile();
				this.json = new JSONObject(String.format("{\"username\":%s,\"inventory\":[]}", this.username));
				updateFile(userData);
			} else {
				loadFile(userData);
			}
			this.inventory = new Inventory(this.json.getJSONArray("inventory"));
		} catch (IOException ex){
			ex.printStackTrace();
		}
	}
	
	public Inventory getInventory(){
		return this.inventory;
	}
	
	public JSONObject getItemFromInventory(int id){
		for (Object o : this.json.getJSONArray("inventory")){
			JSONObject item = (JSONObject) o;
			if (item.getInt("id") == id) return item;
		}
		return null;
	}
	
	public JSONObject getJSON(){
		return this.json;
	}
	
	private void updateFile(File file){
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			writer.write(this.json.toString(4));
			writer.close();
		} catch (IOException ex){
			ex.printStackTrace();
		}
	}
	
	private void loadFile(File file){
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			StringBuilder builder = new StringBuilder();
			reader.lines().forEach(line -> builder.append(line));
			this.json = new JSONObject(builder.toString());
			this.username = this.json.getString("username");
			reader.close();
		} catch (IOException ex){
			ex.printStackTrace();
		}
	}
	
	private void createDirectory(File dir){
		if (!dir.exists()){
			dir.mkdir();
		}
	}
	
	public String getUsername(){
		return this.username;
	}
}
