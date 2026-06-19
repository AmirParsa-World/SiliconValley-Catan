package ir.ac.um.siliconvalley.model;

public class MVP extends Structure {

    public MVP(Player owner, Sector sector) {
        super(owner, sector);
    }

    @Override
    public int getPoint() {
        return 1;
    }

    // we'll have some special behaviors...
}
