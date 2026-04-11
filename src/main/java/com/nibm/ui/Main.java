package com.nibm.ui;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainMenu::new);
    }
}