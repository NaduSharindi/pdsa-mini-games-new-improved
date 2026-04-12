package com.nibm.models;

public class GameRound {
    private String gameType;       // "MINIMUM_COST"
    private int roundNumber;
    private int n;
    private int hungarianCost;
    private int greedyCost;
    private long hungarianTimeMs;
    private long greedyTimeMs;
    private String playerName;
    private int playerAnswer;
    private boolean isCorrect;

    // ── Constructors ──────────────────────────────────────────
    public GameRound() {}

    public GameRound(String gameType, int roundNumber, int n) {
        this.gameType    = gameType;
        this.roundNumber = roundNumber;
        this.n           = n;
    }

    // ── Getters & Setters ─────────────────────────────────────
    public String getGameType()              { return gameType; }
    public void setGameType(String g)        { this.gameType = g; }

    public int getRoundNumber()              { return roundNumber; }
    public void setRoundNumber(int r)        { this.roundNumber = r; }

    public int getN()                        { return n; }
    public void setN(int n)                  { this.n = n; }

    public int getHungarianCost()            { return hungarianCost; }
    public void setHungarianCost(int c)      { this.hungarianCost = c; }

    public int getGreedyCost()               { return greedyCost; }
    public void setGreedyCost(int c)         { this.greedyCost = c; }

    public long getHungarianTimeMs()         { return hungarianTimeMs; }
    public void setHungarianTimeMs(long t)   { this.hungarianTimeMs = t; }

    public long getGreedyTimeMs()            { return greedyTimeMs; }
    public void setGreedyTimeMs(long t)      { this.greedyTimeMs = t; }

    public String getPlayerName()            { return playerName; }
    public void setPlayerName(String p)      { this.playerName = p; }

    public int getPlayerAnswer()             { return playerAnswer; }
    public void setPlayerAnswer(int a)       { this.playerAnswer = a; }

    public boolean isCorrect()               { return isCorrect; }
    public void setCorrect(boolean c)        { this.isCorrect = c; }
}