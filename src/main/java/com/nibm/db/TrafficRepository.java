package com.nibm.db;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.nibm.models.TrafficNetwork;
import com.nibm.models.TrafficRound;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TrafficRepository {

    private final MongoCollection<Document> roundsCol;
    private final MongoCollection<Document> timingsCol;
    private final MongoCollection<Document> resultsCol;

    public TrafficRepository() {
        MongoDatabase db = MongoDBConnection.getDatabase();
        roundsCol  = db.getCollection("traffic_rounds");
        timingsCol = db.getCollection("algorithm_timings");
        resultsCol = db.getCollection("player_results");
    }

    // ── Save full round ───────────────────────────────────────
    public void saveRound(TrafficRound round) {
        try {
            // Save edge capacities as list of documents
            List<Document> edgeDocs = new ArrayList<>();
            int[][] cap = round.getCapacities();
            for (int[] edge : TrafficNetwork.EDGES) {
                int from = edge[0];
                int to   = edge[1];
                edgeDocs.add(new Document()
                        .append("from",     TrafficNetwork.NODE_NAMES[from])
                        .append("to",       TrafficNetwork.NODE_NAMES[to])
                        .append("capacity", cap[from][to])
                );
            }

            Document doc = new Document()
                    .append("gameType",            "TRAFFIC")
                    .append("roundNumber",         round.getRoundNumber())
                    .append("edges",               edgeDocs)
                    .append("fordFulkersonAnswer", round.getFordFulkersonAnswer())
                    .append("edmondsKarpAnswer",   round.getEdmondsKarpAnswer())
                    .append("ffTimeMs",            round.getFfTimeMs())
                    .append("ekTimeMs",            round.getEkTimeMs())
                    .append("timestamp",           new Date());

            roundsCol.insertOne(doc);

            // Save timing records separately
            saveTimingRecord(round.getRoundNumber(),
                    "FordFulkerson", round.getFfTimeMs());
            saveTimingRecord(round.getRoundNumber(),
                    "EdmondsKarp",   round.getEkTimeMs());

        } catch (Exception e) {
            System.err.println("DB Error saving traffic round: "
                    + e.getMessage());
        }
    }

    // ── Save correct player result ────────────────────────────
    public void savePlayerResult(String playerName,
                                 int answer, int roundNumber) {
        try {
            Document doc = new Document()
                    .append("playerName",  playerName)
                    .append("answer",      answer)
                    .append("roundNumber", roundNumber)
                    .append("gameType",    "TRAFFIC")
                    .append("timestamp",   new Date());
            resultsCol.insertOne(doc);
        } catch (Exception e) {
            System.err.println("DB Error saving player result: "
                    + e.getMessage());
        }
    }

    // ── Private timing helper ─────────────────────────────────
    private void saveTimingRecord(int roundNumber,
                                  String algorithm, long timeMs) {
        try {
            Document doc = new Document()
                    .append("gameType",    "TRAFFIC")
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