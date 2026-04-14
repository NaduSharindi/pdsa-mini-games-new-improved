package com.nibm.ui;

import com.nibm.db.KnightRepository;
import com.nibm.games.KnightTourGame;
import com.nibm.models.KnightMove;
import com.nibm.models.KnightRound;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class KnightTourUI extends JDialog {

    // ── Colours ───────────────────────────────────────────────
    private static final Color CLR_HEADER   = new Color(0x7B2D8B);
    private static final Color CLR_BG       = new Color(0xF1EFE8);
    private static final Color CLR_CARD     = new Color(0xFFFFFF);
    private static final Color CLR_BORDER   = new Color(0xD3D1C7);
    private static final Color CLR_TEXT     = new Color(0x2C2C2A);
    private static final Color CLR_MUTED    = new Color(0x888780);
    private static final Color CLR_SUCCESS  = new Color(0x1D9E75);
    private static final Color CLR_DANGER   = new Color(0xE24B4A);
    private static final Color CLR_LIGHT_SQ = new Color(0xF0D9B5);
    private static final Color CLR_DARK_SQ  = new Color(0xB58863);
    private static final Color CLR_START    = new Color(0x1D9E75);
    private static final Color CLR_VISITED  = new Color(0x9B59B6);
    private static final Color CLR_LAST     = new Color(0xE67E22);
    private static final Color CLR_VALID_HINT = new Color(0x85C1E9);

    // ── State ─────────────────────────────────────────────────
    private final KnightTourGame game   = new KnightTourGame();
    private final KnightRepository repo = new KnightRepository();
    private KnightRound currentRound;
    private int roundNumber  = 0;
    private String playerName;
    private int boardSize;
    private boolean gameOver = false;  // round ended

    // Animation after round ends
    private int animStep = 0;
    private Timer animTimer;

    // ── UI Components ─────────────────────────────────────────
    private JLabel lblRound, lblBoardSize, lblStart, lblProgress;
    private JLabel lblWarnTime, lblBackTime;
    private JLabel lblWarnSolved, lblBackSolved;
    private ChessBoardPanel boardPanel;
    private JButton btnGiveUp, btnShowSolution, btnNextRound;
    private JPanel statusPanel;
    private JLabel lblStatus;

    // ── Constructor ───────────────────────────────────────────
    public KnightTourUI(JFrame parent) {
        super(parent, "Game 4 — Knight's Tour", true);
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
                showError("Name must contain letters only."); continue;
            }
            if (name.length() > 50) {
                showError("Name too long. Max 50 characters."); continue;
            }
            return name;
        }
    }

    // ── Board size selection ──────────────────────────────────
    private void askBoardSize() {
        String[] options = {"8 × 8  (64 moves)", "16 × 16  (256 moves)"};
        int choice = JOptionPane.showOptionDialog(
                this,
                "<html><b>Select board size:</b><br><br>"
                        + "8×8: You must click 64 squares in valid knight order.<br>"
                        + "16×16: You must click 256 squares in valid knight order.<br><br>"
                        + "The starting square is placed automatically.</html>",
                "Board Size", JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]);

        if (choice == JOptionPane.CLOSED_OPTION) {
            dispose(); return;
        }

        boardSize = (choice == 0) ? 8 : 16;
        startNewRound(boardSize);
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

        JLabel title = new JLabel("Knight's Tour — Build the Full Sequence");
        title.setFont(new Font("SansSerif", Font.BOLD, 15));
        title.setForeground(Color.WHITE);

        lblRound = new JLabel("Round 0");
        lblRound.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblRound.setForeground(new Color(0xDFB3F0));

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
        body.add(buildInstructionCard());
        body.add(Box.createVerticalStrut(10));
        body.add(buildBoardCard());
        body.add(Box.createVerticalStrut(10));
        body.add(buildTimingCard());
        body.add(Box.createVerticalStrut(10));
        body.add(buildActionRow());
        body.add(Box.createVerticalStrut(10));
        body.add(buildStatusAndNext());
        body.add(Box.createVerticalStrut(20));

        return body;
    }

    // ── Info card ─────────────────────────────────────────────
    private JPanel buildInfoCard() {
        JPanel card = makeCard();
        card.setLayout(new GridLayout(2, 4, 8, 6));

        card.add(makeLabel("Player:", CLR_MUTED, 11));
        card.add(makeLabel(playerName, CLR_TEXT, 12));
        card.add(makeLabel("Board:", CLR_MUTED, 11));
        lblBoardSize = new JLabel("—");
        lblBoardSize.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblBoardSize.setForeground(CLR_HEADER);
        card.add(lblBoardSize);

        card.add(makeLabel("Start:", CLR_MUTED, 11));
        lblStart = new JLabel("—");
        lblStart.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblStart.setForeground(CLR_HEADER);
        card.add(lblStart);

        card.add(makeLabel("Progress:", CLR_MUTED, 11));
        lblProgress = new JLabel("0 / 0");
        lblProgress.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblProgress.setForeground(new Color(0x185FA5));
        card.add(lblProgress);

        return card;
    }

    // ── Instruction card ──────────────────────────────────────
    private JPanel buildInstructionCard() {
        JPanel card = makeCard();
        card.setBackground(new Color(0xEEEDFE));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x9B8FE0)),
                new EmptyBorder(10, 14, 10, 14)
        ));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel h = new JLabel("How to play:");
        h.setFont(new Font("SansSerif", Font.BOLD, 12));
        h.setForeground(new Color(0x3C3489));
        h.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel l1 = makeLabel(
                "1. The green square is your starting position (move #1).",
                new Color(0x3C3489), 11);
        JLabel l2 = makeLabel(
                "2. Click squares ONE BY ONE to build the full knight's tour.",
                new Color(0x3C3489), 11);
        JLabel l3 = makeLabel(
                "3. Each click must be a valid L-shape knight move from your last square.",
                new Color(0x3C3489), 11);
        JLabel l4 = makeLabel(
                "4. Visit ALL " + (boardSize > 0 ? boardSize * boardSize : "N²")
                        + " squares exactly once to WIN.",
                new Color(0x3C3489), 11);
        JLabel l5 = makeLabel(
                "5. Blue squares show valid moves from your current position.",
                new Color(0x3C3489), 11);

        for (JLabel l : new JLabel[]{h, l1, l2, l3, l4, l5}) {
            l.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(l);
            card.add(Box.createVerticalStrut(3));
        }
        return card;
    }

    // ── Chess board card ──────────────────────────────────────
    private JPanel buildBoardCard() {
        JPanel card = makeCard();
        card.setLayout(new BorderLayout());

        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        legend.setOpaque(false);
        legend.add(makeLegendItem("Start",        CLR_START));
        legend.add(makeLegendItem("Your path",    CLR_VISITED));
        legend.add(makeLegendItem("Last move",    CLR_LAST));
        legend.add(makeLegendItem("Valid moves",  CLR_VALID_HINT));

        boardPanel = new ChessBoardPanel();
        int pSize = (boardSize == 16) ? 380 : 320;
        boardPanel.setPreferredSize(new Dimension(pSize, pSize));

        card.add(legend,     BorderLayout.NORTH);
        card.add(boardPanel, BorderLayout.CENTER);
        return card;
    }

    // ── Timing card ───────────────────────────────────────────
    private JPanel buildTimingCard() {
        JPanel card = makeCard();
        card.setLayout(new GridLayout(3, 3, 8, 6));

        card.add(makeLabel("Algorithm",    CLR_MUTED, 11));
        card.add(makeLabel("Time Taken",   CLR_MUTED, 11));
        card.add(makeLabel("Solved?",      CLR_MUTED, 11));

        card.add(makeLabel("Warnsdorff:", CLR_TEXT, 12));
        lblWarnTime = new JLabel("—");
        lblWarnTime.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblWarnTime.setForeground(new Color(0x185FA5));
        card.add(lblWarnTime);
        lblWarnSolved = new JLabel("—");
        lblWarnSolved.setFont(new Font("SansSerif", Font.BOLD, 12));
        card.add(lblWarnSolved);

        card.add(makeLabel("Backtracking:", CLR_TEXT, 12));
        lblBackTime = new JLabel("—");
        lblBackTime.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblBackTime.setForeground(new Color(0xBA7517));
        card.add(lblBackTime);
        lblBackSolved = new JLabel("—");
        lblBackSolved.setFont(new Font("SansSerif", Font.BOLD, 12));
        card.add(lblBackSolved);

        return card;
    }

    // ── Action buttons row ────────────────────────────────────
    private JPanel buildActionRow() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        btnGiveUp = makeButton("Give Up (Draw)",
                new Color(0xFAEEDA), new Color(0x633806));
        btnGiveUp.setEnabled(false);
        btnGiveUp.addActionListener(e -> handleGiveUp());

        btnShowSolution = makeButton("Show Solution", CLR_BG, CLR_MUTED);
        btnShowSolution.setEnabled(false);
        btnShowSolution.addActionListener(e -> animateSolution());

        row.add(btnGiveUp);
        row.add(btnShowSolution);
        return row;
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
        statusPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        statusPanel.setMinimumSize(new Dimension(100, 50));
        statusPanel.setPreferredSize(new Dimension(600, 50));

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
    private void startNewRound(int size) {
        roundNumber++;
        this.boardSize = size;
        gameOver = false;

        lblRound.setText("Round " + roundNumber);
        statusPanel.setVisible(false);
        btnNextRound.setEnabled(false);
        btnGiveUp.setEnabled(false);
        btnShowSolution.setEnabled(false);

        if (animTimer != null && animTimer.isRunning()) animTimer.stop();

        lblBoardSize.setText("Computing...");
        lblProgress.setText("0 / " + (size * size));

        lblWarnTime.setText("Computing...");
        lblBackTime.setText("Computing...");
        lblWarnSolved.setText("—");
        lblBackSolved.setText("—");
        lblWarnSolved.setForeground(CLR_MUTED);
        lblBackSolved.setForeground(CLR_MUTED);

        SwingWorker<KnightRound, Void> worker =
                new SwingWorker<KnightRound, Void>() {
                    protected KnightRound doInBackground() {
                        return game.newRound(roundNumber, size);
                    }
                    protected void done() {
                        try {
                            currentRound = get();
                            List<KnightMove> wMoves =
                                    game.getTourAsMoves(game.getWarnsdorffTour());
                            List<KnightMove> bMoves =
                                    game.isBacktrackSolved()
                                            ? game.getTourAsMoves(
                                            game.getBacktrackTour())
                                            : new java.util.ArrayList<>();
                            repo.saveRound(currentRound, wMoves, bMoves);
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
        lblBoardSize.setText(boardSize + " × " + boardSize);
        lblStart.setText("Row " + game.getStartRow()
                + "  Col " + game.getStartCol());
        lblProgress.setText("1 / " + (boardSize * boardSize));

        lblWarnTime.setText("—");
        lblBackTime.setText("—");
        lblWarnSolved.setText("—");
        lblBackSolved.setText("—");
        lblWarnSolved.setForeground(CLR_MUTED);
        lblBackSolved.setForeground(CLR_MUTED);

        updateInstructionCard();

        boardPanel.setData(boardSize,
                game.getPlayerSequence(),
                game.getWarnsdorffTour(),
                game.getStartRow(), game.getStartCol(),
                false);
        boardPanel.repaint();

        btnGiveUp.setEnabled(true);
    }

    private void revealAlgorithmResults() {
        lblWarnTime.setText(game.getWarnsdorffTimeMs() + " ns");
        lblBackTime.setText(game.getBacktrackTimeMs() + " ns");

        lblWarnSolved.setText(game.isWarnsdorffSolved() ? "✓" : "✗");
        lblWarnSolved.setForeground(
                game.isWarnsdorffSolved() ? CLR_SUCCESS : CLR_DANGER);

        lblBackSolved.setText(game.isBacktrackSolved() ? "✓" : "✗");
        lblBackSolved.setForeground(
                game.isBacktrackSolved() ? CLR_SUCCESS : CLR_DANGER);
    }

    // Update instruction label with correct board total
    private void updateInstructionCard() {
        // Rebuild instruction card is complex — just update via label
        // if you stored the label reference. For now this is handled
        // in buildInstructionCard using boardSize field.
    }

    // ── Handle board click ────────────────────────────────────
    private void handleBoardClick(int row, int col) {
        if (gameOver) return;

        String result = game.processPlayerClick(row, col);

        switch (result) {
            case "WIN":
                gameOver = true;
                boardPanel.setData(boardSize,
                        game.getPlayerSequence(),
                        game.getWarnsdorffTour(),
                        game.getStartRow(), game.getStartCol(), false);
                boardPanel.repaint();
                updateProgress();
                handleWin();
                break;

            case "VALID":
                boardPanel.setData(boardSize,
                        game.getPlayerSequence(),
                        game.getWarnsdorffTour(),
                        game.getStartRow(), game.getStartCol(), false);
                boardPanel.repaint();
                updateProgress();
                break;

            case "INVALID_MOVE":
                gameOver = true;
                boardPanel.setData(boardSize,
                        game.getPlayerSequence(),
                        game.getWarnsdorffTour(),
                        game.getStartRow(), game.getStartCol(), false);
                boardPanel.repaint();
                handleInvalidMove(row, col);
                break;

            case "ALREADY_VISITED":
                showError("You already visited that square!\n"
                        + "Choose an unvisited square.");
                break;

            case "OUT_OF_BOUNDS":
                showError("Click inside the board.");
                break;

            default:
                break;
        }
    }

    private void updateProgress() {
        int done  = game.getPlayerMoveCount();
        int total = boardSize * boardSize;
        lblProgress.setText(done + " / " + total);
    }

    // ── Win handler ───────────────────────────────────────────
    private void handleWin() {
        revealAlgorithmResults();

        List<KnightMove> seq = game.getPlayerSequence();
        boolean valid = game.isValidTour(seq);

        if (valid) {
            showStatus("You completed the knight's tour!", CLR_SUCCESS);
            String seqStr = game.formatSequenceForDB(seq);
            repo.savePlayerResult(playerName, seqStr, roundNumber);
            showWinDialog();
        } else {
            // Shouldn't happen since we validate each click,
            // but extra safety check
            showStatus("Tour invalid — please try again.", CLR_DANGER);
            showLoseDialog("Internal validation failed.");
        }

        btnGiveUp.setEnabled(false);
        btnShowSolution.setEnabled(true);
        btnNextRound.setEnabled(true);
    }

    // ── Invalid move handler ──────────────────────────────────
    private void handleInvalidMove(int row, int col) {
        revealAlgorithmResults();

        showStatus("Invalid move! That's not a valid knight move.", CLR_DANGER);
        btnGiveUp.setEnabled(false);
        btnShowSolution.setEnabled(true);
        btnNextRound.setEnabled(true);
        showLoseDialog("Row " + row + ", Col " + col
                + " is not reachable by an L-shape from your last square.");
    }

    // ── Give up (Draw) ────────────────────────────────────────
    private void handleGiveUp() {
        if (gameOver) return;

        int choice = JOptionPane.showConfirmDialog(this,
                "<html>Are you sure you want to give up?<br>"
                        + "You completed <b>" + game.getPlayerMoveCount()
                        + " / " + (boardSize * boardSize) + "</b> moves.</html>",
                "Give Up?", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (choice != JOptionPane.YES_OPTION) return;

        gameOver = true;
        revealAlgorithmResults();

        showStatus("Draw — you completed "
                + game.getPlayerMoveCount() + " / "
                + (boardSize * boardSize) + " moves.", CLR_MUTED);
        showDrawDialog();

        btnGiveUp.setEnabled(false);
        btnShowSolution.setEnabled(true);
        btnNextRound.setEnabled(true);
    }

    // ── Animate Warnsdorff solution ───────────────────────────
    private void animateSolution() {
        if (animTimer != null && animTimer.isRunning()) animTimer.stop();
        animStep = 0;

        int delay = (boardSize == 8) ? 100 : 40;
        animTimer = new Timer(delay, e -> {
            animStep++;
            boardPanel.setAnimStep(animStep);
            boardPanel.repaint();
            if (animStep >= boardSize * boardSize) {
                ((Timer) e.getSource()).stop();
            }
        });
        boardPanel.setShowSolution(true,
                game.getTourAsMoves(game.getWarnsdorffTour()));
        animTimer.start();
    }

    // ── Result dialogs ────────────────────────────────────────
    private void showWinDialog() {
        JOptionPane.showMessageDialog(this,
                "<html><b>🎉 Congratulations!</b><br>"
                        + "You completed the full knight's tour!<br>"
                        + "Moves: <b>" + (boardSize * boardSize) + "</b><br>"
                        + "Warnsdorff time: " + game.getWarnsdorffTimeMs() + " ns<br>"
                        + "Backtracking time: " + game.getBacktrackTimeMs() + " ns<br><br>"
                        + "Your tour has been saved to the database.</html>",
                "You Win!", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showLoseDialog(String reason) {
        JOptionPane.showMessageDialog(this,
                "<html><b>Invalid move!</b><br>"
                        + reason + "<br><br>"
                        + "You completed <b>" + game.getPlayerMoveCount()
                        + " / " + (boardSize * boardSize) + "</b> moves.<br>"
                        + "Click 'Show Solution' to see the correct tour.</html>",
                "Game Over", JOptionPane.ERROR_MESSAGE);
    }

    private void showDrawDialog() {
        JOptionPane.showMessageDialog(this,
                "<html><b>You gave up.</b><br>"
                        + "You completed <b>" + game.getPlayerMoveCount()
                        + " / " + (boardSize * boardSize) + "</b> moves.<br>"
                        + "Click 'Show Solution' to see the Warnsdorff tour.</html>",
                "Draw", JOptionPane.WARNING_MESSAGE);
    }

    // ── Chess board painting panel ────────────────────────────
    private class ChessBoardPanel extends JPanel {

        private int boardSize;
        private List<KnightMove> playerSeq;
        private int[][] warnsdorffTour;
        private int startRow, startCol;

        private boolean showSolution = false;
        private List<KnightMove> solutionMoves;
        private int animStep = 0;

        private boolean[][] validHints;

        public void setData(int boardSize,
                            List<KnightMove> playerSeq,
                            int[][] warnsdorffTour,
                            int startRow, int startCol,
                            boolean showSolution) {
            this.boardSize      = boardSize;
            this.playerSeq      = playerSeq;
            this.warnsdorffTour = warnsdorffTour;
            this.startRow       = startRow;
            this.startCol       = startCol;
            this.showSolution   = showSolution;
            this.animStep       = 0;

            computeValidHints(playerSeq);
        }

        public void setShowSolution(boolean show,
                                    List<KnightMove> moves) {
            this.showSolution   = show;
            this.solutionMoves  = moves;
            this.animStep       = 0;
            this.validHints     = null;
        }

        public void setAnimStep(int step) { this.animStep = step; }

        private void computeValidHints(List<KnightMove> seq) {
            validHints = new boolean[boardSize][boardSize];
            if (seq == null || seq.isEmpty()) return;
            KnightMove last = seq.get(seq.size() - 1);
            int[] DX = {-2,-1, 1, 2, 2, 1,-1,-2};
            int[] DY = { 1, 2, 2, 1,-1,-2,-2,-1};

            boolean[][] visited = new boolean[boardSize][boardSize];
            for (KnightMove m : seq) visited[m.getRow()][m.getCol()] = true;

            for (int i = 0; i < 8; i++) {
                int nr = last.getRow() + DX[i];
                int nc = last.getCol() + DY[i];
                if (nr >= 0 && nr < boardSize
                        && nc >= 0 && nc < boardSize
                        && !visited[nr][nc]) {
                    validHints[nr][nc] = true;
                }
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (boardSize == 0) return;

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            int w     = getWidth();
            int h     = getHeight();
            int cellW = w / boardSize;
            int cellH = h / boardSize;
            int fs    = (boardSize == 8) ? 11 : 7;

            List<KnightMove> displaySeq = showSolution
                    ? solutionMoves : playerSeq;

            boolean[][] visited = new boolean[boardSize][boardSize];
            int[][] moveNums    = new int[boardSize][boardSize];
            int lastR = startRow, lastC = startCol;

            if (displaySeq != null) {
                int limit = showSolution
                        ? Math.min(animStep, displaySeq.size())
                        : displaySeq.size();
                for (int i = 0; i < limit; i++) {
                    KnightMove m = displaySeq.get(i);
                    visited[m.getRow()][m.getCol()]  = true;
                    moveNums[m.getRow()][m.getCol()] = m.getMoveNumber();
                    if (i == limit - 1) {
                        lastR = m.getRow(); lastC = m.getCol();
                    }
                }
            }

            for (int r = 0; r < boardSize; r++) {
                for (int c = 0; c < boardSize; c++) {
                    int x = c * cellW;
                    int y = r * cellH;

                    Color fill;
                    if (r == startRow && c == startCol && !showSolution) {
                        fill = CLR_START;
                    } else if (r == lastR && c == lastC && visited[r][c]) {
                        fill = CLR_LAST;
                    } else if (visited[r][c]) {
                        fill = CLR_VISITED;
                    } else if (!showSolution && validHints != null
                            && validHints[r][c]) {
                        fill = CLR_VALID_HINT;
                    } else {
                        fill = (r + c) % 2 == 0
                                ? CLR_LIGHT_SQ : CLR_DARK_SQ;
                    }

                    g2.setColor(fill);
                    g2.fillRect(x, y, cellW, cellH);

                    g2.setColor(new Color(0xA0896A));
                    g2.setStroke(new BasicStroke(0.5f));
                    g2.drawRect(x, y, cellW, cellH);

                    if (visited[r][c]) {
                        g2.setColor(Color.WHITE);
                        g2.setFont(new Font("SansSerif", Font.BOLD, fs));
                        FontMetrics fm = g2.getFontMetrics();
                        String lbl = String.valueOf(moveNums[r][c]);
                        g2.drawString(lbl,
                                x + (cellW - fm.stringWidth(lbl)) / 2,
                                y + (cellH + fm.getAscent()) / 2 - 2);
                    }

                    if (r == lastR && c == lastC && visited[r][c]) {
                        g2.setFont(new Font("SansSerif", Font.PLAIN,
                                (boardSize == 8) ? 20 : 13));
                        g2.setColor(Color.WHITE);
                        g2.drawString("♞",
                                x + cellW / 2 - (boardSize == 8 ? 7 : 5),
                                y + cellH / 2 + (boardSize == 8 ? 7 : 5));
                    }
                }
            }

            if (boardSize == 8) {
                g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
                g2.setColor(CLR_MUTED);
                for (int i = 0; i < boardSize; i++) {
                    g2.drawString(String.valueOf(i),
                            i * cellW + cellW / 2 - 3, h - 2);
                    g2.drawString(String.valueOf(i),
                            2, i * cellH + cellH / 2 + 4);
                }
            }
        }

        @Override
        public void addNotify() {
            super.addNotify();
            addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (boardSize == 0 || gameOver || showSolution) return;
                    int cellW = getWidth()  / boardSize;
                    int cellH = getHeight() / boardSize;
                    int col   = e.getX() / cellW;
                    int row   = e.getY() / cellH;
                    if (row < 0 || row >= boardSize
                            || col < 0 || col >= boardSize) return;
                    handleBoardClick(row, col);
                }
            });
        }
    }

    // ── UI helpers ────────────────────────────────────────────
    private JPanel makeLegendItem(String label, Color color) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        item.setOpaque(false);
        JLabel box = new JLabel("■");
        box.setForeground(color);
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

    private void showStatus(String msg, Color color) {
        lblStatus.setText(msg);
        lblStatus.setBackground(color.equals(CLR_SUCCESS)
                ? new Color(0xE1F5EE)
                : color.equals(CLR_DANGER)
                ? new Color(0xFCEBEB)
                : new Color(0xF1EFE8));
        lblStatus.setForeground(color);
        statusPanel.setVisible(true);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg,
                "Validation", JOptionPane.WARNING_MESSAGE);
    }
}