package com.nibm;

import com.nibm.algorithms.GreedyAssignment;
import com.nibm.algorithms.HungarianAlgorithm;
import com.nibm.games.MinimumCostGame;
import com.nibm.models.GameMode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MinCostTest {

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
        assertEquals(13, h.getTotalCost(cost));
    }

    @Test
    public void testGreedyKnownMatrix() {
        int[][] cost = {
                {9, 2, 7},
                {6, 4, 3},
                {5, 8, 1}
        };

        GreedyAssignment g = new GreedyAssignment();
        g.solve(cost);
        assertTrue(g.getTotalCost(cost) > 0);
    }

    @Test
    public void testPlayModeNInRange() {
        MinimumCostGame game = new MinimumCostGame();
        for (int i = 0; i < 10; i++) {
            game.newRound(i + 1, GameMode.PLAY);
            assertTrue(game.getN() >= 4 && game.getN() <= 8);
        }
    }

    @Test
    public void testPerformanceModeNInRange() {
        MinimumCostGame game = new MinimumCostGame();
        for (int i = 0; i < 10; i++) {
            game.newRound(i + 1, GameMode.PERFORMANCE);
            assertTrue(game.getN() >= 50 && game.getN() <= 100);
        }
    }

    @Test
    public void testHungarianAlwaysBeatsGreedyInPlayMode() {
        MinimumCostGame game = new MinimumCostGame();
        for (int i = 0; i < 5; i++) {
            game.newRound(i + 1, GameMode.PLAY);
            assertTrue(game.getCorrectAnswer() <= game.getGreedyCost());
        }
    }

    @Test
    public void testHungarianAlwaysBeatsGreedyInPerformanceMode() {
        MinimumCostGame game = new MinimumCostGame();
        for (int i = 0; i < 5; i++) {
            game.newRound(i + 1, GameMode.PERFORMANCE);
            assertTrue(game.getCorrectAnswer() <= game.getGreedyCost());
        }
    }

    @Test
    public void testCostValuesInRange() {
        MinimumCostGame game = new MinimumCostGame();
        game.newRound(1, GameMode.PLAY);

        int[][] matrix = game.getCostMatrix();
        for (int[] row : matrix) {
            for (int value : row) {
                assertTrue(value >= 20 && value <= 200);
            }
        }
    }
}