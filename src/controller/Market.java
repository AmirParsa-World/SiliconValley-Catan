package controller;

import exception.NotEnoughResourceException;
import model.Player;
import model.ResourceType;
import model.FounderRole;

import java.io.Serializable;
import java.util.EnumMap;
import java.util.Map;

public class Market implements Serializable {
    private static final int INITIAL_PRICE = 4;
    private static final int MIN_PRICE = 2;
    private static final int MAX_PRICE = 6;
    private static final int STAGNATION_LIMIT = 3;

    private final Map<ResourceType, Integer> currentPrices;
    private final Map<ResourceType, Integer> stagnationTrackers;

    private static final long serialVersionUID = 1L;

    public Market() {
        this.currentPrices = new EnumMap<>(ResourceType.class);
        this.stagnationTrackers = new EnumMap<>(ResourceType.class);

        for (ResourceType type : ResourceType.values()) {
            if (type != ResourceType.REGULATORY) {
                currentPrices.put(type, INITIAL_PRICE);
                stagnationTrackers.put(type, 0);
            }
        }
    }

    // Legacy support for older test suites
    public void buyResource(Player player, ResourceType resource) {
        buyResource(player, resource, player.getEngine());
    }

    // Legacy support for older test suites
    public void sellResource(Player player, ResourceType resource) {
        sellResource(player, resource, player.getEngine());
    }

    // Buy asset: pumps price, resets stagnation clock
    public void buyResource(Player player, ResourceType resource, GameEngine engine) {
        validateResource(resource);

        int pricePaid = (player.getRole() == FounderRole.HACKER_CEO) ? 3 : currentPrices.get(resource);

        player.spendResource(ResourceType.CAPITAL, pricePaid);
        player.addResource(resource, 1);

        adjustPrice(resource, 1);
        stagnationTrackers.put(resource, 0);

        if (engine != null) {
            engine.log(player.getName() + " traded: bought 1 " + resource.getDisplayName());
        }
    }

    // Sell asset: dumps price, resets stagnation clock
    public void sellResource(Player player, ResourceType resource, GameEngine engine) {
        validateResource(resource);
        int currentMarketPrice = currentPrices.get(resource);

        player.spendResource(resource, 1);
        player.addResource(ResourceType.CAPITAL, currentMarketPrice);

        adjustPrice(resource, -1);
        stagnationTrackers.put(resource, 0);

        if (engine != null) {
            engine.log(player.getName() + " traded: sold 1 " + resource.getDisplayName());
        }
    }

    // Stagnation clock: drops prices if resources sit dead for 3 rounds
    public void incrementRoundTick() {
        for (ResourceType resource : currentPrices.keySet()) {
            int consecutiveQuietRounds = stagnationTrackers.get(resource) + 1;
            stagnationTrackers.put(resource, consecutiveQuietRounds);

            if (consecutiveQuietRounds >= STAGNATION_LIMIT) {
                adjustPrice(resource, -1);
                stagnationTrackers.put(resource, 0);
                System.out.println("⚠️ MARKET ALERT: " + resource + " price dropped due to 3 rounds of stagnation!");
            }
        }
    }

    private void adjustPrice(ResourceType resource, int change) {
        int oldPrice = currentPrices.get(resource);
        int newPrice = Math.max(MIN_PRICE, Math.min(MAX_PRICE, oldPrice + change));
        currentPrices.put(resource, newPrice);
    }

    private void validateResource(ResourceType resource) {
        if (resource == ResourceType.REGULATORY) {
            throw new IllegalArgumentException("Regulatory Zone tokens cannot be traded on the open market!");
        }
    }

    public int getPrice(ResourceType resource) {
        return currentPrices.getOrDefault(resource, INITIAL_PRICE);
    }

    // Direct 3:1 or 4:1 resource swapping bypassing Capital
    public void executeGenericMarketTrade(Player player, ResourceType offer, ResourceType receive) {
        validateResource(offer);
        validateResource(receive);

        int requiredRate = (player.getRole() == FounderRole.HACKER_CEO) ? 3 : 4;

        if (player.getResource(offer) < requiredRate) {
            throw new NotEnoughResourceException(player.getName() + " doesn't have " + requiredRate + " units of " + offer + " to trade!");
        }

        player.spendResource(offer, requiredRate);
        player.addResource(receive, 1);

        if (player.getEngine() != null) {
            player.getEngine().log(player.getName() + " traded resources directly via maritime-style market");
        }
    }

    public int getSellPrice(ResourceType resource) {
        return getPrice(resource);
    }

    public int getBuyPrice(ResourceType resource) {
        return getPrice(resource);
    }
}