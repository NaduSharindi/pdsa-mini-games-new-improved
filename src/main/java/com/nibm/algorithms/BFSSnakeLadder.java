package com.nibm.algorithms;

import java.util.LinkedList;
import java.util.Queue;

public class BFSSnakeLadder {

    private int minThrows;

    /**
     * board[] maps cell -> destination after snake/ladder.
     * board[i] == i means normal cell.
     * board[i] >  i means ladder.
     * board[i] <  i means snake.
     * Cell index 1-based: board[1] to board[totalCells].
     */
    public void solve(int[] board, int totalCells) {
        if (board == null || totalCells <= 0) {
            throw new IllegalArgumentException(
                    "Board cannot be null and totalCells must be positive.");
        }

        boolean[] visited = new boolean[totalCells + 1];
        Queue<int[]> queue = new LinkedList<>(); // {cell, throws}

        queue.add(new int[]{1, 0}); // start at cell 1, 0 throws
        visited[1] = true;
        minThrows = -1;

        while (!queue.isEmpty()) {
            int[] curr = queue.poll();
            int cell   = curr[0];
            int throws_ = curr[1];

            // Try all 6 dice values
            for (int dice = 1; dice <= 6; dice++) {
                int next = cell + dice;

                if (next > totalCells) continue; // can't go past last cell

                // Apply snake or ladder
                next = board[next];

                if (next == totalCells) {
                    minThrows = throws_ + 1;
                    return; // found shortest path
                }

                if (!visited[next]) {
                    visited[next] = true;
                    queue.add(new int[]{next, throws_ + 1});
                }
            }
        }
    }

    public int getMinThrows() { return minThrows; }
}