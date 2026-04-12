package com.nibm.games;

import com.nibm.algorithms.GreedyAssignment;
import com.nibm.algorithms.HungarianAlgorithm;
import com.nibm.models.GameRound;

import java.util.Random;

public class MinimumCostGame {

    private static final int MIN_N    = 50;
    private static final int MAX_N    = 100;
    private static final int MIN_COST = 20;
    private static final int MAX_COST = 200;

    private int n;
    private int[][] costMatrix;
    private int[][] originalMatrix;  // keep untouched copy for cost lookup
    private int correctAnswer;       // Hungarian optimal cost
    private int roundNumber;

    private long hungarianTimeMs;
    private long greedyTimeMs;
    private int greedyCost;

    private final Random random = new Random();

    // ── Start a new round ─────────────────────────────────────
    public GameRound newRound(int roundNumber) {
        this.roundNumber = roundNumber;

        // Generate random N between 50 and 100
        this.n = MIN_N + random.nextInt(MAX_N - MIN_N + 1);

        // Generate random cost matrix
        this.costMatrix   = generateCostMatrix(n);
        this.originalMatrix = deepCopy(costMatrix);

        // Run Hungarian algorithm — timed
        HungarianAlgorithm hungarian = new HungarianAlgorithm();
        long hStart = System.currentTimeMillis();
        hungarian.solve(deepCopy(costMatrix));   // pass copy; algo mutates it
        long hEnd = System.currentTimeMillis();
        hungarianTimeMs = hEnd - hStart;
        correctAnswer   = hungarian.getTotalCost(originalMatrix);

        // Run Greedy algorithm — timed
        GreedyAssignment greedy = new GreedyAssignment();
        long gStart = System.currentTimeMillis();
        greedy.solve(originalMatrix);
        long gEnd = System.currentTimeMillis();
        greedyTimeMs = gEnd - gStart;
        greedyCost   = greedy.getTotalCost(originalMatrix);

        // Build and return the round model
        GameRound round = new GameRound("MINIMUM_COST", roundNumber, n);
        round.setHungarianCost(correctAnswer);
        round.setGreedyCost(greedyCost);
        round.setHungarianTimeMs(hungarianTimeMs);
        round.setGreedyTimeMs(greedyTimeMs);
        return round;
    }

    // ── Validate player's answer ──────────────────────────────
    public boolean validateAnswer(int playerAnswer) {
        return playerAnswer == correctAnswer;
    }

    // ── Getters ───────────────────────────────────────────────
    public int getN()                { return n; }
    public int getCorrectAnswer()    { return correctAnswer; }
    public int getGreedyCost()       { return greedyCost; }
    public long getHungarianTimeMs() { return hungarianTimeMs; }
    public long getGreedyTimeMs()    { return greedyTimeMs; }
    public int[][] getCostMatrix()   { return originalMatrix; }

    // ── Private helpers ───────────────────────────────────────
    private int[][] generateCostMatrix(int size) {
        int[][] matrix = new int[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                // Cost between $20 and $200
                matrix[i][j] = MIN_COST + random.nextInt(MAX_COST - MIN_COST + 1);
            }
        }
        return matrix;
    }

    private int[][] deepCopy(int[][] src) {
        int[][] copy = new int[src.length][src[0].length];
        for (int i = 0; i < src.length; i++) {
            copy[i] = src[i].clone();
        }
        return copy;
    }
}