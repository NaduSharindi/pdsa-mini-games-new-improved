package com.nibm;

import com.nibm.algorithms.GreedyAssignment;
import com.nibm.algorithms.HungarianAlgorithm;
import com.nibm.games.MinimumCostGame;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MinCostTest {

    // ── Hungarian: known small matrix ────────────────────────
    @Test
    public void testHungarianKnownMatrix() {
        int[][] cost = {
                {9, 2, 7, 8},
                {6, 4, 3, 7},
                {5, 8, 1, 8},
                {7, 6, 9, 4}
        };
        HungarianAlgorithm h = new HungarianAlgorithm();
        h.solve(cost);
        // Known optimal for this matrix = 13
        assertEquals(13, h.getTotalCost(cost));
    }

    // ── Hungarian: all same cost ──────────────────────────────
    @Test
    public void testHungarianUniformCost() {
        int n = 5;
        int[][] cost = new int[n][n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                cost[i][j] = 10;

        HungarianAlgorithm h = new HungarianAlgorithm();
        h.solve(cost);
        assertEquals(n * 10, h.getTotalCost(cost));
    }

    // ── Hungarian: null input throws exception ────────────────
    @Test
    public void testHungarianNullThrows() {
        HungarianAlgorithm h = new HungarianAlgorithm();
        assertThrows(IllegalArgumentException.class, () -> h.solve(null));
    }

    // ── Greedy: known matrix ──────────────────────────────────
    @Test
    public void testGreedyKnownMatrix() {
        int[][] cost = {
                {9, 2, 7},
                {6, 4, 3},
                {5, 8, 1}
        };
        GreedyAssignment g = new GreedyAssignment();
        g.solve(cost);
        int total = g.getTotalCost(cost);
        assertTrue(total > 0, "Greedy should return a positive total cost");
    }

    // ── Greedy: null input throws exception ───────────────────
    @Test
    public void testGreedyNullThrows() {
        GreedyAssignment g = new GreedyAssignment();
        assertThrows(IllegalArgumentException.class, () -> g.solve(null));
    }

    // ── Hungarian always <= Greedy (optimality check) ─────────
    @Test
    public void testHungarianBeatsGreedy() {
        MinimumCostGame game = new MinimumCostGame();
        for (int round = 1; round <= 5; round++) {
            game.newRound(round);
            int hungarian = game.getCorrectAnswer();
            int greedy    = game.getGreedyCost();
            assertTrue(hungarian <= greedy,
                    "Hungarian must always find cost <= Greedy. Round " + round);
        }
    }

    // ── N stays within bounds ────────────────────────────────
    @Test
    public void testNInRange() {
        MinimumCostGame game = new MinimumCostGame();
        for (int i = 1; i <= 10; i++) {
            game.newRound(i);
            int n = game.getN();
            assertTrue(n >= 50 && n <= 100,
                    "N must be between 50 and 100. Got: " + n);
        }
    }

    // ── Correct answer is always positive ────────────────────
    @Test
    public void testAnswerIsPositive() {
        MinimumCostGame game = new MinimumCostGame();
        game.newRound(1);
        assertTrue(game.getCorrectAnswer() > 0,
                "Correct answer must be positive");
    }

    // ── Validate answer: correct input ────────────────────────
    @Test
    public void testValidateCorrectAnswer() {
        MinimumCostGame game = new MinimumCostGame();
        game.newRound(1);
        assertTrue(game.validateAnswer(game.getCorrectAnswer()));
    }

    // ── Validate answer: wrong input ──────────────────────────
    @Test
    public void testValidateWrongAnswer() {
        MinimumCostGame game = new MinimumCostGame();
        game.newRound(1);
        assertFalse(game.validateAnswer(-999));
    }
}