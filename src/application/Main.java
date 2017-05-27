package application;
	
import java.util.ArrayList;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.io.*;
import org.json.simple.parser.*;

public class Main extends Application {
	
	private static JSONArray scoreData = new JSONArray();
	static final int PLAYER_WIDTH = 25;
	static final int SCREEN_WIDTH = 400;
	static final int SCREEN_HEIGHT = 400;
	static final int BLOCK_WIDTH = 60;
	static final int BLOCK_HEIGHT = 20;
	static final int BTN_ALIGN_LEFT = 10;
	static final int BTN_HEIGHT = 80;
	static final int BTN_ALIGN_TOP = 80;
	double brickSpeed = 1.5;
	Pane root = new Pane();
	Scene scene = new Scene(root,SCREEN_WIDTH,SCREEN_HEIGHT);
	int difficulty = 80;
	static int score = 0;
	Text scoreDisplay;
	boolean moveRight = false;
	boolean moveLeft = false;
	Canvas playerCanvas;
	//Handler for left and right keys to control player movement
	final EventHandler<KeyEvent> keyBtnDownEvent = new EventHandler<KeyEvent>() {
		@Override
		public void handle(KeyEvent event) {
			double playerX = playerCanvas.getTranslateX();
//			System.out.println(playerX);
			if(event.getCode() == KeyCode.LEFT && playerX > 0) {
				playerCanvas.setTranslateX(playerX-2);
//				System.out.println("Moving left");
			}
			if (event.getCode() == KeyCode.RIGHT && playerX < SCREEN_WIDTH-PLAYER_WIDTH) {
				playerCanvas.setTranslateX(playerX+2);
//				System.out.println("Moving right");
			}
			event.consume();
		}
	};
	ArrayList<Canvas> blocks = new ArrayList<Canvas>();
	
	@Override
	public void start(Stage primaryStage) {
		try {
			getScores();
			test();
			callStart(primaryStage);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void callStart(Stage primaryStage) {
		Canvas background = new Canvas(SCREEN_WIDTH, SCREEN_HEIGHT);
		GraphicsContext bgGC = background.getGraphicsContext2D();
		bgGC.setFill(Color.BLUEVIOLET);
		bgGC.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
		background.setTranslateX(0);
		background.setTranslateY(0);
		background.setTranslateZ(0);
		Button start = new Button("Start game");
		Button highScore = new Button("High scores");
		Button quit = new Button("Exit");
		start.setDefaultButton(true);
		quit.setCancelButton(true);
		start.setMinHeight(BTN_HEIGHT);
		start.setMinWidth(SCREEN_WIDTH-2*BTN_ALIGN_LEFT);
		start.setTranslateX(BTN_ALIGN_LEFT);
		start.setTranslateY(BTN_ALIGN_TOP);
		start.setTranslateZ(0);
		highScore.setMinHeight(BTN_HEIGHT);
		highScore.setMinWidth(SCREEN_WIDTH-2*BTN_ALIGN_LEFT);
		highScore.setTranslateX(BTN_ALIGN_LEFT);
		highScore.setTranslateY(BTN_ALIGN_TOP+BTN_HEIGHT);
		highScore.setTranslateZ(0);
		quit.setMinHeight(BTN_HEIGHT);
		quit.setMinWidth(SCREEN_WIDTH-2*BTN_ALIGN_LEFT);
		quit.setTranslateX(BTN_ALIGN_LEFT);
		quit.setTranslateY(BTN_ALIGN_TOP+2*BTN_HEIGHT);
		quit.setTranslateZ(0);
		Text title = new Text("The Game of Falling Bricks");
		title.setFont(Font.font("Arial", FontWeight.BOLD, 30));
		title.setTranslateX(5);
		title.setTranslateY(50);
		root.getChildren().add(start);
		root.getChildren().add(highScore);
		root.getChildren().add(quit);
		root.getChildren().add(title);
//		Scene scene = new Scene(root, screenWidth, screenHeight);
		scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		primaryStage.setScene(scene);
		primaryStage.show();
		
		EventHandler<ActionEvent> startClicked = new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				root.getChildren().clear();
				callGame(primaryStage);
			}
		};
		start.setOnAction(startClicked);
		EventHandler<ActionEvent> highScoreClicked = new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				root.getChildren().clear();
				callDeadState(primaryStage);
			}
		};
		highScore.setOnAction(highScoreClicked);
		EventHandler<ActionEvent> quitClicked = new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				primaryStage.close();
			}
		};
		quit.setOnAction(quitClicked);
	}
	
	public void callDeadState(Stage primaryStage) {
		Text text = new Text("You died! Your score was: " + score);
		text.setTranslateX(SCREEN_WIDTH/2-text.getBaselineOffset()/2);
		text.setTranslateY(30);
		TextField userNameInput = new TextField("Please put your name in");
		userNameInput.setTranslateX(BTN_ALIGN_LEFT);
		userNameInput.setTranslateY(BTN_ALIGN_TOP);
		userNameInput.setMinHeight(BTN_HEIGHT);
		Text instructions = new Text("Press enter to submit");
		root.getChildren().add(text);
		root.getChildren().add(instructions);
		root.getChildren().add(userNameInput);
		
		userNameInput.setOnKeyPressed(new EventHandler<KeyEvent>() {
		    @Override
		    public void handle(KeyEvent keyEvent) {
		        if (keyEvent.getCode() == KeyCode.ENTER)  {
		        	submitScore(userNameInput.getText());
					root.getChildren().clear();
					callScores(primaryStage);
		        }
		    }
		});
	}
	
	public void test() {
		for (int i=0;i<scoreData.size();i++) {
			Object a = scoreData.get(i);
			String scoreString = a.toString();
			String tempScore = scoreString.substring(9, scoreString.indexOf(','));
			String name = scoreString.substring(scoreString.indexOf(':', scoreString.indexOf(':')+1)+2, scoreString.indexOf('}')-1);
			System.out.println(name+": "+tempScore);
		}
	}
	
	public void callScores(Stage primaryStage) {
		getScores();
		
		Button switchToMain = new Button("Return to main screen");
		EventHandler<ActionEvent> switchClicked = new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				root.getChildren().clear();
				callStart(primaryStage);
			}
		};
		switchToMain.setOnAction(switchClicked);
		switchToMain.setTranslateX(BTN_ALIGN_LEFT);
		switchToMain.setTranslateY(SCREEN_HEIGHT-BTN_HEIGHT-10);
		switchToMain.setMinHeight(BTN_HEIGHT);
		switchToMain.setMinWidth(SCREEN_WIDTH-2*BTN_ALIGN_LEFT);
		Text title = new Text("High Scores!");
		title.setFont(Font.font("Arial", FontWeight.BOLD, 30));
		title.setTranslateX(5);
		title.setTranslateY(50);
		root.getChildren().add(switchToMain);
		root.getChildren().add(title);
	}
	
	public void callGame(Stage primaryStage) {
		score = 0;
		playerCanvas = new Canvas(PLAYER_WIDTH,PLAYER_WIDTH);
		GraphicsContext playerGC = playerCanvas.getGraphicsContext2D();
		playerGC.setFill(Color.RED);
		playerGC.fillRect(0, 0, PLAYER_WIDTH, PLAYER_WIDTH);
		Text scoreFillerText = new Text("Score: ");
		Text curScore = new Text(String.valueOf(score));
		scoreFillerText.setTranslateX(10);
		scoreFillerText.setTranslateY(10);
		curScore.setTranslateX(80);
		curScore.setTranslateY(10);
		playerCanvas.setTranslateX((SCREEN_WIDTH-(PLAYER_WIDTH))/2);
		playerCanvas.setTranslateY(SCREEN_HEIGHT-(PLAYER_WIDTH));
		root.getChildren().add(playerCanvas);
		root.getChildren().add(scoreFillerText);
//		primaryStage.show();
		
		scene.addEventHandler(KeyEvent.KEY_PRESSED, keyBtnDownEvent);
		scene.setOnKeyPressed(keyBtnDownEvent);
		
		new AnimationTimer() {
			@Override
			public void handle(long time) {
				for(Canvas blockCanvas:blocks) {
					blockCanvas.setTranslateY(blockCanvas.getTranslateY()+brickSpeed);
					boolean contacting = false;
					if ((blockCanvas.getTranslateX() < playerCanvas.getTranslateX()+PLAYER_WIDTH && blockCanvas.getTranslateX() + BLOCK_WIDTH > playerCanvas.getTranslateX()) && (blockCanvas.getTranslateY() > SCREEN_HEIGHT-PLAYER_WIDTH)) {
						contacting = true;
					}
					if (blockCanvas.getTranslateY() >= SCREEN_HEIGHT) {
						scoreUpdate();
						blocks.remove(blockCanvas);
					} else if (contacting) {
						root.getChildren().clear();
						callDeadState(primaryStage);
						stop();
					}
				}
				if (time%100 == 0) {
					if (difficulty>0)
						difficulty--;
					brickSpeed += 0.05;
				}
				if (time%difficulty == 0) {
					spawnBlock(root);
				}
			}
		}.start();
	}
	
	public void scoreUpdate() {
		score++;
	}
	
	public void spawnBlock(Pane root2) {
		double xCoor = Math.random()*(SCREEN_WIDTH-BLOCK_WIDTH);
		Canvas blockCanvas = new Canvas(BLOCK_WIDTH, BLOCK_HEIGHT);
		GraphicsContext blockGC = blockCanvas.getGraphicsContext2D();
		blockGC.setFill(Color.BLACK);
		blockGC.fillRect(0, 0, BLOCK_WIDTH, BLOCK_HEIGHT);
		root2.getChildren().add(blockCanvas);
		blockCanvas.setTranslateX(xCoor);
		blockCanvas.setTranslateY(0);
		blocks.add(blockCanvas);
	}
	
	private static void getScores() {
		try {
			FileReader reader = new FileReader("src/application/scores.json");
			JSONParser parser = new JSONParser();
			scoreData = (JSONArray) parser.parse(reader);
			reader.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	private static void submitScore(String name) {
		try {
			FileWriter writer = new FileWriter("src/application/scores.json");
			JSONObject newScore = new JSONObject();
			newScore.put(name, "test");
			newScore.put("score", score);
			
			scoreData.add(newScore);
			writer.write(scoreData.toJSONString());
			writer.flush();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
