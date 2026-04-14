package com.nibm.algorithms;

import java.util.Arrays;

public class GreedyAssignment {

    private int[] assignment;

    public void solve(int[][] costMatrix) {
        if (costMatrix == null || costMatrix.length == 0 || costMatrix[0].length == 0) {
            throw new IllegalArgumentException("Cost matrix cannot be null or empty.");
        }

        int n = costMatrix.length;
        if (costMatrix[0].length != n) {
            throw new IllegalArgumentException("Cost matrix must be square.");
        }

        assignment = new int[n];
        Arrays.fill(assignment, -1);

        boolean[] taskTaken = new boolean[n];
        boolean[] workerAssigned = new boolean[n];

        int[][] cells = new int[n * n][3];
        int idx = 0;

        for (int i = 0; i < n; i++) {
            if (costMatrix[i].length != n) {
                throw new IllegalArgumentException("Cost matrix must be square.");
            }
            for (int j = 0; j < n; j++) {
                cells[idx][0] = costMatrix[i][j];
                cells[idx][1] = i;
                cells[idx][2] = j;
                idx++;
            }
        }

        Arrays.sort(cells, (a, b) -> Integer.compare(a[0], b[0]));

        for (int[] cell : cells) {
            int worker = cell[1];
            int task = cell[2];

            if (!workerAssigned[worker] && !taskTaken[task]) {
                assignment[worker] = task;
                workerAssigned[worker] = true;
                taskTaken[task] = true;
            }
        }
    }

    public int[] getAssignment() {
        return assignment;
    }

    public int getTotalCost(int[][] costMatrix) {
        if (assignment == null) {
            throw new IllegalStateException("solve() must be called before getTotalCost().");
        }

        int total = 0;
        for (int i = 0; i < assignment.length; i++) {
            if (assignment[i] < 0) {
                throw new IllegalStateException("Incomplete assignment.");
            }
            total += costMatrix[i][assignment[i]];
        }
        return total;
    }
}