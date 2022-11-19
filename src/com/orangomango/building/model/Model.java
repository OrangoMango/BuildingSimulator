package com.orangomango.building.model;

import java.io.*;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Model{
	private int width, height;
	private GraphicsContext gc;
	private Tile[][] map;
	
	public Model(GraphicsContext gc, File file){
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = reader.readLine();
			this.width = Integer.parseInt(line.split("x")[0]);
			this.height = Integer.parseInt(line.split("x")[1]);
			this.map = new Tile[this.width][this.height];
			this.gc = gc;
			for (int i = 0; i < this.height; i++){
				String l = reader.readLine();
				for (int j = 0; j < this.width; j++){
					String[] b = l.split(" ");
					setTile(new Tile(Integer.parseInt(b[j]), j, i, Integer.parseInt(b[j]) != 0 ? Color.color((double)j/this.width, (double)i/this.height, 1.0) : null));
				}
			}
			reader.close();
		} catch (IOException ex){
			ex.printStackTrace();
		}
	}
	
	public void adjustVoids(){
		for (int x = 0; x < this.width; x++){
			for (int y = 0; y < this.height; y++){
				if (this.map[x][y] == null || this.map[x][y].getType() == -1){
					this.map[x][y] = new Tile(1, x, y, Color.color((double)x/this.map.length, (double)y/this.map[0].length, 1.0));
				}
			}
		}		
		
		for (int x = 0; x < this.width; x++){
			for (int y = 0; y < this.height; y++){
				Tile t = this.map[x][y];
				if (t.getType() > 1){
					for (int i = 0; i < t.getWidth(); i++){
						for (int j = 0; j < t.getHeight(); j++){
							removeTile(x+i, y+j);
						}
					}
					setTile(t);
				}
			}
		}
	}
	
	private void copyMap(Tile[][] array){
		for (int x = 0; x < this.width; x++){
			for (int y = 0; y < this.height; y++){
				array[x][y] = this.map[x][y];
			}
		}
	}
	
	public void addRow(){
		Tile[][] map = new Tile[this.width][this.height+1];
		copyMap(map);
		this.map = map;
		this.height++;
		adjustVoids();
	}
	
	public void addColumn(){
		Tile[][] map = new Tile[this.width+1][this.height];
		copyMap(map);
		this.map = map;
		this.width++;
		adjustVoids();
	}
	
	public void removeRow(){
	}
	
	public void removeColumn(){
	}
	
	public void clear(){
		for (int x = 0; x < this.width; x++){
			for (int y = 0; y < this.height; y++){
				this.map[x][y] = new Tile(1, x, y, Color.color((double)x/this.width, (double)y/this.height, 1.0));
			}
		}
	}
	
	public void setTile(Tile t){
		this.map[t.getX()][t.getY()] = t;
	}
	
	public void removeTile(int x, int y){
		this.map[x][y] = new Tile(-1, x, y, null);
	}
	
	public Tile getTileAt(int x, int y){
		return this.map[x][y];
	}
	
	public int getHeight(){
		return this.height;
	}

	public int getWidth(){
		return this.width;
	}
	
	public void saveToFile(File file){
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			writer.write(this.width+"x"+this.height+"\n");
			for (int i = 0; i < this.height; i++){
				for (int j = 0; j < this.width; j++){
					writer.write(getTileAt(j, i).getType()+(j == this.width-1 ? "" : " "));
				}
				if (i < this.height-1) writer.write("\n");
			}
			writer.close();
		} catch (IOException ex){
			ex.printStackTrace();
		}
	}
	
	public void render(){
		for (int i = 0; i < this.height; i++){
			for (int j = this.width-1; j >= 0; j--){
				Tile t = this.map[j][i];
				if (t != null) t.render(this.gc);
			}
		}
	}
}
