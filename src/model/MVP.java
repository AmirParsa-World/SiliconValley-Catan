package model;

public class MVP extends Structure {

    public MVP(Player owner, Vertex vertex) {
        super(owner, vertex);
    }

    @Override
    public int getPoint() {
        return 1;
    }

    // we'll have some special behaviors...
}
