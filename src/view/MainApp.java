package view;

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
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
    private int humanStartLogSize = 0;
    private model.Map gameMap;
    private Market market;
    private BorderPane root;
    private BoardCanvas boardCanvas;
    private PlayerInfoPane playerInfoPane;
    private MarketPane marketPane;
    private ActionPane actionPane;
    private DicePane dicePane;
    private Stage primaryStage;

    private static final double BOT_DELAY_SECONDS = 0.8;
    private static final String SAVE_FILE_PATH = "savegame.dat";

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        root = new BorderPane();

        Alert loadAlert = new Alert(Alert.AlertType.CONFIRMATION);
        loadAlert.setTitle("Silicon Valley Catan");
        loadAlert.setHeaderText("Welcome back!");
        loadAlert.setContentText("Do you want to load your previous saved game?");
        ButtonType buttonYes = new ButtonType("Yes, Load Game");
        ButtonType buttonNo = new ButtonType("No, Start New Game");
        loadAlert.getButtonTypes().setAll(buttonYes, buttonNo);

        Optional<ButtonType> loadResult = loadAlert.showAndWait();

        if (loadResult.isPresent() && loadResult.get() == buttonYes) {
            loadSavedGame(primaryStage);
        } else {
            startNewGameFlow();
        }

        Scene scene = new Scene(root, 1200, 800);

        primaryStage.setTitle("Silicon Valley Catan");
        primaryStage.setScene(scene);

        primaryStage.setOnCloseRequest(event -> {
            Alert exitAlert = new Alert(Alert.AlertType.CONFIRMATION,
                    "Do you want to save the game before exiting?",
                    ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
            exitAlert.setTitle("Exit Game");
            exitAlert.setHeaderText("Closing Silicon Valley Catan");

            Optional<ButtonType> result = exitAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.YES) {
                try {
                    SaveManager.saveGame(SAVE_FILE_PATH, engine);
                } catch (Exception e) {
                    System.err.println("Failed to save game: " + e.getMessage());
                }
            } else if (result.isPresent() && result.get() == ButtonType.CANCEL) {
                event.consume();
            }
        });

        primaryStage.show();
    }

    private void startNewGameFlow() {
        int playerCount = askPlayerCount();
        if (playerCount < 2 || playerCount > 4) {
            playerCount = 2;
        }

        javafx.scene.control.TextInputDialog sizeDialog = new javafx.scene.control.TextInputDialog("5");
        sizeDialog.setTitle("Custom Map Size");
        sizeDialog.setHeaderText("Enter Tech Park Dimensions (2 to 10)\nSizes above 10 are disabled for balance.");
        sizeDialog.setContentText("Grid Size:");

        Optional<String> result = sizeDialog.showAndWait();
        int mapSize = 5;

        if (result.isPresent()) {
            try {
                mapSize = Integer.parseInt(result.get());
                if (mapSize > 10) {
                    mapSize = 10;
                    Platform.runLater(() -> {
                        Alert capAlert = new Alert(Alert.AlertType.WARNING, "Map size capped at 10x10 for optimal gameplay and screen fitting!");
                        capAlert.show();
                    });
                } else if (mapSize < 2) {
                    mapSize = 2;
                }
            } catch (NumberFormatException e) {
                mapSize = 5;
            }
        }

        boolean[] isBotArray = askPlayerTypes(playerCount);
        initializeGame(playerCount, isBotArray, mapSize);
        buildUIComponents();

        PauseTransition startupDelay = new PauseTransition(Duration.seconds(0.3));
        startupDelay.setOnFinished(e -> checkAndRunBotTurn());
        startupDelay.play();
    }

    private void loadSavedGame(Stage stage) {
        SaveManager.loadGame(SAVE_FILE_PATH, new SaveManager.LoadGameCallback() {
            @Override
            public void onSuccess(GameEngine loadedEngine) {
                Platform.runLater(() -> {
                    engine = loadedEngine;
                    gameMap = loadedEngine.getGameMap();
                    market = loadedEngine.getMarket();

                    buildUIComponents();

                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION, "Game loaded successfully!");
                    successAlert.setTitle("Success");
                    successAlert.setHeaderText(null);
                    successAlert.show();

                    checkAndRunBotTurn();
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                Platform.runLater(() -> {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR, errorMessage + "\nStarting a new game instead.");
                    errorAlert.setTitle("Load Failed");
                    errorAlert.setHeaderText("Could not load save file");
                    errorAlert.showAndWait();

                    startNewGameFlow();
                });
            }
        });
    }

    private void buildUIComponents() {
        boardCanvas = new BoardCanvas(gameMap, this);
        playerInfoPane = new PlayerInfoPane(engine, this);
        marketPane = new MarketPane(market, engine, this);
        actionPane = new ActionPane(engine, gameMap, this);
        dicePane = new DicePane(engine, this);

        root.setCenter(boardCanvas);
        root.setRight(playerInfoPane);
        root.setBottom(marketPane);
        root.setLeft(actionPane);
        root.setTop(dicePane);

        updateUI();
    }

    public void triggerManualSave() {
        try {
            SaveManager.saveGame(SAVE_FILE_PATH, engine);
        } catch (Exception e) {
            System.err.println("Failed to save game: " + e.getMessage());
        }
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Your current game state has been saved in background!");
        alert.setTitle("Game Saved");
        alert.setHeaderText(null);
        alert.show();
    }

    public void triggerManualLoad() {
        Stage currentStage = (Stage) root.getScene().getWindow();
        loadSavedGame(currentStage);
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

        MenuBar menuBar = createMenuBar();
        VBox topBar = new VBox(menuBar, dicePane);

        root.setTop(topBar);
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

    private void initializeGame(int playerCount, boolean[] isBotArray, int mapSize) {
        gameMap = new Map(mapSize, mapSize);
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

    private void initializeGame(int playerCount, boolean[] isBotArray) {
        initializeGame(playerCount, isBotArray, 5);
    }

    public void handleDiscardFlow(java.util.Map<Player, Integer> discardMap, Runnable onDone) {
        if (discardMap == null || discardMap.isEmpty()) {
            handleAuditorMovement(onDone);
            return;
        }

        List<Player> affectedPlayers = new ArrayList<>(discardMap.keySet());
        processNextDiscard(discardMap, affectedPlayers, 0, () -> {
            handleAuditorMovement(onDone);
        });
    }

    private void handleAuditorMovement(Runnable onDone) {
        Player current = engine.getCurrentPlayer();

        if (current instanceof SimpleBot) {
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
        int sectorRows = sectors.length;
        int sectorCols = sectors[0].length;
        java.util.List<int[]> validTargets = new java.util.ArrayList<>();

        boolean anyHasStructures = false;
        for (int r = 0; r < sectorRows; r++) {
            for (int c = 0; c < sectorCols; c++) {
                model.Sector s = sectors[r][c];
                if (s != null && !s.isBlocked() && hasPlayersOnSector(s)) {
                    validTargets.add(new int[]{r, c});
                    anyHasStructures = true;
                }
            }
        }

        if (!anyHasStructures) {
            for (int r = 0; r < sectorRows; r++) {
                for (int c = 0; c < sectorCols; c++) {
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
                engine.log("🕵️‍♂️ " + bot.getName() + " moved the Auditor to Sector [" + target[0] + "," + target[1] + "].");
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

            engine.log("🚨 " + player.getName() + " [BOT] lost " + amount + " tech cards to the Tax Inspector.");

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

                    engine.log("🚨 " + player.getName() + " lost " + amount + " tech cards to the Tax Inspector.");

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

        if (!(current instanceof SimpleBot)) {
            // Lock the starting log index for the human player
            this.humanStartLogSize = engine.getGameLog().size();
            actionPane.initTurnLogStart(this.humanStartLogSize);

            displayTurnSummary(humanStartLogSize, current.getName(), "HUMAN");
            return;
        }

        if (engine.getCurrentPhase() == GamePhase.FINISHED)
            return;

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

    public void reviewHumanTurnLog() {
        Player current = engine.getCurrentPlayer();
        displayTurnSummary(this.humanStartLogSize, current.getName(), "HUMAN");
    }

    private void runBotSetupTurn() {
        Player current = engine.getCurrentPlayer();
        if (!(current instanceof SimpleBot)) return;

        SimpleBot bot = (SimpleBot) current;

        // Smart role selection based on remaining market availability
        if (bot.getRole() == null || bot.getRole() == FounderRole.NONE) {

            // 1. Identify all roles claimed by other players
            List<FounderRole> takenRoles = new ArrayList<>();
            for (Player p : engine.getPlayers()) {
                if (p != bot && p.getRole() != null && p.getRole() != FounderRole.NONE) {
                    takenRoles.add(p.getRole());
                }
            }

            // 2. Filter available startup roles
            List<FounderRole> availableRoles = new ArrayList<>();
            for (FounderRole r : FounderRole.values()) {
                if (r != FounderRole.NONE && !takenRoles.contains(r)) {
                    availableRoles.add(r);
                }
            }

            // 3. Final bot role assignment decision
            if (availableRoles.isEmpty()) {
                // Fallback for 4th player: assign NONE if no roles left
                bot.setRole(FounderRole.NONE);
                engine.log("🎭 STRATEGY: No unique roles left in Tech Park. Bot " + bot.getName() + " started with FounderRole.NONE.");
            } else {
                // Bot chooses between available options or staying roleless to avoid penalties
                List<FounderRole> pool = new ArrayList<>(availableRoles);
                pool.add(FounderRole.NONE);

                FounderRole chosenRole = pool.get(new java.util.Random().nextInt(pool.size()));
                bot.setRole(chosenRole);

                if (chosenRole != FounderRole.NONE) {
                    engine.log("🎭 STRATEGY: Bot " + bot.getName() + " claimed the available role: " + chosenRole + " (-1 Point applied).");
                } else {
                    engine.log("🎭 STRATEGY: Bot " + bot.getName() + " decided to remain roleless (NONE) to preserve starting points.");
                }
            }
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

        // Record starting log size to generate bot turn summary
        int startLogSize = engine.getGameLog().size();

        // Common logic for bot building phase after dice roll calculations
        Runnable botPostRollLogic = () -> {
            // Step 2: Delay for AI building execution
            PauseTransition buildDelay = new PauseTransition(Duration.seconds(1.8));
            buildDelay.setOnFinished(e2 -> {
                try {
                    // Execute AI turn logic (automatically advances turn in backend)
                    engine.playBotTurn(new Dice());
                    engine.updateLongestNetworkAward();

                    // Generate and display complete bot performance summary
                    displayTurnSummary(startLogSize, bot.getName(), "BUILD");
                    updateUI();

                    // Step 3: Enable the bot turn confirmation button for human player
                    Platform.runLater(() -> {
                        actionPane.getEndTurnBtn().setText("Next Turn ➡️");
                        actionPane.getEndTurnBtn().setDisable(false);
                        actionPane.getEndTurnBtn().setVisible(true);
                        actionPane.getEndTurnBtn().setManaged(true);
                        actionPane.getEndTurnBtn().setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;"); // Professional blue styling

                        actionPane.getEndTurnBtn().setOnAction(evt -> {
                            actionPane.getEndTurnBtn().setText("End Turn");
                            actionPane.getEndTurnBtn().setStyle(null);
                            actionPane.getEndTurnBtn().setOnAction(click -> actionPane.endTurn());

                            if (engine.getCurrentPhase() == GamePhase.FINISHED) {
                                showVictoryScreen();
                            } else {
                                updateUI();
                                checkAndRunBotTurn(); // Proceed to the next round
                            }
                        });
                    });
                } catch (Exception buildEx) {
                    System.err.println("🚨 Bot Build Logic Error: " + buildEx.getMessage());
                    buildEx.printStackTrace();
                    activateFailSafeButton("Bot Build Error! Skip Bot ➡️");
                }
            });
            buildDelay.play();
        };

        // Safe check: skip dice rolling phase if bot already rolled (e.g., after loading save)
        if (engine.hasRolledThisTurn()) {
            actionPane.updateStatus(bot.getName() + " (BOT)\nalready rolled. Upgrading...");
            // Directly execute post-roll logic without re-rolling
            botPostRollLogic.run();
        } else {
            // Normal gameplay: roll dice with a short delay
            PauseTransition rollDelay = new PauseTransition(Duration.seconds(1.2));
            rollDelay.setOnFinished(e -> {
                try {
                    Dice dice = new Dice();
                    int total = engine.rollDice(dice);

                    try {
                        dicePane.showDiceResult(dice.getLastDie1(), dice.getLastDie2());
                    } catch (Exception uiEx) {
                        System.err.println("🚨 UI Dice Display Error (Baran's code): " + uiEx.getMessage());
                    }

                    dicePane.updateLiveTicker("🎲 🤖 " + bot.getName() + " rolled a total of " + total + "!", "BOT");

                    java.util.Map<Player, Integer> discardMap = engine.distributeResources(total);
                    engine.updateLongestNetworkAward();
                    updateUI();

                    if (total == 7) {
                        dicePane.updateLiveTicker("🚨 Tax Auditor Alert! Checking resource cards...", "PENALTY");
                        handleDiscardFlow(discardMap, botPostRollLogic);
                    } else {
                        botPostRollLogic.run();
                    }

                } catch (Exception ex) {
                    System.err.println("🚨 CRITICAL BOT ERROR IN TURN RUNNER:");
                    ex.printStackTrace();
                    activateFailSafeButton("Bot Crashed! Skip Bot ➡️");
                }
            });
            rollDelay.play();
        }
    }

    // Fail-safe handler to prevent game freezing during critical errors
    private void activateFailSafeButton(String message) {
        Platform.runLater(() -> {
            actionPane.updateStatus(message);
            actionPane.getEndTurnBtn().setText(message);
            actionPane.getEndTurnBtn().setDisable(false);
            actionPane.getEndTurnBtn().setVisible(true);
            actionPane.getEndTurnBtn().setManaged(true);
            actionPane.getEndTurnBtn().setStyle("-fx-background-color: #d32f2f; -fx-text-fill: white; -fx-font-weight: bold;"); // Emergency red styling

            actionPane.getEndTurnBtn().setOnAction(evt -> {
                actionPane.getEndTurnBtn().setText("End Turn");
                actionPane.getEndTurnBtn().setStyle(null);
                actionPane.getEndTurnBtn().setOnAction(click -> actionPane.endTurn());

                // Force the engine to advance past the bot turn gracefully
                engine.nextTurn();
                updateUI();
                checkAndRunBotTurn();
            });
        });
    }

    public void showVictoryScreen() {
        javafx.application.Platform.runLater(() -> {
            model.Player winner = engine.getCurrentPlayer();
            int finalScore = winner.countPlayerPoint();
            String roleStr = winner.getRole() != null ? winner.getRole().toString() : "FOUNDER";

            // Create a dedicated modal stage for the victory celebration
            javafx.stage.Stage victoryStage = new javafx.stage.Stage();
            victoryStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            victoryStage.setTitle("🚀 IPO TRIUMPH - BILLION-DOLLAR EXIT!");

            javafx.scene.layout.VBox layout = new javafx.scene.layout.VBox(18);
            layout.setAlignment(javafx.geometry.Pos.CENTER);
            layout.setStyle("-fx-padding: 30; -fx-background-color: #111116; -fx-border-color: #fbc02d; -fx-border-width: 3; -fx-border-radius: 12; -fx-background-radius: 12;");

            // Startup-themed stylized title label
            javafx.scene.control.Label crownLabel = new javafx.scene.control.Label("✨ UNICORN IPO COMPLETED ✨");
            crownLabel.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 22));
            crownLabel.setTextFill(javafx.scene.paint.Color.web("#fbc02d"));

            javafx.scene.control.Label announcement = new javafx.scene.control.Label("👑 " + winner.getName() + " [" + roleStr + "] 👑");
            announcement.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 18));
            announcement.setTextFill(javafx.scene.paint.Color.WHITE);

            javafx.scene.control.Label subText = new javafx.scene.control.Label("Successfully dominated the Silicon Valley!\n" +
                    "Reached a massive $10B Valuation with " + finalScore + " Tech Points! 🚀");
            subText.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontPosture.ITALIC, 12));
            subText.setTextFill(javafx.scene.paint.Color.LIGHTGRAY);
            subText.setStyle("-fx-text-alignment: center;");

            // Final achievements and milestones statistics box
            javafx.scene.layout.VBox statsBox = new javafx.scene.layout.VBox(6);
            statsBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            statsBox.setStyle("-fx-background-color: #1e1e26; -fx-padding: 12; -fx-background-radius: 6; -fx-max-width: 290; -fx-border-color: #333; -fx-border-radius: 6;");

            javafx.scene.control.Label statsTitle = new javafx.scene.control.Label("📈 Final Startup Milestones:");
            statsTitle.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 12));
            statsTitle.setTextFill(javafx.scene.paint.Color.web("#87CEEB"));

            javafx.scene.control.Label netStat = new javafx.scene.control.Label("• Longest Network: " + engine.calculateLongestNetwork(winner) + " Tech Paths 🛣️");
            netStat.setTextFill(javafx.scene.paint.Color.WHITE);
            netStat.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.SEMI_BOLD, 11));

            javafx.scene.control.Label cardStat = new javafx.scene.control.Label("• Unspent Assets: " + winner.getTotalResources() + " Resource Cards 💳");
            cardStat.setTextFill(javafx.scene.paint.Color.WHITE);
            cardStat.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.SEMI_BOLD, 11));

            statsBox.getChildren().addAll(statsTitle, netStat, cardStat);

            // Control buttons container for flexible exit options
            javafx.scene.layout.HBox buttonContainer = new javafx.scene.layout.HBox(12);
            buttonContainer.setAlignment(javafx.geometry.Pos.CENTER);

            // Option 1: Close popup to admire the final board layout
            javafx.scene.control.Button admireBtn = new javafx.scene.control.Button("Admire Board 🗺️");
            admireBtn.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 12));
            admireBtn.setStyle("-fx-background-color: #007ACC; -fx-text-fill: white; -fx-padding: 8 15; -fx-background-radius: 5; -fx-cursor: hand;");
            admireBtn.setOnAction(e -> victoryStage.close());

            // Option 2: Shutdown the application completely
            javafx.scene.control.Button exitBtn = new javafx.scene.control.Button("Exit App 🛑");
            exitBtn.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 12));
            exitBtn.setStyle("-fx-background-color: #d32f2f; -fx-text-fill: white; -fx-padding: 8 15; -fx-background-radius: 5; -fx-cursor: hand;");
            exitBtn.setOnAction(e -> {
                victoryStage.close();
                javafx.application.Platform.exit();
            });

            buttonContainer.getChildren().addAll(admireBtn, exitBtn);
            layout.getChildren().addAll(crownLabel, announcement, subText, statsBox, buttonContainer);

            javafx.scene.Scene scene = new javafx.scene.Scene(layout, 400, 360);
            victoryStage.setScene(scene);
            victoryStage.setResizable(false);
            victoryStage.showAndWait();
        });
    }

    public void updateUI() {
        if (boardCanvas != null) boardCanvas.redraw();
        if (playerInfoPane != null) playerInfoPane.update();
        if (marketPane != null) marketPane.update();
        if (actionPane != null) actionPane.update();
        if (dicePane != null) dicePane.update();

        if (engine != null && !(engine.getCurrentPlayer() instanceof SimpleBot)) {
            displayTurnSummary(this.humanStartLogSize, engine.getCurrentPlayer().getName(), "HUMAN");
        }
    }

    public GameEngine getEngine() { return engine; }
    public model.Map getGameMap() { return gameMap; }
    public Market getMarket() { return market; }
    public DicePane getDicePane() { return dicePane; }
    public BoardCanvas getBoardCanvas() { return boardCanvas; }
    public ActionPane getActionPane() { return actionPane; }

    void displayTurnSummary(int startLogSize, String entityName, String type) {
        List<String> logs = engine.getGameLog();
        int currentSize = logs.size();

        String diceVal = "-";
        int mvpCount = 0;
        int roadCount = 0;
        int unicornCount = 0;

        int totalYield = 0;
        int tradeCount = 0;
        int taxLost = 0;
        String auditorLoc = "-";

        for (int i = startLogSize; i < currentSize; i++) {
            String logLine = logs.get(i);
            String logLower = logLine.toLowerCase();

            if (logLower.contains("rolled a")) {
                diceVal = logLine.replaceAll(".*rolled a (\\d+).*", "$1");
            }
            else if (logLower.contains("built an mvp") || logLower.contains("built mvp")) {
                mvpCount++;
            }
            else if (logLower.contains("partnership") || logLower.contains("road")) {
                roadCount++;
            }
            else if (logLower.contains("upgraded an") || logLower.contains("unicorn")) {
                unicornCount++;
            }
            else if (logLower.contains("auditor") && (logLower.contains("moved") || logLower.contains("sector"))) {
                auditorLoc = logLine.replaceAll(".*(?:sector|Sector)\\s*[\\[\\(](\\d+,\\d+)[\\]\\)].*", "[$1]");
                if (auditorLoc.equals(logLine)) auditorLoc = "Moved";
            }

            // Calculate aggregate resource deltas isolated for the current active player entity
            if (logLine.contains(entityName)) {
                if (logLower.contains("earned") || logLower.contains("yield") || logLower.contains("received") || logLower.contains("added")) {
                    // Match and extract integer value immediately following resource gain keywords
                    java.util.regex.Matcher m = java.util.regex.Pattern.compile("(?:earned|yield|received|added)\\s+(\\d+)").matcher(logLower);
                    if (m.find()) {
                        totalYield += Integer.parseInt(m.group(1));
                    }
                }
                if (logLower.contains("lost") || logLower.contains("taxed") || logLower.contains("discarded") || logLower.contains("spent")) {
                    // Match and extract integer value immediately following resource penalty keywords
                    java.util.regex.Matcher m = java.util.regex.Pattern.compile("(?:lost|taxed|discarded|spent)\\s+(\\d+)").matcher(logLower);
                    if (m.find()) {
                        taxLost += Integer.parseInt(m.group(1));
                    }
                }
                if (logLower.contains("trade") || logLower.contains("traded") || logLower.contains("market")) {
                    tradeCount++;
                }
            }
        }

        String yieldStr = totalYield > 0 ? "+" + totalYield : "-";
        String taxStr = taxLost > 0 ? "-" + taxLost : "-";
        String tradeStr = tradeCount > 0 ? tradeCount + "Tx" : "-";

        StringBuilder dashboard = new StringBuilder();
        dashboard.append(String.format("👑 %s | ", entityName));
        dashboard.append(String.format("🎲 Dice: %-3s  |  ", diceVal));
        dashboard.append(String.format("🏗️ MVP: %-2d  |  ", mvpCount));
        dashboard.append(String.format("🛣️ Road: %-2d  |  ", roadCount));
        dashboard.append(String.format("🦄 Unicorn: %-2d\n", unicornCount));

        dashboard.append(String.format("📊 STATUS   | "));
        dashboard.append(String.format("🔄 Trade: %-5s  |  ", tradeStr));
        dashboard.append(String.format("📦 Yield: %-6s  |  ", yieldStr));
        dashboard.append(String.format("🕵️‍♂️ Auditor: %-7s  |  ", auditorLoc));
        dashboard.append(String.format("🚨 Tax: %s", taxStr));

        dicePane.updateLiveTicker(dashboard.toString(), type);
    }

    public static void main(String[] args) {
        launch(args);
    }
}