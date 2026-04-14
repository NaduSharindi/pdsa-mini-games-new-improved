package com.nibm.ui;

import com.nibm.db.TrafficRepository;
import com.nibm.games.TrafficGame;
import com.nibm.models.TrafficNetwork;
import com.nibm.models.TrafficRound;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class TrafficUI extends JDialog {

    private static final Color CLR_HEADER  = new Color(0x854F0B);
    private static final Color CLR_BG      = new Color(0xF1EFE8);
    private static final Color CLR_CARD    = new Color(0xFFFFFF);
    private static final Color CLR_BORDER  = new Color(0xD3D1C7);
    private static final Color CLR_TEXT    = new Color(0x2C2C2A);
    private static final Color CLR_MUTED   = new Color(0x888780);
    private static final Color CLR_SUCCESS = new Color(0x1D9E75);
    private static final Color CLR_DANGER  = new Color(0xE24B4A);
    private static final Color CLR_DRAW    = new Color(0xBA7517);
    private static final Color CLR_NODE_BG = new Color(0xFAEEDA);
    private static final Color CLR_NODE_ST = new Color(0x1D9E75);
    private static final Color CLR_NODE_SK = new Color(0xE24B4A);
    private static final Color CLR_EDGE    = new Color(0x854F0B);
    private static final Color CLR_PATH    = new Color(0x185FA5);

    private final TrafficGame game = new TrafficGame();
    private final TrafficRepository repo = new TrafficRepository();

    private TrafficRound currentRound;
    private int roundNumber = 0;
    private String playerName;

    private JLabel lblRound, lblFFTime, lblEKTime, lblFlow, lblPathInfo;
    private NetworkGraphPanel graphPanel;
    private JButton btnClearPath, btnApplyPath, btnFinish, btnNextRound;
    private JTextArea txtUsedPaths;
    private JPanel statusPanel;
    private JLabel lblStatus;

    public TrafficUI(JFrame parent) {
        super(parent, "Game 3 — Traffic Simulation", true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(false);

        playerName = askPlayerName(parent);
        if (playerName == null) {
            dispose();
            return;
        }

        buildUI();
        setSize(700, 800);
        setMinimumSize(new Dimension(780, 760));
        setLocationRelativeTo(parent);
        startNewRound();
        setVisible(true);
    }

    private String askPlayerName(JFrame parent) {
        while (true) {
            String name = JOptionPane.showInputDialog(
                    parent, "Enter your name:", "Player Name",
                    JOptionPane.PLAIN_MESSAGE);
            if (name == null) return null;
            name = name.trim();
            if (name.isEmpty()) {
                showError("Name cannot be empty.");
                continue;
            }
            if (!name.matches("[a-zA-Z ]+")) {
                showError("Name must contain letters and spaces only.");
                continue;
            }
            if (name.length() < 2) {
                showError("Name must be at least 2 characters.");
                continue;
            }
            if (name.length() > 50) {
                showError("Name too long. Max 50 characters.");
                continue;
            }
            return name;
        }
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(CLR_BG);
        root.add(buildHeader(), BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(buildBody());
        scroll.setBorder(null);
        scroll.getViewport().setBackground(CLR_BG);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        root.add(scroll, BorderLayout.CENTER);

        root.add(buildFooter(), BorderLayout.SOUTH);
        setContentPane(root);
    }

    private JPanel buildHeader() {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(CLR_HEADER);
        h.setBorder(new EmptyBorder(16, 24, 14, 24));

        JLabel title = new JLabel("Traffic Simulation — Build Max Flow A → T");
        title.setFont(new Font("SansSerif", Font.BOLD, 15));
        title.setForeground(Color.WHITE);

        lblRound = new JLabel("Round 0");
        lblRound.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblRound.setForeground(new Color(0xFFD799));

        h.add(title, BorderLayout.CENTER);
        h.add(lblRound, BorderLayout.EAST);
        return h;
    }

    private JPanel buildBody() {
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(CLR_BG);
        body.setBorder(new EmptyBorder(14, 16, 8, 16));

        body.add(buildPlayerCard());
        body.add(Box.createVerticalStrut(10));
        body.add(buildGraphCard());
        body.add(Box.createVerticalStrut(10));
        body.add(buildTimingCard());
        body.add(Box.createVerticalStrut(10));
        body.add(buildControlCard());
        body.add(Box.createVerticalStrut(10));
        body.add(buildUsedPathsCard());
        body.add(Box.createVerticalStrut(10));
        body.add(buildStatusAndNext());
        body.add(Box.createVerticalStrut(16));
        return body;
    }

    private JPanel buildPlayerCard() {
        JPanel card = makeCard();
        card.setLayout(new GridLayout(2, 2, 8, 6));

        card.add(makeLabel("Player:", CLR_MUTED, 12));
        JLabel lbl = new JLabel(playerName);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        lbl.setForeground(CLR_HEADER);
        card.add(lbl);

        card.add(makeLabel("Current Total Flow:", CLR_MUTED, 12));
        lblFlow = new JLabel("0");
        lblFlow.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblFlow.setForeground(CLR_SUCCESS);
        card.add(lblFlow);

        return card;
    }

    private JPanel buildGraphCard() {
        JPanel card = makeCard();
        card.setLayout(new BorderLayout());

        JLabel title = makeLabel(
                "Click nodes to build an augmenting path from A to T",
                CLR_MUTED, 11);
        title.setBorder(new EmptyBorder(0, 0, 8, 0));

        graphPanel = new NetworkGraphPanel();
        graphPanel.setPreferredSize(new Dimension(680, 320));

        card.add(title, BorderLayout.NORTH);
        card.add(graphPanel, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildTimingCard() {
        JPanel card = makeCard();
        card.setLayout(new GridLayout(3, 2, 8, 6));

        card.add(makeLabel("Algorithm", CLR_MUTED, 11));
        card.add(makeLabel("Time Taken", CLR_MUTED, 11));

        card.add(makeLabel("Ford-Fulkerson:", CLR_TEXT, 12));
        lblFFTime = new JLabel("—");
        lblFFTime.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblFFTime.setForeground(new Color(0x185FA5));
        card.add(lblFFTime);

        card.add(makeLabel("Edmonds-Karp:", CLR_TEXT, 12));
        lblEKTime = new JLabel("—");
        lblEKTime.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblEKTime.setForeground(CLR_DRAW);
        card.add(lblEKTime);

        return card;
    }

    private JPanel buildControlCard() {
        JPanel card = makeCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        lblPathInfo = new JLabel("Selected Path: —");
        lblPathInfo.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblPathInfo.setForeground(CLR_TEXT);
        lblPathInfo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        btnRow.setOpaque(false);

        btnClearPath = makeButton("Clear Path", CLR_BG, CLR_TEXT);
        btnClearPath.addActionListener(e -> clearPath());

        btnApplyPath = makeButton("Apply Path", CLR_HEADER, Color.WHITE);
        btnApplyPath.addActionListener(e -> applyPath());

        btnFinish = makeButton("Finish Round", CLR_BG, CLR_TEXT);
        btnFinish.addActionListener(e -> finishRound());

        btnRow.add(btnClearPath);
        btnRow.add(btnApplyPath);
        btnRow.add(btnFinish);

        card.add(lblPathInfo);
        card.add(Box.createVerticalStrut(8));
        card.add(btnRow);
        return card;
    }

    private JPanel buildUsedPathsCard() {
        JPanel card = makeCard();
        card.setLayout(new BorderLayout());

        JLabel title = makeLabel("Applied Paths", CLR_TEXT, 12);
        title.setFont(new Font("SansSerif", Font.BOLD, 12));

        txtUsedPaths = new JTextArea(6, 30);
        txtUsedPaths.setEditable(false);
        txtUsedPaths.setLineWrap(true);
        txtUsedPaths.setWrapStyleWord(true);
        txtUsedPaths.setFont(new Font("SansSerif", Font.PLAIN, 12));

        JScrollPane sp = new JScrollPane(txtUsedPaths);
        sp.setBorder(BorderFactory.createLineBorder(CLR_BORDER));

        card.add(title, BorderLayout.NORTH);
        card.add(sp, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildStatusAndNext() {
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setOpaque(false);
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);

        statusPanel = new JPanel(new BorderLayout());
        statusPanel.setOpaque(false);
        statusPanel.setVisible(false);
        statusPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        statusPanel.setMinimumSize(new Dimension(100, 46));
        statusPanel.setPreferredSize(new Dimension(660, 46));
        statusPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));

        lblStatus = new JLabel("", SwingConstants.CENTER);
        lblStatus.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblStatus.setBorder(new EmptyBorder(10, 16, 10, 16));
        lblStatus.setOpaque(true);
        statusPanel.add(lblStatus, BorderLayout.CENTER);

        btnNextRound = makeButton("Next Round →", CLR_BG, CLR_MUTED);
        btnNextRound.setEnabled(false);
        btnNextRound.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnNextRound.setMaximumSize(new Dimension(160, 36));
        btnNextRound.addActionListener(e -> startNewRound());

        wrapper.add(statusPanel);
        wrapper.add(Box.createVerticalStrut(8));
        wrapper.add(btnNextRound);
        wrapper.add(Box.createVerticalStrut(16));
        return wrapper;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT));
        footer.setBackground(CLR_BG);
        footer.setBorder(new EmptyBorder(0, 16, 10, 16));
        JButton btnBack = makeButton("← Back to Menu", CLR_BG, CLR_MUTED);
        btnBack.addActionListener(e -> dispose());
        footer.add(btnBack);
        return footer;
    }

    private void startNewRound() {
        roundNumber++;
        lblRound.setText("Round " + roundNumber);

        btnClearPath.setEnabled(false);
        btnApplyPath.setEnabled(false);
        btnFinish.setEnabled(false);
        btnNextRound.setEnabled(false);
        statusPanel.setVisible(false);
        txtUsedPaths.setText("");
        lblPathInfo.setText("Selected Path: —");
        lblFlow.setText("0");
        lblFFTime.setText("Computing...");
        lblEKTime.setText("Computing...");

        SwingWorker<TrafficRound, Void> worker = new SwingWorker<TrafficRound, Void>() {
            @Override
            protected TrafficRound doInBackground() {
                return game.newRound(roundNumber);
            }

            @Override
            protected void done() {
                try {
                    currentRound = get();
                    repo.saveRound(currentRound);
                    updateRoundUI();
                } catch (Exception ex) {
                    showError("Error starting round: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void updateRoundUI() {
        lblFFTime.setText("—");
        lblEKTime.setText("—");

        // Pass initial empty path — network is fresh
        graphPanel.setNetwork(
                game.getNetwork().getCapacity(),
                game.getResidualCapacity(),
                game.getSelectedPath()
        );
        graphPanel.repaint();

        // ✅ Enable buttons AFTER data is set
        btnClearPath.setEnabled(true);
        btnApplyPath.setEnabled(true);
        btnFinish.setEnabled(true);

        // Show instruction
        showStatus(
                "Click node A to start building your path to T.",
                CLR_DRAW);
    }

    private void clearPath() {
        game.clearCurrentPath();
        refreshPathUI("Path cleared.");
    }

    private void applyPath() {
        if (!game.isPathComplete()) {
            showStatus("Select a full path from A to T first.", CLR_DANGER);
            return;
        }

        int bottleneck = game.getCurrentPathBottleneck();
        boolean ok = game.applyCurrentPath();

        if (!ok) {
            showStatus("Could not apply the selected path.", CLR_DANGER);
            return;
        }

        refreshPathUI("Applied path with bottleneck = " + bottleneck);

        if (game.isFinished()) {
            finishRound();
        }
    }

    private void finishRound() {

        lblFFTime.setText(game.getFfTimeNs() + " ns");
        lblEKTime.setText(game.getEkTimeNs() + " ns");

        btnClearPath.setEnabled(false);
        btnApplyPath.setEnabled(false);
        btnFinish.setEnabled(false);
        btnNextRound.setEnabled(true);

        currentRound.setPlayerName(playerName);
        currentRound.setPlayerFlow(game.getPlayerFlow());
        currentRound.setCorrect(game.isPlayerOptimal());
        currentRound.setPlayerPaths(game.getUsedPaths());

        repo.savePlayerResult(playerName, game.getPlayerFlow(), roundNumber, game.getUsedPaths());

        if (game.isPlayerOptimal()) {
            showStatus("Perfect! You reached the exact maximum flow: " + game.getPlayerFlow(), CLR_SUCCESS);
            showFinishDialog("Correct Result", JOptionPane.INFORMATION_MESSAGE);
        } else if (game.getPlayerFlow() >= game.getCorrectAnswer() - 2) {
            showStatus("Close! Your flow = " + game.getPlayerFlow()
                    + ", optimal = " + game.getCorrectAnswer(), CLR_DRAW);
            showFinishDialog("Round Result", JOptionPane.WARNING_MESSAGE);
        } else {
            showStatus("Not optimal. Your flow = " + game.getPlayerFlow()
                    + ", optimal = " + game.getCorrectAnswer(), CLR_DANGER);
            showFinishDialog("Wrong Result", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showFinishDialog(String title, int messageType) {
        JOptionPane.showMessageDialog(this,
                "<html><b>" + title + "</b><br><br>"
                        + "Your Built Flow: <b>" + game.getPlayerFlow() + "</b><br>"
                        + "Ford-Fulkerson Max Flow: <b>" + game.getCorrectAnswer() + "</b><br>"
                        + "Edmonds-Karp Max Flow: <b>" + game.getEkAnswer() + "</b><br><br>"
                        + "Ford-Fulkerson Time: " + game.getFfTimeNs() + " ns<br>"
                        + "Edmonds-Karp Time: " + game.getEkTimeNs() + " ns</html>",
                title,
                messageType);
    }

    private void refreshPathUI(String message) {
        List<Integer> path = game.getSelectedPath();

        if (path.isEmpty()) {
            lblPathInfo.setText("Selected Path: —");
        } else {
            StringBuilder sb = new StringBuilder("Selected Path: ");
            for (int i = 0; i < path.size(); i++) {
                if (i > 0) sb.append(" → ");
                sb.append(TrafficNetwork.NODE_NAMES[path.get(i)]);
            }
            if (game.isPathComplete()) {
                sb.append(" | Bottleneck = ").append(game.getCurrentPathBottleneck());
            }
            lblPathInfo.setText(sb.toString());
        }

        lblFlow.setText(String.valueOf(game.getPlayerFlow()));

        StringBuilder used = new StringBuilder();
        for (String s : game.getUsedPaths()) {
            used.append(s).append("\n");
        }
        txtUsedPaths.setText(used.toString());

        graphPanel.setNetwork(game.getNetwork().getCapacity(), game.getResidualCapacity(), game.getSelectedPath());
        graphPanel.repaint();

        showStatus(message, CLR_DRAW);
    }

    private class NetworkGraphPanel extends JPanel {

        private int[][] capacity;
        private int[][] residual;
        private List<Integer> selectedPath;

        private final double[][] NODE_POS = {
                {0.05, 0.50},
                {0.28, 0.18},
                {0.28, 0.50},
                {0.28, 0.82},
                {0.54, 0.28},
                {0.54, 0.72},
                {0.78, 0.18},
                {0.78, 0.62},
                {0.95, 0.40},
        };

        public NetworkGraphPanel() {
            setCursor(Cursor.getDefaultCursor());

            // ── Click listener ────────────────────────────────────
            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    // ✅ FIXED: only block if data not loaded
                    if (capacity == null) return;

                    int node = findClickedNode(e.getX(), e.getY());
                    if (node == -1) return;

                    boolean added = game.addNodeToPath(node);
                    if (added) {
                        refreshPathUI("Node "
                                + TrafficNetwork.NODE_NAMES[node] + " added.");
                    } else {
                        // Show why it was rejected
                        if (game.getSelectedPath().isEmpty()) {
                            showStatus(
                                    "Start from node A (source).",
                                    CLR_DANGER);
                        } else {
                            showStatus(
                                    "Node " + TrafficNetwork.NODE_NAMES[node]
                                            + " is not reachable from current path "
                                            + "or has no remaining capacity.",
                                    CLR_DANGER);
                        }
                    }
                }
            });

            // ── Hover listener — changes cursor over nodes ────────
            addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
                @Override
                public void mouseMoved(java.awt.event.MouseEvent e) {
                    if (capacity == null) return;
                    int node = findClickedNode(e.getX(), e.getY());
                    if (node != -1) {
                        setCursor(Cursor.getPredefinedCursor(
                                Cursor.HAND_CURSOR));
                    } else {
                        setCursor(Cursor.getDefaultCursor());
                    }
                }
            });
        }

        public void setNetwork(int[][] capacity, int[][] residual, List<Integer> selectedPath) {
            this.capacity = capacity;
            this.residual = residual;
            this.selectedPath = selectedPath;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (capacity == null) return;

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int r = 20;

            int[] px = new int[9];
            int[] py = new int[9];
            for (int i = 0; i < 9; i++) {
                px[i] = (int) (NODE_POS[i][0] * w);
                py[i] = (int) (NODE_POS[i][1] * h);
            }

            for (int[] edge : TrafficNetwork.EDGES) {
                int from = edge[0];
                int to = edge[1];
                int cap = capacity[from][to];
                int rem = residual[from][to];

                boolean onSelectedPath = isEdgeInSelectedPath(from, to);

                g2.setColor(onSelectedPath ? CLR_PATH : CLR_EDGE);
                g2.setStroke(new BasicStroke(onSelectedPath ? 3f : 2f,
                        BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                drawArrow(g2, px[from], py[from], px[to], py[to], r);

                double angle = Math.atan2(py[to] - py[from], px[to] - px[from]);
                int mx = (px[from] + px[to]) / 2;
                int my = (py[from] + py[to]) / 2;
                int offX = (int) (-Math.sin(angle) * 12);
                int offY = (int) (Math.cos(angle) * 12);

                g2.setFont(new Font("SansSerif", Font.BOLD, 10));
                g2.setColor(new Color(0x633806));
                g2.drawString(rem + "/" + cap, mx + offX - 8, my + offY + 4);
            }

            for (int i = 0; i < 9; i++) {
                Color fill;
                Color textColor;
                if (i == TrafficNetwork.SOURCE) {
                    fill = CLR_NODE_ST;
                    textColor = Color.WHITE;
                } else if (i == TrafficNetwork.SINK) {
                    fill = CLR_NODE_SK;
                    textColor = Color.WHITE;
                } else if (selectedPath != null && selectedPath.contains(i)) {
                    fill = CLR_PATH;
                    textColor = Color.WHITE;
                } else {
                    fill = CLR_NODE_BG;
                    textColor = CLR_TEXT;
                }

                g2.setColor(fill);
                g2.fillOval(px[i] - r, py[i] - r, r * 2, r * 2);
                g2.setColor(CLR_EDGE);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(px[i] - r, py[i] - r, r * 2, r * 2);

                g2.setColor(textColor);
                g2.setFont(new Font("SansSerif", Font.BOLD, 13));
                FontMetrics fm = g2.getFontMetrics();
                String label = TrafficNetwork.NODE_NAMES[i];
                g2.drawString(label,
                        px[i] - fm.stringWidth(label) / 2,
                        py[i] + fm.getAscent() / 2 - 1);
            }
        }

        private boolean isEdgeInSelectedPath(int from, int to) {
            if (selectedPath == null || selectedPath.size() < 2) return false;
            for (int i = 0; i < selectedPath.size() - 1; i++) {
                if (selectedPath.get(i) == from && selectedPath.get(i + 1) == to) {
                    return true;
                }
            }
            return false;
        }

        private int findClickedNode(int x, int y) {
            int w = getWidth();
            int h = getHeight();
            int drawR  = 20;       // visual radius (keep same)
            int clickR = 28;       // ✅ larger click radius — easier to hit

            for (int i = 0; i < 9; i++) {
                int px = (int) (NODE_POS[i][0] * w);
                int py = (int) (NODE_POS[i][1] * h);

                double dist = Math.sqrt(
                        Math.pow(x - px, 2) + Math.pow(y - py, 2));
                if (dist <= clickR) return i;  // ✅ use clickR not drawR
            }
            return -1;
        }

        private void drawArrow(Graphics2D g2, int x1, int y1, int x2, int y2, int r) {
            double angle = Math.atan2(y2 - y1, x2 - x1);
            int sx = (int) (x1 + r * Math.cos(angle));
            int sy = (int) (y1 + r * Math.sin(angle));
            int ex = (int) (x2 - r * Math.cos(angle));
            int ey = (int) (y2 - r * Math.sin(angle));

            g2.drawLine(sx, sy, ex, ey);

            int aw = 9;
            double a1 = angle + Math.toRadians(145);
            double a2 = angle - Math.toRadians(145);
            int[] xp = {ex,
                    (int) (ex + aw * Math.cos(a1)),
                    (int) (ex + aw * Math.cos(a2))};
            int[] yp = {ey,
                    (int) (ey + aw * Math.sin(a1)),
                    (int) (ey + aw * Math.sin(a2))};
            g2.fillPolygon(xp, yp, 3);
        }
    }

    private JPanel makeCard() {
        JPanel card = new JPanel();
        card.setBackground(CLR_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CLR_BORDER),
                new EmptyBorder(12, 14, 12, 14)
        ));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        return card;
    }

    private JLabel makeLabel(String text, Color color, int size) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, size));
        lbl.setForeground(color);
        return lbl;
    }

    private JButton makeButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CLR_BORDER),
                new EmptyBorder(6, 14, 6, 14)
        ));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void showStatus(String message, Color color) {
        lblStatus.setText(message);
        if (color.equals(CLR_SUCCESS)) {
            lblStatus.setBackground(new Color(0xE1F5EE));
        } else if (color.equals(CLR_DANGER)) {
            lblStatus.setBackground(new Color(0xFCEBEB));
        } else {
            lblStatus.setBackground(new Color(0xFAEEDA));
        }
        lblStatus.setForeground(color);
        statusPanel.setVisible(true);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg,
                "Validation Error", JOptionPane.WARNING_MESSAGE);
    }
}