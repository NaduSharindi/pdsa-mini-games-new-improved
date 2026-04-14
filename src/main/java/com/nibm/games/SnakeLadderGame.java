package com.nibm.games;

import com.nibm.algorithms.BFSSnakeLadder;
import com.nibm.algorithms.DijkstraSnakeLadder;
import com.nibm.models.BoardCell;
import com.nibm.models.BoardCell.CellType;
import com.nibm.models.SnakeLadderRound;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SnakeLadderGame {

    private static final int MIN_N = 6;
    private static final int MAX_N = 12;

    private int n;
    private int totalCells;
    private int[] board;
    private int[][] snakes;
    private int[][] ladders;

    private int correctAnswer;
    private int dijkstraAnswer;
    private long bfsTimeNs;
    private long dijkstraTimeNs;

    private int playerPosition;
    private int playerThrows;
    private int lastRoll;
    private boolean gameOver;
    private String lastMoveMessage;

    private final Random random = new Random();

    public SnakeLadderRound newRound(int roundNumber, int n) {
        if (n < MIN_N || n > MAX_N) {
            throw new IllegalArgumentException(
                    "Board size N must be between 6 and 12.");
        }

        this.n = n;
        this.totalCells = n * n;
        this.playerPosition = 1;
        this.playerThrows = 0;
        this.lastRoll = 0;
        this.gameOver = false;
        this.lastMoveMessage = "Game started. You are at cell 1.";

        generateBoard();

        BFSSnakeLadder bfs = new BFSSnakeLadder();
        long bStart = System.nanoTime();
        bfs.solve(board, totalCells);
        bfsTimeNs = System.nanoTime() - bStart;
        correctAnswer = bfs.getMinThrows();

        DijkstraSnakeLadder dijkstra = new DijkstraSnakeLadder();
        long dStart = System.nanoTime();
        dijkstra.solve(board, totalCells);
        dijkstraTimeNs = System.nanoTime() - dStart;
        dijkstraAnswer = dijkstra.getMinThrows();

        SnakeLadderRound round = new SnakeLadderRound();
        round.setRoundNumber(roundNumber);
        round.setN(n);
        round.setTotalCells(totalCells);
        round.setSnakes(snakes);
        round.setLadders(ladders);
        round.setBfsAnswer(correctAnswer);
        round.setDijkstraAnswer(dijkstraAnswer);
        round.setBfsTimeNs(bfsTimeNs);
        round.setDijkstraTimeNs(dijkstraTimeNs);
        return round;
    }

    private void generateBoard() {
        board = new int[totalCells + 1];
        for (int i = 1; i <= totalCells; i++) {
            board[i] = i;
        }

        int count = n - 2;

        List<Integer> available = new ArrayList<>();
        for (int i = 2; i < totalCells; i++) {
            available.add(i);
        }
        Collections.shuffle(available, random);

        ladders = new int[count][2];
        for (int i = 0; i < count; i++) {
            int a = available.remove(0);
            int b = available.remove(0);
            int base = Math.min(a, b);
            int top = Math.max(a, b);

            if (base == top) top = Math.min(totalCells - 1, top + 1);

            ladders[i][0] = base;
            ladders[i][1] = top;
            board[base] = top;
        }

        snakes = new int[count][2];
        for (int i = 0; i < count; i++) {
            int a = available.remove(0);
            int b = available.remove(0);
            int head = Math.max(a, b);
            int tail = Math.min(a, b);

            if (head == tail) tail = Math.max(2, tail - 1);

            snakes[i][0] = head;
            snakes[i][1] = tail;
            board[head] = tail;
        }
    }

    public int rollDice() {
        if (gameOver) return playerPosition;

        int roll = 1 + random.nextInt(6);
        lastRoll = roll;
        playerThrows++;

        int attempted = playerPosition + roll;

        if (attempted > totalCells) {
            lastMoveMessage = "You rolled " + roll + ". Need exact move, so you stay at cell " + playerPosition + ".";
            return playerPosition;
        }

        playerPosition = attempted;

        if (board[playerPosition] > playerPosition) {
            int from = playerPosition;
            playerPosition = board[playerPosition];
            lastMoveMessage = "You rolled " + roll + ", landed on ladder base " + from
                    + " and climbed to " + playerPosition + ".";
        } else if (board[playerPosition] < playerPosition) {
            int from = playerPosition;
            playerPosition = board[playerPosition];
            lastMoveMessage = "You rolled " + roll + ", landed on snake head " + from
                    + " and slid down to " + playerPosition + ".";
        } else {
            lastMoveMessage = "You rolled " + roll + " and moved to cell " + playerPosition + ".";
        }

        if (playerPosition == totalCells) {
            gameOver = true;
            lastMoveMessage += " You reached the last cell!";
        }

        return playerPosition;
    }

    public List<BoardCell> getBoardCells() {
        List<BoardCell> cells = new ArrayList<>();
        for (int i = 1; i <= totalCells; i++) {
            if (board[i] > i) {
                cells.add(new BoardCell(i, board[i], CellType.LADDER));
            } else if (board[i] < i) {
                cells.add(new BoardCell(i, board[i], CellType.SNAKE));
            } else {
                cells.add(new BoardCell(i, board[i], CellType.NORMAL));
            }
        }
        return cells;
    }

    public int getN() { return n; }
    public int getTotalCells() { return totalCells; }
    public int getCorrectAnswer() { return correctAnswer; }
    public int getDijkstraAnswer() { return dijkstraAnswer; }
    public long getBfsTimeNs() { return bfsTimeNs; }
    public long getDijkstraTimeNs() { return dijkstraTimeNs; }
    public int[] getBoard() { return board; }
    public int[][] getSnakes() { return snakes; }
    public int[][] getLadders() { return ladders; }

    public int getPlayerPosition() { return playerPosition; }
    public int getPlayerThrows() { return playerThrows; }
    public int getLastRoll() { return lastRoll; }
    public boolean isGameOver() { return gameOver; }
    public String getLastMoveMessage() { return lastMoveMessage; }
}