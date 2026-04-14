package com.nibm.ui;

import com.nibm.db.GameResultRepository;
import com.nibm.games.MinimumCostGame;
import com.nibm.models.GameMode;
import com.nibm.models.GameRound;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class MinimumCostUI extends JDialog {

    private static final Color CLR_HEADER  = new Color(0x1D9E75);
    private static final Color CLR_BG      = new Color(0xF1EFE8);
    private static final Color CLR_CARD    = new Color(0xFFFFFF);
    private static final Color CLR_BORDER  = new Color(0xD3D1C7);
    private static final Color CLR_TEXT    = new Color(0x2C2C2A);
    private static final Color CLR_MUTED   = new Color(0x888780);
    private static final Color CLR_SUCCESS = new Color(0x1D9E75);
    private static final Color CLR_DANGER  = new Color(0xE24B4A);
    private static final Color CLR_AMBER   = new Color(0xBA7517);

    private final MinimumCostGame game = new MinimumCostGame();
    private final GameResultRepository repo = new GameResultRepository();

    private GameRound currentRound;
    private int roundNumber = 0;
    private String playerName;
    private GameMode selectedMode;

    private JLabel lblRoundNum;
    private JLabel lblMode;
    private JLabel lblN;
    private JLabel lblHungTime;
    private JLabel lblGreedyTime;
    private JTextField txtAnswer;
    private JButton btnSubmit;
    private JButton btnNextRound;
    private JPanel statusPanel;
    private JLabel lblStatus;
    private JTable tblCosts;

    public MinimumCostUI(JFrame parent) {
        super(parent, "Game 1 — Minimum Cost", true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(false);

        playerName = askPlayerName(parent);
        if (playerName == null) {
            dispose();
            return;
        }

        selectedMode = askGameMode();
        if (selectedMode == null) {
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
                    parent,
                    "Enter your name to begin:",
                    "Player Name",
                    JOptionPane.PLAIN_MESSAGE
            );

            if (name == null) return null;

            name = name.trim();
            if (name.isEmpty()) {
                showError("Name cannot be empty.");
                continue;
            }
            if (!name.matches("[a-zA-Z ]+")) {
                showError("Name can only contain letters and spaces.");
                continue;
            }
            if (name.length() > 50) {
                showError("Name is too long. Maximum 50 characters.");
                continue;
            }
            return name;
        }
    }

    private GameMode askGameMode() {
        String[] options = {"Play Mode", "Performance Mode"};
        int choice = JOptionPane.showOptionDialog(
                this,
                "Choose a mode:\n\nPlay Mode = small matrix (4 to 8)\nPerformance Mode = large matrix (50 to 100)",
                "Select Mode",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choice == 0) return GameMode.PLAY;
        if (choice == 1) return GameMode.PERFORMANCE;
        return null;
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
        h.setBorder(new EmptyBorder(14, 20, 12, 20));

        JLabel title = new JLabel("Minimum Cost Assignment");
        title.setFont(new Font("SansSerif", Font.BOLD, 15));
        title.setForeground(Color.WHITE);

        lblRoundNum = new JLabel("Round 0");
        lblRoundNum.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblRoundNum.setForeground(new Color(0xD5F2E8));

        h.add(title, BorderLayout.CENTER);
        h.add(lblRoundNum, BorderLayout.EAST);
        return h;
    }

    private JPanel buildBody() {
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(CLR_BG);
        body.setBorder(new EmptyBorder(12, 16, 8, 16));

        body.add(buildInfoCard());
        body.add(Box.createVerticalStrut(8));
        body.add(buildTimingCard());
        body.add(Box.createVerticalStrut(8));
        body.add(buildMatrixCard());
        body.add(Box.createVerticalStrut(8));
        body.add(buildAnswerCard());
        body.add(Box.createVerticalStrut(8));
        body.add(buildStatusPanel());
        body.add(Box.createVerticalStrut(8));

        return body;
    }

    private JPanel buildInfoCard() {
        JPanel card = makeCard();
        card.setLayout(new GridLayout(3, 2, 8, 5));

        card.add(makeLabel("Player:", CLR_MUTED, 12));
        card.add(makeLabel(playerName, CLR_TEXT, 12));

        card.add(makeLabel("Mode:", CLR_MUTED, 12));
        lblMode = new JLabel(selectedMode.name());
        lblMode.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblMode.setForeground(CLR_SUCCESS);
        card.add(lblMode);

        card.add(makeLabel("Number of Tasks / Employees (N):", CLR_MUTED, 12));
        lblN = new JLabel("—");
        lblN.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblN.setForeground(CLR_SUCCESS);
        card.add(lblN);

        return card;
    }

    private JPanel buildTimingCard() {
        JPanel card = makeCard();
        card.setLayout(new GridLayout(3, 2, 8, 5));

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
        lblGreedyTime.setForeground(CLR_AMBER);
        card.add(lblGreedyTime);

        return card;
    }

    private JPanel buildMatrixCard() {
        JPanel card = makeCard();
        card.setLayout(new BorderLayout(6, 6));

        JLabel title = new JLabel("Cost Matrix ($) — Employees × Tasks");
        title.setFont(new Font("SansSerif", Font.BOLD, 12));
        title.setForeground(CLR_TEXT);

        JLabel hint = new JLabel("Rows = Employees, Columns = Tasks");
        hint.setFont(new Font("SansSerif", Font.PLAIN, 11));
        hint.setForeground(CLR_MUTED);

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(title);
        top.add(Box.createVerticalStrut(3));
        top.add(hint);

        tblCosts = new JTable();
        tblCosts.setEnabled(false);
        tblCosts.setRowHeight(22);
        tblCosts.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tblCosts.setFillsViewportHeight(true);

        JScrollPane costScroll = new JScrollPane(tblCosts);
        costScroll.setPreferredSize(new Dimension(620, 250));
        costScroll.setMinimumSize(new Dimension(620, 250));
        costScroll.getViewport().setBackground(Color.WHITE);

        card.add(top, BorderLayout.NORTH);
        card.add(costScroll, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildAnswerCard() {
        JPanel card = makeCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel question = new JLabel("Enter the minimum total assignment cost:");
        question.setFont(new Font("SansSerif", Font.BOLD, 13));
        question.setForeground(CLR_TEXT);
        question.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel hint = new JLabel(
                selectedMode == GameMode.PLAY
                        ? "Play Mode: calculate from the small matrix."
                        : "Performance Mode: large matrix, use this as an estimate/demo round."
        );
        hint.setFont(new Font("SansSerif", Font.PLAIN, 11));
        hint.setForeground(CLR_MUTED);
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtAnswer = new JTextField();
        txtAnswer.setFont(new Font("SansSerif", Font.PLAIN, 13));
        txtAnswer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        txtAnswer.setPreferredSize(new Dimension(500, 34));
        txtAnswer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CLR_BORDER),
                new EmptyBorder(4, 8, 4, 8)
        ));
        txtAnswer.addActionListener(e -> submitAnswer());

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnSubmit = makeButton("Submit Answer", CLR_SUCCESS, Color.WHITE);
        btnSubmit.addActionListener(e -> submitAnswer());

        btnNextRound = makeButton("Next Round →", CLR_BG, CLR_TEXT);
        btnNextRound.setEnabled(false);
        btnNextRound.addActionListener(e -> {
            GameMode mode = askGameMode();
            if (mode == null) return;

            selectedMode = mode;
            startNewRound();
        });

        btnRow.add(btnSubmit);
        btnRow.add(btnNextRound);

        card.add(question);
        card.add(Box.createVerticalStrut(4));
        card.add(hint);
        card.add(Box.createVerticalStrut(8));
        card.add(txtAnswer);
        card.add(Box.createVerticalStrut(8));
        card.add(btnRow);

        return card;
    }

    private JPanel buildStatusPanel() {
        statusPanel = new JPanel(new BorderLayout());
        statusPanel.setOpaque(false);
        statusPanel.setVisible(false);
        statusPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        statusPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        statusPanel.setPreferredSize(new Dimension(620, 46));

        lblStatus = new JLabel("", SwingConstants.CENTER);
        lblStatus.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblStatus.setBorder(new EmptyBorder(10, 12, 10, 12));
        lblStatus.setOpaque(true);

        statusPanel.add(lblStatus, BorderLayout.CENTER);
        return statusPanel;
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
        lblRoundNum.setText("Round " + roundNumber);

        SwingWorker<GameRound, Void> worker = new SwingWorker<GameRound, Void>() {
            @Override
            protected GameRound doInBackground() {
                return game.newRound(roundNumber, selectedMode);
            }

            @Override
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

        btnSubmit.setEnabled(false);
        btnNextRound.setEnabled(false);
        txtAnswer.setText("");
        txtAnswer.setEnabled(false);
        txtAnswer.setEditable(false);
        statusPanel.setVisible(false);
        lblN.setText("Computing...");
        lblHungTime.setText("—");
        lblGreedyTime.setText("—");
        tblCosts.setModel(new DefaultTableModel());

        worker.execute();
    }

    private void updateRoundUI() {
        lblMode.setText(selectedMode.name());
        lblN.setText(String.valueOf(game.getN()));
        lblHungTime.setText("—");
        lblGreedyTime.setText("—");

        loadCostMatrix(game.getCostMatrix());

        txtAnswer.setEnabled(true);
        txtAnswer.setEditable(true);
        btnSubmit.setEnabled(true);
        txtAnswer.requestFocus();
    }

    private void loadCostMatrix(int[][] matrix) {
        int n = matrix.length;

        String[] columns = new String[n + 1];
        columns[0] = "Emp/Task";
        for (int j = 0; j < n; j++) {
            columns[j + 1] = "T" + j;
        }

        Object[][] data = new Object[n][n + 1];
        for (int i = 0; i < n; i++) {
            data[i][0] = "E" + i;
            for (int j = 0; j < n; j++) {
                data[i][j + 1] = matrix[i][j];
            }
        }

        DefaultTableModel model = new DefaultTableModel(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblCosts.setModel(model);

        for (int c = 0; c < tblCosts.getColumnCount(); c++) {
            tblCosts.getColumnModel().getColumn(c).setPreferredWidth(58);
        }
    }

    private void submitAnswer() {
        lblHungTime.setText(game.getHungarianTimeNs() + " ns");
        lblGreedyTime.setText(game.getGreedyTimeNs() + " ns");
        String input = txtAnswer.getText().trim();

        // Validation 1: not empty
        if (input.isEmpty()) {
            showError("Please enter an answer before submitting.");
            return;
        }

        // Validation 2: digits only
        if (!input.matches("\\d+")) {
            showError("Answer must be a positive whole number "
                    + "(digits only, no decimals or letters).");
            return;
        }

        // Validation 3: parse safely
        int answer;
        try {
            answer = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            showError("Number is too large. Please enter a realistic cost value.");
            return;
        }

        // Validation 4: must be positive
        if (answer <= 0) {
            showError("Answer must be greater than zero.");
            return;
        }

        // Validation 5: upper bound sanity check
        // Max possible cost = N * MAX_COST = 100 * 200 = 20,000
        int maxPossible = game.getN() * 200;
        if (answer > maxPossible) {
            showError("Answer is unrealistically large.\n"
                    + "For N=" + game.getN()
                    + " with max cost $200, the maximum possible "
                    + "total assignment cost is $" + maxPossible + ".\n"
                    + "You entered: $" + answer);
            return;
        }

        // ── All validations passed ─────────────────────────────
        boolean correct = game.validateAnswer(answer);

        // Classify: WIN / DRAW / LOSE
        String result;
        if (correct) {
            result = "WIN";
        } else {
            // DRAW = within 5% of correct answer
            int correct_val = game.getCorrectAnswer();
            double diff     = Math.abs(answer - correct_val);
            double pct      = diff / correct_val;
            result = (pct <= 0.05) ? "DRAW" : "LOSE";
        }

        // Update round model
        currentRound.setPlayerName(playerName);
        currentRound.setPlayerAnswer(answer);
        currentRound.setCorrect(correct);

        // ✅ Only save to DB when player actually wins
        if (result.equals("WIN")) {
            repo.savePlayerResult(playerName, answer, roundNumber,
                    "MINIMUM_COST", selectedMode.name());
        }

        // Show status banner
        switch (result) {
            case "WIN":
                showStatus(
                        "Correct! The minimum cost is $" + answer,
                        CLR_SUCCESS);
                break;
            case "DRAW":
                showStatus(
                        "Very close! Correct optimal cost is $"
                                + game.getCorrectAnswer(),
                        CLR_AMBER);
                break;
            case "LOSE":
            default:
                showStatus(
                        "Wrong! Correct optimal cost is $"
                                + game.getCorrectAnswer(),
                        CLR_DANGER);
                break;
        }

        // Show result dialog
        showResultDialog(answer, result);

        // Lock input after submission
        txtAnswer.setEnabled(false);
        txtAnswer.setEditable(false);
        btnSubmit.setEnabled(false);
        btnNextRound.setEnabled(true);
    }

    private void showResultDialog(int answer, String result) {
        String title;
        int messageType;
        String resultLine;

        switch (result) {
            case "WIN":
                title       = "You Win!";
                messageType = JOptionPane.INFORMATION_MESSAGE;
                resultLine  = "<b style='color:green'>Correct!</b>";
                break;
            case "DRAW":
                title       = "Draw — Very Close!";
                messageType = JOptionPane.WARNING_MESSAGE;
                resultLine  = "<b style='color:orange'>Almost correct (within 5%)</b>";
                break;
            case "LOSE":
            default:
                title       = "Wrong Answer";
                messageType = JOptionPane.ERROR_MESSAGE;
                resultLine  = "<b style='color:red'>Incorrect</b>";
                break;
        }

        String message = "<html>"
                + resultLine + "<br><br>"
                + "Mode: <b>" + selectedMode.name() + "</b><br>"
                + "Your Answer: <b>$" + answer + "</b><br>"
                + "Correct Optimal Cost: <b>$"
                + game.getCorrectAnswer() + "</b><br>"
                + "Greedy Cost: $" + game.getGreedyCost() + "<br><br>"
                + "Hungarian Time: " + game.getHungarianTimeNs() + " ns<br>"
                + "Greedy Time: "    + game.getGreedyTimeNs()    + " ns"
                + "</html>";

        JOptionPane.showMessageDialog(this, message, title, messageType);
    }

    private JPanel makeCard() {
        JPanel card = new JPanel();
        card.setBackground(CLR_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CLR_BORDER),
                new EmptyBorder(10, 12, 10, 12)
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
                new EmptyBorder(6, 12, 6, 12)
        ));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void showStatus(String message, Color color) {
        lblStatus.setText(message);
        lblStatus.setBackground(color.equals(CLR_SUCCESS)
                ? new Color(0xE1F5EE)
                : new Color(0xFCEBEB));
        lblStatus.setForeground(color);
        statusPanel.setVisible(true);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(
                this,
                msg,
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}