package com.sri.netbanking.ui;

import com.sri.netbanking.utils.ConsoleUtil;
import java.util.Scanner;

public class StatementPage implements Page {
    @Override
    public Page handle(Scanner scanner) {
        ConsoleUtil.clearScreen();
        ConsoleUtil.printHeader("BANK STATEMENT");

        System.out.println(ConsoleUtil.ANSI_WHITE_BOLD + "Select statement duration:" + ConsoleUtil.ANSI_RESET);
        System.out.println("1. Last 7 Days");
        System.out.println("2. Current Month");
        System.out.println("3. Custom Date Range");
        System.out.println();

        int choice = ConsoleUtil.readMenuChoice(scanner, 1, 3);
        System.out.println();

        if (choice == 3) {
            ConsoleUtil.readString(scanner, "Enter Start Date (YYYY-MM-DD): ");
            ConsoleUtil.readString(scanner, "Enter End Date (YYYY-MM-DD): ");
            System.out.println();
        }

        int width = 76;
        ConsoleUtil.printBoxTop(width);
        ConsoleUtil.printBoxCenteredText("BANK STATEMENT GENERATION", ConsoleUtil.ANSI_YELLOW_BOLD, width);
        ConsoleUtil.printBoxDivider(width);
        ConsoleUtil.printBoxCenteredText("STATUS: FEATURE UNDER DEVELOPMENT", ConsoleUtil.ANSI_RED + ConsoleUtil.ANSI_BOLD, width);
        ConsoleUtil.printBoxCenteredText("PDF & CSV statement download will be", ConsoleUtil.ANSI_RESET, width);
        ConsoleUtil.printBoxCenteredText("available in the next project phase.", ConsoleUtil.ANSI_RESET, width);
        ConsoleUtil.printBoxBottom(width);

        ConsoleUtil.pressEnterToContinue(scanner);
        return new DashboardPage();
    }
}
