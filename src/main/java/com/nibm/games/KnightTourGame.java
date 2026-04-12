package com.nibm.games;

import com.nibm.algorithms.BacktrackKnight;
import com.nibm.algorithms.WarnsdorffKnight;
import com.nibm.models.KnightMove;
import com.nibm.models.KnightRound;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class KnightTourGame {

    // All 8 knight move offsets
    private static final int[] DX = {-2,-1, 1, 2, 2, 1,-1,-2};
    private static final int[] DY = { 1, 2, 2, 1,-1,-2,-2,-1};

    private int boardSize;
    private int startRow;
    private int startCol;
    private int[][] warnsdorffTour;
    private int[][] backtrackTour;
    private long warnsdorffTimeMs;
    private long backtrackTimeMs;
    private boolean warnsdorffSolved;
    private boolean backtrackSolved;

    // Player's current sequence of clicks
    private List<KnightMove> playerSequence;
    private boolean[][] playerVisited;
    private int lastRow, lastCol;

    private final Random random = new Random();

    // ── Start new round ───────────────────────────────────────
    public KnightRound newRound(int roundNumber, int boardSize) {
        if (boardSize != 8 && boardSize != 16) {
            throw new IllegalArgumentException(
                    "Board size must be 8 or 16. Got: " + boardSize);
        }

        this.boardSize = boardSize;

        // Random start position
        this.startRow = random.nextInt(boardSize);
        this.startCol = random.nextInt(boardSize);

        // Reset player state
        resetPlayerState();

        // Run Warnsdorff — timed
        WarnsdorffKnight warnsdorff = new WarnsdorffKnight();
        long wStart = System.currentTimeMillis();
        warnsdorff.solve(boardSize, startRow, startCol);
        warnsdorffTimeMs = System.currentTimeMillis() - wStart;
        warnsdorffTour   = warnsdorff.getTour();
        warnsdorffSolved = warnsdorff.isSolved();

        // Run Backtracking — timed
        BacktrackKnight backtrack = new BacktrackKnight();
        long bStart = System.currentTimeMillis();
        backtrack.solve(boardSize, startRow, startCol);
        backtrackTimeMs = System.currentTimeMillis() - bStart;
        backtrackTour   = backtrack.getTour();
        backtrackSolved = backtrack.isSolved();

        // Build round model
        KnightRound round = new KnightRound();
        round.setRoundNumber(roundNumber);
        round.setBoardSize(boardSize);
        round.setStartRow(startRow);
        round.setStartCol(startCol);
        round.setWarnsdorffTour(warnsdorffTour);
        round.setBacktrackTour(backtrackTour);
        round.setWarnsdorffTimeMs(warnsdorffTimeMs);
        round.setBacktrackTimeMs(backtrackTimeMs);
        round.setWarnsdorffSolved(warnsdorffSolved);
        round.setBacktrackSolved(backtrackSolved);
        return round;
    }

    // ── Reset player sequence for new round ───────────────────
    private void resetPlayerState() {
        playerSequence  = new ArrayList<>();
        playerVisited   = new boolean[boardSize][boardSize];

        // Move 1 is always the start square
        playerVisited[startRow][startCol] = true;
        playerSequence.add(new KnightMove(startRow, startCol, 1));
        lastRow = startRow;
        lastCol = startCol;
    }

    // ── Called each time player clicks a square ───────────────
    // Returns: "VALID", "INVALID_MOVE", "ALREADY_VISITED", "WIN"
    public String processPlayerClick(int row, int col) {
        // Boundary check
        if (row < 0 || row >= boardSize || col < 0 || col >= boardSize) {
            return "OUT_OF_BOUNDS";
        }

        // Can't click start square again (already move #1)
        if (row == startRow && col == startCol
                && playerSequence.size() == 1) {
            return "ALREADY_VISITED";
        }

        // Check already visited
        if (playerVisited[row][col]) {
            return "ALREADY_VISITED";
        }

        // Check valid knight L-shape move from last position
        if (!isValidKnightMove(lastRow, lastCol, row, col)) {
            return "INVALID_MOVE";
        }

        // Accept the move
        int moveNum = playerSequence.size() + 1;
        playerVisited[row][col] = true;
        playerSequence.add(new KnightMove(row, col, moveNum));
        lastRow = row;
        lastCol = col;

        // Check if tour is complete
        if (playerSequence.size() == boardSize * boardSize) {
            return "WIN";
        }

        return "VALID";
    }

    // ── Validate L-shape knight move ──────────────────────────
    private boolean isValidKnightMove(int r1, int c1, int r2, int c2) {
        int dr = Math.abs(r1 - r2);
        int dc = Math.abs(c1 - c2);
        return (dr == 2 && dc == 1) || (dr == 1 && dc == 2);
    }

    // ── Check if player sequence is a valid knight's tour ─────
    // Used for final validation before saving to DB
    public boolean isValidTour(List<KnightMove> sequence) {
        if (sequence == null || sequence.size() != boardSize * boardSize) {
            return false;
        }

        boolean[][] visited = new boolean[boardSize][boardSize];
        KnightMove first = sequence.get(0);

        // First move must be start position
        if (first.getRow() != startRow || first.getCol() != startCol) {
            return false;
        }

        visited[first.getRow()][first.getCol()] = true;

        for (int i = 1; i < sequence.size(); i++) {
            KnightMove prev = sequence.get(i - 1);
            KnightMove curr = sequence.get(i);

            // Each step must be a valid knight move
            if (!isValidKnightMove(prev.getRow(), prev.getCol(),
                    curr.getRow(), curr.getCol())) {
                return false;
            }

            // No revisiting
            if (visited[curr.getRow()][curr.getCol()]) {
                return false;
            }

            visited[curr.getRow()][curr.getCol()] = true;
        }

        return true;
    }

    // ── Convert tour array to ordered move list ───────────────
    public List<KnightMove> getTourAsMoves(int[][] tour) {
        int total = boardSize * boardSize;
        KnightMove[] moves = new KnightMove[total + 1];

        for (int r = 0; r < boardSize; r++) {
            for (int c = 0; c < boardSize; c++) {
                int num = tour[r][c];
                if (num > 0) {
                    moves[num] = new KnightMove(r, c, num);
                }
            }
        }

        List<KnightMove> list = new ArrayList<>();
        for (int i = 1; i <= total; i++) {
            if (moves[i] != null) list.add(moves[i]);
        }
        return list;
    }

    // ── Format player sequence as readable string for DB ──────
    public String formatSequenceForDB(List<KnightMove> sequence) {
        StringBuilder sb = new StringBuilder();
        for (KnightMove m : sequence) {
            sb.append("(").append(m.getRow())
                    .append(",").append(m.getCol()).append(")");
            if (m.getMoveNumber() < sequence.size()) sb.append("->");
        }
        return sb.toString();
    }

    // ── Getters ───────────────────────────────────────────────
    public int getBoardSize()               { return boardSize; }
    public int getStartRow()                { return startRow; }
    public int getStartCol()                { return startCol; }
    public int[][] getWarnsdorffTour()      { return warnsdorffTour; }
    public int[][] getBacktrackTour()       { return backtrackTour; }
    public long getWarnsdorffTimeMs()       { return warnsdorffTimeMs; }
    public long getBacktrackTimeMs()        { return backtrackTimeMs; }
    public boolean isWarnsdorffSolved()     { return warnsdorffSolved; }
    public boolean isBacktrackSolved()      { return backtrackSolved; }
    public List<KnightMove> getPlayerSequence() { return playerSequence; }
    public int getPlayerMoveCount()         { return playerSequence.size(); }
}