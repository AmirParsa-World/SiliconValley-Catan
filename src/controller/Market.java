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
                stagnationTrackers.put(type, 0); // 0 rounds of inactivity to start
            }
        }
    }

    // 📈 Buying an asset: Increases market price, resets stagnation warning
    public void buyResource(Player player, ResourceType resource) {
        validateResource(resource);

        // Dynamic Price Calculation: Hacker CEO always buys at a fixed rate of 3 CAPITAL!
        int pricePaid = (player.getRole() == FounderRole.HACKER_CEO) ? 3 : currentPrices.get(resource);

        player.spendResource(ResourceType.CAPITAL, pricePaid);
        player.addResource(resource, 1);

        adjustPrice(resource, 1);
        stagnationTrackers.put(resource, 0); // Direct market interaction resets stagnation 🔄
    }

    // 📉 Selling an asset: Decreases market price, resets stagnation warning
    public void sellResource(Player player, ResourceType resource) {
        validateResource(resource);
        int currentMarketPrice = currentPrices.get(resource);

        player.spendResource(resource, 1);
        player.addResource(ResourceType.CAPITAL, currentMarketPrice);

        adjustPrice(resource, -1);
        stagnationTrackers.put(resource, 0); // Direct market interaction resets stagnation 🔄
    }

    // ⏳ The Stagnation Clock: Triggered by the coordinator layer at the end of every full round
    public void incrementRoundTick() {
        for (ResourceType resource : currentPrices.keySet()) {
            int consecutiveQuietRounds = stagnationTrackers.get(resource) + 1;
            stagnationTrackers.put(resource, consecutiveQuietRounds);

            // 🚨 Round 3 Warning hit! Automatically drop price by 1 unit
            if (consecutiveQuietRounds >= STAGNATION_LIMIT) {
                adjustPrice(resource, -1);
                stagnationTrackers.put(resource, 0); // Clear counter after price drop
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


    // 🏦 تبدیل ۳ به ۱ یا ۴ به ۱ منابع بدون دخالت کپیتال طبق داک بازی
    public void executeGenericMarketTrade(Player player, ResourceType offer, ResourceType receive) {
        validateResource(offer);
        validateResource(receive);

        int requiredRate = (player.getRole() == FounderRole.HACKER_CEO) ? 3 : 4;

        if (player.getResource(offer) < requiredRate) {
            throw new NotEnoughResourceException(player.getName() + " doesn't have " + requiredRate + " units of " + offer + " to trade!");
        }

        player.spendResource(offer, requiredRate);
        player.addResource(receive, 1);
        System.out.println("🏦 MARKET TRADE: " + player.getName() + " traded " + requiredRate + " " + offer + " for 1 " + receive);
    }

}