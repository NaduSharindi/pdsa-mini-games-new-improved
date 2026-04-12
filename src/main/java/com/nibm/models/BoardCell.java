package com.nibm.models;

public class BoardCell {

    public enum CellType { NORMAL, SNAKE, LADDER }

    private int cellNumber;
    private int destination;   // same as cellNumber if NORMAL
    private CellType type;

    public BoardCell(int cellNumber, int destination, CellType type) {
        this.cellNumber  = cellNumber;
        this.destination = destination;
        this.type        = type;
    }

    public int getCellNumber()   { return cellNumber; }
    public int getDestination()  { return destination; }
    public CellType getType()    { return type; }

    @Override
    public String toString() {
        return "Cell " + cellNumber + " -> " + destination + " [" + type + "]";
    }
}