package com.sri.netbanking.ui;

import com.sri.netbanking.utils.ConsoleUtil;
import java.util.Scanner;

public class LoginHistoryPage implements Page {
    @Override
    public Page handle(Scanner scanner) {
        ConsoleUtil.clearScreen();
        ConsoleUtil.printHeader("LOGIN HISTORY");

        System.out.println(ConsoleUtil.ANSI_YELLOW + "Displaying last 4 login activity logs:\n" + ConsoleUtil.ANSI_RESET);

        System.out.println(ConsoleUtil.ANSI_CYAN + "┌────────────┬─────────────┬─────────────┬──────────────────────┬──────────────┐" + ConsoleUtil.ANSI_RESET);
        System.out.println(ConsoleUtil.ANSI_CYAN + "│" + ConsoleUtil.ANSI_WHITE_BOLD + "    Date    " + ConsoleUtil.ANSI_CYAN + "│" + ConsoleUtil.ANSI_WHITE_BOLD + " Login Time  " + ConsoleUtil.ANSI_CYAN + "│" + ConsoleUtil.ANSI_WHITE_BOLD + " Logout Time " + ConsoleUtil.ANSI_CYAN + "│" + ConsoleUtil.ANSI_WHITE_BOLD + "   Session Duration   " + ConsoleUtil.ANSI_CYAN + "│" + ConsoleUtil.ANSI_WHITE_BOLD + "    Status    " + ConsoleUtil.ANSI_CYAN + "│" + ConsoleUtil.ANSI_RESET);
        System.out.println(ConsoleUtil.ANSI_CYAN + "├────────────┼─────────────┼─────────────┼──────────────────────┼──────────────┤" + ConsoleUtil.ANSI_RESET);
        System.out.println(ConsoleUtil.ANSI_CYAN + "│" + ConsoleUtil.ANSI_RESET + " 2026-07-22 " + ConsoleUtil.ANSI_CYAN + "│" + ConsoleUtil.ANSI_RESET + " 12:28:40 PM " + ConsoleUtil.ANSI_CYAN + "│" + ConsoleUtil.ANSI_RESET + " 12:35:10 PM " + ConsoleUtil.ANSI_CYAN + "│" + ConsoleUtil.ANSI_RESET + " 6 mins 30 secs       " + ConsoleUtil.ANSI_CYAN + "│" + ConsoleUtil.ANSI_GREEN + " SUCCESS      " + ConsoleUtil.ANSI_CYAN + "│" + ConsoleUtil.ANSI_RESET);
        System.out.println(ConsoleUtil.ANSI_CYAN + "│" + ConsoleUtil.ANSI_RESET + " 2026-07-21 " + ConsoleUtil.ANSI_CYAN + "│" + ConsoleUtil.ANSI_RESET + " 09:12:05 AM " + ConsoleUtil.ANSI_CYAN + "│" + ConsoleUtil.ANSI_RESET + " 10:15:30 AM " + ConsoleUtil.ANSI_CYAN + "│" + ConsoleUtil.ANSI_RESET + " 1 hr 3 mins 25 secs  " + ConsoleUtil.ANSI_CYAN + "│" + ConsoleUtil.ANSI_GREEN + " SUCCESS      " + ConsoleUtil.ANSI_CYAN + "│" + ConsoleUtil.ANSI_RESET);
        System.out.println(ConsoleUtil.ANSI_CYAN + "│" + ConsoleUtil.ANSI_RESET + " 2026-07-21 " + ConsoleUtil.ANSI_CYAN + "│" + ConsoleUtil.ANSI_RESET + " 09:11:42 AM " + ConsoleUtil.ANSI_CYAN + "│" + ConsoleUtil.ANSI_RESET + "     N/A     " + ConsoleUtil.ANSI_CYAN + "│" + ConsoleUtil.ANSI_RESET + " N/A                  " + ConsoleUtil.ANSI_CYAN + "│" + ConsoleUtil.ANSI_RED + " FAILED       " + ConsoleUtil.ANSI_CYAN + "│" + ConsoleUtil.ANSI_RESET);
        System.out.println(ConsoleUtil.ANSI_CYAN + "│" + ConsoleUtil.ANSI_RESET + " 2026-07-19 " + ConsoleUtil.ANSI_CYAN + "│" + ConsoleUtil.ANSI_RESET + " 06:45:10 PM " + ConsoleUtil.ANSI_CYAN + "│" + ConsoleUtil.ANSI_RESET + " 07:12:00 PM " + ConsoleUtil.ANSI_CYAN + "│" + ConsoleUtil.ANSI_RESET + " 26 mins 50 secs      " + ConsoleUtil.ANSI_CYAN + "│" + ConsoleUtil.ANSI_GREEN + " SUCCESS      " + ConsoleUtil.ANSI_CYAN + "│" + ConsoleUtil.ANSI_RESET);
        System.out.println(ConsoleUtil.ANSI_CYAN + "└────────────┴─────────────┴─────────────┴──────────────────────┴──────────────┘" + ConsoleUtil.ANSI_RESET);

        System.out.println("\n" + ConsoleUtil.ANSI_WHITE_BOLD + "Select an option:" + ConsoleUtil.ANSI_RESET);
        System.out.println("1. Clear History");
        System.out.println("2. Back to Dashboard");
        System.out.println();

        int choice = ConsoleUtil.readMenuChoice(scanner, 1, 2);

        if (choice == 1) {
            ConsoleUtil.clearScreen();
            System.out.println(ConsoleUtil.ANSI_CYAN + "========================================" + ConsoleUtil.ANSI_RESET);
            System.out.println(ConsoleUtil.ANSI_GREEN_BOLD + "             CLEAR HISTORY              " + ConsoleUtil.ANSI_RESET);
            System.out.println(ConsoleUtil.ANSI_CYAN + "========================================" + ConsoleUtil.ANSI_RESET);
            System.out.println("\nAre you sure you want to clear all login history?\n");
            System.out.println("1. Yes");
            System.out.println("2. No");
            System.out.println();
            
            int confirm = ConsoleUtil.readMenuChoice(scanner, 1, 2);
            if (confirm == 1) {
                System.out.println("\n" + ConsoleUtil.ANSI_GREEN + "Login history cleared successfully." + ConsoleUtil.ANSI_RESET);
                System.out.println(ConsoleUtil.ANSI_YELLOW + "Press Enter to return to Login History..." + ConsoleUtil.ANSI_RESET);
                scanner.nextLine();
                return this;
            } else {
                return this;
            }
        } else {
            return new DashboardPage();
        }
    }
}
