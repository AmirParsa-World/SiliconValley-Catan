package view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import model.*;
import controller.GameEngine;
import controller.GamePhase;
import controller.Market;

public class MarketPane extends HBox {
    private final Market market;
    private final GameEngine engine;
    private final MainApp app;

    private static final ResourceType[] TRADEABLE = {
            ResourceType.DATA, ResourceType.PATENT, ResourceType.CLOUD,
            ResourceType.CAPITAL, ResourceType.TALENT
    };

    private VBox[] resourceColumns;
    private Label[] priceLabels;
    private Label[] ownedLabels;
    private Button[] buyButtons;
    private Button[] sellButtons;

    private Label hintLabel;

    public MarketPane(Market market, GameEngine engine, MainApp app) {
        this.market = market;
        this.engine = engine;
        this.app = app;
        this.setSpacing(12);
        this.setStyle("-fx-padding: 8; -fx-background-color: #d0d0d0; -fx-border-color: #999; -fx-border-width: 2 0 0 0;");
        this.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("MARKET");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        title.setPadding(new Insets(0, 8, 0, 0));

        hintLabel = new Label("Roll dice first to trade!");
        hintLabel.setFont(Font.font("Arial", FontPosture.ITALIC, 11));
        hintLabel.setTextFill(Color.GRAY);

        resourceColumns = new VBox[TRADEABLE.length];
        priceLabels = new Label[TRADEABLE.length];
        ownedLabels = new Label[TRADEABLE.length];
        buyButtons = new Button[TRADEABLE.length];
        sellButtons = new Button[TRADEABLE.length];

        this.getChildren().addAll(title, hintLabel);

        for (int i = 0; i < TRADEABLE.length; i++) {
            resourceColumns[i] = createResourceColumn(i);
            this.getChildren().add(resourceColumns[i]);
        }

        update();
    }

    // 🔮 متد کمکی برای استخراج نام تمیز و خالص منبع به جای اسم سکتور
    private String getCleanResourceName(ResourceType type) {
        switch (type) {
            case DATA:    return "Data";
            case PATENT:  return "Patent";
            case CLOUD:   return "Cloud";
            case CAPITAL: return "Capital";
            case TALENT:  return "Talent";
            default:      return type.toString();
        }
    }

    private VBox createResourceColumn(int index) {
        ResourceType type = TRADEABLE[index];
        String cleanName = getCleanResourceName(type);

        // 🎯 اینجا به جای اسم سکتور، اسم خالص منبع قرار گرفت
        Label nameLabel = new Label(cleanName);
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(70);

        Rectangle colorBar = new Rectangle(10, 10);
        colorBar.setArcWidth(3);
        colorBar.setArcHeight(3);
        colorBar.setFill(getResourceColor(type));
        colorBar.setStroke(Color.web("#555555"));
        colorBar.setStrokeWidth(0.8);

        HBox header = new HBox(5, colorBar, nameLabel);
        header.setAlignment(Pos.CENTER);

        priceLabels[index] = new Label();
        priceLabels[index].setFont(Font.font("Arial", FontWeight.BOLD, 14));

        ownedLabels[index] = new Label("0");
        ownedLabels[index].setFont(Font.font("Arial", FontWeight.NORMAL, 11));

        buyButtons[index] = new Button("Buy");
        buyButtons[index].setPrefWidth(50);
        buyButtons[index].setPrefHeight(22);
        buyButtons[index].setFont(Font.font("Arial", FontWeight.NORMAL, 10));
        buyButtons[index].setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        buyButtons[index].setTooltip(new Tooltip("Spend CAPITAL to get 1 " + cleanName));
        buyButtons[index].setOnAction(e -> buyResource(type));

        sellButtons[index] = new Button("Sell");
        sellButtons[index].setPrefWidth(50);
        sellButtons[index].setPrefHeight(22);
        sellButtons[index].setFont(Font.font("Arial", FontWeight.NORMAL, 10));
        sellButtons[index].setStyle("-fx-background-color: #e53935; -fx-text-fill: white;");
        sellButtons[index].setTooltip(new Tooltip("Give 1 " + cleanName + " to get CAPITAL"));
        sellButtons[index].setOnAction(e -> sellResource(type));

        HBox buttons = new HBox(4, buyButtons[index], sellButtons[index]);
        buttons.setAlignment(Pos.CENTER);

        VBox col = new VBox(4, header, priceLabels[index], ownedLabels[index], buttons);
        col.setAlignment(Pos.CENTER);
        col.setPadding(new Insets(5));
        col.setStyle("-fx-background-color: #e8e8e8; -fx-background-radius: 4;");
        col.setPrefWidth(85);

        return col;
    }

    private void buyResource(ResourceType type) {
        Player current = engine.getCurrentPlayer();
        if (current instanceof SimpleBot) return;
        if (engine.getCurrentPhase() != GamePhase.NORMAL) return;

        try {
            market.buyResource(current, type, engine);
            int price = market.getPrice(type);
            app.getActionPane().updateStatus("Bought 1 " + getCleanResourceName(type) + "\nfor " + price + " CAPITAL");
            app.updateUI();
        } catch (Exception e) {
            app.getActionPane().updateStatus("Can't buy: " + e.getMessage());
        }
    }

    private void sellResource(ResourceType type) {
        Player current = engine.getCurrentPlayer();
        if (current instanceof SimpleBot) return;
        if (engine.getCurrentPhase() != GamePhase.NORMAL) return;

        try {
            market.sellResource(current, type, engine);
            int price = market.getPrice(type);
            app.getActionPane().updateStatus("Sold 1 " + getCleanResourceName(type) + "\nfor " + price + " CAPITAL");
            app.updateUI();
        } catch (Exception e) {
            app.getActionPane().updateStatus("Can't sell: " + e.getMessage());
        }
    }

    public void update() {
        Player current = engine.getCurrentPlayer();
        boolean canTrade = engine.getCurrentPhase() == GamePhase.NORMAL
                && !(current instanceof SimpleBot)
                && engine.hasRolledThisTurn();

        if (canTrade) {
            hintLabel.setText("Trading enabled - buy/sell resources!");
            hintLabel.setTextFill(Color.GREEN);
        } else if (engine.getCurrentPhase() == GamePhase.SETUP) {
            hintLabel.setText("Complete setup first");
            hintLabel.setTextFill(Color.ORANGE);
        } else if (current instanceof SimpleBot) {
            hintLabel.setText("Waiting for bot...");
            hintLabel.setTextFill(Color.GRAY);
        } else {
            hintLabel.setText("Roll dice first to trade!");
            hintLabel.setTextFill(Color.GRAY);
        }

        for (int i = 0; i < TRADEABLE.length; i++) {
            ResourceType type = TRADEABLE[i];
            String cleanName = getCleanResourceName(type);

            int price = market.getPrice(type);
            priceLabels[i].setText(String.valueOf(price) + " C");
            priceLabels[i].setTextFill(getPriceColor(price));

            int owned = current.getResource(type);
            ownedLabels[i].setText("x" + owned);

            if (type == ResourceType.CAPITAL) {
                buyButtons[i].setDisable(true);
                buyButtons[i].setOpacity(0.4);
                sellButtons[i].setDisable(true);
                sellButtons[i].setOpacity(0.4);
                Tooltip.install(buyButtons[i], new Tooltip("Can't trade CAPITAL with itself"));
                Tooltip.install(sellButtons[i], new Tooltip("Can't trade CAPITAL with itself"));
            } else {
                buyButtons[i].setDisable(!canTrade || current.getResource(ResourceType.CAPITAL) < price);
                sellButtons[i].setDisable(!canTrade || owned < 1);
                buyButtons[i].setOpacity(canTrade ? 1.0 : 0.5);
                sellButtons[i].setOpacity(canTrade ? 1.0 : 0.5);

                String buyTip = "Buy 1 " + cleanName + " for " + price + " CAPITAL";
                if (current.getResource(ResourceType.CAPITAL) < price) {
                    buyTip += " (need " + (price - current.getResource(ResourceType.CAPITAL)) + " more CAPITAL)";
                }
                Tooltip.install(buyButtons[i], new Tooltip(buyTip));

                String sellTip = "Sell 1 " + cleanName + " for " + price + " CAPITAL";
                if (owned < 1) {
                    sellTip += " (need at least 1)";
                }
                Tooltip.install(sellButtons[i], new Tooltip(sellTip));
            }
        }
    }

    private Color getPriceColor(int price) {
        if (price < 4) return Color.GREEN;
        if (price > 4) return Color.RED;
        return Color.BLACK;
    }

    private Color getResourceColor(ResourceType type) {
        switch (type) {
            case TALENT:  return Color.web("#90EE90");
            case CAPITAL: return Color.web("#FFD700");
            case CLOUD:   return Color.web("#87CEEB");
            case PATENT:  return Color.web("#D3D3D3");
            case DATA:    return Color.web("#FFB6C1");
            default:      return Color.GRAY;
        }
    }
}