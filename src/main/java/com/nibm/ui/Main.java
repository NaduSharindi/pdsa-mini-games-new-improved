package com.nibm.ui;

import com.nibm.db.MongoDBConnection;
import com.nibm.ui.*;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        System.out.println("╔══════════════════════════════╗");
        System.out.println("║   PDSA Game Suite — NIBM     ║");
        System.out.println("╚══════════════════════════════╝");

        while (running) {
            System.out.println("\n--- Main Menu ---");
            System.out.println("1. Minimum Cost");
            System.out.println("2. Snake and Ladder");
            System.out.println("3. Traffic Simulation");
            System.out.println("4. Knight's Tour");
            System.out.println("5. Sixteen Queens");
            System.out.println("0. Exit");
            System.out.print("Select: ");

            String input = scanner.nextLine().trim();

            switch (input) {
                case "1":
                    new MinimumCostUI().start(scanner);
                    break;
                case "2":
                    new SnakeLadderUI().start(scanner);
                    break;
                case "3":
                    new TrafficUI().start(scanner);
                    break;
                case "4":
                    new KnightTourUI().start(scanner);
                    break;
                case "5":
                    new QueensUI().start(scanner);
                    break;
                case "0":
                    MongoDBConnection.close();
                    System.out.println("Goodbye!");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option. Try again.");
            }
        }

        scanner.close();
    }
}