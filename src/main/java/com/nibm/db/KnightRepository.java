package com.nibm.db;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.nibm.models.KnightMove;
import com.nibm.models.KnightRound;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class KnightRepository {

    private final MongoCollection<Document> roundsCol;
    private final MongoCollection<Document> timingsCol;
    private final MongoCollection<Document> resultsCol;

    public KnightRepository() {
        MongoDatabase db = MongoDBConnection.getDatabase();
        roundsCol  = db.getCollection("knight_rounds");
        timingsCol = db.getCollection("algorithm_timings");
        resultsCol = db.getCollection("player_results");
    }

    // ── Save full round ───────────────────────────────────────
    public void saveRound(KnightRound round,
                          List<KnightMove> wMoves,
                          List<KnightMove> bMoves) {
        try {
            Document doc = new Document()
                    .append("gameType",          "KNIGHT_TOUR")
                    .append("roundNumber",       round.getRoundNumber())
                    .append("boardSize",         round.getBoardSize())
                    .append("startRow",          round.getStartRow())
                    .append("startCol",          round.getStartCol())
                    .append("warnsdorffSolved",  round.isWarnsdorffSolved())
                    .append("backtrackSolved",   round.isBacktrackSolved())
                    .append("warnsdorffTimeMs",  round.getWarnsdorffTimeMs())
                    .append("backtrackTimeMs",   round.getBacktrackTimeMs())
                    .append("warnsdorffTour",    movesToDocs(wMoves))
                    .append("backtrackTour",     movesToDocs(bMoves))
                    .append("timestamp",         new Date());

            roundsCol.insertOne(doc);

            // Timing records
            saveTimingRecord(round.getRoundNumber(),
                    "Warnsdorff",  round.getWarnsdorffTimeMs());
            saveTimingRecord(round.getRoundNumber(),
                    "Backtracking", round.getBacktrackTimeMs());

        } catch (Exception e) {
            System.err.println("DB Error saving knight round: "
                    + e.getMessage());
        }
    }

    // ── Save correct player result ────────────────────────────
    public void savePlayerResult(String playerName,
                                 String answer, int roundNumber) {
        try {
            Document doc = new Document()
                    .append("playerName",  playerName)
                    .append("answer",      answer)
                    .append("roundNumber", roundNumber)
                    .append("gameType",    "KNIGHT_TOUR")
                    .append("timestamp",   new Date());
            resultsCol.insertOne(doc);
        } catch (Exception e) {
            System.err.println("DB Error saving player result: "
                    + e.getMessage());
        }
    }

    // ── Helpers ───────────────────────────────────────────────
    private List<Document> movesToDocs(List<KnightMove> moves) {
        List<Document> docs = new ArrayList<>();
        for (KnightMove m : moves) {
            docs.add(new Document()
                    .append("moveNumber", m.getMoveNumber())
                    .append("row",        m.getRow())
                    .append("col",        m.getCol())
            );
        }
        return docs;
    }

    private void saveTimingRecord(int roundNumber,
                                  String algorithm, long timeMs) {
        try {
            Document doc = new Document()
                    .append("gameType",    "KNIGHT_TOUR")
                    .append("roundNumber", roundNumber)
                    .append("algorithm",   algorithm)
                    .append("timeMs",      timeMs)
                    .append("timestamp",   new Date());
            timingsCol.insertOne(doc);
        } catch (Exception e) {
            System.err.println("DB Error saving timing: "
                    + e.getMessage());
        }
    }
}