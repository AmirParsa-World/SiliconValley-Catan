package model;

import java.io.Serializable;

public class Unicorn extends Structure implements Serializable {

    private static final long serialVersionUID = 1L;

    public Unicorn(Player owner, Vertex vertex) {
        super(owner, vertex);
    }

    @Override
    public int getPoint() {
        return 2;
    }

    // we'll have some special behaviors...
}
