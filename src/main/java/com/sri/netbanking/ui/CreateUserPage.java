package com.sri.netbanking.ui;

import com.sri.netbanking.utils.ConsoleUtil;
import com.sri.netbanking.utils.UserStore;
import java.util.Scanner;
import java.util.regex.Pattern;

public class CreateUserPage implements Page {
    @Override
    public Page handle(Scanner scanner) {
        ConsoleUtil.clearScreen();
        ConsoleUtil.printHeader("CREATE NEW USER");

        System.out.println("Do you want to proceed with account creation?\n");
        System.out.println("1. Proceed");
        System.out.println("2. Back to Login Menu");
        System.out.println();
        
        int choice = ConsoleUtil.readMenuChoice(scanner, 1, 2);
        if (choice == 2) {
            return new LoginPage();
        }

        ConsoleUtil.clearScreen();
        ConsoleUtil.printHeader("CREATE NEW USER");
        System.out.println(ConsoleUtil.ANSI_YELLOW + "Please enter the details below to create an account:\n" + ConsoleUtil.ANSI_RESET);

        String fullName = ConsoleUtil.readString(scanner, "Enter Full Name: ");
        while (fullName.isEmpty()) {
            System.out.println(ConsoleUtil.ANSI_RED + "Full Name cannot be empty!" + ConsoleUtil.ANSI_RESET);
            fullName = ConsoleUtil.readString(scanner, "Enter Full Name: ");
        }

        String email = ConsoleUtil.readString(scanner, "Enter Email Address: ");
        while (email.isEmpty() || !email.contains("@")) {
            System.out.println(ConsoleUtil.ANSI_RED + "Invalid Email Address! Must contain '@'." + ConsoleUtil.ANSI_RESET);
            email = ConsoleUtil.readString(scanner, "Enter Email Address: ");
        }

        String phoneNumber = ConsoleUtil.readString(scanner, "Enter Phone Number: ");
        while (phoneNumber.isEmpty()) {
            System.out.println(ConsoleUtil.ANSI_RED + "Phone Number cannot be empty!" + ConsoleUtil.ANSI_RESET);
            phoneNumber = ConsoleUtil.readString(scanner, "Enter Phone Number: ");
        }

        String username = ConsoleUtil.readString(scanner, "Enter Username: ");
        while (true) {
            if (username.isEmpty()) {
                System.out.println(ConsoleUtil.ANSI_RED + "Username cannot be empty!" + ConsoleUtil.ANSI_RESET);
            } else if (UserStore.exists(username)) {
                System.out.println(ConsoleUtil.ANSI_RED + "Username already exists! Please choose another username." + ConsoleUtil.ANSI_RESET);
            } else {
                break;
            }
            username = ConsoleUtil.readString(scanner, "Enter Username: ");
        }

        String password = "";
        while (true) {
            System.out.println("\n" + ConsoleUtil.ANSI_WHITE_BOLD + "Password Requirements:" + ConsoleUtil.ANSI_RESET);
            System.out.println("  • Minimum 8 characters");
            System.out.println("  • At least one uppercase letter (A-Z)");
            System.out.println("  • At least one lowercase letter (a-z)");
            System.out.println("  • At least one digit (0-9)");
            System.out.println("  • At least one special character (e.g., !@#$%^&* etc.)");
            System.out.println();
            
            password = ConsoleUtil.readPassword(scanner, "Enter Password: ");
            
            StringBuilder errors = new StringBuilder();
            if (password.length() < 8) {
                errors.append("  • Must be at least 8 characters long\n");
            }
            if (!Pattern.compile("[A-Z]").matcher(password).find()) {
                errors.append("  • Must contain at least one uppercase letter (A-Z)\n");
            }
            if (!Pattern.compile("[a-z]").matcher(password).find()) {
                errors.append("  • Must contain at least one lowercase letter (a-z)\n");
            }
            if (!Pattern.compile("[0-9]").matcher(password).find()) {
                errors.append("  • Must contain at least one digit (0-9)\n");
            }
            if (!Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\",./<>?~`|\\\\]").matcher(password).find()) {
                errors.append("  • Must contain at least one special character\n");
            }

            if (errors.length() == 0) {
                String confirmPassword = ConsoleUtil.readPassword(scanner, "Confirm Password: ");
                if (password.equals(confirmPassword)) {
                    break;
                } else {
                    System.out.println(ConsoleUtil.ANSI_RED + "\nPasswords do not match! Please try again.\n" + ConsoleUtil.ANSI_RESET);
                }
            } else {
                System.out.println(ConsoleUtil.ANSI_RED + "\nPassword validation failed:\n" + errors.toString() + ConsoleUtil.ANSI_RESET);
            }
        }

        UserStore.register(username, password, fullName, email, phoneNumber);

        System.out.println();
        int width = 76;
        ConsoleUtil.printBoxTop(width);
        ConsoleUtil.printBoxCenteredText("ACCOUNT REGISTRATION SUMMARY", ConsoleUtil.ANSI_YELLOW_BOLD, width);
        ConsoleUtil.printBoxDivider(width);
        ConsoleUtil.printBoxLine("Full Name", fullName, ConsoleUtil.ANSI_RESET, width);
        ConsoleUtil.printBoxLine("Email Address", email, ConsoleUtil.ANSI_RESET, width);
        ConsoleUtil.printBoxLine("Phone Number", phoneNumber, ConsoleUtil.ANSI_RESET, width);
        ConsoleUtil.printBoxLine("Username", username, ConsoleUtil.ANSI_RESET, width);
        ConsoleUtil.printBoxLine("Password", "*".repeat(password.length()), ConsoleUtil.ANSI_RESET, width);
        ConsoleUtil.printBoxDivider(width);
        ConsoleUtil.printBoxCenteredText("STATUS: ACCOUNT CREATED SUCCESSFULLY", ConsoleUtil.ANSI_GREEN_BOLD, width);
        ConsoleUtil.printBoxBottom(width);

        System.out.println("\n" + ConsoleUtil.ANSI_GREEN + "✔ Account successfully registered! Press Enter to return to login..." + ConsoleUtil.ANSI_RESET);
        scanner.nextLine();
        return new LoginPage();
    }
}
