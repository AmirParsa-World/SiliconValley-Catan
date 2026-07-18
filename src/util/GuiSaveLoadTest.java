package util;

import controller.GameEngine;
import controller.GamePhase;
import controller.Market;
import model.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Tests save/load using the exact same code paths the GUI menu calls.
 * Runs without JavaFX toolkit — tests the data layer directly.
 * This verifies that SaveManager + serialization round-trips correctly,
 * which is what the GUI File > Save / File > Load menu triggers.
 */
public class GuiSaveLoadTest {
    private static int passed = 0;
    private static int failed = 0;
    private static final String SAVE_PATH = "gui_test_save.catan";

    public static void main(String[] args) throws Exception {
        System.out.println("=== GUI SAVE/LOAD TEST SUITE ===\n");

        testSaveAndLoadFullGame();
        testSaveAndLoadMidGame();
        testSaveAndLoadBotState();
        testLoadRestoresMarketPrices();
        testLoadRestoresTurnOrder();
        testLoadBoardVisualState();

        System.out.println("\n=== RESULTS: " + passed + " passed, " + failed + " failed ===");
        new File(SAVE_PATH).delete();
        System.exit(failed > 0 ? 1 : 0);
    }

    /**
     * Simulates: Game has 2 humans + 1 bot, setup completes, resources assigned,
     * then File > Save > File > Load. Verifies every field matches.
     */
    static void testSaveAndLoadFullGame() throws Exception {
        System.out.println("--- Test: Save & Load Full Game (2 Human + 1 Bot) ---");

        model.Map gameMap = new model.Map();
        Market market = new Market();

        Player human1 = new Player("Alice", "Red");
        Player human2 = new Player("Bob", "Blue");
        SimpleBot bot = new SimpleBot("Bot 1", "Green");

        List<Player> players = new ArrayList<>();
        players.add(human1);
        players.add(human2);
        players.add(bot);

        GameEngine engine = new GameEngine(players, market, gameMap);

        // Complete full setup phase (snake draft for 3 players)
        // Turn 0: Alice
        Vertex v = findFirstValidVertex(gameMap);
        engine.setupPlaceMVPAndPartnership(human1, v, findFirstEmptyEdge(v));
        engine.nextTurn();

        // Turn 1: Bot
        engine.playBotSetupTurn();
        // Turn 2: Bot (backward)
        engine.playBotSetupTurn();
        // Turn 3: Bob (backward)
        v = findFirstValidVertex(gameMap);
        engine.setupPlaceMVPAndPartnership(human2, v, findFirstEmptyEdge(v));
        engine.nextTurn();

        // Assign resources
        human1.addResource(ResourceType.CAPITAL, 3);
        human1.addResource(ResourceType.DATA, 5);
        human2.addResource(ResourceType.TALENT, 2);
        bot.addResource(ResourceType.CLOUD, 7);

        // Record pre-save state
        int h1Capital = human1.getResource(ResourceType.CAPITAL);
        int h1Data = human1.getResource(ResourceType.DATA);
        int h2Talent = human2.getResource(ResourceType.TALENT);
        int botCloud = bot.getResource(ResourceType.CLOUD);
        int h1Structures = human1.getStructures().size();
        int botStructures = bot.getStructures().size();
        GamePhase prePhase = engine.getCurrentPhase();
        String currentPlayer = engine.getCurrentPlayer().getName();
        boolean rolled = engine.hasRolledThisTurn();
        int playerCount = engine.getPlayers().size();

        // SAVE
        File saveFile = new File(SAVE_PATH);
        SaveManager.saveGame(saveFile.getAbsolutePath(), engine);
        assertTrue("Save file exists", saveFile.exists());
        assertTrue("Save file size > 0", saveFile.length() > 0);

        // LOAD — same code path as MainApp.loadGameState
        GameEngine loaded = SaveManager.loadGameSync(saveFile.getAbsolutePath());
        assertNotNull("Loaded engine not null", loaded);

        // Verify engine fields
        assertEqual("Phase preserved", loaded.getCurrentPhase(), prePhase);
        assertEqual("Current player preserved", loaded.getCurrentPlayer().getName(), currentPlayer);
        assertEqual("hasRolledThisTurn preserved", loaded.hasRolledThisTurn(), rolled);
        assertEqual("Player count preserved", loaded.getPlayers().size(), playerCount);

        // Verify player resources
        Player lH1 = loaded.getPlayers().get(0);
        Player lH2 = loaded.getPlayers().get(1);
        Player lBot = loaded.getPlayers().get(2);

        assertEqual("Alice CAPITAL", lH1.getResource(ResourceType.CAPITAL), h1Capital);
        assertEqual("Alice DATA", lH1.getResource(ResourceType.DATA), h1Data);
        assertEqual("Bob TALENT", lH2.getResource(ResourceType.TALENT), h2Talent);
        assertEqual("Bot CLOUD", lBot.getResource(ResourceType.CLOUD), botCloud);

        // Verify structures
        assertEqual("Alice structures", lH1.getStructures().size(), h1Structures);
        assertEqual("Bot structures", lBot.getStructures().size(), botStructures);

        // Verify instanceof survived serialization
        assertTrue("Bot is still SimpleBot", lBot instanceof SimpleBot);
        assertTrue("Alice is plain Player", !(lH1 instanceof SimpleBot));

        saveFile.delete();
        System.out.println();
    }

    static void testSaveAndLoadMidGame() throws Exception {
        System.out.println("--- Test: Save & Load Mid-Game (after dice rolls) ---");

        model.Map gameMap = new model.Map();
        Market market = new Market();

        Player p1 = new Player("Alice", "Red");
        Player p2 = new Player("Bob", "Blue");

        List<Player> players = new ArrayList<>();
        players.add(p1);
        players.add(p2);

        GameEngine engine = new GameEngine(players, market, gameMap);

        // Full setup
        Vertex v = findFirstValidVertex(gameMap);
        engine.setupPlaceMVPAndPartnership(p1, v, findFirstEmptyEdge(v));
        engine.nextTurn();

        v = findFirstValidVertex(gameMap);
        engine.setupPlaceMVPAndPartnership(p2, v, findFirstEmptyEdge(v));
        engine.nextTurn();

        v = findFirstValidVertex(gameMap);
        engine.setupPlaceMVPAndPartnership(p2, v, findFirstEmptyEdge(v));
        engine.nextTurn();

        v = findFirstValidVertex(gameMap);
        engine.setupPlaceMVPAndPartnership(p1, v, findFirstEmptyEdge(v));
        engine.nextTurn();

        // Simulate 5 turns of gameplay
        for (int i = 0; i < 5; i++) {
            Dice dice = new Dice();
            int roll = engine.rollDice(dice);
            engine.distributeResources(roll);
            engine.updateLongestNetworkAward();
            engine.setHasRolledThisTurn(false);
            engine.nextTurn();
        }

        // Record full resource snapshot
        int[][] preResources = new int[2][ResourceType.values().length];
        for (int pi = 0; pi < 2; pi++) {
            for (ResourceType rt : ResourceType.values()) {
                preResources[pi][rt.ordinal()] = engine.getPlayers().get(pi).getResource(rt);
            }
        }
        int preP1Total = p1.getTotalResources();
        int preP2Total = p2.getTotalResources();

        // Save & Load
        File saveFile = new File(SAVE_PATH);
        SaveManager.saveGame(saveFile.getAbsolutePath(), engine);
        GameEngine loaded = SaveManager.loadGameSync(saveFile.getAbsolutePath());

        // Verify every individual resource type
        for (ResourceType rt : ResourceType.values()) {
            if (rt == ResourceType.REGULATORY) continue;
            assertEqual("P1 " + rt + " mid-game", loaded.getPlayers().get(0).getResource(rt), preResources[0][rt.ordinal()]);
            assertEqual("P2 " + rt + " mid-game", loaded.getPlayers().get(1).getResource(rt), preResources[1][rt.ordinal()]);
        }
        assertEqual("P1 total resources", loaded.getPlayers().get(0).getTotalResources(), preP1Total);
        assertEqual("P2 total resources", loaded.getPlayers().get(1).getTotalResources(), preP2Total);

        // Verify game log
        assertTrue("Game log preserved", loaded.getGameLog().size() > 0);

        saveFile.delete();
        System.out.println();
    }

    static void testSaveAndLoadBotState() throws Exception {
        System.out.println("--- Test: Save & Load Bot State (instanceof preserved) ---");

        model.Map gameMap = new model.Map();
        Market market = new Market();

        SimpleBot bot1 = new SimpleBot("Bot Alpha", "Red");
        SimpleBot bot2 = new SimpleBot("Bot Beta", "Blue");

        List<Player> players = new ArrayList<>();
        players.add(bot1);
        players.add(bot2);

        GameEngine engine = new GameEngine(players, market, gameMap);

        // Full setup
        Vertex v = findFirstValidVertex(gameMap);
        engine.setupPlaceMVPAndPartnership(bot1, v, findFirstEmptyEdge(v));
        engine.nextTurn();

        v = findFirstValidVertex(gameMap);
        engine.setupPlaceMVPAndPartnership(bot2, v, findFirstEmptyEdge(v));
        engine.nextTurn();

        v = findFirstValidVertex(gameMap);
        engine.setupPlaceMVPAndPartnership(bot2, v, findFirstEmptyEdge(v));
        engine.nextTurn();

        v = findFirstValidVertex(gameMap);
        engine.setupPlaceMVPAndPartnership(bot1, v, findFirstEmptyEdge(v));
        engine.nextTurn();

        // Run bot turns
        for (int i = 0; i < 3; i++) {
            Dice dice = new Dice();
            int roll = engine.rollDice(dice);
            engine.distributeResources(roll);
            engine.setHasRolledThisTurn(false);
            engine.playBotTurn(new Dice());
            engine.updateLongestNetworkAward();
            engine.nextTurn();
        }

        int bot1Structures = bot1.getStructures().size();
        int bot2Structures = bot2.getStructures().size();
        boolean bot1Longest = bot1.isHasLongestNetwork();

        // Save & Load
        File saveFile = new File(SAVE_PATH);
        SaveManager.saveGame(saveFile.getAbsolutePath(), engine);
        GameEngine loaded = SaveManager.loadGameSync(saveFile.getAbsolutePath());

        Player lBot1 = loaded.getPlayers().get(0);
        Player lBot2 = loaded.getPlayers().get(1);

        assertTrue("Bot1 instanceof SimpleBot after load", lBot1 instanceof SimpleBot);
        assertTrue("Bot2 instanceof SimpleBot after load", lBot2 instanceof SimpleBot);
        assertEqual("Bot1 structures", lBot1.getStructures().size(), bot1Structures);
        assertEqual("Bot2 structures", lBot2.getStructures().size(), bot2Structures);
        assertEqual("Bot1 longest network flag", lBot1.isHasLongestNetwork(), bot1Longest);

        saveFile.delete();
        System.out.println();
    }

    static void testLoadRestoresMarketPrices() throws Exception {
        System.out.println("--- Test: Load Restores Market Prices ---");

        model.Map gameMap = new model.Map();
        Market market = new Market();

        Player p1 = new Player("Alice", "Red");
        List<Player> players = new ArrayList<>();
        players.add(p1);

        GameEngine engine = new GameEngine(players, market, gameMap);

        Vertex v = findFirstValidVertex(gameMap);
        engine.setupPlaceMVPAndPartnership(p1, v, findFirstEmptyEdge(v));
        engine.nextTurn();

        // Give resources and trade to shift prices
        p1.addResource(ResourceType.CAPITAL, 20);
        p1.addResource(ResourceType.DATA, 20);
        p1.addResource(ResourceType.TALENT, 20);
        p1.addResource(ResourceType.CLOUD, 20);
        p1.addResource(ResourceType.PATENT, 20);

        market.buyResource(p1, ResourceType.DATA);
        market.buyResource(p1, ResourceType.DATA);
        market.buyResource(p1, ResourceType.TALENT);
        market.sellResource(p1, ResourceType.CLOUD);

        int dataPrice = market.getPrice(ResourceType.DATA);
        int talentPrice = market.getPrice(ResourceType.TALENT);
        int cloudPrice = market.getPrice(ResourceType.CLOUD);

        File saveFile = new File(SAVE_PATH);
        SaveManager.saveGame(saveFile.getAbsolutePath(), engine);
        GameEngine loaded = SaveManager.loadGameSync(saveFile.getAbsolutePath());

        Market loadedMarket = loaded.getMarket();
        assertEqual("DATA price preserved", loadedMarket.getPrice(ResourceType.DATA), dataPrice);
        assertEqual("TALENT price preserved", loadedMarket.getPrice(ResourceType.TALENT), talentPrice);
        assertEqual("CLOUD price preserved", loadedMarket.getPrice(ResourceType.CLOUD), cloudPrice);

        saveFile.delete();
        System.out.println();
    }

    static void testLoadRestoresTurnOrder() throws Exception {
        System.out.println("--- Test: Load Restores Turn Order ---");

        model.Map gameMap = new model.Map();
        Market market = new Market();

        Player p1 = new Player("Alice", "Red");
        Player p2 = new Player("Bob", "Blue");

        List<Player> players = new ArrayList<>();
        players.add(p1);
        players.add(p2);

        GameEngine engine = new GameEngine(players, market, gameMap);

        // Full setup
        Vertex v = findFirstValidVertex(gameMap);
        engine.setupPlaceMVPAndPartnership(p1, v, findFirstEmptyEdge(v));
        engine.nextTurn();

        v = findFirstValidVertex(gameMap);
        engine.setupPlaceMVPAndPartnership(p2, v, findFirstEmptyEdge(v));
        engine.nextTurn();

        v = findFirstValidVertex(gameMap);
        engine.setupPlaceMVPAndPartnership(p2, v, findFirstEmptyEdge(v));
        engine.nextTurn();

        v = findFirstValidVertex(gameMap);
        engine.setupPlaceMVPAndPartnership(p1, v, findFirstEmptyEdge(v));
        engine.nextTurn();

        // Play 4 turns
        for (int i = 0; i < 4; i++) {
            Dice dice = new Dice();
            engine.rollDice(dice);
            engine.setHasRolledThisTurn(false);
            engine.nextTurn();
        }

        String currentPlayerName = engine.getCurrentPlayer().getName();
        boolean rolled = engine.hasRolledThisTurn();

        File saveFile = new File(SAVE_PATH);
        SaveManager.saveGame(saveFile.getAbsolutePath(), engine);
        GameEngine loaded = SaveManager.loadGameSync(saveFile.getAbsolutePath());

        assertEqual("Current player restored", loaded.getCurrentPlayer().getName(), currentPlayerName);
        assertEqual("hasRolledThisTurn restored", loaded.hasRolledThisTurn(), rolled);

        saveFile.delete();
        System.out.println();
    }

    static void testLoadBoardVisualState() throws Exception {
        System.out.println("--- Test: Load Preserves Board Visual State ---");

        model.Map gameMap = new model.Map();
        Market market = new Market();

        Player p1 = new Player("Alice", "Red");
        Player p2 = new Player("Bob", "Blue");

        List<Player> players = new ArrayList<>();
        players.add(p1);
        players.add(p2);

        GameEngine engine = new GameEngine(players, market, gameMap);

        // Full setup — places 2 MVPs and 2 partnerships per player
        Vertex v1 = findFirstValidVertex(gameMap);
        engine.setupPlaceMVPAndPartnership(p1, v1, findFirstEmptyEdge(v1));
        engine.nextTurn();

        Vertex v2 = findFirstValidVertex(gameMap);
        engine.setupPlaceMVPAndPartnership(p2, v2, findFirstEmptyEdge(v2));
        engine.nextTurn();

        Vertex v3 = findFirstValidVertex(gameMap);
        engine.setupPlaceMVPAndPartnership(p2, v3, findFirstEmptyEdge(v3));
        engine.nextTurn();

        Vertex v4 = findFirstValidVertex(gameMap);
        engine.setupPlaceMVPAndPartnership(p1, v4, findFirstEmptyEdge(v4));
        engine.nextTurn();

        // Count board elements before save
        int preVerticesWithStructure = 0;
        int preEdgesWithOwner = 0;
        int preSectorsWithBlocked = 0;

        for (Vertex[] row : gameMap.getVertices()) {
            for (Vertex v : row) {
                if (v != null && v.hasStructure()) preVerticesWithStructure++;
                for (Edge e : v.getNeighboringEdges()) {
                    if (e.getOwner() != null) preEdgesWithOwner++;
                }
            }
        }
        for (Sector[] row : gameMap.getSectors()) {
            for (Sector s : row) {
                if (s != null && s.isBlocked()) preSectorsWithBlocked++;
            }
        }

        File saveFile = new File(SAVE_PATH);
        SaveManager.saveGame(saveFile.getAbsolutePath(), engine);
        GameEngine loaded = SaveManager.loadGameSync(saveFile.getAbsolutePath());

        model.Map loadedMap = loaded.getGameMap();

        // Count board elements after load
        int postVerticesWithStructure = 0;
        int postEdgesWithOwner = 0;
        int postSectorsWithBlocked = 0;

        for (Vertex[] row : loadedMap.getVertices()) {
            for (Vertex v : row) {
                if (v != null && v.hasStructure()) {
                    postVerticesWithStructure++;
                    assertNotNull("Structured vertex has owner", v.getOwner());
                    assertNotNull("Structured vertex has structure", v.getStructure());
                    assertTrue("Structure owner matches vertex owner", v.getOwner().equals(v.getStructure().getOwner()));
                }
                for (Edge e : v.getNeighboringEdges()) {
                    if (e.getOwner() != null) {
                        postEdgesWithOwner++;
                        assertNotNull("Edge owner not null", e.getOwner());
                        assertTrue("Edge partnership flag", e.isPartnership());
                    }
                }
            }
        }
        for (Sector[] row : loadedMap.getSectors()) {
            for (Sector s : row) {
                if (s != null && s.isBlocked()) postSectorsWithBlocked++;
            }
        }

        assertEqual("Vertices with structures", postVerticesWithStructure, preVerticesWithStructure);
        assertEqual("Edges with owners", postEdgesWithOwner, preEdgesWithOwner);
        assertEqual("Blocked sectors", postSectorsWithBlocked, preSectorsWithBlocked);

        // Verify sector resource types and activation numbers preserved
        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 5; c++) {
                Sector orig = gameMap.getSectors()[r][c];
                Sector loaded2 = loadedMap.getSectors()[r][c];
                if (orig != null && loaded2 != null) {
                    assertEqual("Sector[" + r + "][" + c + "] resource type", loaded2.getResourceType(), orig.getResourceType());
                    assertEqual("Sector[" + r + "][" + c + "] activation number", loaded2.getActivationNumber(), orig.getActivationNumber());
                    assertEqual("Sector[" + r + "][" + c + "] blocked", loaded2.isBlocked(), orig.isBlocked());
                }
            }
        }

        // Verify auditor position
        assertEqual("Auditor row", loadedMap.getAuditor().getRow(), gameMap.getAuditor().getRow());
        assertEqual("Auditor col", loadedMap.getAuditor().getCol(), gameMap.getAuditor().getCol());

        saveFile.delete();
        System.out.println();
    }

    // --- Helpers ---

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

    static void assertNotNull(String label, Object obj) {
        if (obj != null) {
            System.out.println("  [PASS] " + label);
            passed++;
        } else {
            System.out.println("  [FAIL] " + label + " — was null");
            failed++;
        }
    }
}
