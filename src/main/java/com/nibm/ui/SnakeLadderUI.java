package com.nibm.ui;

import com.nibm.db.SnakeLadderRepository;
import com.nibm.games.SnakeLadderGame;
import com.nibm.models.SnakeLadderRound;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SnakeLadderUI extends JDialog {

    // ── Colours ───────────────────────────────────────────────
    private static final Color CLR_HEADER  = new Color(0x534AB7);
    private static final Color CLR_BG      = new Color(0xF1EFE8);
    private static final Color CLR_CARD    = new Color(0xFFFFFF);
    private static final Color CLR_BORDER  = new Color(0xD3D1C7);
    private static final Color CLR_TEXT    = new Color(0x2C2C2A);
    private static final Color CLR_MUTED   = new Color(0x888780);
    private static final Color CLR_SUCCESS = new Color(0x1D9E75);
    private static final Color CLR_DANGER  = new Color(0xE24B4A);
    private static final Color CLR_DRAW    = new Color(0xBA7517);
    private static final Color CLR_SNAKE   = new Color(0xE24B4A);
    private static final Color CLR_LADDER  = new Color(0x1D9E75);
    private static final Color CLR_CELL_A  = new Color(0xEEEDFE);
    private static final Color CLR_CELL_B  = new Color(0xFFFFFF);

    // ── State ─────────────────────────────────────────────────
    private final SnakeLadderGame game   = new SnakeLadderGame();
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
        setSize(700, 800);
        setMinimumSize(new Dimension(680, 760));
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

    // ── Board size input with full validation ─────────────────
    private void askBoardSize() {
        while (true) {
            String input = JOptionPane.showInputDialog(
                    this,
                    "<html><b>Enter board size N</b> (between 6 and 12):<br><br>"
                            + "• Board will be N×N cells<br>"
                            + "• Number of snakes = N-2<br>"
                            + "• Number of ladders = N-2<br>"
                            + "• Positions are randomly generated</html>",
                    "Board Size", JOptionPane.PLAIN_MESSAGE);

            // User cancelled
            if (input == null) { dispose(); return; }

            input = input.trim();

            // Validation 1: not empty
            if (input.isEmpty()) {
                showError("Board size cannot be empty. Please enter a number.");
                continue;
            }

            // Validation 2: digits only (no decimals, no negatives)
            if (!input.matches("\\d+")) {
                showError("Board size must be a positive whole number.\n"
                        + "No decimals, letters, or negative signs allowed.");
                continue;
            }

            // Validation 3: parse
            int n;
            try {
                n = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                showError("Invalid number. Please enter a value between 6 and 12.");
                continue;
            }

            // Validation 4: range
            if (n < 6) {
                showError("N is too small. Minimum value is 6.\nYou entered: " + n);
                continue;
            }
            if (n > 12) {
                showError("N is too large. Maximum value is 12.\nYou entered: " + n);
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
        body.add(Box.createVerticalStrut(16));
        return body;
    }

    // ── Info card ─────────────────────────────────────────────
    private JPanel buildInfoCard() {
        JPanel card = makeCard();
        card.setLayout(new GridLayout(2, 4, 8, 6));

        card.add(makeLabel("Player:",     CLR_MUTED, 11));
        card.add(makeLabel(playerName,    CLR_TEXT,  12));
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

        JLabel title = makeLabel("Board Layout", CLR_TEXT, 12);
        title.setFont(new Font("SansSerif", Font.BOLD, 12));
        title.setBorder(new EmptyBorder(0, 0, 6, 0));

        // Legend
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
        legend.setOpaque(false);
        legend.add(makeLegendItem("Snake (S = head)", CLR_SNAKE));
        legend.add(makeLegendItem("Ladder (L = base)", CLR_LADDER));

        boardPanel = new BoardPanel();
        boardPanel.setPreferredSize(new Dimension(380, 300));

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

    // ── 3-choice card ─────────────────────────────────────────
    private JPanel buildChoiceCard() {
        JPanel card = makeCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel question = new JLabel(
                "What is the minimum number of dice throws to reach the last cell?");
        question.setFont(new Font("SansSerif", Font.BOLD, 12));
        question.setForeground(CLR_TEXT);
        question.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel hint = new JLabel("Select one of the three options below:");
        hint.setFont(new Font("SansSerif", Font.PLAIN, 11));
        hint.setForeground(CLR_MUTED);
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        for (int i = 0; i < 3; i++) {
            final int idx = i;
            choiceButtons[i] = new JButton("—");
            choiceButtons[i].setFont(new Font("SansSerif", Font.BOLD, 15));
            choiceButtons[i].setForeground(CLR_HEADER);
            choiceButtons[i].setBackground(CLR_CELL_A);
            choiceButtons[i].setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(CLR_HEADER, 1),
                    new EmptyBorder(10, 28, 10, 28)
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
        card.add(Box.createVerticalStrut(10));
        card.add(btnRow);
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
        statusPanel.setPreferredSize(new Dimension(600, 46));
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
        btnNextRound.addActionListener(e -> askBoardSize());

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
    private void startNewRound(int n) {
        roundNumber++;
        lblRound.setText("Round " + roundNumber);

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
                            showError("Error starting round: "
                                    + ex.getMessage());
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

        for (int i = 0; i < 3; i++) {
            choiceButtons[i].setText(String.valueOf(currentChoices[i]));
            choiceButtons[i].setEnabled(true);
        }

        boardPanel.setBoard(
                game.getN(), game.getBoard(),
                game.getSnakes(), game.getLadders());
        boardPanel.repaint();
    }

    // ── Submit choice — WIN / DRAW / LOSE ────────────────────
    private void submitChoice(int chosen) {
        // Disable all buttons after selection
        for (JButton btn : choiceButtons) btn.setEnabled(false);

        String result = game.classifyAnswer(chosen);

        currentRound.setPlayerName(playerName);
        currentRound.setPlayerAnswer(chosen);

        switch (result) {
            case "WIN":
                currentRound.setCorrect(true);
                showStatus(
                        "Correct! Minimum throws = " + chosen, CLR_SUCCESS);
                repo.savePlayerResult(playerName, chosen, roundNumber);
                showWinDialog(chosen);
                break;

            case "DRAW":
                currentRound.setCorrect(false);
                showStatus(
                        "So close! Correct answer was "
                                + game.getCorrectAnswer()
                                + ", you chose " + chosen,
                        CLR_DRAW);
                showDrawDialog(chosen, game.getCorrectAnswer());
                break;

            case "LOSE":
            default:
                currentRound.setCorrect(false);
                showStatus(
                        "Wrong! Correct answer was "
                                + game.getCorrectAnswer(),
                        CLR_DANGER);
                showLoseDialog(game.getCorrectAnswer(), chosen);
                break;
        }

        btnNextRound.setEnabled(true);
    }

    // ── Result dialogs ────────────────────────────────────────
    private void showWinDialog(int answer) {
        JOptionPane.showMessageDialog(this,
                "<html><b>You got it right!</b><br><br>"
                        + "Minimum dice throws: <b>" + answer + "</b><br>"
                        + "BFS time:      " + game.getBfsTimeMs()      + " ms<br>"
                        + "Dijkstra time: " + game.getDijkstraTimeMs() + " ms<br><br>"
                        + "Your answer has been saved to the database.</html>",
                "You Win!", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showDrawDialog(int chosen, int correct) {
        JOptionPane.showMessageDialog(this,
                "<html><b>Almost!</b><br><br>"
                        + "Your answer: <b>" + chosen + "</b><br>"
                        + "Correct answer: <b>" + correct + "</b><br><br>"
                        + "You were only 1 throw away — so close!</html>",
                "Draw", JOptionPane.WARNING_MESSAGE);
    }

    private void showLoseDialog(int correct, int chosen) {
        JOptionPane.showMessageDialog(this,
                "<html><b>Wrong answer!</b><br><br>"
                        + "You chose: <b>" + chosen + "</b><br>"
                        + "Correct answer: <b>" + correct + "</b><br><br>"
                        + "Better luck next round!</html>",
                "You Lose", JOptionPane.ERROR_MESSAGE);
    }

    // ── Board drawing panel ───────────────────────────────────
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

            int w     = getWidth();
            int h     = getHeight();
            int cellW = w / n;
            int cellH = h / n;

            // ── Draw cells ────────────────────────────────────
            for (int row = 0; row < n; row++) {
                for (int col = 0; col < n; col++) {
                    int cellNum = getCellNumber(row, col);
                    int x = col * cellW;
                    int y = row * cellH;

                    g2.setColor((row + col) % 2 == 0
                            ? CLR_CELL_A : CLR_CELL_B);
                    g2.fillRect(x, y, cellW, cellH);
                    g2.setColor(CLR_BORDER);
                    g2.drawRect(x, y, cellW, cellH);

                    // Cell number
                    g2.setColor(CLR_MUTED);
                    g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
                    g2.drawString(String.valueOf(cellNum),
                            x + 2, y + cellH - 3);
                }
            }

            // ── Draw snakes (red lines, S at head) ────────────
            g2.setStroke(new BasicStroke(2.5f,
                    BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (int[] snake : snakes) {
                Point head = getCellCenter(snake[0], cellW, cellH);
                Point tail = getCellCenter(snake[1], cellW, cellH);
                g2.setColor(CLR_SNAKE);
                g2.drawLine(head.x, head.y, tail.x, tail.y);
                g2.fillOval(head.x - 5, head.y - 5, 10, 10);
                g2.setFont(new Font("SansSerif", Font.BOLD, 9));
                g2.drawString("S", head.x + 7, head.y - 3);
            }

            // ── Draw ladders (green lines, L at base) ─────────
            for (int[] ladder : ladders) {
                Point base = getCellCenter(ladder[0], cellW, cellH);
                Point top  = getCellCenter(ladder[1], cellW, cellH);
                g2.setColor(CLR_LADDER);
                g2.drawLine(base.x, base.y, top.x, top.y);
                g2.fillOval(base.x - 5, base.y - 5, 10, 10);
                g2.setFont(new Font("SansSerif", Font.BOLD, 9));
                g2.drawString("L", base.x + 7, base.y - 3);
            }

            g2.setStroke(new BasicStroke(1f));

            // ── Highlight START and END cells ─────────────────
            Point start = getCellCenter(1, cellW, cellH);
            Point end   = getCellCenter(n * n, cellW, cellH);

            g2.setFont(new Font("SansSerif", Font.BOLD, 9));
            g2.setColor(CLR_SUCCESS);
            g2.drawString("START", start.x - 14, start.y + 4);

            g2.setColor(CLR_HEADER);
            g2.drawString("END", end.x - 10, end.y + 4);
        }

        // ── Cell center pixel from cell number ────────────────
        // Uses zigzag (boustrophedon) numbering:
        // Bottom-left = 1, zigzag upward
        private Point getCellCenter(int cellNum, int cellW, int cellH) {
            int idx      = cellNum - 1;       // 0-based
            int boardRow = idx / n;           // 0 = bottom row
            int boardCol = idx % n;

            // Odd rows go right-to-left
            if (boardRow % 2 == 1) {
                boardCol = n - 1 - boardCol;
            }

            // Screen row: row 0 on screen = top = highest boardRow
            int screenRow = n - 1 - boardRow;
            int screenCol = boardCol;

            return new Point(
                    screenCol * cellW + cellW / 2,
                    screenRow * cellH + cellH / 2
            );
        }

        // ── Cell number from screen row/col ───────────────────
        private int getCellNumber(int screenRow, int screenCol) {
            int boardRow = n - 1 - screenRow;
            int col = (boardRow % 2 == 0)
                    ? screenCol
                    : n - 1 - screenCol;
            return boardRow * n + col + 1;
        }
    }

    // ── UI helpers ────────────────────────────────────────────
    private JPanel makeLegendItem(String label, Color color) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        item.setOpaque(false);
        JLabel box = new JLabel("■");
        box.setForeground(color);
        box.setFont(new Font("SansSerif", Font.PLAIN, 13));
        item.add(box);
        item.add(makeLabel(label, CLR_MUTED, 11));
        return item;
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
            lblStatus.setBackground(new Color(0xFAEEDA)); // draw
        }
        lblStatus.setForeground(color);
        statusPanel.setVisible(true);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg,
                "Validation Error", JOptionPane.WARNING_MESSAGE);
    }
}