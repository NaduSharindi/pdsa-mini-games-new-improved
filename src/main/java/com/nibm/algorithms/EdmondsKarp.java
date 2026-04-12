package com.nibm.algorithms;

import java.util.LinkedList;
import java.util.Queue;

public class EdmondsKarp {

    private int maxFlow;

    /**
     * Solves max flow using BFS-based augmenting paths (Edmonds-Karp).
     * Guaranteed O(V * E²) — always finds shortest augmenting path.
     */
    public void solve(int[][] capacity, int source, int sink) {
        if (capacity == null || capacity.length == 0) {
            throw new IllegalArgumentException(
                    "Capacity matrix cannot be null or empty.");
        }
        if (source < 0 || sink < 0 ||
                source >= capacity.length || sink >= capacity.length) {
            throw new IllegalArgumentException(
                    "Source or sink index out of bounds.");
        }

        int n = capacity.length;
        int[][] residual = new int[n][n];

        // Build residual graph
        for (int i = 0; i < n; i++) {
            residual[i] = capacity[i].clone();
        }

        maxFlow = 0;

        while (true) {
            // BFS to find shortest augmenting path
            int[] parent = new int[n];
            java.util.Arrays.fill(parent, -1);
            parent[source] = source;

            Queue<Integer> queue = new LinkedList<>();
            queue.add(source);

            while (!queue.isEmpty() && parent[sink] == -1) {
                int curr = queue.poll();
                for (int next = 0; next < n; next++) {
                    if (parent[next] == -1 && residual[curr][next] > 0) {
                        parent[next] = curr;
                        queue.add(next);
                    }
                }
            }

            // No augmenting path found
            if (parent[sink] == -1) break;

            // Find bottleneck along the path
            int pathFlow = Integer.MAX_VALUE;
            for (int v = sink; v != source; v = parent[v]) {
                int u = parent[v];
                pathFlow = Math.min(pathFlow, residual[u][v]);
            }

            // Update residual capacities along the path
            for (int v = sink; v != source; v = parent[v]) {
                int u = parent[v];
                residual[u][v] -= pathFlow;
                residual[v][u] += pathFlow;
            }

            maxFlow += pathFlow;
        }
    }

    public int getMaxFlow() { return maxFlow; }
}