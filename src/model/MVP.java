package model;

import java.io.Serializable;

public class MVP extends Structure implements Serializable {

    private static final long serialVersionUID = 1L;

    public MVP(Player owner, Vertex vertex) {
        super(owner, vertex);
    }

    @Override
    public int getPoint() {
        return 1;
    }

    // we'll have some special behaviors...
}
