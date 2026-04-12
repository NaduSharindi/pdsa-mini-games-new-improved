package com.nibm.models;

public class TrafficRound {

    private int roundNumber;
    private int[][] capacities;
    private int fordFulkersonAnswer;
    private int edmondsKarpAnswer;
    private long ffTimeMs;
    private long ekTimeMs;
    private String playerName;
    private int playerAnswer;
    private boolean correct;

    public TrafficRound() {}

    // ── Getters & Setters ─────────────────────────────────────
    public int getRoundNumber()                { return roundNumber; }
    public void setRoundNumber(int r)          { this.roundNumber = r; }

    public int[][] getCapacities()             { return capacities; }
    public void setCapacities(int[][] c)       { this.capacities = c; }

    public int getFordFulkersonAnswer()        { return fordFulkersonAnswer; }
    public void setFordFulkersonAnswer(int a)  { this.fordFulkersonAnswer = a; }

    public int getEdmondsKarpAnswer()          { return edmondsKarpAnswer; }
    public void setEdmondsKarpAnswer(int a)    { this.edmondsKarpAnswer = a; }

    public long getFfTimeMs()                  { return ffTimeMs; }
    public void setFfTimeMs(long t)            { this.ffTimeMs = t; }

    public long getEkTimeMs()                  { return ekTimeMs; }
    public void setEkTimeMs(long t)            { this.ekTimeMs = t; }

    public String getPlayerName()              { return playerName; }
    public void setPlayerName(String p)        { this.playerName = p; }

    public int getPlayerAnswer()               { return playerAnswer; }
    public void setPlayerAnswer(int a)         { this.playerAnswer = a; }

    public boolean isCorrect()                 { return correct; }
    public void setCorrect(boolean c)          { this.correct = c; }
}