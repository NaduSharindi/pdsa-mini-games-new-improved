package com.nibm.models;

public class SnakeLadderRound {

    private int n;
    private int totalCells;
    private int[][] snakes;
    private int[][] ladders;
    private int bfsAnswer;
    private int dijkstraAnswer;
    private long bfsTimeNs;
    private long dijkstraTimeNs;
    private String playerName;
    private int playerThrows;
    private int finalPosition;
    private boolean completed;
    private int roundNumber;

    public int getN() { return n; }
    public void setN(int n) { this.n = n; }

    public int getTotalCells() { return totalCells; }
    public void setTotalCells(int totalCells) { this.totalCells = totalCells; }

    public int[][] getSnakes() { return snakes; }
    public void setSnakes(int[][] snakes) { this.snakes = snakes; }

    public int[][] getLadders() { return ladders; }
    public void setLadders(int[][] ladders) { this.ladders = ladders; }

    public int getBfsAnswer() { return bfsAnswer; }
    public void setBfsAnswer(int bfsAnswer) { this.bfsAnswer = bfsAnswer; }

    public int getDijkstraAnswer() { return dijkstraAnswer; }
    public void setDijkstraAnswer(int dijkstraAnswer) { this.dijkstraAnswer = dijkstraAnswer; }

    public long getBfsTimeNs() { return bfsTimeNs; }
    public void setBfsTimeNs(long bfsTimeNs) { this.bfsTimeNs = bfsTimeNs; }

    public long getDijkstraTimeNs() { return dijkstraTimeNs; }
    public void setDijkstraTimeNs(long dijkstraTimeNs) { this.dijkstraTimeNs = dijkstraTimeNs; }

    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }

    public int getPlayerThrows() { return playerThrows; }
    public void setPlayerThrows(int playerThrows) { this.playerThrows = playerThrows; }

    public int getFinalPosition() { return finalPosition; }
    public void setFinalPosition(int finalPosition) { this.finalPosition = finalPosition; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public int getRoundNumber() { return roundNumber; }
    public void setRoundNumber(int roundNumber) { this.roundNumber = roundNumber; }
}