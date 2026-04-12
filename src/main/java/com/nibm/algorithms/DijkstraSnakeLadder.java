package com.nibm.algorithms;

import java.util.Arrays;
import java.util.PriorityQueue;

public class DijkstraSnakeLadder {

    private int minThrows;

    /**
     * Uses Dijkstra with uniform edge weight (1 throw per move).
     * On a uniform graph this equals BFS, but demonstrates
     * a different algorithmic approach for comparison.
     */
    public void solve(int[] board, int totalCells) {
        if (board == null || totalCells <= 0) {
            throw new IllegalArgumentException(
                    "Board cannot be null and totalCells must be positive.");
        }

        int[] dist = new int[totalCells + 1];
        Arrays.fill(dist, Integer.MAX_VALUE);
        dist[1] = 0;

        // PriorityQueue: {distance, cell}
        PriorityQueue<int[]> pq = new PriorityQueue<>(
                (a, b) -> a[0] - b[0]
        );
        pq.add(new int[]{0, 1});
        minThrows = -1;

        while (!pq.isEmpty()) {
            int[] curr = pq.poll();
            int throws_ = curr[0];
            int cell    = curr[1];

            if (throws_ > dist[cell]) continue; // stale entry

            if (cell == totalCells) {
                minThrows = throws_;
                return;
            }

            for (int dice = 1; dice <= 6; dice++) {
                int next = cell + dice;
                if (next > totalCells) continue;

                // Apply snake or ladder
                next = board[next];

                int newDist = throws_ + 1;
                if (newDist < dist[next]) {
                    dist[next] = newDist;
                    pq.add(new int[]{newDist, next});
                }
            }
        }
    }

    public int getMinThrows() { return minThrows; }
}