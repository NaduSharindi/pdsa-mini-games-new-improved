package com.nibm.ui;

import com.nibm.db.SnakeLadderRepository;
import com.nibm.games.SnakeLadderGame;
import com.nibm.models.BoardCell;
import com.nibm.models.SnakeLadderRound;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class SnakeLadderUI extends JDialog {

    // ── Colours ───────────────────────────────────────────────
    private static final Color CLR_HEADER   = new Color(0x534AB7); // purple
    private static final Color CLR_BG       = new Color(0xF1EFE8);
    private static final Color CLR_CARD     = new Color(0xFFFFFF);
    private static final Color CLR_BORDER   = new Color(0xD3D1C7);
    private static final Color CLR_TEXT     = new Color(0x2C2C2A);
    private static final Color CLR_MUTED    = new Color(0x888780);
    private static final Color CLR_SUCCESS  = new Color(0x1D9E75);
    private static final Color CLR_DANGER   = new Color(0xE24B4A);
    private static final Color CLR_SNAKE    = new Color(0xE24B4A);  // red
    private static final Color CLR_LADDER   = new Color(0x1D9E75);  // green
    private static final Color CLR_CELL_A   = new Color(0xEEEDFE);  // light purple
    private static final Color CLR_CELL_B   = new Color(0xFFFFFF);  // white

    // ── State ─────────────────────────────────────────────────
    private final SnakeLadderGame game = new SnakeLadderGame();
    private final SnakeLadderRepository repo = new SnakeLadderRepository();
    private SnakeLadderRound currentRound;
    private int roundNumber = 0;
    private String playerName;
    private int[] currentChoices;

    // ── UI Components ─────────────────────────────────────────
    private JLabel lblRound, lblN, lblCells;
    private JLabel lblBfsTime, lblDijkstraTime;
    private BoardPanel boardPanel;
    private JButton[] choiceButtons = new JButton[3];
    private JButton btnNextRound;
    private JPanel statusPanel;
    private JLabel lblStatus;

    // ── Constructor ───────────────────────────────────────────
    public SnakeLadderUI(JFrame parent) {
        super(parent, "Game 2 — Snake and Ladder", true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(false);

        playerName = askPlayerName(parent);
        if (playerName == null) { dispose(); return; }

        buildUI();
        setSize(700, 750);
        setLocationRelativeTo(parent);
        askBoardSize();
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

    // ── Ask board size N with validation ─────────────────────
    private void askBoardSize() {
        while (true) {
            String input = JOptionPane.showInputDialog(
                    this,
                    "Enter board size N (between 6 and 12):\n" +
                            "Board will be N×N with " +
                            "(N-2) snakes and (N-2) ladders.",
                    "Board Size", JOptionPane.PLAIN_MESSAGE);

            // User cancelled
            if (input == null) { dispose(); return; }

            input = input.trim();

            // Validation 1: not empty
            if (input.isEmpty()) {
                showError("Board size cannot be empty."); continue;
            }

            // Validation 2: digits only
            if (!input.matches("\\d+")) {
                showError("Board size must be a whole number."); continue;
            }

            int n;
            try {
                n = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                showError("Invalid number entered."); continue;
            }

            // Validation 3: range check
            if (n < 6 || n > 12) {
                showError("N must be between 6 and 12. You entered: " + n);
                continue;
            }

            startNewRound(n);
            return;
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

        JLabel title = new JLabel("Snake and Ladder — Min Dice Throws");
        title.setFont(new Font("SansSerif", Font.BOLD, 15));
        title.setForeground(Color.WHITE);

        lblRound = new JLabel("Round 0");
        lblRound.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblRound.setForeground(new Color(0xAFA9EC));

        h.add(title,    BorderLayout.CENTER);
        h.add(lblRound, BorderLayout.EAST);
        return h;
    }

    private JPanel buildBody() {
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(CLR_BG);
        body.setBorder(new EmptyBorder(14, 16, 8, 16));

        body.add(buildInfoCard());
        body.add(Box.createVerticalStrut(10));
        body.add(buildBoardCard());
        body.add(Box.createVerticalStrut(10));
        body.add(buildTimingCard());
        body.add(Box.createVerticalStrut(10));
        body.add(buildChoiceCard());
        body.add(Box.createVerticalStrut(10));
        body.add(buildStatusAndNext());

        return body;
    }

    // ── Info card ─────────────────────────────────────────────
    private JPanel buildInfoCard() {
        JPanel card = makeCard();
        card.setLayout(new GridLayout(2, 4, 8, 6));

        card.add(makeLabel("Player:",   CLR_MUTED, 11));
        card.add(makeLabel(playerName,  CLR_TEXT,  12));
        card.add(makeLabel("Board Size:", CLR_MUTED, 11));
        lblN = new JLabel("—");
        lblN.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblN.setForeground(CLR_HEADER);
        card.add(lblN);

        card.add(makeLabel("Total Cells:", CLR_MUTED, 11));
        lblCells = new JLabel("—");
        lblCells.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblCells.setForeground(CLR_HEADER);
        card.add(lblCells);
        card.add(new JLabel(""));
        card.add(new JLabel(""));

        return card;
    }

    // ── Board visual card ─────────────────────────────────────
    private JPanel buildBoardCard() {
        JPanel card = makeCard();
        card.setLayout(new BorderLayout());

        JLabel title = makeLabel("Board Preview", CLR_TEXT, 12);
        title.setFont(new Font("SansSerif", Font.BOLD, 12));
        title.setBorder(new EmptyBorder(0, 0, 8, 0));

        // Legend
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        legend.setOpaque(false);
        JLabel sLegend = makeLabel("■ Snake (head)", CLR_SNAKE, 11);
        JLabel lLegend = makeLabel("■ Ladder (base)", CLR_LADDER, 11);
        legend.add(sLegend);
        legend.add(lLegend);

        boardPanel = new BoardPanel();
        boardPanel.setPreferredSize(new Dimension(380, 380));

        card.add(title,      BorderLayout.NORTH);
        card.add(boardPanel, BorderLayout.CENTER);
        card.add(legend,     BorderLayout.SOUTH);
        return card;
    }

    // ── Timing card ───────────────────────────────────────────
    private JPanel buildTimingCard() {
        JPanel card = makeCard();
        card.setLayout(new GridLayout(3, 2, 8, 6));

        card.add(makeLabel("Algorithm", CLR_MUTED, 11));
        card.add(makeLabel("Time Taken", CLR_MUTED, 11));

        card.add(makeLabel("BFS:", CLR_TEXT, 12));
        lblBfsTime = new JLabel("—");
        lblBfsTime.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblBfsTime.setForeground(new Color(0x185FA5));
        card.add(lblBfsTime);

        card.add(makeLabel("Dijkstra:", CLR_TEXT, 12));
        lblDijkstraTime = new JLabel("—");
        lblDijkstraTime.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblDijkstraTime.setForeground(new Color(0xBA7517));
        card.add(lblDijkstraTime);

        return card;
    }

    // ── 3-choice answer card ──────────────────────────────────
    private JPanel buildChoiceCard() {
        JPanel card = makeCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel question = new JLabel(
                "What is the minimum number of dice throws to reach the last cell?");
        question.setFont(new Font("SansSerif", Font.BOLD, 12));
        question.setForeground(CLR_TEXT);
        question.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        for (int i = 0; i < 3; i++) {
            final int idx = i;
            choiceButtons[i] = new JButton("—");
            choiceButtons[i].setFont(new Font("SansSerif", Font.BOLD, 14));
            choiceButtons[i].setForeground(CLR_HEADER);
            choiceButtons[i].setBackground(CLR_CELL_A);
            choiceButtons[i].setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(0x534AB7), 1),
                    new EmptyBorder(10, 24, 10, 24)
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
        statusPanel.setPreferredSize(new Dimension(500, 50));

        lblStatus = new JLabel("", SwingConstants.CENTER);
        lblStatus.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblStatus.setBorder(new EmptyBorder(10, 16, 10, 16));
        lblStatus.setOpaque(true);
        statusPanel.add(lblStatus, BorderLayout.CENTER);

        btnNextRound = makeButton("Next Round →", CLR_BG, CLR_MUTED);
        btnNextRound.setEnabled(false);
        btnNextRound.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnNextRound.addActionListener(e -> askBoardSize());

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
    private void startNewRound(int n) {
        roundNumber++;
        lblRound.setText("Round " + roundNumber);

        // Disable choices while computing
        for (JButton btn : choiceButtons) {
            btn.setEnabled(false);
            btn.setText("—");
        }
        btnNextRound.setEnabled(false);
        statusPanel.setVisible(false);
        lblN.setText("Computing...");

        SwingWorker<SnakeLadderRound, Void> worker =
                new SwingWorker<SnakeLadderRound, Void>() {
                    protected SnakeLadderRound doInBackground() {
                        return game.newRound(roundNumber, n);
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
        lblN.setText(game.getN() + " × " + game.getN());
        lblCells.setText(String.valueOf(game.getTotalCells()));
        lblBfsTime.setText(game.getBfsTimeMs() + " ms");
        lblDijkstraTime.setText(game.getDijkstraTimeMs() + " ms");

        // Update choice buttons
        for (int i = 0; i < 3; i++) {
            choiceButtons[i].setText(String.valueOf(currentChoices[i]));
            choiceButtons[i].setEnabled(true);
        }

        // Repaint board
        boardPanel.setBoard(
                game.getN(), game.getBoard(),
                game.getSnakes(), game.getLadders());
        boardPanel.repaint();
    }

    // ── Submit choice ─────────────────────────────────────────
    private void submitChoice(int chosen) {
        // Disable all choices after selection
        for (JButton btn : choiceButtons) btn.setEnabled(false);

        boolean correct = game.validateAnswer(chosen);

        currentRound.setPlayerName(playerName);
        currentRound.setPlayerAnswer(chosen);
        currentRound.setCorrect(correct);

        if (correct) {
            showStatus("Correct! Minimum throws = " + chosen, CLR_SUCCESS);
            repo.savePlayerResult(playerName, chosen, roundNumber);
            showWinDialog(chosen);
        } else {
            showStatus("Wrong! Correct answer was "
                    + game.getCorrectAnswer(), CLR_DANGER);
            showLoseDialog(game.getCorrectAnswer(), chosen);
        }

        btnNextRound.setEnabled(true);
    }

    // ── Result dialogs ────────────────────────────────────────
    private void showWinDialog(int answer) {
        JOptionPane.showMessageDialog(this,
                "<html><b>Correct!</b><br>" +
                        "Minimum dice throws: <b>" + answer + "</b><br>" +
                        "BFS time: "      + game.getBfsTimeMs()      + " ms<br>" +
                        "Dijkstra time: " + game.getDijkstraTimeMs() + " ms</html>",
                "You Win!", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showLoseDialog(int correct, int chosen) {
        JOptionPane.showMessageDialog(this,
                "<html><b>Wrong answer!</b><br>" +
                        "You chose: <b>" + chosen + "</b><br>" +
                        "Correct answer: <b>" + correct + "</b></html>",
                "Incorrect", JOptionPane.ERROR_MESSAGE);
    }

    // ── Board painting panel ──────────────────────────────────
    private class BoardPanel extends JPanel {

        private int n;
        private int[] board;
        private int[][] snakes;
        private int[][] ladders;

        public void setBoard(int n, int[] board,
                             int[][] snakes, int[][] ladders) {
            this.n       = n;
            this.board   = board;
            this.snakes  = snakes;
            this.ladders = ladders;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (board == null) return;

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            int width  = getWidth();
            int height = getHeight();
            int cellW  = width  / n;
            int cellH  = height / n;

            // Draw cells
            for (int row = 0; row < n; row++) {
                for (int col = 0; col < n; col++) {
                    // Cell number: bottom-left = 1, zigzag
                    int cellNum = getCellNumber(row, col, n);
                    int x = col * cellW;
                    int y = row * cellH;

                    // Alternating colours
                    g2.setColor((row + col) % 2 == 0 ? CLR_CELL_A : CLR_CELL_B);
                    g2.fillRect(x, y, cellW, cellH);
                    g2.setColor(CLR_BORDER);
                    g2.drawRect(x, y, cellW, cellH);

                    // Cell number
                    g2.setColor(CLR_MUTED);
                    g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
                    g2.drawString(String.valueOf(cellNum),
                            x + 3, y + cellH - 4);
                }
            }

            // Draw snakes (red lines)
            g2.setStroke(new BasicStroke(2.5f));
            for (int[] snake : snakes) {
                Point head = getCellCenter(snake[0], n, cellW, cellH);
                Point tail = getCellCenter(snake[1], n, cellW, cellH);
                g2.setColor(CLR_SNAKE);
                g2.drawLine(head.x, head.y, tail.x, tail.y);
                // Head indicator
                g2.fillOval(head.x - 5, head.y - 5, 10, 10);
                // Small S label
                g2.setFont(new Font("SansSerif", Font.BOLD, 8));
                g2.drawString("S", head.x + 6, head.y - 4);
            }

            // Draw ladders (green lines)
            for (int[] ladder : ladders) {
                Point base = getCellCenter(ladder[0], n, cellW, cellH);
                Point top  = getCellCenter(ladder[1], n, cellW, cellH);
                g2.setColor(CLR_LADDER);
                g2.drawLine(base.x, base.y, top.x, top.y);
                // Base indicator
                g2.fillOval(base.x - 5, base.y - 5, 10, 10);
                // Small L label
                g2.setFont(new Font("SansSerif", Font.BOLD, 8));
                g2.drawString("L", base.x + 6, base.y - 4);
            }

            g2.setStroke(new BasicStroke(1f));

            // Highlight start (cell 1) and end (cell N*N)
            Point start = getCellCenter(1, n, cellW, cellH);
            Point end   = getCellCenter(n * n, n, cellW, cellH);

            g2.setColor(new Color(0x1D9E75));
            g2.setFont(new Font("SansSerif", Font.BOLD, 10));
            g2.drawString("START", start.x - 15, start.y + 4);

            g2.setColor(new Color(0x534AB7));
            g2.drawString("END", end.x - 10, end.y + 4);
        }

        // Convert cell number to pixel center
        private Point getCellCenter(int cellNum, int n, int cellW, int cellH) {
            // Find row/col from cell number (zigzag)
            int idx  = cellNum - 1;
            int row  = n - 1 - (idx / n);
            int col;
            if ((n - 1 - row) % 2 == 0) {
                col = idx % n;
            } else {
                col = n - 1 - (idx % n);
            }
            return new Point(col * cellW + cellW / 2, row * cellH + cellH / 2);
        }

        // Get cell number for a given grid row/col (zigzag numbering)
        private int getCellNumber(int row, int col, int n) {
            int boardRow = n - 1 - row; // flip row (row 0 = top of screen = last row)
            if (boardRow % 2 == 0) {
                return boardRow * n + col + 1;
            } else {
                return boardRow * n + (n - 1 - col) + 1;
            }
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
                "Validation Error", JOptionPane.WARNING_MESSAGE);
    }
}