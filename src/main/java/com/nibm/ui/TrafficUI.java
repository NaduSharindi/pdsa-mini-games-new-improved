package com.nibm.ui;

import com.nibm.db.TrafficRepository;
import com.nibm.games.TrafficGame;
import com.nibm.models.TrafficNetwork;
import com.nibm.models.TrafficRound;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class TrafficUI extends JDialog {

    // ── Colours ───────────────────────────────────────────────
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

    // ── State ─────────────────────────────────────────────────
    private final TrafficGame game         = new TrafficGame();
    private final TrafficRepository repo   = new TrafficRepository();
    private TrafficRound currentRound;
    private int roundNumber = 0;
    private String playerName;

    // ── UI Components ─────────────────────────────────────────
    private JLabel lblRound, lblFFTime, lblEKTime;
    private NetworkGraphPanel graphPanel;
    private JTextField txtAnswer;
    private JButton btnSubmit, btnNextRound;
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
        setSize(740, 820);
        setMinimumSize(new Dimension(700, 780));
        setLocationRelativeTo(parent);
        startNewRound();
        setVisible(true);
    }

    // ── Player name validation ────────────────────────────────
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
                showError("Name must contain letters and spaces only."); continue;
            }
            if (name.length() < 2) {
                showError("Name must be at least 2 characters."); continue;
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
        scroll.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
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

        body.add(buildPlayerCard());
        body.add(Box.createVerticalStrut(10));
        body.add(buildGraphCard());
        body.add(Box.createVerticalStrut(10));
        body.add(buildTimingCard());
        body.add(Box.createVerticalStrut(10));
        body.add(buildAnswerCard());
        body.add(Box.createVerticalStrut(10));
        body.add(buildStatusAndNext());
        body.add(Box.createVerticalStrut(16));
        return body;
    }

    // ── Player info card ──────────────────────────────────────
    private JPanel buildPlayerCard() {
        JPanel card = makeCard();
        card.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 0));
        card.add(makeLabel("Player:", CLR_MUTED, 12));
        JLabel lbl = new JLabel(playerName);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        lbl.setForeground(CLR_HEADER);
        card.add(lbl);
        return card;
    }

    // ── Network graph card ────────────────────────────────────
    private JPanel buildGraphCard() {
        JPanel card = makeCard();
        card.setLayout(new BorderLayout());

        JLabel title = makeLabel(
                "Traffic Network  —  Green = Source (A)  |  Red = Sink (T)"
                        + "  |  Edge labels = capacity (vehicles/min)",
                CLR_MUTED, 10);
        title.setBorder(new EmptyBorder(0, 0, 8, 0));

        graphPanel = new NetworkGraphPanel();
        graphPanel.setPreferredSize(new Dimension(680, 320));

        card.add(title,      BorderLayout.NORTH);
        card.add(graphPanel, BorderLayout.CENTER);
        return card;
    }

    // ── Timing card ───────────────────────────────────────────
    private JPanel buildTimingCard() {
        JPanel card = makeCard();
        card.setLayout(new GridLayout(3, 2, 8, 6));

        card.add(makeLabel("Algorithm",       CLR_MUTED, 11));
        card.add(makeLabel("Time Taken",      CLR_MUTED, 11));

        card.add(makeLabel("Ford-Fulkerson:", CLR_TEXT,  12));
        lblFFTime = new JLabel("—");
        lblFFTime.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblFFTime.setForeground(new Color(0x185FA5));
        card.add(lblFFTime);

        card.add(makeLabel("Edmonds-Karp:",   CLR_TEXT,  12));
        lblEKTime = new JLabel("—");
        lblEKTime.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblEKTime.setForeground(CLR_DRAW);
        card.add(lblEKTime);

        return card;
    }

    // ── Answer input card — player types the max flow ─────────
    private JPanel buildAnswerCard() {
        JPanel card = makeCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel question = new JLabel(
                "What is the maximum flow from A to T? (vehicles/min)");
        question.setFont(new Font("SansSerif", Font.BOLD, 13));
        question.setForeground(CLR_TEXT);
        question.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel hint = new JLabel(
                "Study the network graph above, then enter your answer:");
        hint.setFont(new Font("SansSerif", Font.PLAIN, 11));
        hint.setForeground(CLR_MUTED);
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Input row
        JPanel inputRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        inputRow.setOpaque(false);
        inputRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblUnit = makeLabel("Max Flow =", CLR_TEXT, 13);

        txtAnswer = new JTextField(8);
        txtAnswer.setFont(new Font("SansSerif", Font.BOLD, 15));
        txtAnswer.setHorizontalAlignment(JTextField.CENTER);
        txtAnswer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CLR_BORDER),
                new EmptyBorder(4, 8, 4, 8)
        ));
        // Submit on Enter key
        txtAnswer.addActionListener(e -> submitAnswer());

        JLabel lblUnit2 = makeLabel("vehicles/min", CLR_MUTED, 11);

        btnSubmit = makeButton("Submit Answer", CLR_HEADER, Color.WHITE);
        btnSubmit.addActionListener(e -> submitAnswer());

        inputRow.add(lblUnit);
        inputRow.add(txtAnswer);
        inputRow.add(lblUnit2);
        inputRow.add(Box.createHorizontalStrut(8));
        inputRow.add(btnSubmit);

        card.add(question);
        card.add(Box.createVerticalStrut(4));
        card.add(hint);
        card.add(Box.createVerticalStrut(10));
        card.add(inputRow);
        return card;
    }

    // ── Status + Next round ───────────────────────────────────
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

    // ── Start new round ───────────────────────────────────────
    private void startNewRound() {
        roundNumber++;
        lblRound.setText("Round " + roundNumber);

        txtAnswer.setText("");
        txtAnswer.setEnabled(false);
        btnSubmit.setEnabled(false);
        btnNextRound.setEnabled(false);
        statusPanel.setVisible(false);
        lblFFTime.setText("Computing...");
        lblEKTime.setText("Computing...");

        SwingWorker<TrafficRound, Void> worker =
                new SwingWorker<TrafficRound, Void>() {
                    protected TrafficRound doInBackground() {
                        return game.newRound(roundNumber);
                    }
                    protected void done() {
                        try {
                            currentRound = get();
                            repo.saveRound(currentRound);
                            updateRoundUI();
                        } catch (Exception ex) {
                            showError("Error starting round: "
                                    + ex.getMessage());
                        }
                    }
                };
        worker.execute();
    }

    private void updateRoundUI() {
        lblFFTime.setText(game.getFfTimeMs() + " ms");
        lblEKTime.setText(game.getEkTimeMs() + " ms");

        graphPanel.setNetwork(game.getNetwork().getCapacity());
        graphPanel.repaint();

        txtAnswer.setEnabled(true);
        btnSubmit.setEnabled(true);
        txtAnswer.requestFocus();
    }

    // ── Submit and validate answer ────────────────────────────
    private void submitAnswer() {
        String input = txtAnswer.getText().trim();

        // ── Input Validations ──────────────────────────────────

        // Validation 1: not empty
        if (input.isEmpty()) {
            highlightFieldError(txtAnswer,
                    "Please enter the maximum flow before submitting.");
            return;
        }

        // Validation 2: digits only (no negatives, no decimals)
        if (!input.matches("\\d+")) {
            highlightFieldError(txtAnswer,
                    "Maximum flow must be a positive whole number.\n"
                            + "No letters, decimals, or negative signs allowed.");
            return;
        }

        // Validation 3: parse safely
        int answer;
        try {
            answer = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            highlightFieldError(txtAnswer,
                    "Number is too large. Please check your answer.");
            return;
        }

        // Validation 4: must be positive
        if (answer <= 0) {
            highlightFieldError(txtAnswer,
                    "Maximum flow must be greater than zero.");
            return;
        }

        // Validation 5: sanity upper bound
        // Max possible flow with 13 edges each capacity 15 = 195
        if (answer > 195) {
            highlightFieldError(txtAnswer,
                    "Answer seems too large for this network.\n"
                            + "Maximum possible flow with capacity 15 per edge is 195.");
            return;
        }

        // ── All validations passed ─────────────────────────────
        txtAnswer.setEnabled(false);
        btnSubmit.setEnabled(false);

        String result = game.classifyAnswer(answer);

        currentRound.setPlayerName(playerName);
        currentRound.setPlayerAnswer(answer);

        switch (result) {
            case "WIN":
                currentRound.setCorrect(true);
                showStatus(
                        "Correct! Max flow = " + answer + " vehicles/min",
                        CLR_SUCCESS);
                repo.savePlayerResult(playerName, answer, roundNumber);
                showWinDialog(answer);
                break;

            case "DRAW":
                currentRound.setCorrect(false);
                showStatus(
                        "Close! Correct max flow = "
                                + game.getCorrectAnswer()
                                + ", you entered " + answer,
                        CLR_DRAW);
                showDrawDialog(answer, game.getCorrectAnswer());
                break;

            case "LOSE":
            default:
                currentRound.setCorrect(false);
                showStatus(
                        "Wrong! Correct max flow = "
                                + game.getCorrectAnswer()
                                + " vehicles/min",
                        CLR_DANGER);
                showLoseDialog(game.getCorrectAnswer(), answer);
                break;
        }

        btnNextRound.setEnabled(true);
    }

    // ── Result dialogs ────────────────────────────────────────
    private void showWinDialog(int answer) {
        JOptionPane.showMessageDialog(this,
                "<html><b>Correct!</b><br><br>"
                        + "Maximum Flow: <b>" + answer + " vehicles/min</b><br>"
                        + "Ford-Fulkerson time: " + game.getFfTimeMs() + " ms<br>"
                        + "Edmonds-Karp time:   " + game.getEkTimeMs() + " ms<br><br>"
                        + "Your answer has been saved to the database.</html>",
                "You Win!", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showDrawDialog(int entered, int correct) {
        JOptionPane.showMessageDialog(this,
                "<html><b>Almost correct!</b><br><br>"
                        + "You entered: <b>" + entered + "</b><br>"
                        + "Correct max flow: <b>" + correct + "</b><br><br>"
                        + "You were within ±2 — very close!</html>",
                "Draw", JOptionPane.WARNING_MESSAGE);
    }

    private void showLoseDialog(int correct, int entered) {
        JOptionPane.showMessageDialog(this,
                "<html><b>Wrong answer!</b><br><br>"
                        + "You entered: <b>" + entered + "</b><br>"
                        + "Correct max flow: <b>" + correct + " vehicles/min</b><br><br>"
                        + "Tip: trace each path from A to T and find the bottleneck.</html>",
                "You Lose", JOptionPane.ERROR_MESSAGE);
    }

    // ── Network graph painting panel ──────────────────────────
    private class NetworkGraphPanel extends JPanel {

        private int[][] capacity;

        // Fixed node positions as fractions of panel [x, y]
        // Layout mirrors the CW spec edge list visually
        private final double[][] NODE_POS = {
                {0.05, 0.50},  // A (source)
                {0.28, 0.18},  // B
                {0.28, 0.50},  // C
                {0.28, 0.82},  // D
                {0.54, 0.28},  // E
                {0.54, 0.72},  // F
                {0.78, 0.18},  // G
                {0.78, 0.62},  // H
                {0.95, 0.40},  // T (sink)
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
            int r = 20; // node radius

            // Compute pixel positions
            int[] px = new int[9];
            int[] py = new int[9];
            for (int i = 0; i < 9; i++) {
                px[i] = (int)(NODE_POS[i][0] * w);
                py[i] = (int)(NODE_POS[i][1] * h);
            }

            // ── Draw edges ────────────────────────────────────
            g2.setStroke(new BasicStroke(2f,
                    BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            for (int[] edge : TrafficNetwork.EDGES) {
                int from = edge[0];
                int to   = edge[1];
                int cap  = capacity[from][to];

                g2.setColor(CLR_EDGE);
                drawArrow(g2, px[from], py[from],
                        px[to],   py[to], r);

                // Capacity label — offset perpendicular to edge
                // to avoid overlapping the line
                double angle = Math.atan2(
                        py[to] - py[from], px[to] - px[from]);
                int mx = (px[from] + px[to]) / 2;
                int my = (py[from] + py[to]) / 2;

                // Perpendicular offset so label is beside the line
                int offX = (int)(-Math.sin(angle) * 12);
                int offY = (int)( Math.cos(angle) * 12);

                g2.setFont(new Font("SansSerif", Font.BOLD, 11));
                g2.setColor(new Color(0x633806));
                g2.drawString(String.valueOf(cap),
                        mx + offX - 4, my + offY + 4);
            }

            // ── Draw nodes ────────────────────────────────────
            for (int i = 0; i < 9; i++) {
                Color fill;
                Color textColor;
                if (i == TrafficNetwork.SOURCE) {
                    fill      = CLR_NODE_ST;
                    textColor = Color.WHITE;
                } else if (i == TrafficNetwork.SINK) {
                    fill      = CLR_NODE_SK;
                    textColor = Color.WHITE;
                } else {
                    fill      = CLR_NODE_BG;
                    textColor = CLR_TEXT;
                }

                g2.setColor(fill);
                g2.fillOval(px[i] - r, py[i] - r, r * 2, r * 2);
                g2.setColor(CLR_EDGE);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(px[i] - r, py[i] - r, r * 2, r * 2);

                // Node label
                g2.setColor(textColor);
                g2.setFont(new Font("SansSerif", Font.BOLD, 13));
                FontMetrics fm = g2.getFontMetrics();
                String label = TrafficNetwork.NODE_NAMES[i];
                g2.drawString(label,
                        px[i] - fm.stringWidth(label) / 2,
                        py[i] + fm.getAscent() / 2 - 1);
            }
        }

        // Draw directed arrow — starts/ends at node edge (radius offset)
        private void drawArrow(Graphics2D g2,
                               int x1, int y1,
                               int x2, int y2, int r) {
            double angle = Math.atan2(y2 - y1, x2 - x1);
            int sx = (int)(x1 + r * Math.cos(angle));
            int sy = (int)(y1 + r * Math.sin(angle));
            int ex = (int)(x2 - r * Math.cos(angle));
            int ey = (int)(y2 - r * Math.sin(angle));

            g2.drawLine(sx, sy, ex, ey);

            // Arrowhead
            int aw = 9;
            double a1 = angle + Math.toRadians(145);
            double a2 = angle - Math.toRadians(145);
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

    private void highlightFieldError(JTextField field, String message) {
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CLR_DANGER, 2),
                new EmptyBorder(4, 8, 4, 8)
        ));
        showError(message);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CLR_BORDER),
                new EmptyBorder(4, 8, 4, 8)
        ));
        field.requestFocus();
        field.selectAll();
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg,
                "Validation Error", JOptionPane.WARNING_MESSAGE);
    }
}