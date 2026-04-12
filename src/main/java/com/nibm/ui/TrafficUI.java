package com.nibm.ui;

import com.nibm.db.TrafficRepository;
import com.nibm.games.TrafficGame;
import com.nibm.models.TrafficNetwork;
import com.nibm.models.TrafficRound;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.QuadCurve2D;

public class TrafficUI extends JDialog {

    // ── Colours ───────────────────────────────────────────────
    private static final Color CLR_HEADER  = new Color(0xB36A00);
    private static final Color CLR_BG      = new Color(0xF1EFE8);
    private static final Color CLR_CARD    = new Color(0xFFFFFF);
    private static final Color CLR_BORDER  = new Color(0xD3D1C7);
    private static final Color CLR_TEXT    = new Color(0x2C2C2A);
    private static final Color CLR_MUTED   = new Color(0x888780);
    private static final Color CLR_SUCCESS = new Color(0x1D9E75);
    private static final Color CLR_DANGER  = new Color(0xE24B4A);
    private static final Color CLR_NODE    = new Color(0xFAEEDA);
    private static final Color CLR_NODE_ST = new Color(0x1D9E75); // source
    private static final Color CLR_NODE_SK = new Color(0xE24B4A); // sink
    private static final Color CLR_EDGE    = new Color(0xB36A00);

    // ── State ─────────────────────────────────────────────────
    private final TrafficGame game   = new TrafficGame();
    private final TrafficRepository repo = new TrafficRepository();
    private TrafficRound currentRound;
    private int roundNumber = 0;
    private String playerName;
    private int[] currentChoices;

    // ── UI Components ─────────────────────────────────────────
    private JLabel lblRound, lblFFTime, lblEKTime;
    private NetworkGraphPanel graphPanel;
    private JButton[] choiceButtons = new JButton[3];
    private JButton btnNextRound;
    private JPanel statusPanel;
    private JLabel lblStatus;

    // ── Constructor ───────────────────────────────────────────
    public TrafficUI(JFrame parent) {
        super(parent, "Game 3 — Traffic Simulation", true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(false);

        playerName = askPlayerName(parent);
        if (playerName == null) { dispose(); return; }

        buildUI();
        setSize(720, 780);
        setLocationRelativeTo(parent);
        startNewRound();
        setVisible(true);
    }

    // ── Player name with validation ───────────────────────────
    private String askPlayerName(JFrame parent) {
        while (true) {
            String name = JOptionPane.showInputDialog(
                    parent, "Enter your name:", "Player Name",
                    JOptionPane.PLAIN_MESSAGE);
            if (name == null) return null;
            name = name.trim();
            if (name.isEmpty()) {
                showError("Name cannot be empty."); continue;
            }
            if (!name.matches("[a-zA-Z ]+")) {
                showError("Name must contain letters only."); continue;
            }
            if (name.length() > 50) {
                showError("Name too long. Max 50 characters."); continue;
            }
            return name;
        }
    }

    // ── Build UI ──────────────────────────────────────────────
    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(CLR_BG);
        root.add(buildHeader(), BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(buildBody());
        scroll.setBorder(null);
        scroll.getViewport().setBackground(CLR_BG);
        scroll.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        root.add(scroll, BorderLayout.CENTER);
        root.add(buildFooter(), BorderLayout.SOUTH);
        setContentPane(root);
    }

    private JPanel buildHeader() {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(CLR_HEADER);
        h.setBorder(new EmptyBorder(16, 24, 14, 24));

        JLabel title = new JLabel("Traffic Simulation — Max Flow A → T");
        title.setFont(new Font("SansSerif", Font.BOLD, 15));
        title.setForeground(Color.WHITE);

        lblRound = new JLabel("Round 0");
        lblRound.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblRound.setForeground(new Color(0xFFD799));
        h.add(title,    BorderLayout.CENTER);
        h.add(lblRound, BorderLayout.EAST);
        return h;
    }

    private JPanel buildBody() {
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(CLR_BG);
        body.setBorder(new EmptyBorder(14, 16, 8, 16));

        body.add(buildGraphCard());
        body.add(Box.createVerticalStrut(10));
        body.add(buildTimingCard());
        body.add(Box.createVerticalStrut(10));
        body.add(buildChoiceCard());
        body.add(Box.createVerticalStrut(10));
        body.add(buildStatusAndNext());
        return body;
    }

    // ── Network graph card ────────────────────────────────────
    private JPanel buildGraphCard() {
        JPanel card = makeCard();
        card.setLayout(new BorderLayout());

        JLabel title = makeLabel(
                "Traffic Network (A=Source, T=Sink) — Edge labels = capacity (vehicles/min)",
                CLR_TEXT, 11);
        title.setBorder(new EmptyBorder(0, 0, 8, 0));

        graphPanel = new NetworkGraphPanel();
        graphPanel.setPreferredSize(new Dimension(660, 340));

        card.add(title,      BorderLayout.NORTH);
        card.add(graphPanel, BorderLayout.CENTER);
        return card;
    }

    // ── Timing card ───────────────────────────────────────────
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
        lblEKTime.setForeground(new Color(0xBA7517));
        card.add(lblEKTime);
        return card;
    }

    // ── 3-choice answer card ──────────────────────────────────
    private JPanel buildChoiceCard() {
        JPanel card = makeCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel question = new JLabel(
                "What is the maximum flow from A to T (vehicles/min)?");
        question.setFont(new Font("SansSerif", Font.BOLD, 13));
        question.setForeground(CLR_TEXT);
        question.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel hint = new JLabel(
                "Select the correct maximum flow value:");
        hint.setFont(new Font("SansSerif", Font.PLAIN, 11));
        hint.setForeground(CLR_MUTED);
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        for (int i = 0; i < 3; i++) {
            final int idx = i;
            choiceButtons[i] = new JButton("—");
            choiceButtons[i].setFont(new Font("SansSerif", Font.BOLD, 15));
            choiceButtons[i].setForeground(CLR_HEADER);
            choiceButtons[i].setBackground(new Color(0xFAEEDA));
            choiceButtons[i].setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(CLR_HEADER, 1),
                    new EmptyBorder(12, 28, 12, 28)
            ));
            choiceButtons[i].setFocusPainted(false);
            choiceButtons[i].setCursor(
                    Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            choiceButtons[i].setEnabled(false);
            choiceButtons[i].addActionListener(
                    e -> submitChoice(currentChoices[idx]));
            btnRow.add(choiceButtons[i]);
        }

        card.add(question);
        card.add(Box.createVerticalStrut(4));
        card.add(hint);
        card.add(Box.createVerticalStrut(12));
        card.add(btnRow);
        return card;
    }

    // ── Status + Next round ───────────────────────────────────
    private JPanel buildStatusAndNext() {
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setOpaque(false);

        statusPanel = new JPanel(new BorderLayout());
        statusPanel.setOpaque(false);
        statusPanel.setVisible(false);
        statusPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        statusPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        statusPanel.setPreferredSize(new Dimension(600, 50));

        lblStatus = new JLabel("", SwingConstants.CENTER);
        lblStatus.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblStatus.setBorder(new EmptyBorder(10, 16, 10, 16));
        lblStatus.setOpaque(true);
        statusPanel.add(lblStatus, BorderLayout.CENTER);

        btnNextRound = makeButton("Next Round →", CLR_BG, CLR_MUTED);
        btnNextRound.setEnabled(false);
        btnNextRound.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnNextRound.addActionListener(e -> startNewRound());

        wrapper.add(statusPanel);
        wrapper.add(Box.createVerticalStrut(8));
        wrapper.add(btnNextRound);
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

    // ── Start new round ───────────────────────────────────────
    private void startNewRound() {
        roundNumber++;
        lblRound.setText("Round " + roundNumber);

        for (JButton btn : choiceButtons) {
            btn.setEnabled(false);
            btn.setText("—");
        }
        btnNextRound.setEnabled(false);
        statusPanel.setVisible(false);

        SwingWorker<TrafficRound, Void> worker =
                new SwingWorker<TrafficRound, Void>() {
                    protected TrafficRound doInBackground() {
                        return game.newRound(roundNumber);
                    }
                    protected void done() {
                        try {
                            currentRound = get();
                            repo.saveRound(currentRound);
                            currentChoices = game.generateChoices();
                            updateRoundUI();
                        } catch (Exception ex) {
                            showError("Error starting round: " + ex.getMessage());
                        }
                    }
                };
        worker.execute();
    }

    private void updateRoundUI() {
        lblFFTime.setText(game.getFfTimeMs() + " ms");
        lblEKTime.setText(game.getEkTimeMs() + " ms");

        for (int i = 0; i < 3; i++) {
            choiceButtons[i].setText(String.valueOf(currentChoices[i]));
            choiceButtons[i].setEnabled(true);
        }

        graphPanel.setNetwork(game.getNetwork().getCapacity());
        graphPanel.repaint();
    }

    // ── Submit choice ─────────────────────────────────────────
    private void submitChoice(int chosen) {
        for (JButton btn : choiceButtons) btn.setEnabled(false);

        boolean correct = game.validateAnswer(chosen);
        currentRound.setPlayerName(playerName);
        currentRound.setPlayerAnswer(chosen);
        currentRound.setCorrect(correct);

        if (correct) {
            showStatus("Correct! Max flow = " + chosen
                    + " vehicles/min", CLR_SUCCESS);
            repo.savePlayerResult(playerName, chosen, roundNumber);
            showWinDialog(chosen);
        } else {
            showStatus("Wrong! Correct max flow = "
                    + game.getCorrectAnswer(), CLR_DANGER);
            showLoseDialog(game.getCorrectAnswer(), chosen);
        }
        btnNextRound.setEnabled(true);
    }

    // ── Result dialogs ────────────────────────────────────────
    private void showWinDialog(int answer) {
        JOptionPane.showMessageDialog(this,
                "<html><b>Correct!</b><br>" +
                        "Maximum Flow: <b>" + answer + " vehicles/min</b><br>" +
                        "Ford-Fulkerson time: " + game.getFfTimeMs() + " ms<br>" +
                        "Edmonds-Karp time: "   + game.getEkTimeMs() + " ms</html>",
                "You Win!", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showLoseDialog(int correct, int chosen) {
        JOptionPane.showMessageDialog(this,
                "<html><b>Wrong answer!</b><br>" +
                        "You chose: <b>" + chosen + "</b><br>" +
                        "Correct max flow: <b>" + correct + "</b></html>",
                "Incorrect", JOptionPane.ERROR_MESSAGE);
    }

    // ── Network graph painting panel ──────────────────────────
    private class NetworkGraphPanel extends JPanel {

        private int[][] capacity;

        // Fixed node positions [x%, y%] as percentage of panel size
        // Layout: A(left) -> B,C,D(mid-left) ->
        //         E,F(mid) -> G,H(mid-right) -> T(right)
        private final double[][] NODE_POS = {
                {0.05, 0.50},  // A
                {0.25, 0.20},  // B
                {0.25, 0.50},  // C
                {0.25, 0.80},  // D
                {0.50, 0.30},  // E
                {0.50, 0.70},  // F
                {0.75, 0.20},  // G
                {0.75, 0.60},  // H
                {0.95, 0.40},  // T
        };

        public void setNetwork(int[][] capacity) {
            this.capacity = capacity;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (capacity == null) return;

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int r = 22; // node radius

            // Compute pixel positions
            int[] px = new int[9];
            int[] py = new int[9];
            for (int i = 0; i < 9; i++) {
                px[i] = (int)(NODE_POS[i][0] * w);
                py[i] = (int)(NODE_POS[i][1] * h);
            }

            // Draw edges with capacity labels
            g2.setStroke(new BasicStroke(2f));
            for (int[] edge : TrafficNetwork.EDGES) {
                int from = edge[0];
                int to   = edge[1];
                int cap  = capacity[from][to];

                g2.setColor(CLR_EDGE);
                drawArrow(g2, px[from], py[from], px[to], py[to], r);

                // Capacity label at midpoint
                int mx = (px[from] + px[to]) / 2;
                int my = (py[from] + py[to]) / 2 - 6;
                g2.setColor(new Color(0x633806));
                g2.setFont(new Font("SansSerif", Font.BOLD, 11));
                g2.drawString(String.valueOf(cap), mx, my);
            }

            // Draw nodes
            for (int i = 0; i < 9; i++) {
                Color fill;
                if (i == TrafficNetwork.SOURCE)      fill = CLR_NODE_ST;
                else if (i == TrafficNetwork.SINK)   fill = CLR_NODE_SK;
                else                                  fill = CLR_NODE;

                g2.setColor(fill);
                g2.fillOval(px[i] - r, py[i] - r, r * 2, r * 2);
                g2.setColor(CLR_EDGE);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(px[i] - r, py[i] - r, r * 2, r * 2);

                // Node label
                String label = TrafficNetwork.NODE_NAMES[i];
                g2.setColor(
                        (i == TrafficNetwork.SOURCE || i == TrafficNetwork.SINK)
                                ? Color.WHITE : CLR_TEXT);
                g2.setFont(new Font("SansSerif", Font.BOLD, 13));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(label,
                        px[i] - fm.stringWidth(label) / 2,
                        py[i] + fm.getAscent() / 2 - 1);
            }
        }

        // Draw directed arrow from (x1,y1) to (x2,y2)
        // with node radius offset so arrow starts/ends at node edge
        private void drawArrow(Graphics2D g2,
                               int x1, int y1, int x2, int y2, int r) {
            double angle = Math.atan2(y2 - y1, x2 - x1);
            int sx = (int)(x1 + r * Math.cos(angle));
            int sy = (int)(y1 + r * Math.sin(angle));
            int ex = (int)(x2 - r * Math.cos(angle));
            int ey = (int)(y2 - r * Math.sin(angle));

            g2.drawLine(sx, sy, ex, ey);

            // Arrowhead
            int aw = 8;
            double a1 = angle + Math.toRadians(150);
            double a2 = angle - Math.toRadians(150);
            int[] xp = {ex,
                    (int)(ex + aw * Math.cos(a1)),
                    (int)(ex + aw * Math.cos(a2))};
            int[] yp = {ey,
                    (int)(ey + aw * Math.sin(a1)),
                    (int)(ey + aw * Math.sin(a2))};
            g2.fillPolygon(xp, yp, 3);
        }
    }

    // ── UI helpers ────────────────────────────────────────────
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
        lblStatus.setBackground(color.equals(CLR_SUCCESS)
                ? new Color(0xE1F5EE) : new Color(0xFCEBEB));
        lblStatus.setForeground(color);
        statusPanel.setVisible(true);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg,
                "Error", JOptionPane.WARNING_MESSAGE);
    }
}