package view;

import controller.GameEngine;
import controller.GamePhase;
import controller.Market;
import model.*;
import java.util.ArrayList;
import java.util.List;

public class BotTestRunner {
    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("=== BOT LOGIC TEST SUITE ===\n");

        testSetupPhase();
        testNormalPhaseWithBot();
        testBotBuildsMVP();
        testBotTurnRollsDice();
        testMultipleBotRounds();
        testVictoryCondition();

        System.out.println("\n=== RESULTS: " + passed + " passed, " + failed + " failed ===");
        System.exit(failed > 0 ? 1 : 0);
    }

    static void testSetupPhase() {
        System.out.println("--- Test: Setup Phase with 1 Human + 2 Bots ---");
        Map gameMap = new Map();
        Market market = new Market();

        Player human = new Player("Human", "Red");
        SimpleBot bot1 = new SimpleBot("Bot 1", "Blue");
        SimpleBot bot2 = new SimpleBot("Bot 2", "Green");

        List<Player> players = new ArrayList<>();
        players.add(human);
        players.add(bot1);
        players.add(bot2);

        GameEngine engine = new GameEngine(players, market, gameMap);

        // Snake draft for 3 players: [0,1,2,2,1,0]
        assertEqual("Phase is SETUP", engine.getCurrentPhase(), GamePhase.SETUP);
        assertEqual("First player is Human", engine.getCurrentPlayer().getName(), "Human");

        // Turn 0 (forward): Human
        Vertex v0 = findFirstValidVertex(gameMap);
        Edge e0 = findFirstEmptyEdge(v0);
        engine.setupPlaceMVPAndPartnership(human, v0, e0);
        assertEqual("Human has 1 structure", human.getStructures().size(), 1);
        assertEqual("Vertex owned by human", v0.getOwner(), human);
        assertEqual("Edge owned by human", e0.getOwner(), human);
        engine.nextTurn();

        // Turn 1 (forward): Bot 1 — playBotSetupTurn calls nextTurn internally
        assertEqual("Turn is Bot 1", engine.getCurrentPlayer().getName(), "Bot 1");
        engine.playBotSetupTurn();
        assertEqual("Bot 1 has 1 structure", bot1.getStructures().size(), 1);

        // Turn 2 (forward): Bot 2
        assertEqual("Turn is Bot 2", engine.getCurrentPlayer().getName(), "Bot 2");
        engine.playBotSetupTurn();
        assertEqual("Bot 2 has 1 structure", bot2.getStructures().size(), 1);

        // Turn 3 (backward): Bot 2
        assertEqual("Turn is Bot 2 (backward)", engine.getCurrentPlayer().getName(), "Bot 2");
        engine.playBotSetupTurn();
        assertEqual("Bot 2 has 2 structures", bot2.getStructures().size(), 2);

        // Turn 4 (backward): Bot 1
        assertEqual("Turn is Bot 1 (backward)", engine.getCurrentPlayer().getName(), "Bot 1");
        engine.playBotSetupTurn();
        assertEqual("Bot 1 has 2 structures", bot1.getStructures().size(), 2);

        // Turn 5 (backward): Human
        assertEqual("Turn is Human (backward)", engine.getCurrentPlayer().getName(), "Human");
        Vertex v1 = findFirstValidVertex(gameMap);
        Edge e1 = findFirstEmptyEdge(v1);
        engine.setupPlaceMVPAndPartnership(human, v1, e1);
        assertEqual("Human has 2 structures", human.getStructures().size(), 2);
        engine.nextTurn();

        assertEqual("Phase is NORMAL after setup", engine.getCurrentPhase(), GamePhase.NORMAL);

        System.out.println();
    }

    static void testNormalPhaseWithBot() {
        System.out.println("--- Test: Normal Phase - Bot Turn ---");
        Map gameMap = new Map();
        Market market = new Market();

        Player human = new Player("Human", "Red");
        SimpleBot bot = new SimpleBot("Bot", "Blue");

        List<Player> players = new ArrayList<>();
        players.add(human);
        players.add(bot);

        GameEngine engine = new GameEngine(players, market, gameMap);

        // Setup: 2 players, snake draft [0,1,1,0]
        Vertex hv = findFirstValidVertex(gameMap);
        Edge he = findFirstEmptyEdge(hv);
        engine.setupPlaceMVPAndPartnership(human, hv, he);
        engine.nextTurn();

        Vertex bv = findFirstValidVertex(gameMap);
        Edge be = findFirstEmptyEdge(bv);
        engine.setupPlaceMVPAndPartnership(bot, bv, be);
        engine.nextTurn();

        Vertex bv2 = findFirstValidVertex(gameMap);
        Edge be2 = findFirstEmptyEdge(bv2);
        engine.setupPlaceMVPAndPartnership(bot, bv2, be2);
        engine.nextTurn();

        Vertex hv2 = findFirstValidVertex(gameMap);
        Edge he2 = findFirstEmptyEdge(hv2);
        engine.setupPlaceMVPAndPartnership(human, hv2, he2);
        engine.nextTurn();

        assertEqual("Phase is NORMAL", engine.getCurrentPhase(), GamePhase.NORMAL);

        // Human rolls dice
        Dice dice = new Dice();
        int roll = engine.rollDice(dice);
        assertTrue("Roll is between 2-12", roll >= 2 && roll <= 12);
        assertTrue("Die 1 is 1-6", dice.getLastDie1() >= 1 && dice.getLastDie1() <= 6);
        assertTrue("Die 2 is 1-6", dice.getLastDie2() >= 1 && dice.getLastDie2() <= 6);
        assertEqual("Dice sum matches", dice.getLastDie1() + dice.getLastDie2(), roll);
        assertTrue("hasRolledThisTurn is true", engine.hasRolledThisTurn());

        engine.distributeResources(roll);
        engine.setHasRolledThisTurn(false);
        engine.nextTurn();

        assertEqual("Turn passed to Bot", engine.getCurrentPlayer().getName(), "Bot");

        // Bot rolls dice
        Dice botDice = new Dice();
        int botRoll = engine.rollDice(botDice);
        assertTrue("Bot roll is between 2-12", botRoll >= 2 && botRoll <= 12);

        engine.distributeResources(botRoll);
        engine.setHasRolledThisTurn(false);
        engine.nextTurn();

        assertEqual("Turn passed back to Human", engine.getCurrentPlayer().getName(), "Human");

        System.out.println();
    }

    static void testBotBuildsMVP() {
        System.out.println("--- Test: Bot Builds MVP ---");
        Map gameMap = new Map();
        Market market = new Market();

        SimpleBot bot = new SimpleBot("Bot", "Blue");
        SimpleBot bot2 = new SimpleBot("Bot 2", "Green");

        List<Player> players = new ArrayList<>();
        players.add(bot);
        players.add(bot2);

        GameEngine engine = new GameEngine(players, market, gameMap);

        // Setup: 2 players, snake draft [0,1,1,0]
        Vertex bv = findFirstValidVertex(gameMap);
        Edge be = findFirstEmptyEdge(bv);
        engine.setupPlaceMVPAndPartnership(bot, bv, be);
        engine.nextTurn();

        Vertex bv2 = findFirstValidVertex(gameMap);
        Edge be2 = findFirstEmptyEdge(bv2);
        engine.setupPlaceMVPAndPartnership(bot2, bv2, be2);
        engine.nextTurn();

        Vertex bv3 = findFirstValidVertex(gameMap);
        Edge be3 = findFirstEmptyEdge(bv3);
        engine.setupPlaceMVPAndPartnership(bot2, bv3, be3);
        engine.nextTurn();

        Vertex bv4 = findFirstValidVertex(gameMap);
        Edge be4 = findFirstEmptyEdge(bv4);
        engine.setupPlaceMVPAndPartnership(bot, bv4, be4);
        engine.nextTurn();

        assertEqual("Phase is NORMAL", engine.getCurrentPhase(), GamePhase.NORMAL);
        assertEqual("Bot has 2 structures", bot.getStructures().size(), 2);

        // Give bot resources to build MVP
        bot.addResource(ResourceType.CAPITAL, 10);
        bot.addResource(ResourceType.TALENT, 10);
        bot.addResource(ResourceType.CLOUD, 10);
        bot.addResource(ResourceType.DATA, 10);

        // Bot rolls
        Dice dice = new Dice();
        int roll = engine.rollDice(dice);
        engine.distributeResources(roll);
        engine.setHasRolledThisTurn(false);

        // Bot builds MVP via playBotTurn
        int structuresBefore = bot.getStructures().size();
        engine.playBotTurn(new Dice());

        assertTrue("Bot built MVP (structures increased)", bot.getStructures().size() > structuresBefore);
        assertEqual("Bot has 3 structures", bot.getStructures().size(), 3);

        System.out.println();
    }

    static void testBotTurnRollsDice() {
        System.out.println("--- Test: Bot Turn Rolls Dice and Distributes ---");
        Map gameMap = new Map();
        Market market = new Market();

        SimpleBot bot = new SimpleBot("Bot", "Blue");
        SimpleBot bot2 = new SimpleBot("Bot 2", "Green");

        List<Player> players = new ArrayList<>();
        players.add(bot);
        players.add(bot2);

        GameEngine engine = new GameEngine(players, market, gameMap);

        // Setup
        Vertex bv = findFirstValidVertex(gameMap);
        Edge be = findFirstEmptyEdge(bv);
        engine.setupPlaceMVPAndPartnership(bot, bv, be);
        engine.nextTurn();

        Vertex bv2 = findFirstValidVertex(gameMap);
        Edge be2 = findFirstEmptyEdge(bv2);
        engine.setupPlaceMVPAndPartnership(bot2, bv2, be2);
        engine.nextTurn();

        Vertex bv3 = findFirstValidVertex(gameMap);
        Edge be3 = findFirstEmptyEdge(bv3);
        engine.setupPlaceMVPAndPartnership(bot2, bv3, be3);
        engine.nextTurn();

        Vertex bv4 = findFirstValidVertex(gameMap);
        Edge be4 = findFirstEmptyEdge(bv4);
        engine.setupPlaceMVPAndPartnership(bot, bv4, be4);
        engine.nextTurn();

        assertEqual("Phase is NORMAL", engine.getCurrentPhase(), GamePhase.NORMAL);

        int resourcesBefore = bot.getTotalResources();

        Dice dice = new Dice();
        int roll = engine.rollDice(dice);

        boolean botHasMatchingSector = false;
        for (Sector[] row : gameMap.getSectors()) {
            for (Sector sector : row) {
                if (sector != null && sector.getActivationNumber() == roll && !sector.isBlocked()) {
                    Vertex[] corners = {sector.getBottomLeft(), sector.getBottomRight(),
                                       sector.getTopLeft(), sector.getTopRight()};
                    for (Vertex v : corners) {
                        if (v != null && v.hasStructure() && v.getOwner() != null
                            && v.getOwner().equals(bot)) {
                            botHasMatchingSector = true;
                        }
                    }
                }
            }
        }

        engine.distributeResources(roll);
        int resourcesAfter = bot.getTotalResources();

        if (botHasMatchingSector) {
            assertTrue("Bot received resources from matching sector", resourcesAfter > resourcesBefore);
        } else {
            assertEqual("Bot resources unchanged (no matching sector)", resourcesAfter, resourcesBefore);
        }

        System.out.println();
    }

    static void testMultipleBotRounds() {
        System.out.println("--- Test: Multiple Bot Rounds ---");
        Map gameMap = new Map();
        Market market = new Market();

        SimpleBot bot1 = new SimpleBot("Bot 1", "Blue");
        SimpleBot bot2 = new SimpleBot("Bot 2", "Green");

        List<Player> players = new ArrayList<>();
        players.add(bot1);
        players.add(bot2);

        GameEngine engine = new GameEngine(players, market, gameMap);

        // Full setup
        Vertex bv = findFirstValidVertex(gameMap);
        Edge be = findFirstEmptyEdge(bv);
        engine.setupPlaceMVPAndPartnership(bot1, bv, be);
        engine.nextTurn();

        Vertex bv2 = findFirstValidVertex(gameMap);
        Edge be2 = findFirstEmptyEdge(bv2);
        engine.setupPlaceMVPAndPartnership(bot2, bv2, be2);
        engine.nextTurn();

        Vertex bv3 = findFirstValidVertex(gameMap);
        Edge be3 = findFirstEmptyEdge(bv3);
        engine.setupPlaceMVPAndPartnership(bot2, bv3, be3);
        engine.nextTurn();

        Vertex bv4 = findFirstValidVertex(gameMap);
        Edge be4 = findFirstEmptyEdge(bv4);
        engine.setupPlaceMVPAndPartnership(bot1, bv4, be4);
        engine.nextTurn();

        assertEqual("Phase is NORMAL", engine.getCurrentPhase(), GamePhase.NORMAL);

        int roundsPlayed = 0;
        for (int round = 0; round < 20; round++) {
            if (engine.getCurrentPhase() == GamePhase.FINISHED) break;

            Player current = engine.getCurrentPlayer();
            Dice dice = new Dice();
            int roll = engine.rollDice(dice);
            engine.distributeResources(roll);
            engine.updateLongestNetworkAward();
            engine.setHasRolledThisTurn(false);

            if (current instanceof SimpleBot) {
                engine.playBotTurn(new Dice());
            }

            roundsPlayed++;
            if (engine.getCurrentPhase() != GamePhase.FINISHED) {
                engine.nextTurn();
            }
        }

        int totalResources = bot1.getTotalResources() + bot2.getTotalResources();
        assertTrue("Total resources > 0 after " + roundsPlayed + " rounds", totalResources > 0);

        // Each bot starts with 2 structures from setup — they may or may not build more
        int totalStructures = bot1.getStructures().size() + bot2.getStructures().size();
        assertTrue("Bots have at least 4 structures (from setup)", totalStructures >= 4);

        System.out.println("  [INFO] Rounds played: " + roundsPlayed);
        System.out.println("  [INFO] Bot 1 structures: " + bot1.getStructures().size()
            + ", resources: " + bot1.getTotalResources());
        System.out.println("  [INFO] Bot 2 structures: " + bot2.getStructures().size()
            + ", resources: " + bot2.getTotalResources());

        System.out.println();
    }

    static void testVictoryCondition() {
        System.out.println("--- Test: Victory Condition ---");
        Map gameMap = new Map();
        Market market = new Market();

        SimpleBot bot = new SimpleBot("Bot", "Blue");
        SimpleBot bot2 = new SimpleBot("Bot 2", "Green");

        List<Player> players = new ArrayList<>();
        players.add(bot);
        players.add(bot2);

        GameEngine engine = new GameEngine(players, market, gameMap);

        // Setup
        Vertex bv = findFirstValidVertex(gameMap);
        Edge be = findFirstEmptyEdge(bv);
        engine.setupPlaceMVPAndPartnership(bot, bv, be);
        engine.nextTurn();

        Vertex bv2 = findFirstValidVertex(gameMap);
        Edge be2 = findFirstEmptyEdge(bv2);
        engine.setupPlaceMVPAndPartnership(bot2, bv2, be2);
        engine.nextTurn();

        Vertex bv3 = findFirstValidVertex(gameMap);
        Edge be3 = findFirstEmptyEdge(bv3);
        engine.setupPlaceMVPAndPartnership(bot2, bv3, be3);
        engine.nextTurn();

        Vertex bv4 = findFirstValidVertex(gameMap);
        Edge be4 = findFirstEmptyEdge(bv4);
        engine.setupPlaceMVPAndPartnership(bot, bv4, be4);
        engine.nextTurn();

        assertEqual("Phase is NORMAL", engine.getCurrentPhase(), GamePhase.NORMAL);

        // Give bot enough points to win (10+)
        for (int i = 0; i < 9; i++) {
            Vertex v = findFirstValidVertex(gameMap);
            if (v != null) {
                Unicorn u = new Unicorn(bot, v);
                v.setOwner(bot);
                v.setStructure(u);
                bot.addStructure(u);
            }
        }

        assertTrue("Bot has 10+ points", bot.countPlayerPoint() >= 10);

        // Trigger nextTurn which checks victory
        engine.setHasRolledThisTurn(false);
        engine.nextTurn();

        assertEqual("Game is FINISHED", engine.getCurrentPhase(), GamePhase.FINISHED);

        System.out.println("  [INFO] Winner: " + bot.getName() + " with " + bot.countPlayerPoint() + " points");
        System.out.println();
    }

    // --- Helpers ---

    static Vertex findFirstValidVertex(Map gameMap) {
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

    static void assertEqual(String label, Object expected, Object actual) {
        if (expected.equals(actual)) {
            System.out.println("  [PASS] " + label);
            passed++;
        } else {
            System.out.println("  [FAIL] " + label + " — expected: " + expected + ", got: " + actual);
            failed++;
        }
    }

    static void assertTrue(String label, boolean condition) {
        if (condition) {
            System.out.println("  [PASS] " + label);
            passed++;
        } else {
            System.out.println("  [FAIL] " + label);
            failed++;
        }
    }
}
