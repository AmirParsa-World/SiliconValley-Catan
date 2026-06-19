package ir.ac.um.siliconvalley.model;

import ir.ac.um.siliconvalley.exception.NotEnoughResourceException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Player {

    private final String name;
    private final String color;
    private FounderRole role;
    private final Map<ResourceType, Integer> wallet;
    private final List<Structure> structures;


    public Player (String name, String color) {

        this.name = name;
        this.color = color;
        this.role = null;

        this.structures = new ArrayList<>();
        this.wallet = new HashMap<>(); // now we created an object from Map class
                                       //( it's not the map(board) of the game)

        for (ResourceType type : ResourceType.values()) {
            this.wallet.put(type, 0);
        }

    }


    public void addResource (ResourceType type, int amount) {

        int currentAmount = wallet.get(type);
        this.wallet.put(type, amount+currentAmount);

    }

    public void spendResource (ResourceType type, int amount) {

        if (wallet.get(type) < amount )
            throw new NotEnoughResourceException("Dear player, your haven't enough" + type + " resource ");

        this.wallet.put(type, wallet.get(type) - amount);


    }

    //TODO: give 1 point for the longest partnership (at least must be 3 partnerships)

    public int countPlayerPoint() {

        int totalPoint = (this.role != null) ? -1 : 0; // shorter and easier.
//        int totalPoints;
//        if (this.role != null) {
//            totalPoints = -1;
//        } else {
//            totalPoints = 0;
//        }

        for (Structure structure : this.structures) { // we handle the outOfBoundException by writing this form.
            totalPoint += structure.getPoint();
        }

        return totalPoint;
    }

// Finish TODO.



}
