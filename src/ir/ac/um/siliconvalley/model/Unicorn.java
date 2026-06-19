package ir.ac.um.siliconvalley.model;

public class Unicorn extends Structure {

    public Unicorn(Player owner, Sector sector) {
        super(owner, sector);
    }

    @Override
    public int getPoint() {
        return 2;
    }

    // we'll have some special behaviors...
}
