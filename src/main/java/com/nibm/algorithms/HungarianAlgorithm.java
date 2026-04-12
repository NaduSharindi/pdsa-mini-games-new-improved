package com.nibm.algorithms;

public class HungarianAlgorithm {

    private int n;
    private int[][] cost;
    private int[] assignment;   // assignment[worker] = task
    private int totalCost;

    // ── Entry point ───────────────────────────────────────────
    public void solve(int[][] costMatrix) {
        if (costMatrix == null || costMatrix.length == 0) {
            throw new IllegalArgumentException("Cost matrix cannot be null or empty.");
        }

        this.n = costMatrix.length;
        // Deep copy so we don't mutate the original
        this.cost = new int[n][n];
        for (int i = 0; i < n; i++) {
            this.cost[i] = costMatrix[i].clone();
        }

        this.assignment = new int[n];
        runHungarian();
    }

    private void runHungarian() {
        // Step 1 — Row reduction: subtract row minimum
        for (int i = 0; i < n; i++) {
            int min = Integer.MAX_VALUE;
            for (int j = 0; j < n; j++) {
                min = Math.min(min, cost[i][j]);
            }
            for (int j = 0; j < n; j++) {
                cost[i][j] -= min;
            }
        }

        // Step 2 — Column reduction: subtract column minimum
        for (int j = 0; j < n; j++) {
            int min = Integer.MAX_VALUE;
            for (int i = 0; i < n; i++) {
                min = Math.min(min, cost[i][j]);
            }
            for (int i = 0; i < n; i++) {
                cost[i][j] -= min;
            }
        }

        int[] rowCover   = new int[n];
        int[] colCover   = new int[n];
        int[][] starred  = new int[n][n]; // starred zeros
        int[][] primed   = new int[n][n]; // primed zeros

        // Step 3 — Star zeros
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (cost[i][j] == 0 && rowCover[i] == 0 && colCover[j] == 0) {
                    starred[i][j] = 1;
                    rowCover[i]   = 1;
                    colCover[j]   = 1;
                }
            }
        }

        // Reset covers
        for (int i = 0; i < n; i++) {
            rowCover[i] = 0;
        }
        for (int j = 0; j < n; j++) {
            colCover[j] = 0;
        }

        // Iterative steps 4-6
        while (true) {
            // Step 4 — Cover columns with starred zeros
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (starred[i][j] == 1) {
                        colCover[j] = 1;
                    }
                }
            }
            int coveredCols = 0;
            for (int j = 0; j < n; j++) {
                coveredCols += colCover[j];
            }
            if (coveredCols >= n) break; // Optimal assignment found

            // Step 5 — Find uncovered zero and prime it
            int[] zeroPos;
            while (true) {
                zeroPos = findUncoveredZero(rowCover, colCover);
                if (zeroPos == null) {
                    // Step 6 — Adjust matrix
                    int minVal = findMinUncovered(rowCover, colCover);
                    for (int i = 0; i < n; i++) {
                        for (int j = 0; j < n; j++) {
                            if (rowCover[i] == 1) cost[i][j] += minVal;
                            if (colCover[j] == 0) cost[i][j] -= minVal;
                        }
                    }
                } else {
                    break;
                }
            }

            int row = zeroPos[0], col = zeroPos[1];
            primed[row][col] = 1;

            int starCol = findStarInRow(starred, row);
            if (starCol >= 0) {
                rowCover[row]   = 1;
                colCover[starCol] = 0;
            } else {
                // Augment path
                augmentPath(starred, primed, row, col);
                clearCovers(rowCover, colCover);
                clearPrimes(primed);
            }
        }

        // Extract assignment from starred zeros
        totalCost = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (starred[i][j] == 1) {
                    assignment[i] = j;
                }
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────
    private int[] findUncoveredZero(int[] rowCover, int[] colCover) {
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (cost[i][j] == 0 && rowCover[i] == 0 && colCover[j] == 0) {
                    return new int[]{i, j};
                }
            }
        }
        return null;
    }

    private int findMinUncovered(int[] rowCover, int[] colCover) {
        int min = Integer.MAX_VALUE;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (rowCover[i] == 0 && colCover[j] == 0) {
                    min = Math.min(min, cost[i][j]);
                }
            }
        }
        return min;
    }

    private int findStarInRow(int[][] starred, int row) {
        for (int j = 0; j < n; j++) {
            if (starred[row][j] == 1) return j;
        }
        return -1;
    }

    private int findStarInCol(int[][] starred, int col) {
        for (int i = 0; i < n; i++) {
            if (starred[i][col] == 1) return i;
        }
        return -1;
    }

    private int findPrimeInRow(int[][] primed, int row) {
        for (int j = 0; j < n; j++) {
            if (primed[row][j] == 1) return j;
        }
        return -1;
    }

    private void augmentPath(int[][] starred, int[][] primed, int row, int col) {
        // Build alternating path of primed/starred zeros
        boolean done = false;
        int r = row, c = col;
        while (!done) {
            int starRow = findStarInCol(starred, c);
            if (starRow >= 0) {
                starred[starRow][c] = 0;
                int primeCol = findPrimeInRow(primed, starRow);
                starred[starRow][primeCol] = 1;
                c = primeCol;
            } else {
                starred[r][c] = 1;
                done = true;
            }
        }
    }

    private void clearCovers(int[] rowCover, int[] colCover) {
        for (int i = 0; i < n; i++) rowCover[i] = 0;
        for (int j = 0; j < n; j++) colCover[j] = 0;
    }

    private void clearPrimes(int[][] primed) {
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                primed[i][j] = 0;
            }
        }
    }

    // ── Getters ───────────────────────────────────────────────
    public int[] getAssignment() { return assignment; }

    public int getTotalCost(int[][] originalMatrix) {
        int total = 0;
        for (int i = 0; i < assignment.length; i++) {
            total += originalMatrix[i][assignment[i]];
        }
        return total;
    }
}