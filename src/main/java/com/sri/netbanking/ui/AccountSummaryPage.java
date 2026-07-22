package com.sri.netbanking.ui;

import com.sri.netbanking.utils.ConsoleUtil;
import java.util.Scanner;

public class AccountSummaryPage implements Page {
    @Override
    public Page handle(Scanner scanner) {
        ConsoleUtil.clearScreen();
        ConsoleUtil.printHeader("ACCOUNT SUMMARY");

        int width = 76;
        ConsoleUtil.printBoxTop(width);
        ConsoleUtil.printBoxCenteredText("DEMO ACCOUNT DETAILS", ConsoleUtil.ANSI_YELLOW_BOLD, width);
        ConsoleUtil.printBoxDivider(width);
        ConsoleUtil.printBoxLine("Account Holder Name", "Sriram Jeganathan", ConsoleUtil.ANSI_GREEN, width);
        ConsoleUtil.printBoxLine("Account Number", "1204251040966", ConsoleUtil.ANSI_RESET, width);
        ConsoleUtil.printBoxLine("Account Type", "Savings Account", ConsoleUtil.ANSI_RESET, width);
        ConsoleUtil.printBoxLine("Available Balance", "Rs. 1,84,520.50", ConsoleUtil.ANSI_GREEN_BOLD, width);
        ConsoleUtil.printBoxLine("Branch", "Chennai Main Branch", ConsoleUtil.ANSI_RESET, width);
        ConsoleUtil.printBoxLine("IFSC Code", "SRIB0001204", ConsoleUtil.ANSI_RESET, width);
        ConsoleUtil.printBoxBottom(width);

        ConsoleUtil.pressEnterToContinue(scanner);
        return new DashboardPage();
    }
}
