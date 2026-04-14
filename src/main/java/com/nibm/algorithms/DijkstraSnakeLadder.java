package com.nibm.algorithms;

import java.util.Arrays;
import java.util.PriorityQueue;

public class DijkstraSnakeLadder {

    private int minThrows;

    public void solve(int[] board, int totalCells) {
        if (board == null || totalCells <= 0) {
            throw new IllegalArgumentException(
                    "Board cannot be null and totalCells must be positive.");
        }

        int[] dist = new int[totalCells + 1];
        Arrays.fill(dist, Integer.MAX_VALUE);
        dist[1] = 0;

        PriorityQueue<int[]> pq = new PriorityQueue<>(
                (a, b) -> Integer.compare(a[0], b[0])
        );
        pq.add(new int[]{0, 1});
        minThrows = -1;

        while (!pq.isEmpty()) {
            int[] curr = pq.poll();
            int throwsUsed = curr[0];
            int cell = curr[1];

            if (throwsUsed > dist[cell]) continue;

            if (cell == totalCells) {
                minThrows = throwsUsed;
                return;
            }

            for (int dice = 1; dice <= 6; dice++) {
                int next = cell + dice;
                if (next > totalCells) continue;

                next = board[next];
                int newDist = throwsUsed + 1;

                if (newDist < dist[next]) {
                    dist[next] = newDist;
                    pq.add(new int[]{newDist, next});
                }
            }
        }
    }

    public int getMinThrows() {
        return minThrows;
    }
}