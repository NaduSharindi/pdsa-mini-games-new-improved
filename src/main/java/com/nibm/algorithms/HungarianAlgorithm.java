package com.nibm.algorithms;

public class HungarianAlgorithm {

    private int n;
    private int[] assignment; // assignment[worker] = task

    public void solve(int[][] costMatrix) {
        if (costMatrix == null || costMatrix.length == 0 || costMatrix[0].length == 0) {
            throw new IllegalArgumentException("Cost matrix cannot be null or empty.");
        }

        n = costMatrix.length;
        for (int i = 0; i < n; i++) {
            if (costMatrix[i].length != n) {
                throw new IllegalArgumentException("Cost matrix must be square.");
            }
        }

        assignment = new int[n];

        int[] u = new int[n + 1];
        int[] v = new int[n + 1];
        int[] p = new int[n + 1];
        int[] way = new int[n + 1];

        for (int i = 1; i <= n; i++) {
            p[0] = i;
            int j0 = 0;

            int[] minDist = new int[n + 1];
            boolean[] used = new boolean[n + 1];

            for (int j = 0; j <= n; j++) {
                minDist[j] = Integer.MAX_VALUE;
            }
            minDist[0] = 0;

            do {
                used[j0] = true;
                int i0 = p[j0];
                int delta = Integer.MAX_VALUE;
                int j1 = -1;

                for (int j = 1; j <= n; j++) {
                    if (!used[j]) {
                        int reducedCost = costMatrix[i0 - 1][j - 1] - u[i0] - v[j];
                        if (reducedCost < minDist[j]) {
                            minDist[j] = reducedCost;
                            way[j] = j0;
                        }
                        if (minDist[j] < delta) {
                            delta = minDist[j];
                            j1 = j;
                        }
                    }
                }

                for (int j = 0; j <= n; j++) {
                    if (used[j]) {
                        u[p[j]] += delta;
                        v[j] -= delta;
                    } else {
                        minDist[j] -= delta;
                    }
                }

                j0 = j1;

            } while (p[j0] != 0);

            do {
                int j1 = way[j0];
                p[j0] = p[j1];
                j0 = j1;
            } while (j0 != 0);
        }

        assignment = new int[n];
        for (int j = 1; j <= n; j++) {
            if (p[j] != 0) {
                assignment[p[j] - 1] = j - 1;
            }
        }
    }

    public int[] getAssignment() {
        return assignment;
    }

    public int getTotalCost(int[][] originalMatrix) {
        if (assignment == null) {
            throw new IllegalStateException("solve() must be called before getTotalCost().");
        }

        int total = 0;
        for (int i = 0; i < assignment.length; i++) {
            total += originalMatrix[i][assignment[i]];
        }
        return total;
    }
}