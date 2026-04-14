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
        roundsCol = db.getCollection("game_rounds");
        timingsCol = db.getCollection("algorithm_timings");
        resultsCol = db.getCollection("player_results");
    }

    public void saveRound(GameRound round) {
        try {
            Document doc = new Document()
                    .append("gameType", round.getGameType())
                    .append("mode", round.getMode())
                    .append("roundNumber", round.getRoundNumber())
                    .append("n", round.getN())
                    .append("hungarianCost", round.getHungarianCost())
                    .append("greedyCost", round.getGreedyCost())
                    .append("hungarianTimeNs", round.getHungarianTimeNs())
                    .append("greedyTimeNs", round.getGreedyTimeNs())
                    .append("timestamp", new Date());

            roundsCol.insertOne(doc);

            saveTimingRecord(round.getGameType(), round.getMode(),
                    round.getRoundNumber(), "Hungarian", round.getHungarianTimeNs());

            saveTimingRecord(round.getGameType(), round.getMode(),
                    round.getRoundNumber(), "Greedy", round.getGreedyTimeNs());

        } catch (Exception e) {
            System.err.println("DB Error saving round: " + e.getMessage());
        }
    }

    public void savePlayerResult(String playerName, int answer,
                                 int roundNumber, String gameType, String mode) {
        try {
            Document doc = new Document()
                    .append("playerName", playerName)
                    .append("answer", answer)
                    .append("roundNumber", roundNumber)
                    .append("gameType", gameType)
                    .append("mode", mode)
                    .append("timestamp", new Date());

            resultsCol.insertOne(doc);
        } catch (Exception e) {
            System.err.println("DB Error saving player result: " + e.getMessage());
        }
    }

    private void saveTimingRecord(String gameType, String mode, int roundNumber,
                                  String algorithm, long timeNs) {
        try {
            Document doc = new Document()
                    .append("gameType", gameType)
                    .append("mode", mode)
                    .append("roundNumber", roundNumber)
                    .append("algorithm", algorithm)
                    .append("timeNs", timeNs)
                    .append("timestamp", new Date());

            timingsCol.insertOne(doc);
        } catch (Exception e) {
            System.err.println("DB Error saving timing record: " + e.getMessage());
        }
    }
}