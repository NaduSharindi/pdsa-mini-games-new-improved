package com.nibm.models;

public class QueensRound {

    private int totalSolutions;
    private long seqTimeMs;
    private long threadTimeMs;
    private int claimedCount;
    private boolean allClaimed;

    public QueensRound() {}

    public int getTotalSolutions()          { return totalSolutions; }
    public void setTotalSolutions(int t)    { this.totalSolutions = t; }

    public long getSeqTimeMs()              { return seqTimeMs; }
    public void setSeqTimeMs(long t)        { this.seqTimeMs = t; }

    public long getThreadTimeMs()           { return threadTimeMs; }
    public void setThreadTimeMs(long t)     { this.threadTimeMs = t; }

    public int getClaimedCount()            { return claimedCount; }
    public void setClaimedCount(int c)      { this.claimedCount = c; }

    public boolean isAllClaimed()           { return allClaimed; }
    public void setAllClaimed(boolean a)    { this.allClaimed = a; }
}