package com.sri.netbanking.ui;

import com.sri.netbanking.utils.ConsoleUtil;
import java.util.Scanner;

public class TransactionHistoryPage implements Page {
    @Override
    public Page handle(Scanner scanner) {
        ConsoleUtil.clearScreen();
        ConsoleUtil.printHeader("TRANSACTION HISTORY");

        System.out.println(ConsoleUtil.ANSI_YELLOW + "Displaying last 4 transaction records:\n" + ConsoleUtil.ANSI_RESET);

        System.out.println(ConsoleUtil.ANSI_CYAN + "┌──────────┬────────────────────────┬──────────────┬──────────┬──────────────┐" + ConsoleUtil.ANSI_RESET);
        System.out.println(ConsoleUtil.ANSI_CYAN + "│" + ConsoleUtil.ANSI_WHITE_BOLD + "   Date   " + ConsoleUtil.ANSI_CYAN + "│" + ConsoleUtil.ANSI_WHITE_BOLD + " Description            " + ConsoleUtil.ANSI_CYAN + "│" + ConsoleUtil.ANSI_WHITE_BOLD + " Ref No       " + ConsoleUtil.ANSI_CYAN + "│" + ConsoleUtil.ANSI_WHITE_BOLD + " Type     " + ConsoleUtil.ANSI_CYAN + "│" + ConsoleUtil.ANSI_WHITE_BOLD + " Amount       " + ConsoleUtil.ANSI_CYAN + "│" + ConsoleUtil.ANSI_RESET);
        System.out.println(ConsoleUtil.ANSI_CYAN + "├──────────┼────────────────────────┼──────────────┼──────────┼──────────────┤" + ConsoleUtil.ANSI_RESET);
        System.out.println(ConsoleUtil.ANSI_CYAN + "│" + ConsoleUtil.ANSI_RESET + "2026-07-20" + ConsoleUtil.ANSI_CYAN + "│" + ConsoleUtil.ANSI_RESET + " UPI Transfer           " + ConsoleUtil.ANSI_CYAN + "│" + ConsoleUtil.ANSI_RESET + " 61928374829  " + ConsoleUtil.ANSI_CYAN + "│" + ConsoleUtil.ANSI_RED + " DEBIT     " + ConsoleUtil.ANSI_CYAN + "│" + ConsoleUtil.ANSI_RED + " Rs. 1,200.00 " + ConsoleUtil.ANSI_CYAN + "│" + ConsoleUtil.ANSI_RESET);
        System.out.println(ConsoleUtil.ANSI_CYAN + "│" + ConsoleUtil.ANSI_RESET + "2026-07-18" + ConsoleUtil.ANSI_CYAN + "│" + ConsoleUtil.ANSI_RESET + " Salary Credit          " + ConsoleUtil.ANSI_CYAN + "│" + ConsoleUtil.ANSI_RESET + " 99828371239  " + ConsoleUtil.ANSI_CYAN + "│" + ConsoleUtil.ANSI_GREEN + " CREDIT    " + ConsoleUtil.ANSI_CYAN + "│" + ConsoleUtil.ANSI_GREEN + " Rs. 85,000.00" + ConsoleUtil.ANSI_CYAN + "│" + ConsoleUtil.ANSI_RESET);
        System.out.println(ConsoleUtil.ANSI_CYAN + "│" + ConsoleUtil.ANSI_RESET + "2026-07-15" + ConsoleUtil.ANSI_CYAN + "│" + ConsoleUtil.ANSI_RESET + " Electricity Bill       " + ConsoleUtil.ANSI_CYAN + "│" + ConsoleUtil.ANSI_RESET + " 10293847561  " + ConsoleUtil.ANSI_CYAN + "│" + ConsoleUtil.ANSI_RED + " DEBIT     " + ConsoleUtil.ANSI_CYAN + "│" + ConsoleUtil.ANSI_RED + " Rs. 3,450.00 " + ConsoleUtil.ANSI_CYAN + "│" + ConsoleUtil.ANSI_RESET);
        System.out.println(ConsoleUtil.ANSI_CYAN + "│" + ConsoleUtil.ANSI_RESET + "2026-07-12" + ConsoleUtil.ANSI_CYAN + "│" + ConsoleUtil.ANSI_RESET + " Refund Credit          " + ConsoleUtil.ANSI_CYAN + "│" + ConsoleUtil.ANSI_RESET + " 48291029384  " + ConsoleUtil.ANSI_CYAN + "│" + ConsoleUtil.ANSI_GREEN + " CREDIT    " + ConsoleUtil.ANSI_CYAN + "│" + ConsoleUtil.ANSI_GREEN + " Rs.   450.00 " + ConsoleUtil.ANSI_CYAN + "│" + ConsoleUtil.ANSI_RESET);
        System.out.println(ConsoleUtil.ANSI_CYAN + "└──────────┴────────────────────────┴──────────────┴──────────┴──────────────┘" + ConsoleUtil.ANSI_RESET);

        ConsoleUtil.pressEnterToContinue(scanner);
        return new DashboardPage();
    }
}
