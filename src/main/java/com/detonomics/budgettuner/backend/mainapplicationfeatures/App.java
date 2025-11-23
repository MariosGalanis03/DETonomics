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

        int year;
        int choice;

        // === ΕΙΣΑΓΩΓΗ ΕΤΟΥΣ ===
        do {
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
        int yearID = budgetManager.getBudgetIDByYear(year);


        // === ΚΕΝΤΡΙΚΟ ΜΕΝΟΥ ===
        do {
            System.out.println("\n--- ΚΕΝΤΡΙΚΟ ΜΕΝΟΥ ---");
            System.out.println("1. Προβολή Συνολικών Στοιχείων (Σύνοψη)");
            System.out.println("2. Προβολή Εσόδων");
            System.out.println("3. Προβολή Εξόδων");
            System.out.println("4. Προβολή Φορέων");
            System.out.println("5. Έξοδος");
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
                    Summary summary = budgetManager.loadSummary(yearID);
                    if (summary != null) {
                        System.out.println("\n--- ΣΥΝΟΨΗ ΠΡΟΫΠΟΛΟΓΙΣΜΟΥ " + year + " ---");
                        System.out.println(summary);
                    } else {
                        System.out.println("Δεν βρέθηκαν στοιχεία σύνοψης για το έτος " + year);
                    }
                    break;
                case 2:
                    // κώδικας για εμφάνιση εσόδων
                    break;
                case 3:
                    // κώδικας για εμφάνιση εξόδων
                    break;
                case 4:
                    // κώδικας για εμφάνιση φορέων
                    break;
                case 5:
                    System.out.println("Έξοδος...");
                    break;
                default:
                    System.out.println("Μη έγκυρη επιλογή.");
            }

        } while (choice != 5);

        scanner.close();
    }
}
