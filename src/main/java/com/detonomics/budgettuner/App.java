package com.detonomics.budgettuner;

import java.io.InputStream;
import com.fasterxml.jackson.databind.ObjectMapper;

public class App {
    public static void main(String[] args) {
        ObjectMapper mapper = new ObjectMapper();

        try (InputStream inputStream = App.class.getClassLoader().getResourceAsStream("budget2025.json")) {
            if (inputStream == null) {
                throw new RuntimeException("Resource 'budget2025.json' not found.");
            }

            BudgetData budgetData = mapper.readValue(inputStream, BudgetData.class);
            System.out.println("JSON Data Imported Successfully!");
            
            // Example 1: Accessing summary info
            System.out.println("\n--- Budget Summary ---");
            System.out.println("Title: " + budgetData.getInformation().getTitle());
            System.out.println("Total Revenues (2025): " + budgetData.getInformation().getSummary().getTotalRevenues());

            // Example 2: Accessing the first entity
            System.out.println("\n--- First Entity ---");
            GovernmentEntity firstEntity = budgetData.getEntities().get(0);
            System.out.println("Entity Name: " + firstEntity.getName());
            System.out.println("General Total: " + firstEntity.getGeneralTotal());
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error reading or parsing JSON file.");
        }
    }
}
