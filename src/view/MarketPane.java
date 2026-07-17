package view;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import model.*;
import controller.GameEngine;
import controller.Market;

public class MarketPane extends HBox {
    private final Market market;
    private final GameEngine engine;
    private final MainApp app;
    private Label[] priceLabels;

    public MarketPane(Market market, GameEngine engine, MainApp app) {
        this.market = market;
        this.engine = engine;
        this.app = app;
        this.setSpacing(25);
        this.setStyle("-fx-padding: 15; -fx-background-color: #d0d0d0; -fx-border-color: #999; -fx-border-width: 2 0 0 0;");
        this.setPrefHeight(80);

        Label title = new Label("MARKET PRICES:");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        ResourceType[] resources = {ResourceType.DATA, ResourceType.PATENT, ResourceType.CLOUD, ResourceType.CAPITAL, ResourceType.TALENT};
        priceLabels = new Label[resources.length];

        this.getChildren().add(title);

        for (int i = 0; i < resources.length; i++) {
            Label nameLabel = new Label(resources[i].getDisplayName() + ":");
            nameLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
            priceLabels[i] = new Label();
            priceLabels[i].setFont(Font.font("Arial", FontWeight.BOLD, 16));
            this.getChildren().addAll(nameLabel, priceLabels[i]);
        }

        update();
    }

    public void update() {
        ResourceType[] resources = {ResourceType.DATA, ResourceType.PATENT, ResourceType.CLOUD, ResourceType.CAPITAL, ResourceType.TALENT};
        for (int i = 0; i < resources.length; i++) {
            int price = market.getPrice(resources[i]);
            priceLabels[i].setText(String.valueOf(price));
            priceLabels[i].setTextFill(getPriceColor(price));
        }
    }

    private Color getPriceColor(int price) {
        if (price < 4) return Color.GREEN;
        if (price > 4) return Color.RED;
        return Color.BLACK;
    }
}
