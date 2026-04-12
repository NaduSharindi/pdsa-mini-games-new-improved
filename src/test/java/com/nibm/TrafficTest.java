package com.nibm;

import com.nibm.algorithms.EdmondsKarp;
import com.nibm.algorithms.FordFulkerson;
import com.nibm.games.TrafficGame;
import com.nibm.models.TrafficNetwork;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TrafficTest {

    // ── Known small graph ─────────────────────────────────────
    // Simple 4-node graph: S->A=10, S->B=10, A->T=10, B->T=10
    // Max flow = 20
    private int[][] simpleGraph() {
        int[][] cap = new int[4][4];
        cap[0][1] = 10; // S -> A
        cap[0][2] = 10; // S -> B
        cap[1][3] = 10; // A -> T
        cap[2][3] = 10; // B -> T
        return cap;
    }

    // ── Ford-Fulkerson: known graph ───────────────────────────
    @Test
    public void testFordFulkersonKnown() {
        FordFulkerson ff = new FordFulkerson();
        ff.solve(simpleGraph(), 0, 3);
        assertEquals(20, ff.getMaxFlow(),
                "FF max flow on simple graph should be 20");
    }

    // ── Edmonds-Karp: known graph ─────────────────────────────
    @Test
    public void testEdmondsKarpKnown() {
        EdmondsKarp ek = new EdmondsKarp();
        ek.solve(simpleGraph(), 0, 3);
        assertEquals(20, ek.getMaxFlow(),
                "EK max flow on simple graph should be 20");
    }

    // ── Both algorithms match ─────────────────────────────────
    @Test
    public void testBothAlgorithmsMatch() {
        TrafficGame game = new TrafficGame();
        for (int i = 1; i <= 5; i++) {
            game.newRound(i);
            assertEquals(game.getCorrectAnswer(), game.getEkAnswer(),
                    "FF and EK must give same max flow. Round " + i);
        }
    }

    // ── Null input throws exception ───────────────────────────
    @Test
    public void testFFNullThrows() {
        FordFulkerson ff = new FordFulkerson();
        assertThrows(IllegalArgumentException.class,
                () -> ff.solve(null, 0, 8));
    }

    @Test
    public void testEKNullThrows() {
        EdmondsKarp ek = new EdmondsKarp();
        assertThrows(IllegalArgumentException.class,
                () -> ek.solve(null, 0, 8));
    }

    // ── Invalid source/sink throws exception ──────────────────
    @Test
    public void testInvalidSourceThrows() {
        FordFulkerson ff = new FordFulkerson();
        assertThrows(IllegalArgumentException.class,
                () -> ff.solve(simpleGraph(), -1, 3));
    }

    // ── Capacity stays in range 5–15 ──────────────────────────
    @Test
    public void testCapacityRange() {
        TrafficGame game = new TrafficGame();
        game.newRound(1);
        int[][] cap = game.getNetwork().getCapacity();
        for (int[] edge : TrafficNetwork.EDGES) {
            int c = cap[edge[0]][edge[1]];
            assertTrue(c >= 5 && c <= 15,
                    "Capacity must be 5–15. Got: " + c);
        }
    }

    // ── Max flow is always positive ───────────────────────────
    @Test
    public void testMaxFlowPositive() {
        TrafficGame game = new TrafficGame();
        game.newRound(1);
        assertTrue(game.getCorrectAnswer() > 0,
                "Max flow must be positive");
    }

    // ── 3 choices contain correct answer ─────────────────────
    @Test
    public void testChoicesContainCorrect() {
        TrafficGame game = new TrafficGame();
        game.newRound(1);
        int[] choices = game.generateChoices();
        assertEquals(3, choices.length);
        boolean found = false;
        for (int c : choices) {
            if (c == game.getCorrectAnswer()) { found = true; break; }
        }
        assertTrue(found, "Choices must include correct answer");
    }

    // ── Validate answer ───────────────────────────────────────
    @Test
    public void testValidateAnswer() {
        TrafficGame game = new TrafficGame();
        game.newRound(1);
        assertTrue(game.validateAnswer(game.getCorrectAnswer()));
        assertFalse(game.validateAnswer(0));
    }
}