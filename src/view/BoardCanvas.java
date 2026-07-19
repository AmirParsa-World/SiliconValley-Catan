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
    private final int SQUARE_SIZE;
    private final int VERTEX_SIZE;
    private final int PADDING = 50;

    private BuildMode buildMode = BuildMode.NONE;
    private Vertex selectedVertex = null;

    enum BuildMode {
        NONE,
        SETUP_MVP,
        SETUP_ROAD,
        BUILD_MVP,
        BUILD_PARTNERSHIP,
        UPGRADE_UNICORN,
        MOVE_AUDITOR
    }

    public BoardCanvas(model.Map gameMap, MainApp app) {
        this.gameMap = gameMap;
        this.app = app;

        int sectorRows = gameMap.getSectors().length;
        int sectorCols = gameMap.getSectors()[0].length;

        // Dynamically scale tile and node sizes based on board rows to ensure clean UI fitting
        if (sectorRows <= 3) {
            this.SQUARE_SIZE = 140;
            this.VERTEX_SIZE = 28;
        } else if (sectorRows == 4) {
            this.SQUARE_SIZE = 120;
            this.VERTEX_SIZE = 26;
        } else if (sectorRows == 5) {
            this.SQUARE_SIZE = 100;
            this.VERTEX_SIZE = 24;
        } else {
            // Caps max width layout for extra large matrices (6x6 up to 10x10)
            this.SQUARE_SIZE = 500 / sectorRows;
            this.VERTEX_SIZE = Math.max(12, 120 / sectorRows);
        }

        double canvasWidth = sectorCols * SQUARE_SIZE + (PADDING * 2.0);
        double canvasHeight = sectorRows * SQUARE_SIZE + (PADDING * 2.0);

        this.canvas = new Canvas(canvasWidth, canvasHeight);
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
            case MOVE_AUDITOR:
                handleMoveAuditor(mouseX, mouseY);
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
                Player current = app.getEngine().getCurrentPlayer();

                app.getEngine().setupPlaceMVPAndPartnership(
                        app.getEngine().getCurrentPlayer(), selectedVertex, clickedEdge);

                app.getEngine().log("🚀 " + current.getName() + " placed initial MVP and starting Partnership.");

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
        if (clickedVertex == null) return;
        Player current = app.getEngine().getCurrentPlayer();

        // 1. Distance rule check: Cannot build directly adjacent to another structure
        for (Edge edge : clickedVertex.getNeighboringEdges()) {
            if (edge != null) {
                Vertex neighbor = (edge.getU() == clickedVertex) ? edge.getV() : edge.getU();

                if (neighbor != null && neighbor.hasStructure()) {
                    app.getActionPane().updateStatus("Error: Too close! 🛑\nDistance rule violated. Cannot build adjacent to another structure.");
                    buildMode = BuildMode.NONE;
                    app.updateUI();
                    return;
                }
            }
        }

        // 2. Connectivity check: Must connect to at least one owned road path
        boolean isConnectedToRoad = false;
        for (Edge edge : clickedVertex.getNeighboringEdges()) {
            if (edge != null && current.equals(edge.getOwner())) {
                isConnectedToRoad = true;
                break;
            }
        }
        if (!isConnectedToRoad) {
            app.getActionPane().updateStatus("Error: Too far! 🗺️\nYou must build your MVP connected to at least one of your Partnerships (Roads).");
            buildMode = BuildMode.NONE;
            app.updateUI();
            return;
        }

        // 3. Financial validation: Forward asset verification to engine
        try {
            app.getEngine().buildMVP(current, clickedVertex);
            app.getEngine().log("🏗️ " + current.getName() + " successfully deployed a new MVP.");
            app.getEngine().updateLongestNetworkAward();
            buildMode = BuildMode.NONE;
            app.getActionPane().updateStatus("MVP built successfully! 🎉");
            app.updateUI();
        } catch (Exception e) {
            buildMode = BuildMode.NONE;
            app.getActionPane().updateStatus("Error: Insufficient resources! 💸\n" + e.getMessage());
            app.updateUI();
        }
    }

    private void handleBuildPartnership(double mouseX, double mouseY) {
        Edge clickedEdge = findEdgeAt(mouseX, mouseY);
        if (clickedEdge == null) return;
        Player current = app.getEngine().getCurrentPlayer();

        // 1. Validation check: Path must not be claimed already
        if (clickedEdge.getOwner() != null) {
            app.getActionPane().updateStatus("Error: Invalid Edge! 🛑\nThis partnership path is already owned by " + clickedEdge.getOwner().getName());
            buildMode = BuildMode.NONE;
            app.updateUI();
            return;
        }

        // 2. Connectivity check: Must hook into player's existing cluster or roads
        boolean isConnected = false;
        Vertex u = clickedEdge.getU();
        Vertex v = clickedEdge.getV();

        if ((u.hasStructure() && current.equals(u.getOwner())) || (v.hasStructure() && current.equals(v.getOwner()))) {
            isConnected = true;
        }
        for (Edge edge : u.getNeighboringEdges()) {
            if (edge != clickedEdge && current.equals(edge.getOwner())) isConnected = true;
        }
        for (Edge edge : v.getNeighboringEdges()) {
            if (edge != clickedEdge && current.equals(edge.getOwner())) isConnected = true;
        }

        if (!isConnected) {
            app.getActionPane().updateStatus("Error: Unconnected Path! 🗺️\nPartnerships must connect to your existing roads or structures.");
            buildMode = BuildMode.NONE;
            app.updateUI();
            return;
        }

        // 3. Asset validation: Delegate resource check to backend engine
        try {
            app.getEngine().buildPartnership(current, clickedEdge);
            app.getEngine().log(" Road Secured: " + current.getName() + " established a new Partnership path.");
            app.getEngine().updateLongestNetworkAward();
            buildMode = BuildMode.NONE;
            app.getActionPane().updateStatus("Partnership built successfully! 🛣️");
            app.updateUI();
        } catch (Exception e) {
            buildMode = BuildMode.NONE;
            app.getActionPane().updateStatus("Error: Insufficient resources for Road! 💸\n" + e.getMessage());
            app.updateUI();
        }
    }

    private void handleUpgradeUnicorn(double mouseX, double mouseY) {
        Vertex clickedVertex = findVertexAt(mouseX, mouseY);
        if (clickedVertex == null) return;
        Player current = app.getEngine().getCurrentPlayer();

        // 1. Target validation: Vertex must hold an un-upgraded MVP belonging to the current player
        boolean hasStructure = clickedVertex.hasStructure();
        boolean isMVP = hasStructure && clickedVertex.getStructure() instanceof MVP;
        boolean isOwner = hasStructure && current.equals(clickedVertex.getOwner());

        if (!hasStructure || !isOwner) {
            app.getActionPane().updateStatus("Error: Invalid Target! 🦄\nYou can only upgrade a structure that belongs to you!");
            buildMode = BuildMode.NONE;
            app.updateUI();
            return;
        }
        if (!isMVP) {
            app.getActionPane().updateStatus("Error: Already Upgraded! 🛑\nThis structure is already a Unicorn or is not an MVP.");
            buildMode = BuildMode.NONE;
            app.updateUI();
            return;
        }

        // 2. Evolution check: Verify materials and apply upgrade via backend engine
        try {
            app.getEngine().upgradeToUnicorn(current, clickedVertex);
            app.getEngine().log("🦄✨ Valuation Spike! " + current.getName() + " upgraded an MVP into a Tech Unicorn!");
            app.getEngine().updateLongestNetworkAward();
            buildMode = BuildMode.NONE;
            app.getActionPane().updateStatus("Upgraded to Unicorn successfully! 🦄✨");
            app.updateUI();
        } catch (Exception e) {
            buildMode = BuildMode.NONE;
            app.getActionPane().updateStatus("Error: Insufficient assets for Unicorn! 💸\n" + e.getMessage());
            app.updateUI();
        }
    }

    private Vertex findVertexAt(double x, double y) {
        int vertexRows = gameMap.getVertices().length;
        int vertexCols = gameMap.getVertices()[0].length;

        for (int row = 0; row < vertexRows; row++) {
            for (int col = 0; col < vertexCols; col++) {
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
        int vertexRows = gameMap.getVertices().length;
        int vertexCols = gameMap.getVertices()[0].length;

        for (int row = 0; row < vertexRows; row++) {
            for (int col = 0; col < vertexCols; col++) {
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

    public void enterMoveAuditorMode() {
        buildMode = BuildMode.MOVE_AUDITOR;
        selectedVertex = null;
        app.getActionPane().updateStatus("Click a sector\nto move the Auditor");
        redraw();
    }

    private void handleMoveAuditor(double mouseX, double mouseY) {
        int sectorRows = gameMap.getSectors().length;
        int sectorCols = gameMap.getSectors()[0].length;

        int col = (int) ((mouseX - PADDING) / SQUARE_SIZE);
        int row = (int) ((mouseY - PADDING) / SQUARE_SIZE);

        if (row < 0 || row >= sectorRows || col < 0 || col >= sectorCols) return;

        try {
            Player current = app.getEngine().getCurrentPlayer();
            app.getEngine().moveAuditor(current, row, col);

            app.getEngine().log("🕵️‍♂️ Auditor Deployed: " + current.getName() + " moved the Regulatory Inspector to Sector [" + row + "," + col + "].");

            buildMode = BuildMode.NONE;
            app.getActionPane().updateStatus("Auditor moved!");
            app.updateUI();
        } catch (Exception e) {
            app.getActionPane().updateStatus("Error: " + e.getMessage());
        }
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

    public boolean isAuditorMovePending() {
        return buildMode == BuildMode.MOVE_AUDITOR;
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
        int sectorRows = gameMap.getSectors().length;
        int sectorCols = gameMap.getSectors()[0].length;

        for (int row = 0; row < sectorRows; row++) {
            for (int col = 0; col < sectorCols; col++) {
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
        gc.setLineWidth(1.5);
        gc.strokeRect(x, y, SQUARE_SIZE, SQUARE_SIZE);

        // Render activation token number dead center inside the tile
        double fontSize = SQUARE_SIZE * 0.25;
        gc.setFont(Font.font("Arial", FontWeight.BOLD, fontSize));
        gc.setFill(Color.BLACK);

        String numStr = String.valueOf(sector.getActivationNumber());
        gc.fillText(numStr, x + (SQUARE_SIZE / 2.0) - (fontSize * 0.3 * numStr.length()), y + (SQUARE_SIZE / 2.0) + (fontSize * 0.3));

        // Apply a clean translucent tint overlay and text badge for locked audited sectors
        if (sector.isBlocked()) {
            gc.setFill(Color.rgb(255, 0, 0, 0.12));
            gc.fillRect(x, y, SQUARE_SIZE, SQUARE_SIZE);

            gc.setFill(Color.web("#B22222"));
            gc.setFont(Font.font("Arial", FontWeight.BOLD, Math.max(8, SQUARE_SIZE * 0.12)));
            gc.fillText("⚠️ LOCKED", x + 6, y + (SQUARE_SIZE * 0.2));
        }

        if (buildMode == BuildMode.MOVE_AUDITOR) {
            gc.setStroke(Color.CYAN);
            gc.setLineWidth(2.5);
            gc.strokeRect(x + 2, y + 2, SQUARE_SIZE - 4, SQUARE_SIZE - 4);
        }
    }

    private void drawEdges(GraphicsContext gc) {
        Set<Edge> drawn = new HashSet<>();
        int vertexRows = gameMap.getVertices().length;
        int vertexCols = gameMap.getVertices()[0].length;

        for (int row = 0; row < vertexRows; row++) {
            for (int col = 0; col < vertexCols; col++) {
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
        int vertexRows = gameMap.getVertices().length;
        int vertexCols = gameMap.getVertices()[0].length;

        for (int row = 0; row < vertexRows; row++) {
            for (int col = 0; col < vertexCols; col++) {
                Vertex vertex = gameMap.getVertices()[row][col];
                drawVertex(gc, vertex);
            }
        }
    }

    private void drawVertex(GraphicsContext gc, Vertex vertex) {
        double x = getVertexX(vertex);
        double y = getVertexY(vertex);
        double size = VERTEX_SIZE;

        if (vertex.hasStructure()) {
            gc.setFill(getPlayerColor(vertex.getOwner()));
            gc.fillOval(x - size / 2.0, y - size / 2.0, size, size);

            gc.setStroke(Color.BLACK);
            gc.setLineWidth(size > 15 ? 3 : 1.5);
            gc.strokeOval(x - size / 2.0, y - size / 2.0, size, size);

            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, size * 0.6));
            String label = (vertex.getStructure() instanceof Unicorn) ? "U" : "M";
            gc.fillText(label, x - (size * 0.2), y + (size * 0.2));
        } else {
            // Unoccupied node point
            double emptySize = size * 0.75;
            gc.setFill(Color.WHITE);
            gc.fillOval(x - emptySize / 2.0, y - emptySize / 2.0, emptySize, emptySize);

            gc.setStroke(Color.web("#666666"));
            gc.setLineWidth(1.5);
            gc.strokeOval(x - emptySize / 2.0, y - emptySize / 2.0, emptySize, emptySize);
        }
    }

    private void drawAuditor(GraphicsContext gc) {
        Regulator auditor = gameMap.getAuditor();
        if (auditor != null) {
            int col = auditor.getCol();
            int row = auditor.getRow();

            double x = col * SQUARE_SIZE + PADDING;
            double y = row * SQUARE_SIZE + PADDING;

            // 1. Heavy red line border for regulatory alert styling
            gc.setStroke(Color.web("#B22222"));
            gc.setLineWidth(4);
            gc.strokeRect(x + 2, y + 2, SQUARE_SIZE - 4, SQUARE_SIZE - 4);

            // 2. Inner danger-tape dashed sequence
            gc.setStroke(Color.ORANGE);
            gc.setLineWidth(1.5);
            gc.setLineDashes(4, 4);
            gc.strokeRect(x + 5, y + 5, SQUARE_SIZE - 10, SQUARE_SIZE - 10);
            gc.setLineDashes();

            // 3. Offset detective hat icon to the top-right corner to prevent central token overlap
            double iconX = x + SQUARE_SIZE - (SQUARE_SIZE * 0.22);
            double iconY = y + (SQUARE_SIZE * 0.22);

            double baseWidth = SQUARE_SIZE * 0.26;
            double crownWidth = baseWidth * 0.65;

            // Hat base shadow
            gc.setFill(Color.rgb(0, 0, 0, 0.25));
            gc.fillRect(iconX - (baseWidth / 2.0), iconY + 1, baseWidth, 3);
            gc.fillRoundRect(iconX - (crownWidth / 2.0), iconY - 10, crownWidth, 11, 4, 4);

            // Hat brim
            gc.setFill(Color.web("#2F4F4F"));
            gc.fillRect(iconX - (baseWidth / 2.0), iconY, baseWidth, 3);

            // Hat crown shape
            gc.fillRoundRect(iconX - (crownWidth / 2.0), iconY - 11, crownWidth, 12, 5, 5);

            // Decorative band
            gc.setFill(Color.RED);
            gc.fillRect(iconX - (crownWidth / 2.0), iconY, crownWidth, 1.5);
        }
    }

    private double getVertexX(Vertex vertex) {
        int vertexRows = gameMap.getVertices().length;
        int vertexCols = gameMap.getVertices()[0].length;

        for (int row = 0; row < vertexRows; row++) {
            for (int col = 0; col < vertexCols; col++) {
                if (gameMap.getVertices()[row][col] == vertex) {
                    return col * SQUARE_SIZE + PADDING;
                }
            }
        }
        return 0;
    }

    private double getVertexY(Vertex vertex) {
        int vertexRows = gameMap.getVertices().length;
        int vertexCols = gameMap.getVertices()[0].length;

        for (int row = 0; row < vertexRows; row++) {
            for (int col = 0; col < vertexCols; col++) {
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