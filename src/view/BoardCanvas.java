package view;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import model.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class BoardCanvas extends StackPane {
    private final model.Map gameMap;
    private final MainApp app;
    private final Canvas canvas;
    private final int SQUARE_SIZE = 100;
    private final int PADDING = 50;

    private BuildMode buildMode = BuildMode.NONE;
    private Vertex selectedVertex = null;

    enum BuildMode {
        NONE,
        SETUP_MVP,
        SETUP_ROAD,
        BUILD_MVP,
        BUILD_PARTNERSHIP,
        UPGRADE_UNICORN
    }

    public BoardCanvas(model.Map gameMap, MainApp app) {
        this.gameMap = gameMap;
        this.app = app;
        this.canvas = new Canvas(650, 650);
        this.getChildren().add(canvas);

        canvas.setOnMouseClicked(this::handleClick);
        redraw();
    }

    private void handleClick(MouseEvent event) {
        if (buildMode == BuildMode.NONE) return;

        double mouseX = event.getX();
        double mouseY = event.getY();

        switch (buildMode) {
            case SETUP_MVP:
                handleSetupMVP(mouseX, mouseY);
                break;
            case SETUP_ROAD:
                handleSetupRoad(mouseX, mouseY);
                break;
            case BUILD_MVP:
                handleBuildMVP(mouseX, mouseY);
                break;
            case BUILD_PARTNERSHIP:
                handleBuildPartnership(mouseX, mouseY);
                break;
            case UPGRADE_UNICORN:
                handleUpgradeUnicorn(mouseX, mouseY);
                break;
        }
    }

    private void handleSetupMVP(double mouseX, double mouseY) {
        Vertex clickedVertex = findVertexAt(mouseX, mouseY);
        if (clickedVertex != null && !clickedVertex.hasStructure()
            && app.getEngine().isValidStructurePlacement(clickedVertex)) {
            selectedVertex = clickedVertex;
            buildMode = BuildMode.SETUP_ROAD;
            app.getActionPane().updateStatus("Now click an edge\nconnected to your MVP");
            redraw();
        }
    }

    private void handleSetupRoad(double mouseX, double mouseY) {
        Edge clickedEdge = findEdgeAt(mouseX, mouseY);
        if (clickedEdge != null && clickedEdge.getOwner() == null && selectedVertex != null) {
            if (clickedEdge.getU() == selectedVertex || clickedEdge.getV() == selectedVertex) {
                app.getEngine().setupPlaceMVPAndPartnership(
                    app.getEngine().getCurrentPlayer(), selectedVertex, clickedEdge);

                selectedVertex = null;
                buildMode = BuildMode.NONE;
                app.getEngine().nextTurn();
                app.updateUI();
                app.checkAndRunBotTurn();
            }
        }
    }

    private void handleBuildMVP(double mouseX, double mouseY) {
        Vertex clickedVertex = findVertexAt(mouseX, mouseY);
        if (clickedVertex != null && !clickedVertex.hasStructure()
            && app.getEngine().isValidStructurePlacement(clickedVertex)) {
            try {
                Player current = app.getEngine().getCurrentPlayer();
                app.getEngine().buildMVP(current, clickedVertex);
                app.getEngine().updateLongestNetworkAward();
                buildMode = BuildMode.NONE;
                app.getActionPane().updateStatus("MVP built!");
                app.updateUI();
            } catch (Exception e) {
                app.getActionPane().updateStatus("Error: " + e.getMessage());
            }
        }
    }

    private void handleBuildPartnership(double mouseX, double mouseY) {
        Edge clickedEdge = findEdgeAt(mouseX, mouseY);
        if (clickedEdge != null && clickedEdge.getOwner() == null) {
            try {
                Player current = app.getEngine().getCurrentPlayer();
                app.getEngine().buildPartnership(current, clickedEdge);
                app.getEngine().updateLongestNetworkAward();
                buildMode = BuildMode.NONE;
                app.getActionPane().updateStatus("Partnership built!");
                app.updateUI();
            } catch (Exception e) {
                app.getActionPane().updateStatus("Error: " + e.getMessage());
            }
        }
    }

    private void handleUpgradeUnicorn(double mouseX, double mouseY) {
        Vertex clickedVertex = findVertexAt(mouseX, mouseY);
        if (clickedVertex != null && clickedVertex.hasStructure()
            && clickedVertex.getStructure() instanceof MVP
            && clickedVertex.getOwner().equals(app.getEngine().getCurrentPlayer())) {
            try {
                Player current = app.getEngine().getCurrentPlayer();
                app.getEngine().upgradeToUnicorn(current, clickedVertex);
                app.getEngine().updateLongestNetworkAward();
                buildMode = BuildMode.NONE;
                app.getActionPane().updateStatus("Upgraded to Unicorn!");
                app.updateUI();
            } catch (Exception e) {
                app.getActionPane().updateStatus("Error: " + e.getMessage());
            }
        }
    }

    private Vertex findVertexAt(double x, double y) {
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 6; col++) {
                Vertex vertex = gameMap.getVertices()[row][col];
                double vX = getVertexX(vertex);
                double vY = getVertexY(vertex);

                if (Math.abs(x - vX) < 15 && Math.abs(y - vY) < 15) {
                    return vertex;
                }
            }
        }
        return null;
    }

    private Edge findEdgeAt(double x, double y) {
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 6; col++) {
                Vertex vertex = gameMap.getVertices()[row][col];
                for (Edge edge : vertex.getNeighboringEdges()) {
                    double uX = getVertexX(edge.getU());
                    double uY = getVertexY(edge.getU());
                    double vX = getVertexX(edge.getV());
                    double vY = getVertexY(edge.getV());

                    double dist = distanceToLine(x, y, uX, uY, vX, vY);
                    if (dist < 10) {
                        return edge;
                    }
                }
            }
        }
        return null;
    }

    private double distanceToLine(double px, double py, double x1, double y1, double x2, double y2) {
        double A = px - x1;
        double B = py - y1;
        double C = x2 - x1;
        double D = y2 - y1;

        double dot = A * C + B * D;
        double lenSq = C * C + D * D;
        double param = -1;

        if (lenSq != 0) param = dot / lenSq;

        double xx, yy;

        if (param < 0) {
            xx = x1;
            yy = y1;
        } else if (param > 1) {
            xx = x2;
            yy = y2;
        } else {
            xx = x1 + param * C;
            yy = y1 + param * D;
        }

        double dx = px - xx;
        double dy = py - yy;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public void enterSetupMode() {
        Player current = app.getEngine().getCurrentPlayer();

        if (current instanceof SimpleBot) {
            return;
        }

        if (current.getRole() == null) {
            chooseRole(current);
        }

        buildMode = BuildMode.SETUP_MVP;
        selectedVertex = null;
        app.getActionPane().updateStatus("Click a vertex\nto place your MVP");
    }

    public void enterBuildMVPMode() {
        buildMode = BuildMode.BUILD_MVP;
        selectedVertex = null;
        app.getActionPane().updateStatus("Click a vertex\nto place your MVP");
    }

    public void enterBuildPartnershipMode() {
        buildMode = BuildMode.BUILD_PARTNERSHIP;
        selectedVertex = null;
        app.getActionPane().updateStatus("Click an edge\nto place your partnership");
    }

    public void enterUpgradeUnicornMode() {
        buildMode = BuildMode.UPGRADE_UNICORN;
        selectedVertex = null;
        app.getActionPane().updateStatus("Click your MVP\nto upgrade to Unicorn");
    }

    public void cancelBuildMode() {
        buildMode = BuildMode.NONE;
        selectedVertex = null;
    }

    public boolean isSetupMode() {
        return buildMode == BuildMode.SETUP_MVP || buildMode == BuildMode.SETUP_ROAD;
    }

    public boolean isBuildMode() {
        return buildMode != BuildMode.NONE;
    }

    private void chooseRole(Player player) {
        List<String> availableRoles = new ArrayList<>();
        availableRoles.add("No Role");

        boolean hackerTaken = false, vcTaken = false, guruTaken = false;
        for (Player p : app.getEngine().getPlayers()) {
            if (p != player && p.getRole() != null) {
                switch (p.getRole()) {
                    case HACKER_CEO: hackerTaken = true; break;
                    case VC_FUNDED: vcTaken = true; break;
                    case GURU_CTO: guruTaken = true; break;
                }
            }
        }

        if (!hackerTaken) availableRoles.add("Hacker CEO");
        if (!vcTaken) availableRoles.add("VC Funded");
        if (!guruTaken) availableRoles.add("Guru CTO");

        ChoiceDialog<String> dialog = new ChoiceDialog<>("No Role", availableRoles);
        dialog.setTitle("Choose Your Role");
        dialog.setHeaderText(player.getName() + ", choose your founder role:");
        dialog.setContentText("Role (-1 point if chosen):");

        Optional<String> result = dialog.showAndWait();
        String chosen = result.orElse("No Role");

        switch (chosen) {
            case "Hacker CEO":
                player.setRole(FounderRole.HACKER_CEO);
                app.getActionPane().updateStatus("Hacker CEO selected!\n-1 point, 3:1 trade rate");
                break;
            case "VC Funded":
                player.setRole(FounderRole.VC_FUNDED);
                app.getActionPane().updateStatus("VC Funded selected!\n-1 point, 9 card limit");
                break;
            case "Guru CTO":
                player.setRole(FounderRole.GURU_CTO);
                app.getActionPane().updateStatus("Guru CTO selected!\n-1 point, cheaper upgrades");
                break;
            default:
                player.setRole(FounderRole.NONE);
                app.getActionPane().updateStatus("No role selected.\nPlace your MVP & Road!");
                break;
        }
    }

    public void redraw() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        gc.setFill(Color.web("#f5f5dc"));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        drawSectors(gc);
        drawEdges(gc);
        drawVertices(gc);
        drawAuditor(gc);

        if (selectedVertex != null && buildMode != BuildMode.NONE) {
            double x = getVertexX(selectedVertex);
            double y = getVertexY(selectedVertex);
            gc.setStroke(Color.YELLOW);
            gc.setLineWidth(4);
            gc.strokeOval(x - 18, y - 18, 36, 36);
        }
    }

    private void drawSectors(GraphicsContext gc) {
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 5; col++) {
                Sector sector = gameMap.getSectors()[row][col];
                if (sector != null) {
                    drawSquareSector(gc, row, col, sector);
                }
            }
        }
    }

    private void drawSquareSector(GraphicsContext gc, int row, int col, Sector sector) {
        int x = col * SQUARE_SIZE + PADDING;
        int y = row * SQUARE_SIZE + PADDING;

        LinearGradient gradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, getSectorColor(sector.getResourceType())),
                new Stop(1, getSectorColor(sector.getResourceType()).darker()));
        gc.setFill(gradient);
        gc.fillRect(x, y, SQUARE_SIZE, SQUARE_SIZE);

        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeRect(x, y, SQUARE_SIZE, SQUARE_SIZE);

        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        String resourceName = sector.getResourceType().getDisplayName();
        double textWidth = resourceName.length() * 6.5;
        gc.fillText(resourceName, x + (SQUARE_SIZE - textWidth) / 2, y + SQUARE_SIZE / 2 - 8);

        gc.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        String numStr = String.valueOf(sector.getActivationNumber());
        textWidth = numStr.length() * 11;
        gc.fillText(numStr, x + (SQUARE_SIZE - textWidth) / 2, y + SQUARE_SIZE / 2 + 15);

        if (sector.isBlocked()) {
            gc.setFill(Color.rgb(255, 0, 0, 0.4));
            gc.fillRect(x, y, SQUARE_SIZE, SQUARE_SIZE);
            gc.setFill(Color.RED);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            gc.fillText("BLOCKED", x + 15, y + SQUARE_SIZE / 2 + 4);
        }
    }

    private void drawEdges(GraphicsContext gc) {
        Set<Edge> drawn = new HashSet<>();
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 6; col++) {
                Vertex vertex = gameMap.getVertices()[row][col];
                for (Edge edge : vertex.getNeighboringEdges()) {
                    if (drawn.add(edge)) {
                        drawEdge(gc, edge);
                    }
                }
            }
        }
    }

    private void drawEdge(GraphicsContext gc, Edge edge) {
        Vertex u = edge.getU();
        Vertex v = edge.getV();

        double uX = getVertexX(u);
        double uY = getVertexY(u);
        double vX = getVertexX(v);
        double vY = getVertexY(v);

        if (edge.getOwner() != null) {
            gc.setStroke(getPlayerColor(edge.getOwner()));
            gc.setLineWidth(8);
            gc.strokeLine(uX, uY, vX, vY);

            gc.setStroke(Color.WHITE);
            gc.setLineWidth(2);
            gc.strokeLine(uX, uY, vX, vY);
        } else {
            gc.setStroke(Color.web("#888888"));
            gc.setLineWidth(3);
            gc.setLineDashes(5, 5);
            gc.strokeLine(uX, uY, vX, vY);
            gc.setLineDashes();
        }
    }

    private void drawVertices(GraphicsContext gc) {
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 6; col++) {
                Vertex vertex = gameMap.getVertices()[row][col];
                drawVertex(gc, vertex, row, col);
            }
        }
    }

    private void drawVertex(GraphicsContext gc, Vertex vertex, int row, int col) {
        double x = getVertexX(vertex);
        double y = getVertexY(vertex);
        int size = 24;

        if (vertex.hasStructure()) {
            gc.setFill(getPlayerColor(vertex.getOwner()));
            gc.fillOval(x - size / 2, y - size / 2, size, size);

            gc.setStroke(Color.BLACK);
            gc.setLineWidth(3);
            gc.strokeOval(x - size / 2, y - size / 2, size, size);

            if (vertex.getStructure() instanceof Unicorn) {
                gc.setFill(Color.GOLD);
                gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
                gc.fillText("U", x - 5, y + 5);
            } else {
                gc.setFill(Color.WHITE);
                gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
                gc.fillText("M", x - 5, y + 5);
            }
        } else {
            gc.setFill(Color.WHITE);
            gc.fillOval(x - 10, y - 10, 20, 20);

            gc.setStroke(Color.web("#666666"));
            gc.setLineWidth(2);
            gc.strokeOval(x - 10, y - 10, 20, 20);
        }
    }

    private void drawAuditor(GraphicsContext gc) {
        Regulator auditor = gameMap.getAuditor();
        if (auditor != null) {
            int x = auditor.getCol() * SQUARE_SIZE + PADDING + SQUARE_SIZE / 2;
            int y = auditor.getRow() * SQUARE_SIZE + PADDING + SQUARE_SIZE / 2;

            gc.setFill(Color.RED);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 16));
            gc.fillText("A", x - 5, y + 6);

            gc.setStroke(Color.RED);
            gc.setLineWidth(3);
            gc.strokeOval(x - 15, y - 15, 30, 30);
        }
    }

    private double getVertexX(Vertex vertex) {
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 6; col++) {
                if (gameMap.getVertices()[row][col] == vertex) {
                    return col * SQUARE_SIZE + PADDING;
                }
            }
        }
        return 0;
    }

    private double getVertexY(Vertex vertex) {
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 6; col++) {
                if (gameMap.getVertices()[row][col] == vertex) {
                    return row * SQUARE_SIZE + PADDING;
                }
            }
        }
        return 0;
    }

    private Color getSectorColor(ResourceType type) {
        switch (type) {
            case DATA: return Color.web("#90EE90");
            case PATENT: return Color.web("#FFD700");
            case CLOUD: return Color.web("#87CEEB");
            case CAPITAL: return Color.web("#D3D3D3");
            case TALENT: return Color.web("#FFB6C1");
            case REGULATORY: return Color.web("#FFA07A");
            default: return Color.WHITE;
        }
    }

    private Color getPlayerColor(Player player) {
        if (player == null) return Color.GRAY;
        switch (player.getColor()) {
            case "Red": return Color.RED;
            case "Blue": return Color.BLUE;
            case "Green": return Color.GREEN;
            case "Yellow": return Color.YELLOW;
            default: return Color.GRAY;
        }
    }
}
