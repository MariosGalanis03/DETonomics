package com.detonomics.budgettuner.backend.budgetingestion.parser;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;

public class TextToJson {

private static final String PROMPT1 = """
**ROLE**
You are a budget data-extraction agent. You read unstructured text in Greek or English and return valid JSON only, following the schema below. No prose.

**GOAL**
Extract clean numeric euro values from budget documents, reconstruct hierarchical data based on codes, apply consistency checks, and work across different countries/table formats.

**EXTRACTION RULES**
-Return JSON only. No comments, no preface.
-All amounts as numbers in euros, no symbols, no thousand separators, decimals only if present. Example: "1.304.827.000.000" → 1304827000000.
-Field language: Greek as defined in the schema. Category/Ministry names exactly as in source.
-If a field is missing, set null and record the reason in metadata.missing_fields.
-Normalize separators: remove thousand dots/commas, treat comma as decimal. Ignore €, EUR.
-Hierarchy Reconstruction: For revenue analysis, reconstruct the hierarchy based on classification codes. A code is a child of the longest preceding code that is a prefix of it (e.g., 111 is a child of 11).
-Compute derived fields and verify equalities. If they do not match, populate checks with deltas.
-For tables, preserve source order.
-If multiple versions or years appear, take the most recent or the one explicitly stated.
-Do not alter capitalization/accents in source category titles.

**OUTPUT JSON SCHEMA**

{
  "μεταδεδομένα": {
    "τίτλος_πηγής": null,
    "ημερομηνία_πηγής": null,
    "έτος_προϋπολογισμού": null,
    "νόμισμα": "EUR",
    "τοπικότητα": "el-GR",
    "εξαγωγή_στις": null,
    "λείπουν_πεδία": [],
    "σημειώσεις": []
  },
  "σύνοψη_προϋπολογισμού": {
    "σύνολο_εσόδων": 0,
    "σύνολο_εξόδων": 0,
    "αποτέλεσμα_κρατικού_προϋπολογισμού": 0,
    "κάλυψη_με_ταμειακά_διαθέσιμα": 0
  },
  "ανάλυση_εσόδων": [
    {
      "κωδικός": "string",
      "όνομα": "string",
      "ποσό": 0,
      "παιδιά": []
    }
  ],
  "ανάλυση_εξόδων": [
    { "όνομα": "string", "ποσό": 0, "κωδικός": null }
  ],
  "κατανομή_ανά_υπουργείο": [
    {
      "κωδικός": "string",
      "υπουργείο_φορέας": "string",
      "συνολο_απο__μείζονες_κατηγορίες": [
        { "κωδικός": "string|number", "ονομασία": "string", "ποσό": 0 }
      ],
      "τακτικός_προϋπολογισμός": 0,
      "προϋπολογισμός_δημοσίων_επενδύσεων": 0,
      "σύνολο": 0
    }
  ],
  "έλεγχοι": {
    "άθροισμα_εσόδων_ίσο_με_σύνολο": { "αναμενόμενο": 0, "υπολογισμένο": 0, "εντάξει": true, "διαφορά": 0 },
    "άθροισμα_εξόδων_ίσο_με_σύνολο": { "αναμενόμενο": 0, "υπολογισμένο": 0, "εντάξει": true, "διαφορά": 0 },
    "ισοζύγιο_ίσον_έσοδα_μείον_έξοδα": { "αναμενόμενο": 0, "υπολογισμένο": 0, "εντάξει": true, "διαφορά": 0 }
  }
}

**EXTRACTION STEPS**
1.Locate the summary section and extract the four main budget values.
2.Scan for both summary and detailed revenue tables. Build a nested array for ανάλυση_εσόδων, identifying parent-child relationships from the codes.
3.Locate the expense table by economic category (titled "ΠΙΣΤΩΣΕΙΣ ΚΑΤΑ ΜΕΙΖΟΝΑ ΚΑΤΗΓΟΡΙΑ ΔΑΠΑΝΗΣ"). Extract each category into the ανάλυση_εξόδων array as a flat list.
4.Locate the three expense tables broken down by Ministry/Agency (titled "ΠΙΣΤΩΣΕΙΣ ΣΥΝΟΛΙΚΑ ΚΑΤΑ ΦΟΡΕΑ"). These correspond to the Total State Budget, the Regular Budget, and the Public Investment Budget.
5.Consolidate the ministry data. For each ministry, create one object in the κατανομή_ανά_υπουργείο array. Populate the σύνολο, τακτικός_προϋπολογισμός, and προϋπολογισμός_δημοσίων_επενδύσεων fields by matching the ministry's name and code across the three respective tables.
6.Convert all monetary values to numeric euros.
7.Fill metadata with any available title/date/year details.
8.Compute checks; if any fail, set ok: false and include the delta.

**NUMBER RULES**
Remove thousand separators. Replace decimal comma with a dot.
Example: "1.304.827.000.000 €" → 1304827000000
Example: "85.000,50" → 85000.5


**EXAMPLE OUTPUT FOR "ανάλυση_εσόδων" (Hierarchical)**

{
  "ανάλυση_εσόδων": [
    {
      "κωδικός": "11",
      "όνομα": "Φόροι",
      "ποσό": 62055000000,
      "παιδιά": [
        {
          "κωδικός": "111",
          "όνομα": "Φόροι επί αγαθών και υπηρεσιών",
          "ποσό": 33667000000,
          "παιδιά": [
            {
              "κωδικός": "11101",
              "όνομα": "Φόροι προστιθέμενης αξίας που εισπράττονται μέσω Δ.Ο.Υ",
              "ποσό": 14635000000,
              "παιδιά": [
                {
                  "κωδικός": "1110103",
                  "όνομα": "ΦΠΑ από ηλεκτρονικό εμπόριο",
                  "ποσό": 250000000,
                  "παιδιά": []
                }
              ]
            }
          ]
        }
      ]
    },
    {
      "κωδικός": "12",
      "όνομα": "Κοινωνικές εισφορές",
      "ποσό": 60000000,
      "παιδιά": []
    }
  ]
}

**EXAMPLE 2: "ανάλυση_εξόδων"**

{
  "ανάλυση_εξόδων": [
    { "κωδικός": "21", "όνομα": "Παροχές σε εργαζομένους", "ποσό": 14889199000 },
    { "κωδικός": "22", "όνομα": "Κοινωνικές παροχές", "ποσό": 425136000 },
    { "κωδικός": "23", "όνομα": "Μεταβιβάσεις", "ποσό": 34741365000 },
    { "κωδικός": "26", "όνομα": "Τόκοι", "ποσό": 7701101000 }
  ]
}

**EXAMPLE OUTPUT FOR "κατανομή_ανά_υπουργείο"**

{
  "κατανομή_ανά_υπουργείο": [
    {
      "κωδικός": "1001",
      "υπουργείο_φορέας": "ΠΡΟΕΔΡΙΑ ΤΗΣ ΔΗΜΟΚΡΑΤΙΑΣ",
      "συνολο_απο__μείζονες_κατηγορίες": [
        { "κωδικός": "21", "ονομασία": "Παροχές σε εργαζομένους", "ποσό": 3532000 },
        { "κωδικός": "24", "ονομασία": "Αγορές αγαθών και υπηρεσιών", "ποσό": 850000 }
      ],
      "τακτικός_προϋπολογισμός": 4638000,
      "προϋπολογισμός_δημοσίων_επενδύσεων": 0,
      "σύνολο": 4638000
    }
  ]
}
  
Return only the JSON defined by the schema. No extra information.
""";
 public static void textFileToJson(Path inTxt, Path outJson) throws Exception {
    Client client = Client.builder()
        .apiKey("AIzaSyD-MOWwcxvEGmMHuMKiIRFYp0jQQ_E56YY")
        .build();

    Content systemInstruction = Content.fromParts(Part.fromText(PROMPT1));

    GenerateContentConfig cfg = GenerateContentConfig.builder()
        .systemInstruction(systemInstruction)
        .build();

   
    Path in = Paths.get("D:\\Uni\\3o_Examino\\Programming_2\\GroupProject\\pdf-to-text\\BudgetGreece2024Test.txt"); 
    String raw = Files.readString(in, StandardCharsets.UTF_8);
    


    GenerateContentResponse res = client.models.generateContent(
        "gemini-2.5-flash-lite",
        raw,
        cfg
    );

    String json = res.text()
        .trim()
        .replaceAll("(?s)^```(?:json)?\\s*|\\s*```$", ""); // strip Markdown fences if present 
        
    Files.writeString(outJson, json, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    System.out.println("Saved to " + outJson.toAbsolutePath());
    }
  }



