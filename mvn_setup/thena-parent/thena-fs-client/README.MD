# Thena File System Client

A Git-like versioned file system implementation using PostgreSQL as the storage backend, designed to replace a 10-year-old production system with modern patterns and performance optimizations.

## Architecture Overview

This system implements a content-addressable storage model similar to Git, where:
- **Blobs** store file content with hash-based deduplication
- **Trees** represent directory snapshots containing nodes
- **Nodes** are file/folder entries within trees
- **Commits** link trees with versioning metadata
- **Refs** (branches) point to specific commits
- **Props** store metadata like permissions and labels
- **Tags** provide named references for releases

### Entity Relationships

```
Ref -> Commit -> Tree -> [Node...] -> Blob
                                  -> Props
Tag -> Commit
```

## Database Schema

### Multi-Tenant Design
Tables use placeholder naming with `{table_name}` for tenant-specific prefixes:
- `{blob}` becomes `tenant_blob`
- `{commit}` becomes `tenant_commit`
- etc.

### Custom Composite Types
PostgreSQL composite types enable embedded arrays:

```sql
CREATE TYPE {node} AS (
  node_path TEXT,
  node_name TEXT,
  blob_id TEXT,
  props_id TEXT
);
```

### Referential Integrity
Since PostgreSQL cannot validate foreign keys within composite types, custom triggers handle validation:

```sql
CREATE OR REPLACE FUNCTION {tree}_validate_tree() 
RETURNS TRIGGER AS $$
DECLARE
    missing_count INTEGER;
BEGIN
    -- Validate path + name uniqueness within the tree
    SELECT count(*) INTO missing_count
    FROM (
        SELECT nodes.node_path, nodes.node_name, count(*)
        FROM unnest(NEW.tree_nodes) nodes
        GROUP BY nodes.node_path, nodes.node_name
        HAVING count(*) > 1
    ) duplicates;

    IF missing_count > 0 THEN
        RAISE EXCEPTION 'Validation failed: % duplicate path+name combinations in tree', missing_count;
    END IF;

    -- Validate blob_id and props_id references exist
    -- ... additional validation logic

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
```

## Entity Patterns

### Immutable Value Objects
All entities use the Immutables library pattern:

```java
@Value.Immutable
@JsonSerialize(as = ImmutableBlob.class)
@JsonDeserialize(as = ImmutableBlob.class)
public interface Blob extends FileSystemEntity {
  String getId();
  String getBlobType();
  JsonObject getBlobValue();
  
  @Value.Auxiliary
  @Nullable 
  BlobTransitives getTransitives();
}
```

### Transitives Pattern
Optional join data loaded separately for performance:

```java
@Value.Immutable
interface BlobTransitives {
  OffsetDateTime getCreatedAt();
  OffsetDateTime getUpdatedAt();
}
```

### Entity Types
```java
public enum FileSystemEntityType {
  BLOB, TREE, COMMIT, REF, PROPS, TAG, NODE
}
```

## API Design

### Query Interface Hierarchy
```java
public interface FsDbQuery {
  Uni<World> findAll();
  
  BlobQuery queryBlob();
  TreeQuery queryTree();
  CommitQuery queryCommit();
  RefQuery queryRef();
  TagQuery queryTag();
  PropsQuery queryProps();
  NodeQuery queryNode();
}
```

### Expression Builder Pattern
Dynamic SQL generation with streaming builders:

```java
public interface NameExpressionBuilder {
  NameExpressionBuilder startsWith(String prefix);
  NameExpressionBuilder endsWith(String suffix);
  NameExpressionBuilder contains(String substring);
  NameExpressionBuilder equals(String exact);
  NameExpressionBuilder in(List<String> values);
}
```

Implementation with parameter management:
```java
public NameExpressionBuilder startsWith(String prefix) {
  final int paramPos = sqlProps.apply(prefix + "%");
  appendCondition(sqlColumnName + " LIKE $" + paramPos);
  return this;
}
```

### Filter Implementation
SQL builders with parameter tracking:

```java
@Value.Immutable
@JsonSerialize(as = ImmutableRefTableFilter.class)
@JsonDeserialize(as = ImmutableRefTableFilter.class)
public interface RefTableFilter {
  Optional<Consumer<NameExpressionBuilder>> getRefName();
  Optional<Consumer<NameExpressionBuilder>> getCommitId();
  Optional<List<String>> getCommitIds();
  
  default String toSqlWhere(List<Object> params) {
    // SQL generation logic with parameter management
  }
}
```

## Builder Patterns

### Commit Builder
Two-tier validation pattern:

```java
public interface CommitBuilder {
  CommitBuilder branchHead(String commitId);
  CommitBuilder branchName(String branchName);
  CommitBuilder newFolder(Consumer<NewFolder> doc);
  CommitBuilder mergeFolder(String docId, Consumer<MergeFolder> doc);
  CommitBuilder newFile(Consumer<NewFile> doc);
  CommitBuilder mergeFile(String docId, Consumer<MergeFile> doc);
  CommitBuilder remove(String docId);
  CommitBuilder commitAuthor(String author);
  CommitBuilder commitMessage(String message);
  
  Uni<CommitResult> build();
}
```

Implementation with validation:
```java
@Override
public CommitBuilder commitAuthor(String author) {
  RepoAssert.notEmpty(author, () -> "commitAuthor can't be empty!");
  this.commitAuthorValue = author;
  return this;
}

@Override
public Uni<CommitResult> build() {
  // Double validation - ensure required fields were set
  RepoAssert.notEmpty(commitAuthorValue, () -> "commitAuthor must be set before calling build()!");
  RepoAssert.notEmpty(commitMessageValue, () -> "commitMessage must be set before calling build()!");
  
  // Ensure either branch name or branch head is set
  RepoAssert.isTrue(branchNameValue != null || branchHeadId != null, 
      () -> "Either branchName or branchHead must be set before calling build()!");
      
  // Build and execute transaction
  return db_uni.onItem().transformToUni(db -> db.withTransaction(scope, this::visitTransaction));
}
```

### Tag Builder
Rich metadata support:

```java
public interface TagBuilder {
  TagBuilder tagName(String tagName);
  TagBuilder tagAuthor(String tagAuthor);
  TagBuilder tagDescription(String description);
  TagBuilder tagMessage(String message);
  TagBuilder commitId(String commitId);
  TagBuilder externalId(String externalId);
  TagBuilder tagStartsAt(OffsetDateTime startsAt);
  TagBuilder tagEndsAt(OffsetDateTime endsAt);
  
  Uni<TagResult> build();
}
```

### Branch Builder
Extended metadata for branch management:

```java
public interface BranchBuilder {
  BranchBuilder branchName(String branchName);
  BranchBuilder branchAuthor(String branchAuthor);
  BranchBuilder branchDescription(String description);
  BranchBuilder branchProps(JsonObject props);
  BranchBuilder branchPermissions(JsonObject permissions);
  BranchBuilder branchFlags(JsonObject flags);
  BranchBuilder fromCommitId(String commitId);
  
  Uni<BranchResult> build();
}
```

## Concurrency Control

### Pessimistic Locking
Non-blocking locks for concurrent operations:

```java
private Uni<Optional<Ref>> visitLock(FsDb tx) {    
  final var query = ImmutableRefTableLockFilter.builder()
    .refName(branchNameValue)
    .docIds(Optional.ofNullable(allIds.isEmpty() ? null : allIds))
    .build();
  return tx.query().queryRef().findOneWithLock(query);
}
```

SQL generation:
```sql
SELECT * FROM {ref} 
WHERE ref_name = $1 
FOR UPDATE NOWAIT
```

## Performance Optimizations

### Content Deduplication
Hash-based blob storage prevents duplicate content:
```java
// Same content = same hash = same blob ID
String blobId = HashUtil.sha1(content);
```

### Lazy Loading
Transitives loaded only when needed:
```java
@Value.Auxiliary
@Nullable 
BlobTransitives getTransitives();
```

### Batch Operations
Array-based node storage reduces table joins:
```sql
-- Nodes stored as array in tree table instead of separate node table
tree_nodes {node}[]
```

## Development Tools

### FileSystem Printer
Debug database state with static/real modes:

```java
public class FileSystemPrinter {
  public enum PrintingMode {
    IMAGINARY, // Static IDs/timestamps for tests
    REAL       // Actual values
  }
  
  public String printSync() {
    // Comprehensive database state visualization
  }
}
```

Output format:
```
FileSystem World
References (Branches):
  - main -> 1 (Initial commit) by system
Commits:
  - 1 by system at "OffsetDateTime.now()"
    tree: 1
    message: Initial commit
Trees:
  - Tree 1 (2 nodes)
    /README.md -> blob:1
    /src (folder)
```

## Usage Examples

### Creating a Commit
```java
commitBuilder
  .branchName("feature-branch")
  .commitAuthor("developer@company.com")
  .commitMessage("Add new feature")
  .newFile(file -> file
    .docPath("/src/main/java")
    .docName("NewFeature.java")
    .docValue(JsonObject.of("content", sourceCode)))
  .mergeFile("existing-file-id", file -> file
    .docValue(JsonObject.of("content", updatedContent)))
  .remove("old-file-id")
  .build();
```

### Querying Files
```java
// Find blobs by type
blobQuery.findAllByType("java-source");

// Find commits in tree
commitQuery.findAllByTreeId("tree-123");

// Complex ref filtering
refQuery.findAllByFilter(ImmutableRefTableFilter.builder()
  .refName(name -> name.startsWith("feature/"))
  .commitIds(List.of("commit1", "commit2"))
  .build());
```

### Creating Tags
```java
tagBuilder
  .tagName("v1.0.0")
  .tagAuthor("release-manager@company.com")
  .tagDescription("Production release")
  .commitId("commit-hash")
  .externalId("JIRA-123")
  .tagStartsAt(OffsetDateTime.now())
  .build();
```

## Technology Stack

- **Java 21** with Jakarta EE annotations
- **PostgreSQL** with custom types and triggers
- **Vert.x** for JsonObject handling and reactive programming
- **Smallrye Mutiny** for reactive streams
- **Immutables** library for value objects
- **Jackson** for JSON serialization
- **Thena SQL** annotation processor for code generation

## Configuration

### Database Connection
Multi-tenant table naming configured via Thena SQL:
```java
@TenantSql.Table(
  name = "blob",
  order = 200,
  ddl = "CREATE TABLE {blob} (...)"
)
```

### Reactive Configuration
All operations return `Uni<T>` or `Multi<T>` for non-blocking execution:
```java
Uni<CommitResult> result = commitBuilder.build();
result.subscribe().with(
  success -> log.info("Commit created: {}", success.getCommitId()),
  failure -> log.error("Commit failed", failure)
);
```

## Best Practices

### Validation Patterns
- Method-level validation with `RepoAssert.notEmpty()`
- Build-time validation before transaction
- Database-level constraint validation via triggers

### Error Handling
```java
return db_uni.onItem().transformToUni(db -> db.withTransaction(scope, this::visitTransaction))
  .onItem().transform(this::visitSuccess)
  .onFailure().recoverWithItem(this::visitFailure);
```

### Resource Management
- Use transactions for multi-table operations
- Implement proper connection pooling
- Handle concurrent access with appropriate locking

### Testing
- Use `PrintingMode.IMAGINARY` for consistent test output
- Leverage the FileSystemPrinter for state verification
- Test trigger validation with edge cases

## Troubleshooting

### Common Issues

**Trigger Performance**: Monitor trigger execution time during bulk operations
```sql
-- Check trigger timing
EXPLAIN ANALYZE INSERT INTO {tree} VALUES (...);
```

**Lock Contention**: Use `FOR UPDATE NOWAIT` to avoid blocking
```java
// Handle lock failures gracefully
.onFailure(LockException.class).recoverWithItem(fallbackStrategy)
```
