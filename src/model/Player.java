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

    // 🔗 اتصال موقت به موتور بازی (transient مانع خراب شدن سیستم Save/Load می‌شود)
    private transient GameEngine engine;

    private static final long serialVersionUID = 1L;

    // 🔄 گتر و سترهای مدیریت ارتباط با موتور اصلی بازی
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

        // مقداردهی اولیه کیف پول منابع
        this.wallet = new EnumMap<>(ResourceType.class);
        for (ResourceType type : ResourceType.values()) {
            this.wallet.put(type, 0);
        }
    }

    // 📊 متدهای گتر اصلی بازیکن
    public String getName() { return name; }
    public String getColor() { return color; }
    public List<Structure> getStructures() { return structures; }
    public Map<ResourceType, Integer> getWallet() { return wallet; }

    // 📦 دریافت موجودی یک منبع خاص
    public int getResource(ResourceType type) {
        return wallet.getOrDefault(type, 0);
    }

    // 💰 محاسبه مجموع کل منابع موجود در کیف پول
    public int getTotalResources() {
        int total = 0;
        for (int amount : wallet.values()) {
            total += amount;
        }
        return total;
    }

    // 📥 اضافه کردن منبع به کیف پول
    public void addResource(ResourceType type, int amount) {
        int currentAmount = wallet.getOrDefault(type, 0);
        this.wallet.put(type, amount + currentAmount);
    }

    // 📤 کسر منبع از کیف پول همراه با کنترل خطا
    public void spendResource(ResourceType type, int amount) {
        if (wallet.getOrDefault(type, 0) < amount) {
            throw new NotEnoughResourceException("Dear player, you don't have enough " + type + " resource.");
        }
        this.wallet.put(type, wallet.get(type) - amount);
    }

    // 🏗️ اضافه کردن سازه جدید به لیست بازیکن
    public void addStructure(Structure structure) {
        structures.add(structure);
    }

    // 🏆 محاسبه داینامیک امتیاز کل بازیکن (با احتساب نقش‌ها و جاده‌ها)
    public int countPlayerPoint() {
        int points = 0;

        // 🏢 ۱. محاسبه امتیاز حاصل از سازه‌ها (تغییر CompanyStructure به Structure)
        if (this.structures != null) {
            for (Structure structure : this.structures) { // 👈 این خط اصلاح شد
                points += structure.getPoint();
            }
        }

        // 🛣️ ۲. محاسبه امتیاز بابت داشتن بزرگترین شبکه (پاداش جاده‌ها)
        if (this.hasLongestNetwork) {
            points += 2; // امتیاز بابت Longest Network
        }

        // 🎯 گام طلایی: کسر ۱ امتیاز در صورت انتخاب هر نقشی بجای NONE
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

    // 💸 سوزاندن تصادفی کارت‌ها (استفاده در جریمه‌های مالی و تاس ۷ بازرس)
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