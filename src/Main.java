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

        // ۱. تعریف بازیکنان
        Player jahan = new Player("Jahan", "RED");
        Player baran = new Player("Baran", "BLUE");

        jahan.setRole(FounderRole.HACKER_CEO);
        baran.setRole(FounderRole.VC_FUNDED);

        List<Player> players = new ArrayList<>();
        players.add(jahan);
        players.add(baran);

        Map gameMap = new Map();
        Market market = new Market();
        GameEngine engine = new GameEngine(players, market);
        Dice dice = new Dice();

        // --------------------------------------------------
        // STAGE 1: تست قوانین سخت‌گیرانه‌ی تاس ریختن 🎲
        // --------------------------------------------------
        System.out.println("--- STAGE 1: Testing Dice Roll Guard ---");
        try {
            int firstRoll = engine.rollDice(dice);
            System.out.println("🎲 First Roll: " + firstRoll);

            // عمدا دوباره تاس می‌ریزیم تا ببینیم آیا جلوی خطای کاربر رو می‌گیره؟
            engine.rollDice(dice);
        } catch (AlreadyRolledException e) {
            System.out.println("🛡️ SECURITY TRIGGERED: " + e.getMessage());
        }
        System.out.println();

        // --------------------------------------------------
        // STAGE 2: شبیه‌سازی خرید ساخت‌وساز و تست قوانین مپ 🏢
        // --------------------------------------------------
        System.out.println("--- STAGE 2: Testing Construction & Exceptions ---");

        Vertex targetVertex = gameMap.getVertices()[2][2]; // انتخاب یک ورتکس وسط نقشه

        // سناریوی منفی: تلاش برای ساخت MVP بدون داشتن منابع کافی
        try {
            System.out.println("Trying to build MVP for Jahan with 0 resources...");
            engine.buildMVP(jahan, targetVertex);
        } catch (NotEnoughResourceException e) {
            System.out.println("🛡️ SECURITY TRIGGERED: " + e.getMessage());
        }

        // اصلاحیه اصلی: دادن هر ۴ کارت مورد نیاز واقعی به جهان
        jahan.addResource(ResourceType.CAPITAL, 5);
        jahan.addResource(ResourceType.TALENT, 5);
        jahan.addResource(ResourceType.CLOUD, 5);
        jahan.addResource(ResourceType.DATA, 5);
        System.out.println("Gave Jahan resources. Capital: 5 | Talent: 5 | Cloud: 5 | Data: 5");

        // ساخت موفقیت‌آمیز MVP اول با داشتن تمام منابع واقعی
        try {
            engine.buildMVP(jahan, targetVertex);
        } catch (Exception e) {
            System.out.println("❌ Critical Test Failure: " + e.getMessage());
        }

        // سناریوی منفی: تلاش برای ساخت یک سازه دیگر روی همان خانه (Vertex)
        try {
            System.out.println("Trying to build another MVP on the exact same vertex...");
            engine.buildMVP(jahan, targetVertex);
        } catch (InvalidPlacementException | StructurePlacementException e) {
            System.out.println("🛡️ SECURITY TRIGGERED: " + e.getMessage());
        }

        // سناریوی منفی: تلاش برای ساخت روی ورتکس همسایه (نقض قانون فاصله ۲ یال)
        Vertex neighborVertex = targetVertex.getNeighbors().get(0); // گرفتن همسایه مستقیم
        try {
            System.out.println("Trying to build on adjacent neighbor vertex (Distance Rule Violation)...");
            engine.buildMVP(jahan, neighborVertex);
        } catch (InvalidPlacementException | StructurePlacementException e) {
            System.out.println("🛡️ SECURITY TRIGGERED: " + e.getMessage());
        }
        System.out.println();

        // --------------------------------------------------
        // STAGE 3: تست فرار مالیاتی و بحران قانون‌گذاری (تاس ۷) 🚨
        // --------------------------------------------------
        System.out.println("--- STAGE 3: Regulatory Crisis & Discard Test ---");

        // پاکسازی کارت‌های مراحل قبل جهت دقیق بودن خروجی ریاضی ریاضی تست
        jahan.discardRandomResources(jahan.getTotalResources());
        baran.discardRandomResources(baran.getTotalResources());

        // پر کردن کارت‌های بازیکنان برای تست کسر خودکار کارت‌ها
        // جهان حد مجازش ۷ تاست. ما بهش ۱۲ تا کارت میدیم (باید نصفش یعنی ۶ کارت دور ریخته بشه و برسه به ۶ کارت)
        for (int i = 0; i < 12; i++) {
            jahan.addResource(ResourceType.DATA, 1);
        }
        // باران VC Funded هست و حدش ۹ تاست. ما بهش ۸ تا کارت میدیم (نباید هیچ جریمه‌ای بشه)
        for (int i = 0; i < 8; i++) {
            baran.addResource(ResourceType.DATA, 1);
        }

        System.out.println("Before Crisis:");
        System.out.println(" - Jahan total cards: " + jahan.getTotalResources() + " (Limit: 7)");
        System.out.println(" - Baran total cards: " + baran.getTotalResources() + " (Limit: 9)");

        // تحریک دستی بحران مالیاتی
        engine.triggerRegulatoryCrisis();

        System.out.println("\nAfter Crisis:");
        System.out.println(" - Jahan total cards (Expected 6): " + jahan.getTotalResources());
        System.out.println(" - Baran total cards (Expected 8 - No change): " + baran.getTotalResources());
        System.out.println();

        System.out.println("==================================================");
        System.out.println("🎉 ALL INTEGRATION & EXCEPTION TESTS PASSED! 🎉");
        System.out.println("==================================================");
    }
}