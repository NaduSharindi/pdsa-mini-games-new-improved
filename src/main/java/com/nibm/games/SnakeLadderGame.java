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
    private long bfsTimeMs;
    private long dijkstraTimeMs;

    private final Random random = new Random();

    // ── Start a new round ─────────────────────────────────────
    public SnakeLadderRound newRound(int roundNumber, int n) {
        // Validate N range
        if (n < MIN_N || n > MAX_N) {
            throw new IllegalArgumentException(
                    "Board size N must be between "
                            + MIN_N + " and " + MAX_N + ". Got: " + n);
        }

        // Validate enough interior cells exist
        // Interior cells = totalCells - 2 (exclude cell 1 and N²)
        // Need 2*(N-2) cells for snakes + 2*(N-2) cells for ladders
        // = 4*(N-2) total cells needed
        int totalCellsNeeded = 4 * (n - 2);
        int interiorCells    = (n * n) - 2;
        if (interiorCells < totalCellsNeeded) {
            throw new IllegalArgumentException(
                    "Not enough cells for " + (n - 2)
                            + " snakes and " + (n - 2) + " ladders on a "
                            + n + "×" + n + " board.");
        }

        this.n          = n;
        this.totalCells = n * n;

        generateBoard();

        // ── Run BFS — timed ───────────────────────────────────
        BFSSnakeLadder bfs = new BFSSnakeLadder();
        long bStart = System.currentTimeMillis();
        bfs.solve(board, totalCells);
        bfsTimeMs     = System.currentTimeMillis() - bStart;
        correctAnswer = bfs.getMinThrows();

        // ── Run Dijkstra — timed ──────────────────────────────
        DijkstraSnakeLadder dijkstra = new DijkstraSnakeLadder();
        long dStart = System.currentTimeMillis();
        dijkstra.solve(board, totalCells);
        dijkstraTimeMs = System.currentTimeMillis() - dStart;
        dijkstraAnswer = dijkstra.getMinThrows();

        // ── Build round model ─────────────────────────────────
        SnakeLadderRound round = new SnakeLadderRound();
        round.setRoundNumber(roundNumber);
        round.setN(n);
        round.setTotalCells(totalCells);
        round.setSnakes(snakes);
        round.setLadders(ladders);
        round.setBfsAnswer(correctAnswer);
        round.setDijkstraAnswer(dijkstraAnswer);
        round.setBfsTimeMs(bfsTimeMs);
        round.setDijkstraTimeMs(dijkstraTimeMs);
        return round;
    }

    // ── Board generation with safe placement ──────────────────
    private void generateBoard() {
        board = new int[totalCells + 1];

        // All cells point to themselves (normal)
        for (int i = 1; i <= totalCells; i++) board[i] = i;

        int count = n - 2; // snakes = ladders = N-2

        // Interior cells: exclude cell 1 (start) and N² (end)
        List<Integer> available = new ArrayList<>();
        for (int i = 2; i < totalCells; i++) available.add(i);
        Collections.shuffle(available, random);

        // ── Place ladders: base < top (goes UP) ──────────────
        ladders = new int[count][2];
        for (int i = 0; i < count; i++) {
            int base = available.remove(0);
            int top  = available.remove(0);

            // Ensure base < top (ladder goes up)
            if (base > top) {
                int tmp = base; base = top; top = tmp;
            }

            // Safety: base must not equal top
            if (base == top) {
                top = (top < totalCells - 1) ? top + 1 : top - 1;
            }

            ladders[i][0] = base;
            ladders[i][1] = top;
            board[base]   = top;
        }

        // ── Place snakes: head > tail (goes DOWN) ─────────────
        snakes = new int[count][2];
        for (int i = 0; i < count; i++) {
            int head = available.remove(0);
            int tail = available.remove(0);

            // Ensure head > tail (snake goes down)
            if (head < tail) {
                int tmp = head; head = tail; tail = tmp;
            }

            // Safety: head must not equal tail
            if (head == tail) {
                tail = (tail > 2) ? tail - 1 : tail + 1;
            }

            snakes[i][0] = head;
            snakes[i][1] = tail;
            board[head]  = tail;
        }
    }

    // ── Generate 3 choices — always positive, always distinct ─
    public int[] generateChoices() {
        int correct = correctAnswer;
        int wrong1, wrong2;

        // Wrong1: correct + random offset (1–5)
        wrong1 = correct + 1 + random.nextInt(5);

        // Wrong2: correct - random offset (1–5), min 1
        do {
            wrong2 = correct - 1 - random.nextInt(5);
            if (wrong2 < 1) wrong2 = correct + 6 + random.nextInt(5);
        } while (wrong2 == wrong1 || wrong2 == correct || wrong2 < 1);

        // Ensure wrong1 != wrong2 (extra safety)
        if (wrong1 == wrong2) wrong1 = wrong2 + 1;

        // Shuffle into random order
        int[] choices = {correct, wrong1, wrong2};
        for (int i = 2; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int tmp = choices[i];
            choices[i] = choices[j];
            choices[j] = tmp;
        }
        return choices;
    }

    // ── Result classification ─────────────────────────────────
    // Returns "WIN", "DRAW", or "LOSE"
    public String classifyAnswer(int playerAnswer) {
        if (playerAnswer == correctAnswer) return "WIN";
        if (Math.abs(playerAnswer - correctAnswer) == 1) return "DRAW";
        return "LOSE";
    }

    // ── Validate answer ───────────────────────────────────────
    public boolean validateAnswer(int playerAnswer) {
        return playerAnswer == correctAnswer;
    }

    // ── Board cell list for drawing ───────────────────────────
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

    // ── Getters ───────────────────────────────────────────────
    public int getN()               { return n; }
    public int getTotalCells()      { return totalCells; }
    public int getCorrectAnswer()   { return correctAnswer; }
    public int getDijkstraAnswer()  { return dijkstraAnswer; }
    public long getBfsTimeMs()      { return bfsTimeMs; }
    public long getDijkstraTimeMs() { return dijkstraTimeMs; }
    public int[] getBoard()         { return board; }
    public int[][] getSnakes()      { return snakes; }
    public int[][] getLadders()     { return ladders; }
}