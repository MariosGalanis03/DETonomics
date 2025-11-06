package com.detonomics.budgettuner.backend.mainapplicationfeatures;

import java.io.InputStream;
import com.fasterxml.jackson.databind.ObjectMapper;

public class App {
    @SuppressWarnings("CallToPrintStackTrace")
    public static void main(String[] args) {
        ObjectMapper mapper = new ObjectMapper();

        try (InputStream inputStream = App.class.getClassLoader().getResourceAsStream("budget2025.json")) {
            if (inputStream == null) {
                throw new RuntimeException("Resource 'budget2025.json' not found.");
            }

            BudgetData budgetData = mapper.readValue(inputStream, BudgetData.class);
            System.out.println("JSON Data Imported Successfully!");
            System.out.println();
            
            System.out.println(budgetData);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error reading or parsing JSON file.");
        }
    }
}
