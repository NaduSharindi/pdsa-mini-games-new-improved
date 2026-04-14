package com.nibm.db;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.nibm.models.QueenSolution;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class QueensSolutionRepository {

    private final MongoCollection<Document> solutionsCol;
    private final MongoCollection<Document> timingsCol;
    private final MongoCollection<Document> resultsCol;
    private final MongoCollection<Document> statsCol;

    public QueensSolutionRepository() {
        MongoDatabase db = MongoDBConnection.getDatabase();
        solutionsCol = db.getCollection("queens_solutions");
        timingsCol   = db.getCollection("algorithm_timings");
        resultsCol   = db.getCollection("player_results");
        statsCol     = db.getCollection("queens_stats");
    }

    // ── Save one solution (unclaimed) ─────────────────────────
    public void saveSolution(QueenSolution solution) {
        try {
            // Convert int[] positions to List<Integer> for MongoDB
            List<Integer> posList = new ArrayList<>();
            for (int p : solution.getPositions()) posList.add(p);

            Document doc = new Document()
                    .append("solutionIndex", solution.getSolutionIndex())
                    .append("positions",     posList)
                    .append("claimed",       false)
                    .append("claimedBy",     null)
                    .append("timestamp",     new Date());
            solutionsCol.insertOne(doc);
        } catch (Exception e) {
            System.err.println("DB Error saving solution: "
                    + e.getMessage());
        }
    }

    // ── Count total solutions stored ──────────────────────────
    public long countSolutions() {
        try {
            return solutionsCol.countDocuments();
        } catch (Exception e) {
            System.err.println("DB Error counting solutions: "
                    + e.getMessage());
            return 0;
        }
    }

    // ── Count claimed solutions ───────────────────────────────
    public int countClaimedSolutions() {
        try {
            return (int) solutionsCol.countDocuments(
                    Filters.eq("claimed", true));
        } catch (Exception e) {
            System.err.println("DB Error counting claimed: "
                    + e.getMessage());
            return 0;
        }
    }

    // ── Check if all solutions are claimed ────────────────────
    public boolean areAllClaimed() {
        try {
            long total    = solutionsCol.countDocuments();
            long claimed  = solutionsCol.countDocuments(
                    Filters.eq("claimed", true));
            return total > 0 && claimed >= total;
        } catch (Exception e) {
            System.err.println("DB Error checking all claimed: "
                    + e.getMessage());
            return false;
        }
    }

    // ── Find unclaimed solution matching player positions ─────
    public QueenSolution findUnclaimedMatch(int[] playerPositions) {
        try {
            // Convert player positions to List<Integer>
            List<Integer> posList = new ArrayList<>();
            for (int p : playerPositions) posList.add(p);

            Document doc = solutionsCol.find(
                    Filters.and(
                            Filters.eq("positions", posList),
                            Filters.eq("claimed", false)
                    )
            ).first();

            if (doc == null) return null;
            return docToSolution(doc);

        } catch (Exception e) {
            System.err.println("DB Error finding unclaimed match: "
                    + e.getMessage());
            return null;
        }
    }

    // ── Check if positions match an already-claimed solution ──
    public boolean isAlreadyClaimed(int[] playerPositions) {
        try {
            List<Integer> posList = new ArrayList<>();
            for (int p : playerPositions) posList.add(p);

            Document doc = solutionsCol.find(
                    Filters.and(
                            Filters.eq("positions", posList),
                            Filters.eq("claimed", true)
                    )
            ).first();
            return doc != null;
        } catch (Exception e) {
            System.err.println("DB Error checking claimed: "
                    + e.getMessage());
            return false;
        }
    }

    // ── Claim a solution for a player ─────────────────────────
    public void claimSolution(ObjectId id, String playerName) {
        try {
            solutionsCol.updateOne(
                    Filters.eq("_id", id),
                    Updates.combine(
                            Updates.set("claimed",   true),
                            Updates.set("claimedBy", playerName),
                            Updates.set("claimedAt", new Date())
                    )
            );
        } catch (Exception e) {
            System.err.println("DB Error claiming solution: "
                    + e.getMessage());
        }
    }

    // ── Reset ALL claims (when all solutions found) ───────────
    public void resetAllClaims() {
        try {
            solutionsCol.updateMany(
                    Filters.eq("claimed", true),
                    Updates.combine(
                            Updates.set("claimed",   false),
                            Updates.set("claimedBy", null),
                            Updates.unset("claimedAt")
                    )
            );
            System.out.println("All solution claims reset.");
        } catch (Exception e) {
            System.err.println("DB Error resetting claims: "
                    + e.getMessage());
        }
    }

    // ── Save player correct result ────────────────────────────
    public void savePlayerResult(String playerName,
                                 String positionString,
                                 int solutionIndex) {
        try {
            Document doc = new Document()
                    .append("gameType",      "SIXTEEN_QUEENS")
                    .append("playerName",    playerName)
                    .append("solutionIndex", solutionIndex)
                    .append("positions",     positionString)
                    .append("timestamp",     new Date());
            resultsCol.insertOne(doc);
        } catch (Exception e) {
            System.err.println("DB Error saving player result: "
                    + e.getMessage());
        }
    }

    // ── Save timing record ────────────────────────────────────
    public void saveTimingRecord(String algorithm,
                                 long timeNs, int totalSolutions) {
        try {
            Document doc = new Document()
                    .append("gameType",       "SIXTEEN_QUEENS")
                    .append("algorithm",      algorithm)
                    .append("timeNs",         timeNs)
                    .append("totalSolutions", totalSolutions)
                    .append("timestamp",      new Date());
            timingsCol.insertOne(doc);
        } catch (Exception e) {
            System.err.println("DB Error saving timing: "
                    + e.getMessage());
        }
    }

    // ── Get sequential time from DB ───────────────────────────
    public long getSeqTime() {
        try {
            Document doc = timingsCol.find(
                    Filters.and(
                            Filters.eq("gameType",  "SIXTEEN_QUEENS"),
                            Filters.eq("algorithm", "Sequential")
                    )
            ).first();
            return doc != null ? doc.getLong("timeNs") : 0L;
        } catch (Exception e) {
            return 0L;
        }
    }

    // ── Get threaded time from DB ─────────────────────────────
    public long getThreadTime() {
        try {
            Document doc = timingsCol.find(
                    Filters.and(
                            Filters.eq("gameType",  "SIXTEEN_QUEENS"),
                            Filters.eq("algorithm", "Threaded")
                    )
            ).first();
            return doc != null ? doc.getLong("timeNs") : 0L;
        } catch (Exception e) {
            return 0L;
        }
    }

    // ── Helper: Document → QueenSolution ─────────────────────
    private QueenSolution docToSolution(Document doc) {
        QueenSolution sol = new QueenSolution();
        sol.setId(doc.getObjectId("_id"));
        sol.setSolutionIndex(doc.getInteger("solutionIndex"));
        sol.setClaimed(doc.getBoolean("claimed", false));
        sol.setClaimedBy(doc.getString("claimedBy"));

        // Convert List<Integer> back to int[]
        List<?> posList = (List<?>) doc.get("positions");
        int[] pos = new int[posList.size()];
        for (int i = 0; i < posList.size(); i++) {
            pos[i] = ((Number) posList.get(i)).intValue();
        }
        sol.setPositions(pos);
        return sol;
    }
}