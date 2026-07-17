package view;

import javafx.scene.control.Label;
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
    private List<Label> playerLabels;

    public PlayerInfoPane(GameEngine engine, MainApp app) {
        this.engine = engine;
        this.app = app;
        this.setSpacing(10);
        this.setStyle("-fx-padding: 15; -fx-background-color: #e8e8e8; -fx-border-color: #999; -fx-border-width: 0 0 0 2;");
        this.setPrefWidth(250);

        currentPlayerLabel = new Label();
        currentPlayerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        phaseLabel = new Label();
        phaseLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 14));

        Label title = new Label("PLAYERS");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        title.setUnderline(true);

        this.getChildren().addAll(title, currentPlayerLabel, phaseLabel);

        // Create player labels
        playerLabels = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            Label playerLabel = new Label();
            playerLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 13));
            playerLabel.setVisible(false);
            playerLabel.setManaged(false);
            playerLabels.add(playerLabel);
            this.getChildren().add(playerLabel);
        }

        update();
    }

    public void update() {
        Player current = engine.getCurrentPlayer();
        currentPlayerLabel.setText("Current: " + current.getName());
        currentPlayerLabel.setTextFill(getPlayerColor(current));

        phaseLabel.setText("Phase: " + engine.getCurrentPhase());

        List<Player> players = engine.getPlayers();

        for (int i = 0; i < 4; i++) {
            Label label = playerLabels.get(i);
            if (i < players.size()) {
                Player player = players.get(i);
                label.setText(formatPlayer(player));
                label.setTextFill(getPlayerColor(player));
                label.setVisible(true);
                label.setManaged(true);
            } else {
                label.setVisible(false);
                label.setManaged(false);
            }
        }
    }

    private String formatPlayer(Player p) {
        String roleStr = p.getRole() != null ? p.getRole().toString() : "None";
        return p.getName() + " (" + roleStr + ")\n" +
               "Points: " + p.countPlayerPoint() + " | Cards: " + p.getTotalResources();
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
