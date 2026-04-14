package com.nibm.algorithms;

import java.util.LinkedList;
import java.util.Queue;

public class BFSSnakeLadder {

    private int minThrows;

    public void solve(int[] board, int totalCells) {
        if (board == null || totalCells <= 0) {
            throw new IllegalArgumentException(
                    "Board cannot be null and totalCells must be positive.");
        }

        boolean[] visited = new boolean[totalCells + 1];
        Queue<int[]> queue = new LinkedList<>(); // {cell, throws}

        queue.add(new int[]{1, 0});
        visited[1] = true;
        minThrows = -1;

        while (!queue.isEmpty()) {
            int[] curr = queue.poll();
            int cell = curr[0];
            int throwsUsed = curr[1];

            if (cell == totalCells) {
                minThrows = throwsUsed;
                return;
            }

            for (int dice = 1; dice <= 6; dice++) {
                int next = cell + dice;
                if (next > totalCells) continue;

                next = board[next];

                if (!visited[next]) {
                    visited[next] = true;
                    queue.add(new int[]{next, throwsUsed + 1});
                }
            }
        }
    }

    public int getMinThrows() {
        return minThrows;
    }
}