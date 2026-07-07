package ir.ac.um.siliconvalley.model;

public class Edge {

    private Player owner;

    private boolean partnership;


    public Vertex(Player owner,
                  boolean partnership) {

        this.owner = owner;
        this.partnership = partnership;
    }

    public Player getOwner() {
        return owner;
    }

    public boolean isPartnership() {
        return partnership;
    }

}
