package com.sri.netbanking;

import com.sri.netbanking.ui.LoginPage;
import com.sri.netbanking.ui.Page;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        // Single Scanner instance used throughout the lifetime of the application
        try (Scanner scanner = new Scanner(System.in)) {
            Page currentPage = new LoginPage();
            
            while (currentPage != null) {
                currentPage = currentPage.handle(scanner);
            }
        } catch (Exception e) {
            System.err.println("An unexpected error occurred: " + e.getMessage());
        }
    }
}
