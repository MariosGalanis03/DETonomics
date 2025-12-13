# Checkstyle Errors Summary

**Total Errors: 90**

## Remaining Error Types by Count

### 1. HiddenField (44 errors)
- Constructor/method parameters that have the same name as class fields
- Common pattern: Parameters like `expenseID`, `code`, `name`, `amount` hiding class fields
- Files affected: ExpenseCategory, RevenueCategory, Ministry, MinistryExpense, BudgetYear, SqlSequence, Summary

### 2. VisibilityModifier (31 errors)
- Public fields in inner classes (JsonToSQLite.java) that should be private with accessors
- All fields in BudgetFile, Metadata, BudgetSummary, RevenueCategory, ExpenseCategory, Ministry, MinistryExpenseItem classes

### 3. HideUtilityClassConstructor (5 errors)
- Utility classes should have private constructors
- Files: App.java, BudgetFormatter.java, DatabaseManager.java, TextToJson.java, IngestBudgetPdf.java

### 4. ParameterName (3 errors)
- Parameter names using snake_case instead of camelCase
- `revenue_category_id` → should be `revenueCategoryId`
- `expense_category_id` → should be `expenseCategoryId`
- Files: BudgetManager.java

### 5. ConstantName (1 error)
- Constant name `dbPath` should be uppercase: `DB_PATH`
- File: BudgetManager.java

### 6. MethodLength (1 error)
- Method `main` in App.java is 169 lines (max allowed: 150)

### 7. ParameterNumber (1 error)
- Summary constructor has 9 parameters (max allowed: 7)
- File: Summary.java

### 8. DesignForExtension (1 error)
- BudgetManager class appears designed for extension but method lacks proper Javadoc
- File: BudgetManager.java

### 9. LeftCurly (1 error)
- Opening brace should be on new line
- File: JsonToSQLite.java line 210

### 10. WhitespaceAfter (1 error)
- Typecast not followed by whitespace
- File: JsonToSQLite.java line 309

### 11. OperatorWrap (1 error)
- Colon operator should be on new line
- File: JsonToSQLite.java line 355

## Files with Most Errors

1. **JsonToSQLite.java** - ~31 errors (VisibilityModifier)
2. **ExpenseCategory.java** - ~5 errors (HiddenField)
3. **RevenueCategory.java** - ~6 errors (HiddenField)
4. **Ministry.java** - ~9 errors (HiddenField)
5. **MinistryExpense.java** - ~5 errors (HiddenField)
6. **BudgetYear.java** - ~5 errors (HiddenField)
7. **SqlSequence.java** - ~5 errors (HiddenField)
8. **Summary.java** - ~10 errors (HiddenField, ParameterNumber)
9. **BudgetManager.java** - ~6 errors (ParameterName, ConstantName, DesignForExtension)
10. **App.java** - ~2 errors (HideUtilityClassConstructor, MethodLength)
11. **BudgetFormatter.java** - ~1 error (HideUtilityClassConstructor)
12. **DatabaseManager.java** - ~1 error (HideUtilityClassConstructor)
13. **TextToJson.java** - ~1 error (HideUtilityClassConstructor)
14. **IngestBudgetPdf.java** - ~1 error (HideUtilityClassConstructor)

## Remaining Fix Priorities

1. **HiddenField** (44) - Medium: Rename parameters or use `this.` prefix
2. **VisibilityModifier** (31) - Medium: Make fields private and add getters/setters
3. **HideUtilityClassConstructor** (5) - Easy: Add private constructor
4. **ParameterName** (3) - Easy: Rename to camelCase
5. **ConstantName** (1) - Easy: Rename to uppercase
6. **MethodLength** (1) - Hard: Refactor main method
7. **ParameterNumber** (1) - Hard: Use builder pattern or DTO
8. **DesignForExtension** (1) - Medium: Add Javadoc or make class final
9. **LeftCurly** (1) - Easy: Move brace to new line
10. **WhitespaceAfter** (1) - Easy: Add space after typecast
11. **OperatorWrap** (1) - Easy: Move operator to new line

## Progress Summary

- **Fixed:** 88 errors (TrailingSpaces: 55, FinalParameters: 30, NeedBraces: 3)
- **Remaining:** 90 errors
- **Total Original:** 176 errors (excluding line length errors)
