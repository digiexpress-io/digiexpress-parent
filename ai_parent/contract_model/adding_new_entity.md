# Adding New Entity to Contract System

This document provides a comprehensive guide for adding a new entity to the contract management system, based on the patterns established by the ContractDateRelativity implementation.

## Entity Creation Tree Structure

### 1. **Core Entity Foundation**
```
📁 entities/
├── 📄 NewEntity.java                    # Main entity interface (@Value.Immutable)
├── 📄 ContractDocType.java              # Add enum value
└── 📄 ContractEntity.java               # Base interface (usually inherited)
```

### 2. **Database Layer**
```
📁 tables/
├── 📄 NewEntityTable.java               # Database table definition
│   ├── DDL with commit tracking fields
│   ├── SQL queries with commit JOINs  
│   ├── Row mappers with transitives
│   └── Insert/Update mappers
└── 📄 ContractDbBuilder.java            # Generated - adds persistence methods
    ├── getNewEntityInserts()
    ├── getNewEntityUpdates()  
    └── getNewEntityDeletes()
```

### 3. **API Layer (Root Interfaces)**
```
📁 api/
├── 📄 ThenaContractContainers.java      # Add List<NewEntity> getNewEntities()
├── 📄 ThenaContractNewObject.java       # Add NewNewEntity interface & addNewEntity()
└── 📄 ThenaContractMergeObject.java     # Add MergeNewEntity interface & modify/remove methods
```

### 4. **Create SPI (Entity Creation)**
```
📁 spi/create/
├── 📄 NewNewEntityBuilder.java          # Implements NewNewEntity interface
│   ├── Follows NewNoteBuilder pattern
│   ├── Uses ContractCommitBuilder logger
│   ├── UUID generation and validation
│   └── Returns ImmutableNewEntity
└── 📄 NewContractBuilder.java           # Add addNewEntity() method
    ├── Consumer<NewNewEntity> pattern
    ├── Integration with persistence unit
    └── Container building logic
```

### 5. **Modify SPI (Entity Updates)**
```
📁 spi/modify/
├── 📄 MergeNewEntityBuilder.java        # Implements MergeNewEntity interface
│   ├── Follows MergeNoteBuilder pattern
│   ├── Equals comparison for change detection
│   ├── CommitId updates only if modified
│   └── Returns ImmutablePersistenceUnit
└── 📄 MergeContractBuilder.java         # Add add/modify/remove methods
    ├── addNewEntity(Consumer<NewNewEntity>)
    ├── modifyNewEntity(String id, Consumer<MergeNewEntity>)
    └── removeNewEntity(String id) - if delete supported
```

### 6. **TypeScript Interfaces**
```
📁 contract-types/
└── 📄 contract-types.ts                 # TypeScript mirror of Java entities
    ├── Add NewEntityId type
    ├── Add CONTRACT_NEW_ENTITY to ContractDocType union
    ├── Add NewEntityTransitives interface  
    ├── Add NewEntity interface
    └── Add newEntities: NewEntity[] to ContractContainer
```

### 7. **Commit & Logging Integration**
```
📁 spi/commitlog/
└── 📄 ContractCommitBuilder.java        # Auto-handles new entity logging
    ├── add(newEntity) for creates
    └── merge(oldEntity, newEntity) for updates
```

### 8. **Generated Files (Auto-created)**
```
📁 target/generated-sources/annotations/
├── 📄 ImmutableNewEntity.java           # Immutables-generated builder
├── 📄 ImmutableNewEntityTransitives.java
└── 📄 NewEntityTableImpl.java           # Table implementation
```

---

## Required Updates Checklist

### ✅ **Java Backend (10 files)**
1. **Entity Definition**: `entities/NewEntity.java`
2. **Doc Type Enum**: `entities/ContractDocType.java` 
3. **Database Table**: `tables/NewEntityTable.java`
4. **Container API**: `api/ThenaContractContainers.java`
5. **New Object API**: `api/ThenaContractNewObject.java`
6. **Merge Object API**: `api/ThenaContractMergeObject.java`
7. **Create Builder**: `spi/create/NewNewEntityBuilder.java`
8. **Modify Builder**: `spi/modify/MergeNewEntityBuilder.java`
9. **New Contract Integration**: `spi/create/NewContractBuilder.java`
10. **Merge Contract Integration**: `spi/modify/MergeContractBuilder.java`

### ✅ **TypeScript Frontend (1 file)**
11. **Type Definitions**: `contract-types/contract-types.ts`

---

## Key Patterns to Follow

### **Database Pattern**
- ✅ **Commit Tracking**: Always include `commit_id`, `created_commit_id`
- ✅ **Transitives**: Join with commit table for timestamps
- ✅ **No Direct Timestamps**: Use commit table joins instead

### **Builder Pattern**  
- ✅ **Fluent API**: Consumer-based configuration
- ✅ **Validation**: `RepoAssert.isTrue(built, ...)` before close
- ✅ **Change Detection**: equals() comparison in merge builders
- ✅ **UUID Generation**: `UUIDs.timeBased().toString()`

### **Naming Conventions**
- ✅ **Java**: PascalCase entities, camelCase methods
- ✅ **TypeScript**: Exact mirror of Java naming
- ✅ **Container Arrays**: Lowercase plural (e.g., `notes`, `parties`)
- ✅ **Method Names**: `addEntityName`, `modifyEntityName`, `removeEntityName`

### **Delete Support**
- ✅ **Most Entities**: Support deletes via `get*Deletes()` in PersistenceUnit
- ❌ **Date Relativity**: Permanent rules, throws `UnsupportedOperationException`

---

## Implementation Examples

### Entity Definition Template
```java
@Value.Immutable
@JsonSerialize(as = ImmutableNewEntity.class)
@JsonDeserialize(as = ImmutableNewEntity.class)
public interface NewEntity extends ContractEntity {
  String getId();
  String getContractId();
  
  String getCommitId();
  String getCreatedCommitId();
  
  // Transitive data from joins
  @Value.Auxiliary
  @Nullable NewEntityTransitives getTransitives();
  
  // Business fields here
  String getEntityField();
  Optional<String> getOptionalField();
  
  @Override 
  default ContractDocType getDocType() { 
    return ContractDocType.NEW_ENTITY; 
  }
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableNewEntityTransitives.class)
  @JsonDeserialize(as = ImmutableNewEntityTransitives.class)
  interface NewEntityTransitives {
    OffsetDateTime getCreatedAt();
    OffsetDateTime getUpdatedAt();
  }
}
```

### API Interface Template
```java
// In ThenaContractNewObject.java
interface NewNewEntity {
  NewNewEntity entityField(String entityField);
  NewNewEntity optionalField(@Nullable String optionalField);
  void build();
}

// In NewContract interface
NewContract addNewEntity(Consumer<NewNewEntity> newEntity);
```

### Builder Template
```java
public class NewNewEntityBuilder implements NewNewEntity {
  private final ContractCommitBuilder logger;
  private final ImmutableNewEntity.Builder next;
  private boolean built;
  
  // Constructor and fluent methods...
  
  public ImmutableNewEntity close() {
    RepoAssert.isTrue(built, () -> "you must call NewNewEntity.build() to finalize entity CREATE!");
    final var entity = next.build();
    this.logger.add(entity);
    return entity;
  }
}
```

### TypeScript Template
```typescript
export type NewEntityId = string;

export interface NewEntityTransitives {
  createdAt: string; // OffsetDateTime
  updatedAt: string; // OffsetDateTime
}

export interface NewEntity {
  id: NewEntityId;
  contractId: ContractId;
  commitId: CommitId;
  createdCommitId: CommitId;
  
  // Transitive data from joins
  transitives?: NewEntityTransitives;
  
  // Business fields
  entityField: string;
  optionalField?: string;
}

// Add to ContractContainer
export interface ContractContainer {
  // ... existing fields
  newEntities: NewEntity[];
}

// Add to ContractDocType
export type ContractDocType =
  | "CONTRACT"
  | "PARTY"
  // ... existing types
  | "NEW_ENTITY";
```

---

## Step-by-Step Implementation Guide

1. **Start with Entity Definition** (`entities/NewEntity.java`)
2. **Add to ContractDocType** (`entities/ContractDocType.java`)
3. **Create Database Table** (`tables/NewEntityTable.java`)
4. **Update Container API** (`api/ThenaContractContainers.java`)
5. **Update New Object API** (`api/ThenaContractNewObject.java`)
6. **Update Merge Object API** (`api/ThenaContractMergeObject.java`)
7. **Implement Create Builder** (`spi/create/NewNewEntityBuilder.java`)
8. **Implement Modify Builder** (`spi/modify/MergeNewEntityBuilder.java`)
9. **Update New Contract Builder** (`spi/create/NewContractBuilder.java`)
10. **Update Merge Contract Builder** (`spi/modify/MergeContractBuilder.java`)
11. **Mirror in TypeScript** (`contract-types/contract-types.ts`)

This systematic approach ensures all necessary components are created following established patterns and conventions.