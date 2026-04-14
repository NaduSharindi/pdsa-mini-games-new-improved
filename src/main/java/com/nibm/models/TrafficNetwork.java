package com.nibm.models;

public class TrafficNetwork {

    public static final String[] NODE_NAMES =
            {"A", "B", "C", "D", "E", "F", "G", "H", "T"};

    public static final int NODE_COUNT = 9;
    public static final int SOURCE = 0; // A
    public static final int SINK = 8;   // T

    public static final int[][] EDGES = {
            {0, 1}, // A -> B
            {0, 2}, // A -> C
            {0, 3}, // A -> D
            {1, 4}, // B -> E
            {1, 5}, // B -> F
            {2, 4}, // C -> E
            {2, 5}, // C -> F
            {3, 5}, // D -> F
            {4, 6}, // E -> G
            {4, 7}, // E -> H
            {5, 7}, // F -> H
            {6, 8}, // G -> T
            {7, 8}  // H -> T
    };

    private int[][] capacity;

    public TrafficNetwork(int[][] capacity) {
        this.capacity = capacity;
    }

    public int[][] getCapacity() {
        return capacity;
    }

    public void setCapacity(int[][] capacity) {
        this.capacity = capacity;
    }

    public int getEdgeCapacity(int from, int to) {
        return capacity[from][to];
    }
}