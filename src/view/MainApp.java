package view;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import controller.GameEngine;
import controller.Market;
import model.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MainApp extends Application {
    private GameEngine engine;
    private Map gameMap;
    private Market market;
    private BoardCanvas boardCanvas;
    private PlayerInfoPane playerInfoPane;
    private MarketPane marketPane;
    private ActionPane actionPane;
    private DicePane dicePane;

    @Override
    public void start(Stage primaryStage) {
        // Ask for player count
        int playerCount = askPlayerCount();
        if (playerCount < 2 || playerCount > 4) {
            playerCount = 2;
        }

        initializeGame(playerCount);

        BorderPane root = new BorderPane();

        // Create UI components
        boardCanvas = new BoardCanvas(gameMap, this);
        playerInfoPane = new PlayerInfoPane(engine, this);
        marketPane = new MarketPane(market, engine, this);
        actionPane = new ActionPane(engine, gameMap, this);
        dicePane = new DicePane(engine, this);

        // Layout
        root.setCenter(boardCanvas);
        root.setRight(playerInfoPane);
        root.setBottom(marketPane);
        root.setLeft(actionPane);
        root.setTop(dicePane);

        Scene scene = new Scene(root, 1200, 800);

        primaryStage.setTitle("Silicon Valley Catan - " + playerCount + " Players");
        primaryStage.setScene(scene);
        primaryStage.show();

        updateUI();
    }

    private int askPlayerCount() {
        ChoiceDialog<Integer> dialog = new ChoiceDialog<>(2, 2, 3, 4);
        dialog.setTitle("Silicon Valley Catan");
        dialog.setHeaderText("Welcome to Silicon Valley Catan!");
        dialog.setContentText("Select number of players:");

        Optional<Integer> result = dialog.showAndWait();
        return result.orElse(2);
    }

    private void initializeGame(int playerCount) {
        gameMap = new Map();
        market = new Market();

        String[] colors = {"Red", "Blue", "Green", "Yellow"};

        List<Player> players = new ArrayList<>();
        for (int i = 0; i < playerCount; i++) {
            Player player = new Player("Player " + (i + 1), colors[i]);
            // Role will be chosen during setup phase
            players.add(player);
        }

        engine = new GameEngine(players, market);
    }

    public void updateUI() {
        if (boardCanvas != null) boardCanvas.redraw();
        if (playerInfoPane != null) playerInfoPane.update();
        if (marketPane != null) marketPane.update();
        if (actionPane != null) actionPane.update();
        if (dicePane != null) dicePane.update();
    }

    public GameEngine getEngine() { return engine; }
    public Map getGameMap() { return gameMap; }
    public Market getMarket() { return market; }
    public DicePane getDicePane() { return dicePane; }
    public BoardCanvas getBoardCanvas() { return boardCanvas; }
    public ActionPane getActionPane() { return actionPane; }

    public static void main(String[] args) {
        launch(args);
    }
}
