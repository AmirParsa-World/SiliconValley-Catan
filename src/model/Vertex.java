package model;

import java.util.ArrayList;
import java.util.List;

public class Vertex {

    private Player owner;
    private Structure structure; // Dynamic reference to MVP/Unicorn
    private final List<Edge> neighboringEdges; // Maximum of 4 paths

    // at the beginning of the game, all vertices must be null; then we set them with their setter
    //If needed, it's definitely possible, but not for all.
    public Vertex() {
        this.owner = null;
        this.structure = null;
        this.neighboringEdges = new ArrayList<>(4); // Allocating memory for 4 slots
    }

    // Getters
    public Player getOwner() { return this.owner; }
    public Structure getStructure() { return this.structure; }
    public List<Edge> getNeighboringEdges() { return this.neighboringEdges; }

    // Setters for dynamic gameplay actions
    public void setOwner(Player owner) { this.owner = owner; }
    public void setStructure(Structure structure) { this.structure = structure; }

    // Helper method to add an Edge connection during map setup
    public void addNeighboringEdge(Edge edge) {
        if (this.neighboringEdges.size() < 4) {
            this.neighboringEdges.add(edge);
        }
    }
}