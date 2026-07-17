package view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Player;
import model.ResourceType;

import java.util.LinkedHashMap;
import java.util.Map;

public class DiscardDialog {

    public static void show(Player player, int cardsToDiscard) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Regulatory Crisis - Discard Cards");
        dialog.setResizable(false);

        Map<ResourceType, Integer> selections = new LinkedHashMap<>();
        for (ResourceType type : ResourceType.values()) {
            if (type != ResourceType.REGULATORY) {
                selections.put(type, 0);
            }
        }

        Label instruction = new Label(player.getName() + ", you have " + player.getTotalResources()
            + " cards. You must discard " + cardsToDiscard + ".");
        instruction.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        instruction.setWrapText(true);

        Label remainingLabel = new Label("Cards to discard: " + cardsToDiscard);
        remainingLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        remainingLabel.setTextFill(Color.RED);

        VBox resourceRows = new VBox(8);
        ResourceType[] activeTypes = {
            ResourceType.DATA, ResourceType.PATENT,
            ResourceType.CLOUD, ResourceType.CAPITAL, ResourceType.TALENT
        };

        int[] remaining = {cardsToDiscard};

        for (ResourceType type : activeTypes) {
            int available = player.getResource(type);
            if (available == 0) continue;

            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle("-fx-padding: 5; -fx-background-color: #f5f5f5; -fx-background-radius: 5;");

            Canvas icon = new Canvas(16, 16);
            GraphicsContext gc = icon.getGraphicsContext2D();
            gc.setFill(getResourceColor(type));
            gc.fillOval(0, 0, 16, 16);
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(1);
            gc.strokeOval(0, 0, 16, 16);

            Label nameLabel = new Label(type.getDisplayName() + ":");
            nameLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 13));

            Label countLabel = new Label(String.valueOf(available));
            countLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

            Button minusBtn = new Button("-");
            minusBtn.setPrefWidth(30);
            minusBtn.setFont(Font.font("Arial", FontWeight.BOLD, 14));

            Label discardCount = new Label("0");
            discardCount.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            discardCount.setTextFill(Color.RED);
            discardCount.setMinWidth(20);

            Button plusBtn = new Button("+");
            plusBtn.setPrefWidth(30);
            plusBtn.setFont(Font.font("Arial", FontWeight.BOLD, 14));

            final ResourceType rType = type;
            plusBtn.setOnAction(e -> {
                int currentSelection = selections.get(rType);
                if (currentSelection < available && remaining[0] > 0) {
                    selections.put(rType, currentSelection + 1);
                    discardCount.setText(String.valueOf(selections.get(rType)));
                    remaining[0]--;
                    remainingLabel.setText("Cards to discard: " + remaining[0]);
                    updateButtonStates(plusBtn, minusBtn, selections, rType, available, remaining[0]);
                }
            });

            minusBtn.setOnAction(e -> {
                int currentSelection = selections.get(rType);
                if (currentSelection > 0) {
                    selections.put(rType, currentSelection - 1);
                    discardCount.setText(String.valueOf(selections.get(rType)));
                    remaining[0]++;
                    remainingLabel.setText("Cards to discard: " + remaining[0]);
                    updateButtonStates(plusBtn, minusBtn, selections, rType, available, remaining[0]);
                }
            });

            updateButtonStates(plusBtn, minusBtn, selections, type, available, remaining[0]);

            row.getChildren().addAll(icon, nameLabel, countLabel, minusBtn, discardCount, plusBtn);
            resourceRows.getChildren().add(row);
        }

        Button confirmBtn = new Button("Confirm Discard");
        confirmBtn.setPrefWidth(200);
        confirmBtn.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        confirmBtn.setStyle("-fx-background-color: #e53935; -fx-text-fill: white; -fx-background-radius: 5;");
        confirmBtn.setDisable(true);

        confirmBtn.setOnAction(e -> {
            for (Map.Entry<ResourceType, Integer> entry : selections.entrySet()) {
                if (entry.getValue() > 0) {
                    player.spendResource(entry.getKey(), entry.getValue());
                }
            }
            dialog.close();
        });

        // Update confirm button based on remaining
        Runnable updateConfirm = () -> {
            confirmBtn.setDisable(remaining[0] > 0);
        };

        // Wrap the plus/minus actions to also update confirm
        // We'll check remaining[0] in the button handlers above
        // For simplicity, we check via a listener on remainingLabel text
        remainingLabel.textProperty().addListener((obs, oldVal, newVal) -> {
            String num = newVal.replaceAll("[^0-9]", "");
            try {
                int r = Integer.parseInt(num);
                confirmBtn.setDisable(r > 0);
            } catch (NumberFormatException ex) {
                confirmBtn.setDisable(true);
            }
        });

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        layout.getChildren().addAll(instruction, remainingLabel, resourceRows, confirmBtn);

        Scene scene = new Scene(layout, 420, 400);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private static void updateButtonStates(Button plus, Button minus,
            Map<ResourceType, Integer> selections, ResourceType type,
            int available, int remaining) {
        plus.setDisable(selections.get(type) >= available || remaining <= 0);
        minus.setDisable(selections.get(type) <= 0);
    }

    private static Color getResourceColor(ResourceType type) {
        switch (type) {
            case DATA:    return Color.web("#4CAF50");
            case PATENT:  return Color.web("#FF9800");
            case CLOUD:   return Color.web("#2196F3");
            case CAPITAL: return Color.web("#9E9E9E");
            case TALENT:  return Color.web("#E91E63");
            default:      return Color.GRAY;
        }
    }
}
