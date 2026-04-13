package com.nibm.games;

import com.nibm.algorithms.EdmondsKarp;
import com.nibm.algorithms.FordFulkerson;
import com.nibm.models.TrafficNetwork;
import com.nibm.models.TrafficRound;

import java.util.Random;

public class TrafficGame {

    private static final int MIN_CAP = 5;
    private static final int MAX_CAP = 15;

    private TrafficNetwork network;
    private int correctAnswer;
    private int ekAnswer;
    private long ffTimeMs;
    private long ekTimeMs;

    private final Random random = new Random();

    // ── Start new round ───────────────────────────────────────
    public TrafficRound newRound(int roundNumber) {
        int[][] capacity = buildCapacityMatrix();
        this.network = new TrafficNetwork(capacity);

        // ── Run Ford-Fulkerson — timed ────────────────────────
        FordFulkerson ff = new FordFulkerson();
        long ffStart = System.currentTimeMillis();
        ff.solve(deepCopy(capacity),
                TrafficNetwork.SOURCE, TrafficNetwork.SINK);
        ffTimeMs      = System.currentTimeMillis() - ffStart;
        correctAnswer = ff.getMaxFlow();

        // ── Run Edmonds-Karp — timed ──────────────────────────
        EdmondsKarp ek = new EdmondsKarp();
        long ekStart = System.currentTimeMillis();
        ek.solve(deepCopy(capacity),
                TrafficNetwork.SOURCE, TrafficNetwork.SINK);
        ekTimeMs = System.currentTimeMillis() - ekStart;
        ekAnswer = ek.getMaxFlow();

        // ── Build round model ─────────────────────────────────
        TrafficRound round = new TrafficRound();
        round.setRoundNumber(roundNumber);
        round.setCapacities(capacity);
        round.setFordFulkersonAnswer(correctAnswer);
        round.setEdmondsKarpAnswer(ekAnswer);
        round.setFfTimeMs(ffTimeMs);
        round.setEkTimeMs(ekTimeMs);
        return round;
    }

    // ── Build capacity matrix with random values 5–15 ─────────
    private int[][] buildCapacityMatrix() {
        int n   = TrafficNetwork.NODE_COUNT;
        int[][] cap = new int[n][n];
        for (int[] edge : TrafficNetwork.EDGES) {
            cap[edge[0]][edge[1]] =
                    MIN_CAP + random.nextInt(MAX_CAP - MIN_CAP + 1);
        }
        return cap;
    }

    // ── Classify player answer: WIN / DRAW / LOSE ─────────────
    // WIN  = exact correct answer
    // DRAW = within ±2 of correct (close guess)
    // LOSE = more than ±2 away
    public String classifyAnswer(int playerAnswer) {
        if (playerAnswer == correctAnswer)                       return "WIN";
        if (Math.abs(playerAnswer - correctAnswer) <= 2)        return "DRAW";
        return "LOSE";
    }

    public boolean validateAnswer(int playerAnswer) {
        return playerAnswer == correctAnswer;
    }

    // ── Getters ───────────────────────────────────────────────
    public TrafficNetwork getNetwork()   { return network; }
    public int getCorrectAnswer()        { return correctAnswer; }
    public int getEkAnswer()             { return ekAnswer; }
    public long getFfTimeMs()            { return ffTimeMs; }
    public long getEkTimeMs()            { return ekTimeMs; }

    // ── Fixed deepCopy ────────────────────────────────────────
    private int[][] deepCopy(int[][] src) {
        int[][] copy = new int[src.length][];
        for (int i = 0; i < src.length; i++) {
            copy[i] = src[i].clone();  // clone each row individually
        }
        return copy;
    }
}