package com.nibm;

import com.nibm.algorithms.NQueensSequential;
import com.nibm.algorithms.NQueensThreaded;
import com.nibm.games.QueensPuzzleGame;
import com.nibm.db.QueensSolutionRepository;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

public class QueensTest {

    // ── Sequential: produces solutions ───────────────────────
    @Test
    public void testSequentialProducesSolutions() {
        NQueensSequential seq = new NQueensSequential();
        seq.solve();
        assertTrue(seq.getTotalSolutions() > 0,
                "Sequential must find at least one solution");
    }

    // ── Sequential: board size = 16 ──────────────────────────
    @Test
    public void testSequentialBoardSize() {
        NQueensSequential seq = new NQueensSequential();
        assertEquals(16, seq.getBoardSize());
    }

    // ── Sequential: timing is non-negative ───────────────────
    @Test
    public void testSequentialTimingNonNegative() {
        NQueensSequential seq = new NQueensSequential();
        seq.solve();
        assertTrue(seq.getTimeTakenMs() >= 0);
    }

    // ── Sequential: each solution has 16 queens ───────────────
    @Test
    public void testSequentialSolutionLength() {
        NQueensSequential seq = new NQueensSequential();
        seq.solve();
        for (int[] sol : seq.getSolutions()) {
            assertEquals(16, sol.length,
                    "Each solution must have 16 positions");
        }
    }

    // ── Sequential: all solutions are valid ───────────────────
    @Test
    public void testSequentialAllSolutionsValid() {
        NQueensSequential seq = new NQueensSequential();
        seq.solve();
        QueensPuzzleGame dummy =
                new QueensPuzzleGame(null); // null repo — only using validator

        // Check first 10 solutions to keep test fast
        List<int[]> solutions = seq.getSolutions();
        int check = Math.min(10, solutions.size());
        for (int i = 0; i < check; i++) {
            assertTrue(dummy.isValidQueenPlacement(solutions.get(i)),
                    "Solution " + i + " must be valid");
        }
    }

    // ── Threaded: produces same count as sequential ───────────
    @Test
    public void testThreadedMatchesSequentialCount() {
        NQueensSequential seq = new NQueensSequential();
        seq.solve();

        NQueensThreaded thr = new NQueensThreaded();
        thr.solve();

        assertEquals(seq.getTotalSolutions(), thr.getTotalSolutions(),
                "Sequential and Threaded must find the same total solutions");
    }

    // ── Threaded: timing is non-negative ─────────────────────
    @Test
    public void testThreadedTimingNonNegative() {
        NQueensThreaded thr = new NQueensThreaded();
        thr.solve();
        assertTrue(thr.getTimeTakenMs() >= 0);
    }

    // ── Threaded: all solutions are valid ─────────────────────
    @Test
    public void testThreadedAllSolutionsValid() {
        NQueensThreaded thr = new NQueensThreaded();
        thr.solve();
        QueensPuzzleGame dummy = new QueensPuzzleGame(null);

        List<int[]> solutions = thr.getSolutions();
        int check = Math.min(10, solutions.size());
        for (int i = 0; i < check; i++) {
            assertTrue(dummy.isValidQueenPlacement(solutions.get(i)),
                    "Threaded solution " + i + " must be valid");
        }
    }

    // ── Validator: known valid 8-queen solution ───────────────
    // Using 8-queen solution scaled to verify logic
    @Test
    public void testValidatorAcceptsKnownGoodSolution() {
        QueensPuzzleGame game = new QueensPuzzleGame(null);

        // Get a real 16-queen solution from sequential solver
        NQueensSequential seq = new NQueensSequential();
        seq.solve();
        assertTrue(seq.getTotalSolutions() > 0);

        int[] validSol = seq.getSolutions().get(0);
        assertTrue(game.isValidQueenPlacement(validSol),
                "Validator must accept a known valid solution");
    }

    // ── Validator: null rejected ──────────────────────────────
    @Test
    public void testValidatorRejectsNull() {
        QueensPuzzleGame game = new QueensPuzzleGame(null);
        assertFalse(game.isValidQueenPlacement(null));
    }

    // ── Validator: wrong length rejected ─────────────────────
    @Test
    public void testValidatorRejectsWrongLength() {
        QueensPuzzleGame game = new QueensPuzzleGame(null);
        assertFalse(game.isValidQueenPlacement(new int[8]));
    }

    // ── Validator: all queens in same column rejected ─────────
    @Test
    public void testValidatorRejectsSameColumn() {
        QueensPuzzleGame game = new QueensPuzzleGame(null);
        int[] allSameCol = new int[16]; // all 0 → same column
        assertFalse(game.isValidQueenPlacement(allSameCol));
    }

    // ── Validator: diagonal conflict rejected ─────────────────
    @Test
    public void testValidatorRejectsDiagonalConflict() {
        QueensPuzzleGame game = new QueensPuzzleGame(null);
        // Queens at (0,0) and (1,1) are on same diagonal
        int[] diagonal = new int[16];
        diagonal[0] = 0;
        diagonal[1] = 1; // diagonal conflict with row 0
        for (int i = 2; i < 16; i++) diagonal[i] = i + 1;
        assertFalse(game.isValidQueenPlacement(diagonal));
    }

    // ── Validator: out-of-bounds col rejected ─────────────────
    @Test
    public void testValidatorRejectsOutOfBoundsCol() {
        QueensPuzzleGame game = new QueensPuzzleGame(null);
        int[] bad = new int[16];
        bad[0] = 99; // col 99 is out of bounds
        assertFalse(game.isValidQueenPlacement(bad));
    }

    // ── Sequential solutions list not null ────────────────────
    @Test
    public void testSequentialSolutionsNotNull() {
        NQueensSequential seq = new NQueensSequential();
        seq.solve();
        assertNotNull(seq.getSolutions());
    }

    // ── Threaded solutions list not null ──────────────────────
    @Test
    public void testThreadedSolutionsNotNull() {
        NQueensThreaded thr = new NQueensThreaded();
        thr.solve();
        assertNotNull(thr.getSolutions());
    }
}