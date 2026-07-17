package model;

import java.io.Serializable;

public class Sector implements Serializable {
    private final ResourceType resourceType;
    private final int activationNumber;
    private boolean isBlocked;

    // Geometric Corner bindings linked via Map algorithm 📐
    private Vertex bottomLeft;
    private Vertex bottomRight;
    private Vertex topLeft;
    private Vertex topRight;
    private static final long serialVersionUID = 1L;

    public Vertex getBottomLeft() { return bottomLeft; }
    public Vertex getBottomRight() { return bottomRight; }
    public Vertex getTopLeft() { return topLeft; }
    public Vertex getTopRight() { return topRight; }

    public Sector(ResourceType resourceType, int activationNumber) {
        this.resourceType = resourceType;
        this.activationNumber = activationNumber;
        this.isBlocked = (resourceType == ResourceType.REGULATORY);
    }

    // Missing setter called during Step 11 inside Map initialization
    public void setCorners(Vertex bl, Vertex br, Vertex tl, Vertex tr) {
        this.bottomLeft = bl;
        this.bottomRight = br;
        this.topLeft = tl;
        this.topRight = tr;
    }

    public ResourceType getResourceType() { return this.resourceType; }
    public int getActivationNumber() { return this.activationNumber; }
    public boolean isBlocked() { return this.isBlocked; }

    public void block() { this.isBlocked = true; }
    public void unblock() { this.isBlocked = false; }
}