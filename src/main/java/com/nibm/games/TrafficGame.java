package com.nibm.games;

import com.nibm.algorithms.EdmondsKarp;
import com.nibm.algorithms.FordFulkerson;
import com.nibm.models.TrafficNetwork;
import com.nibm.models.TrafficRound;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

public class TrafficGame {

    private static final int MIN_CAP = 5;
    private static final int MAX_CAP = 15;

    private TrafficNetwork network;
    private int correctAnswer;
    private int ekAnswer;
    private long ffTimeNs;
    private long ekTimeNs;

    private int[][] originalCapacity;
    private int[][] residualCapacity;

    private int playerFlow;
    private boolean finished;

    private final List<Integer> selectedPath = new ArrayList<>();
    private final List<String> usedPaths = new ArrayList<>();

    private final Random random = new Random();

    public TrafficRound newRound(int roundNumber) {
        int[][] capacity = buildCapacityMatrix();

        this.originalCapacity = deepCopy(capacity);
        this.network = new TrafficNetwork(deepCopy(capacity));
        this.residualCapacity = deepCopy(capacity);

        this.playerFlow = 0;
        this.finished = false;
        this.selectedPath.clear();
        this.usedPaths.clear();

        FordFulkerson ff = new FordFulkerson();
        long ffStart = System.nanoTime();
        ff.solve(deepCopy(capacity), TrafficNetwork.SOURCE, TrafficNetwork.SINK);
        ffTimeNs = System.nanoTime() - ffStart;
        correctAnswer = ff.getMaxFlow();

        EdmondsKarp ek = new EdmondsKarp();
        long ekStart = System.nanoTime();
        ek.solve(deepCopy(capacity), TrafficNetwork.SOURCE, TrafficNetwork.SINK);
        ekTimeNs = System.nanoTime() - ekStart;
        ekAnswer = ek.getMaxFlow();

        TrafficRound round = new TrafficRound();
        round.setRoundNumber(roundNumber);
        round.setCapacities(deepCopy(capacity));
        round.setFordFulkersonAnswer(correctAnswer);
        round.setEdmondsKarpAnswer(ekAnswer);
        round.setFfTimeNs(ffTimeNs);
        round.setEkTimeNs(ekTimeNs);
        return round;
    }

    private int[][] buildCapacityMatrix() {
        int n = TrafficNetwork.NODE_COUNT;
        int[][] cap = new int[n][n];
        for (int[] edge : TrafficNetwork.EDGES) {
            cap[edge[0]][edge[1]] = MIN_CAP + random.nextInt(MAX_CAP - MIN_CAP + 1);
        }
        return cap;
    }

    public boolean canSelectNode(int node) {
        if (finished) return false;
        if (node < 0 || node >= TrafficNetwork.NODE_COUNT) return false;

        if (selectedPath.isEmpty()) {
            return node == TrafficNetwork.SOURCE;
        }

        int last = selectedPath.get(selectedPath.size() - 1);

        // Only allow movement on actual original graph edges with remaining residual capacity
        if (originalCapacity[last][node] <= 0) return false;
        if (residualCapacity[last][node] <= 0) return false;

        // Prevent cycles/repeated nodes in a single selected path
        return !selectedPath.contains(node);
    }

    public boolean addNodeToPath(int node) {
        if (!canSelectNode(node)) return false;
        selectedPath.add(node);
        return true;
    }

    public void clearCurrentPath() {
        selectedPath.clear();
    }

    public boolean isPathComplete() {
        return !selectedPath.isEmpty()
                && selectedPath.get(0) == TrafficNetwork.SOURCE
                && selectedPath.get(selectedPath.size() - 1) == TrafficNetwork.SINK;
    }

    public int getCurrentPathBottleneck() {
        if (!isPathComplete()) return 0;

        int bottleneck = Integer.MAX_VALUE;
        for (int i = 0; i < selectedPath.size() - 1; i++) {
            int u = selectedPath.get(i);
            int v = selectedPath.get(i + 1);
            bottleneck = Math.min(bottleneck, residualCapacity[u][v]);
        }
        return bottleneck;
    }

    public boolean applyCurrentPath() {
        if (!isPathComplete()) return false;

        int bottleneck = getCurrentPathBottleneck();
        if (bottleneck <= 0) return false;

        for (int i = 0; i < selectedPath.size() - 1; i++) {
            int u = selectedPath.get(i);
            int v = selectedPath.get(i + 1);

            // Correct residual update
            residualCapacity[u][v] -= bottleneck;
            residualCapacity[v][u] += bottleneck;
        }

        playerFlow += bottleneck;
        usedPaths.add(pathToString(selectedPath) + "  |  +" + bottleneck);

        selectedPath.clear();

        if (!hasAnyForwardAugmentingPath()) {
            finished = true;
        }

        return true;
    }

    // For the player UI, we only care whether another usable forward path exists
    private boolean hasAnyForwardAugmentingPath() {
        int n = TrafficNetwork.NODE_COUNT;
        boolean[] visited = new boolean[n];
        Queue<Integer> queue = new LinkedList<>();

        queue.offer(TrafficNetwork.SOURCE);
        visited[TrafficNetwork.SOURCE] = true;

        while (!queue.isEmpty()) {
            int u = queue.poll();
            if (u == TrafficNetwork.SINK) return true;

            for (int v = 0; v < n; v++) {
                if (!visited[v] && originalCapacity[u][v] > 0 && residualCapacity[u][v] > 0) {
                    visited[v] = true;
                    queue.offer(v);
                }
            }
        }
        return false;
    }

    private String pathToString(List<Integer> path) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < path.size(); i++) {
            if (i > 0) sb.append(" → ");
            sb.append(TrafficNetwork.NODE_NAMES[path.get(i)]);
        }
        return sb.toString();
    }

    public boolean isPlayerOptimal() {
        return playerFlow == correctAnswer;
    }

    public TrafficNetwork getNetwork() {
        return network;
    }

    public int getCorrectAnswer() {
        return correctAnswer;
    }

    public int getEkAnswer() {
        return ekAnswer;
    }

    public long getFfTimeNs() {
        return ffTimeNs;
    }

    public long getEkTimeNs() {
        return ekTimeNs;
    }

    public int[][] getResidualCapacity() {
        return deepCopy(residualCapacity);
    }

    public int[][] getOriginalCapacity() {
        return deepCopy(originalCapacity);
    }

    public int getPlayerFlow() {
        return playerFlow;
    }

    public boolean isFinished() {
        return finished;
    }

    public List<Integer> getSelectedPath() {
        return new ArrayList<>(selectedPath);
    }

    public List<String> getUsedPaths() {
        return new ArrayList<>(usedPaths);
    }

    private int[][] deepCopy(int[][] src) {
        int[][] copy = new int[src.length][];
        for (int i = 0; i < src.length; i++) {
            copy[i] = src[i].clone();
        }
        return copy;
    }
}