package com.nibm;

import com.nibm.algorithms.EdmondsKarp;
import com.nibm.algorithms.FordFulkerson;
import com.nibm.games.TrafficGame;
import com.nibm.models.TrafficNetwork;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TrafficTest {

    private int[][] simpleGraph() {
        int[][] cap = new int[4][4];
        cap[0][1] = 10;
        cap[0][2] = 10;
        cap[1][3] = 10;
        cap[2][3] = 10;
        return cap;
    }

    @Test
    public void testFFSimpleGraph() {
        FordFulkerson ff = new FordFulkerson();
        ff.solve(simpleGraph(), 0, 3);
        assertEquals(20, ff.getMaxFlow());
    }

    @Test
    public void testEKSimpleGraph() {
        EdmondsKarp ek = new EdmondsKarp();
        ek.solve(simpleGraph(), 0, 3);
        assertEquals(20, ek.getMaxFlow());
    }

    @Test
    public void testFFandEKAgreeOnGameNetwork() {
        TrafficGame game = new TrafficGame();
        for (int i = 1; i <= 5; i++) {
            game.newRound(i);
            assertEquals(game.getCorrectAnswer(), game.getEkAnswer());
        }
    }

    @Test
    public void testCapacityRange() {
        TrafficGame game = new TrafficGame();
        game.newRound(1);
        int[][] cap = game.getNetwork().getCapacity();

        for (int[] edge : TrafficNetwork.EDGES) {
            int c = cap[edge[0]][edge[1]];
            assertTrue(c >= 5 && c <= 15);
        }
    }

    @Test
    public void testPlayerStartsAtZeroFlow() {
        TrafficGame game = new TrafficGame();
        game.newRound(1);
        assertEquals(0, game.getPlayerFlow());
        assertFalse(game.isFinished());
    }

    @Test
    public void testCannotStartPathFromWrongNode() {
        TrafficGame game = new TrafficGame();
        game.newRound(1);

        assertFalse(game.addNodeToPath(1));
        assertTrue(game.addNodeToPath(TrafficNetwork.SOURCE));
    }

    @Test
    public void testClearPath() {
        TrafficGame game = new TrafficGame();
        game.newRound(1);

        game.addNodeToPath(TrafficNetwork.SOURCE);
        game.clearCurrentPath();

        assertTrue(game.getSelectedPath().isEmpty());
    }

    @Test
    public void testResidualReverseEdgeIsCreatedAfterApply() {
        TrafficGame game = new TrafficGame();
        game.newRound(1);

        assertTrue(game.addNodeToPath(0));

        // Find one valid full path automatically based on graph structure
        int[][] original = game.getOriginalCapacity();

        int next1 = -1, next2 = -1, next3 = -1;

        for (int v = 0; v < original.length; v++) {
            if (original[0][v] > 0) {
                for (int x = 0; x < original.length; x++) {
                    if (original[v][x] > 0) {
                        for (int y = 0; y < original.length; y++) {
                            if (original[x][y] > 0 && y == TrafficNetwork.SINK) {
                                next1 = v;
                                next2 = x;
                                next3 = y;
                                break;
                            }
                        }
                        if (next3 != -1) break;
                    }
                }
                if (next3 != -1) break;
            }
        }

        assertTrue(next1 != -1 && next2 != -1 && next3 != -1);

        assertTrue(game.addNodeToPath(next1));
        assertTrue(game.addNodeToPath(next2));
        assertTrue(game.addNodeToPath(next3));

        int bottleneck = game.getCurrentPathBottleneck();
        assertTrue(bottleneck > 0);

        assertTrue(game.applyCurrentPath());

        int[][] residual = game.getResidualCapacity();
        assertTrue(residual[next1][0] > 0 || residual[next2][next1] > 0);
    }
}