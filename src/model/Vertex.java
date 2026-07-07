package ir.ac.um.siliconvalley.model;

public class Vertex {

    private Player owner;

    private boolean mvp;

    private boolean unicorn;

    public Vertex(Player owner,
                  boolean mvp,
                  boolean unicorn) {

        this.owner = owner;
        this.mvp = mvp;
        this.unicorn = unicorn;
    }

    public Player getOwner() {
        return owner;
    }

    public boolean isMvp() {
        return mvp;
    }

    public boolean isUnicorn() {
        return unicorn;
    }
}
