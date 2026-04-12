package com.nibm.algorithms;

import java.util.Arrays;

public class GreedyAssignment {

    private int[] assignment;

    public void solve(int[][] costMatrix) {
        if (costMatrix == null || costMatrix.length == 0) {
            throw new IllegalArgumentException("Cost matrix cannot be null or empty.");
        }

        int n = costMatrix.length;
        assignment = new int[n];
        boolean[] taskTaken = new boolean[n];

        // Build list of all (cost, worker, task) and sort by cost
        int[][] cells = new int[n * n][3];
        int idx = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                cells[idx][0] = costMatrix[i][j];
                cells[idx][1] = i;
                cells[idx][2] = j;
                idx++;
            }
        }
        // Sort ascending by cost
        Arrays.sort(cells, (a, b) -> a[0] - b[0]);

        boolean[] workerAssigned = new boolean[n];
        Arrays.fill(assignment, -1);

        for (int[] cell : cells) {
            int worker = cell[1];
            int task   = cell[2];
            if (!workerAssigned[worker] && !taskTaken[task]) {
                assignment[worker] = task;
                workerAssigned[worker] = true;
                taskTaken[task]   = true;
            }
        }
    }

    public int[] getAssignment() { return assignment; }

    public int getTotalCost(int[][] costMatrix) {
        int total = 0;
        for (int i = 0; i < assignment.length; i++) {
            total += costMatrix[i][assignment[i]];
        }
        return total;
    }
}