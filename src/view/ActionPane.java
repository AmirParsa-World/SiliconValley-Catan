package view;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import model.*;
import controller.GameEngine;
import controller.GamePhase;

public class ActionPane extends VBox {
    private final GameEngine engine;
    private final model.Map gameMap;
    private final MainApp app;
    private Label statusLabel;
    private Label phaseLabel;
    private Button startSetupBtn;
    private Button rollDiceBtn;
    private Button buildMVPBtn;
    private Button upgradeUnicornBtn;
    private Button buildPartnershipBtn;
    private Button endTurnBtn;

    public ActionPane(GameEngine engine, model.Map gameMap, MainApp app) {
        this.engine = engine;
        this.gameMap = gameMap;
        this.app = app;
        this.setSpacing(10);
        this.setStyle("-fx-padding: 15; -fx-background-color: #f0f0f0; -fx-border-color: #999; -fx-border-width: 0 2 0 0;");
        this.setPrefWidth(200);

        Label title = new Label("ACTIONS");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        title.setUnderline(true);

        phaseLabel = new Label();
        phaseLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        phaseLabel.setWrapText(true);

        statusLabel = new Label();
        statusLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        statusLabel.setWrapText(true);

        // Setup phase button
        startSetupBtn = createButton("Start Setup");
        startSetupBtn.setOnAction(e -> startSetup());
        startSetupBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");

        // Normal phase buttons
        rollDiceBtn = createButton("Roll Dice");
        rollDiceBtn.setOnAction(e -> rollDice());

        buildMVPBtn = createButton("Build MVP");
        buildMVPBtn.setOnAction(e -> buildMVP());

        upgradeUnicornBtn = createButton("Upgrade Unicorn");
        upgradeUnicornBtn.setOnAction(e -> upgradeToUnicorn());

        buildPartnershipBtn = createButton("Build Partnership");
        buildPartnershipBtn.setOnAction(e -> buildPartnership());

        endTurnBtn = createButton("End Turn");
        endTurnBtn.setOnAction(e -> endTurn());

        this.getChildren().addAll(title, phaseLabel, statusLabel, startSetupBtn, rollDiceBtn, buildMVPBtn, upgradeUnicornBtn, buildPartnershipBtn, endTurnBtn);
        update();
    }

    private Button createButton(String text) {
        Button btn = new Button(text);
        btn.setPrefWidth(180);
        btn.setFont(Font.font("Arial", FontWeight.NORMAL, 13));
        return btn;
    }

    public void update() {
        Player current = engine.getCurrentPlayer();
        GamePhase phase = engine.getCurrentPhase();

        // Update phase label
        if (phase == GamePhase.SETUP) {
            phaseLabel.setText("SETUP PHASE");
            phaseLabel.setTextFill(Color.ORANGE);
        } else if (phase == GamePhase.NORMAL) {
            phaseLabel.setText("NORMAL PHASE");
            phaseLabel.setTextFill(Color.GREEN);
        } else {
            phaseLabel.setText("GAME OVER");
            phaseLabel.setTextFill(Color.RED);
        }

        statusLabel.setText("Current Player:\n" + current.getName());
        statusLabel.setTextFill(getPlayerColor(current));

        // Setup phase controls
        boolean isSetup = (phase == GamePhase.SETUP);
        boolean inSetupMode = app.getBoardCanvas().isSetupMode();

        startSetupBtn.setVisible(isSetup && !inSetupMode);
        startSetupBtn.setManaged(isSetup && !inSetupMode);

        // Normal phase controls
        boolean isNormal = (phase == GamePhase.NORMAL);
        rollDiceBtn.setVisible(isNormal);
        rollDiceBtn.setManaged(isNormal);
        buildMVPBtn.setVisible(isNormal);
        buildMVPBtn.setManaged(isNormal);
        upgradeUnicornBtn.setVisible(isNormal);
        upgradeUnicornBtn.setManaged(isNormal);
        buildPartnershipBtn.setVisible(isNormal);
        buildPartnershipBtn.setManaged(isNormal);
        endTurnBtn.setVisible(isNormal);
        endTurnBtn.setManaged(isNormal);

        if (isNormal) {
            rollDiceBtn.setDisable(engine.hasRolledThisTurn());
            buildMVPBtn.setDisable(!engine.hasRolledThisTurn());
            upgradeUnicornBtn.setDisable(!engine.hasRolledThisTurn());
            buildPartnershipBtn.setDisable(!engine.hasRolledThisTurn());
            endTurnBtn.setDisable(!engine.hasRolledThisTurn());
        }
    }

    public void updateStatus(String message) {
        statusLabel.setText(message);
    }

    private void startSetup() {
        app.getBoardCanvas().enterSetupMode();
        statusLabel.setText("Click a vertex\nto place your MVP");
        update();
    }

    private void rollDice() {
        try {
            int[] result = engine.rollDice();
            int dice1 = result[0];
            int dice2 = result[1];
            int total = dice1 + dice2;

            app.getDicePane().showDiceResult(dice1, dice2);
            statusLabel.setText("Rolled: " + total);
            engine.distributeResources(total, gameMap);
            app.updateUI();
        } catch (Exception e) {
            statusLabel.setText("Error: " + e.getMessage());
        }
    }

    private void buildMVP() {
        app.getBoardCanvas().enterBuildMVPMode();
        statusLabel.setText("Click a vertex\nto place your MVP");
    }

    private void upgradeToUnicorn() {
        app.getBoardCanvas().enterUpgradeUnicornMode();
        statusLabel.setText("Click your MVP\nto upgrade to Unicorn");
    }

    private void buildPartnership() {
        app.getBoardCanvas().enterBuildPartnershipMode();
        statusLabel.setText("Click an edge\nto place your partnership");
    }

    private void endTurn() {
        app.getBoardCanvas().cancelBuildMode();
        engine.setHasRolledThisTurn(false);
        engine.nextTurn();
        statusLabel.setText("Turn ended!");
        app.updateUI();
    }

    private Color getPlayerColor(Player player) {
        switch (player.getColor()) {
            case "Red": return Color.RED;
            case "Blue": return Color.BLUE;
            case "Green": return Color.GREEN;
            case "Yellow": return Color.YELLOW;
            default: return Color.BLACK;
        }
    }
}
