package util;

import controller.GameEngine;
import controller.Market;
import model.*;
import util.SaveManager;
import java.util.ArrayList;
import java.util.List;

public class DeepPersistenceTester {
    public static void main(String[] args) throws Exception {
        List<Player> players = new ArrayList<>();
        Player p1 = new Player("Jahan", "Red");
        p1.addResource(ResourceType.DATA, 10);
        players.add(p1);

        Map testMap = new Map();
        GameEngine engine = new GameEngine(players, new Market(), testMap);

        engine.nextTurn();
        p1.addResource(ResourceType.CLOUD, 5);
        p1.setHasLongestNetwork(true);

        System.out.println("Saving with complex state...");
        SaveManager.saveGame("deep_test.dat", engine);

        System.out.println("Loading...");
        SaveManager.loadGame("deep_test.dat", new SaveManager.LoadGameCallback() {
            @Override
            public void onSuccess(GameEngine loadedEngine) {
                Player loadedPlayer = loadedEngine.getCurrentPlayer();
                boolean passed = true;

                if (loadedPlayer.getResource(ResourceType.DATA) != 10) passed = false;
                if (loadedPlayer.getResource(ResourceType.CLOUD) != 5) passed = false;
                if (!loadedPlayer.isHasLongestNetwork()) passed = false;

                if (passed) {
                    System.out.println("EXTREME SUCCESS: Deep state preserved perfectly!");
                } else {
                    System.out.println("ERROR: State data lost or corrupted.");
                }
            }

            @Override
            public void onFailure(String error) {
                System.out.println("Critical Load Error: " + error);
            }
        });

        Thread.sleep(2000);
    }
}
