package model;

public class Unicorn extends Structure {

    public Unicorn(Player owner, Vertex vertex) {
        super(owner, vertex);
    }

    @Override
    public int getPoint() {
        return 2;
    }

    // we'll have some special behaviors...
}
