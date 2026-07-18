package util;

import controller.GameEngine;
import model.Map;
import model.Player;
import controller.Market;
import util.SaveManager;
import java.util.ArrayList;
import java.util.List;

public class PersistenceTester {
    public static void main(String[] args) throws Exception {
        List<Player> players = new ArrayList<>();
        players.add(new Player("Jahan", "Red"));
        players.get(0).addResource(model.ResourceType.CAPITAL, 5);

        Map testMap = new Map();
        GameEngine engine = new GameEngine(players, new Market(), testMap);

        System.out.println("Saving game...");
        SaveManager.saveGame("test_save.dat", engine);

        System.out.println("Loading game...");
        SaveManager.loadGame("test_save.dat", new SaveManager.LoadGameCallback() {
            @Override
            public void onSuccess(GameEngine loadedEngine) {
                System.out.println("Success!");
                int res = loadedEngine.getCurrentPlayer().getResource(model.ResourceType.CAPITAL);
                if (res == 5) {
                    System.out.println("TEST PASSED: Loaded data matches original!");
                } else {
                    System.out.println("TEST FAILED: Data mismatch!");
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                System.out.println("Error: " + errorMessage);
            }
        });

        Thread.sleep(2000);
    }
}
