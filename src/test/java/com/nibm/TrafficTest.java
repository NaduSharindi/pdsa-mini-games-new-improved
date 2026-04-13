package com.nibm;

import com.nibm.algorithms.EdmondsKarp;
import com.nibm.algorithms.FordFulkerson;
import com.nibm.games.TrafficGame;
import com.nibm.models.TrafficNetwork;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TrafficTest {

    // ── Shared simple 4-node graph ────────────────────────────
    // S(0)->A(1)=10, S(0)->B(2)=10, A(1)->T(3)=10, B(2)->T(3)=10
    // Max flow = 20
    private int[][] simpleGraph() {
        int[][] cap = new int[4][4];
        cap[0][1] = 10;
        cap[0][2] = 10;
        cap[1][3] = 10;
        cap[2][3] = 10;
        return cap;
    }

    // Single path: S->A->T, cap=5. Max flow = 5
    private int[][] singlePathGraph() {
        int[][] cap = new int[3][3];
        cap[0][1] = 5;
        cap[1][2] = 5;
        return cap;
    }

    // ── Ford-Fulkerson: known graph ───────────────────────────
    @Test
    public void testFFSimpleGraph() {
        FordFulkerson ff = new FordFulkerson();
        ff.solve(simpleGraph(), 0, 3);
        assertEquals(20, ff.getMaxFlow(),
                "Simple parallel graph max flow should be 20");
    }

    // ── Ford-Fulkerson: single path ───────────────────────────
    @Test
    public void testFFSinglePath() {
        FordFulkerson ff = new FordFulkerson();
        ff.solve(singlePathGraph(), 0, 2);
        assertEquals(5, ff.getMaxFlow());
    }

    // ── Ford-Fulkerson: no path = 0 flow ─────────────────────
    @Test
    public void testFFNoPath() {
        int[][] cap = new int[3][3]; // no edges
        FordFulkerson ff = new FordFulkerson();
        ff.solve(cap, 0, 2);
        assertEquals(0, ff.getMaxFlow(),
                "No path from source to sink = 0 flow");
    }

    // ── Ford-Fulkerson: null throws ───────────────────────────
    @Test
    public void testFFNullThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new FordFulkerson().solve(null, 0, 3));
    }

    // ── Ford-Fulkerson: empty matrix throws ───────────────────
    @Test
    public void testFFEmptyThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new FordFulkerson().solve(new int[0][0], 0, 0));
    }

    // ── Ford-Fulkerson: negative source throws ────────────────
    @Test
    public void testFFNegativeSourceThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new FordFulkerson().solve(simpleGraph(), -1, 3));
    }

    // ── Ford-Fulkerson: sink out of bounds throws ─────────────
    @Test
    public void testFFSinkOutOfBoundsThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new FordFulkerson().solve(simpleGraph(), 0, 99));
    }

    // ── Edmonds-Karp: known graph ─────────────────────────────
    @Test
    public void testEKSimpleGraph() {
        EdmondsKarp ek = new EdmondsKarp();
        ek.solve(simpleGraph(), 0, 3);
        assertEquals(20, ek.getMaxFlow());
    }

    // ── Edmonds-Karp: single path ─────────────────────────────
    @Test
    public void testEKSinglePath() {
        EdmondsKarp ek = new EdmondsKarp();
        ek.solve(singlePathGraph(), 0, 2);
        assertEquals(5, ek.getMaxFlow());
    }

    // ── Edmonds-Karp: null throws ─────────────────────────────
    @Test
    public void testEKNullThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new EdmondsKarp().solve(null, 0, 3));
    }

    // ── Edmonds-Karp: empty throws ────────────────────────────
    @Test
    public void testEKEmptyThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new EdmondsKarp().solve(new int[0][0], 0, 0));
    }

    // ── Edmonds-Karp: negative source throws ──────────────────
    @Test
    public void testEKNegativeSourceThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new EdmondsKarp().solve(simpleGraph(), -1, 3));
    }

    // ── Both algorithms must agree ────────────────────────────
    @Test
    public void testFFandEKAgreeOnSimpleGraph() {
        FordFulkerson ff = new FordFulkerson();
        EdmondsKarp   ek = new EdmondsKarp();
        ff.solve(simpleGraph(), 0, 3);
        ek.solve(simpleGraph(), 0, 3);
        assertEquals(ff.getMaxFlow(), ek.getMaxFlow(),
                "FF and EK must give same result");
    }

    // ── Both agree on generated game network ──────────────────
    @Test
    public void testFFandEKAgreeOnGameNetwork() {
        TrafficGame game = new TrafficGame();
        for (int i = 1; i <= 5; i++) {
            game.newRound(i);
            assertEquals(game.getCorrectAnswer(), game.getEkAnswer(),
                    "FF and EK must match on game network. Round " + i);
        }
    }

    // ── Capacity range 5–15 per edge ─────────────────────────
    @Test
    public void testCapacityRange() {
        TrafficGame game = new TrafficGame();
        game.newRound(1);
        int[][] cap = game.getNetwork().getCapacity();
        for (int[] edge : TrafficNetwork.EDGES) {
            int c = cap[edge[0]][edge[1]];
            assertTrue(c >= 5 && c <= 15,
                    "Edge capacity must be 5–15. Got: " + c);
        }
    }

    // ── Non-edge cells are always 0 ───────────────────────────
    @Test
    public void testNonEdgeCellsAreZero() {
        TrafficGame game = new TrafficGame();
        game.newRound(1);
        int[][] cap = game.getNetwork().getCapacity();
        int n = TrafficNetwork.NODE_COUNT;

        // Build set of valid edges
        boolean[][] isEdge = new boolean[n][n];
        for (int[] e : TrafficNetwork.EDGES) isEdge[e[0]][e[1]] = true;

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (!isEdge[i][j]) {
                    assertEquals(0, cap[i][j],
                            "Non-edge [" + i + "][" + j + "] must be 0");
                }
            }
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

    // ── Timings are non-negative ──────────────────────────────
    @Test
    public void testTimingsNonNegative() {
        TrafficGame game = new TrafficGame();
        game.newRound(1);
        assertTrue(game.getFfTimeMs() >= 0);
        assertTrue(game.getEkTimeMs() >= 0);
    }

    // ── WIN classification ────────────────────────────────────
    @Test
    public void testClassifyWin() {
        TrafficGame game = new TrafficGame();
        game.newRound(1);
        assertEquals("WIN",
                game.classifyAnswer(game.getCorrectAnswer()));
    }

    // ── DRAW classification (±2) ──────────────────────────────
    @Test
    public void testClassifyDraw() {
        TrafficGame game = new TrafficGame();
        game.newRound(1);
        int correct = game.getCorrectAnswer();
        assertEquals("DRAW", game.classifyAnswer(correct + 2));
        if (correct > 2) {
            assertEquals("DRAW", game.classifyAnswer(correct - 2));
        }
    }

    // ── LOSE classification ───────────────────────────────────
    @Test
    public void testClassifyLose() {
        TrafficGame game = new TrafficGame();
        game.newRound(1);
        assertEquals("LOSE",
                game.classifyAnswer(game.getCorrectAnswer() + 10));
    }

    // ── Validate answer: correct ──────────────────────────────
    @Test
    public void testValidateCorrect() {
        TrafficGame game = new TrafficGame();
        game.newRound(1);
        assertTrue(game.validateAnswer(game.getCorrectAnswer()));
    }

    // ── Validate answer: wrong ────────────────────────────────
    @Test
    public void testValidateWrong() {
        TrafficGame game = new TrafficGame();
        game.newRound(1);
        assertFalse(game.validateAnswer(0));
        assertFalse(game.validateAnswer(-5));
    }

    // ── Exactly 13 edges defined ──────────────────────────────
    @Test
    public void testEdgeCount() {
        assertEquals(13, TrafficNetwork.EDGES.length,
                "Traffic network must have exactly 13 edges");
    }

    // ── Source = 0 (A), Sink = 8 (T) ─────────────────────────
    @Test
    public void testSourceAndSinkIndices() {
        assertEquals(0, TrafficNetwork.SOURCE);
        assertEquals(8, TrafficNetwork.SINK);
    }
}