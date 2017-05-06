package application;
	
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;


public class Main extends Application {
	@Override
	public void start(Stage primaryStage) {
		try {
			BorderPane root = new BorderPane();
			Canvas playerCanvas = new Canvas(10,10);
			playerCanvas.setTranslateX(195);
			playerCanvas.setTranslateY(390);
			GraphicsContext playerGC = playerCanvas.getGraphicsContext2D();
			drawPlayerRect(playerGC);
			root.getChildren().add(playerCanvas);
			Scene scene = new Scene(root,400,400);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void drawPlayerRect(GraphicsContext gc) {
		gc.setFill(Color.BLACK);
		gc.setStroke(Color.RED);
		gc.setLineWidth(5);
		gc.strokeLine(0, 0, 0, 10);
		gc.strokeLine(0, 10, 10, 10);
		gc.strokeLine(10, 10, 10, 0);
		gc.strokeLine(10, 0, 0, 0);
		gc.fillRect(0, 0, 10, 10);
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
