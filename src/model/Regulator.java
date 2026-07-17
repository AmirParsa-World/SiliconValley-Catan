package model;

import java.io.Serializable;

public class Regulator implements Serializable {

    private static final long serialVersionUID = 1L;

    private int row;
    private int col;

    public Regulator(int row, int col) {

        this.row = row;
        this.col = col;
    }

    public void move(int row, int col) {

        this.row = row;
        this.col = col;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }
}
