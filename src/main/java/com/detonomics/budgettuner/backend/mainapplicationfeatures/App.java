package com.detonomics.budgettuner.backend.mainapplicationfeatures;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.util.List;
import java.util.Scanner;

// Η κεντρική κλάση της εφαρμογής, υπεύθυνη για τη φόρτωση δεδομένων,
// τη διαχείριση της ροής της εφαρμογής και την αλληλεπίδραση με τον χρήστη.
public class App {
    
    // Απενεργοποίηση της προειδοποίησης για χρήση printStackTrace (καθαρός χειρισμός εξαιρέσεων).
    @SuppressWarnings("CallToPrintStackTrace")
    public static void main(String[] args) {
        ObjectMapper mapper = new ObjectMapper();
        BudgetData budgetData = null;
        BudgetService budgetService = null; 

        try (InputStream inputStream = App.class.getClassLoader().getResourceAsStream("budget2025.json")) {
            if (inputStream == null) {
                throw new RuntimeException("Το αρχείο 'budget2025.json' δεν βρέθηκε.");
            }

            // 1. Φόρτωση των δεδομένων από το JSON
            budgetData = mapper.readValue(inputStream, BudgetData.class);
            
            // 2. Δημιουργία της Service κλάσης για πρόσβαση στη λογική των δεδομένων
            budgetService = new BudgetService(budgetData);

            System.out.println("Επιτυχής Φόρτωση Δεδομένων JSON!");
            System.out.println("Τίτλος Προϋπολογισμού: " + budgetData.getInformation().title());
            System.out.println("------------------------------------");
            
            // Εκκίνηση του μενού επιλογών
            startMenu(budgetData, budgetService);

        } catch (Exception e) {
            System.err.println("\n----------------------------------------------------");
            System.err.println("ΣΦΑΛΜΑ: Προέκυψε πρόβλημα κατά τη φόρτωση του JSON.");
            System.err.println("Λεπτομέρειες: " + e.getMessage()); 
            System.err.println("----------------------------------------------------\n");
        }
    }

    // Εμφανίζει το Κεντρικό Μενού της εφαρμογής και χειρίζεται τη βασική ροή.
    private static void startMenu(BudgetData budgetData, BudgetService budgetService) {
        Scanner scanner = new Scanner(System.in);
        int choice;

        do {
            System.out.println("\n--- ΚΕΝΤΡΙΚΟ ΜΕΝΟΥ ---");
            System.out.println("1. Προβολή Συνολικών Στοιχείων (Σύνοψη)");
            System.out.println("2. Διαχείριση Εσόδων");
            System.out.println("3. Διαχείριση Εξόδων");
            System.out.println("4. Αναζήτηση Φορέων");
            System.out.println("0. Έξοδος");
            System.out.println("-----------------------");
            System.out.print("Επιλογή: ");

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
                    System.out.println("\n--- ΣΥΝΟΨΗ ΠΡΟΫΠΟΛΟΓΙΣΜΟΥ ---");
                    // Καλεί την getSummary() από την BudgetService
                    BudgetSummary summary = budgetService.getSummary(); 
                    System.out.println(summary);
                    break;
                case 2:
                    revenueMenu(scanner, budgetData, budgetService); // Μετάβαση στο υπο-μενού Εσόδων
                    break;
                case 3:
                    // Κλήση με όλες τις παραμέτρους
                    expenditureMenu(scanner, budgetData, budgetService); // Μετάβαση στο υπο-μενού Εξόδων
                    break;
                case 4:
                    entityMenu(scanner, budgetService); // Μετάβαση στο υπο-μενού Φορέων
                    break;
                case 0:
                    System.out.println("Έξοδος από την εφαρμογή. Αντίο!");
                    break;
                default:
                    if (choice != -1) {
                        System.out.println("Μη έγκυρη επιλογή. Παρακαλώ δοκιμάστε ξανά.");
                    }
                    break;
            }
        } while (choice != 0);
        
        scanner.close();
    }

    // Υπο-Μενού για τη διαχείριση των Εσόδων.
    private static void revenueMenu(Scanner scanner, BudgetData budgetData, BudgetService budgetService) {
        int choice;
        do {
            System.out.println("\n--- ΜΕΝΟΥ ΕΣΟΔΩΝ ---");
            System.out.println("1. Προβολή Όλων των Εσόδων");
            System.out.println("2. Αναζήτηση με Κωδικό Κατηγορίας");
            System.out.println("9. Πίσω στο Κεντρικό Μενού");
            System.out.println("-----------------------");
            System.out.print("Επιλογή: ");

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
                    System.out.println("\n--- ΛΕΠΤΟΜΕΡΕΙΕΣ ΟΛΩΝ ΤΩΝ ΕΣΟΔΩΝ ---");
                    // Χρήση της BudgetFormatter για μορφοποίηση λίστας εσόδων.
                    System.out.println(BudgetFormatter.getFormattedRevenues(budgetData.getRevenues()));
                    break;
                case 2:
                    searchRevenues(scanner, budgetService); // Λειτουργία αναζήτησης
                    break;
                case 9:
                    System.out.println("Επιστροφή στο Κεντρικό Μενού.");
                    break;
                default:
                    if (choice != -1) {
                        System.out.println("Μη έγκυρη επιλογή. Παρακαλώ δοκιμάστε ξανά.");
                    }
                    break;
            }
        } while (choice != 9);
    }
    
    // Υπο-Μενού για τη διαχείριση των Εξόδων.
    // Πλέον περιλαμβάνει την αναζήτηση.
    private static void expenditureMenu(Scanner scanner, BudgetData budgetData, BudgetService budgetService) {
        int choice;
        do {
            System.out.println("\n--- ΜΕΝΟΥ ΕΞΟΔΩΝ ---");
            System.out.println("1. Προβολή Όλων των Εξόδων");
            System.out.println("2. Αναζήτηση με Κωδικό Κατηγορίας"); // ΝΕΑ ΕΠΙΛΟΓΗ
            System.out.println("9. Πίσω στο Κεντρικό Μενού");
            System.out.println("-----------------------");
            System.out.print("Επιλογή: ");

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
                    System.out.println("\n--- ΛΕΠΤΟΜΕΡΕΙΕΣ ΟΛΩΝ ΤΩΝ ΕΞΟΔΩΝ ---");
                    // Χρήση της BudgetFormatter για μορφοποίηση λίστας εξόδων.
                    System.out.println(BudgetFormatter.getFormattedExpenditures(budgetData.getExpenditures()));
                    break;
                case 2:
                    // Κλήση της νέας συνάρτησης αναζήτησης
                    searchExpenditures(scanner, budgetService); 
                    break;
                case 9:
                    System.out.println("Επιστροφή στο Κεντρικό Μενού.");
                    break;
                default:
                    if (choice != -1) {
                        System.out.println("Μη έγκυρη επιλογή. Παρακαλώ δοκιμάστε ξανά.");
                    }
                    break;
            }
        } while (choice != 9);
    }

    // Υπο-Μενού για την Αναζήτηση Κυβερνητικών Φορέων.
    private static void entityMenu(Scanner scanner, BudgetService budgetService) {
        int choice;
        do {
            System.out.println("\n--- ΜΕΝΟΥ ΦΟΡΕΩΝ ---");
            System.out.println("1. Προβολή Όλων των Φορέων (Σύνοψη)");
            System.out.println("2. Αναζήτηση με Κωδικό Φορέα");
            System.out.println("9. Πίσω στο Κεντρικό Μενού");
            System.out.println("-----------------------");
            System.out.print("Επιλογή: ");

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
                    // Κλήση της Service για τη λίστα φορέων
                    System.out.println(budgetService.getEntitySummaryList());
                    break;
                case 2:
                    searchEntity(scanner, budgetService); // Λειτουργία αναζήτησης
                    break;
                case 9:
                    System.out.println("Επιστροφή στο Κεντρικό Μενού.");
                    break;
                default:
                    if (choice != -1) {
                        System.out.println("Μη έγκυρη επιλογή. Παρακαλώ δοκιμάστε ξανά.");
                    }
                    break;
            }
        } while (choice != 9);
    }

    // Χειρίζεται τη λογική για την αναζήτηση Κυβερνητικού Φορέα με βάση τον κωδικό.
    private static void searchEntity(Scanner scanner, BudgetService budgetService) {
        System.out.print("Εισάγετε τον Κωδικό Φορέα: ");
        
        if (scanner.hasNextInt()) {
            int code = scanner.nextInt();
            scanner.nextLine();
            
            GovernmentEntity entity = budgetService.findEntityByCode(code); 
            
            if (entity != null) {
                System.out.println("\nΒρέθηκε Φορέας:");
                System.out.println(entity); 
            } else {
                System.out.println("\nΔεν βρέθηκε φορέας με κωδικό: " + code + ".");
            }
        } else {
            System.out.println("\nΆκυρη είσοδος. Παρακαλώ εισάγετε έναν έγκυρο αριθμό.");
            scanner.nextLine();
        }
    }

    // Χειρίζεται τη λογική για την αναζήτηση Εσόδων με βάση τον κωδικό κατηγορίας.
    private static void searchRevenues(Scanner scanner, BudgetService budgetService) {
        // Εμφάνιση των διαθέσιμων κωδικών για διευκόλυνση του χρήστη
        System.out.println(budgetService.getRevenueCategoryList()); 
        
        System.out.print("Εισάγετε τον Κωδικό Εσόδου: ");
        
        if (scanner.hasNextInt()) {
            int code = scanner.nextInt();
            scanner.nextLine();
            
            System.out.println("\n--- ΑΠΟΤΕΛΕΣΜΑΤΑ ΓΙΑ ΚΩΔΙΚΟ '" + code + "' ---");
            
            // Κλήση της Service για τη λήψη των αντικειμένων
            List<RevenueItem> results = budgetService.findRevenuesByCode(code);
            
            if (!results.isEmpty()) {
                // Χρήση του BudgetFormatter για τη μορφοποίηση της λίστας
                System.out.println(BudgetFormatter.getFormattedRevenues(results));
            } else {
                System.out.println("Δεν βρέθηκαν έσοδα με κωδικό: " + code + ".");
            }
        } else {
            System.out.println("\nΆκυρη είσοδος. Παρακαλώ εισάγετε έναν έγκυρο αριθμό.");
            scanner.nextLine();
        }
    }
    
    // ΝΕΑ ΜΕΘΟΔΟΣ: Χειρίζεται τη λογική για την αναζήτηση Εξόδων με βάση τον κωδικό κατηγορίας.
    private static void searchExpenditures(Scanner scanner, BudgetService budgetService) {
        // Εμφάνιση των διαθέσιμων κωδικών για διευκόλυνση του χρήστη
        System.out.println(budgetService.getExpenditureCategoryList()); 
        
        System.out.print("Εισάγετε τον Κωδικό Εξόδου: ");
        
        if (scanner.hasNextInt()) {
            int code = scanner.nextInt();
            scanner.nextLine();
            
            System.out.println("\n--- ΑΠΟΤΕΛΕΣΜΑΤΑ ΓΙΑ ΚΩΔΙΚΟ '" + code + "' ---");
            
            // Κλήση της Service για τη λήψη των αντικειμένων
            List<ExpenditureItem> results = budgetService.findExpendituresByCode(code);
            
            if (!results.isEmpty()) {
                // Χρήση του BudgetFormatter για τη μορφοποίηση της λίστας
                System.out.println(BudgetFormatter.getFormattedExpenditures(results));
            } else {
                System.out.println("Δεν βρέθηκαν έξοδα με κωδικό: " + code + ".");
            }
        } else {
            System.out.println("\nΆκυρη είσοδος. Παρακαλώ εισάγετε έναν έγκυρο αριθμό.");
            scanner.nextLine();
        }
    }
}
