package util;

import controller.GameEngine;
import controller.Market;
import model.*;

import java.util.ArrayList;
import java.util.List;

public class MarketDemo {
    public static void main(String[] args) {
        System.out.println("=== MARKET TRADING DEMO ===\n");

        // Set up game
        model.Map gameMap = new model.Map();
        Market market = new Market();
        Player alice = new Player("Alice", "Red");
        Player bob = new Player("Bob", "Blue");

        List<Player> players = new ArrayList<>();
        players.add(alice);
        players.add(bob);

        GameEngine engine = new GameEngine(players, market, gameMap);

        // Complete setup phase
        Vertex v = findFirstValidVertex(gameMap);
        engine.setupPlaceMVPAndPartnership(alice, v, findFirstEmptyEdge(v));
        engine.nextTurn();
        v = findFirstValidVertex(gameMap);
        engine.setupPlaceMVPAndPartnership(bob, v, findFirstEmptyEdge(v));
        engine.nextTurn();
        v = findFirstValidVertex(gameMap);
        engine.setupPlaceMVPAndPartnership(bob, v, findFirstEmptyEdge(v));
        engine.nextTurn();
        v = findFirstValidVertex(gameMap);
        engine.setupPlaceMVPAndPartnership(alice, v, findFirstEmptyEdge(v));
        engine.nextTurn();

        // Give Alice lots of CAPITAL for trading
        alice.addResource(ResourceType.CAPITAL, 50);
        alice.addResource(ResourceType.DATA, 10);
        alice.addResource(ResourceType.CLOUD, 10);

        ResourceType[] tradeable = {ResourceType.DATA, ResourceType.PATENT, ResourceType.CLOUD, ResourceType.TALENT};

        System.out.println("--- INITIAL MARKET PRICES ---");
        printPrices(market, tradeable);
        System.out.println("Alice CAPITAL: " + alice.getResource(ResourceType.CAPITAL));
        System.out.println();

        // Buy 1 of each resource
        System.out.println("--- BUYING 1 DATA ---");
        market.buyResource(alice, ResourceType.DATA);
        printPrices(market, tradeable);
        System.out.println("Alice CAPITAL: " + alice.getResource(ResourceType.CAPITAL) + " (spent " + market.getPrice(ResourceType.DATA) + ")");
        System.out.println("Alice DATA: " + alice.getResource(ResourceType.DATA));
        System.out.println();

        System.out.println("--- BUYING 1 DATA ---");
        market.buyResource(alice, ResourceType.DATA);
        printPrices(market, tradeable);
        System.out.println("Alice CAPITAL: " + alice.getResource(ResourceType.CAPITAL));
        System.out.println("Alice DATA: " + alice.getResource(ResourceType.DATA));
        System.out.println();

        System.out.println("--- BUYING 1 PATENT ---");
        market.buyResource(alice, ResourceType.PATENT);
        printPrices(market, tradeable);
        System.out.println("Alice CAPITAL: " + alice.getResource(ResourceType.CAPITAL));
        System.out.println("Alice PATENT: " + alice.getResource(ResourceType.PATENT));
        System.out.println();

        System.out.println("--- BUYING 1 CLOUD ---");
        market.buyResource(alice, ResourceType.CLOUD);
        printPrices(market, tradeable);
        System.out.println("Alice CAPITAL: " + alice.getResource(ResourceType.CAPITAL));
        System.out.println("Alice CLOUD: " + alice.getResource(ResourceType.CLOUD));
        System.out.println();

        System.out.println("--- BUYING 1 TALENT ---");
        market.buyResource(alice, ResourceType.TALENT);
        printPrices(market, tradeable);
        System.out.println("Alice CAPITAL: " + alice.getResource(ResourceType.CAPITAL));
        System.out.println("Alice TALENT: " + alice.getResource(ResourceType.TALENT));
        System.out.println();

        // Buy more to push prices up
        System.out.println("--- BUYING 3 MORE DATA (pushing price up) ---");
        for (int i = 0; i < 3; i++) {
            market.buyResource(alice, ResourceType.DATA);
        }
        printPrices(market, tradeable);
        System.out.println("Alice CAPITAL: " + alice.getResource(ResourceType.CAPITAL));
        System.out.println("Alice DATA: " + alice.getResource(ResourceType.DATA));
        System.out.println();

        // Now sell some resources
        System.out.println("--- SELLING 2 DATA (pushing price down) ---");
        market.sellResource(alice, ResourceType.DATA);
        market.sellResource(alice, ResourceType.DATA);
        printPrices(market, tradeable);
        System.out.println("Alice CAPITAL: " + alice.getResource(ResourceType.CAPITAL));
        System.out.println("Alice DATA: " + alice.getResource(ResourceType.DATA));
        System.out.println();

        // Sell CLOUD to drop its price
        System.out.println("--- SELLING 3 CLOUD ---");
        for (int i = 0; i < 3; i++) {
            market.sellResource(alice, ResourceType.CLOUD);
        }
        printPrices(market, tradeable);
        System.out.println("Alice CAPITAL: " + alice.getResource(ResourceType.CAPITAL));
        System.out.println("Alice CLOUD: " + alice.getResource(ResourceType.CLOUD));
        System.out.println();

        // Generic market trade: 4 DATA for 1 PATENT
        System.out.println("--- GENERIC TRADE: 4 DATA -> 1 PATENT ---");
        int dataBefore = alice.getResource(ResourceType.DATA);
        int patentBefore = alice.getResource(ResourceType.PATENT);
        market.executeGenericMarketTrade(alice, ResourceType.DATA, ResourceType.PATENT);
        System.out.println("Alice DATA: " + dataBefore + " -> " + alice.getResource(ResourceType.DATA));
        System.out.println("Alice PATENT: " + patentBefore + " -> " + alice.getResource(ResourceType.PATENT));
        System.out.println();

        // Hacker CEO gets 3:1 rate
        System.out.println("--- HACKER CEO ROLE (3:1 trade rate) ---");
        alice.setRole(FounderRole.HACKER_CEO);
        int cloudBefore = alice.getResource(ResourceType.CLOUD);
        int talentBefore = alice.getResource(ResourceType.TALENT);
        market.executeGenericMarketTrade(alice, ResourceType.CLOUD, ResourceType.TALENT);
        System.out.println("Alice CLOUD: " + cloudBefore + " -> " + alice.getResource(ResourceType.CLOUD) + " (spent 3, not 4)");
        System.out.println("Alice TALENT: " + talentBefore + " -> " + alice.getResource(ResourceType.TALENT));
        System.out.println();

        // Hacker CEO buy at 3 instead of 4
        System.out.println("--- HACKER CEO BUY (costs 3 CAPITAL, not 4) ---");
        int capitalBefore = alice.getResource(ResourceType.CAPITAL);
        market.buyResource(alice, ResourceType.DATA);
        System.out.println("Alice CAPITAL: " + capitalBefore + " -> " + alice.getResource(ResourceType.CAPITAL) + " (spent 3)");
        System.out.println("Alice DATA: " + alice.getResource(ResourceType.DATA));
        System.out.println();

        System.out.println("=== FINAL MARKET STATE ---");
        printPrices(market, tradeable);
        System.out.println("Alice CAPITAL: " + alice.getResource(ResourceType.CAPITAL));
        System.out.println("Alice DATA: " + alice.getResource(ResourceType.DATA));
        System.out.println("Alice PATENT: " + alice.getResource(ResourceType.PATENT));
        System.out.println("Alice CLOUD: " + alice.getResource(ResourceType.CLOUD));
        System.out.println("Alice TALENT: " + alice.getResource(ResourceType.TALENT));

        System.out.println("\n=== DEMO COMPLETE ===");
    }

    static void printPrices(Market market, ResourceType[] types) {
        System.out.print("  Prices: ");
        for (ResourceType type : types) {
            System.out.print(type.name() + "=" + market.getPrice(type) + "  ");
        }
        System.out.println();
    }

    static Vertex findFirstValidVertex(model.Map gameMap) {
        Vertex[][] vertices = gameMap.getVertices();
        for (int r = 0; r < vertices.length; r++) {
            for (int c = 0; c < vertices[r].length; c++) {
                Vertex v = vertices[r][c];
                if (v != null && !v.hasStructure()) {
                    boolean tooClose = false;
                    for (Vertex n : v.getNeighbors()) {
                        if (n.hasStructure()) { tooClose = true; break; }
                    }
                    if (!tooClose) return v;
                }
            }
        }
        return null;
    }

    static Edge findFirstEmptyEdge(Vertex vertex) {
        for (Edge edge : vertex.getNeighboringEdges()) {
            if (edge.getOwner() == null) return edge;
        }
        return null;
    }
}
