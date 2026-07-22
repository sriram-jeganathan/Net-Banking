package com.sri.netbanking.ui;

import com.sri.netbanking.utils.ConsoleUtil;
import java.util.Scanner;

public class DashboardPage implements Page {
    @Override
    public Page handle(Scanner scanner) {
        ConsoleUtil.clearScreen();
        ConsoleUtil.printHeader("CUSTOMER DASHBOARD");

        System.out.println(ConsoleUtil.ANSI_WHITE_BOLD + "Select an option from the menu below:" + ConsoleUtil.ANSI_RESET);
        System.out.println("1. Account Summary");
        System.out.println("2. Transfer Money");
        System.out.println("3. Transaction History");
        System.out.println("4. Login History");
        System.out.println("5. Bank Statement");
        System.out.println("6. Logout");
        System.out.println();

        int choice = ConsoleUtil.readMenuChoice(scanner, 1, 6);

        switch (choice) {
            case 1:
                return new AccountSummaryPage();
            case 2:
                return new TransferPage();
            case 3:
                return new TransactionHistoryPage();
            case 4:
                return new LoginHistoryPage();
            case 5:
                return new StatementPage();
            case 6:
                System.out.println("\n" + ConsoleUtil.ANSI_GREEN + "Logging out..." + ConsoleUtil.ANSI_RESET);
                try { Thread.sleep(600); } catch (InterruptedException ignored) {}
                return new LoginPage();
            default:
                return this;
        }
    }
}
