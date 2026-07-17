package view;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import model.*;
import controller.GameEngine;

public class DicePane extends HBox {
    private final GameEngine engine;
    private final MainApp app;
    private Label dice1Label;
    private Label dice2Label;
    private Label totalLabel;

    public DicePane(GameEngine engine, MainApp app) {
        this.engine = engine;
        this.app = app;
        this.setSpacing(30);
        this.setStyle("-fx-padding: 15; -fx-background-color: #c0c0c0; -fx-border-color: #999; -fx-border-width: 0 0 2 0;");
        this.setPrefHeight(60);

        Label dice1Text = new Label("Dice 1:");
        dice1Text.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        dice1Label = new Label("-");
        dice1Label.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        Label dice2Text = new Label("Dice 2:");
        dice2Text.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        dice2Label = new Label("-");
        dice2Label.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        Label totalText = new Label("Total:");
        totalText.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        totalLabel = new Label("-");
        totalLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));

        this.getChildren().addAll(dice1Text, dice1Label, dice2Text, dice2Label, totalText, totalLabel);
    }

    public void update() {
        // Keep showing last dice result
    }

    public void showDiceResult(int dice1, int dice2) {
        dice1Label.setText(String.valueOf(dice1));
        dice2Label.setText(String.valueOf(dice2));
        totalLabel.setText(String.valueOf(dice1 + dice2));

        if (dice1 + dice2 == 7) {
            totalLabel.setTextFill(Color.RED);
        } else {
            totalLabel.setTextFill(Color.BLACK);
        }
    }
}
