package util;

import controller.GameEngine;
import model.Player;
import controller.Market;
import util.SaveManager;
import java.util.ArrayList;
import java.util.List;

public class PersistenceTester {
    public static void main(String[] args) throws InterruptedException {
        // ۱. ساخت یک انجین واقعی با داده‌های تست
        List<Player> players = new ArrayList<>();
        players.add(new Player("Jahan", "Red"));
        players.get(0).addResource(model.ResourceType.CAPITAL, 5);

        GameEngine originalEngine = new GameEngine(players, new Market());

        System.out.println("🚀 Saving game...");
        SaveManager.saveGameAsync("test_save.dat", originalEngine);

        // صبر کردن برای اینکه ترد پس‌زمینه کارش را تمام کند (مخصوص تست)
        Thread.sleep(1000);

        System.out.println("📂 Loading game...");
        SaveManager.loadGameAsync("test_save.dat", new SaveManager.LoadGameCallback() {
            @Override
            public void onSuccess(GameEngine loadedEngine) {
                System.out.println("✅ Success!");
                // تست تطابق: چک می‌کنیم آیا منابع بازیکن درست لود شد؟
                int res = loadedEngine.getCurrentPlayer().getResource(model.ResourceType.CAPITAL);
                if (res == 5) {
                    System.out.println("🏆 TEST PASSED: Loaded data matches original!");
                } else {
                    System.out.println("❌ TEST FAILED: Data mismatch!");
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                System.out.println("❌ Error: " + errorMessage);
            }
        });
    }
}