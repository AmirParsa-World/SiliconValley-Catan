package model;

import exception.NotEnoughResourceException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class Player {

    private final String name;
    private final String color;
    private FounderRole role;
    private final Map<ResourceType, Integer> wallet;
    private final List<Structure> structures;

    public Player(String name, String color) {
        this.name = name;
        this.color = color;
        this.role = null;
        this.structures = new ArrayList<>();

        // Enum map: baran
        this.wallet = new EnumMap<>(ResourceType.class);

        for (ResourceType type : ResourceType.values()) {
            this.wallet.put(type, 0);
        }
    }

    // getter methods: world
    public String getName() { return name; }
    public String getColor() { return color; }
    public List<Structure> getStructures() { return structures; }
    public Map<ResourceType, Integer> getWallet() { return wallet; }

    // Some other good methods: baran
    public int getResource(ResourceType type) {
        return wallet.getOrDefault(type, 0);
    }

    // baran
    public int getTotalResources() {
        int total = 0;
        for (int amount : wallet.values()) {
            total += amount;
        }
        return total;
    }

    public void addResource(ResourceType type, int amount) {
        int currentAmount = wallet.getOrDefault(type, 0);
        this.wallet.put(type, amount + currentAmount);
    }

    public void spendResource(ResourceType type, int amount) {
        if (wallet.getOrDefault(type, 0) < amount) {
            throw new NotEnoughResourceException("Dear player, you don't have enough " + type + " resource.");
        }
        this.wallet.put(type, wallet.get(type) - amount);
    }

    public void addStructure(Structure structure) {
        structures.add(structure);
    }

    // world
    public int countPlayerPoint() {
        int totalPoint = (this.role != null) ? -1 : 0;
        for (Structure structure : this.structures) {
            totalPoint += structure.getPoint();
        }
        return totalPoint;
    }

    @Override
    public String toString() {
        return name + " (" + color + ") | Points: " + countPlayerPoint() + " | Total Resources: " + getTotalResources();
    }
}