package model;

public class Edge {
    private final Vertex u; // Connecting intersection 1
    private final Vertex v; // Connecting intersection 2
    private Player owner;
    private boolean partnership;

    // The correct constructor for map initialization!
    public Edge(Vertex u, Vertex v) {
        this.u = u;
        this.v = v;
        this.owner = null;        // No owner at start of game
        this.partnership = false; // No partnership built yet
    }

    public Vertex getU() { return u; }
    public Vertex getV() { return v; }
    public Player getOwner() { return owner; }
    public boolean isPartnership() { return partnership; }

    public void setOwner(Player owner) { this.owner = owner; }
    public void setPartnership(boolean partnership) { this.partnership = partnership; }
}