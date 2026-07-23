package com.sri.netbanking.ui;

import com.sri.netbanking.utils.ConsoleUtil;
import com.sri.netbanking.utils.UserStore;
import java.util.Scanner;

public class LoginPage implements Page {
    @Override
    public Page handle(Scanner scanner) {
        ConsoleUtil.clearScreen();
        ConsoleUtil.printHeader("NET BANKING SYSTEM");

        System.out.println(ConsoleUtil.ANSI_YELLOW + "Welcome to the Net Banking System portal." + ConsoleUtil.ANSI_RESET);
        System.out.println("Please choose an option to proceed:\n");
        System.out.println("1. Login");
        System.out.println("2. Create Account");
        System.out.println("3. Exit");
        System.out.println();

        int choice = ConsoleUtil.readMenuChoice(scanner, 1, 3);

        if (choice == 1) {
            System.out.println();
            int width = 76;
            ConsoleUtil.printBoxTop(width);
            ConsoleUtil.printBoxCenteredText("SECURE USER SIGN IN", ConsoleUtil.ANSI_CYAN_BOLD, width);
            ConsoleUtil.printBoxBottom(width);
            System.out.println();
            String username = ConsoleUtil.readString(scanner, "Enter Username: ");
            String password = ConsoleUtil.readPassword(scanner, "Enter Password: ");
            
            if (UserStore.authenticate(username, password)) {
                System.out.println("\n" + ConsoleUtil.ANSI_GREEN + "✔ Login Successful! Press Enter to go to Dashboard..." + ConsoleUtil.ANSI_RESET);
                scanner.nextLine();
                return new DashboardPage();
            } else {
                System.out.println("\n" + ConsoleUtil.ANSI_RED + "✘ Invalid Username or Password! Press Enter to try again..." + ConsoleUtil.ANSI_RESET);
                scanner.nextLine();
                return this;
            }
        } else if (choice == 2) {
            return new CreateUserPage();
        } else {
            System.out.println("\n" + ConsoleUtil.ANSI_YELLOW + "Thank you for using Net Banking System. Goodbye!" + ConsoleUtil.ANSI_RESET);
            return null; // Exits the application
        }
    }
}
