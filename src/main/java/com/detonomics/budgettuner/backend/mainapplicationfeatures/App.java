package com.detonomics.budgettuner.backend.mainapplicationfeatures;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.util.Scanner;

public class App {
    @SuppressWarnings("CallToPrintStackTrace")
    public static void main(String[] args) {
        ObjectMapper mapper = new ObjectMapper();
        BudgetData budgetData = null;

        try (InputStream inputStream = App.class.getClassLoader().getResourceAsStream("budget2025.json")) {
            if (inputStream == null) {
                throw new RuntimeException("Resource 'budget2025.json' not found.");
            }

            budgetData = mapper.readValue(inputStream, BudgetData.class);
            System.out.println("JSON Data Imported Successfully!");
            System.out.println("Τίτλος Προϋπολογισμού: " + budgetData.getInformation().getTitle());
            System.out.println("------------------------------------");
            
            // Κλήση της μεθόδου μενού
            startMenu(budgetData);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error reading or parsing JSON file.");
        }
    }

    // Νέα μέθοδος για το διαδραστικό μενού
    private static void startMenu(BudgetData budgetData) {
        Scanner scanner = new Scanner(System.in);
        int choice;

        do {
            System.out.println("\n--- ΚΕΝΤΡΙΚΟ ΜΕΝΟΥ ΔΙΑΧΕΙΡΙΣΗΣ ΠΡΟΫΠΟΛΟΓΙΣΜΟΥ ---");
            System.out.println("1. Προβολή Συνολικής Σύνοψης");
            System.out.println("2. Προβολή Όλων των Εσόδων");
            System.out.println("3. Προβολή Όλων των Εξόδων");
            System.out.println("4. Αναζήτηση Φορέα με βάση τον Κωδικό");
            System.out.println("5. Αναζήτηση Εσόδων ανά Κατηγορία");
            System.out.println("6. Έξοδος");
            System.out.print("Επιλογή: ");

            // Χειρισμός μη έγκυρης εισόδου
            if (scanner.hasNextInt()) {
                choice = scanner.nextInt();
                scanner.nextLine(); // Καθαρισμός buffer
            } else {
                System.out.println("\nΆκυρη επιλογή. Παρακαλώ εισάγετε έναν αριθμό.");
                scanner.nextLine(); // Καθαρισμός buffer για να αποφευχθεί το ατέρμονο loop
                choice = 0;
                continue;
            }
            
            System.out.println("------------------------------------");

            switch (choice) {
                case 1:
                    System.out.println(budgetData.getInformation().getSummary());
                    break;
                case 2:
                    System.out.println("--- ΛΕΠΤΟΜΕΡΕΙΕΣ ΕΣΟΔΩΝ ---");
                    System.out.println(budgetData.getFormattedRevenues());
                    break;
                case 3:
                    System.out.println("--- ΛΕΠΤΟΜΕΡΕΙΕΣ ΕΞΟΔΩΝ ---");
                    System.out.println(budgetData.getFormattedExpenditures());
                    break;
                case 4:
                    searchEntity(scanner, budgetData);
                    break;
                case 5:
                    searchRevenues(scanner, budgetData);
                    break;
                case 6:
                    System.out.println("Ευχαριστούμε που χρησιμοποιήσατε την εφαρμογή BudgetTuner. Έξοδος...");
                    break;
                default:
                    System.out.println("Η επιλογή " + choice + " δεν είναι έγκυρη. Προσπαθήστε ξανά.");
            }
        } while (choice != 6);

        scanner.close();
    }
    
    // Μέθοδοι που θα καλέσουν λειτουργίες της BudgetData

    private static void searchEntity(Scanner scanner, BudgetData budgetData) {
        System.out.println("--- ΛΙΣΤΑ ΔΙΑΘΕΣΙΜΩΝ ΦΟΡΕΩΝ (Σύνοψη) ---");
        System.out.println(budgetData.getEntitySummaryList()); 
        System.out.println("----------------------------------------");

        System.out.print("Εισάγετε τον Κωδικό Φορέα: ");
        
        if (scanner.hasNextInt()) {
            int code = scanner.nextInt();
            scanner.nextLine();
            GovernmentEntity entity = budgetData.findEntityByCode(code); 
            
            if (entity != null) {
                System.out.println("\nΒρέθηκε Φορέας:");
                System.out.println(entity); 
            } else {
                System.out.println("\nΔεν βρέθηκε φορέας με κωδικό: " + code + ". Ελέγξτε τη λίστα και προσπαθήστε ξανά.");
            }
        } else {
            System.out.println("\nΆκυρη είσοδος. Παρακαλώ εισάγετε έναν έγκυρο αριθμό.");
            scanner.nextLine();
        }
    }

    private static void searchRevenues(Scanner scanner, BudgetData budgetData) {
        System.out.print("Εισάγετε Κατηγορία Εσόδου (π.χ. Φόροι): ");
        String category = scanner.nextLine();

        System.out.println("\n--- ΑΠΟΤΕΛΕΣΜΑΤΑ ΓΙΑ '" + category + "' ---");
        String results = budgetData.findRevenuesByCategory(category.trim()); 
        
        if (!results.isEmpty()) {
            System.out.println(results);
        } else {
            System.out.println("\nΔεν βρέθηκαν έσοδα για την κατηγορία: " + category);
        }
    }
}
