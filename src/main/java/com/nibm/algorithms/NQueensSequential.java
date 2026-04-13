package com.nibm.algorithms;

import java.util.ArrayList;
import java.util.List;

public class NQueensSequential {

    private static final int BOARD_SIZE = 16;
    private List<int[]> solutions;  // each int[] = col positions per row
    private long timeTakenMs;

    /**
     * Finds ALL solutions to the 16-Queens problem.
     * Uses backtracking — single threaded.
     * Time complexity: O(16!) worst case, pruned heavily.
     */
    public void solve() {
        solutions    = new ArrayList<>();
        int[] board  = new int[BOARD_SIZE]; // board[row] = col of queen

        long start   = System.currentTimeMillis();
        backtrack(board, 0);
        timeTakenMs  = System.currentTimeMillis() - start;
    }

    private void backtrack(int[] board, int row) {
        if (row == BOARD_SIZE) {
            solutions.add(board.clone()); // store valid solution
            return;
        }
        for (int col = 0; col < BOARD_SIZE; col++) {
            if (isSafe(board, row, col)) {
                board[row] = col;
                backtrack(board, row + 1);
            }
        }
    }

    // Check if placing queen at (row, col) is safe
    private boolean isSafe(int[] board, int row, int col) {
        for (int r = 0; r < row; r++) {
            int c = board[r];
            // Same column
            if (c == col) return false;
            // Same diagonal
            if (Math.abs(c - col) == Math.abs(r - row)) return false;
        }
        return true;
    }

    public List<int[]> getSolutions()  { return solutions; }
    public int getTotalSolutions()     { return solutions.size(); }
    public long getTimeTakenMs()       { return timeTakenMs; }
    public int getBoardSize()          { return BOARD_SIZE; }
}