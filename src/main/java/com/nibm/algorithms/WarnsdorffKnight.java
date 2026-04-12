package com.nibm.algorithms;

public class WarnsdorffKnight {

    // All 8 possible knight moves
    private static final int[] DX = {-2,-1, 1, 2, 2, 1,-1,-2};
    private static final int[] DY = { 1, 2, 2, 1,-1,-2,-2,-1};

    private int[][] tour;   // tour[i][j] = move number (1-based), 0 = unvisited
    private boolean solved;
    private int boardSize;

    /**
     * Warnsdorff's heuristic — always move to the square
     * that has the fewest onward moves (min-degree heuristic).
     * Very fast: O(n²) for an n×n board.
     */
    public void solve(int boardSize, int startRow, int startCol) {
        validateInput(boardSize, startRow, startCol);

        this.boardSize = boardSize;
        this.tour      = new int[boardSize][boardSize];
        this.solved    = false;

        int row = startRow;
        int col = startCol;
        tour[row][col] = 1;   // first move

        for (int move = 2; move <= boardSize * boardSize; move++) {
            int[] next = getWarnsdorffNext(row, col);
            if (next == null) {
                // Stuck — no valid next move (rare with Warnsdorff)
                solved = false;
                return;
            }
            row = next[0];
            col = next[1];
            tour[row][col] = move;
        }
        solved = true;
    }

    // Pick the neighbour with the fewest onward moves
    private int[] getWarnsdorffNext(int row, int col) {
        int minDegree = Integer.MAX_VALUE;
        int[] bestMove = null;

        for (int i = 0; i < 8; i++) {
            int nr = row + DX[i];
            int nc = col + DY[i];
            if (isValid(nr, nc) && tour[nr][nc] == 0) {
                int degree = countOnwardMoves(nr, nc);
                if (degree < minDegree) {
                    minDegree = degree;
                    bestMove  = new int[]{nr, nc};
                }
            }
        }
        return bestMove;
    }

    // Count how many unvisited squares can be reached from (r,c)
    private int countOnwardMoves(int r, int c) {
        int count = 0;
        for (int i = 0; i < 8; i++) {
            int nr = r + DX[i];
            int nc = c + DY[i];
            if (isValid(nr, nc) && tour[nr][nc] == 0) count++;
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