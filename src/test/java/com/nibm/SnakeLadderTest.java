package com.nibm;

import com.nibm.algorithms.BFSSnakeLadder;
import com.nibm.algorithms.DijkstraSnakeLadder;
import com.nibm.games.SnakeLadderGame;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SnakeLadderTest {

    // ── BFS: simple known board ───────────────────────────────
    @Test
    public void testBFSSimpleBoard() {
        // 6x6 board, no snakes or ladders
        int total = 36;
        int[] board = new int[total + 1];
        for (int i = 1; i <= total; i++) board[i] = i;

        BFSSnakeLadder bfs = new BFSSnakeLadder();
        bfs.solve(board, total);
        // Min throws on plain board >= 6 (ceil(35/6))
        assertTrue(bfs.getMinThrows() >= 6,
                "Min throws on plain board should be >= 6");
    }

    // ── BFS: null board throws exception ──────────────────────
    @Test
    public void testBFSNullThrows() {
        BFSSnakeLadder bfs = new BFSSnakeLadder();
        assertThrows(IllegalArgumentException.class,
                () -> bfs.solve(null, 36));
    }

    // ── Dijkstra: matches BFS on same board ───────────────────
    @Test
    public void testDijkstraMatchesBFS() {
        int total = 36;
        int[] board = new int[total + 1];
        for (int i = 1; i <= total; i++) board[i] = i;
        // Add one ladder: cell 5 -> cell 20
        board[5] = 20;

        BFSSnakeLadder bfs = new BFSSnakeLadder();
        bfs.solve(board, total);

        DijkstraSnakeLadder dijk = new DijkstraSnakeLadder();
        dijk.solve(board, total);

        assertEquals(bfs.getMinThrows(), dijk.getMinThrows(),
                "BFS and Dijkstra must give same answer on same board");
    }

    // ── Dijkstra: null board throws exception ─────────────────
    @Test
    public void testDijkstraNullThrows() {
        DijkstraSnakeLadder d = new DijkstraSnakeLadder();
        assertThrows(IllegalArgumentException.class,
                () -> d.solve(null, 36));
    }

    // ── Game: N stays in valid range ──────────────────────────
    @Test
    public void testNOutOfRangeThrows() {
        SnakeLadderGame game = new SnakeLadderGame();
        assertThrows(IllegalArgumentException.class,
                () -> game.newRound(1, 5));   // too small
        assertThrows(IllegalArgumentException.class,
                () -> game.newRound(1, 13));  // too large
    }

    // ── Game: snake and ladder counts correct ─────────────────
    @Test
    public void testSnakeLadderCounts() {
        SnakeLadderGame game = new SnakeLadderGame();
        for (int n = 6; n <= 12; n++) {
            game.newRound(1, n);
            assertEquals(n - 2, game.getSnakes().length,
                    "Snake count must be N-2 for N=" + n);
            assertEquals(n - 2, game.getLadders().length,
                    "Ladder count must be N-2 for N=" + n);
        }
    }

    // ── Game: correct answer is positive ─────────────────────
    @Test
    public void testAnswerIsPositive() {
        SnakeLadderGame game = new SnakeLadderGame();
        game.newRound(1, 8);
        assertTrue(game.getCorrectAnswer() > 0,
                "Min throws must be positive");
    }

    // ── Game: 3 choices generated correctly ──────────────────
    @Test
    public void testChoicesContainCorrect() {
        SnakeLadderGame game = new SnakeLadderGame();
        game.newRound(1, 8);
        int[] choices = game.generateChoices();
        assertEquals(3, choices.length);

        boolean found = false;
        for (int c : choices) {
            if (c == game.getCorrectAnswer()) { found = true; break; }
        }
        assertTrue(found, "Choices must contain the correct answer");
    }

    // ── Game: validate answer ─────────────────────────────────
    @Test
    public void testValidateAnswer() {
        SnakeLadderGame game = new SnakeLadderGame();
        game.newRound(1, 8);
        assertTrue(game.validateAnswer(game.getCorrectAnswer()));
        assertFalse(game.validateAnswer(-1));
    }

    // ── Game: BFS == Dijkstra on generated board ──────────────
    @Test
    public void testBFSEqualsDijkstraOnGame() {
        SnakeLadderGame game = new SnakeLadderGame();
        for (int i = 1; i <= 5; i++) {
            game.newRound(i, 8);
            assertEquals(game.getCorrectAnswer(), game.getDijkstraAnswer(),
                    "BFS and Dijkstra answers must match. Round " + i);
        }
    }
}