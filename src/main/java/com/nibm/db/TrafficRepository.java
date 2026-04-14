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
        roundsCol = db.getCollection("traffic_rounds");
        timingsCol = db.getCollection("algorithm_timings");
        resultsCol = db.getCollection("player_results");
    }

    public void saveRound(TrafficRound round) {
        try {
            List<Document> edgeDocs = new ArrayList<>();
            int[][] cap = round.getCapacities();

            for (int[] edge : TrafficNetwork.EDGES) {
                int from = edge[0];
                int to = edge[1];
                edgeDocs.add(new Document()
                        .append("from", TrafficNetwork.NODE_NAMES[from])
                        .append("to", TrafficNetwork.NODE_NAMES[to])
                        .append("capacity", cap[from][to]));
            }

            Document doc = new Document()
                    .append("gameType", "TRAFFIC")
                    .append("roundNumber", round.getRoundNumber())
                    .append("edges", edgeDocs)
                    .append("fordFulkersonAnswer", round.getFordFulkersonAnswer())
                    .append("edmondsKarpAnswer", round.getEdmondsKarpAnswer())
                    .append("ffTimeNs", round.getFfTimeNs())
                    .append("ekTimeNs", round.getEkTimeNs())
                    .append("timestamp", new Date());

            roundsCol.insertOne(doc);

            saveTimingRecord(round.getRoundNumber(), "FordFulkerson", round.getFfTimeNs());
            saveTimingRecord(round.getRoundNumber(), "EdmondsKarp", round.getEkTimeNs());

        } catch (Exception e) {
            System.err.println("DB Error saving traffic round: " + e.getMessage());
        }
    }

    public void savePlayerResult(String playerName, int playerFlow,
                                 int roundNumber, List<String> paths) {
        try {
            Document doc = new Document()
                    .append("playerName", playerName)
                    .append("playerFlow", playerFlow)
                    .append("roundNumber", roundNumber)
                    .append("paths", paths)
                    .append("gameType", "TRAFFIC")
                    .append("timestamp", new Date());

            resultsCol.insertOne(doc);
        } catch (Exception e) {
            System.err.println("DB Error saving player result: " + e.getMessage());
        }
    }

    private void saveTimingRecord(int roundNumber, String algorithm, long timeNs) {
        try {
            Document doc = new Document()
                    .append("gameType", "TRAFFIC")
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