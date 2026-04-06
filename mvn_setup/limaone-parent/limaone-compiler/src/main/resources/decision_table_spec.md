# Decision Table YAML Specification

## General/Basics

### Basic Structure
```yaml
name: table_name
description: Optional table description
hitPolicy: ALL | FIRST
valueSets:
  status: ACTIVE, INACTIVE, PENDING, BLOCKED
  priority: LOW, MEDIUM, HIGH, CRITICAL
  locale: en, fi, de, fr, es
table: |
  | column1:TYPE | column2:TYPE | -> output1:TYPE | output2:TYPE |
  |--------------|--------------|----------------|----------------|
  | value1       | value2       | result1        | result2        |
  | value3       | value4       | result3        | result4        |
```

### Hit Policy
- **`FIRST`** - Stop at first matching rule
- **`ALL`** - Evaluate all rules, return all matches

### Metadata
- **`name`** - Required. Table identifier
- **`description`** - Optional. Human-readable description

### Value Sets
- **`valueSets`** - Optional. Defines predefined values for columns
- **Format**: `columnName: value1, value2, value3`
- **Usage**: Can be referenced in expressions using `in[...]` operators

---

## Headers

### Column Types
Headers define the structure using `columnName:TYPE` syntax.

#### Input vs Output Columns
- **Input columns**: Left side of `->` arrow
- **Output columns**: Right side of `->` arrow

#### Supported Data Types

| Type | Description | Example |
|------|-------------|---------|
| `STRING` | Text values | `name:STRING` |
| `INTEGER` | Whole numbers | `age:INTEGER` |
| `LONG` | Large integers | `id:LONG` |
| `DECIMAL` | Decimal numbers | `price:DECIMAL` |
| `BOOLEAN` | True/false values | `active:BOOLEAN` |
| `DATE` | Date only | `startDate:DATE` |
| `DATE_TIME` | Date with time | `timestamp:DATE_TIME` |
| `INTL` | Internationalized text | `message:INTL` |

---

## Rows - Expression Syntax by Data Type

### STRING Type

#### Input Columns (Complex Expressions)
```yaml
| name:STRING     | status:STRING        | -> message:STRING |
|-----------------|---------------------|-------------------|
| John            | ACTIVE              | Welcome           |
| in["John","Jane"]| in["ACTIVE","PENDING"]| Processing      |
| qin["admin*"]   | !in["BLOCKED"]      | Admin Access      |
```

**String Operators:**
- **Exact match**: `John`
- **In list**: `in["value1", "value2", "value3"]`
- **Not in list**: `!in["blocked", "cancelled"]` 
- **Pattern match**: `qin["pattern*", "admin/#"]`
  - `*` = exactly one word
  - `#` = zero or more words
  - `/` or `.` = word separators

#### Output Columns (Simple Values)
```yaml
| input:STRING | -> result:STRING | message:STRING |
|--------------|------------------|----------------|
| test         | success          | Operation complete |
| error        | failure          | Something went wrong |
```

### INTEGER/LONG/DECIMAL Types

#### Input Columns (Complex Expressions)
```yaml
| age:INTEGER | income:DECIMAL | score:LONG | -> category:STRING |
|-------------|----------------|------------|-------------------|
| >= 18       | > 50000.00     | [80..100]  | Premium           |
| [13..17]    | [0..50000)     | < 50       | Junior            |
| = 65        | <= 25000       | >= 90      | Senior            |
```

**Number Operators:**
- **Comparison**: `=`, `<`, `<=`, `>`, `>=`
  - `= 42`, `>= 18`, `< 100`
- **Ranges**:
  - `[10..100]` - Inclusive both ends
  - `(0..50)` - Exclusive both ends  
  - `[10..100)` - Inclusive start, exclusive end
  - `(10..100]` - Exclusive start, inclusive end

#### Output Columns (Simple Values)
```yaml
| input:STRING | -> score:INTEGER | rating:DECIMAL | count:LONG |
|--------------|------------------|----------------|------------|
| excellent    | 95               | 4.8            | 1000000    |
| good         | 80               | 4.2            | 500000     |
```

### BOOLEAN Type

#### Input/Output Columns
```yaml
| isActive:BOOLEAN | hasPermission:BOOLEAN | -> allowed:BOOLEAN |
|------------------|----------------------|-------------------|
| true             | true                 | true              |
| false            | true                 | false             |
| true             | false                | false             |
```

**Boolean Values:**
- `true`
- `false`

### DATE/DATE_TIME Types

#### Input Columns (Complex Expressions)
```yaml
| startDate:DATE | endDate:DATE_TIME | -> status:STRING |
|----------------|-------------------|------------------|
| after 2023-01-01 | before 2024-12-31T23:59:59Z | Active |
| equals 2023-12-25 | between 2023-01-01T00:00:00Z and 2023-12-31T23:59:59Z | Holiday |
```

**Date Operators:**
- **`equals`**: `equals 2023-12-25`
- **`before`**: `before 2024-12-31`
- **`after`**: `after 2023-01-01`
- **`between`**: `between 2023-01-01 and 2023-12-31`

**Date Formats:**
- **DATE**: `YYYY-MM-DD` (e.g., `2023-12-25`)
- **DATE_TIME**: `YYYY-MM-DDTHH:MM:SSZ` (e.g., `2023-12-25T14:30:00Z`)

#### Output Columns (Simple Values)
```yaml
| input:STRING | -> effectiveDate:DATE | timestamp:DATE_TIME |
|--------------|----------------------|---------------------|
| start        | 2023-01-01           | 2023-01-01T00:00:00Z |
| end          | 2023-12-31           | 2023-12-31T23:59:59Z |
```

### INTL Type (Internationalization)

#### Input/Output Columns
```yaml
| locale:STRING | -> message:INTL |
|---------------|-----------------|
| en            | {"en": "Hello", "fi": "Hei", "de": "Hallo"} |
| fi            | {"en": "Goodbye", "fi": "Näkemiin", "de": "Auf Wiedersehen"} |
```

**INTL Format:**
- JSON object with locale codes as keys
- Locale format: `en`, `fi`, `de-DE`, `en-US`
- Validation: `/^[a-z]{2}(-[A-Z]{2})?$/`

### Special Values

#### Null/Empty Handling
```yaml
| optional:STRING | required:INTEGER | -> result:STRING |
|-----------------|------------------|------------------|
| null            | null             | Default          |
|                 | >= 1             | Valid            |
| any_value       | null             | Missing Data     |
```

**Special Values:**
- `null` - Explicit null value
- Empty cell - Also treated as null
- `""` - Empty string (for STRING types)

### STRING Type with Value Sets

```yaml
name: user_access_control
hitPolicy: FIRST
valueSets:
  status: ACTIVE, INACTIVE, PENDING, BLOCKED, SUSPENDED
  role: ADMIN, USER, GUEST, MODERATOR
  department: IT, HR, FINANCE, SALES, MARKETING
table: |
  | status:STRING        | role:STRING      | department:STRING | -> access:STRING | level:INTEGER |
  |---------------------|------------------|-------------------|------------------|---------------|
  | in["ACTIVE"]        | in["ADMIN"]      | any               | FULL             | 100           |
  | in["ACTIVE"]        | in["MODERATOR"]  | in["IT","HR"]     | ELEVATED         | 80            |
  | in["ACTIVE","PENDING"]| in["USER"]     | !in["FINANCE"]    | STANDARD         | 50            |
  | !in["BLOCKED","SUSPENDED"]| any        | any               | LIMITED          | 20            |
  | in["BLOCKED"]       | any              | any               | DENIED           | 0             |
```

### INTL Type with Locale Value Sets

```yaml
name: localization_messages
hitPolicy: ALL
valueSets:
  locale: en, fi, de, fr, es, sv, no, da
  messageType: WELCOME, ERROR, WARNING, INFO
table: |
  | locale:STRING | messageType:STRING | -> message:INTL |
  |---------------|-------------------|-----------------| 
  | in["en"]      | in["WELCOME"]     | {"en": "Welcome", "fi": "Tervetuloa", "de": "Willkommen"} |
  | in["fi"]      | in["ERROR"]       | {"en": "Error occurred", "fi": "Virhe tapahtui", "de": "Fehler aufgetreten"} |
  | any           | in["WARNING"]     | {"en": "Warning", "fi": "Varoitus", "de": "Warnung"} |
```

### Mixed Types with Value Sets

```yaml
name: product_pricing
description: Dynamic pricing based on category, customer tier, and region
hitPolicy: FIRST
valueSets:
  category: ELECTRONICS, CLOTHING, BOOKS, HOME, SPORTS
  customerTier: BRONZE, SILVER, GOLD, PLATINUM, DIAMOND
  region: US, EU, ASIA, LATAM, AFRICA
  season: SPRING, SUMMER, FALL, WINTER, HOLIDAY
table: |
  | category:STRING     | customerTier:STRING | basePrice:DECIMAL | region:STRING | season:STRING | -> finalPrice:DECIMAL | discount:DECIMAL | message:STRING |
  |--------------------|--------------------|-------------------|---------------|---------------|-------------------|------------------|----------------|
  | in["ELECTRONICS"]  | in["PLATINUM","DIAMOND"] | >= 1000    | in["US","EU"] | in["HOLIDAY"] | basePrice * 0.75  | 25.0             | Premium Holiday Deal |
  | in["CLOTHING"]     | in["GOLD","PLATINUM"]   | [100..500] | any           | in["FALL","WINTER"] | basePrice * 0.85 | 15.0           | Seasonal Discount |
  | any                | in["BRONZE"]            | < 50       | !in["ASIA"]   | any           | basePrice * 0.95  | 5.0              | Basic Discount |
  | any                | any                     | any        | in["ASIA"]    | any           | basePrice * 1.1   | 0.0              | Regional Adjustment |
```

### Value Set Validation Examples

#### Valid Usage
```yaml
name: valid_example
valueSets:
  status: ACTIVE, INACTIVE, PENDING
table: |
  | status:STRING | -> result:STRING |
  |---------------|------------------|
  | in["ACTIVE"]  | Processing       |
  | PENDING       | Waiting          | # Direct value from set
  | !in["INACTIVE"] | Not Inactive   |
```

#### Output Columns Using Value Sets
```yaml
name: output_with_valuesets
valueSets:
  priority: LOW, MEDIUM, HIGH, CRITICAL
  action: APPROVE, REVIEW, REJECT, ESCALATE
table: |
  | score:INTEGER | urgency:BOOLEAN | -> priority:STRING | action:STRING |
  |---------------|-----------------|-------------------|---------------|
  | >= 90         | true            | CRITICAL          | APPROVE       |
  | [70..89]      | any             | HIGH              | REVIEW        |
  | [50..69]      | false           | MEDIUM            | REVIEW        |
  | < 50          | any             | LOW               | REJECT        |
```

### Complete Example with All Features

```yaml
name: comprehensive_loan_approval
description: Complete loan approval system with all data types and value sets
hitPolicy: FIRST
valueSets:
  employment: PERMANENT, CONTRACT, TEMPORARY, UNEMPLOYED, SELF_EMPLOYED
  creditRating: EXCELLENT, GOOD, FAIR, POOR, NO_CREDIT
  loanType: MORTGAGE, PERSONAL, AUTO, BUSINESS, STUDENT
  region: NORTH, SOUTH, EAST, WEST, CENTRAL
  riskLevel: LOW, MEDIUM, HIGH, CRITICAL
table: |
  | age:INTEGER | income:DECIMAL | creditScore:INTEGER | employment:STRING | loanAmount:DECIMAL | loanType:STRING | region:STRING | hasCollateral:BOOLEAN | -> approved:BOOLEAN | interestRate:DECIMAL | riskLevel:STRING | maxAmount:DECIMAL | message:INTL |
  |-------------|----------------|--------------------|--------------------|-------------------|-----------------|---------------|---------------------|-------------------|------------------|------------------|---------------|--------------|
  | >= 25       | > 100000       | [750..850]         | in["PERMANENT"]    | [10000..500000]   | in["MORTGAGE","AUTO"] | any     | true                | true              | 2.5              | LOW              | 500000        | {"en": "Excellent rate approved", "fi": "Erinomainen korko hyväksytty"} |
  | >= 21       | [60000..100000]| [650..749]         | !in["UNEMPLOYED"]  | [5000..200000]    | !in["BUSINESS"] | in["NORTH","CENTRAL"] | any           | true              | 4.2              | MEDIUM           | 200000        | {"en": "Standard rate approved", "fi": "Vakiokorko hyväksytty"} |
  | >= 18       | [30000..59999] | [600..649]         | in["CONTRACT","SELF_EMPLOYED"] | < 50000 | any           | any           | false               | true              | 6.8              | HIGH             | 50000         | {"en": "Higher rate due to risk", "fi": "Korkeampi korko riskin vuoksi"} |
  | < 18        | any            | any                | any                | any               | any             | any           | any                 | false             | null             | CRITICAL         | 0             | {"en": "Age requirement not met", "fi": "Ikävaatimus ei täyty"} |
  | any         | < 30000        | < 600              | in["UNEMPLOYED"]   | any               | any             | any           | any                 | false             | null             | CRITICAL         | 0             | {"en": "Insufficient income/credit", "fi": "Riittämätön tulo/luotto"} |
```

**Key improvements with valueSets:**
1. **Cleaner syntax** - No need to repeat values in expressions
2. **Validation support** - Parser can validate values exist in sets
3. **Reusability** - Same value set can be used across multiple columns
4. **Maintenance** - Easy to update allowed values in one place
5. **Documentation** - Clear visibility of all possible values

---

## Implementation Notes

### CST ’ Commands ’ AST Flow
1. **Parse YAML** ’ Extract metadata, valueSets, and table markdown
2. **Parse Table** ’ Use flexmark to parse markdown table structure
3. **Build CST** ’ Create concrete syntax tree preserving structure
4. **Generate Commands** ’ Transform CST to decision table command sequence
5. **Build AST** ’ Apply commands to create executable decision table

### Command Types Required
- `SET_NAME` - Table name
- `SET_HIT_POLICY` - Execution policy
- `SET_VALUE_SET` - Predefined value sets
- `ADD_HEADER_IN` / `ADD_HEADER_OUT` - Column definitions
- `SET_HEADER_REF` / `SET_HEADER_TYPE` - Column properties
- `ADD_ROW` - Table rows
- `SET_CELL_VALUE` - Cell expressions

### Validation Rules
- Value set references must exist in `valueSets` section
- Data types must match column definitions
- Expression syntax must be valid for each data type
- Date formats must be ISO compliant
- Locale codes must follow pattern `/^[a-z]{2}(-[A-Z]{2})?$/`