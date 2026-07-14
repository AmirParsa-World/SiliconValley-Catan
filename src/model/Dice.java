package model;

import java.util.Random;

public class Dice {

    private int currentFace;
    private final Random random;


    public Dice() {

        this.currentFace = 0;
        this.random =new Random();

    }

    public int roll() {

        this.currentFace = random.nextInt(6)+1;
        return this.currentFace;

    }
}
