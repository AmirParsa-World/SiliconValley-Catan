package view;

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import controller.GameEngine;
import controller.GamePhase;
import controller.Market;
import model.*;
import util.SaveManager;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.util.Duration;

public class MainApp extends Application {
    private GameEngine engine;
    private model.Map gameMap;
    private Market market;
    private BoardCanvas boardCanvas;
    private PlayerInfoPane playerInfoPane;
    private MarketPane marketPane;
    private ActionPane actionPane;
    private DicePane dicePane;
    private BorderPane root;
    private Stage primaryStage;

    private static final double BOT_DELAY_SECONDS = 0.8;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        root = new BorderPane();

        MenuBar menuBar = createMenuBar();
        root.setTop(menuBar);

        int playerCount = askPlayerCount();
        if (playerCount < 2 || playerCount > 4) {
            playerCount = 2;
        }

        boolean[] isBotArray = askPlayerTypes(playerCount);
        initializeGame(playerCount, isBotArray);
        buildGameUI();

        Scene scene = new Scene(root, 1200, 800);

        primaryStage.setTitle("Silicon Valley Catan - " + playerCount + " Players");
        primaryStage.setScene(scene);
        primaryStage.show();

        updateUI();

        PauseTransition startupDelay = new PauseTransition(Duration.seconds(0.3));
        startupDelay.setOnFinished(e -> checkAndRunBotTurn());
        startupDelay.play();
    }

    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();

        Menu fileMenu = new Menu("File");

        MenuItem saveItem = new MenuItem("Save Game");
        saveItem.setOnAction(e -> saveGame());

        MenuItem loadItem = new MenuItem("Load Game");
        loadItem.setOnAction(e -> loadGame());

        MenuItem newGameItem = new MenuItem("New Game");
        newGameItem.setOnAction(e -> startNewGame());

        fileMenu.getItems().addAll(saveItem, loadItem, new SeparatorMenuItem(), newGameItem);
        menuBar.getMenus().add(fileMenu);

        return menuBar;
    }

    private void saveGame() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Game");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Catan Save Files", "*.catan"));
        fileChooser.setInitialFileName("save.catan");
        File file = fileChooser.showSaveDialog(primaryStage);
        if (file != null) {
            try {
                SaveManager.saveGame(file.getAbsolutePath(), engine);
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Game Saved");
                alert.setHeaderText(null);
                alert.setContentText("Game saved successfully!");
                alert.showAndWait();
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Save Failed");
                alert.setHeaderText(null);
                alert.setContentText("Failed to save game: " + e.getMessage());
                alert.showAndWait();
            }
        }
    }

    private void loadGame() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Game");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Catan Save Files", "*.catan"));
        File file = fileChooser.showOpenDialog(primaryStage);
        if (file != null) {
            SaveManager.loadGame(file.getAbsolutePath(), new SaveManager.LoadGameCallback() {
                @Override
                public void onSuccess(GameEngine loadedEngine) {
                    loadGameState(loadedEngine);
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Game Loaded");
                    alert.setHeaderText(null);
                    alert.setContentText("Game loaded successfully! It is " + engine.getCurrentPlayer().getName() + "'s turn.");
                    alert.showAndWait();
                }

                @Override
                public void onFailure(String errorMessage) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Load Failed");
                    alert.setHeaderText(null);
                    alert.setContentText(errorMessage);
                    alert.showAndWait();
                }
            });
        }
    }

    private void loadGameState(GameEngine loadedEngine) {
        this.engine = loadedEngine;
        this.gameMap = loadedEngine.getGameMap();
        this.market = getMarketFromEngine(loadedEngine);

        buildGameUI();
        updateUI();

        PauseTransition delay = new PauseTransition(Duration.seconds(0.3));
        delay.setOnFinished(e -> checkAndRunBotTurn());
        delay.play();
    }

    private Market getMarketFromEngine(GameEngine engine) {
        return engine.getMarket();
    }

    private void buildGameUI() {
        boardCanvas = new BoardCanvas(gameMap, this);
        playerInfoPane = new PlayerInfoPane(engine, this);
        marketPane = new MarketPane(market, engine, this);
        actionPane = new ActionPane(engine, gameMap, this);
        dicePane = new DicePane(engine, this);

        root.setCenter(boardCanvas);
        root.setRight(playerInfoPane);
        root.setBottom(marketPane);
        root.setLeft(actionPane);
    }

    private void startNewGame() {
        int playerCount = askPlayerCount();
        if (playerCount < 2 || playerCount > 4) {
            playerCount = 2;
        }

        boolean[] isBotArray = askPlayerTypes(playerCount);
        initializeGame(playerCount, isBotArray);

        buildGameUI();
        updateUI();

        PauseTransition startupDelay = new PauseTransition(Duration.seconds(0.3));
        startupDelay.setOnFinished(e -> checkAndRunBotTurn());
        startupDelay.play();
    }

    private int askPlayerCount() {
        ChoiceDialog<Integer> dialog = new ChoiceDialog<>(2, 2, 3, 4);
        dialog.setTitle("Silicon Valley Catan");
        dialog.setHeaderText("Welcome to Silicon Valley Catan!");
        dialog.setContentText("Select number of players:");

        Optional<Integer> result = dialog.showAndWait();
        return result.orElse(2);
    }

    private boolean[] askPlayerTypes(int playerCount) {
        String[] colors = {"Red", "Blue", "Green", "Yellow"};
        boolean[] isBot = new boolean[playerCount];

        for (int i = 0; i < playerCount; i++) {
            ChoiceDialog<String> dialog = new ChoiceDialog<>("Human", "Human", "Bot");
            dialog.setTitle("Player " + (i + 1) + " Type");
            dialog.setHeaderText("Player " + (i + 1) + " (" + colors[i] + "):");
            dialog.setContentText("Is this player Human or Bot?");

            Optional<String> result = dialog.showAndWait();
            isBot[i] = result.orElse("Human").equals("Bot");
        }

        return isBot;
    }

    private void initializeGame(int playerCount, boolean[] isBotArray) {
        gameMap = new model.Map();
        market = new Market();

        String[] colors = {"Red", "Blue", "Green", "Yellow"};

        List<Player> players = new ArrayList<>();
        for (int i = 0; i < playerCount; i++) {
            String name = isBotArray[i] ? "Bot " + (i + 1) : "Player " + (i + 1);
            Player player = isBotArray[i]
                ? new SimpleBot(name, colors[i])
                : new Player(name, colors[i]);
            players.add(player);
        }

        engine = new GameEngine(players, market, gameMap);
    }

    // Handle discard flow: shows dialog for humans, auto-discards for bots
    // Then handles auditor movement if it was a 7 roll
    // Then calls onDone when all complete
    public void handleDiscardFlow(java.util.Map<Player, Integer> discardMap, Runnable onDone) {
        if (discardMap == null || discardMap.isEmpty()) {
            onDone.run();
            return;
        }

        List<Player> affectedPlayers = new ArrayList<>(discardMap.keySet());
        processNextDiscard(discardMap, affectedPlayers, 0, () -> {
            // After all discards, handle auditor movement
            handleAuditorMovement(onDone);
        });
    }

    private void handleAuditorMovement(Runnable onDone) {
        Player current = engine.getCurrentPlayer();

        if (current instanceof SimpleBot) {
            // Bot auto-moves auditor to a random valid sector
            actionPane.updateStatus(current.getName() + " (BOT)\nmoving Auditor...");
            updateUI();

            PauseTransition delay = new PauseTransition(Duration.seconds(0.5));
            delay.setOnFinished(e -> {
                autoMoveAuditor((SimpleBot) current);
                updateUI();
                PauseTransition afterDelay = new PauseTransition(Duration.seconds(0.5));
                afterDelay.setOnFinished(e2 -> onDone.run());
                afterDelay.play();
            });
            delay.play();
        } else {
            // Human clicks a sector to move the auditor
            actionPane.updateStatus(current.getName() + ",\nclick a sector to\nmove the Auditor");
            updateUI();
            boardCanvas.enterMoveAuditorMode();

            waitForAuditorMove(onDone);
        }
    }

    private void waitForAuditorMove(Runnable onDone) {
        PauseTransition check = new PauseTransition(Duration.seconds(0.2));
        check.setOnFinished(e -> {
            if (boardCanvas.isAuditorMovePending()) {
                waitForAuditorMove(onDone);
            } else {
                onDone.run();
            }
        });
        check.play();
    }

    private void autoMoveAuditor(SimpleBot bot) {
        model.Sector[][] sectors = gameMap.getSectors();
        java.util.List<int[]> validTargets = new java.util.ArrayList<>();

        boolean anyHasStructures = false;
        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 5; c++) {
                model.Sector s = sectors[r][c];
                if (s != null && !s.isBlocked() && hasPlayersOnSector(s)) {
                    validTargets.add(new int[]{r, c});
                    anyHasStructures = true;
                }
            }
        }

        if (!anyHasStructures) {
            for (int r = 0; r < 5; r++) {
                for (int c = 0; c < 5; c++) {
                    if (sectors[r][c] != null && !sectors[r][c].isBlocked()) {
                        validTargets.add(new int[]{r, c});
                    }
                }
            }
        }

        if (!validTargets.isEmpty()) {
            int[] target = validTargets.get(new java.util.Random().nextInt(validTargets.size()));
            try {
                engine.moveAuditor(bot, target[0], target[1]);
            } catch (Exception e) {
                for (int[] t : validTargets) {
                    try {
                        engine.moveAuditor(bot, t[0], t[1]);
                        break;
                    } catch (Exception ignored) {}
                }
            }
        }
    }

    private boolean hasPlayersOnSector(model.Sector sector) {
        model.Vertex[] corners = {
            sector.getBottomLeft(), sector.getBottomRight(),
            sector.getTopLeft(), sector.getTopRight()
        };
        for (model.Vertex v : corners) {
            if (v != null && v.hasStructure()) return true;
        }
        return false;
    }

    private void processNextDiscard(java.util.Map<Player, Integer> discardMap,
                                     List<Player> affectedPlayers, int index, Runnable onDone) {
        if (index >= affectedPlayers.size()) {
            onDone.run();
            return;
        }

        Player player = affectedPlayers.get(index);
        int amount = discardMap.get(player);

        if (player instanceof SimpleBot) {
            player.discardRandomResources(amount);
            actionPane.updateStatus(player.getName() + " (BOT)\ndiscarded " + amount + " cards.");
            updateUI();

            PauseTransition delay = new PauseTransition(Duration.seconds(0.5));
            delay.setOnFinished(e -> processNextDiscard(discardMap, affectedPlayers, index + 1, onDone));
            delay.play();
        } else {
            actionPane.updateStatus(player.getName() + ",\nchoose cards to discard!");
            updateUI();

            PauseTransition showDelay = new PauseTransition(Duration.seconds(0.3));
            showDelay.setOnFinished(e -> {
                Platform.runLater(() -> {
                    DiscardDialog.show(player, amount);
                    actionPane.updateStatus(player.getName() + "\ndiscarded " + amount + " cards.");
                    updateUI();

                    PauseTransition nextDelay = new PauseTransition(Duration.seconds(0.5));
                    nextDelay.setOnFinished(e2 -> processNextDiscard(discardMap, affectedPlayers, index + 1, onDone));
                    nextDelay.play();
                });
            });
            showDelay.play();
        }
    }

    public void checkAndRunBotTurn() {
        Player current = engine.getCurrentPlayer();
        if (!(current instanceof SimpleBot)) return;

        if (engine.getCurrentPhase() == GamePhase.FINISHED) return;

        PauseTransition delay = new PauseTransition(Duration.seconds(BOT_DELAY_SECONDS));
        delay.setOnFinished(e -> {
            if (engine.getCurrentPhase() == GamePhase.SETUP) {
                runBotSetupTurn();
            } else if (engine.getCurrentPhase() == GamePhase.NORMAL) {
                runBotNormalTurn();
            } else {
                updateUI();
            }
        });
        delay.play();
    }

    private void runBotSetupTurn() {
        Player current = engine.getCurrentPlayer();
        if (!(current instanceof SimpleBot)) return;

        SimpleBot bot = (SimpleBot) current;
        if (bot.getRole() == null) {
            bot.setRole(FounderRole.NONE);
        }

        actionPane.updateStatus(bot.getName() + " (BOT)\nis setting up...");
        updateUI();

        PauseTransition thinkingDelay = new PauseTransition(Duration.seconds(0.6));
        thinkingDelay.setOnFinished(e -> {
            engine.playBotSetupTurn();
            updateUI();

            if (engine.getCurrentPhase() != GamePhase.FINISHED) {
                checkAndRunBotTurn();
            }
        });
        thinkingDelay.play();
    }

    private void runBotNormalTurn() {
        Player current = engine.getCurrentPlayer();
        if (!(current instanceof SimpleBot)) return;

        SimpleBot bot = (SimpleBot) current;
        actionPane.updateStatus(bot.getName() + " (BOT)\nis thinking...");
        updateUI();

        PauseTransition rollDelay = new PauseTransition(Duration.seconds(0.5));
        rollDelay.setOnFinished(e -> {
            try {
                Dice dice = new Dice();
                int total = engine.rollDice(dice);
                dicePane.showDiceResult(dice.getLastDie1(), dice.getLastDie2());
                java.util.Map<Player, Integer> discardMap = engine.distributeResources(total);
                engine.updateLongestNetworkAward();
                actionPane.updateStatus(bot.getName() + " rolled " + total);
                updateUI();

                handleDiscardFlow(discardMap, () -> {
                    PauseTransition buildDelay = new PauseTransition(Duration.seconds(0.6));
                    buildDelay.setOnFinished(e2 -> {
                        engine.playBotTurn(new Dice());
                        engine.updateLongestNetworkAward();
                        updateUI();

                        if (engine.getCurrentPhase() == GamePhase.FINISHED) {
                            showVictoryScreen();
                        } else {
                            checkAndRunBotTurn();
                        }
                    });
                    buildDelay.play();
                });
            } catch (Exception ex) {
                actionPane.updateStatus("Bot error: " + ex.getMessage());
                updateUI();
            }
        });
        rollDelay.play();
    }

    private void showVictoryScreen() {
        Player winner = null;
        for (Player p : engine.getPlayers()) {
            if (p.countPlayerPoint() >= 10) {
                winner = p;
                break;
            }
        }

        if (winner != null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Game Over!");
            alert.setHeaderText(winner.getName() + " wins!");
            alert.setContentText("Final score: " + winner.countPlayerPoint() + " points");
            alert.showAndWait();
        }
    }

    public void updateUI() {
        if (boardCanvas != null) boardCanvas.redraw();
        if (playerInfoPane != null) playerInfoPane.update();
        if (marketPane != null) marketPane.update();
        if (actionPane != null) actionPane.update();
        if (dicePane != null) dicePane.update();
    }

    public GameEngine getEngine() { return engine; }
    public model.Map getGameMap() { return gameMap; }
    public Market getMarket() { return market; }
    public DicePane getDicePane() { return dicePane; }
    public BoardCanvas getBoardCanvas() { return boardCanvas; }
    public ActionPane getActionPane() { return actionPane; }

    public static void main(String[] args) {
        launch(args);
    }
}
