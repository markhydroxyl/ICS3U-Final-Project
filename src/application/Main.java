package application;
	
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;

public class Main extends Application {
	
	final int playerWidth = 50;
	final int screenWidth = 400;
	final int screenHeight = 800;
	
	@Override
	public void start(Stage primaryStage) {
		try {
			BorderPane root = new BorderPane();
			Canvas playerCanvas = new Canvas(playerWidth,playerWidth);
			GraphicsContext playerGC = playerCanvas.getGraphicsContext2D();
			drawPlayerRect(playerGC);
			playerCanvas.setTranslateX((screenWidth-(playerWidth))/2);
			playerCanvas.setTranslateY(screenHeight-(playerWidth));
			root.getChildren().add(playerCanvas);
			Scene scene = new Scene(root,screenWidth,screenHeight);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.show();
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void drawPlayerRect(GraphicsContext gc) {
		gc.setFill(Color.BLACK);
		gc.fillRect(0, 0, playerWidth, playerWidth);
	}
	
	//Player square starts on bottom line, in the middle
	//
	
	public static void main(String[] args) {
		launch(args);
	}
}
