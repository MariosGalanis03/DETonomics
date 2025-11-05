package com.detonomics.budgettuner.tools;

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
  // Java 15+ text block
  private static final String PROMPT = """
**ROLE**
You are a budget data-extraction agent. You read unstructured text in Greek or English and return valid JSON only, following the schema below. No prose.

**GOAL**
Extract clean numeric euro values, apply consistency checks, and work across different countries/table formats.


**EXTRACTION RULES**
-Return JSON only. No comments, no preface.
-All amounts as numbers in euros, no symbols, no thousand separators, decimals only if present. Example: "1304827000000" → 1304827000000.
-Field language: Greek as defined in the schema. Category/Ministry names exactly as in source.
-If a field is missing, set null and record the reason in metadata.missing_fields.
-Normalize separators: remove thousand dots/commas, treat comma as decimal. Ignore €, EUR.
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
    { "όνομα": "string", "ποσό": 0, "κωδικός": null }
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
-Locate the summary section and extract the four values.
-Scan revenue and expense lists. Build arrays of objects with onoma, poso, and kodikos if present.
-Locate the “Budget Allocation by Ministry/Agency” table and extract all rows.
-Convert all monetary values to numeric euros.
-Fill metadata with any available title/date/year details.
-Compute checks; if any fail, set ok: false and include delta.


**NUMBER RULES**
-Remove thousand separators. Replace decimal comma with a dot.
Example:
"1.304.827.000.000 €" → 1304827000000
"85.000,50" → 85000.5


**EXAMPLE OUTPUT FOR "κατανομή_ανά_υπουργείο"**
{
  "αναλυση_ανα_υπουργειο": [
    {
      "κωδικός_φορέα": "1001",
      "υπουργείο_φορέας": "ΥΠΟΥΡΓΕΙΟ Προεδρίας της Δημοκρατίας",
      "έτος": "2025",
      "μείζονες_κατηγορίες": [
        { "κωδικός": "21", "ονομασία": "Παροχές σε εργαζομένους", "ποσό": 3532000 },
        { "κωδικός": "23", "ονομασία": "Μεταβιβάσεις", "ποσό": 203000 },
        { "κωδικός": "24", "ονομασία": "Αγορές αγαθών και υπηρεσιών", "ποσό": 850000 },
        { "κωδικός": "31", "ονομασία": "Πάγια περιουσιακά στοιχεία", "ποσό": 53000 }
      ],
      "τακτικός_προϋπολογισμός": 4638000,
      "προϋπολογισμός_δημοσίων_επενδύσεων": 0,
      "σύνολο": 4638000
    }
  ]
}

Return only the JSON defined by the schema. No extra information.
""";

public static void main(String[] args) throws Exception {
  Client client = Client.builder()
      .apiKey("AIzaSyD-MOWwcxvEGmMHuMKiIRFYp0jQQ_E56YY")
      .build();

  Content systemInstruction = Content.fromParts(Part.fromText(PROMPT));

  GenerateContentConfig cfg = GenerateContentConfig.builder()
      .systemInstruction(systemInstruction)
      .build();

  Path in = Paths.get("data/P2025Test.txt"); 
  String raw = Files.readString(in, StandardCharsets.UTF_8);

  GenerateContentResponse res = client.models.generateContent(
      "gemini-2.5-flash-lite",
      raw,
      cfg
  );

  System.out.println(res.text());

  String json = res.text()
  .trim()
  .replaceAll("(?s)^```(?:json)?\\s*|\\s*```$", "")
  Path out = Paths.get("src/main/resources/budget_data.json");
  Files.writeString(out, json, StandardCharsets.UTF_8,
    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
  System.out.println("Saved to " + out.toAbsolutePath());
  }
}
