package model;

import java.io.Serializable;
import java.util.Random;

public class Dice implements Serializable {

    private static final long serialVersionUID = 1L;
    private int currentFace;
    private int lastDie1;
    private int lastDie2;
    private final Random random;

    public Dice() {
        this.currentFace = 0;
        this.lastDie1 = 0;
        this.lastDie2 = 0;
        this.random = new Random();
    }

    public int roll() {
        this.lastDie1 = random.nextInt(6) + 1;
        this.lastDie2 = random.nextInt(6) + 1;
        this.currentFace = lastDie1 + lastDie2;
        return this.currentFace;
    }

    public int getCurrentFace() {
        return this.currentFace;
    }

    public int getLastDie1() {
        return this.lastDie1;
    }

    public int getLastDie2() {
        return this.lastDie2;
    }
}