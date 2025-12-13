package com.detonomics.budgettuner.backend.mainapplicationfeatures;

import java.util.ArrayList;
import java.util.Scanner;
import java.nio.charset.StandardCharsets;

// Η κεντρική κλάση της εφαρμογής
public final class App {

    private App() {
        throw new AssertionError("Utility class");
    }

    public static void main(final String[] args) {
        Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);
        System.out.println();
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println("Καλωσορίσατε στο Budget Tuner!");
        System.out.println("Το εργαλείο διαχείρισης προϋπολογισμών");
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

        System.out.println("Σύνολο αποθηκευμένων στοιχείων στη Βάση:");
        SqlSequence statistics = BudgetLoader.loadSqliteSequence();
        System.out.printf(
                "- Προϋπολογισμοί: %d%n- Κατηγορίες Εσόδων: %d%n"
                        + "- Κατηγορίες Εξόδων: %d%n- Υπουργεία: %d%n"
                        + "- Έξοδα Υπουργείων: %d%n",
                statistics.getBudgets(), statistics.getRevenueCategories(),
                statistics.getExpenseCategories(), statistics.getMinistries(),
                statistics.getMinistryExpenses());
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

        // Φόρτωση λίστας ετών προϋπολογισμού
        ArrayList<Integer> years = BudgetLoader.loadBudgetYearsList();
        boolean programrunning = true;
        boolean menurunning = true;
        int year;
        int choice;

        while (programrunning) {
            // === ΕΙΣΑΓΩΓΗ ΕΤΟΥΣ ===
            do {
                System.out.println("Διαθέσιμα Έτη στη Βάση: ");
                for (int y : years) {
                    System.out.println("- " + y);
                }
                System.out.print(
                        "Εισάγετε το έτος προϋπολογισμού, 0 για έξοδο "
                                + "ή 1 για εισαγωγή έτους: ");

                while (!scanner.hasNextInt()) {
                    System.out.println(
                            "Άκυρη είσοδος. Παρακαλώ εισάγετε έναν "
                                    + "έγκυρο αριθμό έτους.");
                    scanner.nextLine();
                }

                year = scanner.nextInt();
                scanner.nextLine();

                if (years.contains(year)) {
                    System.out.println("Φορτώνεται ο προϋπολογισμός για το "
                            + "έτος " + year + "...");
                } else if (year == 0) {
                    System.out.println("Έξοδος από την εφαρμογή...");
                    programrunning = false;
                    menurunning = false;
                } else if (year == 1) {
                    System.out.println(
                            "Εισαγωγή νέου έτους προϋπολογισμού στη βάση...");
                    System.out.print(
                            "Εισάγετε τη διαδρομή του αρχείου PDF "
                                    + "προϋπολογισμού (ή 0 για ακύρωση "
                                    + "εισαγωγής νέου έτους): ");
                    String pdfPath = scanner.nextLine();
                    if (pdfPath.equals("0")) {
                        System.out.println("Ακύρωση εισαγωγής νέου έτους.");
                        continue;
                    }
                    try {
                        BudgetModifier.insertNewBudgetYear(pdfPath);
                        // Ενημέρωση της λίστας ετών μετά την εισαγωγή
                        years = BudgetLoader.loadBudgetYearsList();
                        System.out.println(
                                "Η εισαγωγή ολοκληρώθηκε με επιτυχία.");
                    } catch (Exception e) {
                        System.out.println(
                                "Σφάλμα κατά την εισαγωγή του "
                                        + "προϋπολογισμού: " + e.getMessage());
                    }
                } else {
                    System.out.println("Το έτος " + year
                            + " δεν βρέθηκε στη βάση δεδομένων. "
                            + "Παρακαλώ εισάγετε ένα άλλο έτος.");
                }
            } while (!years.contains(year) && programrunning);

            // Βρισκει το ID για το συγκεκριμένο έτος
            int budgetID = BudgetLoader.loadBudgetIDByYear(year);
            BudgetYear budget = BudgetLoader.loadBudgetYear(budgetID);

            // === ΚΕΝΤΡΙΚΟ ΜΕΝΟΥ ===
            if (!programrunning) {
                break;
            }
            menurunning = true;
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
                    System.out.println(
                            "\nΆκυρη επιλογή. Παρακαλώ εισάγετε έναν "
                                    + "αριθμό.");
                    scanner.nextLine();
                    choice = -1;
                }

                switch (choice) {
                    case 1:
                        System.out.println("\n--- ΣΥΝΟΨΗ ΠΡΟΫΠΟΛΟΓΙΣΜΟΥ ("
                                + year + ") ---");
                        System.out.println(budget.getSummary());
                        break;
                    case 2:
                        System.out.println("\n--- ΕΣΟΔΑ ΠΡΟΫΠΟΛΟΓΙΣΜΟΥ ("
                                + year + ") ---");
                        System.out.println(BudgetFormatter
                                .getFormattedRevenues(budget.getRevenues()));
                        break;
                    case 3:
                        System.out.println("\n--- ΕΞΟΔΑ ΠΡΟΫΠΟΛΟΓΙΣΜΟΥ ("
                                + year + ") ---");
                        System.out.println(BudgetFormatter
                                .getFormattedExpenditures(
                                        budget.getExpenses()));
                        break;
                    case 4:
                        System.out.println("\n--- ΦΟΡΕΙΣ ΠΡΟΫΠΟΛΟΓΙΣΜΟΥ ("
                                + year + ") ---");
                        System.out.println(BudgetFormatter
                                .getFormattedMinistries(
                                        budget.getMinistries()));
                        break;
                    case 5:
                        System.out.println(
                                "\n--- ΔΑΠΑΝΕΣ ΦΟΡΕΩΝ ΠΡΟΫΠΟΛΟΓΙΣΜΟΥ ("
                                        + year + ") ---");
                        System.out.println(BudgetFormatter
                                .getFormattedMinistryExpenses(
                                        budget.getMinistries(),
                                        budget.getExpenses(),
                                        budget.getMinistryExpenses()));
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
