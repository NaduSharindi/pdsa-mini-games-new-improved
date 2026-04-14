package com.nibm.models;

public class GameRound {

    private String gameType;
    private int roundNumber;
    private int n;
    private int hungarianCost;
    private int greedyCost;
    private long hungarianTimeNs;
    private long greedyTimeNs;
    private String playerName;
    private int playerAnswer;
    private boolean isCorrect;
    private String mode; // PLAY or PERFORMANCE

    public GameRound() {}

    public GameRound(String gameType, int roundNumber, int n) {
        this.gameType = gameType;
        this.roundNumber = roundNumber;
        this.n = n;
    }

    public String getGameType() {
        return gameType;
    }

    public void setGameType(String gameType) {
        this.gameType = gameType;
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    public void setRoundNumber(int roundNumber) {
        this.roundNumber = roundNumber;
    }

    public int getN() {
        return n;
    }

    public void setN(int n) {
        this.n = n;
    }

    public int getHungarianCost() {
        return hungarianCost;
    }

    public void setHungarianCost(int hungarianCost) {
        this.hungarianCost = hungarianCost;
    }

    public int getGreedyCost() {
        return greedyCost;
    }

    public void setGreedyCost(int greedyCost) {
        this.greedyCost = greedyCost;
    }

    public long getHungarianTimeNs() {
        return hungarianTimeNs;
    }

    public void setHungarianTimeNs(long hungarianTimeNs) {
        this.hungarianTimeNs = hungarianTimeNs;
    }

    public long getGreedyTimeNs() {
        return greedyTimeNs;
    }

    public void setGreedyTimeNs(long greedyTimeNs) {
        this.greedyTimeNs = greedyTimeNs;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public int getPlayerAnswer() {
        return playerAnswer;
    }

    public void setPlayerAnswer(int playerAnswer) {
        this.playerAnswer = playerAnswer;
    }

    public boolean isCorrect() {
        return isCorrect;
    }

    public void setCorrect(boolean correct) {
        isCorrect = correct;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }
}