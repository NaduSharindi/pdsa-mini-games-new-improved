package com.nibm.algorithms;

public class FordFulkerson {

    private int maxFlow;
    private int[][] residual;
    private int n;

    /**
     * Solves max flow using DFS-based augmenting paths.
     * @param capacity  n×n adjacency matrix (capacity[i][j] = capacity of edge i->j)
     * @param source    source node index
     * @param sink      sink node index
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

        this.n = capacity.length;

        // Build residual graph as deep copy
        residual = new int[n][n];
        for (int i = 0; i < n; i++) {
            residual[i] = capacity[i].clone();
        }

        maxFlow = 0;

        // Augment while DFS finds a path from source to sink
        while (true) {
            boolean[] visited = new boolean[n];
            int pathFlow = dfs(source, sink, Integer.MAX_VALUE, visited);
            if (pathFlow == 0) break;   // no augmenting path found
            maxFlow += pathFlow;
        }
    }

    // DFS to find augmenting path — returns bottleneck flow
    private int dfs(int node, int sink, int flow, boolean[] visited) {
        if (node == sink) return flow;
        visited[node] = true;

        for (int next = 0; next < n; next++) {
            if (!visited[next] && residual[node][next] > 0) {
                int pushed = dfs(next, sink,
                        Math.min(flow, residual[node][next]), visited);
                if (pushed > 0) {
                    // Update residual capacities
                    residual[node][next] -= pushed;
                    residual[next][node] += pushed;
                    return pushed;
                }
            }
        }
        return 0;
    }

    public int getMaxFlow() { return maxFlow; }
}