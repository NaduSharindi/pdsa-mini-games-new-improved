package com.nibm.models;

import org.bson.types.ObjectId;

public class QueenSolution {

    private ObjectId id;
    private int solutionIndex;  // 1-based index
    private int[] positions;    // positions[row] = col of queen
    private boolean claimed;
    private String claimedBy;   // player name who claimed it

    public QueenSolution() {}

    public QueenSolution(int solutionIndex, int[] positions) {
        this.solutionIndex = solutionIndex;
        this.positions     = positions;
        this.claimed       = false;
        this.claimedBy     = null;
    }

    // Convert positions array to readable string "R0C3,R1C7,..."
    public String toPositionString() {
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < positions.length; r++) {
            if (r > 0) sb.append(",");
            sb.append("R").append(r).append("C").append(positions[r]);
        }
        return sb.toString();
    }

    // Check if this solution matches a given board
    public boolean matches(int[] playerPositions) {
        if (playerPositions == null
                || playerPositions.length != positions.length) {
            return false;
        }
        for (int r = 0; r < positions.length; r++) {
            if (positions[r] != playerPositions[r]) return false;
        }
        return true;
    }

    // ── Getters & Setters ─────────────────────────────────────
    public ObjectId getId()                  { return id; }
    public void setId(ObjectId id)           { this.id = id; }

    public int getSolutionIndex()            { return solutionIndex; }
    public void setSolutionIndex(int i)      { this.solutionIndex = i; }

    public int[] getPositions()              { return positions; }
    public void setPositions(int[] p)        { this.positions = p; }

    public boolean isClaimed()              { return claimed; }
    public void setClaimed(boolean c)        { this.claimed = c; }

    public String getClaimedBy()             { return claimedBy; }
    public void setClaimedBy(String p)       { this.claimedBy = p; }
}