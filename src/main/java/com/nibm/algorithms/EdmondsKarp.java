package com.nibm.algorithms;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

public class EdmondsKarp {

    private int maxFlow;

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
        for (int i = 0; i < n; i++) {
            residual[i] = capacity[i].clone();
        }

        maxFlow = 0;

        while (true) {
            int[] parent = new int[n];
            Arrays.fill(parent, -1);
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

            if (parent[sink] == -1) break;

            int pathFlow = Integer.MAX_VALUE;
            for (int v = sink; v != source; v = parent[v]) {
                int u = parent[v];
                pathFlow = Math.min(pathFlow, residual[u][v]);
            }

            for (int v = sink; v != source; v = parent[v]) {
                int u = parent[v];
                residual[u][v] -= pathFlow;
                residual[v][u] += pathFlow;
            }

            maxFlow += pathFlow;
        }
    }

    public int getMaxFlow() {
        return maxFlow;
    }
}