package com.detonomics.budgettuner.controller;

import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Scanner;

import com.detonomics.budgettuner.model.BudgetYear;
import com.detonomics.budgettuner.model.SqlSequence;
import com.detonomics.budgettuner.service.BudgetDataService;
import com.detonomics.budgettuner.service.BudgetDataServiceImpl;
import com.detonomics.budgettuner.util.BudgetFormatter;

/**
 * The main entry point for the Budget Tuner command-line interface.
 */
public final class BudgetTunerCLI {

    /**
     * Default constructor.
     */
    public BudgetTunerCLI() {
    }

    /**
     * The main method that starts the application.
     */
    public static void main(final String[] args) {
        BudgetDataService service = new BudgetDataServiceImpl();
        BudgetTunerCLI app = new BudgetTunerCLI();
        app.run(service, System.in, System.out);
    }

    /**
     * Runs the application logic.
     * 
     * @param dataService The service to access budget data.
     * @param in          The input stream.
     * @param out         The output stream.
     */
    public void run(final BudgetDataService dataService, final InputStream in, final PrintStream out) {
        try (Scanner scanner = new Scanner(in, StandardCharsets.UTF_8)) {
            out.println();
            out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
            out.println("Καλωσορίσατε στο Budget Tuner!");
            out.println("Το εργαλείο διαχείρισης προϋπολογισμών");
            out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

            out.println("Σύνολο αποθηκευμένων στοιχείων στη Βάση:");
            SqlSequence statistics = dataService.loadStatistics();
            out.printf(
                    "- Προϋπολογισμοί: %d%n- Κατηγορίες Εσόδων: %d%n"
                            + "- Κατηγορίες Εξόδων: %d%n- Υπουργεία: %d%n"
                            + "- Έξοδα Υπουργείων: %d%n",
                    statistics.getBudgets(), statistics.getRevenueCategories(),
                    statistics.getExpenseCategories(), statistics.getMinistries(),
                    statistics.getMinistryExpenses());
            out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

            // Load list of budget years
            ArrayList<Integer> years = dataService.loadBudgetYears();
            boolean mainMenurunning = true;
            int choice;

            while (mainMenurunning) {
                out.println("\n--- ΚΥΡΙΟ ΜΕΝΟΥ ---");
                out.println("1. Επιλογή Έτους για Προβολή");
                out.println("2. Σύγκριση Δύο Ετών");
                out.println("3. Εισαγωγή Νέου Έτους");
                out.println("0. Έξοδος");
                out.print("Επιλογή: ");

                if (scanner.hasNextInt()) {
                    choice = scanner.nextInt();
                    scanner.nextLine(); // Consume newline
                } else {
                    out.println("\nΆκυρη επιλογή. Παρακαλώ εισάγετε έναν αριθμό.");
                    scanner.nextLine();
                    continue;
                }

                switch (choice) {
                    case 1:
                        // 1. Select Year
                        int year = selectYear(scanner, years, "Εισάγετε το έτος προϋπολογισμού: ", out);
                        out.println("Φορτώνεται ο προϋπολογισμός για το έτος " + year + "...");

                        int budgetID = dataService.loadBudgetIDByYear(year);
                        BudgetYear budget = dataService.loadBudgetYear(budgetID);

                        // 2. Enter View Menu
                        // Returns false if the user chose "Exit" (7), true if "Change Year" (6)
                        boolean keepAppRunning = handleViewBudgetMenu(scanner, budget, year, out);
                        if (!keepAppRunning) {
                            mainMenurunning = false;
                        }
                        break;

                    case 2:
                        out.println("Σύγκριση δύο ετών προϋπολογισμού...");

                        // 1. Select Years
                        int year1 = selectYear(scanner, years, "Εισάγετε το πρώτο έτος για σύγκριση: ", out);
                        int budgetID1 = dataService.loadBudgetIDByYear(year1);
                        BudgetYear budget1 = dataService.loadBudgetYear(budgetID1);

                        int year2 = selectYear(scanner, years, "Εισάγετε το δεύτερο έτος για σύγκριση: ", out);
                        int budgetID2 = dataService.loadBudgetIDByYear(year2);
                        BudgetYear budget2 = dataService.loadBudgetYear(budgetID2);

                        // 2. Enter Comparison Menu

                        handleCompareBudgetsMenu(scanner, budget1, budget2, year1, year2, out);
                        break;

                    case 3:
                        // Insert new year
                        out.println("Εισαγωγή νέου έτους προϋπολογισμού στη βάση...");
                        out.print("Εισάγετε τη διαδρομή του αρχείου PDF προϋπολογισμού "
                                + "(ή 0 για ακύρωση): ");
                        String pdfPath = scanner.nextLine();
                        if (!pdfPath.equals("0")) {
                            try {
                                dataService.insertNewBudgetYear(pdfPath);
                                // Reload years list after insertion
                                years = dataService.loadBudgetYears();
                                out.println("Η εισαγωγή ολοκληρώθηκε με επιτυχία.");
                            } catch (Exception e) {
                                out.println("Σφάλμα κατά την εισαγωγή: " + e.getMessage());
                            }
                        } else {
                            out.println("Ακύρωση εισαγωγής νέου έτους.");
                        }
                        break;

                    case 0:
                        out.println("Έξοδος από την εφαρμογή.");
                        mainMenurunning = false;
                        break;

                    default:
                        out.println("Μη έγκυρη επιλογή.");
                        break;
                }
            }
        }
    }

    /**
     * Helper method to prompt the user to select a valid year from the list.
     */
    /**
     * Helper method to prompt the user to select a valid year from the list.
     */
    private int selectYear(final Scanner scanner, final ArrayList<Integer> availableYears, final String message,
            final PrintStream out) {
        int selectedYear;
        do {
            out.println("Διαθέσιμα Έτη στη Βάση: ");
            for (int y : availableYears) {
                out.println("- " + y);
            }
            out.print(message);

            while (!scanner.hasNextInt()) {
                out.println("Άκυρη είσοδος. Παρακαλώ εισάγετε έναν έγκυρο αριθμό έτους.");
                scanner.nextLine();
            }
            selectedYear = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            if (!availableYears.contains(selectedYear)) {
                out.println("Το έτος " + selectedYear
                        + " δεν βρέθηκε στη βάση δεδομένων. Παρακαλώ εισάγετε ένα άλλο έτος.");
            }
        } while (!availableYears.contains(selectedYear));

        return selectedYear;
    }

    /**
     * Handles the menu for viewing a specific budget year.
     * 
     * @return true if the application should continue running, false if the user
     *         selected Exit.
     */
    /**
     * Handles the menu for viewing a specific budget year.
     * 
     * @return true if the application should continue running, false if the user
     *         selected Exit.
     */
    private boolean handleViewBudgetMenu(final Scanner scanner, final BudgetYear budget, final int year,
            final PrintStream out) {
        boolean menuRunning = true;
        boolean keepAppRunning = true;

        while (menuRunning) {
            out.println("\n--- ΚΕΝΤΡΙΚΟ ΜΕΝΟΥ ---");
            out.println("1. Προβολή Συνολικών Στοιχείων (Σύνοψη)");
            out.println("2. Προβολή Εσόδων");
            out.println("3. Προβολή Εξόδων");
            out.println("4. Προβολή Φορέων");
            out.println("5. Προβολή Δαπανών Φορέων");
            out.println("6. Αλλαγή Έτους Προϋπολογισμού");
            out.println("7. Επιστροφή στο Κύριο Μενού");
            out.println("8. Έξοδος");
            out.println("-----------------------");
            out.print("Επιλογή: ");

            int choice;
            if (scanner.hasNextInt()) {
                choice = scanner.nextInt();
                scanner.nextLine();
            } else {
                out.println("\nΆκυρη επιλογή. Παρακαλώ εισάγετε έναν αριθμό.");
                scanner.nextLine();
                choice = -1;
            }

            switch (choice) {
                case 1:
                    out.println("\n--- ΣΥΝΟΨΗ ΠΡΟϋΠΟΛΟΓΙΣΜΟΥ (" + year + ") ---");
                    out.println(budget.getSummary());
                    break;
                case 2:
                    out.println("\n--- ΕΣΟΔΑ ΠΡΟϋΠΟΛΟΓΙΣΜΟΥ (" + year + ") ---");
                    out.println(BudgetFormatter.getFormattedRevenues(budget.getRevenues()));
                    break;
                case 3:
                    out.println("\n--- ΕΞΟΔΑ ΠΡΟϋΠΟΛΟΓΙΣΜΟΥ (" + year + ") ---");
                    out.println(BudgetFormatter.getFormattedExpenditures(budget.getExpenses()));
                    break;
                case 4:
                    out.println("\n--- ΦΟΡΕΙΣ ΠΡΟϋΠΟΛΟΓΙΣΜΟΥ (" + year + ") ---");
                    out.println(BudgetFormatter.getFormattedMinistries(budget.getMinistries()));
                    break;
                case 5:
                    out.println("\n--- ΔΑΠΑΝΕΣ ΦΟΡΕΩΝ ΠΡΟϋΠΟΛΟΓΙΣΜΟΥ (" + year + ") ---");
                    out.println(BudgetFormatter.getFormattedMinistryExpenses(
                            budget.getMinistries(), budget.getExpenses(), budget.getMinistryExpenses()));
                    break;
                case 6:
                    out.println("Αλλαγή έτους προϋπολογισμού...");
                    menuRunning = false;
                    break;
                case 7:
                    out.println("Επιστροφή στο Κύριο Μενού...");
                    menuRunning = false;
                    break;
                case 8:
                    out.println("Έξοδος...");
                    menuRunning = false;
                    keepAppRunning = false;
                    break;
                default:
                    out.println("Μη έγκυρη επιλογή.");
                    break;
            }
        }
        return keepAppRunning;
    }

    /**
     * Handles the menu for comparing two budget years.
     */
    private void handleCompareBudgetsMenu(final Scanner scanner, final BudgetYear budget1,
            final BudgetYear budget2, final int year1, final int year2, final PrintStream out) {
        boolean compareRunning = true;
        while (compareRunning) {
            out.println("\n--- ΜΕΝΟΥ ΣΥΓΚΡΙΣΗΣ ---");
            out.println("1. Σύγκριση Συνολικών Στοιχείων (Σύνοψη)");
            out.println("2. Σύγκριση Εσόδων");
            out.println("3. Σύγκριση Εξόδων");
            out.println("4. Σύγκριση Φορέων");
            out.println("5. Επιστροφή στο Κύριο Μενού");
            out.print("Επιλογή: ");

            int compareChoice;
            if (scanner.hasNextInt()) {
                compareChoice = scanner.nextInt();
                scanner.nextLine();
            } else {
                out.println("\nΆκυρη επιλογή. Παρακαλώ εισάγετε έναν αριθμό.");
                scanner.nextLine();
                compareChoice = -1;
            }

            switch (compareChoice) {
                case 1:
                    String[] lines1 = budget1.getSummary().toString().split("\n");
                    String[] lines2 = budget2.getSummary().toString().split("\n");
                    out.println("\n" + String.format("%-80s", year1) + "|" + year2);
                    out.println(String.format("%-80s", "").replace(' ', '-') + "|"
                            + String.format("%-80s", "").replace(' ', '-'));
                    for (int i = 0; i < Math.max(lines1.length, lines2.length); i++) {
                        String l1 = i < lines1.length ? lines1[i] : "";
                        String l2 = i < lines2.length ? lines2[i] : "";
                        out.println(String.format("%-80s", l1) + "|" + l2);
                    }
                    break;
                case 2:
                    out.println(BudgetFormatter.getFormattedComparativeRevenues(budget1.getRevenues(),
                            budget2.getRevenues(), year1, year2));
                    break;
                case 3:
                    out.println(BudgetFormatter.getFormattedComparativeExpenditures(budget1.getExpenses(),
                            budget2.getExpenses(), year1, year2));
                    break;
                case 4:
                    out.println(BudgetFormatter.getFormattedComparativeMinistries(budget1.getMinistries(),
                            budget2.getMinistries(), year1, year2));
                    break;
                case 5:
                    compareRunning = false;
                    break;
                default:
                    out.println("Μη έγκυρη επιλογή.");
                    break;
            }
        }
    }
}
