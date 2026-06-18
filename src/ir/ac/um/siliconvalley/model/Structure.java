package ir.ac.um.siliconvalley.model;

public abstract class Structure {

    // general field for every structure.
    private final Player owner;
    private final Sector sector;


    public Structure (Player owner, Sector sector) {

        this.owner = owner;
        this.sector = sector;

    }

    public Player getOwner() {
        return this.owner;
    }

    public Sector getSector() {
        return this.sector;
    }

}
