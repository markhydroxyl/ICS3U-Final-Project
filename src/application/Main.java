package application;
	
import java.util.ArrayList;
import java.util.Arrays;
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
	//Constants used in code
	static final int PLAYER_WIDTH = 25;
	static final int SCREEN_WIDTH = 400;
	static final int SCREEN_HEIGHT = 400;
	static final int BLOCK_WIDTH = 60;
	static final int BLOCK_HEIGHT = 20;
	static final int BTN_ALIGN_LEFT = 10;
	static final int BTN_HEIGHT = 80;
	static final int BTN_ALIGN_TOP = 80;
	static final double START_BRICK_SPEED = 1.5;
	static final int START_DIFFICULTY = 80;
	static int score = 0;
	double brickSpeed = START_BRICK_SPEED;
	int difficulty = START_DIFFICULTY;
	
	private static JSONArray scoreData = new JSONArray();
	Pane root = new Pane();
	Scene scene = new Scene(root,SCREEN_WIDTH,SCREEN_HEIGHT);
	Text scoreDisplay;
	Canvas playerCanvas;
	
	//Handler for left and right keys to control player movement
	final EventHandler<KeyEvent> keyBtnDownEvent = new EventHandler<KeyEvent>() {
		@Override
		public void handle(KeyEvent event) {
			double playerX = playerCanvas.getTranslateX();
			if(event.getCode() == KeyCode.LEFT && playerX > 0) {
				playerCanvas.setTranslateX(playerX-2);
			}
			if (event.getCode() == KeyCode.RIGHT && playerX < SCREEN_WIDTH-PLAYER_WIDTH) {
				playerCanvas.setTranslateX(playerX+2);
			}
			event.consume();
		}
	};
	ArrayList<Canvas> blocks = new ArrayList<Canvas>();
	
	//Called at the start of the program by JavaFX
	@Override
	public void start(Stage primaryStage) {
		try {
			getScores();
			callStart(primaryStage);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//Called at the start of the program and when "return to main menu" is clicked
	//Gives the user options for which of three actions to take: play the game, view the high scores, or exit
	public void callStart(Stage primaryStage) {
		//Initializing and setting properties of start button
		Button start = new Button("Start game");
		start.setDefaultButton(true);
		start.setMinHeight(BTN_HEIGHT);
		start.setMinWidth(SCREEN_WIDTH-2*BTN_ALIGN_LEFT);
		start.setTranslateX(BTN_ALIGN_LEFT);
		start.setTranslateY(BTN_ALIGN_TOP);
		start.setTranslateZ(0);
		
		//Initializing and setting properties of high score button
		Button highScore = new Button("High scores");
		highScore.setMinHeight(BTN_HEIGHT);
		highScore.setMinWidth(SCREEN_WIDTH-2*BTN_ALIGN_LEFT);
		highScore.setTranslateX(BTN_ALIGN_LEFT);
		highScore.setTranslateY(BTN_ALIGN_TOP+BTN_HEIGHT);
		highScore.setTranslateZ(0);
		
		//Initializing and setting properties of quit button
		Button quit = new Button("Exit");
		quit.setMinHeight(BTN_HEIGHT);
		quit.setMinWidth(SCREEN_WIDTH-2*BTN_ALIGN_LEFT);
		quit.setTranslateX(BTN_ALIGN_LEFT);
		quit.setTranslateY(BTN_ALIGN_TOP+2*BTN_HEIGHT);
		quit.setTranslateZ(0);
		quit.setCancelButton(true);
		
		//Title text
		Text title = new Text("The Game of Falling Bricks");
		title.setFont(Font.font("Arial", FontWeight.BOLD, 30));
		title.setTranslateX(5);
		title.setTranslateY(50);
		
		//Placing nodes into root pane
		root.getChildren().add(start);
		root.getChildren().add(highScore);
		root.getChildren().add(quit);
		root.getChildren().add(title);
		scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		primaryStage.setScene(scene);
		primaryStage.show();
		
		//EventHandlers for buttons
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
				callScores(primaryStage);
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
	
	//Called immediately after death
	//Prompts the user for a name to submit a score with
	public void callDeadState(Stage primaryStage) {
		//Text at the top
		Text text = new Text("You died! Your score was: " + score);
		text.setTranslateX(SCREEN_WIDTH/2-70);
		text.setTranslateY(30);
		
		//Input field for user
		TextField userNameInput = new TextField();
		userNameInput.setTranslateX(BTN_ALIGN_LEFT);
		userNameInput.setTranslateY(BTN_ALIGN_TOP);
		userNameInput.setMinHeight(BTN_HEIGHT);
		userNameInput.setMaxHeight(BTN_HEIGHT);
		userNameInput.setMinWidth(SCREEN_WIDTH-2*BTN_ALIGN_LEFT);
		
		//Instructions text
		Text instructions = new Text("Press enter to submit");
		instructions.setTranslateX(SCREEN_WIDTH/2-60);
		instructions.setTranslateY(BTN_ALIGN_TOP+BTN_HEIGHT+25);
		
		//Placing nodes into root pane
		root.getChildren().add(text);
		root.getChildren().add(instructions);
		root.getChildren().add(userNameInput);
		
		//EventHandler for enter key
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
	
	//Called when high scores are brought up
	//Sorts existing scores and displays them
	public void callScores(Stage primaryStage) {
		//Ensure that scores are up to date
		getScores();
		
		//Sorting scores
		int[] scoreArray = new int[scoreData.size()];
		boolean[] checked = new boolean[scoreData.size()];
		for (int i=0;i<scoreData.size();i++) {
			Object a = scoreData.get(i);
			String scoreString = a.toString();
			scoreArray[i] = Integer.parseInt(scoreString.substring(9, scoreString.indexOf(',')));
			checked[i] = false;
		}
		Arrays.sort(scoreArray);
		
		//Sorting scores, printing out scores
		for (int j=scoreArray.length-1; j>=0; j--) {
			Object[] scorePairs = new Object[scoreData.size()];
			for (int i=0; i<scoreData.size(); i++) {
				scorePairs[i] = scoreData.get(i);
			}
			for (int i=0; i<scoreData.size(); i++) {
				String scoreString = scorePairs[i].toString();
				int nextScore = Integer.parseInt(scoreString.substring(9, scoreString.indexOf(',')));
				if (nextScore==scoreArray[j] && !checked[i]) {
					String name = scoreString.substring(scoreString.indexOf(':', scoreString.indexOf(':')+1)+2, scoreString.indexOf('}')-1);
					Text scoreText = new Text(name+": "+nextScore);
					scoreText.setTranslateX(BTN_ALIGN_LEFT);
					scoreText.setTranslateY((scoreData.size()-j)*22+65);
					scoreText.setFont(Font.font("Arial", 20));
					root.getChildren().add(scoreText);
					checked[i] = true;
					break;
				}
			}
		}
		
		//Creating return to main button and setting properties
		Button switchToMain = new Button("Return to main screen");
		switchToMain.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				root.getChildren().clear();
				callStart(primaryStage);
			}
		});
		switchToMain.setTranslateX(BTN_ALIGN_LEFT);
		switchToMain.setTranslateY(SCREEN_HEIGHT-BTN_HEIGHT-10);
		switchToMain.setMinHeight(BTN_HEIGHT);
		switchToMain.setMinWidth(SCREEN_WIDTH-2*BTN_ALIGN_LEFT);
		
		//Title text
		Text title = new Text("High Scores!");
		title.setFont(Font.font("Arial", FontWeight.BOLD, 30));
		title.setTranslateX(110);
		title.setTranslateY(50);
		
		root.getChildren().addAll(title, switchToMain);
	}
	
	//Called when game button is clicked
	//Creates a square player, and randomly spawns falling bricks
	public void callGame(Stage primaryStage) {
		//Initialize score and difficulty to default
		score = 0;
		brickSpeed = START_BRICK_SPEED;
		difficulty = START_DIFFICULTY;
		
		//Creates a player sprite
		playerCanvas = new Canvas(PLAYER_WIDTH,PLAYER_WIDTH);
		GraphicsContext playerGC = playerCanvas.getGraphicsContext2D();
		playerGC.setFill(Color.RED);
		playerGC.fillRect(0, 0, PLAYER_WIDTH, PLAYER_WIDTH);
		playerCanvas.setTranslateX((SCREEN_WIDTH-(PLAYER_WIDTH))/2);
		playerCanvas.setTranslateY(SCREEN_HEIGHT-(PLAYER_WIDTH));
		
		//Setup for scene
		root.getChildren().add(playerCanvas);
		scene.addEventHandler(KeyEvent.KEY_PRESSED, keyBtnDownEvent);
		scene.setOnKeyPressed(keyBtnDownEvent);
		
		//AnimationTimer used to periodically spawn blocks and check for collision, gradually increasing difficulty
		new AnimationTimer() {
			@Override
			public void handle(long time) {
				for(Canvas blockCanvas:blocks) {
					blockCanvas.setTranslateY(blockCanvas.getTranslateY()+brickSpeed);
					boolean contacting = false;
					if ((blockCanvas.getTranslateX() < playerCanvas.getTranslateX()+PLAYER_WIDTH
							&& blockCanvas.getTranslateX() + BLOCK_WIDTH > playerCanvas.getTranslateX())
							&& (blockCanvas.getTranslateY() > SCREEN_HEIGHT-PLAYER_WIDTH)) {
						contacting = true;
					}
					if (blockCanvas.getTranslateY() >= SCREEN_HEIGHT) {
						scoreUpdate();
						blocks.remove(blockCanvas);
					} else if (contacting) {
						root.getChildren().clear();
						blocks.clear();
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

	//Called when a block reaches the ground, i.e. successfully dodged
	//Increments score
	public void scoreUpdate() {
		score++;
	}
	
	//Called by AnimationTimer in callGame
	//Creates a block and adds it to the root pane
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
	
	//Gets the scores from the JSON Object
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
	
	//Submits the latest score with the user-provided name
	@SuppressWarnings("unchecked")
	private static void submitScore(String name) {
		try {
			FileWriter writer = new FileWriter("src/application/scores.json");
			JSONObject newScore = new JSONObject();
			newScore.put("name", name);
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
