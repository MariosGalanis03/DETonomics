package com.detonomics.budgettuner.util.ingestion;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;

public final class TextToJson {

  private TextToJson() {
    throw new AssertionError("Utility class");
  }

  private static final String PROMPT1 = """
      **ROLE**
      You are a budget data-extraction agent. You read unstructured text in
      Greek or English and return valid JSON only, following the schema below.
      No prose.

      **GOAL**
      Extract clean numeric euro values from budget documents, reconstruct
      hierarchical data based on codes, apply consistency checks, and work
      across different countries/table formats.

      **EXTRACTION RULES**
      -Return JSON only. No comments, no preface.
      -All amounts as numbers in euros, no symbols, no thousand separators,
      decimals only if present. Example: "1.304.827.000.000" → 1304827000000.
      -Field language: Greek as defined in the schema. Category/Ministry names
      exactly as in source.
      -If a field is missing, set null and record the reason in
      metadata.missing_fields.
      -Normalize separators: remove thousand dots/commas, treat comma as decimal.
      Ignore €, EUR.
      -Hierarchy Reconstruction: For revenue analysis, reconstruct the hierarchy
      based on classification codes. A code is a child of the longest preceding
      code that is a prefix of it (e.g., 111 is a child of 11).
      -Compute derived fields and verify equalities. If they do not match,
      populate checks with deltas.
      -For tables, preserve source order.
      -If multiple versions or years appear, take the most recent or the one
      explicitly stated.
      -Do not alter capitalization/accents in source category titles.

      **OUTPUT JSON SCHEMA**

      {
        "metadata": {
          "sourceTitle": null,
          "sourceDate": null,
          "budgetYear": null,
          "currency": "EUR",
          "locale": "Greece",
          "missingFields": [],
        },
        "budgetSummary": {
          "totalRevenue": 0,
          "totalExpenses": 0,
          "stateBudgetBalance": 0,
          "coverageWwithCashReserves": 0
        },
        "revenueAnalysis": [
          {
            "code": "string",
            "name": "string",
            "amount": 0,
            "children": []
          }
        ],
        "expenseAnalysis": [
          { "name": "string", "amount": 0, "code": null }
        ],
        "distributionByMinistry": [
          {
            "code": "string",
            "ministryBody": "string",
            "totalFromMajorCategories": [
              { "code": "string|number", "name": "string", "amount": 0 }
            ],
            "regularBudget": 0,
            "publicInvestmentBudget": 0,
            "total": 0
          }
        ],
        "checks": {
          "sumOfRevenueEqualsTotal": { "expected": 0, "calculated": 0, "ok": true,
      "difference": 0 },
          "sumOfExpensesEqualsTotal": { "expected": 0, "calculated": 0, "ok": true,
      "difference": 0 },
          "balanceEqualsRevenueMinusExpenses": { "expected": 0, "calculated": 0,
      "ok": true, "difference": 0 }
        }
      }

      **EXTRACTION STEPS**
      1.Locate the summary section and extract the four main budget values.
      2.Scan for both summary and detailed revenue tables. Build a nested array
      for ανάλυση_εσόδων, identifying parent-child relationships from the codes.
      3.Locate the expense table by economic category (titled "ΠΙΣΤΩΣΕΙΣ ΚΑΤΑ
      ΜΕΙΖΟΝΑ ΚΑΤΗΓΟΡΙΑ ΔΑΠΑΝΗΣ"). Extract each category into the
      ανάλυση_εξόδων array as a flat list.
      4.Locate the three expense tables broken down by Ministry/Agency (titled
      "ΠΙΣΤΩΣΕΙΣ ΣΥΝΟΛΙΚΑ ΚΑΤΑ ΦΟΡΕΑ"). These correspond to the Total State
      Budget, the Regular Budget, and the Public Investment Budget.
      5.Consolidate the ministry data. For each ministry, create one object in
      the κατανομή_ανά_υπουργείο array. Populate the σύνολο,
      τακτικός_προϋπολογισμός, and προϋπολογισμός_δημοσίων_επενδύσεων fields by
      matching the ministry's name and code across the three respective tables.
      6.Convert all monetary values to numeric euros.
      7.Fill metadata with any available title/date/year details.
      8.Compute checks; if any fail, set ok: false and include the delta.

      **NUMBER RULES**
      Remove thousand separators. Replace decimal comma with a dot.
      Example: "1.304.827.000.000 €" → 1304827000000
      Example: "85.000,50" → 85000.5


      **EXAMPLE OUTPUT FOR "revenueAnalysis" (Hierarchical)**

      {
        "revenueAnalysis": [
          {
            "code": "11",
            "name": "Φόροι",
            "amount": 62055000000,
            "children": [
              {
                "code": "111",
                "name": "Φόροι επί αγαθών και υπηρεσιών",
                "amount": 33667000000,
                "children": [
                  {
                    "code": "11101",
                    "name": "Φόροι προστιθέμενης αξίας που εισπράττονται μέσω Δ.Ο.Υ",
                    "amount": 14635000000,
                    "children": [
                      {
                        "code": "1110103",
                        "name": "ΦΠΑ από ηλεκτρονικό εμπόριο",
                        "amount": 250000000,
                        "children": []
                      }
                    ]
                  }
                ]
              }
            ]
          },
          {
            "code": "12",
            "name": "Κοινωνικές εισφορές",
            "amount": 60000000,
            "children": []
          }
        ]
      }
      **EXAMPLE 2: "expenseAnalysis"**
      {
        "expenseAnalysis": [
          { "code": "21", "name": "Παροχές σε εργαζομένους", "amount": 14889199000 },
          { "code": "22", "name": "Κοινωνικές παροχές", "amount": 425136000 },
          { "code": "23", "name": "Μεταβιβάσεις", "amount": 34741365000 },
          { "code": "26", "name": "Τόκοι", "amount": 7701101000 }
        ]
      }

      **EXAMPLE OUTPUT FOR "distributionByMinistry"**

      {
        "distributionByMinistry": [
          {
            "code": "1001",
            "ministryBody": "ΠΡΟΕΔΡΙΑ ΤΗΣ ΔΗΜΟΚΡΑΤΙΑΣ",
            "totalFromMajorCategories": [
              { "code": "21", "name": "Παροχές σε εργαζομένους", "amount": 3532000 },
              { "code": "24", "name": "Αγορές αγαθών και υπηρεσιών",
      "amount": 850000 }
            ],
            "regularBudget": 4638000,
            "publicInvestment_budget": 0,
            "total": 4638000
          }
        ]
      }

      Return only the JSON defined by the schema. No extra information.
      """;

  public static void textFileToJson(final Path inTxt, final Path outJson)
      throws Exception {
    String apiKey = System.getenv("GEMINI_API_KEY");

    if (apiKey == null || apiKey.isBlank()) {
      throw new IllegalArgumentException(
          "Error: GEMINI_API_KEY environment variable is not set.");
    }

    Client client = Client.builder()
        .apiKey(apiKey)
        .build();

    Content systemInstruction = Content.fromParts(Part.fromText(PROMPT1));

    GenerateContentConfig cfg = GenerateContentConfig.builder()
        .systemInstruction(systemInstruction)
        .build();

    String raw = Files.readString(inTxt, StandardCharsets.UTF_8);

    GenerateContentResponse res = client.models.generateContent(
        "gemini-2.5-flash",
        raw,
        cfg);

    String text = res.text();

    if (text == null) {
      System.err.println(
          "Model returned null text. Check API key, model name, or "
              + "input size.");
      System.err.println("Raw response: " + res);
      return;
    }

    String json = text.trim()
        .replaceAll("(?s)^```(?:json)?\\s*|\\s*```$", "");
    // remove markdown fences

    Path parent = outJson.getParent();
    if (parent != null) {
      Files.createDirectories(parent);
    }

    Files.writeString(outJson, json, StandardCharsets.UTF_8,
        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

    System.out.println("Saved to " + outJson.toAbsolutePath());
  }
}
