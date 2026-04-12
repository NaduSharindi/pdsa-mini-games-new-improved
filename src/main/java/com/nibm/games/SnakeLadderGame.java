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
    private int[] board;           // 1-indexed board array
    private int[][] snakes;
    private int[][] ladders;
    private int correctAnswer;     // BFS result
    private int dijkstraAnswer;
    private long bfsTimeMs;
    private long dijkstraTimeMs;

    private final Random random = new Random();

    // ── Start a new round ─────────────────────────────────────
    public SnakeLadderRound newRound(int roundNumber, int n) {
        // Validate N range
        if (n < MIN_N || n > MAX_N) {
            throw new IllegalArgumentException(
                    "Board size N must be between 6 and 12. Got: " + n);
        }

        this.n          = n;
        this.totalCells = n * n;

        // Build board with snakes and ladders
        generateBoard();

        // Run BFS — timed
        BFSSnakeLadder bfs = new BFSSnakeLadder();
        long bStart = System.currentTimeMillis();
        bfs.solve(board, totalCells);
        bfsTimeMs     = System.currentTimeMillis() - bStart;
        correctAnswer = bfs.getMinThrows();

        // Run Dijkstra — timed
        DijkstraSnakeLadder dijkstra = new DijkstraSnakeLadder();
        long dStart = System.currentTimeMillis();
        dijkstra.solve(board, totalCells);
        dijkstraTimeMs = System.currentTimeMillis() - dStart;
        dijkstraAnswer = dijkstra.getMinThrows();

        // Build round model
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

    // ── Board generation ──────────────────────────────────────
    private void generateBoard() {
        board = new int[totalCells + 1];

        // Initialise all cells as normal (point to themselves)
        for (int i = 1; i <= totalCells; i++) {
            board[i] = i;
        }

        int count = n - 2; // number of snakes = number of ladders = N-2

        // Get shuffled list of interior cells
        // (exclude cell 1 = start, totalCells = end)
        List<Integer> available = new ArrayList<>();
        for (int i = 2; i < totalCells; i++) {
            available.add(i);
        }
        Collections.shuffle(available, random);

        // Place ladders: base < top
        ladders = new int[count][2];
        for (int i = 0; i < count; i++) {
            int base = available.remove(0);
            int top  = available.remove(0);
            // Ensure base < top (ladder goes up)
            if (base > top) { int tmp = base; base = top; top = tmp; }
            ladders[i][0] = base;
            ladders[i][1] = top;
            board[base]   = top;
        }

        // Place snakes: head > tail
        snakes = new int[count][2];
        for (int i = 0; i < count; i++) {
            int head = available.remove(0);
            int tail = available.remove(0);
            // Ensure head > tail (snake goes down)
            if (head < tail) { int tmp = head; head = tail; tail = tmp; }
            snakes[i][0] = head;
            snakes[i][1] = tail;
            board[head]  = tail;
        }
    }

    // ── Generate 3 choices for UI ─────────────────────────────
    // One correct + two plausible wrong answers
    public int[] generateChoices() {
        int correct = correctAnswer;
        int wrong1, wrong2;

        do { wrong1 = correct + random.nextInt(5) + 1; }
        while (wrong1 == correct);

        do { wrong2 = Math.max(1, correct - random.nextInt(5) - 1); }
        while (wrong2 == correct || wrong2 == wrong1);

        // Shuffle into random positions
        int[] choices = {correct, wrong1, wrong2};
        for (int i = 2; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int tmp = choices[i]; choices[i] = choices[j]; choices[j] = tmp;
        }
        return choices;
    }

    // ── Validate player answer ────────────────────────────────
    public boolean validateAnswer(int playerAnswer) {
        return playerAnswer == correctAnswer;
    }

    // ── Board cell list for UI drawing ────────────────────────
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
    public int getN()              { return n; }
    public int getTotalCells()     { return totalCells; }
    public int getCorrectAnswer()  { return correctAnswer; }
    public int getDijkstraAnswer() { return dijkstraAnswer; }
    public long getBfsTimeMs()     { return bfsTimeMs; }
    public long getDijkstraTimeMs(){ return dijkstraTimeMs; }
    public int[] getBoard()        { return board; }
    public int[][] getSnakes()     { return snakes; }
    public int[][] getLadders()    { return ladders; }
}