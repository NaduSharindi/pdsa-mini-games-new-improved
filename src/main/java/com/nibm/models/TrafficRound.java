package com.nibm.models;

import java.util.ArrayList;
import java.util.List;

public class TrafficRound {

    private int roundNumber;
    private int[][] capacities;
    private int fordFulkersonAnswer;
    private int edmondsKarpAnswer;
    private long ffTimeNs;
    private long ekTimeNs;

    private String playerName;
    private int playerFlow;
    private boolean correct;
    private List<String> playerPaths = new ArrayList<>();

    public int getRoundNumber() {
        return roundNumber;
    }

    public void setRoundNumber(int roundNumber) {
        this.roundNumber = roundNumber;
    }

    public int[][] getCapacities() {
        return capacities;
    }

    public void setCapacities(int[][] capacities) {
        this.capacities = capacities;
    }

    public int getFordFulkersonAnswer() {
        return fordFulkersonAnswer;
    }

    public void setFordFulkersonAnswer(int fordFulkersonAnswer) {
        this.fordFulkersonAnswer = fordFulkersonAnswer;
    }

    public int getEdmondsKarpAnswer() {
        return edmondsKarpAnswer;
    }

    public void setEdmondsKarpAnswer(int edmondsKarpAnswer) {
        this.edmondsKarpAnswer = edmondsKarpAnswer;
    }

    public long getFfTimeNs() {
        return ffTimeNs;
    }

    public void setFfTimeNs(long ffTimeNs) {
        this.ffTimeNs = ffTimeNs;
    }

    public long getEkTimeNs() {
        return ekTimeNs;
    }

    public void setEkTimeNs(long ekTimeNs) {
        this.ekTimeNs = ekTimeNs;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public int getPlayerFlow() {
        return playerFlow;
    }

    public void setPlayerFlow(int playerFlow) {
        this.playerFlow = playerFlow;
    }

    public boolean isCorrect() {
        return correct;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }

    public List<String> getPlayerPaths() {
        return playerPaths;
    }

    public void setPlayerPaths(List<String> playerPaths) {
        this.playerPaths = playerPaths;
    }
}