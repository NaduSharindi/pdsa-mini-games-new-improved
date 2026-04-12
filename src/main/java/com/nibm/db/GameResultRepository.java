package com.nibm.db;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.nibm.models.GameRound;
import org.bson.Document;

import java.util.Date;

public class GameResultRepository {

    private final MongoCollection<Document> roundsCol;
    private final MongoCollection<Document> timingsCol;
    private final MongoCollection<Document> resultsCol;

    public GameResultRepository() {
        MongoDatabase db = MongoDBConnection.getDatabase();
        roundsCol  = db.getCollection("game_rounds");
        timingsCol = db.getCollection("algorithm_timings");
        resultsCol = db.getCollection("player_results");
    }

    // ── Save full round data ──────────────────────────────────
    public void saveRound(GameRound round) {
        try {
            Document doc = new Document()
                    .append("gameType",        round.getGameType())
                    .append("roundNumber",     round.getRoundNumber())
                    .append("n",               round.getN())
                    .append("hungarianCost",   round.getHungarianCost())
                    .append("greedyCost",      round.getGreedyCost())
                    .append("hungarianTimeMs", round.getHungarianTimeMs())
                    .append("greedyTimeMs",    round.getGreedyTimeMs())
                    .append("timestamp",       new Date());
            roundsCol.insertOne(doc);

            // Save timing records separately for chart reporting
            saveTimingRecord(round.getGameType(), round.getRoundNumber(),
                    "Hungarian", round.getHungarianTimeMs());
            saveTimingRecord(round.getGameType(), round.getRoundNumber(),
                    "Greedy", round.getGreedyTimeMs());

        } catch (Exception e) {
            System.err.println("DB Error saving round: " + e.getMessage());
        }
    }

    // ── Save player correct result ────────────────────────────
    public void savePlayerResult(String playerName, int answer,
                                 int roundNumber, String gameType) {
        try {
            Document doc = new Document()
                    .append("playerName",  playerName)
                    .append("answer",      answer)
                    .append("roundNumber", roundNumber)
                    .append("gameType",    gameType)
                    .append("timestamp",   new Date());
            resultsCol.insertOne(doc);
        } catch (Exception e) {
            System.err.println("DB Error saving player result: " + e.getMessage());
        }
    }

    // ── Save timing record ────────────────────────────────────
    private void saveTimingRecord(String gameType, int roundNumber,
                                  String algorithm, long timeMs) {
        Document doc = new Document()
                .append("gameType",    gameType)
                .append("roundNumber", roundNumber)
                .append("algorithm",   algorithm)
                .append("timeMs",      timeMs)
                .append("timestamp",   new Date());
        timingsCol.insertOne(doc);
    }
}