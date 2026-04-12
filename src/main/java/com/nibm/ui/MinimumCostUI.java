package com.nibm.ui;

import com.nibm.db.GameResultRepository;
import com.nibm.games.MinimumCostGame;
import com.nibm.models.GameRound;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MinimumCostUI extends JDialog {

    // ── Colours ───────────────────────────────────────────────
    private static final Color CLR_HEADER  = new Color(0x1D9E75);
    private static final Color CLR_BG      = new Color(0xF1EFE8);
    private static final Color CLR_CARD    = new Color(0xFFFFFF);
    private static final Color CLR_BORDER  = new Color(0xD3D1C7);
    private static final Color CLR_TEXT    = new Color(0x2C2C2A);
    private static final Color CLR_MUTED   = new Color(0x888780);
    private static final Color CLR_SUCCESS = new Color(0x1D9E75);
    private static final Color CLR_DANGER  = new Color(0xE24B4A);
    private static final Color CLR_AMBER   = new Color(0xBA7517);

    // ── State ─────────────────────────────────────────────────
    private final MinimumCostGame game   = new MinimumCostGame();
    private final GameResultRepository repo = new GameResultRepository();
    private GameRound currentRound;
    private int roundNumber = 0;
    private String playerName;

    // ── UI components ─────────────────────────────────────────
    private JLabel lblRoundNum, lblN, lblHungTime, lblGreedyTime;
    private JLabel lblHintH, lblHintG;
    private JTextField txtAnswer;
    private JButton btnSubmit, btnNextRound;
    private JPanel statusPanel;
    private JLabel lblStatus;

    // ── Constructor ───────────────────────────────────────────
    public MinimumCostUI(JFrame parent) {
        super(parent, "Game 1 — Minimum Cost", true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(false);

        playerName = askPlayerName(parent);
        if (playerName == null) {      // user cancelled name dialog
            dispose();
            return;
        }

        buildUI();
        pack();
        setMinimumSize(new Dimension(480, 620));
        setSize(480, 620);
        setLocationRelativeTo(parent);
        startNewRound();
        setVisible(true);
    }

    // ── Player name dialog with validation ───────────────────
    private String askPlayerName(JFrame parent) {
        while (true) {
            String name = JOptionPane.showInputDialog(
                    parent,
                    "Enter your name to begin:",
                    "Player Name",
                    JOptionPane.PLAIN_MESSAGE
            );

            if (name == null) return null;   // cancelled

            // Validation 1: not empty
            name = name.trim();
            if (name.isEmpty()) {
                showError("Name cannot be empty. Please enter your name.");
                continue;
            }

            // Validation 2: only letters and spaces
            if (!name.matches("[a-zA-Z ]+")) {
                showError("Name can only contain letters and spaces.");
                continue;
            }

            // Validation 3: length
            if (name.length() > 50) {
                showError("Name is too long. Maximum 50 characters.");
                continue;
            }

            return name;
        }
    }

    // ── Build the full UI ─────────────────────────────────────
    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(CLR_BG);

        root.add(buildHeader(), BorderLayout.NORTH);

        // Wrap body in scroll pane so nothing gets cut off
        JScrollPane scroll = new JScrollPane(buildBody());
        scroll.setBorder(null);
        scroll.setBackground(CLR_BG);
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

        JLabel title = new JLabel("Minimum Cost Assignment");
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setForeground(Color.WHITE);

        lblRoundNum = new JLabel("Round 0");
        lblRoundNum.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblRoundNum.setForeground(new Color(0x9FE1CB));

        h.add(title,      BorderLayout.CENTER);
        h.add(lblRoundNum, BorderLayout.EAST);
        return h;
    }

    private JPanel buildBody() {
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(CLR_BG);
        body.setBorder(new EmptyBorder(16, 20, 8, 20));

        body.add(buildInfoCard());
        body.add(Box.createVerticalStrut(12));
        body.add(buildTimingCard());
        body.add(Box.createVerticalStrut(12));
        body.add(buildAnswerCard());
        body.add(Box.createVerticalStrut(12));
        body.add(buildStatusPanel());
        body.add(Box.createVerticalStrut(8));

        return body;
    }

    // ── Info card: N and player ───────────────────────────────
    private JPanel buildInfoCard() {
        JPanel card = makeCard();
        card.setLayout(new GridLayout(2, 2, 8, 6));

        card.add(makeLabel("Player:", CLR_MUTED, 12));
        card.add(makeLabel(playerName, CLR_TEXT, 13));
        card.add(makeLabel("Number of Tasks (N):", CLR_MUTED, 12));

        lblN = new JLabel("—");
        lblN.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblN.setForeground(new Color(0x1D9E75));
        card.add(lblN);

        return card;
    }

    // ── Timing card ───────────────────────────────────────────
    private JPanel buildTimingCard() {
        JPanel card = makeCard();
        card.setLayout(new GridLayout(3, 2, 8, 6));

        card.add(makeLabel("Algorithm", CLR_MUTED, 11));
        card.add(makeLabel("Time Taken", CLR_MUTED, 11));

        card.add(makeLabel("Hungarian:", CLR_TEXT, 12));
        lblHungTime = new JLabel("—");
        lblHungTime.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblHungTime.setForeground(new Color(0x185FA5));
        card.add(lblHungTime);

        card.add(makeLabel("Greedy:", CLR_TEXT, 12));
        lblGreedyTime = new JLabel("—");
        lblGreedyTime.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblGreedyTime.setForeground(new Color(0xBA7517));
        card.add(lblGreedyTime);

        return card;
    }

    // ── Answer input card ─────────────────────────────────────
    private JPanel buildAnswerCard() {
        JPanel card = makeCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setMinimumSize(new Dimension(100, 160));
        card.setPreferredSize(new Dimension(440, 160));

        JLabel question = new JLabel(
                "What is the minimum cost to assign all tasks?");
        question.setFont(new Font("SansSerif", Font.BOLD, 13));
        question.setForeground(CLR_TEXT);
        question.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel hint = new JLabel(
                "Enter the optimal total cost in dollars ($)");
        hint.setFont(new Font("SansSerif", Font.PLAIN, 11));
        hint.setForeground(CLR_MUTED);
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtAnswer = new JTextField();
        txtAnswer.setFont(new Font("SansSerif", Font.PLAIN, 14));
        txtAnswer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        txtAnswer.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtAnswer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CLR_BORDER),
                new EmptyBorder(4, 8, 4, 8)
        ));

        // Allow submit on Enter key
        txtAnswer.addActionListener(e -> submitAnswer());

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnSubmit = makeButton("Submit Answer", new Color(0x1D9E75), Color.WHITE);
        btnSubmit.addActionListener(e -> submitAnswer());

        btnNextRound = makeButton("Next Round →", CLR_BG, CLR_TEXT);
        btnNextRound.setEnabled(false);
        btnNextRound.addActionListener(e -> startNewRound());

        btnRow.add(btnSubmit);
        btnRow.add(Box.createHorizontalStrut(8));
        btnRow.add(btnNextRound);

        card.add(question);
        card.add(Box.createVerticalStrut(4));
        card.add(hint);
        card.add(Box.createVerticalStrut(10));
        card.add(txtAnswer);
        card.add(Box.createVerticalStrut(10));
        card.add(btnRow);

        return card;
    }

    // ── Status panel ─────────────────────────────────────────
    private JPanel buildStatusPanel() {
        statusPanel = new JPanel(new BorderLayout());
        statusPanel.setOpaque(false);
        statusPanel.setVisible(false);
        statusPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        statusPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        statusPanel.setMinimumSize(new Dimension(100, 50));   // add this
        statusPanel.setPreferredSize(new Dimension(440, 50)); // add this

        lblStatus = new JLabel("", SwingConstants.CENTER);
        lblStatus.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblStatus.setBorder(new EmptyBorder(10, 16, 10, 16));
        lblStatus.setOpaque(true);

        statusPanel.add(lblStatus, BorderLayout.CENTER);
        return statusPanel;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT));
        footer.setBackground(CLR_BG);
        footer.setBorder(new EmptyBorder(0, 20, 12, 20));

        JButton btnBack = makeButton("← Back to Menu", CLR_BG, CLR_MUTED);
        btnBack.addActionListener(e -> dispose());
        footer.add(btnBack);

        return footer;
    }

    // ── Game logic ────────────────────────────────────────────
    private void startNewRound() {
        roundNumber++;
        lblRoundNum.setText("Round " + roundNumber);

        // Run game on background thread — keeps UI responsive
        SwingWorker<GameRound, Void> worker = new SwingWorker<GameRound, Void>() {
            protected GameRound doInBackground() {
                return game.newRound(roundNumber);
            }
            protected void done() {
                try {
                    currentRound = get();
                    repo.saveRound(currentRound);
                    updateRoundUI();
                } catch (Exception e) {
                    showError("Error starting round: " + e.getMessage());
                }
            }
        };

        // Disable buttons while computing
        btnSubmit.setEnabled(false);
        btnNextRound.setEnabled(false);
        txtAnswer.setText("");
        txtAnswer.setEnabled(false);
        statusPanel.setVisible(false);
        lblN.setText("Computing...");

        worker.execute();
    }

    private void updateRoundUI() {
        lblN.setText(String.valueOf(game.getN()));
        lblHungTime.setText(game.getHungarianTimeMs() + " ms");
        lblGreedyTime.setText(game.getGreedyTimeMs() + " ms");
        txtAnswer.setEnabled(true);
        btnSubmit.setEnabled(true);
        txtAnswer.requestFocus();
    }

    // ── Submit & validate answer ──────────────────────────────
    private void submitAnswer() {
        String input = txtAnswer.getText().trim();

        // Validation 1: not empty
        if (input.isEmpty()) {
            highlightError(txtAnswer, "Please enter an answer before submitting.");
            return;
        }

        // Validation 2: numeric only
        if (!input.matches("\\d+")) {
            highlightError(txtAnswer,
                    "Answer must be a positive whole number (digits only).");
            return;
        }

        // Validation 3: reasonable range
        int answer;
        try {
            answer = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            highlightError(txtAnswer, "Number is too large. Please check your answer.");
            return;
        }

        if (answer <= 0) {
            highlightError(txtAnswer, "Answer must be greater than zero.");
            return;
        }

        // Check answer
        boolean correct = game.validateAnswer(answer);

        currentRound.setPlayerName(playerName);
        currentRound.setPlayerAnswer(answer);
        currentRound.setCorrect(correct);

        if (correct) {
            showStatus("Correct! The minimum cost is $" + answer, CLR_SUCCESS);
            repo.savePlayerResult(playerName, answer, roundNumber, "MINIMUM_COST");
            showWinDialog(answer);
        } else {
            int diff = Math.abs(answer - game.getCorrectAnswer());
            if (diff <= game.getCorrectAnswer() * 0.05) {
                // within 5% — draw
                showStatus("So close! The correct answer was $"
                        + game.getCorrectAnswer(), CLR_AMBER);
                showDrawDialog(game.getCorrectAnswer());
            } else {
                showStatus("Incorrect. The correct answer was $"
                        + game.getCorrectAnswer(), CLR_DANGER);
                showLoseDialog(game.getCorrectAnswer());
            }
        }

        // Lock input after submission
        txtAnswer.setEnabled(false);
        btnSubmit.setEnabled(false);
        btnNextRound.setEnabled(true);
    }

    // ── Result dialogs ────────────────────────────────────────
    private void showWinDialog(int answer) {
        JOptionPane.showMessageDialog(this,
                "<html><b>You got it right!</b><br>" +
                        "Minimum Cost: <b>$" + answer + "</b><br>" +
                        "Hungarian time: " + game.getHungarianTimeMs() + " ms<br>" +
                        "Greedy time: " + game.getGreedyTimeMs() + " ms</html>",
                "Correct!", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showLoseDialog(int correct) {
        JOptionPane.showMessageDialog(this,
                "<html><b>Wrong answer.</b><br>" +
                        "The minimum cost was: <b>$" + correct + "</b><br>" +
                        "Your answer: $" + txtAnswer.getText().trim() + "</html>",
                "Incorrect", JOptionPane.ERROR_MESSAGE);
    }

    private void showDrawDialog(int correct) {
        JOptionPane.showMessageDialog(this,
                "<html><b>Very close!</b><br>" +
                        "The minimum cost was: <b>$" + correct + "</b><br>" +
                        "Your answer was within 5%.</html>",
                "Almost!", JOptionPane.WARNING_MESSAGE);
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
                ? new Color(0xE1F5EE)
                : color.equals(CLR_DANGER)
                ? new Color(0xFCEBEB)
                : new Color(0xFAEEDA));
        lblStatus.setForeground(color);
        statusPanel.setVisible(true);
    }

    private void highlightError(JTextField field, String message) {
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CLR_DANGER, 1),
                new EmptyBorder(4, 8, 4, 8)
        ));
        JOptionPane.showMessageDialog(this, message,
                "Validation Error", JOptionPane.WARNING_MESSAGE);
        // Reset border after user clicks OK
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CLR_BORDER),
                new EmptyBorder(4, 8, 4, 8)
        ));
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg,
                "Error", JOptionPane.ERROR_MESSAGE);
    }
}