package com.orangomango.building.model;

import javafx.scene.paint.Color;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

import java.io.File;
import java.io.InputStream;
import org.json.*;
import static com.orangomango.building.MainApplication.getPositionData;

public class Tile{
	private int x, y, type;
	private Color color;
	private boolean selected;
	private double alpha = 1.0;
	private int w = 1, h = 1;

	private static final ImageAtlas atlas = new ImageAtlas(new File("atlas.json"));
	public static final Image[] images = new Image[atlas.getJSON().getJSONArray("images").length()+2];
	
	static {
		// 0 -> nothing, 1 -> empty
		
		int counter = 2;
		for (Object object : atlas.getJSON().getJSONArray("images")){
			JSONObject json = (JSONObject)object;
			InputStream stream = Tile.class.getClassLoader().getResourceAsStream(json.getString("fileName"));
			images[counter] = stream != null ? new Image(stream) : null;
			counter++;
		}
	}

	public static final double OFFSET_X = 32;
	public static final double OFFSET_Y = 16;
	
	public Tile(int type, int x, int y, Color color){
		this.type = type;
		this.x = x;
		this.y = y;
		if (type > 1){
			this.w = atlas.getImageById(type).getInt("width");
			this.h = atlas.getImageById(type).getInt("height");
		}
		this.color = color;
	}
	
	public void render(GraphicsContext gc){
		/*
		 * Width = 2*OFFSET_X+OFFSET_X*(w-1)+OFFSET_X*(h-1) = (2+w+h-2)*OFFSET_X = (w+h)*OFFSET_X
		 * Height = 2*OFFSET_Y+OFFSET_Y*(h-1)+OFFSET_Y*(w-1) = (2+w+h-2)*OFFSET_Y = (w+h)*OFFSET_Y
		 * 
		 * First point -> y = OFFSET_Y+OFFSET_Y*(w-1) = w*OFFSET_Y
		 * x = w*OFFSET_X
		 * bottom x -> y*OFFSET_X
		 * right y = y*OFFSET_Y
		 */
		
		gc.save();
		gc.translate(this.x*OFFSET_X+this.y*OFFSET_X, this.y*OFFSET_Y-this.x*OFFSET_Y);
		gc.setGlobalAlpha(this.alpha);
		if (this.type == 1){
			gc.setFill(this.color);
			gc.fillPolygon(new double[]{0, OFFSET_X, OFFSET_X*2, OFFSET_X}, new double[]{OFFSET_Y, 0, OFFSET_Y, OFFSET_Y*2}, 4);
		} else if (this.type > 1){
			gc.translate(0, -OFFSET_Y*(this.w-1));
			gc.drawImage(images[this.type], 0, OFFSET_Y*(this.w+this.h)-images[this.type].getHeight());
		}
		gc.restore();
	}
	
	public boolean isSelected(){
		return this.selected;
	}
	
	public void toggleSelect(){
		this.selected = !this.selected;
		Thread t = new Thread(() -> {
			while (this.selected){
				try {
					this.alpha = this.alpha == 1.0 ? 0.7 : 1.0;
					Thread.sleep(500);
				} catch (InterruptedException ex){
					ex.printStackTrace();
				}
			}
			this.alpha = 1.0;
		});
		t.setDaemon(true);
		t.start();
	}
	
	public boolean isPlaceAvailable(int type, Model m){
		if (atlas.getImageById(type) == null) return true;
		if (atlas.getImageById(type).getInt("width")+this.x > m.getWidth()){
			return false;
		}
		if (atlas.getImageById(type).getInt("height")+this.y > m.getHeight()){
			return false;
		}
		for (int i = 0; i < atlas.getImageById(type).getInt("width"); i++){
			for (int j = 0; j < atlas.getImageById(type).getInt("height"); j++){
				if (i < this.w && j < this.h) continue;
				Tile t = m.getTileAt(this.x+i, this.y+j);
				if (t.getType() != 1){
					return false;
				}
			}
		}
		return true;
	}
	
	public void setType(int type, Model m){
		int w = this.w;
		int h = this.h;
		if (type > 1){
			if (isPlaceAvailable(type, m)){
				this.w = atlas.getImageById(type).getInt("width");
				this.h = atlas.getImageById(type).getInt("height");
			} else {
				return;
			}
		} else {
			this.w = 1;
			this.h = 1;
		}
		this.type = type;
		for (int i = 0; i < w; i++){
			for (int j = 0; j < h; j++){
				m.setTile(new Tile(1, this.x+i, this.y+j, Color.color(((double)this.x+i)/m.getWidth(), ((double)this.y+j)/m.getHeight(), 1.0)));
			}
		}
		for (int i = 0; i < this.w; i++){
			for (int j = 0; j < this.h; j++){
				m.removeTile(this.x+i, this.y+j);
			}
		}
		m.setTile(this);
		m.adjustVoids();
	}
	
	public boolean contains(double x, double y){
		for (int i = 0; i < this.w; i++){
			for (int j = 0; j < this.h; j++){
				double centerX = (getPositionData()[0]+(this.x+1+i)*OFFSET_X+(this.y+j)*OFFSET_X)*getPositionData()[2];
				double centerY = (getPositionData()[1]+(this.y+1+j)*OFFSET_Y-(this.x+i)*OFFSET_Y)*getPositionData()[3];
				double distance = Math.sqrt(Math.pow(x-centerX, 2)+Math.pow(y-centerY, 2));
				if (distance <= OFFSET_Y){
					return true;
				}
			}
		}
		return false;
	}
	
	public int getX(){
		return this.x;
	}
	
	public int getY(){
		return this.y;
	}
	
	public int getWidth(){
		return this.w;
	}
	
	public int getHeight(){
		return this.h;
	}
	
	public int getType(){
		return this.type;
	}
}
