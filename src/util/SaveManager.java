package util;

import controller.GameEngine;
import javafx.application.Platform;
import java.io.*;

public class SaveManager {

    public static void saveGame(String filePath, GameEngine engine) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(engine);
            System.out.println("Game saved successfully!");
        }
    }

    public static void loadGame(String filePath, LoadGameCallback callback) {
        new Thread(() -> {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
                GameEngine loadedEngine = (GameEngine) ois.readObject();
                runOnFXThread(() -> callback.onSuccess(loadedEngine));
            } catch (FileNotFoundException e) {
                runOnFXThread(() -> callback.onFailure("Save file not found!"));
            } catch (IOException | ClassNotFoundException e) {
                runOnFXThread(() -> callback.onFailure("Save file is corrupted or class structure has changed!"));
            }
        }).start();
    }

    public static GameEngine loadGameSync(String filePath) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            return (GameEngine) ois.readObject();
        }
    }

    private static void runOnFXThread(Runnable action) {
        try {
            Platform.runLater(action);
        } catch (IllegalStateException e) {
            // JavaFX toolkit not initialized (e.g. running in console tester) — run directly
            action.run();
        }
    }

    public interface LoadGameCallback {
        void onSuccess(GameEngine loadedEngine);
        void onFailure(String errorMessage);
    }
}
