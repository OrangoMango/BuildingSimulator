package com.orangomango.building.model;

import java.io.*;
import org.json.*;

public class ImageAtlas{
	private File file;
	private JSONObject json;
	
	public ImageAtlas(File file){
		this.file = file;
		try {
			StringBuilder builder = new StringBuilder();
			BufferedReader reader = new BufferedReader(new FileReader(this.file));
			reader.lines().forEach(line -> builder.append(line));
			reader.close();
			this.json = new JSONObject(builder.toString());
		} catch (IOException ex){
			ex.printStackTrace();
		}
	}
	
	public JSONObject getJSON(){
		return this.json;
	}
	
	public JSONObject getItemById(int id){
		for (Object object : this.json.getJSONArray("items")){
			JSONObject ob = (JSONObject)object;
			if (ob.getInt("id") == id) return ob;
		}
		return null;
	}
	
	public JSONObject getImageById(int id){
		for (Object object : this.json.getJSONArray("images")){
			JSONObject ob = (JSONObject)object;
			if (ob.getInt("id") == id) return ob;
		}
		return null;
	}
}
