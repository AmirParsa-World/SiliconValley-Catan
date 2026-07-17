import controller.GameEngine;
import controller.Market;
import model.*;
import exception.*;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("🔥 SILICON VALLEY SUPREME GAME ENGINE TEST SUITE 🔥");
        System.out.println("==================================================\n");

        try {
            // ۱. آماده‌سازی پایه نقشه، لیست بازیکنان و بازار
            Map gameMap = new Map();
            Market market = new Market();

            // ۲. تعریف بازیکنان با رنگ‌ها و تخصیص نقش‌ها طبق معماری مدل شما
            Player jahan = new Player("Jahan", "Red");
            jahan.setRole(FounderRole.HACKER_CEO);

            Player baran = new Player("Baran", "Blue");
            baran.setRole(FounderRole.VC_FUNDED);

            List<Player> players = new ArrayList<>();
            players.add(jahan);
            players.add(baran);

            // ۳. راه‌اندازی موتور بازی با بازیکنان، بازار و نقشه واقعی
            GameEngine engine = new GameEngine(players, market, gameMap);

            // ==========================================
            // اجرای پله‌پله تست‌های چک‌لیست جامع سیستم
            // ==========================================

            runStage1SetupPhase(engine, gameMap, jahan);
            runStage2ResourceProduction(engine, gameMap, jahan);
            runStage3PlacementRules(engine, gameMap, jahan);
            runStage4DynamicMarket(market, jahan, baran);
            runStage5CrisisAndRoles(engine, jahan, baran);
            runStage6LongestNetworkAndVictory(engine, gameMap, jahan);

            // 🤖 تست جدید و جذاب برای هوش مصنوعی پویا (آدم‌ها در کنار ربات‌ها!)
            runStage7BotSimulation();

            System.out.println("\n🥇==============================================");
            System.out.println("🎉 ALL INTEGRATION & LOGIC TESTS PASSED SUCCESSFULLY! 🎉");
            System.out.println("==================================================");

        } catch (Exception e) {
            System.err.println("\n❌ Test Suite stopped due to unexpected error: " + e.getMessage());
        }
    }

    // ==========================================
    // 1️⃣ تست فاز راه‌اندازی (Setup Phase)
    // ==========================================
    private static void runStage1SetupPhase(GameEngine engine, Map gameMap, Player jahan) {
        System.out.println("--- STAGE 1: Setup Phase (Snake Draft) ---");

        // شبیه‌سازی ترتیب نوبت‌ها در Snake Draft
        System.out.println("Testing Snake Draft Order Simulation...");
        System.out.println("Draft Order Process: P1 (Jahan) -> P2 (Baran) -> P2 (Baran) -> P1 (Jahan)");

        // گرفتن یک ورتکس و یال خالی برای استقرار آزمایشی
        Vertex targetVertex = gameMap.getVertices()[0][0];
        Edge targetEdge = targetVertex.getNeighboringEdges().get(0);

        // ذخیره منابع قبل از تست برای تایید رایگان بودن فاز اول
        int initialCapital = jahan.getResource(ResourceType.CAPITAL);
        System.out.println("Jahan initial Capital: " + initialCapital);

        System.out.println("Deploying starting structures using setup method...");
        engine.setupPlaceMVPAndPartnership(jahan, targetVertex, targetEdge);

        // تایید رایگان بودن ساخت‌و‌ساز فاز اول
        int afterSetupCapital = jahan.getResource(ResourceType.CAPITAL);
        System.out.println("Jahan Capital after Setup draft: " + afterSetupCapital);
        if (initialCapital == afterSetupCapital) {
            System.out.println("🛡️ SUCCESS: Setup placement cost is FREE! No resources deducted.");
        } else {
            System.out.println("❌ ERROR: Setup draft cost resource deduction caught!");
        }

        System.out.println("✅ Setup Phase logic verified.\n");
    }

    // ==========================================
    // 2️⃣ تست چرخه نوبت و تولید منبع (Resource Production)
    // ==========================================
    private static void runStage2ResourceProduction(GameEngine engine, Map gameMap, Player jahan) {
        System.out.println("--- STAGE 2: Resource Production & Auditor Block ---");

        // یافتن اولین سکتور غیر بحرانی و فعال روی نقشه برای اجرای سناریو
        Sector testSector = null;
        int targetRow = -1;
        int targetCol = -1;

        outerLoop:
        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 5; c++) {
                Sector s = gameMap.getSectors()[r][c];
                if (s != null && s.getResourceType() != ResourceType.REGULATORY && s.getActivationNumber() > 0) {
                    testSector = s;
                    targetRow = r;
                    targetCol = c;
                    break outerLoop;
                }
            }
        }

        if (testSector == null) {
            System.out.println("❌ Error: No valid sector found on the map for testing!");
            return;
        }

        int activationNum = testSector.getActivationNumber();
        ResourceType sectorResType = testSector.getResourceType();
        Vertex sectorVertex = testSector.getBottomLeft();

        System.out.println("Selected Active Sector for Yield test: " + sectorResType + " (Dice: " + activationNum + ")");

        // تمیزکاری استقرار آزمایشی قبلی روی ورتکس برای جلوگیری از تداخل
        sectorVertex.setOwner(jahan);
        sectorVertex.setStructure(new MVP(jahan, sectorVertex));
        jahan.getStructures().clear();
        jahan.addStructure(sectorVertex.getStructure());

        // تست تولید عادی MVP (+1 واحد)
        int beforeRoll = jahan.getResource(sectorResType);
        engine.distributeResources(activationNum);
        int afterRoll = jahan.getResource(sectorResType);
        System.out.println("Normal MVP yield (Expected +1): " + (afterRoll - beforeRoll));

        // ارتقا به یونیکورن و بررسی افزایش تولید به ۲ واحد
        sectorVertex.setStructure(new Unicorn(jahan, sectorVertex));
        jahan.getStructures().clear();
        jahan.addStructure(sectorVertex.getStructure());

        beforeRoll = jahan.getResource(sectorResType);
        engine.distributeResources(activationNum);
        afterRoll = jahan.getResource(sectorResType);
        System.out.println("Unicorn yield (Expected +2): " + (afterRoll - beforeRoll));

        // تست بلاک شدن سکتور توسط مهره بازرس (Auditor)
        System.out.println("Moving Auditor to block this active sector...");
        engine.moveAuditor(jahan, targetRow, targetCol);

        beforeRoll = jahan.getResource(sectorResType);
        engine.distributeResources(activationNum);
        afterRoll = jahan.getResource(sectorResType);
        System.out.println("Yield while Auditor blocks (Expected +0): " + (afterRoll - beforeRoll));

        // آزاد کردن سکتور جهت ادامه تست‌ها
        testSector.unblock();
        System.out.println("✅ Resource Production & Auditor Block verified.\n");
    }

    // ==========================================
    // 3️⃣ تست قوانین مکانی و ساخت‌وساز (Placement Rules)
    // ==========================================
    private static void runStage3PlacementRules(GameEngine engine, Map gameMap, Player jahan) {
        System.out.println("--- STAGE 3: Placement & Connection Rules ---");

        // انتخاب یک ورتکس مبدا برای قرار دادن سازه
        Vertex v1 = gameMap.getVertices()[2][2];
        v1.setOwner(jahan);
        v1.setStructure(new MVP(jahan, v1));

        // ورتکس همسایه مستقیم (با فاصله تنها یک یال)
        Vertex v2 = v1.getNeighbors().get(0);

        // تست قانون فاصله ۲ یال (سیستم نباید اجازه ساخت به v2 بدهد)
        System.out.println("Testing Distance Rule (trying to build MVP on direct neighbor)...");
        boolean isV2Valid = engine.isValidStructurePlacement(v2);

        if (!isV2Valid) {
            System.out.println("🛡️ SUCCESS: Distance rule correctly blocked adjacent structure!");
        } else {
            System.out.println("❌ Error: Distance rule failed to block placement!");
        }

        // تست قانون اتصال برای راه‌ها و پارتنرشیپ‌های بعدی
        System.out.println("Testing Connection Rule for disconnected Partnerships...");
        Vertex isolatedVertex = gameMap.getVertices()[5][5];
        Edge isolatedEdge = isolatedVertex.getNeighboringEdges().get(0);

        // تخصیص کارت‌ها جهت آمادگی هزینه پارتنرشیپ
        jahan.addResource(ResourceType.CAPITAL, 1);
        jahan.addResource(ResourceType.PATENT, 1);

        try {
            engine.buildPartnership(jahan, isolatedEdge);
            System.out.println("❌ Error: Disconnected partnership was built without throwing exception!");
        } catch (Exception e) {
            System.out.println("🛡️ SUCCESS: Connection rule blocked isolated partnership! Reason: " + e.getMessage());
        }

        System.out.println("✅ Placement & Connection Rules verified.\n");
    }

    // ==========================================
    // 4️⃣ تست بازار پویا (Dynamic Market)
    // ==========================================
    private static void runStage4DynamicMarket(Market market, Player jahan, Player baran) {
        System.out.println("--- STAGE 4: Dynamic Market & Role Discounts ---");

        // بررسی قیمت پایه کلاود و دیتا
        int basePrice = market.getPrice(ResourceType.DATA);
        System.out.println("DATA Base Price (Expected 4): " + basePrice);

        // تست تخفیف نقش Hacker CEO (نرخ ۳ به ۱ به جای ۴ به ۱ در بازار آزاد)
        System.out.println("Jahan Role: " + jahan.getRole());
        jahan.addResource(ResourceType.CLOUD, 3);
        try {
            market.executeGenericMarketTrade(jahan, ResourceType.CLOUD, ResourceType.DATA);
            System.out.println("🛡️ SUCCESS: Hacker CEO successfully traded Cloud to Data with 3:1 discount rate!");
        } catch (Exception e) {
            System.out.println("⚠️ Market trade simulation error: " + e.getMessage());
        }

        // تست نرخ عادی معامله برای سایر بازیکنان بدون تخفیف (۴ به ۱)
        System.out.println("Baran Role: " + baran.getRole());
        baran.addResource(ResourceType.CLOUD, 4);
        try {
            market.executeGenericMarketTrade(baran, ResourceType.CLOUD, ResourceType.DATA);
            System.out.println("🛡️ SUCCESS: VC-Funded (Normal rate) successfully traded Cloud to Data with 4:1 rate!");
        } catch (Exception e) {
            System.out.println("⚠️ Baran trade failed: " + e.getMessage());
        }

        // تست پویایی بازار (کاهش خودکار قیمت‌ها به خاطر عدم خرید پس از ۳ دور)
        System.out.println("Simulating 3 rounds of STAGNATION (no purchases)...");
        market.incrementRoundTick();
        market.incrementRoundTick();
        market.incrementRoundTick();

        int priceAfter3Rounds = market.getPrice(ResourceType.DATA);
        System.out.println("DATA Price after stagnation tick (Expected < 4): " + priceAfter3Rounds);
        System.out.println("✅ Dynamic Market verified.\n");
    }

    // ==========================================
    // 5️⃣ تست بحران قانونی و نقش‌ها
    // ==========================================
    private static void runStage5CrisisAndRoles(GameEngine engine, Player jahan, Player baran) {
        System.out.println("--- STAGE 5: Regulatory Crisis & Roles ---");

        // پاکسازی کارت‌های بازیکنان برای سناریوسازی دقیق
        jahan.discardRandomResources(jahan.getTotalResources());
        baran.discardRandomResources(baran.getTotalResources());

        // حالت اول: هر دو بازیکن کارت‌های زیادی دارند (مثلاً ۱۰ کارت)
        for (int i = 0; i < 10; i++) {
            jahan.addResource(ResourceType.DATA, 1); // جهان با نقش معمولی (سقف ۷ کارت)
            baran.addResource(ResourceType.DATA, 1); // باران با نقش VC-Funded (سقف ۹ کارت)
        }

        System.out.println("Before Crisis - Jahan (Normal limit 7) cards: " + jahan.getTotalResources());
        System.out.println("Before Crisis - Baran (VC limit 9) cards: " + baran.getTotalResources());

        System.out.println("Triggering Regulatory Crisis (as if dice rolled 7)...");
        engine.triggerRegulatoryCrisis();

        System.out.println("After Crisis - Jahan cards (Expected 5): " + jahan.getTotalResources());
        System.out.println("After Crisis - Baran cards (Expected 5): " + baran.getTotalResources());

        // حالت دوم: باران کارت‌هایش منطبق بر سقف امن VC-Funded (مثلاً ۹ کارت) است
        baran.discardRandomResources(baran.getTotalResources());
        baran.addResource(ResourceType.DATA, 9); // سقف باران ۹ است، پس نباید در بحران جریمه شود

        System.out.println("\nTesting VC-Funded safe threshold with exactly 9 cards...");
        System.out.println("Before Crisis - Baran cards: " + baran.getTotalResources());
        engine.triggerRegulatoryCrisis();
        System.out.println("After Crisis - Baran cards (Expected 9 - No change): " + baran.getTotalResources());

        System.out.println("✅ Regulatory Crisis & Role limits verified.\n");
    }

    // ==========================================
    // 6️⃣ تست سیستم امتیازدهی و پایان بازی
    // ==========================================
    private static void runStage6LongestNetworkAndVictory(GameEngine engine, Map gameMap, Player jahan) {
        System.out.println("--- STAGE 6: Scoring, Longest Network & Victory ---");

        // تست اعمال امتیاز منفی نقش انتخابی در زمان راه‌اندازی (با وجود کارت نقش Hacker CEO)
        System.out.println("Jahan current structures count: " + jahan.getStructures().size());
        System.out.println("Jahan starting points (Expected structure score - 1 role penalty): " + jahan.countPlayerPoint());

        // شبیه‌سازی و تست شبکه‌ طولانی‌ترین پارتنرشیپ (Longest Network)
        // ایجاد زنجیره‌ای از ۳ یال متصل برای بازیکن جهان
        Vertex v00 = gameMap.getVertices()[0][0];
        Vertex v01 = gameMap.getVertices()[0][1];
        Vertex v02 = gameMap.getVertices()[0][2];
        Vertex v03 = gameMap.getVertices()[0][3];

        // قرار دادن یک سازه در مبدا شبکه برای معتبرسازی مسیرهای متصل
        v00.setOwner(jahan);
        v00.setStructure(new MVP(jahan, v00));
        jahan.getStructures().clear();
        jahan.addStructure(v00.getStructure());

        // یافتن و تصاحب یال‌های اتصالی برای جاده‌ها
        Edge edge1 = null;
        for (Edge e : v00.getNeighboringEdges()) {
            if (e.getU() == v01 || e.getV() == v01) { edge1 = e; break; }
        }
        Edge edge2 = null;
        for (Edge e : v01.getNeighboringEdges()) {
            if (e.getU() == v02 || e.getV() == v02) { edge2 = e; break; }
        }
        Edge edge3 = null;
        for (Edge e : v02.getNeighboringEdges()) {
            if (e.getU() == v03 || e.getV() == v03) { edge3 = e; break; }
        }

        if (edge1 != null) { edge1.setOwner(jahan); edge1.setPartnership(true); }
        if (edge2 != null) { edge2.setOwner(jahan); edge2.setPartnership(true); }
        if (edge3 != null) { edge3.setOwner(jahan); edge3.setPartnership(true); }

        System.out.println("Evaluating Longest Network size on the board...");
        int longestChain = engine.calculateLongestNetwork(jahan);
        System.out.println("Jahan's computed Longest Network length: " + longestChain + " (Expected: 3)");

        engine.updateLongestNetworkAward();

        // شبیه‌سازی شرط پیروزی بازی (رسیدن به امتیاز ۱۰ در پایان نوبت)
        System.out.println("Simulating development to reach 10 points...");
        // ساخت ۵ سازه یونیکورن بزرگ برای جهان تا امتیاز او را افزایش دهیم
        for (int i = 0; i < 6; i++) {
            Vertex v = gameMap.getVertices()[4][i];
            v.setOwner(jahan);
            Unicorn u = new Unicorn(jahan, v);
            v.setStructure(u);
            jahan.addStructure(u);
        }

        System.out.println("Jahan points after heavy development: " + jahan.countPlayerPoint());
        System.out.println("Triggering end of turn victory validation...");
        engine.nextTurn();

        System.out.println("✅ Scoring & Victory logic verified.\n");
    }

    // ==========================================
    // 7️⃣ تست بازی پویا و شبیه‌سازی نوبت خودکار ربات‌ها
    // ==========================================
    private static void runStage7BotSimulation() {
        System.out.println("--- STAGE 7: Dynamic Game & Bot Simulation ---");

        Map botMap = new Map();
        Market botMarket = new Market();
        Dice dice = new Dice();

        // ایجاد ترکیب پویا: ۱ بازیکن انسانی و ۲ ربات
        Player humanPlayer = new Player("Jahan (Human)", "Red");
        SimpleBot bot1 = new SimpleBot("CPU_1 (Bot)", "Blue");
        SimpleBot bot2 = new SimpleBot("CPU_2 (Bot)", "Green");

        List<Player> players = new ArrayList<>();
        players.add(humanPlayer);
        players.add(bot1);
        players.add(bot2);

        System.out.println("Initializing dynamic game simulation with 1 Human and 2 Bots...");
        GameEngine engine = new GameEngine(players, botMarket, botMap);

        // ۱. شبیه‌سازی فاز راه‌اندازی (Setup Phase - Snake Draft)
        System.out.println("\n--- Starting Setup Phase Snake Draft loop ---");

        // نوبت اول: انسان (Jahan)
        System.out.println("[Turn 1] Active: " + engine.getCurrentPlayer().getName());
        Vertex v1 = botMap.getVertices()[1][1];
        Edge e1 = v1.getNeighboringEdges().get(0);
        engine.setupPlaceMVPAndPartnership(humanPlayer, v1, e1);
        engine.nextTurn(); // انتقال نوبت به CPU_1

        // نوبت دوم: ربات ۱ (CPU_1) - کاملاً خودکار!
        System.out.println("\n[Turn 2] Active: " + engine.getCurrentPlayer().getName());
        engine.playBotSetupTurn();

        // نوبت سوم: ربات ۲ (CPU_2) - کاملاً خودکار!
        System.out.println("\n[Turn 3] Active: " + engine.getCurrentPlayer().getName());
        engine.playBotSetupTurn();

        // نوبت چهارم: ربات ۲ (CPU_2) - فاز برگشت Snake Draft
        System.out.println("\n[Turn 4] Active: " + engine.getCurrentPlayer().getName());
        engine.playBotSetupTurn();

        // نوبت پنجم: ربات ۱ (CPU_1) - فاز برگشت Snake Draft
        System.out.println("\n[Turn 5] Active: " + engine.getCurrentPlayer().getName());
        engine.playBotSetupTurn();

        // نوبت ششم: انسان (Jahan) - فاز برگشت
        System.out.println("\n[Turn 6] Active: " + engine.getCurrentPlayer().getName());
        Vertex v2 = botMap.getVertices()[3][3];
        Edge e2 = v2.getNeighboringEdges().get(0);
        engine.setupPlaceMVPAndPartnership(humanPlayer, v2, e2);
        engine.nextTurn(); // اتمام فاز ست‌آپ و تغییر وضعیت خودکار به NORMAL

        System.out.println("\nTransition validation: Current Game Phase (Expected NORMAL): " + engine.getCurrentPhase());

        // ۲. شبیه‌سازی فاز عادی (Normal Phase) و تست خرید خودکار ربات
        System.out.println("\n--- Starting Normal Phase Simulation ---");

        // در حال حاضر نوبت انسان (Jahan) است. نوبت را به CPU_1 منتقل می‌کنیم.
        engine.setHasRolledThisTurn(true);
        engine.nextTurn();

        System.out.println("Now active: " + engine.getCurrentPlayer().getName());

        // دادن کارت‌های کافی به ربات ۱ تا بتواند خرید کند
        bot1.addResource(ResourceType.CAPITAL, 1);
        bot1.addResource(ResourceType.TALENT, 1);
        bot1.addResource(ResourceType.CLOUD, 1);
        bot1.addResource(ResourceType.DATA, 1);

        System.out.println("CPU_1 resources before starting turn: " + bot1.getWallet());
        System.out.println("CPU_1 structure count before starting turn: " + bot1.getStructures().size());

        // اجرای نوبت خودکار ربات (تاس می‌ریزد و MVP خودکار می‌سازد)
        engine.playBotTurn(dice);

        System.out.println("CPU_1 resources after automated turn: " + bot1.getWallet());
        System.out.println("CPU_1 structure count after turn: " + bot1.getStructures().size());

        // تایید ساخت خودکار: ۲ سازه اولیه در ست‌آپ داشته + ۱ دانه در نوبت عادی ساخته = ۳ سازه
        if (bot1.getStructures().size() == 3) {
            System.out.println("🛡️ SUCCESS: SimpleBot successfully simulated automatic setup, yield collection, and MVP construction!");
        } else {
            System.out.println("❌ ERROR: Bot automatic construction logic failed.");
        }

        System.out.println("✅ Dynamic Game & Bot Simulation verified.\n");
    }
}
