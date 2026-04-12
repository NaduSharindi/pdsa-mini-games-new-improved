package com.nibm.models;

public class KnightMove {
    private int row;
    private int col;
    private int moveNumber;

    public KnightMove(int row, int col, int moveNumber) {
        this.row        = row;
        this.col        = col;
        this.moveNumber = moveNumber;
    }

    public int getRow()        { return row; }
    public int getCol()        { return col; }
    public int getMoveNumber() { return moveNumber; }

    @Override
    public String toString() {
        return "Move " + moveNumber
                + " -> (" + row + "," + col + ")";
    }
}