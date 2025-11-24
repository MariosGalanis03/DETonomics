package com.detonomics.budgettuner.backend.mainapplicationfeatures;

import java.util.ArrayList;
import java.util.Scanner;

// Η κεντρική κλάση της εφαρμογής
public class App {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        BudgetManager budgetManager = new BudgetManager();

        // Φόρτωση λίστας ετών προϋπολογισμού
        ArrayList<Integer> years = budgetManager.loadBudgetYears();
        boolean programrunning = true;
        int year;
        int choice;

        while (programrunning) {
            // === ΕΙΣΑΓΩΓΗ ΕΤΟΥΣ ===
            do {
                System.out.println("Διαθέσιμα Έτη στη Βάση: " + years);
                System.out.print("Εισάγετε το έτος προϋπολογισμού: ");

                while (!scanner.hasNextInt()) {
                    System.out.println("Άκυρη είσοδος. Παρακαλώ εισάγετε έναν έγκυρο αριθμό έτους.");
                    scanner.nextLine();
                }

                year = scanner.nextInt();
                scanner.nextLine();

                if (years.contains(year)) {
                    System.out.println("Φορτώνεται ο προϋπολογισμός για το έτος " + year + "...");
                } else {
                    System.out.println("Το έτος " + year + " δεν βρέθηκε στη βάση δεδομένων. Παρακαλώ εισάγετε ένα άλλο έτος.");
                }
            } while (!years.contains(year));

            // Βρισκει το ID για το συγκεκριμένο έτος
            int budgetID = budgetManager.getBudgetIDByYear(year);
            BudgetYear budget = budgetManager.loadBudgetYear(budgetID);
            boolean menurunning = true;

            // === ΚΕΝΤΡΙΚΟ ΜΕΝΟΥ ===
            while (menurunning) {
                System.out.println("\n--- ΚΕΝΤΡΙΚΟ ΜΕΝΟΥ ---");
                System.out.println("1. Προβολή Συνολικών Στοιχείων (Σύνοψη)");
                System.out.println("2. Προβολή Εσόδων");
                System.out.println("3. Προβολή Εξόδων");
                System.out.println("4. Προβολή Φορέων");
                System.out.println("5. Προβολή Δαπανών Φορέων");
                System.out.println("6. Αλλαγή Έτους Προϋπολογισμού");
                System.out.println("7. Έξοδος");
                System.out.println("-----------------------");
                System.out.println("Επιλογή: ");

                if (scanner.hasNextInt()) {
                    choice = scanner.nextInt();
                    scanner.nextLine();
                } else {
                    System.out.println("\nΆκυρη επιλογή. Παρακαλώ εισάγετε έναν αριθμό.");
                    scanner.nextLine();
                    choice = -1;
                }

                switch (choice) {
                    case 1:
                            System.out.println("\n--- ΣΥΝΟΨΗ ΠΡΟΫΠΟΛΟΓΙΣΜΟΥ (" + year + ") ---");
                            System.out.println(budget.getSummary());
                        break;
                    case 2:
                        System.out.println("\n--- ΕΣΟΔΑ ΠΡΟΫΠΟΛΟΓΙΣΜΟΥ (" + year + ") ---");
                        System.out.println(BudgetFormatter.getFormattedRevenues(budget.getRevenues()));
                        break;
                    case 3:
                        System.out.println("\n--- ΕΞΟΔΑ ΠΡΟΫΠΟΛΟΓΙΣΜΟΥ (" + year + ") ---");
                        System.out.println(BudgetFormatter.getFormattedExpenditures(budget.getExpenses()));
                        break;
                    case 4:
                        System.out.println("\n--- ΦΟΡΕΙΣ ΠΡΟΫΠΟΛΟΓΙΣΜΟΥ (" + year + ") ---");
                        System.out.println(BudgetFormatter.getFormattedMinistries(budget.getMinistries()));
                        break;
                    case 5:
                        System.out.println("\n--- ΔΑΠΑΝΕΣ ΦΟΡΕΩΝ ΠΡΟΫΠΟΛΟΓΙΣΜΟΥ (" + year + ") ---");
                        System.out.println(BudgetFormatter.getFormattedMinistryExpenses(budget.getMinistries(), budget.getExpenses(), budget.getMinistryExpenses()));
                        break;
                    case 6:
                        System.out.println("Αλλαγή έτους προϋπολογισμού...");
                        menurunning = false;
                        break;
                    case 7:
                        System.out.println("Έξοδος...");
                        menurunning = false;
                        programrunning = false;
                        break;
                    default:
                        System.out.println("Μη έγκυρη επιλογή.");
                }
            }
        }

        scanner.close();
    }
}
