package com.nibm.ui;

import com.nibm.db.MongoDBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MainMenu extends JFrame {

    // ── Colours ──────────────────────────────────────────────
    private static final Color CLR_HEADER_BG   = new Color(0x1D9E75);
    private static final Color CLR_HEADER_TEXT  = new Color(0xFFFFFF);
    private static final Color CLR_HEADER_SUB   = new Color(0x9FE1CB);
    private static final Color CLR_BG           = new Color(0xF1EFE8);
    private static final Color CLR_CARD_BG      = new Color(0xFFFFFF);
    private static final Color CLR_CARD_BORDER  = new Color(0xD3D1C7);
    private static final Color CLR_TITLE        = new Color(0x2C2C2A);
    private static final Color CLR_SUBTITLE     = new Color(0x888780);
    private static final Color CLR_FOOTER       = new Color(0xB4B2A9);

    // Badge colours per game [bg, fg]
    private static final Color[][] BADGE_COLORS = {
            { new Color(0xE1F5EE), new Color(0x085041) },  // Game 1 – teal
            { new Color(0xEEEDFE), new Color(0x3C3489) },  // Game 2 – purple
            { new Color(0xFAEEDA), new Color(0x633806) },  // Game 3 – amber
            { new Color(0xFAECE7), new Color(0x712B13) },  // Game 4 – coral
            { new Color(0xFBEAF0), new Color(0x72243E) },  // Game 5 – pink
    };

    private static final String[][] GAMES = {
            { "1", "Minimum Cost",       "Hungarian & Greedy · Task assignment"        },
            { "2", "Snake and Ladder",   "BFS & Dijkstra · Min dice throws"            },
            { "3", "Traffic Simulation", "Ford-Fulkerson & Edmonds-Karp · Max flow"    },
            { "4", "Knight's Tour",      "Warnsdorff & Backtracking · Chess moves"     },
            { "5", "Sixteen Queens",     "Sequential & Threaded · N-Queens puzzle"     },
    };

    // ── Constructor ───────────────────────────────────────────
    public MainMenu() {
        setTitle("PDSA Game Suite — COBSCCOMP242P-063");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setResizable(false);

        // Graceful shutdown on window close
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                exitApp();
            }
        });

        buildUI();
        pack();
        setLocationRelativeTo(null); // centre on screen
        setVisible(true);
    }

    // ── UI Construction ───────────────────────────────────────
    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(CLR_BG);

        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildGameList(), BorderLayout.CENTER);
        root.add(buildFooter(), BorderLayout.SOUTH);

        setContentPane(root);
    }

    // ── Header panel ─────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(CLR_HEADER_BG);
        header.setBorder(new EmptyBorder(24, 28, 20, 28));

        JLabel title = new JLabel("PDSA Game Suite");
        title.setFont(new Font("SansSerif", Font.PLAIN, 22));
        title.setForeground(CLR_HEADER_TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Problem Solving & Algorithm Design — COBSCCOMP242P-063");
        sub.setFont(new Font("SansSerif", Font.PLAIN, 12));
        sub.setForeground(CLR_HEADER_SUB);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        header.add(title);
        header.add(Box.createVerticalStrut(4));
        header.add(sub);

        return header;
    }

    // ── Game list panel ───────────────────────────────────────
    private JPanel buildGameList() {
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setBackground(CLR_BG);
        wrapper.setBorder(new EmptyBorder(20, 24, 8, 24));

        // Section label
        JLabel sectionLabel = new JLabel("SELECT A GAME");
        sectionLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        sectionLabel.setForeground(CLR_SUBTITLE);
        sectionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrapper.add(sectionLabel);
        wrapper.add(Box.createVerticalStrut(12));

        // Game cards
        for (int i = 0; i < GAMES.length; i++) {
            wrapper.add(buildGameCard(i));
            wrapper.add(Box.createVerticalStrut(8));
        }

        // Divider
        JSeparator sep = new JSeparator();
        sep.setForeground(CLR_CARD_BORDER);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrapper.add(Box.createVerticalStrut(8));
        wrapper.add(sep);
        wrapper.add(Box.createVerticalStrut(12));

        // Exit button
        wrapper.add(buildExitButton());

        return wrapper;
    }

    // ── Individual game card ──────────────────────────────────
    private JPanel buildGameCard(int index) {
        String[] game = GAMES[index];
        Color badgeBg = BADGE_COLORS[index][0];
        Color badgeFg = BADGE_COLORS[index][1];

        JPanel card = new JPanel(new BorderLayout(14, 0));
        card.setBackground(CLR_CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CLR_CARD_BORDER, 1),
                new EmptyBorder(10, 14, 10, 14)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Badge (number circle)
        JLabel badge = new JLabel(game[0], SwingConstants.CENTER);
        badge.setFont(new Font("SansSerif", Font.BOLD, 14));
        badge.setForeground(badgeFg);
        badge.setOpaque(true);
        badge.setBackground(badgeBg);
        badge.setPreferredSize(new Dimension(36, 36));
        badge.setBorder(BorderFactory.createLineBorder(badgeBg, 1));

        // Text block
        JPanel textBlock = new JPanel();
        textBlock.setLayout(new BoxLayout(textBlock, BoxLayout.Y_AXIS));
        textBlock.setOpaque(false);

        JLabel cardTitle = new JLabel(game[1]);
        cardTitle.setFont(new Font("SansSerif", Font.BOLD, 13));
        cardTitle.setForeground(CLR_TITLE);

        JLabel cardSub = new JLabel(game[2]);
        cardSub.setFont(new Font("SansSerif", Font.PLAIN, 11));
        cardSub.setForeground(CLR_SUBTITLE);

        textBlock.add(cardTitle);
        textBlock.add(Box.createVerticalStrut(2));
        textBlock.add(cardSub);

        // Arrow
        JLabel arrow = new JLabel("›");
        arrow.setFont(new Font("SansSerif", Font.PLAIN, 18));
        arrow.setForeground(CLR_SUBTITLE);

        card.add(badge, BorderLayout.WEST);
        card.add(textBlock, BorderLayout.CENTER);
        card.add(arrow, BorderLayout.EAST);

        // Hover effect
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                card.setBackground(CLR_BG);
                card.repaint();
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                card.setBackground(CLR_CARD_BG);
                card.repaint();
            }
            public void mouseClicked(java.awt.event.MouseEvent e) {
                openGame(index + 1);
            }
        });

        return card;
    }

    // ── Exit button ───────────────────────────────────────────
    private JButton buildExitButton() {
        JButton btn = new JButton("Exit Application");
        btn.setFont(new Font("SansSerif", Font.PLAIN, 13));
        btn.setForeground(CLR_SUBTITLE);
        btn.setBackground(CLR_BG);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CLR_CARD_BORDER, 1),
                new EmptyBorder(8, 0, 8, 0)
        ));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(CLR_CARD_BORDER);
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(CLR_BG);
            }
        });

        btn.addActionListener(e -> exitApp());
        return btn;
    }

    // ── Footer ────────────────────────────────────────────────
    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setBackground(CLR_BG);
        footer.setBorder(new EmptyBorder(0, 0, 12, 0));

        JLabel label = new JLabel(
                "BSc (Hons) Computing · NIBM School of Computing and Engineering"
        );
        label.setFont(new Font("SansSerif", Font.PLAIN, 11));
        label.setForeground(CLR_FOOTER);

        footer.add(label);
        return footer;
    }

    // ── Game launcher ─────────────────────────────────────────
    private void openGame(int gameNumber) {
        switch (gameNumber) {
            case 1:
                new MinimumCostUI(this);
                break;
            case 2:
                new SnakeLadderUI(this);
                break;
            case 3:
                new TrafficUI(this);
                break;
            case 4:
                new KnightTourUI(this);
                break;
            case 5:
                new QueensUI(this);
                break;
            default:
                break;
        }
    }

    // ── Exit handler ──────────────────────────────────────────
    private void exitApp() {
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to exit?",
                "Exit PDSA Game Suite",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (choice == JOptionPane.YES_OPTION) {
            MongoDBConnection.close();
            dispose();
            System.exit(0);
        }
    }

    // ── Entry point ───────────────────────────────────────────
    public static void main(String[] args) {
        // Run on the Event Dispatch Thread (Swing rule)
        SwingUtilities.invokeLater(MainMenu::new);
    }
}