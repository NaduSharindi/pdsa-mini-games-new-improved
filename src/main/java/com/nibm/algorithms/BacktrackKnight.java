package com.nibm.algorithms;

public class BacktrackKnight {

    private static final int[] DX = {-2,-1, 1, 2, 2, 1,-1,-2};
    private static final int[] DY = { 1, 2, 2, 1,-1,-2,-2,-1};

    private int[][] tour;
    private boolean solved;
    private int boardSize;

    /**
     * Recursive backtracking — exhaustive search.
     * Guaranteed to find a solution if one exists.
     * Uses Warnsdorff ordering internally to prune the search
     * tree and make the 8×8 case practical.
     * Note: 16×16 backtracking may be slow — Warnsdorff
     * is preferred for 16×16 in practice.
     */
    public void solve(int boardSize, int startRow, int startCol) {
        validateInput(boardSize, startRow, startCol);

        this.boardSize = boardSize;
        this.tour      = new int[boardSize][boardSize];
        this.solved    = false;

        // Mark all as unvisited
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                tour[i][j] = -1;
            }
        }

        tour[startRow][startCol] = 1;
        solved = backtrack(startRow, startCol, 2);
    }

    private boolean backtrack(int row, int col, int moveNum) {
        if (moveNum > boardSize * boardSize) return true; // all visited

        // Get next moves sorted by Warnsdorff degree (pruning)
        int[][] moves = getSortedMoves(row, col);

        for (int[] move : moves) {
            int nr = move[0];
            int nc = move[1];
            tour[nr][nc] = moveNum;
            if (backtrack(nr, nc, moveNum + 1)) return true;
            tour[nr][nc] = -1;  // undo
        }
        return false;
    }

    // Returns valid unvisited next moves sorted by degree (Warnsdorff pruning)
    private int[][] getSortedMoves(int row, int col) {
        int[][] candidates = new int[8][3]; // {row, col, degree}
        int count = 0;

        for (int i = 0; i < 8; i++) {
            int nr = row + DX[i];
            int nc = col + DY[i];
            if (isValid(nr, nc) && tour[nr][nc] == -1) {
                candidates[count][0] = nr;
                candidates[count][1] = nc;
                candidates[count][2] = countOnwardMoves(nr, nc);
                count++;
            }
        }

        // Sort by degree ascending (Warnsdorff heuristic for pruning)
        for (int i = 0; i < count - 1; i++) {
            for (int j = i + 1; j < count; j++) {
                if (candidates[j][2] < candidates[i][2]) {
                    int[] tmp    = candidates[i];
                    candidates[i] = candidates[j];
                    candidates[j] = tmp;
                }
            }
        }

        int[][] result = new int[count][2];
        for (int i = 0; i < count; i++) {
            result[i][0] = candidates[i][0];
            result[i][1] = candidates[i][1];
        }
        return result;
    }

    private int countOnwardMoves(int r, int c) {
        int count = 0;
        for (int i = 0; i < 8; i++) {
            int nr = r + DX[i];
            int nc = c + DY[i];
            if (isValid(nr, nc) && tour[nr][nc] == -1) count++;
        }
        return count;
    }

    private boolean isValid(int r, int c) {
        return r >= 0 && r < boardSize && c >= 0 && c < boardSize;
    }

    private void validateInput(int boardSize, int startRow, int startCol) {
        if (boardSize != 8 && boardSize != 16) {
            throw new IllegalArgumentException(
                    "Board size must be 8 or 16. Got: " + boardSize);
        }
        if (startRow < 0 || startRow >= boardSize ||
                startCol < 0 || startCol >= boardSize) {
            throw new IllegalArgumentException(
                    "Start position out of bounds: ("
                            + startRow + "," + startCol + ")");
        }
    }

    public int[][] getTour()    { return tour; }
    public boolean isSolved()   { return solved; }
}