package com.orangomango.building;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.canvas.*;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.scene.input.MouseButton;
import javafx.util.Duration;
import javafx.animation.*;
import javafx.geometry.Rectangle2D;

import java.io.*;
import java.util.zip.*;
import org.json.JSONObject;
import com.orangomango.building.model.*;
import com.orangomango.building.user.*;
import com.orangomango.account.Account;

public class MainApplication extends Application{
	private Model model;
	private static double cameraX, cameraY, mouseOldX, mouseOldY, scaleX = 1.0, scaleY = 1.0;
	private int buildPage = 0;
	private Tile selectedTile;
	private ImageAtlas atlas = new ImageAtlas(new File("atlas.json"));;
	private int showingInfo = -1, selectedInventoryItem = 0;
	private User user;
	private boolean openMenu, showInventory;
	private int frames, fps;
	
	private static Image PAGE_NEXT_IMAGE = new Image(MainApplication.class.getClassLoader().getResourceAsStream("page_next.png"));
	private static Image PAGE_PREVIOUS_IMAGE = new Image(MainApplication.class.getClassLoader().getResourceAsStream("page_previous.png"));
	private static Image BUTTON_CLOSE_IMAGE = new Image(MainApplication.class.getClassLoader().getResourceAsStream("button_close.png"));
	private Image[] itemImages = new Image[this.atlas.getJSON().getJSONArray("items").length()];

	public static void main(String[] args){
		launch(args);
	}
	
	@Override
	public void start(Stage stage){
		stage.setTitle("Building simulator");
		
		Thread fpsCounter = new Thread(() -> {
			while (true){
				try {
					this.fps = frames;
					frames = 0;
					Thread.sleep(1000);
				} catch (InterruptedException ex){
					ex.printStackTrace();
				}
			}
		});
		fpsCounter.setDaemon(true);
		fpsCounter.start();
		
		Account account = new Account("Test", "ciao");
		System.out.println(account.checkCredentials());
		
		Canvas canvas = new Canvas(800, 500);
		GraphicsContext gc = canvas.getGraphicsContext2D();
		
		for (int i = 0; i < itemImages.length; i++){
			this.itemImages[i] = new Image(getClass().getClassLoader().getResourceAsStream(this.atlas.getItemById(i).getString("fileName")));
		}
		
		this.model = new Model(gc, new File("test.world"));
		this.user = new User("orangomango");

		canvas.setOnMousePressed(e -> {	
			if (e.getButton() == MouseButton.PRIMARY){
				// This if-statement will only be executed if a dialog is open
				if (openMenu || showingInfo >= 0 || showInventory){
					if (showingInfo >= 0){
						if ((new Rectangle2D(520, 150, 30, 30)).contains(e.getX(), e.getY())){
							showingInfo = -1;
						}
					} else if (openMenu){
						if ((new Rectangle2D(570, 100, 30, 30)).contains(e.getX(), e.getY())){
							openMenu = false;
						}
					} else if (showInventory){
						if ((new Rectangle2D(570, 100, 30, 30)).contains(e.getX(), e.getY())){
							showInventory = false;
							selectedInventoryItem = 0;
						} else {
							int x = 0, y = 0;
							for (Inventory.Item item : this.user.getInventory().getItems()){
								if ((new Rectangle2D(220+x*55, 120+y*65, 44, 44)).contains(e.getX(), e.getY())){
									selectedInventoryItem = item.getId();
									break;
								}
								if (x == 3){
									x = 0;
									y++;
								} else {
									x++;
								}
							}
						}
					}
					return;
				}
				if ((new Rectangle2D(645, 20, 30, 30)).contains(e.getX(), e.getY()) && selectedTile != null){
					buildPage++;
				} else if ((new Rectangle2D(605, 20, 30, 30)).contains(e.getX(), e.getY()) && selectedTile != null){
					if (buildPage > 0) buildPage--;
				} else if ((new Rectangle2D(15, 10, 35, 35)).contains(e.getX(), e.getY())){
					this.model.saveToFile(new File("../res/test.world"));
					System.out.println("Saved successfully");
				} else if ((new Rectangle2D(55, 10, 35, 35)).contains(e.getX(), e.getY())){
					this.model = new Model(gc, new File("test.world"));
					System.out.println("Loaded successfully");
				} else if ((new Rectangle2D(95, 10, 35, 35)).contains(e.getX(), e.getY())){
					this.model.clear();
				} else if ((new Rectangle2D(135, 10, 35, 35)).contains(e.getX(), e.getY())){
					showInventory = true;
				} else if ((new Rectangle2D(185, 10, 35, 35)).contains(e.getX(), e.getY())){
					try {
						ZipOutputStream zos = new ZipOutputStream(new FileOutputStream("user.zip"));
						ZipEntry entry = new ZipEntry("test.world");
						zos.putNextEntry(entry);
						
						byte[] buffer = new byte[1024];
						FileInputStream fis = new FileInputStream("test.world");
						int len;
						while ((len = fis.read(buffer)) > 0){
							zos.write(buffer, 0, len);
						}
						fis.close();
						
						zos.closeEntry();
						zos.close();
					} catch (IOException ex){
						ex.printStackTrace();
					}
					account.uploadFile(new File("user.zip"));
					System.out.println("Upload completed");
				} else if ((new Rectangle2D(225, 10, 35, 35)).contains(e.getX(), e.getY())){
					account.downloadFile("user.zip", new File("user.zip"));
					try {
						ZipInputStream zis = new ZipInputStream(new FileInputStream("user.zip"));
						ZipEntry zipEntry = zis.getNextEntry();
						while (zipEntry != null) {
							byte[] buffer = new byte[1024];
							FileOutputStream fos = new FileOutputStream(zipEntry.getName());
							int len;
							while ((len = zis.read(buffer)) > 0){
								fos.write(buffer, 0, len);
							}
							fos.close();
							zipEntry = zis.getNextEntry();
						}
						zis.closeEntry();
						zis.close();
					} catch (IOException ex){
						ex.printStackTrace();
					}
					System.out.println("File downloaded");
				} else if ((new Rectangle2D(15, 70, 35, 35)).contains(e.getX(), e.getY())){
					this.model.addRow();
				} else if ((new Rectangle2D(55, 70, 35, 35)).contains(e.getX(), e.getY())){
					this.model.addColumn();
				} else if ((new Rectangle2D(95, 70, 35, 35)).contains(e.getX(), e.getY())){
					this.model.removeRow();
				} else if ((new Rectangle2D(135, 70, 35, 35)).contains(e.getX(), e.getY())){
					this.model.removeColumn();
				} else if ((new Rectangle2D(690, 35, 10, 10)).contains(e.getX(), e.getY()) && selectedTile != null){
					showingInfo = 0+5*buildPage;
				} else if ((new Rectangle2D(690, 125, 10, 10)).contains(e.getX(), e.getY()) && selectedTile != null){
					showingInfo = 1+5*buildPage;
				} else if ((new Rectangle2D(690, 215, 10, 10)).contains(e.getX(), e.getY()) && selectedTile != null){
					showingInfo = 2+5*buildPage;
				} else if ((new Rectangle2D(690, 305, 10, 10)).contains(e.getX(), e.getY()) && selectedTile != null){
					showingInfo = 3+5*buildPage;
				} else if ((new Rectangle2D(690, 395, 10, 10)).contains(e.getX(), e.getY()) && selectedTile != null){
					showingInfo = 4+5*buildPage;
				} else if ((new Rectangle2D(675, 20, 100, 460)).contains(e.getX(), e.getY()) && selectedTile != null){
					int type = -1;
					if ((new Rectangle2D(685, 30, 80, 80)).contains(e.getX(), e.getY())){
						type = 0;
					} else if ((new Rectangle2D(685, 120, 80, 80)).contains(e.getX(), e.getY())){
						type = 1;
					} else if ((new Rectangle2D(685, 210, 80, 80)).contains(e.getX(), e.getY())){
						type = 2;
					} else if ((new Rectangle2D(685, 300, 80, 80)).contains(e.getX(), e.getY())){
						type = 3;
					} else if ((new Rectangle2D(685, 390, 80, 80)).contains(e.getX(), e.getY())){
						type = 4;
					}
					type += 5*buildPage;
					if (type >= 0){
						selectedTile.setType(type, this.model);
					}
				} else if ((new Rectangle2D(260, 385, 90, 30)).contains(e.getX(), e.getY()) && selectedTile != null && selectedTile.getType() > 1 && this.atlas.getImageById(selectedTile.getType()).optBoolean("openMenu")){
					openMenu = true;
				} else {
					boolean found = false;
					for (int i = 0; i < this.model.getWidth(); i++){
						for (int j = 0; j < this.model.getHeight(); j++){
							Tile t = this.model.getTileAt(i, j);
							if (t != null && t.contains(e.getX(), e.getY()) && t.getType() >= 0){
								t.toggleSelect();
								if (selectedTile != null && selectedTile.isSelected()) selectedTile.toggleSelect();
								selectedTile = t.isSelected() ? t : null;
								found = true;
								break;
							}
						}
					}
					if (!found){
						if (selectedTile != null) selectedTile.toggleSelect();
						selectedTile = null;
					}
				}			
			} else if (e.getButton() == MouseButton.SECONDARY){
				mouseOldX = e.getX();
				mouseOldY = e.getY();
			}
		});
		canvas.setOnMouseDragged(e -> {
			if (e.getButton() == MouseButton.SECONDARY){
				cameraX += e.getX()-mouseOldX;
				cameraY += e.getY()-mouseOldY;
				mouseOldX = e.getX();
				mouseOldY = e.getY();
			}
		});
		canvas.setOnScroll(e -> {
			if (e.getDeltaY() > 0){
				if (scaleX < 1.4 && scaleY < 1.4) scale(gc, scaleX+0.15, scaleY+0.15);
			} else {
				if (scaleX > 0.8 && scaleY > 0.8) scale(gc, scaleX-0.05, scaleY-0.05);
			}
		});
		
		Timeline loop = new Timeline(new KeyFrame(Duration.millis(1000.0/40), e -> update(gc)));
		loop.setCycleCount(Animation.INDEFINITE);
		loop.play();
		
		StackPane pane = new StackPane(canvas);
		Scene scene = new Scene(pane, 800, 500);
		stage.setResizable(false);
		stage.setScene(scene);
		stage.show();
	}
	
	private synchronized void scale(GraphicsContext gc, double sx, double sy){
		gc.scale(1/scaleX, 1/scaleY);
		scaleX = sx;
		scaleY = sy;
		gc.scale(sx, sy);
	}
	
	private void doOnNoScale(GraphicsContext gc, Runnable r){
		double sx = scaleX;
		double sy = scaleY;
		scale(gc, 1, 1);
		r.run();
		scale(gc, sx, sy);
	}
	
	public static double[] getPositionData(){
		return new double[]{cameraX+50, cameraY+300, scaleX, scaleY};
	}
	
	private void update(GraphicsContext gc){
		doOnNoScale(gc, () -> {
			gc.clearRect(0, 0, 800, 500);
			gc.setFill(Color.web("#FCF7BC"));
			gc.fillRect(0, 0, 800, 500);
		});
		gc.save();
		gc.translate(50+cameraX, 300+cameraY);
		this.model.render();
		gc.restore();
		
		final double sx = scaleX;
		final double sy = scaleY;
		doOnNoScale(gc, () -> {
			gc.save();
			gc.setFill(Color.BLACK);
			gc.setGlobalAlpha(0.6);
			gc.fillRect(15, 375, 200, 100);
			gc.restore();
			if (selectedTile != null){
				gc.setFill(Color.WHITE);
				gc.fillText(String.format("Tile at: %d %d of type %d\nwith w: %d h: %d", selectedTile.getX(), selectedTile.getY(), selectedTile.getType(), selectedTile.getWidth(), selectedTile.getHeight()), 25, 395);
				gc.save();
				gc.setFill(Color.BLACK);
				gc.setGlobalAlpha(0.7);
				gc.fillRect(675, 20, 100, 460);
				gc.setFill(selectedTile.isPlaceAvailable(0+5*buildPage, this.model) ? Color.YELLOW : Color.web("#DD3E3E"));
				gc.fillRect(685, 30, 80, 80);
				if (0+5*buildPage < Tile.images.length && Tile.images[0+5*buildPage] != null) gc.drawImage(Tile.images[0+5*buildPage], 693, 55, 32*(Tile.images[0+5*buildPage].getWidth()/Tile.images[0+5*buildPage].getHeight()), 32);
				gc.setFill(selectedTile.isPlaceAvailable(1+5*buildPage, this.model) ? Color.YELLOW : Color.web("#DD3E3E"));
				gc.fillRect(685, 120, 80, 80);
				if (1+5*buildPage < Tile.images.length && Tile.images[1+5*buildPage] != null) gc.drawImage(Tile.images[1+5*buildPage], 693, 145, 32*(Tile.images[1+5*buildPage].getWidth()/Tile.images[1+5*buildPage].getHeight()), 32);
				gc.setFill(selectedTile.isPlaceAvailable(2+5*buildPage, this.model) ? Color.YELLOW : Color.web("#DD3E3E"));
				gc.fillRect(685, 210, 80, 80);
				if (2+5*buildPage < Tile.images.length && Tile.images[2+5*buildPage] != null) gc.drawImage(Tile.images[2+5*buildPage], 693, 235, 32*(Tile.images[2+5*buildPage].getWidth()/Tile.images[2+5*buildPage].getHeight()), 32);
				gc.setFill(selectedTile.isPlaceAvailable(3+5*buildPage, this.model) ? Color.YELLOW : Color.web("#DD3E3E"));
				gc.fillRect(685, 300, 80, 80);
				if (3+5*buildPage < Tile.images.length && Tile.images[3+5*buildPage] != null) gc.drawImage(Tile.images[3+5*buildPage], 693, 325, 32*(Tile.images[3+5*buildPage].getWidth()/Tile.images[3+5*buildPage].getHeight()), 32);
				gc.setFill(selectedTile.isPlaceAvailable(4+5*buildPage, this.model) ? Color.YELLOW : Color.web("#DD3E3E"));
				gc.fillRect(685, 390, 80, 80);
				if (4+5*buildPage < Tile.images.length && Tile.images[4+5*buildPage] != null) gc.drawImage(Tile.images[4+5*buildPage], 693, 415, 32*(Tile.images[4+5*buildPage].getWidth()/Tile.images[4+5*buildPage].getHeight()), 32);
				gc.drawImage(PAGE_PREVIOUS_IMAGE, 605, 20, 30, 30);
				gc.drawImage(PAGE_NEXT_IMAGE, 640, 20, 30, 30);
				gc.setFill(Color.BLUE);
				gc.setTextAlign(TextAlignment.CENTER);
				gc.fillText(this.atlas.getImageById(0+5*buildPage) != null ? this.atlas.getImageById(0+5*buildPage).getString("name") : "Unknown", 725, 105);
				gc.fillText(this.atlas.getImageById(1+5*buildPage) != null ? this.atlas.getImageById(1+5*buildPage).getString("name") : "Unknown", 725, 195);
				gc.fillText(this.atlas.getImageById(2+5*buildPage) != null ? this.atlas.getImageById(2+5*buildPage).getString("name") : "Unknown", 725, 285);
				gc.fillText(this.atlas.getImageById(3+5*buildPage) != null ? this.atlas.getImageById(3+5*buildPage).getString("name") : "Unknown", 725, 375);
				gc.fillText(this.atlas.getImageById(4+5*buildPage) != null ? this.atlas.getImageById(4+5*buildPage).getString("name") : "Unknown", 725, 465);
				gc.fillRect(690, 35, 10, 10);
				gc.fillRect(690, 125, 10, 10);
				gc.fillRect(690, 215, 10, 10);
				gc.fillRect(690, 305, 10, 10);
				gc.fillRect(690, 395, 10, 10);
				gc.restore();
			}
			gc.setFill(Color.WHITE);
			gc.fillText(String.format("Page: %d\nSX: %.2f SY: %.2f\nFPS: %d", buildPage, sx, sy, fps), 25, 435);
			gc.setFill(Color.web("#FD9F59"));
			gc.fillRect(15, 10, 35, 35);
			gc.fillRect(55, 10, 35, 35);
			gc.fillRect(95, 10, 35, 35);
			gc.fillRect(135, 10, 35, 35);
			gc.fillRect(185, 10, 35, 35);
			gc.fillRect(225, 10, 35, 35);
			
			gc.fillRect(15, 70, 35, 35);
			gc.fillRect(55, 70, 35, 35);
			gc.fillRect(95, 70, 35, 35);
			gc.fillRect(135, 70, 35, 35);

			// User
			gc.setFill(Color.BLUE);
			gc.fillText("User: "+this.user.getUsername(), 15, 60);
		});
		
		if (showingInfo >= 0){
			doOnNoScale(gc, () -> {
				gc.save();
				gc.setGlobalAlpha(0.7);
				gc.setFill(Color.BLACK);
				gc.fillRect(0, 0, 800, 500);
				gc.restore();
				gc.setFill(Color.WHITE);
				gc.fillRect(250, 150, 300, 200);
				gc.drawImage(BUTTON_CLOSE_IMAGE, 520, 150, 30, 30);
				if (showingInfo < Tile.images.length && showingInfo > 1){
					gc.drawImage(Tile.images[showingInfo], 265, 173, 64, 64*Tile.images[showingInfo].getHeight()/Tile.images[showingInfo].getWidth());
					JSONObject object = this.atlas.getImageById(showingInfo);
					gc.setFill(Color.BLACK);
					gc.fillText(object.getString("name"), 355, 190);
					gc.fillText(String.format("Width: %d tiles\nHeight: %d tiles\nObject ID: %d", object.getInt("width"), object.getInt("height"), object.getInt("id")), 265, 260);
					if (!selectedTile.isPlaceAvailable(showingInfo, this.model)){
						gc.setFill(Color.RED);
						gc.fillText("This object can't be placed", 265, 340);
					}
				} else {
					gc.setFill(Color.RED);
					gc.fillText("Unknown object", 320, 235);
				}
			});
		}
		
		if (selectedTile != null && selectedTile.getType() > 1 && this.atlas.getImageById(selectedTile.getType()).optBoolean("openMenu")){
			doOnNoScale(gc, () -> {
				gc.save();
				gc.setGlobalAlpha(0.6);
				gc.setFill(Color.BLACK);
				gc.fillRect(250, 375, 110, 100);
				gc.setGlobalAlpha(0.7);
				gc.setFill(Color.YELLOW);
				gc.fillRect(260, 385, 90, 30);
				gc.restore();
			});
		}
		
		if (openMenu){
			doOnNoScale(gc, () -> {
				gc.save();
				gc.setGlobalAlpha(0.7);
				gc.setFill(Color.BLACK);
				gc.fillRect(0, 0, 800, 500);
				gc.setGlobalAlpha(0.8);
				gc.setFill(Color.WHITE);
				gc.fillRect(200, 100, 400, 300);
				gc.drawImage(BUTTON_CLOSE_IMAGE, 570, 100, 30, 30);
				gc.restore();
				switch (selectedTile.getType()){
					case 4:
						break;
				}
			});
		}
		
		if (showInventory){
			doOnNoScale(gc, () -> {
				gc.save();
				gc.setGlobalAlpha(0.7);
				gc.setFill(Color.BLACK);
				gc.fillRect(0, 0, 800, 500);
				gc.setGlobalAlpha(0.8);
				gc.setFill(Color.WHITE);
				gc.fillRect(200, 100, 400, 300);
				gc.drawImage(BUTTON_CLOSE_IMAGE, 570, 100, 30, 30);
				gc.setLineWidth(4);
				gc.setStroke(Color.BLACK);
				gc.strokeLine(450, 100, 450, 400);
				gc.restore();
				int x = 0;
				int y = 0;
				for (Inventory.Item item : this.user.getInventory().getItems()){
					gc.setFill(selectedInventoryItem == x+4*y ? Color.web("#E87252") : Color.ORANGE);
					gc.fillRect(220+x*55-2, 120+y*65-2, 44, 44);
					gc.drawImage(itemImages[item.getId()], 220+x*55, 120+y*65);
					gc.setFill(Color.BLACK);
					gc.fillText("x"+item.getAmount(), 220+x*55, 175+y*65);
					if (x == 3){
						x = 0;
						y++;
					} else {
						x++;
					}
				}
				
				Inventory.Item selected = this.user.getInventory().getItems().get(selectedInventoryItem);
				gc.drawImage(itemImages[selected.getId()], 490, 150, 60, 60);
				gc.setFill(Color.BLACK);
				gc.setTextAlign(TextAlignment.CENTER);
				gc.fillText(this.atlas.getItemById(selected.getId()).getString("name"), 525, 230);
				gc.setTextAlign(TextAlignment.LEFT);
				gc.fillText(maxLength(this.atlas.getItemById(selected.getId()).getString("description"), 16)+"\n\nAmount: "+selected.getAmount(), 457, 265);
			});
		}
		frames++;
	}
	
	private static String maxLength(String string, int max){
		StringBuilder builder = new StringBuilder();
		
		int c = 0;
		for (String s : string.split(" ")){
			builder.append(s);
			if (c > 0 && c % 3 == 0){
				builder.append("\n");
			} else {
				builder.append(" ");
			}
			c++;
		}
		
		return builder.toString();
	}
}
