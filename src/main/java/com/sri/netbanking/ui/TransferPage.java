package com.sri.netbanking.ui;

import com.sri.netbanking.utils.ConsoleUtil;
import java.util.Scanner;

public class TransferPage implements Page {
    @Override
    public Page handle(Scanner scanner) {
        ConsoleUtil.clearScreen();
        ConsoleUtil.printHeader("TRANSFER FUNDS");

        System.out.println(ConsoleUtil.ANSI_YELLOW + "Initiate a mock funds transfer below:\n" + ConsoleUtil.ANSI_RESET);
        
        String recipient = ConsoleUtil.readString(scanner, "Enter Recipient Account Number: ");
        String amount = ConsoleUtil.readString(scanner, "Enter Amount (INR): ");
        String remarks = ConsoleUtil.readString(scanner, "Enter Remarks (optional): ");

        System.out.println();
        int width = 76;
        ConsoleUtil.printBoxTop(width);
        ConsoleUtil.printBoxCenteredText("TRANSACTION REVIEW", ConsoleUtil.ANSI_YELLOW_BOLD, width);
        ConsoleUtil.printBoxDivider(width);
        ConsoleUtil.printBoxLine("Recipient", recipient, ConsoleUtil.ANSI_RESET, width);
        ConsoleUtil.printBoxLine("Amount", "Rs. " + amount, ConsoleUtil.ANSI_RESET, width);
        ConsoleUtil.printBoxLine("Remarks", remarks, ConsoleUtil.ANSI_RESET, width);
        ConsoleUtil.printBoxDivider(width);
        ConsoleUtil.printBoxCenteredText("STATUS: FEATURE UNDER DEVELOPMENT", ConsoleUtil.ANSI_RED + ConsoleUtil.ANSI_BOLD, width);
        ConsoleUtil.printBoxCenteredText("No real funds will be transferred.", ConsoleUtil.ANSI_RED, width);
        ConsoleUtil.printBoxBottom(width);

        ConsoleUtil.pressEnterToContinue(scanner);
        return new DashboardPage();
    }
}
