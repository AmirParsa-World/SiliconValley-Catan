package view;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import model.*;
import controller.GameEngine;
import java.util.ArrayList;
import java.util.List;

public class PlayerInfoPane extends VBox {
    private final GameEngine engine;
    private final MainApp app;
    private Label currentPlayerLabel;
    private Label phaseLabel;
    private List<VBox> playerCards;

    public PlayerInfoPane(GameEngine engine, MainApp app) {
        this.engine = engine;
        this.app = app;
        this.setSpacing(10);
        this.setStyle("-fx-padding: 15; -fx-background-color: #e8e8e8; -fx-border-color: #999; -fx-border-width: 0 0 0 2;");
        this.setPrefWidth(260);

        currentPlayerLabel = new Label();
        currentPlayerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        phaseLabel = new Label();
        phaseLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 14));

        Label title = new Label("PLAYERS");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        title.setUnderline(true);

        this.getChildren().addAll(title, currentPlayerLabel, phaseLabel);

        // Create player cards
        playerCards = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            VBox card = createPlayerCard();
            card.setVisible(false);
            card.setManaged(false);
            playerCards.add(card);
            this.getChildren().add(card);
        }

        update();
    }

    private VBox createPlayerCard() {
        VBox card = new VBox(3);
        card.setStyle("-fx-padding: 8; -fx-background-color: white; -fx-border-color: #bbb; -fx-border-width: 1; -fx-background-radius: 5; -fx-border-radius: 5;");

        Label nameLabel = new Label();
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        nameLabel.setId("nameLabel");

        Label roleLabel = new Label();
        roleLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 11));
        roleLabel.setTextFill(Color.GRAY);
        roleLabel.setId("roleLabel");

        Label pointsLabel = new Label();
        pointsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        pointsLabel.setId("pointsLabel");

        // Resource row with colored icons
        HBox resourcesBox = new HBox(6);
        resourcesBox.setId("resourcesBox");

        card.getChildren().addAll(nameLabel, roleLabel, pointsLabel, resourcesBox);
        return card;
    }

    private HBox createResourceDisplay(ResourceType type, int count) {
        HBox box = new HBox(2);
        box.setStyle("-fx-padding: 2 4; -fx-background-color: #f0f0f0; -fx-background-radius: 3;");

        // Small colored circle as resource icon
        Canvas icon = new Canvas(12, 12);
        GraphicsContext gc = icon.getGraphicsContext2D();
        gc.setFill(getResourceColor(type));
        gc.fillOval(0, 0, 12, 12);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.strokeOval(0, 0, 12, 12);

        Label countLabel = new Label(String.valueOf(count));
        countLabel.setFont(Font.font("Arial", FontWeight.BOLD, 11));

        box.getChildren().addAll(icon, countLabel);
        return box;
    }

    public void update() {
        Player current = engine.getCurrentPlayer();
        currentPlayerLabel.setText("Current: " + current.getName());
        currentPlayerLabel.setTextFill(getPlayerColor(current));

        phaseLabel.setText("Phase: " + engine.getCurrentPhase());

        List<Player> players = engine.getPlayers();

        for (int i = 0; i < 4; i++) {
            VBox card = playerCards.get(i);
            if (i < players.size()) {
                Player player = players.get(i);

                // Update name
                Label nameLabel = (Label) card.getChildren().get(0);
                String longNet = player.isHasLongestNetwork() ? " [CROWN]" : "";
                nameLabel.setText(player.getName() + longNet);
                nameLabel.setTextFill(getPlayerColor(player));

                // Update role
                Label roleLabel = (Label) card.getChildren().get(1);
                String roleStr = player.getRole() != null ? player.getRole().toString() : "No Role";
                roleLabel.setText("Role: " + roleStr);

                // Update points
                Label pointsLabel = (Label) card.getChildren().get(2);
                pointsLabel.setText("Points: " + player.countPlayerPoint() + "  |  Cards: " + player.getTotalResources());

                // Update resource breakdown
                HBox resourcesBox = (HBox) card.getChildren().get(3);
                resourcesBox.getChildren().clear();

                ResourceType[] activeTypes = {
                    ResourceType.DATA, ResourceType.PATENT,
                    ResourceType.CLOUD, ResourceType.CAPITAL, ResourceType.TALENT
                };
                for (ResourceType type : activeTypes) {
                    int count = player.getResource(type);
                    resourcesBox.getChildren().add(createResourceDisplay(type, count));
                }

                card.setVisible(true);
                card.setManaged(true);
            } else {
                card.setVisible(false);
                card.setManaged(false);
            }
        }
    }

    private Color getResourceColor(ResourceType type) {
        switch (type) {
            case DATA:    return Color.web("#4CAF50");  // green
            case PATENT:  return Color.web("#FF9800");  // orange
            case CLOUD:   return Color.web("#2196F3");  // blue
            case CAPITAL: return Color.web("#9E9E9E");  // gray
            case TALENT:  return Color.web("#E91E63");  // pink
            default:      return Color.GRAY;
        }
    }

    private Color getPlayerColor(Player player) {
        switch (player.getColor()) {
            case "Red":    return Color.RED;
            case "Blue":   return Color.BLUE;
            case "Green":  return Color.GREEN;
            case "Yellow": return Color.YELLOW;
            default:       return Color.BLACK;
        }
    }
}
