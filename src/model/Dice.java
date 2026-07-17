package model;

import java.io.Serializable;
import java.util.Random;

public class Dice implements Serializable {

    private static final long serialVersionUID = 1L;
    private int currentFace;
    private final Random random;

    public Dice() {
        this.currentFace = 0;
        this.random = new Random();
    }

    public int roll() {
        // 🎲 شبیه‌سازی ریختن دو تاس ۶ وجهی به صورت مستقل و جمع کردن آن‌ها (بین ۲ تا ۱۲)
        int die1 = random.nextInt(6) + 1; // تاس اول (۱ تا ۶)
        int die2 = random.nextInt(6) + 1; // تاس دوم (۱ تا ۶)

        this.currentFace = die1 + die2; // مجموع دو تاس
        return this.currentFace;
    }

    public int getCurrentFace() {
        return this.currentFace;
    }
}