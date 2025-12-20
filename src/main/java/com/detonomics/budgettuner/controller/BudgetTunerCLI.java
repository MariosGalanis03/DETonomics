package com.detonomics.budgettuner.controller;

import java.util.ArrayList;
import java.util.Scanner;
import java.nio.charset.StandardCharsets;

import com.detonomics.budgettuner.model.BudgetYear;
import com.detonomics.budgettuner.model.SqlSequence;
import com.detonomics.budgettuner.dao.BudgetYearDao;
import com.detonomics.budgettuner.dao.SqlSequenceDao;
import com.detonomics.budgettuner.util.BudgetFormatter;

/**
 * The main entry point for the Budget Tuner command-line interface.
 */
public final class BudgetTunerCLI {

    private BudgetTunerCLI() {
        throw new AssertionError("Utility class");
    }

    /**
     * The main method that starts the application.
     */
    public static void main(final String[] args) {
        try (Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8)) {
            System.out.println();
            System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
            System.out.println("Καλωσορίσατε στο Budget Tuner!");
            System.out.println("Το εργαλείο διαχείρισης προϋπολογισμών");
            System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

            System.out.println("Σύνολο αποθηκευμένων στοιχείων στη Βάση:");
            SqlSequence statistics = SqlSequenceDao.loadSqliteSequence();
            System.out.printf(
                    "- Προϋπολογισμοί: %d%n- Κατηγορίες Εσόδων: %d%n"
                            + "- Κατηγορίες Εξόδων: %d%n- Υπουργεία: %d%n"
                            + "- Έξοδα Υπουργείων: %d%n",
                    statistics.getBudgets(), statistics.getRevenueCategories(),
                    statistics.getExpenseCategories(), statistics.getMinistries(),
                    statistics.getMinistryExpenses());
            System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

            // Load list of budget years
            ArrayList<Integer> years = BudgetYearDao.loadBudgetYearsList();
            boolean mainMenurunning = true;
            int choice;

            while (mainMenurunning) {
                System.out.println("\n--- ΚΥΡΙΟ ΜΕΝΟΥ ---");
                System.out.println("1. Επιλογή Έτους για Προβολή");
                System.out.println("2. Σύγκριση Δύο Ετών");
                System.out.println("3. Εισαγωγή Νέου Έτους");
                System.out.println("0. Έξοδος");
                System.out.print("Επιλογή: ");

                if (scanner.hasNextInt()) {
                    choice = scanner.nextInt();
                    scanner.nextLine(); // Consume newline
                } else {
                    System.out.println("\nΆκυρη επιλογή. Παρακαλώ εισάγετε έναν αριθμό.");
                    scanner.nextLine();
                    continue;
                }

                switch (choice) {
                    case 1:
                        // 1. Select Year
                        int year = selectYear(scanner, years, "Εισάγετε το έτος προϋπολογισμού: ");
                        System.out.println("Φορτώνεται ο προϋπολογισμός για το έτος " + year + "...");
                        
                        int budgetID = BudgetYearDao.loadBudgetIDByYear(year);
                        BudgetYear budget = BudgetYearDao.loadBudgetYear(budgetID);

                        // 2. Enter View Menu
                        // Returns false if the user chose "Exit" (7), true if "Change Year" (6)
                        boolean keepAppRunning = handleViewBudgetMenu(scanner, budget, year);
                        if (!keepAppRunning) {
                            mainMenurunning = false;
                        }
                        break;

                    case 2:
                        System.out.println("Σύγκριση δύο ετών προϋπολογισμού...");
                        
                        // 1. Select Years
                        int year1 = selectYear(scanner, years, "Εισάγετε το πρώτο έτος για σύγκριση: ");
                        int budgetID1 = BudgetYearDao.loadBudgetIDByYear(year1);
                        BudgetYear budget1 = BudgetYearDao.loadBudgetYear(budgetID1);

                        int year2 = selectYear(scanner, years, "Εισάγετε το δεύτερο έτος για σύγκριση: ");
                        int budgetID2 = BudgetYearDao.loadBudgetIDByYear(year2);
                        BudgetYear budget2 = BudgetYearDao.loadBudgetYear(budgetID2);

                        // 2. Enter Comparison Menu
                        handleCompareBudgetsMenu(scanner, budget1, budget2, year1, year2);
                        break;

                    case 3:
                        // Insert new year
                        System.out.println("Εισαγωγή νέου έτους προϋπολογισμού στη βάση...");
                        System.out.print("Εισάγετε τη διαδρομή του αρχείου PDF προϋπολογισμού " 
                                + "(ή 0 για ακύρωση): ");
                        String pdfPath = scanner.nextLine();
                        if (!pdfPath.equals("0")) {
                            try {
                                BudgetYearDao.insertNewBudgetYear(pdfPath);
                                // Reload years list after insertion
                                years = BudgetYearDao.loadBudgetYearsList();
                                System.out.println("Η εισαγωγή ολοκληρώθηκε με επιτυχία.");
                            } catch (Exception e) {
                                System.out.println("Σφάλμα κατά την εισαγωγή: " + e.getMessage());
                            }
                        } else {
                            System.out.println("Ακύρωση εισαγωγής νέου έτους.");
                        }
                        break;

                    case 0:
                        System.out.println("Έξοδος από την εφαρμογή.");
                        mainMenurunning = false;
                        break;

                    default:
                        System.out.println("Μη έγκυρη επιλογή.");
                        break;
                }
            }
        }
    }

    /**
     * Helper method to prompt the user to select a valid year from the list.
     */
    private static int selectYear(Scanner scanner, ArrayList<Integer> availableYears, String message) {
        int selectedYear;
        do {
            System.out.println("Διαθέσιμα Έτη στη Βάση: ");
            for (int y : availableYears) {
                System.out.println("- " + y);
            }
            System.out.print(message);
            
            while (!scanner.hasNextInt()) {
                System.out.println("Άκυρη είσοδος. Παρακαλώ εισάγετε έναν έγκυρο αριθμό έτους.");
                scanner.nextLine();
            }
            selectedYear = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            if (!availableYears.contains(selectedYear)) {
                System.out.println("Το έτος " + selectedYear 
                        + " δεν βρέθηκε στη βάση δεδομένων. Παρακαλώ εισάγετε ένα άλλο έτος.");
            }
        } while (!availableYears.contains(selectedYear));
        
        return selectedYear;
    }

    /**
     * Handles the menu for viewing a specific budget year.
     * @return true if the application should continue running, false if the user selected Exit.
     */
    private static boolean handleViewBudgetMenu(Scanner scanner, BudgetYear budget, int year) {
        boolean menuRunning = true;
        boolean keepAppRunning = true;

        while (menuRunning) {
            System.out.println("\n--- ΚΕΝΤΡΙΚΟ ΜΕΝΟΥ ---");
            System.out.println("1. Προβολή Συνολικών Στοιχείων (Σύνοψη)");
            System.out.println("2. Προβολή Εσόδων");
            System.out.println("3. Προβολή Εξόδων");
            System.out.println("4. Προβολή Φορέων");
            System.out.println("5. Προβολή Δαπανών Φορέων");
            System.out.println("6. Αλλαγή Έτους Προϋπολογισμού");
            System.out.println("7. Επιστροφή στο Κύριο Μενού");
            System.out.println("8. Έξοδος");
            System.out.println("-----------------------");
            System.out.print("Επιλογή: ");

            int choice;
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
                    System.out.println("\n--- ΣΥΝΟΨΗ ΠΡΟϋΠΟΛΟΓΙΣΜΟΥ (" + year + ") ---");
                    System.out.println(budget.getSummary());
                    break;
                case 2:
                    System.out.println("\n--- ΕΣΟΔΑ ΠΡΟϋΠΟΛΟΓΙΣΜΟΥ (" + year + ") ---");
                    System.out.println(BudgetFormatter.getFormattedRevenues(budget.getRevenues()));
                    break;
                case 3:
                    System.out.println("\n--- ΕΞΟΔΑ ΠΡΟϋΠΟΛΟΓΙΣΜΟΥ (" + year + ") ---");
                    System.out.println(BudgetFormatter.getFormattedExpenditures(budget.getExpenses()));
                    break;
                case 4:
                    System.out.println("\n--- ΦΟΡΕΙΣ ΠΡΟϋΠΟΛΟΓΙΣΜΟΥ (" + year + ") ---");
                    System.out.println(BudgetFormatter.getFormattedMinistries(budget.getMinistries()));
                    break;
                case 5:
                    System.out.println("\n--- ΔΑΠΑΝΕΣ ΦΟΡΕΩΝ ΠΡΟϋΠΟΛΟΓΙΣΜΟΥ (" + year + ") ---");
                    System.out.println(BudgetFormatter.getFormattedMinistryExpenses(
                            budget.getMinistries(), budget.getExpenses(), budget.getMinistryExpenses()));
                    break;
                case 6:
                    System.out.println("Αλλαγή έτους προϋπολογισμού...");
                    menuRunning = false;
                    break;
                case 7:
                    System.out.println("Επιστροφή στο Κύριο Μενού...");
                    menuRunning = false;
                    break;
                case 8:
                    System.out.println("Έξοδος...");
                    menuRunning = false;
                    keepAppRunning = false;
                    break;
                default:
                    System.out.println("Μη έγκυρη επιλογή.");
                    break;
            }
        }
        return keepAppRunning;
    }

    /**
     * Handles the menu for comparing two budget years.
     */
    private static void handleCompareBudgetsMenu(Scanner scanner, BudgetYear budget1,
                                                 BudgetYear budget2, int year1, int year2) {
        boolean compareRunning = true;
        while (compareRunning) {
            System.out.println("\n--- ΜΕΝΟΥ ΣΥΓΚΡΙΣΗΣ ---");
            System.out.println("1. Σύγκριση Συνολικών Στοιχείων (Σύνοψη)");
            System.out.println("2. Σύγκριση Εσόδων");
            System.out.println("3. Σύγκριση Εξόδων");
            System.out.println("4. Σύγκριση Φορέων");
            System.out.println("5. Επιστροφή στο Κύριο Μενού");
            System.out.print("Επιλογή: ");

            int compareChoice;
            if (scanner.hasNextInt()) {
                compareChoice = scanner.nextInt();
                scanner.nextLine();
            } else {
                System.out.println("\nΆκυρη επιλογή. Παρακαλώ εισάγετε έναν αριθμό.");
                scanner.nextLine();
                compareChoice = -1;
            }

            switch (compareChoice) {
                case 1:
                    String[] lines1 = budget1.getSummary().toString().split("\n");
                    String[] lines2 = budget2.getSummary().toString().split("\n");
                    System.out.println("\n" + String.format("%-80s", year1) + "|" + year2);
                    System.out.println(String.format("%-80s", "").replace(' ', '-') + "|" + String.format("%-80s", "").replace(' ', '-'));
                    for (int i = 0; i < Math.max(lines1.length, lines2.length); i++) {
                        String l1 = i < lines1.length ? lines1[i] : "";
                        String l2 = i < lines2.length ? lines2[i] : "";
                        System.out.println(String.format("%-80s", l1) + "|" + l2);
                    }
                    break;
                case 2:
                    System.out.println(BudgetFormatter.getFormattedComparativeRevenues(budget1.getRevenues(), budget2.getRevenues(), year1, year2));
                    break;
                case 3:
                    System.out.println(BudgetFormatter.getFormattedComparativeExpenditures(budget1.getExpenses(), budget2.getExpenses(), year1, year2));
                    break;
                case 4:
                    System.out.println(BudgetFormatter.getFormattedComparativeMinistries(budget1.getMinistries(), budget2.getMinistries(), year1, year2));
                    break;
                case 5:
                    compareRunning = false;
                    break;
                default:
                    System.out.println("Μη έγκυρη επιλογή.");
                    break;
            }
        }
    }
}
