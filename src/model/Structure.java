package model;

public abstract class Structure {

    // general field for every structure.
    private final Player owner;
    private final Vertex location;


    public Structure (Player owner, Vertex location) {

        this.owner = owner;
        this.location = location;

    }

    public abstract int getPoint();// int the subclasses will override.


    public Player getOwner() {
        return this.owner;
    }

    public Vertex getLocation() {
        return this.location;
    }


}
