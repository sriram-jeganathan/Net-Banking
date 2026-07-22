package com.sri.netbanking.utils;

import java.util.Scanner;

public class ConsoleUtil {
    // ANSI colors
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";
    
    // Bold Colors
    public static final String ANSI_BOLD = "\u001B[1m";
    public static final String ANSI_GREEN_BOLD = "\u001B[1;32m";
    public static final String ANSI_CYAN_BOLD = "\u001B[1;36m";
    public static final String ANSI_YELLOW_BOLD = "\u001B[1;33m";
    public static final String ANSI_WHITE_BOLD = "\u001B[1;37m";

    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public static void printHeader(String title) {
        int width = 76;
        printBoxTop(width);
        printBoxCenteredText(title, ANSI_GREEN_BOLD, width);
        printBoxBottom(width);
        System.out.println();
    }

    public static void printDoubleBorder(int width) {
        System.out.println(ANSI_CYAN + "═".repeat(width) + ANSI_RESET);
    }

    public static void printSingleBorder(int width) {
        System.out.println(ANSI_CYAN + "─".repeat(width) + ANSI_RESET);
    }

    public static void printCentered(String formattedText, int rawLength, int width) {
        int padding = (width - rawLength) / 2;
        if (padding < 0) padding = 0;
        System.out.println(" ".repeat(padding) + formattedText);
    }

    public static void pressEnterToContinue(Scanner scanner) {
        System.out.println("\n" + ANSI_YELLOW + "Press Enter to return to Dashboard..." + ANSI_RESET);
        scanner.nextLine();
    }

    public static String readString(Scanner scanner, String prompt) {
        System.out.print(ANSI_WHITE_BOLD + prompt + ANSI_RESET);
        return scanner.nextLine().trim();
    }

    public static String readPassword(Scanner scanner, String prompt) {
        System.out.print(ANSI_WHITE_BOLD + prompt + ANSI_RESET);
        return scanner.nextLine().trim();
    }

    public static int readMenuChoice(Scanner scanner, int min, int max) {
        while (true) {
            System.out.print(ANSI_WHITE_BOLD + "Enter your choice (" + min + "-" + max + "): " + ANSI_RESET);
            String input = scanner.nextLine().trim();
            try {
                int choice = Integer.parseInt(input);
                if (choice >= min && choice <= max) {
                    return choice;
                }
            } catch (NumberFormatException ignored) {}
            System.out.println(ANSI_RED + "Invalid choice! Please enter a number between " + min + " and " + max + "." + ANSI_RESET);
        }
    }

    public static void printBoxTop(int contentWidth) {
        System.out.println(ANSI_CYAN + "╔" + "═".repeat(contentWidth) + "╗" + ANSI_RESET);
    }

    public static void printBoxBottom(int contentWidth) {
        System.out.println(ANSI_CYAN + "╚" + "═".repeat(contentWidth) + "╝" + ANSI_RESET);
    }

    public static void printBoxDivider(int contentWidth) {
        System.out.println(ANSI_CYAN + "╟" + "─".repeat(contentWidth) + "╢" + ANSI_RESET);
    }

    public static void printBoxLine(String label, String value, String valueColor, int contentWidth) {
        int labelLen = label == null ? 0 : label.length();
        int valLen = value == null ? 0 : value.length();
        int baseLen = 2 + valLen;
        if (labelLen > 0) {
            baseLen += labelLen + 2; // For label and ": "
        }
        int spacesNeeded = contentWidth - baseLen;
        if (spacesNeeded < 0) spacesNeeded = 0;
        
        System.out.print(ANSI_CYAN + "║" + ANSI_RESET + "  ");
        if (labelLen > 0) {
            System.out.print(ANSI_WHITE_BOLD + label + ": " + ANSI_RESET);
        }
        System.out.print(valueColor + (value == null ? "" : value) + ANSI_RESET);
        System.out.println(" ".repeat(spacesNeeded) + ANSI_CYAN + "║" + ANSI_RESET);
    }

    public static void printBoxCenteredText(String text, String color, int contentWidth) {
        int padding = (contentWidth - text.length()) / 2;
        int rightPadding = contentWidth - text.length() - padding;
        if (padding < 0) padding = 0;
        if (rightPadding < 0) rightPadding = 0;
        System.out.println(ANSI_CYAN + "║" + ANSI_RESET + " ".repeat(padding) + color + text + ANSI_RESET + " ".repeat(rightPadding) + ANSI_CYAN + "║" + ANSI_RESET);
    }
}
