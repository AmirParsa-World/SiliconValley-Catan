package util;

import controller.GameEngine;
import controller.Market;
import model.*;
import java.util.ArrayList;
import java.util.List;

public class GameLogTester {
    public static void main(String[] args) {
        System.out.println("=== 🎮 Starting Game Log Test ===\n");

        // ۱. ساخت بازیکنان تستی
        List<Player> players = new ArrayList<>();
        Player jahan = new Player("Jahan", "Red");
        Player baran = new Player("Baran", "Blue");
        players.add(jahan);
        players.add(baran);

        GameEngine engine = new GameEngine(players, new Market());

        // ۲. شبیه‌سازی فاز SETUP (رفت و برگشت نوبت‌ها به صورت اسنیک درفت)
        engine.log("Game initialized with " + players.size() + " players.");

        // نوبت اول: جهان کارش را می‌کند و پاس می‌دهد به باران
        engine.nextTurn();

        // نوبت دوم: باران کارش را می‌کند و پاس می‌دهد (چون اسنیک درفت است، دوباره نوبت باران می‌شود)
        engine.nextTurn();

        // نوبت سوم: باران دوباره بازی می‌کند و نوبت را می‌دهد به جهان
        engine.nextTurn();

        // نوبت چهارم: جهان بازی می‌کند و با زدن nextTurn فاز بازی NORMAL می‌شود!
        engine.nextTurn();

        // ۳. شبیه‌سازی کارهای فاز عادی (تجارت بین دو بازیکن)
        // ابتدا کمی منابع به هر دو می‌دهیم تا پول داشته باشند!
        jahan.addResource(ResourceType.DATA, 5);
        baran.addResource(ResourceType.CLOUD, 3);

        // اجرای تجارت (جهان ۲ دیتا می‌دهد و در عوض ۲ کلاود از باران می‌گیرد)
        engine.executePeerTrade(jahan, baran, ResourceType.DATA, 2, ResourceType.CLOUD, 2);

        // ۴. خروجی گرفتن از تمام لاگ‌های ذخیره شده برای باران (UI)
        System.out.println("\n=== 📜 EXTRACTED GAME LOGS (For Baran's UI) ===");
        List<String> logs = engine.getGameLog();
        for (String logEntry : logs) {
            System.out.println(logEntry);
        }
        System.out.println("=============================================");
    }
}