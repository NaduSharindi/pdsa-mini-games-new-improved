package com.nibm;

import com.nibm.algorithms.BacktrackKnight;
import com.nibm.algorithms.WarnsdorffKnight;
import com.nibm.games.KnightTourGame;
import com.nibm.models.KnightMove;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

public class KnightTest {

    // ── Warnsdorff: 8×8 solves from corner ───────────────────
    @Test
    public void testWarnsdorff8x8Solves() {
        WarnsdorffKnight w = new WarnsdorffKnight();
        w.solve(8, 0, 0);
        assertTrue(w.isSolved());
    }

    // ── Warnsdorff: all 64 cells visited ────────────────────
    @Test
    public void testWarnsdorffAllCellsVisited() {
        WarnsdorffKnight w = new WarnsdorffKnight();
        w.solve(8, 3, 3);
        assertTrue(w.isSolved());
        int[][] tour = w.getTour();
        for (int r = 0; r < 8; r++)
            for (int c = 0; c < 8; c++)
                assertTrue(tour[r][c] >= 1 && tour[r][c] <= 64,
                        "Cell (" + r + "," + c + ") unvisited");
    }

    // ── Warnsdorff: bad size throws ──────────────────────────
    @Test
    public void testWarnsdorffBadSize() {
        assertThrows(IllegalArgumentException.class,
                () -> new WarnsdorffKnight().solve(10, 0, 0));
    }

    // ── Warnsdorff: bad start throws ─────────────────────────
    @Test
    public void testWarnsdorffBadStart() {
        assertThrows(IllegalArgumentException.class,
                () -> new WarnsdorffKnight().solve(8, 8, 0));
    }

    // ── Backtracking: 8×8 solves ─────────────────────────────
    @Test
    public void testBacktrack8x8Solves() {
        BacktrackKnight b = new BacktrackKnight();
        b.solve(8, 0, 0);
        assertTrue(b.isSolved());
    }

    // ── Backtracking: no duplicate move numbers ───────────────
    @Test
    public void testBacktrackNoDuplicates() {
        BacktrackKnight b = new BacktrackKnight();
        b.solve(8, 0, 0);
        assertTrue(b.isSolved());
        boolean[] seen = new boolean[65];
        for (int r = 0; r < 8; r++)
            for (int c = 0; c < 8; c++) {
                int v = b.getTour()[r][c];
                assertFalse(seen[v], "Duplicate move: " + v);
                seen[v] = true;
            }
    }

    // ── Backtracking: bad size throws ────────────────────────
    @Test
    public void testBacktrackBadSize() {
        assertThrows(IllegalArgumentException.class,
                () -> new BacktrackKnight().solve(9, 0, 0));
    }

    // ── Game: invalid size throws ─────────────────────────────
    @Test
    public void testGameBadSize() {
        assertThrows(IllegalArgumentException.class,
                () -> new KnightTourGame().newRound(1, 5));
    }

    // ── Game: start in bounds ────────────────────────────────
    @Test
    public void testStartInBounds() {
        KnightTourGame g = new KnightTourGame();
        g.newRound(1, 8);
        assertTrue(g.getStartRow() >= 0 && g.getStartRow() < 8);
        assertTrue(g.getStartCol() >= 0 && g.getStartCol() < 8);
    }

    // ── Game: valid click accepted ────────────────────────────
    @Test
    public void testValidClickAccepted() {
        KnightTourGame g = new KnightTourGame();
        g.newRound(1, 8);
        // Find move #2 in tour — that is a valid next click
        int[][] tour = g.getWarnsdorffTour();
        for (int r = 0; r < 8; r++)
            for (int c = 0; c < 8; c++)
                if (tour[r][c] == 2) {
                    String result = g.processPlayerClick(r, c);
                    assertTrue(result.equals("VALID")
                                    || result.equals("WIN"),
                            "Move #2 should be VALID");
                    return;
                }
    }

    // ── Game: invalid L-shape rejected ───────────────────────
    @Test
    public void testInvalidMoveRejected() {
        KnightTourGame g = new KnightTourGame();
        g.newRound(1, 8);
        // Find a cell that is NOT a valid knight move from start
        int sr = g.getStartRow();
        int sc = g.getStartCol();
        // Adjacent cell is never a valid knight move
        int nr = (sr + 1) % 8;
        int nc = sc;
        if (nr == sr && nc == sc) nc = (sc + 1) % 8;
        String result = g.processPlayerClick(nr, nc);
        assertEquals("INVALID_MOVE", result);
    }

    // ── Game: already visited rejected ────────────────────────
    @Test
    public void testAlreadyVisitedRejected() {
        KnightTourGame g = new KnightTourGame();
        g.newRound(1, 8);
        // Clicking start again = ALREADY_VISITED
        String result = g.processPlayerClick(
                g.getStartRow(), g.getStartCol());
        assertEquals("ALREADY_VISITED", result);
    }

    // ── Game: isValidTour accepts correct sequence ────────────
    @Test
    public void testIsValidTourAcceptsCorrect() {
        KnightTourGame g = new KnightTourGame();
        g.newRound(1, 8);
        List<KnightMove> moves = g.getTourAsMoves(g.getWarnsdorffTour());
        assertTrue(g.isValidTour(moves));
    }

    // ── Game: isValidTour rejects null ────────────────────────
    @Test
    public void testIsValidTourRejectsNull() {
        KnightTourGame g = new KnightTourGame();
        g.newRound(1, 8);
        assertFalse(g.isValidTour(null));
    }

    // ── Game: move list = N² moves ───────────────────────────
    @Test
    public void testMoveListSize() {
        KnightTourGame g = new KnightTourGame();
        g.newRound(1, 8);
        if (g.isWarnsdorffSolved()) {
            assertEquals(64,
                    g.getTourAsMoves(g.getWarnsdorffTour()).size());
        }
    }
}