package util; // یا هر پکیجی که دوست داری مثل controller

import controller.GameEngine;
import java.io.*;

public class SaveManager {

    // ۱. متد ذخیره ناهمگام بازی
    public static void saveGameAsync(String filePath, GameEngine engine) {
        // ایجاد یک ترد مستقل تا ترد اصلی UI (کدهای باران) فریز نشود
        new Thread(() -> {
            // استفاده از ساختار try-with-resources برای بستن خودکار استریم‌ها
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {

                // نوشتن کل درخت اشیاء موتور بازی روی هارد
                oos.writeObject(engine);
                System.out.println("💾 Game saved successfully by background thread!");

            } catch (IOException e) {
                System.err.println("❌ Failed to save game: " + e.getMessage());
            }
        }).start(); // استارت زدن ترد در پس‌زمینه
    }

    // ۲. متد لود ناهمگام بازی
    public static void loadGameAsync(String filePath, LoadGameCallback callback) {
        new Thread(() -> {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {

                // خواندن بایت‌ها و تبدیل مجدد آن‌ها به شیء واقعی گیم‌انجين
                GameEngine loadedEngine = (GameEngine) ois.readObject();

                // شلیک خبر موفقیت به کنترلر اصلی بازی
                callback.onSuccess(loadedEngine);

            } catch (FileNotFoundException e) {
                callback.onFailure("❌ فایل ذخیره پیدا نشد!");
            } catch (IOException | ClassNotFoundException e) {
                callback.onFailure("❌ فایل ذخیره خراب است یا ساختار کلس‌ها تغییر کرده!");
            }
        }).start();
    }

    // ۳. اینترفیس واکنشی (Callback) برای انتقال امن دیتا به بخش گرافیک
    public interface LoadGameCallback {
        void onSuccess(GameEngine loadedEngine);
        void onFailure(String errorMessage);
    }


}