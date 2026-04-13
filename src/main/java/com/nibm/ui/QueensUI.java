package com.nibm.ui;

import com.nibm.db.QueensSolutionRepository;
import com.nibm.games.QueensPuzzleGame;
import com.nibm.models.QueensRound;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class QueensUI extends JDialog {

    public static final int BOARD_SIZE = 16;

    // ── Colours ───────────────────────────────────────────────
    private static final Color CLR_HEADER   = new Color(0x993556);
    private static final Color CLR_BG       = new Color(0xF1EFE8);
    private static final Color CLR_CARD     = new Color(0xFFFFFF);
    private static final Color CLR_BORDER   = new Color(0xD3D1C7);
    private static final Color CLR_TEXT     = new Color(0x2C2C2A);
    private static final Color CLR_MUTED    = new Color(0x888780);
    private static final Color CLR_SUCCESS  = new Color(0x1D9E75);
    private static final Color CLR_DANGER   = new Color(0xE24B4A);
    private static final Color CLR_DRAW     = new Color(0xBA7517);
    private static final Color CLR_LIGHT_SQ = new Color(0xF0D9B5);
    private static final Color CLR_DARK_SQ  = new Color(0xB58863);
    private static final Color CLR_QUEEN    = new Color(0xE24B4A);
    private static final Color CLR_CONFLICT = new Color(0xFF6B6B);
    private static final Color CLR_SAFE     = new Color(0x1D9E75);

    // ── State ─────────────────────────────────────────────────
    private final QueensSolutionRepository repo =
            new QueensSolutionRepository();
    private final QueensPuzzleGame game =
            new QueensPuzzleGame(repo);

    private String playerName;
    private int[] queenCols;    // queenCols[row] = col, -1 = empty
    private int queensPlaced = 0;
    private QueensRound round;

    // ── UI Components ─────────────────────────────────────────
    private JLabel lblTotal, lblClaimed, lblRemaining;
    private JLabel lblSeqTime, lblThreadTime, lblSpeedup;
    private JLabel lblPlaced, lblStatus;
    private ChessBoardPanel boardPanel;
    private JButton btnSubmit, btnClear, btnNewRound;
    private JPanel statusPanel;

    // ── Constructor ───────────────────────────────────────────
    public QueensUI(JFrame parent) {
        super(parent, "Game 5 — Sixteen Queens Puzzle", true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(false);

        playerName = askPlayerName(parent);
        if (playerName == null) { dispose(); return; }

        buildUI();
        setSize(800, 900);
        setMinimumSize(new Dimension(780, 860));
        setLocationRelativeTo(parent);

        // Initialize game in background (may take time for 16x16)
        initializeGame();
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

    // ── Initialize game in background ────────────────────────
    private void initializeGame() {
        queenCols = new int[BOARD_SIZE];
        java.util.Arrays.fill(queenCols, -1);

        lblTotal.setText("Computing...");
        btnSubmit.setEnabled(false);
        btnClear.setEnabled(false);

        SwingWorker<QueensRound, Void> worker =
                new SwingWorker<QueensRound, Void>() {
                    protected QueensRound doInBackground() {
                        return game.initialize();
                    }
                    protected void done() {
                        try {
                            round = get();
                            updateStatsUI();
                            btnSubmit.setEnabled(true);
                            btnClear.setEnabled(true);
                            boardPanel.repaint();
                        } catch (Exception ex) {
                            showError("Error initializing game: "
                                    + ex.getMessage());
                        }
                    }
                };
        worker.execute();
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

        JLabel title = new JLabel(
                "Sixteen Queens Puzzle — 16×16 Board");
        title.setFont(new Font("SansSerif", Font.BOLD, 15));
        title.setForeground(Color.WHITE);

        JLabel sub = new JLabel("Player: " + playerName);
        sub.setFont(new Font("SansSerif", Font.PLAIN, 12));
        sub.setForeground(new Color(0xF4C0D1));

        h.add(title, BorderLayout.CENTER);
        h.add(sub,   BorderLayout.EAST);
        return h;
    }

    private JPanel buildBody() {
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(CLR_BG);
        body.setBorder(new EmptyBorder(14, 16, 8, 16));

        body.add(buildInstructionCard());
        body.add(Box.createVerticalStrut(10));
        body.add(buildStatsCard());
        body.add(Box.createVerticalStrut(10));
        body.add(buildComparisonCard());
        body.add(Box.createVerticalStrut(10));
        body.add(buildBoardCard());
        body.add(Box.createVerticalStrut(10));
        body.add(buildActionRow());
        body.add(Box.createVerticalStrut(10));
        body.add(buildStatusPanel());
        body.add(Box.createVerticalStrut(16));
        return body;
    }

    // ── Instruction card ──────────────────────────────────────
    private JPanel buildInstructionCard() {
        JPanel card = makeCard();
        card.setBackground(new Color(0xFBEAF0));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xED93B1)),
                new EmptyBorder(10, 14, 10, 14)
        ));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel h = new JLabel("How to play:");
        h.setFont(new Font("SansSerif", Font.BOLD, 12));
        h.setForeground(CLR_HEADER);
        h.setAlignmentX(Component.LEFT_ALIGNMENT);

        String[] lines = {
                "1. Click any square in each row to place a queen (one per row).",
                "2. All 16 queens must be placed — one per row, none threatening each other.",
                "3. No two queens can share the same column or diagonal.",
                "4. Click an occupied square to remove that queen.",
                "5. Submit your arrangement — if it is a valid unseen solution you WIN!",
                "6. If already found by another player — try a different arrangement."
        };

        card.add(h);
        for (String line : lines) {
            card.add(Box.createVerticalStrut(3));
            JLabel l = makeLabel(line, new Color(0x72243E), 11);
            l.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(l);
        }
        return card;
    }

    // ── Stats card ────────────────────────────────────────────
    private JPanel buildStatsCard() {
        JPanel card = makeCard();
        card.setLayout(new GridLayout(2, 3, 12, 8));

        card.add(makeLabel("Total Solutions:", CLR_MUTED, 11));
        card.add(makeLabel("Already Found:", CLR_MUTED, 11));
        card.add(makeLabel("Remaining:", CLR_MUTED, 11));

        lblTotal = new JLabel("—");
        lblTotal.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblTotal.setForeground(CLR_HEADER);
        card.add(lblTotal);

        lblClaimed = new JLabel("—");
        lblClaimed.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblClaimed.setForeground(new Color(0x1D9E75));
        card.add(lblClaimed);

        lblRemaining = new JLabel("—");
        lblRemaining.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblRemaining.setForeground(new Color(0xBA7517));
        card.add(lblRemaining);

        return card;
    }

    // ── Algorithm comparison card ─────────────────────────────
    private JPanel buildComparisonCard() {
        JPanel card = makeCard();
        card.setLayout(new GridLayout(3, 3, 12, 8));

        card.add(makeLabel("Algorithm",    CLR_MUTED, 11));
        card.add(makeLabel("Time Taken",   CLR_MUTED, 11));
        card.add(makeLabel("Speed Ratio",  CLR_MUTED, 11));

        card.add(makeLabel("Sequential:", CLR_TEXT, 12));
        lblSeqTime = new JLabel("—");
        lblSeqTime.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblSeqTime.setForeground(new Color(0x185FA5));
        card.add(lblSeqTime);

        lblSpeedup = new JLabel("—");
        lblSpeedup.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblSpeedup.setForeground(CLR_SUCCESS);
        card.add(lblSpeedup);

        card.add(makeLabel("Threaded:", CLR_TEXT, 12));
        lblThreadTime = new JLabel("—");
        lblThreadTime.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblThreadTime.setForeground(CLR_DRAW);
        card.add(lblThreadTime);
        card.add(new JLabel(""));

        return card;
    }

    // ── Chess board card ──────────────────────────────────────
    private JPanel buildBoardCard() {
        JPanel card = makeCard();
        card.setLayout(new BorderLayout());

        // Queen counter
        lblPlaced = new JLabel("Queens placed: 0 / 16");
        lblPlaced.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblPlaced.setForeground(CLR_HEADER);
        lblPlaced.setBorder(new EmptyBorder(0, 0, 6, 0));

        boardPanel = new ChessBoardPanel();
        boardPanel.setPreferredSize(new Dimension(640, 640));

        card.add(lblPlaced,   BorderLayout.NORTH);
        card.add(boardPanel,  BorderLayout.CENTER);
        return card;
    }

    // ── Action row ────────────────────────────────────────────
    private JPanel buildActionRow() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        btnSubmit = makeButton(
                "Submit Arrangement", CLR_HEADER, Color.WHITE);
        btnSubmit.setEnabled(false);
        btnSubmit.addActionListener(e -> submitArrangement());

        btnClear = makeButton("Clear Board", CLR_BG, CLR_MUTED);
        btnClear.setEnabled(false);
        btnClear.addActionListener(e -> clearBoard());

        btnNewRound = makeButton("Play Again", CLR_BG, CLR_MUTED);
        btnNewRound.setEnabled(false);
        btnNewRound.addActionListener(e -> newRound());

        row.add(btnSubmit);
        row.add(btnClear);
        row.add(btnNewRound);
        return row;
    }

    // ── Status panel ──────────────────────────────────────────
    private JPanel buildStatusPanel() {
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setOpaque(false);
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);

        statusPanel = new JPanel(new BorderLayout());
        statusPanel.setOpaque(false);
        statusPanel.setVisible(false);
        statusPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        statusPanel.setMinimumSize(new Dimension(100, 46));
        statusPanel.setPreferredSize(new Dimension(700, 46));
        statusPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));

        lblStatus = new JLabel("", SwingConstants.CENTER);
        lblStatus.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblStatus.setBorder(new EmptyBorder(10, 16, 10, 16));
        lblStatus.setOpaque(true);
        statusPanel.add(lblStatus, BorderLayout.CENTER);

        wrapper.add(statusPanel);
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

    // ── Update stats after init ───────────────────────────────
    private void updateStatsUI() {
        if (round == null) return;
        int total    = round.getTotalSolutions();
        int claimed  = game.getClaimedCount();
        int remaining = total - claimed;

        lblTotal.setText(String.valueOf(total));
        lblClaimed.setText(String.valueOf(claimed));
        lblRemaining.setText(String.valueOf(remaining));

        lblSeqTime.setText(round.getSeqTimeMs() + " ms");
        lblThreadTime.setText(round.getThreadTimeMs() + " ms");

        // Speed comparison
        if (round.getThreadTimeMs() > 0 && round.getSeqTimeMs() > 0) {
            double ratio = (double) round.getSeqTimeMs()
                    / round.getThreadTimeMs();
            lblSpeedup.setText(String.format("%.2fx faster", ratio));
        } else {
            lblSpeedup.setText("N/A");
        }
    }

    // ── Handle board click ────────────────────────────────────
    private void handleBoardClick(int row, int col) {
        // Toggle queen at this row
        if (queenCols[row] == col) {
            // Remove queen from this row
            queenCols[row] = -1;
            queensPlaced--;
        } else {
            // Place/move queen in this row
            if (queenCols[row] == -1) queensPlaced++;
            queenCols[row] = col;
        }

        lblPlaced.setText("Queens placed: " + queensPlaced + " / 16");
        statusPanel.setVisible(false);
        boardPanel.repaint();
    }

    // ── Submit arrangement ────────────────────────────────────
    private void submitArrangement() {
        // Validation: all 16 queens must be placed
        if (queensPlaced != BOARD_SIZE) {
            showError("You must place exactly 16 queens (one per row).\n"
                    + "Currently placed: " + queensPlaced + " / 16\n"
                    + "Click a square in each empty row to place a queen.");
            return;
        }

        // Validation: check for conflicts before submitting
        if (!game.isValidQueenPlacement(queenCols)) {
            showStatus(
                    "Invalid arrangement — queens are threatening each other!",
                    CLR_DANGER);
            showLoseDialog(
                    "Two or more queens are in the same column or diagonal.");
            return;
        }

        // Process submission
        String result = game.processSubmission(playerName,
                queenCols.clone());

        // Refresh stats
        int claimed   = game.getClaimedCount();
        int total     = round.getTotalSolutions();
        int remaining = total - claimed;
        lblClaimed.setText(String.valueOf(claimed));
        lblRemaining.setText(String.valueOf(remaining));

        switch (result) {
            case "WIN":
                showStatus(
                        "Correct! New solution found and saved!", CLR_SUCCESS);
                showWinDialog(claimed, total);
                btnNewRound.setEnabled(true);
                break;

            case "ALREADY_CLAIMED":
                showStatus(
                        "Already found! Try a different arrangement.",
                        CLR_DRAW);
                showAlreadyClaimedDialog();
                clearBoard(); // let them try again
                break;

            case "ALL_DONE":
                showStatus(
                        "All " + total
                                + " solutions found! Claims reset for next players.",
                        CLR_SUCCESS);
                showAllDoneDialog(total);
                updateStatsUI();
                btnNewRound.setEnabled(true);
                break;

            case "INVALID":
            default:
                showStatus(
                        "Not a valid solution. Check your queen placement.",
                        CLR_DANGER);
                showLoseDialog(
                        "Your arrangement is not one of the "
                                + total + " valid solutions.");
                break;
        }
    }

    // ── Clear board ───────────────────────────────────────────
    private void clearBoard() {
        java.util.Arrays.fill(queenCols, -1);
        queensPlaced = 0;
        lblPlaced.setText("Queens placed: 0 / 16");
        statusPanel.setVisible(false);
        boardPanel.repaint();
    }

    // ── New round (same game — refresh stats) ─────────────────
    private void newRound() {
        clearBoard();
        updateStatsUI();
        btnNewRound.setEnabled(false);
        statusPanel.setVisible(false);
    }

    // ── Result dialogs ────────────────────────────────────────
    private void showWinDialog(int claimed, int total) {
        JOptionPane.showMessageDialog(this,
                "<html><b>Solution found!</b><br><br>"
                        + "Your queen arrangement is a valid solution.<br>"
                        + "Solutions found so far: <b>" + claimed
                        + " / " + total + "</b><br><br>"
                        + "Your name has been saved to the database.<br>"
                        + "Try to find another arrangement!</html>",
                "You Win!", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showAlreadyClaimedDialog() {
        JOptionPane.showMessageDialog(this,
                "<html><b>Already found!</b><br><br>"
                        + "This solution has already been identified "
                        + "by another player.<br><br>"
                        + "Please try a <b>different arrangement</b> "
                        + "of queens on the board.</html>",
                "Already Claimed", JOptionPane.WARNING_MESSAGE);
    }

    private void showAllDoneDialog(int total) {
        JOptionPane.showMessageDialog(this,
                "<html><b>All solutions found!</b><br><br>"
                        + "All <b>" + total + "</b> solutions to the "
                        + "16-Queens puzzle have been identified.<br><br>"
                        + "All claim flags have been reset.<br>"
                        + "Future players can now submit solutions again.</html>",
                "Puzzle Complete!", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showLoseDialog(String reason) {
        JOptionPane.showMessageDialog(this,
                "<html><b>Not a valid solution.</b><br><br>"
                        + reason + "<br><br>"
                        + "Remember: no two queens can share the same<br>"
                        + "row, column, or diagonal.</html>",
                "Invalid Arrangement", JOptionPane.ERROR_MESSAGE);
    }

    // ── Chess board painting panel ────────────────────────────
    private class ChessBoardPanel extends JPanel {

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            int w     = getWidth();
            int h     = getHeight();
            int cellW = w / BOARD_SIZE;
            int cellH = h / BOARD_SIZE;

            // Detect conflicts for highlighting
            boolean[][] conflict = detectConflicts();

            for (int row = 0; row < BOARD_SIZE; row++) {
                for (int col = 0; col < BOARD_SIZE; col++) {
                    int x = col * cellW;
                    int y = row * cellH;

                    // Cell background
                    Color fill = (row + col) % 2 == 0
                            ? CLR_LIGHT_SQ : CLR_DARK_SQ;
                    g2.setColor(fill);
                    g2.fillRect(x, y, cellW, cellH);

                    // Conflict highlight
                    if (queenCols[row] != -1
                            && queenCols[row] == col
                            && conflict[row][col]) {
                        g2.setColor(new Color(0xFF6B6B, true));
                        g2.fillRect(x, y, cellW, cellH);
                    }

                    // Cell border
                    g2.setColor(new Color(0xA0896A));
                    g2.setStroke(new BasicStroke(0.5f));
                    g2.drawRect(x, y, cellW, cellH);

                    // Row label (left side)
                    if (col == 0) {
                        g2.setColor(CLR_MUTED);
                        g2.setFont(new Font("SansSerif", Font.PLAIN, 8));
                        g2.drawString(String.valueOf(row),
                                x + 2, y + 10);
                    }

                    // Queen symbol
                    if (queenCols[row] == col) {
                        g2.setFont(new Font("SansSerif", Font.BOLD,
                                cellW - 6));
                        Color qColor = conflict[row][col]
                                ? CLR_CONFLICT : CLR_QUEEN;
                        g2.setColor(qColor);
                        FontMetrics fm = g2.getFontMetrics();
                        String q = "♛";
                        g2.drawString(q,
                                x + (cellW - fm.stringWidth(q)) / 2,
                                y + cellH - 4);
                    }
                }
            }

            // Column numbers at bottom
            g2.setFont(new Font("SansSerif", Font.PLAIN, 8));
            g2.setColor(CLR_MUTED);
            for (int col = 0; col < BOARD_SIZE; col++) {
                g2.drawString(String.valueOf(col),
                        col * cellW + cellW / 2 - 3,
                        h - 1);
            }
        }

        // Detect which queens are in conflict
        private boolean[][] detectConflicts() {
            boolean[][] conflict = new boolean[BOARD_SIZE][BOARD_SIZE];
            for (int r1 = 0; r1 < BOARD_SIZE; r1++) {
                if (queenCols[r1] == -1) continue;
                for (int r2 = r1 + 1; r2 < BOARD_SIZE; r2++) {
                    if (queenCols[r2] == -1) continue;
                    int c1 = queenCols[r1];
                    int c2 = queenCols[r2];
                    if (c1 == c2
                            || Math.abs(c1 - c2) == Math.abs(r1 - r2)) {
                        conflict[r1][c1] = true;
                        conflict[r2][c2] = true;
                    }
                }
            }
            return conflict;
        }

        @Override
        public void addNotify() {
            super.addNotify();
            addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (queenCols == null) return;
                    int cellW = getWidth()  / BOARD_SIZE;
                    int cellH = getHeight() / BOARD_SIZE;
                    int col   = e.getX() / cellW;
                    int row   = e.getY() / cellH;
                    if (row < 0 || row >= BOARD_SIZE
                            || col < 0 || col >= BOARD_SIZE) return;
                    handleBoardClick(row, col);
                }
            });
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

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg,
                "Validation Error", JOptionPane.WARNING_MESSAGE);
    }
}