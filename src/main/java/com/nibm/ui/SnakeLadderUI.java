package com.nibm.ui;

import com.nibm.db.SnakeLadderRepository;
import com.nibm.games.SnakeLadderGame;
import com.nibm.models.SnakeLadderRound;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SnakeLadderUI extends JDialog {

    private static final Color CLR_HEADER  = new Color(0x534AB7);
    private static final Color CLR_BG      = new Color(0xF1EFE8);
    private static final Color CLR_CARD    = new Color(0xFFFFFF);
    private static final Color CLR_BORDER  = new Color(0xD3D1C7);
    private static final Color CLR_TEXT    = new Color(0x2C2C2A);
    private static final Color CLR_MUTED   = new Color(0x888780);
    private static final Color CLR_SUCCESS = new Color(0x1D9E75);
    private static final Color CLR_SNAKE   = new Color(0xE24B4A);
    private static final Color CLR_LADDER  = new Color(0x1D9E75);
    private static final Color CLR_CELL_A  = new Color(0xEEEDFE);
    private static final Color CLR_CELL_B  = new Color(0xFFFFFF);

    private final SnakeLadderGame game = new SnakeLadderGame();
    private final SnakeLadderRepository repo = new SnakeLadderRepository();

    private SnakeLadderRound currentRound;
    private int roundNumber = 0;
    private String playerName;

    private JLabel lblRound, lblN, lblCells;
    private JLabel lblBfsTime, lblDijkstraTime;
    private JLabel lblPlayerPos, lblThrows, lblLastRoll, lblOptimal;
    private JLabel lblStatus;
    private JButton btnRoll, btnNewRound;
    private BoardPanel boardPanel;

    public SnakeLadderUI(JFrame parent) {
        super(parent, "Game 2 — Snake and Ladder", true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(false);

        playerName = askPlayerName(parent);
        if (playerName == null) {
            dispose();
            return;
        }

        buildUI();
        setSize(700, 800);
        setMinimumSize(new Dimension(680, 760));
        setLocationRelativeTo(parent);
        askBoardSize();
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
            return name;
        }
    }

    private void askBoardSize() {
        while (true) {
            String input = JOptionPane.showInputDialog(
                    this,
                    "Enter board size N (6 to 12):",
                    "Board Size",
                    JOptionPane.PLAIN_MESSAGE);

            if (input == null) {
                dispose();
                return;
            }

            input = input.trim();
            if (!input.matches("\\d+")) {
                showError("Enter a whole number between 6 and 12.");
                continue;
            }

            int n = Integer.parseInt(input);
            if (n < 6 || n > 12) {
                showError("Board size must be between 6 and 12.");
                continue;
            }

            startNewRound(n);
            return;
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

        JLabel title = new JLabel("Snake and Ladder — Play");
        title.setFont(new Font("SansSerif", Font.BOLD, 15));
        title.setForeground(Color.WHITE);

        lblRound = new JLabel("Round 0");
        lblRound.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblRound.setForeground(new Color(0xAFA9EC));

        h.add(title, BorderLayout.CENTER);
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
        body.add(buildStatsCard());
        body.add(Box.createVerticalStrut(10));
        body.add(buildControlCard());
        body.add(Box.createVerticalStrut(10));
        body.add(buildStatusCard());
        body.add(Box.createVerticalStrut(12));
        return body;
    }

    private JPanel buildInfoCard() {
        JPanel card = makeCard();
        card.setLayout(new GridLayout(2, 4, 8, 6));

        card.add(makeLabel("Player:", CLR_MUTED, 11));
        card.add(makeLabel(playerName, CLR_TEXT, 12));
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

        card.add(makeLabel("Optimal Throws:", CLR_MUTED, 11));
        lblOptimal = new JLabel("—");
        lblOptimal.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblOptimal.setForeground(CLR_SUCCESS);
        card.add(lblOptimal);

        return card;
    }

    private JPanel buildBoardCard() {
        JPanel card = makeCard();
        card.setLayout(new BorderLayout());

        JLabel title = makeLabel("Board Layout", CLR_TEXT, 12);
        title.setFont(new Font("SansSerif", Font.BOLD, 12));
        title.setBorder(new EmptyBorder(0, 0, 6, 0));

        boardPanel = new BoardPanel();
        boardPanel.setPreferredSize(new Dimension(380, 300));

        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
        legend.setOpaque(false);
        legend.add(makeLegendItem("Snake", CLR_SNAKE));
        legend.add(makeLegendItem("Ladder", CLR_LADDER));
        legend.add(makeLegendItem("Player", CLR_HEADER));

        card.add(title, BorderLayout.NORTH);
        card.add(boardPanel, BorderLayout.CENTER);
        card.add(legend, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildStatsCard() {
        JPanel card = makeCard();
        card.setLayout(new GridLayout(3, 4, 8, 6));

        card.add(makeLabel("Position:", CLR_MUTED, 11));
        lblPlayerPos = new JLabel("—");
        card.add(lblPlayerPos);

        card.add(makeLabel("Throws Used:", CLR_MUTED, 11));
        lblThrows = new JLabel("—");
        card.add(lblThrows);

        card.add(makeLabel("Last Roll:", CLR_MUTED, 11));
        lblLastRoll = new JLabel("—");
        card.add(lblLastRoll);

        card.add(makeLabel("BFS Time:", CLR_MUTED, 11));
        lblBfsTime = new JLabel("—");
        lblBfsTime.setForeground(new Color(0x185FA5));
        card.add(lblBfsTime);

        card.add(makeLabel("Dijkstra Time:", CLR_MUTED, 11));
        lblDijkstraTime = new JLabel("—");
        lblDijkstraTime.setForeground(new Color(0xBA7517));
        card.add(lblDijkstraTime);

        card.add(new JLabel(""));
        card.add(new JLabel(""));
        return card;
    }

    private JPanel buildControlCard() {
        JPanel card = makeCard();
        card.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 4));

        btnRoll = makeButton("Roll Dice", CLR_HEADER, Color.WHITE);
        btnRoll.addActionListener(e -> rollDice());

        btnNewRound = makeButton("Next Round →", CLR_BG, CLR_TEXT);
        btnNewRound.setEnabled(false);
        btnNewRound.addActionListener(e -> askBoardSize());

        card.add(btnRoll);
        card.add(btnNewRound);
        return card;
    }

    private JPanel buildStatusCard() {
        JPanel card = makeCard();
        card.setLayout(new BorderLayout());

        lblStatus = new JLabel("Ready to play.", SwingConstants.CENTER);
        lblStatus.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblStatus.setOpaque(true);
        lblStatus.setBackground(new Color(0xEEF2FF));
        lblStatus.setBorder(new EmptyBorder(12, 16, 12, 16));

        card.add(lblStatus, BorderLayout.CENTER);
        return card;
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

    private void startNewRound(int n) {
        roundNumber++;
        lblRound.setText("Round " + roundNumber);

        SwingWorker<SnakeLadderRound, Void> worker = new SwingWorker<SnakeLadderRound, Void>() {
            @Override
            protected SnakeLadderRound doInBackground() {
                return game.newRound(roundNumber, n);
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
        lblN.setText(game.getN() + " × " + game.getN());
        lblCells.setText(String.valueOf(game.getTotalCells()));

        // hide until game ends
        lblOptimal.setText("—");
        lblBfsTime.setText("—");
        lblDijkstraTime.setText("—");

        lblPlayerPos.setText(String.valueOf(game.getPlayerPosition()));
        lblThrows.setText(String.valueOf(game.getPlayerThrows()));
        lblLastRoll.setText("-");
        lblStatus.setText(game.getLastMoveMessage());
        lblStatus.setBackground(new Color(0xEEF2FF));

        btnRoll.setEnabled(true);
        btnNewRound.setEnabled(false);

        boardPanel.setBoard(
                game.getN(),
                game.getBoard(),
                game.getSnakes(),
                game.getLadders(),
                game.getPlayerPosition()
        );
        boardPanel.repaint();
    }

    private void rollDice() {
        game.rollDice();

        lblPlayerPos.setText(String.valueOf(game.getPlayerPosition()));
        lblThrows.setText(String.valueOf(game.getPlayerThrows()));
        lblLastRoll.setText(String.valueOf(game.getLastRoll()));
        lblStatus.setText(game.getLastMoveMessage());
        lblStatus.setBackground(new Color(0xEEF2FF));

        boardPanel.setBoard(
                game.getN(),
                game.getBoard(),
                game.getSnakes(),
                game.getLadders(),
                game.getPlayerPosition()
        );
        boardPanel.repaint();

        if (game.isGameOver()) {
            btnRoll.setEnabled(false);
            btnNewRound.setEnabled(true);

            // show only after finish
            lblOptimal.setText(String.valueOf(game.getCorrectAnswer()));
            lblBfsTime.setText(game.getBfsTimeNs() + " ns");
            lblDijkstraTime.setText(game.getDijkstraTimeNs() + " ns");

            currentRound.setPlayerName(playerName);
            currentRound.setPlayerThrows(game.getPlayerThrows());
            currentRound.setFinalPosition(game.getPlayerPosition());
            currentRound.setCompleted(true);

            repo.savePlayerResult(
                    playerName,
                    game.getPlayerThrows(),
                    game.getPlayerPosition(),
                    roundNumber
            );

            showFinishDialog();
        }
    }

    private void showFinishDialog() {
        String performance;
        if (game.getPlayerThrows() == game.getCorrectAnswer()) {
            performance = "Perfect! You matched the optimal answer.";
        } else if (game.getPlayerThrows() <= game.getCorrectAnswer() + 2) {
            performance = "Nice! You were close to the optimal answer.";
        } else {
            performance = "You finished, but used more throws than the optimal path.";
        }

        JOptionPane.showMessageDialog(this,
                "<html><b>Round Complete!</b><br><br>"
                        + "Your Throws: <b>" + game.getPlayerThrows() + "</b><br>"
                        + "Optimal Throws (BFS): <b>" + game.getCorrectAnswer() + "</b><br>"
                        + "Dijkstra Result: <b>" + game.getDijkstraAnswer() + "</b><br><br>"
                        + "BFS Time: " + game.getBfsTimeNs() + " ns<br>"
                        + "Dijkstra Time: " + game.getDijkstraTimeNs() + " ns<br><br>"
                        + performance + "</html>",
                "Game Complete",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private class BoardPanel extends JPanel {

        private int n;
        private int[] board;
        private int[][] snakes;
        private int[][] ladders;
        private int playerPosition;

        public void setBoard(int n, int[] board, int[][] snakes, int[][] ladders, int playerPosition) {
            this.n = n;
            this.board = board;
            this.snakes = snakes;
            this.ladders = ladders;
            this.playerPosition = playerPosition;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (board == null) return;

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int cellW = w / n;
            int cellH = h / n;

            for (int row = 0; row < n; row++) {
                for (int col = 0; col < n; col++) {
                    int cellNum = getCellNumber(row, col);
                    int x = col * cellW;
                    int y = row * cellH;

                    g2.setColor((row + col) % 2 == 0 ? CLR_CELL_A : CLR_CELL_B);
                    g2.fillRect(x, y, cellW, cellH);
                    g2.setColor(CLR_BORDER);
                    g2.drawRect(x, y, cellW, cellH);

                    g2.setColor(CLR_MUTED);
                    g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
                    g2.drawString(String.valueOf(cellNum), x + 2, y + cellH - 3);
                }
            }

            g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            for (int[] snake : snakes) {
                Point head = getCellCenter(snake[0], cellW, cellH);
                Point tail = getCellCenter(snake[1], cellW, cellH);
                g2.setColor(CLR_SNAKE);
                g2.drawLine(head.x, head.y, tail.x, tail.y);
            }

            for (int[] ladder : ladders) {
                Point base = getCellCenter(ladder[0], cellW, cellH);
                Point top = getCellCenter(ladder[1], cellW, cellH);
                g2.setColor(CLR_LADDER);
                g2.drawLine(base.x, base.y, top.x, top.y);
            }

            Point player = getCellCenter(playerPosition, cellW, cellH);
            g2.setColor(CLR_HEADER);
            g2.fillOval(player.x - 8, player.y - 8, 16, 16);

            Point start = getCellCenter(1, cellW, cellH);
            Point end = getCellCenter(n * n, cellW, cellH);

            g2.setFont(new Font("SansSerif", Font.BOLD, 9));
            g2.setColor(CLR_SUCCESS);
            g2.drawString("START", start.x - 14, start.y + 4);

            g2.setColor(CLR_HEADER);
            g2.drawString("END", end.x - 10, end.y + 4);
        }

        private Point getCellCenter(int cellNum, int cellW, int cellH) {
            int idx = cellNum - 1;
            int boardRow = idx / n;
            int boardCol = idx % n;

            if (boardRow % 2 == 1) {
                boardCol = n - 1 - boardCol;
            }

            int screenRow = n - 1 - boardRow;
            int screenCol = boardCol;

            return new Point(
                    screenCol * cellW + cellW / 2,
                    screenRow * cellH + cellH / 2
            );
        }

        private int getCellNumber(int screenRow, int screenCol) {
            int boardRow = n - 1 - screenRow;
            int col = (boardRow % 2 == 0)
                    ? screenCol
                    : n - 1 - screenCol;
            return boardRow * n + col + 1;
        }
    }

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

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg,
                "Validation Error", JOptionPane.WARNING_MESSAGE);
    }
}