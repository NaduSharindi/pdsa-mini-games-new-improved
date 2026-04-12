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
        // Build capacity matrix with random values 5–15
        int[][] capacity = buildCapacityMatrix();
        this.network = new TrafficNetwork(capacity);

        // Run Ford-Fulkerson — timed
        FordFulkerson ff = new FordFulkerson();
        long ffStart = System.currentTimeMillis();
        ff.solve(deepCopy(capacity),
                TrafficNetwork.SOURCE, TrafficNetwork.SINK);
        ffTimeMs      = System.currentTimeMillis() - ffStart;
        correctAnswer = ff.getMaxFlow();

        // Run Edmonds-Karp — timed
        EdmondsKarp ek = new EdmondsKarp();
        long ekStart = System.currentTimeMillis();
        ek.solve(deepCopy(capacity),
                TrafficNetwork.SOURCE, TrafficNetwork.SINK);
        ekTimeMs = System.currentTimeMillis() - ekStart;
        ekAnswer = ek.getMaxFlow();

        // Build round model
        TrafficRound round = new TrafficRound();
        round.setRoundNumber(roundNumber);
        round.setCapacities(capacity);
        round.setFordFulkersonAnswer(correctAnswer);
        round.setEdmondsKarpAnswer(ekAnswer);
        round.setFfTimeMs(ffTimeMs);
        round.setEkTimeMs(ekTimeMs);
        return round;
    }

    // ── Build capacity matrix ─────────────────────────────────
    private int[][] buildCapacityMatrix() {
        int n = TrafficNetwork.NODE_COUNT;
        int[][] cap = new int[n][n];

        // Set random capacity for each defined edge
        for (int[] edge : TrafficNetwork.EDGES) {
            int from = edge[0];
            int to   = edge[1];
            cap[from][to] = MIN_CAP +
                    random.nextInt(MAX_CAP - MIN_CAP + 1);
        }
        return cap;
    }

    // ── Generate 3 choices ────────────────────────────────────
    public int[] generateChoices() {
        int correct = correctAnswer;
        int wrong1, wrong2;

        do { wrong1 = correct + random.nextInt(10) + 1; }
        while (wrong1 == correct);

        do { wrong2 = Math.max(1, correct - random.nextInt(10) - 1); }
        while (wrong2 == correct || wrong2 == wrong1);

        int[] choices = {correct, wrong1, wrong2};
        // Shuffle
        for (int i = 2; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int tmp = choices[i];
            choices[i] = choices[j];
            choices[j] = tmp;
        }
        return choices;
    }

    // ── Validate answer ───────────────────────────────────────
    public boolean validateAnswer(int playerAnswer) {
        return playerAnswer == correctAnswer;
    }

    // ── Getters ───────────────────────────────────────────────
    public TrafficNetwork getNetwork()   { return network; }
    public int getCorrectAnswer()        { return correctAnswer; }
    public int getEkAnswer()             { return ekAnswer; }
    public long getFfTimeMs()            { return ffTimeMs; }
    public long getEkTimeMs()            { return ekTimeMs; }

    // ── Deep copy helper ──────────────────────────────────────
    private int[][] deepCopy(int[][] src) {
        int[][] copy = new int[src.length][src.length];
        for (int i = 0; i < src.length; i++) {
            copy[i] = src[i].clone();
        }
        return copy;
    }
}