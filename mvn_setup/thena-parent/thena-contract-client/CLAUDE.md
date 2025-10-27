# Finnish Insurance Contract System - AI Development Guide

This document contains the AI prompt and patterns for continuing development of the Finnish insurance contract system. Use this as context when working with AI assistants to maintain consistency with established patterns.

## System Overview

The system implements a Finnish insurance policy data model covering life insurance, pension plans, and loan coverage using:
- PostgreSQL database with versioned data model
- Java entities with Immutables pattern
- Annotation processing with @TenantSql
- Commit-based versioning system
- Business vs technical date differentiation

## Established Technical Patterns

### 1. Entity Structure Rules
- All primary keys named `id` (UUID type)
- Snake_case naming throughout (database and fields)
- All entities extend `ContractEntity` interface
- All entities implement `getDocType()` returning `ContractDocType` enum value
- Business dates expanded to 3 fields: `date`, `interval`, `type`
- Technical dates remain single DATE fields
- All tables have `commit_id` and `created_commit_id`
- Contract table additionally has `updated_tree_commit_id`

### 2. Transitive Pattern
All entities use transitive pattern for commit join data:
```java
@Value.Auxiliary
@Nullable EntityNameTransitives getTransitives();

@Value.Immutable
interface EntityNameTransitives {
  OffsetDateTime getCreatedAt();
  OffsetDateTime getUpdatedAt();
  // Virtual fields if needed (like contract_id for child entities)
}
```

### 3. Multi-FK Relations Pattern
For entities with multiple optional foreign keys (Note, Reference):
```java
@Nullable ContractOneOfRelations getRelations();
```
Located in `ContractEntity` as nested interface with `ContractRelationType` enum.

### 4. Table Mapping Rules
- Sequential order values in `@TenantSql.Table(order = N)`
- Required methods: `findAll()`, `findAllByContractId()`, `getById()`, `insertMany()`, `updateMany()`
- Commit joins use aliases: `updated_commit`, `created_commit`, `updated_tree_commit`
- Nullable fields mapped via variables with `Optional.ofNullable()`
- All variables use `final` keyword
- JSON fields use `io.vertx.core.json.JsonObject`
- Retrieved via `row.getJsonObject("field_name")`

### 5. Business Date Expansion
Original single date field becomes three fields:
```sql
-- Before
field_date DATE

-- After  
field_date DATE NOT NULL,
field_date_interval INTERVAL NOT NULL,
field_date_type VARCHAR(100) NOT NULL
```

Java entity:
```java
LocalDate getFieldDate();
Duration getFieldDateInterval(); 
String getFieldDateType();
```

## Current Entity Schema

### Completed Entities (order 0-8):
1. **Contract** (order=0) - Root entity with parent_contract_id, business dates
2. **Party** (order=1) - Standard entity with business dates  
3. **Coverage** (order=2) - Standard entity with business dates, insured_id FK
4. **Capability** (order=3) - Standard entity without business dates
5. **Reference** (order=4) - Multi-FK entity with ContractOneOfRelations
6. **Note** (order=5) - Multi-FK entity with ContractOneOfRelations  
7. **PaymentPlan** (order=6) - Standard entity with business dates
8. **InvPlan** (order=7) - Standard entity with business dates
9. **InvPlanAlloc** (order=8) - Child entity with virtual contract_id

### Entity Relationships:
```
Contract (root)
├── Party (contract_id FK)
├── Coverage (contract_id FK, insured_id FK → Party)  
├── Capability (contract_id FK)
├── PaymentPlan (contract_id FK, party_id FK → Party)
├── InvPlan (contract_id FK)
│   └── InvPlanAlloc (inv_plan_id FK, virtual contract_id)
├── Reference (contract_id FK + ContractOneOfRelations)
└── Note (contract_id FK + ContractOneOfRelations)
```

## AI Prompt for Modifications/Extensions

When working with AI to modify or extend this system, use this context:

---

**CONTEXT**: You are working on a Finnish insurance contract system with established patterns. The system uses PostgreSQL, Java with Immutables, and annotation processing with @TenantSql.

**ESTABLISHED RULES**:
1. All primary keys named "id" (UUID)
2. Snake_case naming everywhere
3. Business dates expanded to 3 fields (date, interval, type)
4. Technical dates remain single DATE fields
5. All entities have commit_id, created_commit_id
6. Contract has additional updated_tree_commit_id
7. Transitive pattern for commit joins
8. ContractOneOfRelations for multi-FK entities
9. Sequential order values in @TenantSql.Table annotations
10. Import jakarta.annotation.Nullable (not javax)
11. JSON fields use io.vertx.core.json.JsonObject
12. All variables final, nullable via Optional.ofNullable()

**CURRENT SCHEMA**: [Describe current entities: Contract, Party, Coverage, Capability, Reference, Note, PaymentPlan, InvPlan, InvPlanAlloc with their order values 0-8]

**PATTERNS TO FOLLOW**:
- Standard entities: Basic entity with contract_id FK, commit fields, business/technical dates
- Multi-FK entities: Use ContractOneOfRelations pattern (like Reference, Note)
- Child entities: No direct contract_id, virtual contract_id in transitives (like InvPlanAlloc)

**REQUEST**: [Your specific modification/addition request]

---

## Example Modification Requests

### Adding New Standard Entity
"Add new entity 'Premium' (order=9) with contract_id FK, premium_amount (BigDecimal), premium_status (String), and business dates for premium_due_date"

### Adding New Multi-FK Entity  
"Add new entity 'Attachment' (order=10) that can link to any existing entity using ContractOneOfRelations pattern, with attachment_name, attachment_type, attachment_data (JsonObject)"

### Adding New Child Entity
"Add new entity 'CoverageLimit' (order=11) as child of Coverage with coverage_id FK, limit_type, limit_amount, virtual contract_id in transitives"

### Modifying Existing Entity
"Add new field 'contract_priority' (Integer) to Contract entity and update all related mappers"

### Database Schema Changes
"Add new index on payment_plan table for payment_plan_frequency field"

## File Locations

- **Entities**: `src/main/java/io/resys/thena/contract/client/entities/`
- **Tables**: `src/main/java/io/resys/thena/contract/client/tables/`  
- **Schema**: `schema.sql`
- **Base interfaces**: `ContractEntity.java`, `ContractDocType.java`

## Development Commands

Use these for validation:
```bash
# Compile and check annotations
mvn compile

# Run tests if available  
mvn test

# Check for SQL syntax
# Review schema.sql for any syntax errors
```

## Notes for AI Assistant

- Always increment order values sequentially
- Follow exact naming patterns (snake_case, specific prefixes)
- Use established commit join patterns in SQL
- Maintain transitive pattern consistency
- Update ContractDocType enum when adding entities
- Never break existing foreign key relationships
- Test SQL joins work with existing commit table structure