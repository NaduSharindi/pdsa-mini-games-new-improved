package com.nibm;

import com.nibm.algorithms.BFSSnakeLadder;
import com.nibm.algorithms.DijkstraSnakeLadder;
import com.nibm.games.SnakeLadderGame;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SnakeLadderTest {

    @Test
    public void testBFSPlainBoard() {
        int total = 36;
        int[] board = new int[total + 1];
        for (int i = 1; i <= total; i++) board[i] = i;

        BFSSnakeLadder bfs = new BFSSnakeLadder();
        bfs.solve(board, total);

        assertTrue(bfs.getMinThrows() > 0);
    }

    @Test
    public void testDijkstraMatchesBFS() {
        int total = 36;
        int[] board = new int[total + 1];
        for (int i = 1; i <= total; i++) board[i] = i;
        board[2] = 20;

        BFSSnakeLadder bfs = new BFSSnakeLadder();
        bfs.solve(board, total);

        DijkstraSnakeLadder dijkstra = new DijkstraSnakeLadder();
        dijkstra.solve(board, total);

        assertEquals(bfs.getMinThrows(), dijkstra.getMinThrows());
    }

    @Test
    public void testNewRoundCreatesBoard() {
        SnakeLadderGame game = new SnakeLadderGame();
        game.newRound(1, 8);

        assertEquals(8, game.getN());
        assertEquals(64, game.getTotalCells());
        assertNotNull(game.getBoard());
        assertEquals(6, game.getSnakes().length);
        assertEquals(6, game.getLadders().length);
    }

    @Test
    public void testPlayerStartsAtCellOne() {
        SnakeLadderGame game = new SnakeLadderGame();
        game.newRound(1, 8);

        assertEquals(1, game.getPlayerPosition());
        assertEquals(0, game.getPlayerThrows());
        assertFalse(game.isGameOver());
    }

    @Test
    public void testRollDiceIncrementsThrows() {
        SnakeLadderGame game = new SnakeLadderGame();
        game.newRound(1, 8);

        game.rollDice();

        assertEquals(1, game.getPlayerThrows());
        assertTrue(game.getPlayerPosition() >= 1);
    }

    @Test
    public void testAlgorithmAnswersMatch() {
        SnakeLadderGame game = new SnakeLadderGame();
        game.newRound(1, 8);

        assertEquals(game.getCorrectAnswer(), game.getDijkstraAnswer());
    }
}