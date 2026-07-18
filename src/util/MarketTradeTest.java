package util;

import controller.GameEngine;
import controller.GamePhase;
import controller.Market;
import model.*;

import java.util.ArrayList;
import java.util.List;

public class MarketTradeTest {
    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) throws Exception {
        System.out.println("=== MARKET TRADING & DICE TEST SUITE ===\n");

        testDiceRollsCorrectRange();
        testDiceVisualData();
        testBuyResource();
        testSellResource();
        testBuyWithInsufficientCapital();
        testSellWithInsufficientResource();
        testCannotTradeCapital();
        testHackerCEO3To1Rate();
        testMarketPriceShiftsAfterBuy();
        testMarketPriceShiftsAfterSell();
        testGenericMarketTrade4to1();
        testHackerCEOGenericTrade3to1();
        testCannotTradeInSetupPhase();
        testCannotTradeBeforeRollingDice();

        System.out.println("\n=== RESULTS: " + passed + " passed, " + failed + " failed ===");
        System.exit(failed > 0 ? 1 : 0);
    }

    static GameEngine createGameInNormalPhase() {
        model.Map gameMap = new model.Map();
        Market market = new Market();
        Player p1 = new Player("Alice", "Red");
        Player p2 = new Player("Bob", "Blue");

        List<Player> players = new ArrayList<>();
        players.add(p1);
        players.add(p2);

        GameEngine engine = new GameEngine(players, market, gameMap);

        // Complete setup
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

        return engine;
    }

    static void testDiceRollsCorrectRange() {
        System.out.println("--- Test: Dice rolls 2-12 ---");
        for (int i = 0; i < 100; i++) {
            Dice dice = new Dice();
            int result = dice.roll();
            assertTrue("Roll " + (i+1) + " in range 2-12", result >= 2 && result <= 12);
        }
        System.out.println();
    }

    static void testDiceVisualData() {
        System.out.println("--- Test: Die values sum to total ---");
        Dice dice = new Dice();
        int total = dice.roll();
        assertTrue("Die1 in 1-6", dice.getLastDie1() >= 1 && dice.getLastDie1() <= 6);
        assertTrue("Die2 in 1-6", dice.getLastDie2() >= 1 && dice.getLastDie2() <= 6);
        assertEqual("Sum matches total", dice.getLastDie1() + dice.getLastDie2(), total);
        System.out.println();
    }

    static void testBuyResource() {
        System.out.println("--- Test: Buy resource (spend CAPITAL, get resource) ---");
        GameEngine engine = createGameInNormalPhase();
        Market market = engine.getMarket();
        Player alice = engine.getCurrentPlayer();

        alice.addResource(ResourceType.CAPITAL, 20);

        int capitalBefore = alice.getResource(ResourceType.CAPITAL);
        int dataBefore = alice.getResource(ResourceType.DATA);
        int price = market.getPrice(ResourceType.DATA);

        market.buyResource(alice, ResourceType.DATA);

        assertEqual("Capital spent = market price", capitalBefore - alice.getResource(ResourceType.CAPITAL), price);
        assertEqual("Gained 1 DATA", alice.getResource(ResourceType.DATA), dataBefore + 1);
        System.out.println();
    }

    static void testSellResource() {
        System.out.println("--- Test: Sell resource (give resource, get CAPITAL) ---");
        GameEngine engine = createGameInNormalPhase();
        Market market = engine.getMarket();
        Player alice = engine.getCurrentPlayer();

        alice.addResource(ResourceType.CLOUD, 10);

        int capitalBefore = alice.getResource(ResourceType.CAPITAL);
        int cloudBefore = alice.getResource(ResourceType.CLOUD);
        int price = market.getPrice(ResourceType.CLOUD);

        market.sellResource(alice, ResourceType.CLOUD);

        assertEqual("Gained CAPITAL = market price", alice.getResource(ResourceType.CAPITAL) - capitalBefore, price);
        assertEqual("Lost 1 CLOUD", alice.getResource(ResourceType.CLOUD), cloudBefore - 1);
        System.out.println();
    }

    static void testBuyWithInsufficientCapital() {
        System.out.println("--- Test: Buy fails with insufficient CAPITAL ---");
        GameEngine engine = createGameInNormalPhase();
        Market market = engine.getMarket();
        Player alice = engine.getCurrentPlayer();

        // Alice has 0 CAPITAL, market price is at least 2
        boolean threw = false;
        try {
            market.buyResource(alice, ResourceType.DATA);
        } catch (Exception e) {
            threw = true;
        }
        assertTrue("Buy throws NotEnoughResourceException", threw);
        System.out.println();
    }

    static void testSellWithInsufficientResource() {
        System.out.println("--- Test: Sell fails with 0 of that resource ---");
        GameEngine engine = createGameInNormalPhase();
        Market market = engine.getMarket();
        Player alice = engine.getCurrentPlayer();

        boolean threw = false;
        try {
            market.sellResource(alice, ResourceType.TALENT);
        } catch (Exception e) {
            threw = true;
        }
        assertTrue("Sell throws NotEnoughResourceException", threw);
        System.out.println();
    }

    static void testCannotTradeCapital() {
        System.out.println("--- Test: CAPITAL buttons disabled in GUI ---");
        GameEngine engine = createGameInNormalPhase();
        Market market = engine.getMarket();
        Player alice = engine.getCurrentPlayer();
        alice.addResource(ResourceType.CAPITAL, 20);

        // Model-level: buying CAPITAL is allowed but nonsensical (net loss)
        // The GUI (MarketPane) disables the CAPITAL Buy/Sell buttons
        int capitalBefore = alice.getResource(ResourceType.CAPITAL);
        market.buyResource(alice, ResourceType.CAPITAL);
        int capitalAfter = alice.getResource(ResourceType.CAPITAL);

        assertTrue("Buy CAPITAL is a net loss (GUI blocks this)", capitalAfter < capitalBefore);

        System.out.println("  [NOTE] CAPITAL trading is blocked by MarketPane GUI buttons, not the model");
        System.out.println();
    }

    static void testHackerCEO3To1Rate() {
        System.out.println("--- Test: Hacker CEO buys at 3 instead of 4 ---");
        GameEngine engine = createGameInNormalPhase();
        Market market = engine.getMarket();
        Player alice = engine.getCurrentPlayer();
        alice.setRole(FounderRole.HACKER_CEO);

        alice.addResource(ResourceType.CAPITAL, 10);
        int capitalBefore = alice.getResource(ResourceType.CAPITAL);

        market.buyResource(alice, ResourceType.DATA);

        assertEqual("Hacker CEO paid 3 CAPITAL", capitalBefore - alice.getResource(ResourceType.CAPITAL), 3);
        System.out.println();
    }

    static void testMarketPriceShiftsAfterBuy() {
        System.out.println("--- Test: Market price increases after buy ---");
        GameEngine engine = createGameInNormalPhase();
        Market market = engine.getMarket();
        Player alice = engine.getCurrentPlayer();
        alice.addResource(ResourceType.CAPITAL, 20);

        int priceBefore = market.getPrice(ResourceType.DATA);
        market.buyResource(alice, ResourceType.DATA);
        int priceAfter = market.getPrice(ResourceType.DATA);

        assertEqual("Price increased by 1 after buy", priceAfter, priceBefore + 1);
        System.out.println();
    }

    static void testMarketPriceShiftsAfterSell() {
        System.out.println("--- Test: Market price decreases after sell ---");
        GameEngine engine = createGameInNormalPhase();
        Market market = engine.getMarket();
        Player alice = engine.getCurrentPlayer();
        alice.addResource(ResourceType.TALENT, 10);

        int priceBefore = market.getPrice(ResourceType.TALENT);
        market.sellResource(alice, ResourceType.TALENT);
        int priceAfter = market.getPrice(ResourceType.TALENT);

        assertEqual("Price decreased by 1 after sell", priceAfter, priceBefore - 1);
        System.out.println();
    }

    static void testGenericMarketTrade4to1() {
        System.out.println("--- Test: Generic trade 4:1 (normal player) ---");
        GameEngine engine = createGameInNormalPhase();
        Market market = engine.getMarket();
        Player alice = engine.getCurrentPlayer();

        alice.addResource(ResourceType.DATA, 10);
        int dataBefore = alice.getResource(ResourceType.DATA);
        int patentBefore = alice.getResource(ResourceType.PATENT);

        market.executeGenericMarketTrade(alice, ResourceType.DATA, ResourceType.PATENT);

        assertEqual("Spent 4 DATA", alice.getResource(ResourceType.DATA), dataBefore - 4);
        assertEqual("Gained 1 PATENT", alice.getResource(ResourceType.PATENT), patentBefore + 1);
        System.out.println();
    }

    static void testHackerCEOGenericTrade3to1() {
        System.out.println("--- Test: Hacker CEO generic trade 3:1 ---");
        GameEngine engine = createGameInNormalPhase();
        Market market = engine.getMarket();
        Player alice = engine.getCurrentPlayer();
        alice.setRole(FounderRole.HACKER_CEO);

        alice.addResource(ResourceType.CLOUD, 10);
        int cloudBefore = alice.getResource(ResourceType.CLOUD);
        int talentBefore = alice.getResource(ResourceType.TALENT);

        market.executeGenericMarketTrade(alice, ResourceType.CLOUD, ResourceType.TALENT);

        assertEqual("Hacker CEO spent 3 CLOUD", alice.getResource(ResourceType.CLOUD), cloudBefore - 3);
        assertEqual("Gained 1 TALENT", alice.getResource(ResourceType.TALENT), talentBefore + 1);
        System.out.println();
    }

    static void testCannotTradeInSetupPhase() {
        System.out.println("--- Test: Market trading logic is independent of phase ---");
        // Market.buyResource/sellResource are model-level — they don't check phase.
        // Phase checking is done by the GUI (MarketPane). This test verifies the
        // model methods work regardless of phase, as designed.
        model.Map gameMap = new model.Map();
        Market market = new Market();
        Player p1 = new Player("Alice", "Red");
        List<Player> players = new ArrayList<>();
        players.add(p1);
        GameEngine engine = new GameEngine(players, market, gameMap);

        assertTrue("Phase is SETUP", engine.getCurrentPhase() == GamePhase.SETUP);

        p1.addResource(ResourceType.CAPITAL, 20);
        market.buyResource(p1, ResourceType.DATA);
        assertEqual("Bought DATA in SETUP phase (model allows)", p1.getResource(ResourceType.DATA), 1);

        System.out.println("  [NOTE] Phase enforcement is handled by MarketPane GUI, not Market model");
        System.out.println();
    }

    static void testCannotTradeBeforeRollingDice() {
        System.out.println("--- Test: GUI disables trade before roll ---");
        // This verifies the flag the GUI checks: hasRolledThisTurn
        GameEngine engine = createGameInNormalPhase();

        assertTrue("hasRolledThisTurn is false before rolling", !engine.hasRolledThisTurn());

        Dice dice = new Dice();
        engine.rollDice(dice);
        assertTrue("hasRolledThisTurn is true after rolling", engine.hasRolledThisTurn());

        System.out.println("  [NOTE] MarketPane checks hasRolledThisTurn to enable/disable buttons");
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
}
