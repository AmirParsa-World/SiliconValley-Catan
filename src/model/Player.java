package model;

import controller.GameEngine;
import exception.NotEnoughResourceException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class Player implements Serializable {

    private final String name;
    private final String color;
    private FounderRole role;
    private final Map<ResourceType, Integer> wallet;
    private final List<Structure> structures;
    private boolean hasLongestNetwork;

    // transient avoids breaking save/load serialization
    private transient GameEngine engine;

    private static final long serialVersionUID = 1L;

    public GameEngine getEngine() {
        return this.engine;
    }

    public void setEngine(GameEngine engine) {
        this.engine = engine;
    }

    public Player(String name, String color) {
        this.name = name;
        this.color = color;
        this.role = null;
        this.structures = new ArrayList<>();
        this.hasLongestNetwork = false;

        // Init wallet with 0 for all resources
        this.wallet = new EnumMap<>(ResourceType.class);
        for (ResourceType type : ResourceType.values()) {
            this.wallet.put(type, 0);
        }
    }

    public String getName() { return name; }
    public String getColor() { return color; }
    public List<Structure> getStructures() { return structures; }
    public Map<ResourceType, Integer> getWallet() { return wallet; }

    public int getResource(ResourceType type) {
        return wallet.getOrDefault(type, 0);
    }

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

    // Dynamic Victory Point calculation
    public int countPlayerPoint() {
        int points = 0;

        // 1. Structure points
        if (this.structures != null) {
            for (Structure structure : this.structures) {
                points += structure.getPoint();
            }
        }

        // 2. Longest network bonus
        if (this.hasLongestNetwork) {
            points += 2;
        }

        // Perk balancing: choosing a role costs 1 VP
        if (this.role != null && this.role != FounderRole.NONE) {
            points -= 1;
        }

        return points;
    }

    @Override
    public String toString() {
        return name + " (" + color + ") | Points: " + countPlayerPoint() + " | Total Resources: " + getTotalResources();
    }

    public FounderRole getRole() { return this.role; }

    public void setRole(FounderRole role) { this.role = role; }

    // Drops random cards for regulatory audits (dice 7)
    public void discardRandomResources(int amount) {
        int discarded = 0;
        while (discarded < amount && getTotalResources() > 0) {
            for (ResourceType type : ResourceType.values()) {
                if (getResource(type) > 0) {
                    spendResource(type, 1);
                    discarded++;
                    if (discarded == amount) break;
                }
            }
        }
    }

    public void setHasLongestNetwork(boolean hasLongestNetwork) {
        this.hasLongestNetwork = hasLongestNetwork;
    }

    public boolean isHasLongestNetwork() {
        return this.hasLongestNetwork;
    }
}