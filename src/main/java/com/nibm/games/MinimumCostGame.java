package com.nibm.games;

import com.nibm.algorithms.GreedyAssignment;
import com.nibm.algorithms.HungarianAlgorithm;
import com.nibm.models.GameMode;
import com.nibm.models.GameRound;

import java.util.Random;

public class MinimumCostGame {

    private static final int PLAY_MIN_N = 4;
    private static final int PLAY_MAX_N = 8;

    private static final int PERF_MIN_N = 50;
    private static final int PERF_MAX_N = 100;

    private static final int MIN_COST = 20;
    private static final int MAX_COST = 200;

    private int n;
    private int[][] costMatrix;
    private int correctAnswer;
    private int roundNumber;
    private int greedyCost;
    private long hungarianTimeNs;
    private long greedyTimeNs;
    private GameMode mode;

    private final Random random = new Random();

    public GameRound newRound(int roundNumber, GameMode mode) {
        this.roundNumber = roundNumber;
        this.mode = mode;

        if (mode == GameMode.PLAY) {
            n = PLAY_MIN_N + random.nextInt(PLAY_MAX_N - PLAY_MIN_N + 1);
        } else {
            n = PERF_MIN_N + random.nextInt(PERF_MAX_N - PERF_MIN_N + 1);
        }

        costMatrix = generateCostMatrix(n);

        HungarianAlgorithm hungarian = new HungarianAlgorithm();
        long hStart = System.nanoTime();
        hungarian.solve(deepCopy(costMatrix));
        hungarianTimeNs = System.nanoTime() - hStart;
        correctAnswer = hungarian.getTotalCost(costMatrix);

        GreedyAssignment greedy = new GreedyAssignment();
        long gStart = System.nanoTime();
        greedy.solve(deepCopy(costMatrix));
        greedyTimeNs = System.nanoTime() - gStart;
        greedyCost = greedy.getTotalCost(costMatrix);

        GameRound round = new GameRound("MINIMUM_COST", roundNumber, n);
        round.setMode(mode.name());
        round.setHungarianCost(correctAnswer);
        round.setGreedyCost(greedyCost);
        round.setHungarianTimeNs(hungarianTimeNs);
        round.setGreedyTimeNs(greedyTimeNs);

        return round;
    }

    public boolean validateAnswer(int playerAnswer) {
        return playerAnswer == correctAnswer;
    }

    public int getN() {
        return n;
    }

    public int getCorrectAnswer() {
        return correctAnswer;
    }

    public int getGreedyCost() {
        return greedyCost;
    }

    public long getHungarianTimeNs() {
        return hungarianTimeNs;
    }

    public long getGreedyTimeNs() {
        return greedyTimeNs;
    }

    public int[][] getCostMatrix() {
        return deepCopy(costMatrix);
    }

    public GameMode getMode() {
        return mode;
    }

    private int[][] generateCostMatrix(int size) {
        int[][] matrix = new int[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
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