package com.nibm.models;

public class BoardCell {

    public enum CellType { NORMAL, SNAKE, LADDER }

    private final int cellNumber;
    private final int destination;
    private final CellType type;

    public BoardCell(int cellNumber, int destination, CellType type) {
        this.cellNumber = cellNumber;
        this.destination = destination;
        this.type = type;
    }

    public int getCellNumber() { return cellNumber; }
    public int getDestination() { return destination; }
    public CellType getType() { return type; }
}