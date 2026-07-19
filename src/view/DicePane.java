package view;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import model.*;
import controller.GameEngine;

public class DicePane extends HBox {
    private final GameEngine engine;
    private final MainApp app;
    private Canvas dice1Canvas;
    private Canvas dice2Canvas;
    private Label totalLabel;
    private Label liveTickerLabel;

    private static final int DIE_SIZE = 60;
    private static final int DOT_RADIUS = 6;

    public DicePane(GameEngine engine, MainApp app) {
        this.engine = engine;
        this.app = app;
        this.setSpacing(30);
        this.setStyle("-fx-padding: 15; -fx-background-color: #c0c0c0; -fx-border-color: #999; -fx-border-width: 0 0 2 0;");
        this.setPrefHeight(90);

        Label dice1Text = new Label("Dice 1:");
        dice1Text.setFont(Font.font("Arial", FontWeight.NORMAL, 16));

        dice1Canvas = new Canvas(DIE_SIZE, DIE_SIZE);
        drawBlankDie(dice1Canvas);

        Label dice2Text = new Label("Dice 2:");
        dice2Text.setFont(Font.font("Arial", FontWeight.NORMAL, 16));

        dice2Canvas = new Canvas(DIE_SIZE, DIE_SIZE);
        drawBlankDie(dice2Canvas);

        Label totalText = new Label("Total:");
        totalText.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        totalLabel = new Label("-");
        totalLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));

        StackPane dice1Pane = new StackPane(dice1Canvas);
        StackPane dice2Pane = new StackPane(dice2Canvas);

        // Initialize component explicitly to prevent NullPointerException
        liveTickerLabel = new Label("✨ Game Started! Welcome to Silicon Valley Catan.");
        liveTickerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        liveTickerLabel.setStyle("-fx-background-color: #424242; -fx-padding: 8 15; -fx-background-radius: 5; -fx-text-fill: white;");
        liveTickerLabel.setWrapText(true);
        liveTickerLabel.setPrefWidth(700);  // Set optimal dashboard width
        liveTickerLabel.setPrefHeight(95); // Adjust height for multi-line log messages

        // Configure layout layout margins
        HBox.setMargin(liveTickerLabel, new javafx.geometry.Insets(0, 0, 0, 40));

        // Append all child UI components into the HBox container
        this.getChildren().addAll(dice1Text, dice1Pane, dice2Text, dice2Pane, totalText, totalLabel, liveTickerLabel);
    }

    private void drawBlankDie(Canvas canvas) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, DIE_SIZE, DIE_SIZE);
        gc.setFill(Color.WHITE);
        gc.fillRoundRect(2, 2, DIE_SIZE - 4, DIE_SIZE - 4, 10, 10);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeRoundRect(2, 2, DIE_SIZE - 4, DIE_SIZE - 4, 10, 10);
    }

    private void drawDie(Canvas canvas, int value) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, DIE_SIZE, DIE_SIZE);

        // White rounded rectangle background
        gc.setFill(Color.WHITE);
        gc.fillRoundRect(2, 2, DIE_SIZE - 4, DIE_SIZE - 4, 10, 10);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeRoundRect(2, 2, DIE_SIZE - 4, DIE_SIZE - 4, 10, 10);

        // Dot positions relative to center
        double cx = DIE_SIZE / 2.0;
        double cy = DIE_SIZE / 2.0;
        double offset = DIE_SIZE / 4.0;

        gc.setFill(Color.BLACK);

        switch (value) {
            case 1:
                drawDot(gc, cx, cy);
                break;
            case 2:
                drawDot(gc, cx - offset, cy + offset);
                drawDot(gc, cx + offset, cy - offset);
                break;
            case 3:
                drawDot(gc, cx - offset, cy + offset);
                drawDot(gc, cx, cy);
                drawDot(gc, cx + offset, cy - offset);
                break;
            case 4:
                drawDot(gc, cx - offset, cy - offset);
                drawDot(gc, cx + offset, cy - offset);
                drawDot(gc, cx - offset, cy + offset);
                drawDot(gc, cx + offset, cy + offset);
                break;
            case 5:
                drawDot(gc, cx - offset, cy - offset);
                drawDot(gc, cx + offset, cy - offset);
                drawDot(gc, cx, cy);
                drawDot(gc, cx - offset, cy + offset);
                drawDot(gc, cx + offset, cy + offset);
                break;
            case 6:
                drawDot(gc, cx - offset, cy - offset);
                drawDot(gc, cx + offset, cy - offset);
                drawDot(gc, cx - offset, cy);
                drawDot(gc, cx + offset, cy);
                drawDot(gc, cx - offset, cy + offset);
                drawDot(gc, cx + offset, cy + offset);
                break;
        }
    }

    private void drawDot(GraphicsContext gc, double x, double y) {
        gc.fillOval(x - DOT_RADIUS, y - DOT_RADIUS, DOT_RADIUS * 2, DOT_RADIUS * 2);
    }

    public void update() {
        // Refresh dice display if results exist
        if (totalLabel != null && !totalLabel.getText().equals("-")) {
            // Redraw dice canvases to ensure they're visible
            dice1Canvas.getParent().requestLayout();
            dice2Canvas.getParent().requestLayout();
        }
    }

    public void showDiceResult(int dice1, int dice2) {
        drawDie(dice1Canvas, dice1);
        drawDie(dice2Canvas, dice2);

        int total = dice1 + dice2;
        totalLabel.setText(String.valueOf(total));

        if (total == 7) {
            totalLabel.setTextFill(Color.RED);
        } else {
            totalLabel.setTextFill(Color.BLACK);
        }

        // Visual feedback: highlight the dice pane background briefly
        this.setStyle("-fx-padding: 15; -fx-background-color: #e0e0e0; -fx-border-color: #4CAF50; -fx-border-width: 0 0 2 0;");
    }

    public void updateLiveTicker(String message, String type) {
        liveTickerLabel.setText(message);
        switch (type) {
            case "DICE":
                liveTickerLabel.setStyle("-fx-background-color: #2196F3; -fx-padding: 8 15; -fx-background-radius: 5; -fx-text-fill: white;");
                break;
            case "BUILD":
                liveTickerLabel.setStyle("-fx-background-color: #4CAF50; -fx-padding: 8 15; -fx-background-radius: 5; -fx-text-fill: white;");
                break;
            case "PENALTY":
                liveTickerLabel.setStyle("-fx-background-color: #F44336; -fx-padding: 8 15; -fx-background-radius: 5; -fx-text-fill: white;");
                break;
            case "BOT":
                liveTickerLabel.setStyle("-fx-background-color: #E91E63; -fx-padding: 8 15; -fx-background-radius: 5; -fx-text-fill: white;");
                break;
            default:
                liveTickerLabel.setStyle("-fx-background-color: #757575; -fx-padding: 8 15; -fx-background-radius: 5; -fx-text-fill: white;");
        }
    }
}