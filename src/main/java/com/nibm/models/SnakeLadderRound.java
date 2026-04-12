package com.nibm.models;

public class SnakeLadderRound {

    private int n;                  // board size NxN
    private int totalCells;         // N*N
    private int[][] snakes;         // [i][0]=head, [i][1]=tail
    private int[][] ladders;        // [i][0]=base, [i][1]=top
    private int bfsAnswer;
    private int dijkstraAnswer;
    private long bfsTimeMs;
    private long dijkstraTimeMs;
    private String playerName;
    private int playerAnswer;
    private boolean correct;
    private int roundNumber;

    public SnakeLadderRound() {}

    // ── Getters & Setters ─────────────────────────────────────
    public int getN()                        { return n; }
    public void setN(int n)                  { this.n = n; }

    public int getTotalCells()               { return totalCells; }
    public void setTotalCells(int t)         { this.totalCells = t; }

    public int[][] getSnakes()               { return snakes; }
    public void setSnakes(int[][] s)         { this.snakes = s; }

    public int[][] getLadders()              { return ladders; }
    public void setLadders(int[][] l)        { this.ladders = l; }

    public int getBfsAnswer()                { return bfsAnswer; }
    public void setBfsAnswer(int a)          { this.bfsAnswer = a; }

    public int getDijkstraAnswer()           { return dijkstraAnswer; }
    public void setDijkstraAnswer(int a)     { this.dijkstraAnswer = a; }

    public long getBfsTimeMs()               { return bfsTimeMs; }
    public void setBfsTimeMs(long t)         { this.bfsTimeMs = t; }

    public long getDijkstraTimeMs()          { return dijkstraTimeMs; }
    public void setDijkstraTimeMs(long t)    { this.dijkstraTimeMs = t; }

    public String getPlayerName()            { return playerName; }
    public void setPlayerName(String p)      { this.playerName = p; }

    public int getPlayerAnswer()             { return playerAnswer; }
    public void setPlayerAnswer(int a)       { this.playerAnswer = a; }

    public boolean isCorrect()               { return correct; }
    public void setCorrect(boolean c)        { this.correct = c; }

    public int getRoundNumber()              { return roundNumber; }
    public void setRoundNumber(int r)        { this.roundNumber = r; }
}