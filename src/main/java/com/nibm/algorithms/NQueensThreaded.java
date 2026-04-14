package com.nibm.algorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class NQueensThreaded {

    private static final int BOARD_SIZE   = 8;
    private static final int THREAD_COUNT = 8; // one thread per first-col

    private List<int[]> solutions;
    private long timeTakenMs;

    /**
     * Finds ALL solutions using multiple threads.
     * Strategy: assign each possible column for the FIRST row
     * to a separate thread. Each thread independently backtracks
     * from its assigned starting column.
     * This divides the search space evenly across threads.
     */
    public void solve() {
        // Thread-safe list to collect solutions from all threads
        List<int[]> combined = Collections.synchronizedList(
                new ArrayList<>());

        ExecutorService executor =
                Executors.newFixedThreadPool(THREAD_COUNT);
        List<Future<List<int[]>>> futures = new ArrayList<>();

        long start  = System.nanoTime();

        // Submit one task per starting column in row 0
        for (int startCol = 0; startCol < BOARD_SIZE; startCol++) {
            final int col = startCol;
            Callable<List<int[]>> task = () -> {
                List<int[]> partial = new ArrayList<>();
                int[] board         = new int[BOARD_SIZE];
                board[0]            = col;  // fix first queen column
                backtrack(board, 1, partial);
                return partial;
            };
            futures.add(executor.submit(task));
        }

        // Collect results from all threads
        for (Future<List<int[]>> future : futures) {
            try {
                combined.addAll(future.get());
            } catch (Exception e) {
                System.err.println("Thread error: " + e.getMessage());
            }
        }

        executor.shutdown();
        timeTakenMs = System.nanoTime() - start;
        solutions   = new ArrayList<>(combined);
    }

    private void backtrack(int[] board, int row,
                           List<int[]> results) {
        if (row == BOARD_SIZE) {
            results.add(board.clone());
            return;
        }
        for (int col = 0; col < BOARD_SIZE; col++) {
            if (isSafe(board, row, col)) {
                board[row] = col;
                backtrack(board, row + 1, results);
            }
        }
    }

    private boolean isSafe(int[] board, int row, int col) {
        for (int r = 0; r < row; r++) {
            int c = board[r];
            if (c == col) return false;
            if (Math.abs(c - col) == Math.abs(r - row)) return false;
        }
        return true;
    }

    public List<int[]> getSolutions() { return solutions; }
    public int getTotalSolutions()    { return solutions.size(); }
    public long getTimeTakenMs()      { return timeTakenMs; }
    public int getBoardSize()         { return BOARD_SIZE; }
}