# Dialob Form Specification

## Overview

Dialob forms are reactive, stateful form definitions that enable complex conditional logic without custom UI code. Forms consist of field definitions, conditional expressions, value sets, and context variables that together create dynamic user experiences.

## Form Architecture

### Core Concepts

**Form Instance Lifecycle:**
```
Form Definition → Form Instance (initial state) → User Input + Current Instance → New Instance State
```

The form engine uses a reactive model where:
- Field values become variables in expressions
- Expression changes trigger dependency recalculation
- UI automatically updates based on new state

### Form Structure

```json
{
  "_id": "form-id",
  "_rev": "version",
  "name": "form-name",
  "data": { /* field definitions */ },
  "metadata": { /* form metadata */ },
  "variables": [ /* context variables */ ],
  "valueSets": [ /* dropdown options */ ]
}
```

## Field Types & Properties

### Basic Field Properties

```json
{
  "id": "fieldId",
  "type": "text|list|boolean|group|note",
  "label": { "fi": "Finnish label", "en": "English label" },
  "required": "true|false|expression",
  "activeWhen": "conditional_expression",
  "props": { /* type-specific properties */ }
}
```

## Form Structure & Hierarchy

### Pages (Root Containers)

Pages are the main containers for all Dialob elements and provide the foundational structure for forms.

**Page Characteristics:**
- **Required**: All forms need at least one page
- **Unlimited**: Can have multiple pages for multi-step workflows
- **Root Level**: Top-level containers in form hierarchy
- **Visibility Only**: Page logic supports only visibility rules (no validation/required)

**Page Structure:**
```
Page
├─ Groups (organizational containers)
│  ├─ Input Fields (data collection)
│  ├─ Output Elements (notes/information)
│  └─ Nested Groups (sub-organization)
└─ Direct Items (outside groups)
```

**Creating Pages:**
1. Click "Add" button in top right corner of empty form
2. Define page label and optional description
3. Set page-level visibility rules if needed
4. Begin adding groups and items

### Groups (Organizational Containers)

Groups exist within pages and serve to organize different types of questions into logical sets.

**Group Types:**

**1. General Group:**
```json
{
  "id": "personalInfo",
  "type": "group",
  "label": { "fi": "Personal Information" },
  "items": ["firstName", "lastName", "email"],
  "activeWhen": "userType = 'individual'"
}
```

**2. Survey Group (Horizontal):**
```json
{
  "id": "serviceSurvey", 
  "type": "survey",
  "label": { "fi": "Rate our services" },
  "items": ["friendliness", "speed", "quality"],
  "valueSetId": "ratingScale"
}
```

**3. Survey Group (Vertical):**
```json
{
  "id": "verticalSurvey",
  "type": "survey", 
  "view": "vertical",
  "label": { "fi": "Feedback Questions" },
  "items": ["item1", "item2"],
  "valueSetId": "yesNoOptions"
}
```

**4. Multi-Row Group:**
```json
{
  "id": "familyMembers",
  "type": "rowgroup",
  "label": { "fi": "Family Members" },
  "items": ["firstName", "lastName", "birthDate"]
}
```

**Group Capabilities:**
- **Nesting**: Groups can contain other groups (unlimited depth)
- **Mixed Content**: Can contain inputs, outputs, and sub-groups
- **Visibility Logic**: Only visibility rules apply to groups
- **Flexible Placement**: Items can exist inside or outside groups

**Creating Groups:**
1. Use "Add Item" → "Structure" → Select group type
2. Define group label and properties
3. Add items using "Add Item" button within group
4. Configure group-level visibility rules

**Group Management:**
- **Delete**: Hamburger icon → "Delete"
- **Reorder**: Drag and drop in tree view
- **Edit**: Hamburger icon → "Options"

### Item Creation & Placement

**Two Creation Methods:**

**Within Groups (Add Item button):**
- Creates item inside the active group
- Maintains group organization
- Item becomes child of the group

**Outside Groups (Hamburger + Insert New):**
- Creates item outside current group
- Places item at page level or as sibling to group
- Item becomes independent element

### Field Types

**Text Fields:**
```json
{
  "id": "fieldId",
  "type": "text",           // Single-line text input
  "view": "text",           // or "textBox" for multi-line
  "label": { "fi": "Text field label" },
  "defaultValue": "default text",
  "props": {
    "controlType": "fileUpload" // for file uploads
  }
}
```

**Number Fields:**
```json
{
  "id": "ageField", 
  "type": "number",         // Integer values only
  "label": { "fi": "Enter your age" }
}
```

**Decimal Fields:**
```json
{
  "id": "priceField",
  "type": "decimal",        // Decimal values allowed
  "label": { "fi": "Enter amount" }
}
```

**Boolean Fields:**
```json
{
  "id": "agreeTerms",
  "type": "boolean",        // True/false, yes/no questions
  "label": { "fi": "Do you agree to terms?" },
  "props": {
    "display": "checkbox"  // or radio buttons (default)
  }
}
```

**Date Fields:**
```json
{
  "id": "birthDate",
  "type": "date",           // Date picker, returns "yyyy-mm-dd"
  "label": { "fi": "Birth date" }
}
```

**Time Fields:**
```json
{
  "id": "appointmentTime",
  "type": "time",           // Time picker, returns "hh:mm:ss"
  "label": { "fi": "Appointment time" }
}
```

**Choice Fields (Single Selection):**
```json
{
  "id": "country",
  "type": "choice",         // Single selection dropdown/radio
  "label": { "fi": "Select country" },
  "valueSetId": "vs1",      // References valueSets array
  "required": "true"
}
```

**Multi-Choice Fields (Multiple Selection):**
```json
{
  "id": "skills", 
  "type": "multichoice",    // Multiple selection checkboxes
  "label": { "fi": "Select your skills" },
  "valueSetId": "vs2"
}
```

**Survey Fields (Matrix Questions):**
```json
{
  "id": "satisfactionSurvey",
  "type": "survey",         // Survey group with matrix layout
  "label": { "fi": "Rate our services" },
  "items": ["friendliness", "responseTime", "quality"],
  "valueSetId": "vs3"       // Survey options (Poor/Good/Excellent)
}
```

**Multi-Row Fields (Repeating Groups):**
```json
{
  "id": "familyMembers",
  "type": "rowgroup",       // Repeating row of fields
  "label": { "fi": "Family members" },
  "items": ["firstName", "lastName", "age"]
}
```

**Groups (Containers):**
```json
{
  "id": "personalInfo",
  "type": "group",
  "view": "page",           // Optional: creates page boundary
  "label": { "fi": "Personal Information" },
  "items": ["firstName", "lastName", "birthDate"],
  "props": {
    "columns": 1,
    "border": "true",
    "collapsible": "true",
    "noPrint": "true"
  }
}
```

**Notes (Display Text):**
```json
{
  "id": "welcomeNote",
  "type": "note",           // Read-only display text
  "label": { "fi": "# Welcome to our form" },
  "description": { "fi": "Please fill out all required fields" }
}
```

## Expression Language (DEL)

### Overview

Dialob Expression Language (DEL) resembles natural grammar with:
- **Subject**: Field ID or reference (e.g., `customerAge`, `mainList`)  
- **Verb**: Operator or function (e.g., `>=`, `is answered`, `matches`)
- **Object**: Values or conditions to evaluate against

DEL expressions are if/then statements where:
- The "if" part is written by the user
- The "then" part is evaluated by Dialob Manager
- No punctuation (semicolons, periods) at expression end

### Critical Rule Types & Logic Direction

**Validation Rules** (ERROR CONDITIONS):
```javascript
// TRUE = Show error message, prevent progression
answer < 18                     // Error if under 18  
answer not matches "^EE[0-9]{9}$"  // Error if wrong format
lengthOf(answer) < 5           // Error if too short
```

**Visibility/Required Rules** (POSITIVE CONDITIONS):
```javascript  
// TRUE = Show field or make required
age >= 18                      // Show if 18 or older
mainList is answered           // Show when answered
language = 'fi'                // Show for Finnish users
```

### Field State Properties

```javascript
// User interaction state
mainList is answered          // User provided any input
mainList is not answered      // No user input yet

// Validation state  
email is valid               // Passes all validation rules
email is not valid           // Has validation errors

// Value state
firstName is null            // No value assigned
firstName is blank           // Empty string value ("")
```

### Operators & Comparisons

**Basic Comparisons:**
```javascript
age = 25                     // Equal to
age != 25                    // Not equal to  
age > 18, age < 65           // Greater/less than
age >= 18, age <= 65         // Greater/less than or equal
```

**Logical Operators:**
```javascript
age > 18 and citizenship = "finnish"     // Both conditions
income > 50000 or hasScholarship = true  // Either condition
not (age < 18)                          // Negation
```

**Set Membership:**
```javascript
country in ('finland', 'sweden', 'norway')     // Single selection
skills not in ('programming', 'design')        // Exclusion  
colors in ('red', 'blue')                     // Multi-choice lists
```

**Pattern Matching:**
```javascript
// Regular expressions for format validation
email matches '^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$'
phone not matches '^\+[0-9]{1,3}[0-9\s-]{4,14}$'
ssn matches '(\d{2})(\d{2})(\d{2})([+-A])\d{3}[0-9A-Z]'
```

### Built-in Functions

**Date & Time Functions:**
```javascript
today()                      // Current system date (yyyy-mm-dd)
now()                        // Current system time (hh:mm:ss)

// Date arithmetic returns period format "P2Y3M15D" (2 years, 3 months, 15 days)
endDate - startDate > 1 year + 6 months
birthDate + 18 years < today()

// Time arithmetic returns duration format "PT8H30M" (8 hours, 30 minutes)  
endTime - startTime < 8 hours
appointment + 2 hours > now()
```

**Text Functions:**
```javascript
lengthOf(password) >= 8      // Character count including spaces
lengthOf(description) <= 500
```

**Finnish-Specific Validators:**
```javascript
isHetu(personalId)           // Finnish personal ID validation
isLyt(companyId)            // Finnish company ID validation  
birthDateFromHetu(personalId) // Extract birthdate from Hetu
```

**Counting & Aggregation:**
```javascript
count(selectedItems) > 2              // Count multi-choice selections or row groups
sum of (income1, income2, income3)    // Sum numeric fields
min of (score1, score2, score3)       // Minimum value from fields
max of (score1, score2, score3)       // Maximum value from fields

// Multi-row specific operations
sum of rowGroupField                   // Sum all values in row group column
count(rowGroupId)                     // Count active rows in row group
```

**Boolean Multi-Row Operations:**
```javascript
any of booleanRowField                // True if any row has true value
all of booleanRowField                // True only if all rows have true value
```

**IBAN Validation:**
```javascript
isIban(accountNumber)        // Valid IBAN format
isNotIban(accountNumber)     // Invalid IBAN format
```

### Advanced Expression Patterns

**Time Duration Calculations:**
```javascript
// Check work day length
workEndTime - workStartTime > 6 hours + 30 minutes

// Validate appointment scheduling  
appointmentDate - today() >= 2 days
appointmentTime > now() + 2 hours
```

**Date Period Validations:**
```javascript
// Age verification from birthdate
today() - birthDate >= 18 years

// Contract duration limits
contractEnd - contractStart <= 2 years + 6 months

// Historical date ranges
eventDate > "2020-01-01" and eventDate < "2025-12-31"
```

**Complex Conditional Logic:**
```javascript
// Multi-level dependencies
mainCategory is answered and 
subCategory in ('option1', 'option2') and 
subCategory is answered

// Authentication-based visibility
(authentication in ('no') and SocialSecurityNumber = "anon") or 
(SocialSecurityNumber != "anon")

// Language-specific rules
language = 'fi' and residence in ('finland', 'sweden')
```

### Regular Expression Examples

**Email Validation:**
```javascript
answer not matches "^(?i)^[\w!#$%&'*+/=?`{|}~^-]+(?:\.[\w!#$%&'*+/=?`{|}~^-]+)*@(?:[A-Z0-9-]+\.)+[A-Z]{2,6}$"
```

**Estonian VAT Number:**
```javascript  
answer not matches "^(EE)?[0-9]{9}$"
```

**US Phone Number:**
```javascript
answer not matches "^\(?([0-9]{3})\)?[-. ]?([0-9]{3})[-. ]?([0-9]{4})$"
```

**International Phone (E.123):**
```javascript
answer not matches "^\+(?:[0-9] ?){6,14}[0-9]$" 
```

### Language Support

```javascript
language = 'fi'              // Finnish language active
language != 'en'             // Not English  
language in ('fi', 'sv')     // Finnish or Swedish
```

Language codes follow ISO 639-1 two-character standard.

### Reserved Words Summary

**Logic Building:**
`and`, `or`, `not`, `in`, `is`, `answered`, `valid`, `true`, `false`, `matches`

**Time/Date:**  
`day`, `days`, `week`, `weeks`, `month`, `months`, `year`, `years`, `hour`, `hours`, `minute`, `minutes`, `second`, `seconds`

**Functions:**
`today()`, `now()`, `lengthOf()`, `count()`, `isHetu()`, `isLyt()`, `birthDateFromHetu()`, `isIban()`, `isNotIban()`, `sum of()`, `min of()`, `max of()`, `any of()`, `all of()`

## Value Sets (Lists)

Value sets are key-value pairs that populate dropdown menus, choice selections, and survey button options. They provide the selectable options for choice, multi-choice, and survey field types.

### Value Set Types

**Global Lists:**
- Exist independently from specific fields
- Can be reused across multiple questions
- Changes automatically update all questions using the list
- Best for commonly used options (countries, languages, ratings)

**Local Lists:** 
- Bound to specific response fields
- Created within the question where they're used
- Changes only affect that specific question
- Best for one-time use, specific options

### Value Set Structure

```json
{
  "id": "vs1",
  "entries": [
    {
      "id": "option1",                    // Unique key (what gets stored)
      "label": {
        "fi": "Finnish option text",     // What user sees (localized)
        "en": "English option text"
      }
    },
    {
      "id": "option2", 
      "label": {
        "fi": "Toinen vaihtoehto",
        "en": "Second option"
      }
    }
  ]
}
```

### Creating Value Sets

**Global Lists:**
1. Navigate to "Lists" in top menu
2. Select "Add new list"
3. Define list name and entries
4. Set unique keys and localized labels

**Local Lists:**
1. Create Choice/Multi-choice field
2. Click hamburger icon → "Options" → "Choices" tab
3. Select "Create local list"
4. Define entries directly in the field

### Usage Patterns

**With Choice Fields:**
```json
{
  "id": "country",
  "type": "choice",
  "valueSetId": "countryList",
  "label": { "fi": "Valitse maa" }
}
```

**With Multi-Choice Fields:**
```json
{
  "id": "skills",
  "type": "multichoice", 
  "valueSetId": "skillsList",
  "label": { "fi": "Valitse taidot" }
}
```

**With Survey Groups:**
```json
{
  "id": "satisfactionSurvey",
  "type": "survey",
  "items": ["service", "speed", "quality"],
  "valueSetId": "ratingScale"    // Applied at group level
}
```

### Value Set Logic Examples

**Single Selection:**
```javascript
country = "finland"                     // Exact match
country in ("finland", "sweden")       // Multiple options
country not in ("usa", "canada")       // Exclusion
```

**Multiple Selection (Multi-choice):**
```javascript
"programming" in skills                 // Has specific skill
"design" not in skills                 // Doesn't have skill
count(skills) > 2                      // Selection count validation
```

**Survey Response Logic:**
```javascript
serviceRating = "poor"                 // Show improvement field
friendliness in ("good", "excellent")  // Positive rating logic
```

### Local List Visibility Rules

Local lists support conditional visibility for individual entries:

```json
{
  "entries": [
    {
      "id": "advanced_option",
      "label": { "fi": "Advanced Option" },
      "activeWhen": "userLevel = 'expert'"    // Only show for experts
    }
  ]
}
```

### CSV Import/Export

**CSV Format:**
```csv
ID,en,fi
option1,"English label","Finnish label"  
option2,"Second option","Toinen vaihtoehto"
```

**Import Modes:**
- **Replace all**: Replaces entire value set
- **Append**: Adds entries to existing list
- **Update**: Updates existing entries by ID, adds new ones

**CSV Requirements:**
- First column must be "ID"
- Language columns use ISO 639-1 codes (en, fi, sv, etc.)
- Form languages must be activated before CSV upload
- Empty rows are ignored, order is preserved

## Custom Variables & Expressions

Dialob supports two types of custom variables that extend form functionality beyond basic field interactions:

### Context Variables

**Static/immutable variables available across the entire dialog session**

```json
{
  "variables": [
    {
      "name": "SocialSecurityNumber",
      "context": true,
      "contextType": "text",
      "defaultValue": "anon"
    },
    {
      "name": "FirstNames", 
      "context": true,
      "contextType": "text"
    },
    {
      "name": "userAge",
      "context": true, 
      "contextType": "number",
      "defaultValue": 18
    }
  ]
}
```

**Context Variable Properties:**
- **Immutable**: Value cannot change during form session
- **Global scope**: Available in all expressions across the form
- **Pre-populated**: Usually set during form instance creation
- **Types**: text, number, boolean, date, time
- **Default values**: Used when no external value provided

**Usage Patterns:**
```javascript
// In expressions (no braces)
SocialSecurityNumber = "anon"
userAge >= 18

// In labels/text (with braces)  
"Welcome, {FirstNames}!"
"Your age is {userAge} years"

// In validation messages
"Must be {minimumAge} or older"
```

### Expression Variables

**User-defined functions/calculations available across the dialog session**

Expression variables enable custom mathematical operations, logical functions, and complex calculations based on form data.

**Creating Expression Variables:**
```javascript
// Mathematical operations
totalCost: price + tax + shipping
averageScore: (test1 + test2 + test3) / 3
discountAmount: totalCost * discountPercent / 100

// Logical expressions  
isAdult: age >= 18
isEligible: isAdult and hasLicense and not hasPenalties
needsParentalConsent: age < 18 and riskLevel = "high"

// Date calculations
yearsOfExperience: today() - startDate  
contractExpiry: startDate + contractLength
isExpired: contractExpiry < today()

// Complex combinations
finalScore: (baseScore * difficultyMultiplier) + bonusPoints
riskAssessment: (age < 25 and experience < 2) or (violations > 3)
```

**Expression Variable Usage:**
```javascript
// In validation rules
answer > {minimumRequired}
{totalCost} <= budgetLimit

// In visibility conditions  
{isEligible} and applicationComplete is answered
{riskLevel} = "high" 

// In labels/messages (with braces)
"Total cost: €{totalCost}"
"Your risk score: {riskAssessment}"
"Contract expires: {contractExpiry}"
```

### Variable Lifecycle & Availability

**Important Constraints:**
- Variables are only available when their values are set
- Expression variables depending on form fields become active only after all required fields are answered
- No NULL values - variables either exist with a value or don't exist
- Context variables are available immediately
- Expression variables wait for dependencies

**Example Dependency Chain:**
```javascript
// These fields must be answered first:
price: number field
tax: number field  
shipping: number field

// This expression becomes available after all above are answered:
totalCost: price + tax + shipping

// This validation can only work after totalCost is available:
budgetValidation: answer <= {totalCost}
```

### Advanced Usage Examples

**Pre-filling Form Data:**
```javascript
// Context variables for user data
{
  "name": "userEmail", 
  "contextType": "text",
  "defaultValue": "user@example.com"
}

// Use in form field default values
emailField: {
  "type": "text",
  "defaultValue": "{userEmail}"
}
```

**Dynamic Calculations:**
```javascript
// Expression variables for complex math
vatAmount: subtotal * vatRate / 100
totalWithVat: subtotal + {vatAmount}
finalDiscount: {totalWithVat} > 1000 ? 50 : 0
finalTotal: {totalWithVat} - {finalDiscount}

// Display in real-time
summaryNote: "Subtotal: €{subtotal}, VAT: €{vatAmount}, Total: €{finalTotal}"
```

**Multi-Field Validation:**
```javascript
// Expression for complex eligibility
isQualified: (education = "university" and experience >= 3) or 
             (education = "college" and experience >= 5) or
             (certificationScore >= 85)

// Use in multiple field validations
applicationField: {
  "required": "{isQualified}",
  "validation": "not {isQualified}",
  "message": "You must meet qualification requirements"
}
```

**Conditional Form Flow:**
```javascript
// Risk assessment expression
riskCategory: (age < 25 and violations > 0) ? "high" : 
              (age < 30 and violations = 0) ? "medium" : "low"

// Different form sections based on risk
highRiskSection: {
  "visibility": "{riskCategory} = 'high'",
  "required": "{riskCategory} = 'high'"
}
```

### Published Variables

**Published Setting:**
- Makes variable value available on filling side UI
- Generally should remain unselected (default)
- Only enable for specific implementation needs
- Can expose internal calculations to form fillers

### Variable Management

**Creating Variables:**
1. Navigate to "Variables" menu in Composer
2. Choose "Context Variables" or "Expression Variables" tab
3. Define ID, type, description, and value/expression
4. Set default values for context variables
5. Write expressions for expression variables

**Naming Rules:**
- Must start with letter [a-z, A-Z]
- Can include numbers [0-9]
- Cannot use reserved DEL keywords
- Must be unique across all variables
- Case-sensitive (userAge ≠ UserAge)

**References:**
- Expression rules: variable name without braces
- Labels/text: variable name with braces {variableName}
- Automatic reference updates when IDs change

## Form Metadata

```json
{
  "metadata": {
    "label": "form-display-name",
    "created": "2025-05-23T02:58:55.852Z",
    "lastSaved": "2025-10-30T08:41:56.897Z",
    "valid": true,
    "tenantId": "tenant-uuid",
    "languages": ["en", "fi"],
    "defaultActiveLanguage": "fi",
    "labels": ["demo", "feedback"]
  }
}
```

## Best Practices

### Performance Considerations
- Use `FormMetaQuery` for lightweight form headers (avoids loading heavy 1MB+ form content)
- Be cautious with `FormTagQuery.findAll()` on tenants with many forms
- Forms can be large (1MB+), consider caching strategies

### Expression Design
- Keep expressions simple and readable
- Use meaningful field IDs that work well as variables
- Avoid circular dependencies in conditional logic
- Test edge cases where fields become undefined

### Form Organization
- Use groups to create logical sections
- Leverage page groups for multi-step workflows
- Structure field IDs hierarchically for related data
- Use descriptive value set IDs despite naming being arbitrary

### Internationalization
- Always provide labels for all supported languages
- Use value sets for locale-specific option text
- Consider cultural differences in form flow logic

## Integration Patterns

### With FormDb Interface

```java
// Query forms
FormTenant tenant = formDb.withTenant("customer-123");
Form form = tenant.formQuery()
  .formTag("feedback-form", "v1.0.1")
  .findOne()
  .await().indefinitely().orElse(null);

// Create form instances
IdAndRevision instance = tenant.createFormInstance()
  .formId(form.getId())
  .language("fi")
  .context(Map.of("SocialSecurityNumber", "anon"))
  .build()
  .await().indefinitely();

// Apply user actions
FormInstance updated = tenant.mergeFormInstance()
  .formInstanceId(instance.getId())
  .props(userActions)
  .build()
  .await().indefinitely();
```

### File Uploads
- Form defines `controlType: "fileUpload"` 
- Form instance stores file reference ID
- Actual file storage handled by external system (Azure, Google Cloud, etc.)

## Common Patterns

### Authentication-Aware Forms
```javascript
// Show login prompt for anonymous users
authentication in ('yes') and SocialSecurityNumber = "anon"

// Enable features for authenticated users
SocialSecurityNumber != "anon"
```

### Cascading Dropdowns
```javascript
// Second dropdown appears after first selection
mainCategory is answered

// Third dropdown depends on second selection
subCategory in ('option1', 'option2') and subCategory is answered
```

### Conditional Validation
```javascript
// Field required only in certain contexts
mainList in ('employment') and employmentType is answered

// Dynamic validation rules  
age >= 18 or parentalConsent is answered
```

## Expression Rule Types

### Validation Rules

**Purpose**: Define error conditions that prevent form submission
**Logic**: TRUE = Show error message, FALSE = Allow progression
**Location**: "Validations" tab in Composer

```javascript
// Age restriction validation
answer < 18                          // Error: "Must be 18 or older"

// Format validation with regex  
answer not matches "^EE[0-9]{9}$"    // Error: "Invalid Estonian VAT format"

// Length validation
lengthOf(answer) < 8                 // Error: "Password too short"

// Cross-field validation
endDate < startDate                  // Error: "End date must be after start date"

// Time-based validation  
appointmentTime < now() + 2 hours    // Error: "Must book at least 2 hours ahead"
```

### Visibility Rules

**Purpose**: Control when fields/groups are shown
**Logic**: TRUE = Show field, FALSE = Hide field
**Location**: "Rules" tab in Composer

```javascript
// Show based on previous answer
mainList is answered

// Show for specific selections
mainList in ('cityService', 'school')

// Show for authenticated users
SocialSecurityNumber != "anon"

// Language-specific visibility
language = 'fi' and userType = 'citizen'

// Complex conditional visibility
(age >= 18 and hasLicense = true) or (age < 18 and parentPresent = true)
```

### Required Rules  

**Purpose**: Make fields mandatory based on conditions
**Logic**: TRUE = Field required, FALSE = Field optional
**Location**: "Rules" tab in Composer

```javascript
// Always required
true

// Conditionally required
mainList in ('employment') and employmentType is answered

// Required for specific user types
userType = 'business' and country = 'finland'

// Age-dependent requirements
age < 18  // Parental consent required for minors
```

## Practical Examples

### Multi-Level Form Flow

```javascript
// Main category selection
mainList: Choice list (cityService, employment, education, etc.)

// Second level - employment subcategories  
employmentMainList: 
  visibility: mainList = 'employment' and mainList is answered
  
// Third level - specific employment services
jobSearchServices:
  visibility: employmentMainList = 'jobSearch' and employmentMainList is answered
  
// Fourth level - detailed information
jobSearchLocation:
  visibility: jobSearchServices is answered
  required: jobSearchServices in ('localSearch', 'regionalSearch')
```

### Complex Validation Scenarios

```javascript
// Estonian VAT number with helpful message
vatNumber:
  validation: answer not matches "^EE[0-9]{9}$"
  message: "Estonian VAT number must start with 'EE' followed by 9 digits (e.g., EE123456789)"

// Age verification with multiple conditions
birthDate:
  validation: today() - answer < 18 years or answer > today()
  message: "Must be 18 or older and birthdate cannot be in the future"
  
// Appointment scheduling with business hours
appointmentTime:
  validation: answer < "09:00" or answer > "17:00" or 
             (appointmentDate - today() < 1 day)
  message: "Appointments must be during business hours (9AM-5PM) and at least 1 day in advance"
```

### File Upload Patterns

```javascript
// Conditional file upload
hasAttachment: boolean field
  label: "Do you want to attach a document?"
  
attachmentFile: text field with fileUpload
  visibility: hasAttachment = true
  required: hasAttachment = true and documentType in ('contract', 'invoice')
  
// File upload with format validation (if supported)
documentUpload:
  validation: lengthOf(answer) = 0  // No built-in file validation in DEL
  message: "Please attach a document"
```

### Internationalization Patterns

```javascript
// Language-specific field visibility
finnishSsnField:
  visibility: language = 'fi' and nationality = 'finnish'
  validation: answer not matches "(\d{2})(\d{2})(\d{2})([+-A])\d{3}[0-9A-Z]"
  message: "Invalid Finnish personal ID format"
  
swedishSsnField:
  visibility: language = 'sv' and nationality = 'swedish'
  
// Multi-language error messages in value sets
errorMessages: value set with entries like:
  - id: "ageError_fi", label: { "fi": "Ikä on pakollinen" }
  - id: "ageError_en", label: { "en": "Age is required" }
```

## Field Type Validation Examples

### Number Field Validations
```javascript
// Range validation
age:
  type: "number"
  validation: answer < 1 or answer > 120
  message: "Age must be between 1 and 120"
  
// Comparison validation  
quantity:
  type: "number"
  validation: answer <= 0
  message: "Quantity must be greater than 0"
```

### Decimal Field Validations
```javascript
// Precision validation
price:
  type: "decimal"
  validation: answer <= 0 or answer > 1000000
  message: "Price must be between 0 and 1,000,000"
  
// Negative value check
balance:
  type: "decimal" 
  validation: answer < 0
  message: "Balance cannot be negative"
```

### Date Field Validations
```javascript
// Past date restriction
eventDate:
  type: "date"
  validation: answer < today()
  message: "Event date cannot be in the past"
  
// Date range validation
birthDate:
  type: "date"
  validation: answer > today() or today() - answer < 18 years
  message: "Must be 18 or older and birthdate cannot be future"
  
// Business day validation  
appointmentDate:
  type: "date"
  validation: answer < today() + 1 day
  message: "Appointment must be at least 1 day in advance"
```

### Time Field Validations
```javascript
// Business hours validation
meetingTime:
  type: "time"
  validation: answer < "09:00" or answer > "17:00"
  message: "Meeting must be during business hours (9 AM - 5 PM)"
  
// Time comparison
startTime:
  type: "time"
  
endTime:
  type: "time"
  validation: answer <= startTime
  message: "End time must be after start time"
```

### Choice Field Logic
```javascript
// Single selection logic
country:
  type: "choice"
  valueSetId: "countryList"
  
// Dependent field based on choice
stateProvince:
  type: "choice" 
  visibility: country in ('usa', 'canada')
  required: country in ('usa', 'canada')
  
// Validation based on choice
ageVerification:
  type: "number"
  validation: country = 'finland' and answer < 18
  message: "Must be 18 or older in Finland"
```

### Multi-Choice Field Logic
```javascript
// Selection count validation
skills:
  type: "multichoice"
  validation: count(skills) < 2 or count(skills) > 5
  message: "Please select 2-5 skills"
  
// Conditional requirements based on selections
experience:
  type: "number"
  visibility: "programming" in skills
  required: "programming" in skills
  
// Exclusion logic
preferences:
  type: "multichoice"
  validation: "vegetarian" in answer and "meat" in answer
  message: "Cannot select both vegetarian and meat options"
```

### Multi-Row Field Operations
```javascript
// Row count validation
familyMembers:
  type: "rowgroup" 
  items: ["firstName", "lastName", "age"]
  validation: count(familyMembers) > 10
  message: "Maximum 10 family members allowed"
  
// Sum validation in row group
expenses:
  type: "rowgroup"
  items: ["category", "amount"]
  validation: sum of amount > 10000
  message: "Total expenses cannot exceed 10,000"
  
// Conditional row requirements
incomeSource:
  type: "text"
  required: any of hasIncome  // Required if any row has income marked
```

### Survey Field Logic
```javascript
// Survey response validation
serviceSurvey:
  type: "survey"
  items: ["friendliness", "speed", "quality"]
  valueSetId: "ratingScale"  // Poor, Good, Excellent
  
// Follow-up based on survey response
improvementSuggestion:
  type: "text"
  visibility: serviceSurvey = "poor"
  required: serviceSurvey = "poor"
  
// Multiple survey criteria
overallSatisfaction:
  type: "choice"
  visibility: friendliness is answered and speed is answered and quality is answered
```

## Form Lifecycle Management

### Version Control & Tagging

Dialob supports systematic form versioning through a tagging system that enables form evolution and branching strategies.

**Tagging Concepts:**
- **Tags**: Named snapshots of form state at specific points in time
- **Immutable**: Tagged versions cannot be modified
- **"Latest Version"**: Only editable version, created as copy after tagging
- **Linear vs Branching**: Two different versioning approaches

### Linear Tagging

**Sequential evolution of a single form:**

```
v1.0 → v2.0 → v3.0 → Latest Version
```

**Use Case:** Core form that evolves over time
- Create v1.0 with 10 core questions
- Create v2.0 based on v1.0 + 20 additional questions  
- Create v3.0 from v2.0 with modified existing questions
- Result: Evolutionary progression of the same form

### Branching

**Multiple variations based on common foundation:**

```
         ┌─ v2.0 (Customer A)
v1.0 ────┼─ v3.0 (Customer B)  
         └─ v4.0 (Customer C)
```

**Use Case:** Different forms sharing core elements
- Create v1.0 with 10 core questions
- Create variants for different user bases/requirements
- Each branch modifies core questions for specific needs
- Result: Multiple specialized forms from single foundation

### Version Management Operations

**Creating Linear Tags:**
1. Navigate to "Version" menu in Composer
2. Select "Create version tag"
3. Enter tag name and optional description
4. New immutable version created, "Latest Version" becomes editable copy

**Managing Versions:**
1. "Version" → "Manage versions" 
2. View all tags with creation dates and creators
3. "Activate" to switch between versions
4. "Download" tags as JSON files
5. "Copy" to create new dialogs from existing tags

### Multi-User Considerations

**Concurrent Access:**
- Multiple users can work with different versions simultaneously
- Session IDs tied to user login maintain version isolation
- Real-time editing by multiple users on same session **not supported**
- Results in unpredictable behavior - avoid concurrent editing

**Production Environment:**
- Editor can modify form while users fill current version
- Form fillers continue with their session version
- Changes visible in real-time during active sessions

## Internationalization & Localization

### Multi-Language Support

Dialob provides comprehensive internationalization through the Translation feature supporting all ISO 639-1 languages.

### Translation Management

**Translation Dialog Tabs:**

1. **Manage Translation Files**
   - Download/upload JSON translation files
   - External translator workflow support
   - Bulk translation file management

2. **Manage Languages** 
   - Add/remove supported languages
   - Switch active language for editing
   - Copy translations between languages

3. **Missing Translations**
   - Identify untranslated content
   - Individual and bulk translation tools
   - Progress tracking for incomplete translations

### Language Creation Methods

**Copy from Active:**
- Duplicates current language content to new language
- Provides complete coverage (no empty fields)
- Requires manual translation of copied content
- Ensures no missing text during development

**Create Empty:**
- Creates blank translation set for new language
- Clean slate for native translation
- Risk of empty fields if translation incomplete
- Better for professional translator workflow

### Translation Workflow

**Adding New Language:**
1. Navigate to "Translations" in top menu
2. Select "Manage languages" tab
3. Choose language from ISO 639-1 dropdown
4. Select creation method (Copy/Empty)
5. Begin translating content

**File-Based Translation:**
1. Download translation JSON from "Manage translation files"
2. Translate strings externally (professional translators)
3. Upload completed translation file
4. System validates and applies translations
5. Review using "Missing translations" tab

### AI-Powered Translation

**Automated Translation Features:**
- Integration with external AI translation services
- Individual field translation buttons
- Bulk translation capabilities
- Preservation of markdown and DEL syntax
- Translation validation and review workflow

**AI Translation Indicators:**
- Visual flags on AI-translated content
- Source language and timestamp tracking
- Human validation workflow
- Removal of indicators after review

### Translation Content Types

**Translatable Elements:**
- Item labels (questions, groups, pages)
- Item descriptions  
- Validation messages
- Value set entries (choice options)
- Note content and instructions

**Translation ID Format:**
```
i:{itemId}:l                    // Item label
i:{itemId}:d                    // Item description  
i:{itemId}:v:{ruleIndex}        // Validation message
vs:{valueSetId}:{entryIndex}:{entryId}  // Value set entry
```

### Form Download & Export

**JSON Export:**
- Complete form definition download
- Includes all translations and metadata
- Version-specific export capability
- Integration-ready format

**CSV Value Set Export:**
- Multi-language value set data
- Compatible with external editing tools
- ISO 639-1 language column headers
- Import/export workflow support

### Unique Identifiers & Naming

**ID Management:**
- Auto-generated unique identifiers
- Real-time uniqueness validation  
- Automatic reference updates when IDs change
- Case-sensitive naming (firstName ≠ FirstName)

**Naming Rules:**
- Must start with letter [a-z, A-Z]
- Can include numbers [0-9] 
- Cannot use DEL reserved words
- Must be globally unique within form
- Automatic conflict detection

**ID Change Process:**
1. Click on current ID in item editor
2. Enter new unique identifier
3. System validates uniqueness
4. Automatic update of all references
5. DEL expressions maintain correctness

## Form Development Workflow

### Item Editing & Management

**Item Editor Dialog:**

All item configuration is managed through a centralized editing dialog accessible via the hamburger icon → "Options".

**Editor Tabs:**

1. **Label Tab**
   - Localized labels for all supported languages
   - Markdown formatting support with preview
   - Primary display text for form fillers

2. **Description Tab**  
   - Additional explanatory text
   - Markdown formatting supported
   - Optional help text for complex fields

3. **Rules Tab**
   - Visibility rules (when to show item)
   - Required rules (when item is mandatory)  
   - Default value assignment
   - Written in DEL syntax

4. **Validations Tab** (Input types only)
   - Validation rules (error conditions)
   - Custom validation messages
   - Localized error text
   - Written in DEL syntax

5. **Choices Tab** (Choice/Survey types only)
   - Local list creation/editing
   - Global list assignment
   - Choice reordering (drag-and-drop)
   - Individual choice visibility rules

6. **Properties Tab**
   - Type-specific behavior settings
   - UI customization options
   - Advanced field properties

**Item Management Operations:**

**ID Editing:**
- Click "Edit" icon in dialog header
- Real-time uniqueness validation
- Automatic reference updates across form

**Type Switching:**
- Dropdown in dialog header (when applicable)
- Maintains compatible properties
- Preserves labels and rules where possible

**Item Reordering:**
- Tree view in left sidebar shows current hierarchy
- Drag-and-drop items to new positions
- Visual feedback during repositioning
- Maintains parent-child relationships

### Form Testing & Preview

**Live Preview Feature:**

**Accessing Preview:**
- Click "Preview" button in top-right corner
- Available throughout development process
- Requires error-free form state

**Preview Capabilities:**
- Real-time form behavior testing
- Conditional logic verification
- Multi-language testing (language dropdown)
- Context variable value assignment
- Full form interaction simulation

**Context Variable Handling:**
- Preview dialog appears when context variables exist
- Default values displayed for modification
- Test different variable scenarios
- Validate form behavior under various conditions

**Error Prevention:**
- Preview blocked if form contains errors
- Real-time validation feedback
- Error indicators in tree view
- Guided error resolution

### Form Organization Best Practices

**Hierarchical Structure:**
```
Form
├─ Page 1 (Introduction)
│  ├─ Welcome Group
│  │  ├─ Welcome Note
│  │  └─ Instructions Note
│  └─ Basic Info Group
│     ├─ First Name (text)
│     ├─ Last Name (text)
│     └─ Email (text)
├─ Page 2 (Details)
│  ├─ Personal Details Group
│  │  ├─ Birth Date (date)
│  │  ├─ Gender (choice)
│  │  └─ Nationality (choice)
│  └─ Contact Info Group
│     ├─ Phone Number (text)
│     └─ Address Group
│        ├─ Street (text)
│        ├─ City (text)
│        └─ Country (choice)
└─ Page 3 (Completion)
   └─ Summary Group
      ├─ Review Note
      └─ Confirmation (boolean)
```

**Organizational Guidelines:**

**Page Usage:**
- Use multiple pages for logical form sections
- Keep pages focused on related content
- Consider user cognitive load per page
- Enable progress tracking across pages

**Group Strategy:**
- Group related fields logically
- Use descriptive group labels
- Nest groups for complex relationships
- Balance group size and depth

**Item Placement:**
- Place instructions and help text as notes
- Group validation-related fields together
- Position conditional fields near their triggers
- Maintain consistent field ordering patterns

**Naming Conventions:**
- Use descriptive, meaningful IDs
- Follow consistent naming patterns
- Consider future maintenance needs
- Document complex logic relationships

### Output Types & Information Display

**Note Type (Information Display):**

Notes provide read-only information to form fillers without collecting data.

```json
{
  "id": "welcomeMessage",
  "type": "note",
  "label": {
    "fi": "# Tervetuloa lomakkeeseen\n\nTäytä kaikki pakolliset kentät.",
    "en": "# Welcome to the form\n\nPlease fill all required fields."
  },
  "activeWhen": "userType is answered"
}
```

**Note Capabilities:**
- **No Return Value**: Pure information display
- **Markdown Support**: Rich text formatting
- **Conditional Display**: Can have visibility rules
- **Localization**: Multi-language support
- **Variable Interpolation**: Can display calculated values

**Note Usage Patterns:**
```javascript
// Welcome messages
"# Welcome, {firstName}!"

// Dynamic instructions  
"Complete {remainingFields} more fields"

// Status information
"Your application status: {applicationStatus}"

// Calculated results
"Total cost: €{totalAmount}"
```

**Supported Input/Output Types Summary:**

**Input Types (Data Collection):**
- **Text/TextBox**: String data (single/multi-line)
- **Number**: Integer values only  
- **Decimal**: Precision numeric values
- **Boolean**: True/false selections
- **Date**: Date picker (yyyy-mm-dd)
- **Time**: Time picker (hh:mm:ss)
- **Choice**: Single selection from list
- **Multi-Choice**: Multiple selections from list
- **Survey**: Matrix-style ratings
- **Multi-Row**: Repeating field groups
- **Address**: Location with autocomplete (optional)

**Output Types (Information Display):**
- **Note**: Read-only information display

**Specialized Elements:**
- **Survey Items**: Individual questions within survey groups
- **Groups**: Organizational containers (all types)
- **Pages**: Root-level containers

Each type has specific return formats, validation capabilities, and usage patterns optimized for different data collection needs.