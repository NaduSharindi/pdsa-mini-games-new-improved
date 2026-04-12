package com.nibm.models;

public class KnightRound {

    private int roundNumber;
    private int boardSize;      // 8 or 16
    private int startRow;
    private int startCol;
    private int[][] warnsdorffTour;
    private int[][] backtrackTour;
    private long warnsdorffTimeMs;
    private long backtrackTimeMs;
    private boolean warnsdorffSolved;
    private boolean backtrackSolved;
    private String playerName;
    private String playerAnswer;  // player's submitted move sequence
    private boolean correct;

    public KnightRound() {}

    // ── Getters & Setters ─────────────────────────────────────
    public int getRoundNumber()                  { return roundNumber; }
    public void setRoundNumber(int r)            { this.roundNumber = r; }

    public int getBoardSize()                    { return boardSize; }
    public void setBoardSize(int b)              { this.boardSize = b; }

    public int getStartRow()                     { return startRow; }
    public void setStartRow(int r)               { this.startRow = r; }

    public int getStartCol()                     { return startCol; }
    public void setStartCol(int c)               { this.startCol = c; }

    public int[][] getWarnsdorffTour()           { return warnsdorffTour; }
    public void setWarnsdorffTour(int[][] t)     { this.warnsdorffTour = t; }

    public int[][] getBacktrackTour()            { return backtrackTour; }
    public void setBacktrackTour(int[][] t)      { this.backtrackTour = t; }

    public long getWarnsdorffTimeMs()            { return warnsdorffTimeMs; }
    public void setWarnsdorffTimeMs(long t)      { this.warnsdorffTimeMs = t; }

    public long getBacktrackTimeMs()             { return backtrackTimeMs; }
    public void setBacktrackTimeMs(long t)       { this.backtrackTimeMs = t; }

    public boolean isWarnsdorffSolved()          { return warnsdorffSolved; }
    public void setWarnsdorffSolved(boolean s)   { this.warnsdorffSolved = s; }

    public boolean isBacktrackSolved()           { return backtrackSolved; }
    public void setBacktrackSolved(boolean s)    { this.backtrackSolved = s; }

    public String getPlayerName()                { return playerName; }
    public void setPlayerName(String p)          { this.playerName = p; }

    public String getPlayerAnswer()              { return playerAnswer; }
    public void setPlayerAnswer(String a)        { this.playerAnswer = a; }

    public boolean isCorrect()                   { return correct; }
    public void setCorrect(boolean c)            { this.correct = c; }
}