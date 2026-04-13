package com.nibm;

import com.nibm.algorithms.BFSSnakeLadder;
import com.nibm.algorithms.DijkstraSnakeLadder;
import com.nibm.games.SnakeLadderGame;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SnakeLadderTest {

    // ── BFS: plain board no snakes/ladders ────────────────────
    @Test
    public void testBFSPlainBoard() {
        int total = 36;
        int[] board = new int[total + 1];
        for (int i = 1; i <= total; i++) board[i] = i;
        BFSSnakeLadder bfs = new BFSSnakeLadder();
        bfs.solve(board, total);
        assertTrue(bfs.getMinThrows() >= 6,
                "Plain 6x6 board needs at least 6 throws");
    }

    // ── BFS: ladder shortcut reduces throws ───────────────────
    @Test
    public void testBFSLadderHelps() {
        int total = 36;
        int[] plain = new int[total + 1];
        int[] withLadder = new int[total + 1];
        for (int i = 1; i <= total; i++) {
            plain[i] = i;
            withLadder[i] = i;
        }
        withLadder[2] = 30; // big ladder from cell 2

        BFSSnakeLadder bfsPlain   = new BFSSnakeLadder();
        BFSSnakeLadder bfsLadder  = new BFSSnakeLadder();
        bfsPlain.solve(plain, total);
        bfsLadder.solve(withLadder, total);

        assertTrue(bfsLadder.getMinThrows() <= bfsPlain.getMinThrows(),
                "Ladder should reduce or equal minimum throws");
    }

    // ── BFS: null throws ─────────────────────────────────────
    @Test
    public void testBFSNullThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new BFSSnakeLadder().solve(null, 36));
    }

    // ── BFS: zero totalCells throws ───────────────────────────
    @Test
    public void testBFSZeroCellsThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new BFSSnakeLadder().solve(new int[1], 0));
    }

    // ── Dijkstra: matches BFS ─────────────────────────────────
    @Test
    public void testDijkstraMatchesBFS() {
        int total = 36;
        int[] board = new int[total + 1];
        for (int i = 1; i <= total; i++) board[i] = i;
        board[4] = 20; // ladder

        BFSSnakeLadder bfs = new BFSSnakeLadder();
        bfs.solve(board, total);

        DijkstraSnakeLadder dijk = new DijkstraSnakeLadder();
        dijk.solve(board, total);

        assertEquals(bfs.getMinThrows(), dijk.getMinThrows(),
                "BFS and Dijkstra must agree on same board");
    }

    // ── Dijkstra: null throws ─────────────────────────────────
    @Test
    public void testDijkstraNullThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new DijkstraSnakeLadder().solve(null, 36));
    }

    // ── Game: N below range throws ────────────────────────────
    @Test
    public void testNBelowRangeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new SnakeLadderGame().newRound(1, 5));
    }

    // ── Game: N above range throws ────────────────────────────
    @Test
    public void testNAboveRangeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new SnakeLadderGame().newRound(1, 13));
    }

    // ── Game: snake count = N-2 for all valid N ───────────────
    @Test
    public void testSnakeCount() {
        SnakeLadderGame game = new SnakeLadderGame();
        for (int n = 6; n <= 12; n++) {
            game.newRound(1, n);
            assertEquals(n - 2, game.getSnakes().length,
                    "Snake count must be N-2 for N=" + n);
        }
    }

    // ── Game: ladder count = N-2 for all valid N ──────────────
    @Test
    public void testLadderCount() {
        SnakeLadderGame game = new SnakeLadderGame();
        for (int n = 6; n <= 12; n++) {
            game.newRound(1, n);
            assertEquals(n - 2, game.getLadders().length,
                    "Ladder count must be N-2 for N=" + n);
        }
    }

    // ── Game: ladder base < top ───────────────────────────────
    @Test
    public void testLadderGoesUp() {
        SnakeLadderGame game = new SnakeLadderGame();
        game.newRound(1, 8);
        for (int[] l : game.getLadders()) {
            assertTrue(l[0] < l[1],
                    "Ladder base must be < top. Got: "
                            + l[0] + " -> " + l[1]);
        }
    }

    // ── Game: snake head > tail ───────────────────────────────
    @Test
    public void testSnakeGoesDown() {
        SnakeLadderGame game = new SnakeLadderGame();
        game.newRound(1, 8);
        for (int[] s : game.getSnakes()) {
            assertTrue(s[0] > s[1],
                    "Snake head must be > tail. Got: "
                            + s[0] + " -> " + s[1]);
        }
    }

    // ── Game: no cell 1 or N² used by snake/ladder ────────────
    @Test
    public void testNoCellOneOrLastUsed() {
        SnakeLadderGame game = new SnakeLadderGame();
        game.newRound(1, 8);
        int last = 64;
        for (int[] s : game.getSnakes()) {
            assertNotEquals(1,    s[0], "Snake head at cell 1");
            assertNotEquals(1,    s[1], "Snake tail at cell 1");
            assertNotEquals(last, s[0], "Snake head at last cell");
            assertNotEquals(last, s[1], "Snake tail at last cell");
        }
        for (int[] l : game.getLadders()) {
            assertNotEquals(1,    l[0], "Ladder base at cell 1");
            assertNotEquals(1,    l[1], "Ladder top at cell 1");
            assertNotEquals(last, l[0], "Ladder base at last cell");
            assertNotEquals(last, l[1], "Ladder top at last cell");
        }
    }

    // ── Game: 3 choices generated, all positive, all distinct ─
    @Test
    public void testChoicesPositiveAndDistinct() {
        SnakeLadderGame game = new SnakeLadderGame();
        game.newRound(1, 8);
        int[] choices = game.generateChoices();
        assertEquals(3, choices.length);
        // All positive
        for (int c : choices) assertTrue(c > 0, "Choice must be > 0: " + c);
        // All distinct
        assertNotEquals(choices[0], choices[1]);
        assertNotEquals(choices[1], choices[2]);
        assertNotEquals(choices[0], choices[2]);
    }

    // ── Game: choices contain correct answer ──────────────────
    @Test
    public void testChoicesContainCorrect() {
        SnakeLadderGame game = new SnakeLadderGame();
        game.newRound(1, 8);
        int[] choices = game.generateChoices();
        boolean found = false;
        for (int c : choices) {
            if (c == game.getCorrectAnswer()) { found = true; break; }
        }
        assertTrue(found, "Choices must include correct answer");
    }

    // ── Game: WIN classification ──────────────────────────────
    @Test
    public void testClassifyWin() {
        SnakeLadderGame game = new SnakeLadderGame();
        game.newRound(1, 8);
        assertEquals("WIN", game.classifyAnswer(game.getCorrectAnswer()));
    }

    // ── Game: DRAW classification (±1) ───────────────────────
    @Test
    public void testClassifyDraw() {
        SnakeLadderGame game = new SnakeLadderGame();
        game.newRound(1, 8);
        int correct = game.getCorrectAnswer();
        assertEquals("DRAW", game.classifyAnswer(correct + 1));
        if (correct > 1) {
            assertEquals("DRAW", game.classifyAnswer(correct - 1));
        }
    }

    // ── Game: LOSE classification ─────────────────────────────
    @Test
    public void testClassifyLose() {
        SnakeLadderGame game = new SnakeLadderGame();
        game.newRound(1, 8);
        assertEquals("LOSE", game.classifyAnswer(
                game.getCorrectAnswer() + 10));
    }

    // ── Game: min throws is always positive ───────────────────
    @Test
    public void testMinThrowsPositive() {
        SnakeLadderGame game = new SnakeLadderGame();
        game.newRound(1, 8);
        assertTrue(game.getCorrectAnswer() > 0);
    }

    // ── Game: BFS == Dijkstra on game board ───────────────────
    @Test
    public void testBFSEqualsDijkstraOnGameBoard() {
        SnakeLadderGame game = new SnakeLadderGame();
        for (int i = 1; i <= 5; i++) {
            game.newRound(i, 8);
            assertEquals(game.getCorrectAnswer(),
                    game.getDijkstraAnswer(),
                    "BFS and Dijkstra must match. Round " + i);
        }
    }
}