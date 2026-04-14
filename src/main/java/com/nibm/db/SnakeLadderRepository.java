package com.nibm.db;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.nibm.models.SnakeLadderRound;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SnakeLadderRepository {

    private final MongoCollection<Document> roundsCol;
    private final MongoCollection<Document> timingsCol;
    private final MongoCollection<Document> resultsCol;

    public SnakeLadderRepository() {
        MongoDatabase db = MongoDBConnection.getDatabase();
        roundsCol = db.getCollection("sl_rounds");
        timingsCol = db.getCollection("algorithm_timings");
        resultsCol = db.getCollection("player_results");
    }

    public void saveRound(SnakeLadderRound round) {
        try {
            List<Document> snakeDocs = new ArrayList<>();
            for (int[] s : round.getSnakes()) {
                snakeDocs.add(new Document("head", s[0]).append("tail", s[1]));
            }

            List<Document> ladderDocs = new ArrayList<>();
            for (int[] l : round.getLadders()) {
                ladderDocs.add(new Document("base", l[0]).append("top", l[1]));
            }

            Document doc = new Document()
                    .append("gameType", "SNAKE_LADDER")
                    .append("roundNumber", round.getRoundNumber())
                    .append("n", round.getN())
                    .append("totalCells", round.getTotalCells())
                    .append("snakes", snakeDocs)
                    .append("ladders", ladderDocs)
                    .append("bfsAnswer", round.getBfsAnswer())
                    .append("dijkstraAnswer", round.getDijkstraAnswer())
                    .append("bfsTimeNs", round.getBfsTimeNs())
                    .append("dijkstraTimeNs", round.getDijkstraTimeNs())
                    .append("timestamp", new Date());

            roundsCol.insertOne(doc);

            saveTimingRecord("SNAKE_LADDER", round.getRoundNumber(),
                    "BFS", round.getBfsTimeNs());
            saveTimingRecord("SNAKE_LADDER", round.getRoundNumber(),
                    "Dijkstra", round.getDijkstraTimeNs());

        } catch (Exception e) {
            System.err.println("DB Error saving SL round: " + e.getMessage());
        }
    }

    public void savePlayerResult(String playerName, int throwsUsed,
                                 int finalPosition, int roundNumber) {
        try {
            Document doc = new Document()
                    .append("playerName", playerName)
                    .append("throwsUsed", throwsUsed)
                    .append("finalPosition", finalPosition)
                    .append("roundNumber", roundNumber)
                    .append("gameType", "SNAKE_LADDER")
                    .append("timestamp", new Date());

            resultsCol.insertOne(doc);
        } catch (Exception e) {
            System.err.println("DB Error saving player result: " + e.getMessage());
        }
    }

    private void saveTimingRecord(String gameType, int roundNumber,
                                  String algorithm, long timeNs) {
        try {
            Document doc = new Document()
                    .append("gameType", gameType)
                    .append("roundNumber", roundNumber)
                    .append("algorithm", algorithm)
                    .append("timeNs", timeNs)
                    .append("timestamp", new Date());
            timingsCol.insertOne(doc);
        } catch (Exception e) {
            System.err.println("DB Error saving timing: " + e.getMessage());
        }
    }
}