package com.orangomango.building.user;

import org.json.*;
import java.util.*;

public class Inventory{
	public static class Item{
		private int id, amount;
		
		public Item(JSONObject o){
			this.id = o.getInt("id");
			this.amount = o.getInt("amount");
		}
		
		public int getId(){
			return this.id;
		}
		
		public int getAmount(){
			return this.amount;
		}
	}
	
	private List<Item> items = new ArrayList<>();
	
	public Inventory(JSONArray array){
		for (Object o : array){
			JSONObject item = (JSONObject) o;
			items.add(new Item(item));
		}
	}
	
	public List<Item> getItems(){
		return this.items;
	}
}
