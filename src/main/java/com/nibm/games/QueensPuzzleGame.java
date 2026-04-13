package com.nibm.games;

import com.nibm.algorithms.NQueensSequential;
import com.nibm.algorithms.NQueensThreaded;
import com.nibm.db.QueensSolutionRepository;
import com.nibm.models.QueenSolution;
import com.nibm.models.QueensRound;

import java.util.List;

public class QueensPuzzleGame {

    public static final int BOARD_SIZE = 16;

    private final QueensSolutionRepository repo;

    private int totalSolutions;
    private long seqTimeMs;
    private long threadTimeMs;
    private boolean initialized = false;

    public QueensPuzzleGame(QueensSolutionRepository repo) {
        this.repo = repo;
    }

    // ── Initialize: run both solvers, save to DB ──────────────
    // Called ONCE on app startup (or first time game is opened).
    // Checks if solutions already exist in DB before re-computing.
    public QueensRound initialize() {
        // Check if solutions already computed and stored
        long existingCount = repo.countSolutions();

        if (existingCount > 0) {
            // Solutions already in DB — just load stats
            totalSolutions = (int) existingCount;
            seqTimeMs      = repo.getSeqTime();
            threadTimeMs   = repo.getThreadTime();
            initialized    = true;

            QueensRound round = new QueensRound();
            round.setTotalSolutions(totalSolutions);
            round.setSeqTimeMs(seqTimeMs);
            round.setThreadTimeMs(threadTimeMs);
            round.setClaimedCount(repo.countClaimedSolutions());
            round.setAllClaimed(repo.areAllClaimed());
            return round;
        }

        // ── Run Sequential solver ─────────────────────────────
        NQueensSequential seq = new NQueensSequential();
        seq.solve();
        seqTimeMs      = seq.getTimeTakenMs();
        totalSolutions = seq.getTotalSolutions();

        // Save all solutions to DB (unclaimed)
        List<int[]> solutions = seq.getSolutions();
        for (int i = 0; i < solutions.size(); i++) {
            repo.saveSolution(new QueenSolution(i + 1, solutions.get(i)));
        }

        // ── Run Threaded solver ───────────────────────────────
        NQueensThreaded threaded = new NQueensThreaded();
        threaded.solve();
        threadTimeMs = threaded.getTimeTakenMs();

        // Save timing records to DB
        repo.saveTimingRecord("Sequential", seqTimeMs, totalSolutions);
        repo.saveTimingRecord("Threaded",   threadTimeMs,
                threaded.getTotalSolutions());

        initialized = true;

        QueensRound round = new QueensRound();
        round.setTotalSolutions(totalSolutions);
        round.setSeqTimeMs(seqTimeMs);
        round.setThreadTimeMs(threadTimeMs);
        round.setClaimedCount(0);
        round.setAllClaimed(false);
        return round;
    }

    // ── Process player's submitted board ──────────────────────
    // Returns: "WIN", "ALREADY_CLAIMED", "INVALID", "ALL_DONE"
    public String processSubmission(String playerName,
                                    int[] playerPositions) {
        if (!initialized) {
            throw new IllegalStateException(
                    "Game not initialized. Call initialize() first.");
        }

        // Validate the board first
        if (!isValidQueenPlacement(playerPositions)) {
            return "INVALID";
        }

        // Check if all solutions have been claimed
        if (repo.areAllClaimed()) {
            // Reset all claims so future players can try again
            repo.resetAllClaims();
            return "ALL_DONE";
        }

        // Check if this solution exists and is unclaimed
        QueenSolution match = repo.findUnclaimedMatch(playerPositions);
        if (match != null) {
            // Claim it for this player
            repo.claimSolution(match.getId(), playerName);
            repo.savePlayerResult(playerName,
                    match.toPositionString(), match.getSolutionIndex());
            return "WIN";
        }

        // Check if this solution exists but is already claimed
        if (repo.isAlreadyClaimed(playerPositions)) {
            return "ALREADY_CLAIMED";
        }

        // Valid placement but not a correct solution
        return "INVALID";
    }

    // ── Validate: all 16 queens placed, no threats ────────────
    public boolean isValidQueenPlacement(int[] positions) {
        if (positions == null || positions.length != BOARD_SIZE) {
            return false;
        }

        // Check each position is within bounds
        for (int r = 0; r < BOARD_SIZE; r++) {
            if (positions[r] < 0 || positions[r] >= BOARD_SIZE) {
                return false;
            }
        }

        // Check no two queens threaten each other
        for (int r1 = 0; r1 < BOARD_SIZE; r1++) {
            for (int r2 = r1 + 1; r2 < BOARD_SIZE; r2++) {
                int c1 = positions[r1];
                int c2 = positions[r2];
                // Same column
                if (c1 == c2) return false;
                // Same diagonal
                if (Math.abs(c1 - c2) == Math.abs(r1 - r2)) return false;
            }
        }
        return true;
    }

    // ── Getters ───────────────────────────────────────────────
    public int getTotalSolutions()  { return totalSolutions; }
    public long getSeqTimeMs()      { return seqTimeMs; }
    public long getThreadTimeMs()   { return threadTimeMs; }
    public boolean isInitialized()  { return initialized; }

    public int getClaimedCount() {
        return repo.countClaimedSolutions();
    }

    public boolean areAllClaimed() {
        return repo.areAllClaimed();
    }
}