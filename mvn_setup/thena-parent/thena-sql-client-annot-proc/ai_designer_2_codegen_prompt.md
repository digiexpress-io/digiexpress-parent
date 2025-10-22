# Thena SQL Annotation Processor - Architecture & Implementation Guide

  ## Overview
  This is a Java 21 annotation processor that generates SQL repository code using JavaPoet. It processes `@TenantSql` annotations to create type-safe, tenant-aware database access layers with reactive programming support (Smallrye Mutiny).

  ## Architecture

  ### Core Concepts

  **Tenant-Aware SQL**: All table names use `{table_name}` placeholders that get replaced with tenant-prefixed names at runtime. This enables multi-tenancy where each tenant has isolated database tables.

  **Generated Code Package Structure**:
  - Base package: Contains interfaces (e.g., `Batch2DbQuery`, `Batch2DbBuilder`)
  - `.spi` sub-package: Contains all implementations (e.g., `Batch2DbQueryImpl`, `BatchTableNames`, `BatchRegistry`)

  **Reactive Programming**: Uses Smallrye Mutiny with `Uni<T>` for single results and `Multi<T>` for streaming results.

  ### Annotation Types

  1. **`@TenantSql.Registry`** - Marks a package/interface as registry holder
     - Generates: TableNames class, Registry factory, Db interfaces and implementations

  2. **`@TenantSql.Table`** - Marks an interface as a table definition
     - Properties: `name`, `order`, `ddl`, `constraints`, `drop`
     - Generates: Table implementation with all CRUD methods

  3. **Query Annotations**:
     - `@Find` - Single result: `Uni<Entity>` or `Uni<Optional<Entity>>`
     - `@FindAll` - Multiple results: `Uni<List<Entity>>` or `Multi<Entity>`
     - Properties: `sql`, `rowMapper`, `optional`, `wrapper`, `sqlBuilder`

  4. **Mutation Annotations**:
     - `@Insert` / `@InsertAll` - Insert operations
     - `@Update` / `@UpdateAll` - Update operations
     - `@Delete` / `@DeleteAll` - Delete operations
     - Properties: `sql`, `propsMapper`

  ### Functional Interfaces

  ```java
  // Transforms Row to entity
  interface RowMapper<T> {
    T apply(Row row);
  }

  // Transforms entity to Tuple for parameters
  interface PropsMapper<T> {
    Tuple apply(T object);
  }

  // Custom SQL builder (optional, only when method has 1 parameter)
  interface SqlBuilder<T> {
    SqlTuple apply(Tenant tenant, T parameter);
  }

  Generated Code Structure

  1. Table Implementation ({Table}Impl.java)

  Location: {package}.spi

  Purpose: Implements individual table operations

  Key Features:
  - SQL Builder Support: When sqlBuilder is specified on @Find/@FindAll with exactly 1 parameter:
    a. Instantiates the custom SQL builder
    b. Calls sqlBuilder.apply(dataSource.getTenant(), parameter)
    c. Extracts value and props from returned SqlTuple
    d. Performs case-insensitive table name replacement: {table_name} → tenant-prefixed name
    e. Wraps in ImmutableSqlTuple with row mapper attached
  - Standard SQL Generation: When no custom builder:
    - Uses template SQL with table name placeholders
    - Resolves placeholders at compile time where possible
    - Returns Sql (no params), SqlTuple (single params), or SqlTupleList (batch params)

  Fields:
  - tables - TableNames instance for tenant-prefixed names
  - dataSource - Database connection
  - errorHandler - Error handling

  Example Generated Method:
  @Override
  public SqlTuple findByAppId(String appId) {
    final var sqlBuilder = new CustomSqlBuilder();
    final var baseSql = sqlBuilder.apply(dataSource.getTenant(), appId);

    var sqlValue = baseSql.getValue();
    sqlValue = sqlValue.replaceAll("(?i)\\{batch_consumer\\}", tables.getBatchConsumer());

    return ImmutableSqlTuple.builder()
      .value(sqlValue)
      .props(baseSql.getProps())
      .rowMapper(new BatchConsumerMapper())
      .build();
  }

  2. Table Names ({Name}TableNames.java)

  Location: {package}.spi

  Purpose: Manages tenant-prefixed table names

  Key Methods:
  - defaults() - Creates default names without tenant prefix
  - toRepo(Tenant tenant) - Creates tenant-prefixed names
  - get{TableName}() - Returns actual table name string

  Example:
  public String getBatchConsumer() {
    return tenantPrefix + "batch_consumer";
  }

  3. Registry Factory ({Name}Registry.java)

  Location: {package}.spi

  Purpose: Factory for creating table implementations

  Key Methods:
  - get{TableNamePlural}() - Returns table implementation instance
  - Each getter instantiates the table impl with names, dataSource, errorHandler

  Example:
  public BatchConsumersImpl getBatchConsumers() {
    return new BatchConsumersImpl(names, dataSource, dataSource.getErrorHandler());
  }

  4. Query Interface ({Name}DbQuery.java)

  Location: Base package

  Purpose: Defines query operations API

  Structure:
  - findAll() - Returns Uni<World> with all data
  - query{Table}() - Returns nested query interface for each table
  - Nested interfaces: {Table}Query with all find methods
  - World interface: Immutable container with Map<String, Entity> for each table

  Example:
  public interface Batch2DbQuery {
    Uni<World> findAll();

    BatchConsumersQuery queryBatchConsumers();

    interface BatchConsumersQuery {
      Uni<List<BatchConsumer>> findAllByAppId(String appId);
      Multi<BatchConsumer> streamAll(); // when wrapper = MULTI
    }

    @Value.Immutable
    interface World {
      Map<String, BatchConsumer> getBatchConsumers();
      // ... other tables
    }
  }

  5. Query Implementation ({Name}DbQueryImpl.java)

  Location: {package}.spi

  Purpose: Implements query operations with reactive execution

  Key Features:

  Constructor:
  public Batch2DbQueryImpl(ThenaSqlDataSource dataSource) {
    this.dataSource = dataSource;
    final var names = BatchTableNames.defaults().toRepo(dataSource.getTenant());
    this.registry = new BatchRegistry(names, dataSource);
    this.errorHandler = dataSource.getErrorHandler();
  }

  Query Execution Pattern:
  1. Get SQL from registry: registry.getBatchConsumers().findAllByAppId(appId)
  2. Log SQL if debug enabled
  3. Extract row mapper with @SuppressWarnings("unchecked")
  4. Execute via dataSource.getClient().preparedQuery(sql.getValue())
  5. Map results using .mapping(mapper::apply)
  6. Transform based on return type:
    - SELECT + optional=true: Optional.of() or Optional.empty()
    - SELECT + optional=false: Return entity or throw {Name}FindException
    - SELECT_ALL + wrapper=MULTI: .transformToMulti(rowset -> Multi.createFrom().iterable(rowset))
    - SELECT_ALL + wrapper=UNI: .transformToUni(rowset -> Multi.createFrom().iterable(rowset).collect().asList())
  7. Handle failures with error handler

  findAll() Implementation:
  1. Finds all tables with no-arg findAll methods
  2. Combines with Uni.combine().all().unis(...)
  3. Uses .with(sets -> {...}) to merge results
  4. Extracts typed lists: final List<BatchConsumer> item_0 = (List<BatchConsumer>) sets.get(0);
  5. Builds World with Maps keyed by getId():
  builder.batchConsumers(item_0
    .stream()
    .collect(Collectors.toMap(
      e -> e.getId(),
      e -> e
    )));

  Exception Handling:
  - For @Find(optional=false): Throws {Name}FindException when not found
  - Error handler invoked via errorHandler.deadEnd(new SqlFailed/SqlTupleFailed(...))

  6. Builder Interface ({Name}DbBuilder.java)

  Location: Base package

  Purpose: Defines builder API for persistence operations

  Structure:
  - {table}() - Returns nested builder interface for each table
  - Nested interfaces: {Table}Builder with insert/update/delete methods
  - PersistenceUnit interface: Immutable container with List<Entity> for changes
  - from(PersistenceUnit) - Load existing unit
  - persist() - Returns Uni<PersistenceUnit> with saved data

  7. Builder Implementation ({Name}DbBuilderImpl.java)

  Location: {package}.spi

  Purpose: Implements persistence operations with transaction support

  Key Features:
  - init field: ImmutablePersistenceUnit.Builder initialized at construction
  - from() method: Calls init.from(unit) to load existing data
  - persist() method:
    a. Builds entries: final var entries = init.build();
    b. Combines all operations with Uni.combine().all()
    c. Executes inserts/updates/deletes via registry
    d. Returns updated PersistenceUnit

  8. Main Db Interface ({Name}Db.java)

  Location: Base package

  Purpose: Main entry point combining query and builder

  Methods:
  - query() - Returns {Name}DbQuery
  - builder() - Returns {Name}DbBuilder

  9. Main Db Implementation ({Name}DbImpl.java)

  Location: {package}.spi

  Purpose: Implements main Db interface

  Simple delegation:
  public Batch2DbQuery query() {
    return new Batch2DbQueryImpl(dataSource);
  }

  public Batch2DbBuilder builder() {
    return new Batch2DbBuilderImpl(dataSource);
  }

  10. Internal Tenant Query ({Name}DbInternalTenantQuery.java)

  Location: {package}.spi

  Purpose: Schema management operations

  Methods:
  - createTables() - Executes DDL for all tables in order
  - createConstraints() - Adds foreign keys after all tables exist
  - dropTables() - Drops tables in reverse order

  11. Find Exception ({Name}FindException.java)

  Location: {package}.spi

  Purpose: Thrown when @Find(optional=false) query returns no result

  Structure:
  public class Batch2FindException extends RuntimeException {
    public Batch2FindException(String message) { super(message); }
    public Batch2FindException(String message, Throwable cause) { super(message, cause); }
  }

  Code Generation Flow

  Processing Steps

  1. Extract Registry Metadata (ModelExtractor)
    - Processes @TenantSql.Registry annotations
    - Builds RegistryModel with package, name, table class names
  2. Extract Table Metadata (ModelExtractor)
    - Processes @TenantSql.Table annotations
    - Extracts all methods with SQL annotations
    - Builds TableModel with SQL methods, parameters, return types
    - Extracts table names from {table_name} placeholders
    - Detects SQL builder usage via sqlBuilder property
  3. Generate Code (SqlAnnotationProcessor)
    - For each table: TableSqlCodeGenerator
    - For registry: All other generators in sequence

  Key Generator Classes

  TableSqlCodeGenerator:
  - Generates table implementation
  - Handles SQL builder logic for custom queries
  - Resolves table name placeholders
  - Creates methods for all CRUD operations

  TableNameSqlCodeGenerator:
  - Generates TableNames class in .spi
  - Creates getters for each table
  - Handles tenant prefix logic

  RegistryFactorySqlCodeGenerator:
  - Generates Registry factory in .spi
  - Creates getters with pluralized names
  - Instantiates table implementations

  DbQueryInterfaceSqlCodeGenerator:
  - Generates query interface in base package
  - Creates nested query interfaces per table
  - Generates World interface with Map getters
  - Determines return types based on wrapper and optional flags

  DbQueryImplSqlCodeGenerator:
  - Generates query implementation in .spi
  - Creates anonymous inner classes for table queries
  - Implements findAll() with Uni.combine() pattern
  - Handles Multi vs Uni wrappers
  - Implements optional vs required results
  - Generates exception throwing for non-optional queries

  DbBuilderInterfaceSqlCodeGenerator:
  - Generates builder interface in base package
  - Creates nested builder interfaces per table
  - Generates PersistenceUnit interface

  DbBuilderImplSqlCodeGenerator:
  - Generates builder implementation in .spi
  - Uses Immutables pattern with init field
  - Implements from() and persist() methods

  DbQueryExceptionSqlCodeGenerator:
  - Generates custom exception class
  - Simple RuntimeException extension

  Important Patterns

  Table Name Resolution

  Compile Time (in table implementations):
  // Template: "SELECT * FROM {batch_consumer} WHERE id = $1"
  // Becomes: "SELECT * FROM " + tables.getBatchConsumer() + " WHERE id = $1"

  Runtime with SQL Builder:
  var sqlValue = baseSql.getValue();
  sqlValue = sqlValue.replaceAll("(?i)\\{batch_consumer\\}", tables.getBatchConsumer());
  // Case-insensitive replacement of {batch_consumer} with tenant-prefixed name

  Mapper Extraction

  All query implementations extract row mapper with unchecked cast:
  @SuppressWarnings("unchecked")
  final RowMapper<BatchConsumer> mapper = (RowMapper<BatchConsumer>) sql.getRowMapper();

  Return Type Determination

  - @Find:
    - optional=true: Uni<Optional<Entity>>
    - optional=false: Uni<Entity> (throws exception if not found)
  - @FindAll:
    - wrapper=UNI: Uni<List<Entity>>
    - wrapper=MULTI: Multi<Entity>

  SQL Builder Usage

  When Used: @Find or @FindAll with sqlBuilder property AND exactly 1 parameter

  Process:
  1. Custom builder receives Tenant and parameter
  2. Returns SqlTuple with SQL and Tuple
  3. Processor performs table name replacement at runtime
  4. Row mapper added to final SqlTuple

  When NOT Used: Standard SQL template with compile-time table name resolution

  Model Classes

  TableModel

  - interfaceName, packageName, implClassName
  - tableName, order
  - ddlSql, constraintsSql, dropSql
  - List<SqlMethod> sqlMethods

  SqlMethod

  - SqlMethodType type - SELECT, INSERT, UPDATE, DELETE, etc.
  - String methodName, sqlTemplate, resolvedSql
  - String mapperClassName - Row or Props mapper
  - List<MethodParameter> parameters
  - TypeName returnType, wrapperType
  - SqlPropsType propsType - SQL, SQL_TUPLE, SQL_TUPLE_LIST
  - List<String> tableNames - Extracted from {table_name} placeholders
  - boolean optional - For @Find queries
  - boolean multiWrapper - For @FindAll (true = Multi, false = Uni)
  - String sqlBuilderClassName - Optional custom SQL builder

  RegistryModel

  - String name - e.g., "Batch2"
  - String packageName
  - String tableClassName, registryClassName
  - String worldName
  - List<String> nonTenantTables - Tables without tenant prefix

  File Locations

  thena-sql-client/
    src/main/java/io/resys/thena/api/annotations/
      TenantSql.java - All annotations and interfaces

  thena-sql-client-annot-proc/
    src/main/java/io/resys/thena/processor/
      SqlAnnotationProcessor.java - Main processor

      model/
        TableModel.java - Data models
        ModelExtractor.java - Annotation extraction

      codegen/
        TableSqlCodeGenerator.java - Table implementations
        TableNameSqlCodeGenerator.java - TableNames class
        RegistryFactorySqlCodeGenerator.java - Registry factory
        DbInterfaceSqlCodeGenerator.java - Main Db interface
        DbQueryInterfaceSqlCodeGenerator.java - Query interface
        DbQueryImplSqlCodeGenerator.java - Query implementation
        DbBuilderInterfaceSqlCodeGenerator.java - Builder interface
        DbBuilderImplSqlCodeGenerator.java - Builder implementation
        DbImplSqlCodeGenerator.java - Main Db implementation
        DbInternalTenantQuerySqlCodeGenerator.java - Schema management
        DbQueryExceptionSqlCodeGenerator.java - Exception class

  Usage Example

  // Define registry
  @TenantSql.Registry(
    name = "Batch2",
    worldName = "World",
    nonTenantTables = {"process", "process_id_seq"}
  )
  package io.resys.batch2.spi.sql;

  // Define table
  @TenantSql.Table(
    name = "batch_consumer",
    order = 1,
    ddl = "CREATE TABLE {batch_consumer} (id VARCHAR(40) PRIMARY KEY, ...)",
    constraints = "ALTER TABLE {batch_consumer} ADD CONSTRAINT ...",
    drop = "DROP TABLE {batch_consumer}"
  )
  public interface BatchConsumers {

    @TenantSql.Find(
      sql = "SELECT * FROM {batch_consumer} WHERE app_id = $1",
      rowMapper = BatchConsumerMapper.class,
      optional = false
    )
    Uni<BatchConsumer> findByAppId(String appId);

    @TenantSql.FindAll(
      sql = "SELECT * FROM {batch_consumer}",
      rowMapper = BatchConsumerMapper.class,
      wrapper = WrapperType.MULTI
    )
    Multi<BatchConsumer> findAll();

    @TenantSql.FindAll(
      sql = "", // SQL built by custom builder
      rowMapper = BatchConsumerMapper.class,
      sqlBuilder = CustomSearchBuilder.class
    )
    Uni<List<BatchConsumer>> search(SearchCriteria criteria);
  }

  // Custom SQL builder
  public class CustomSearchBuilder implements TenantSql.SqlBuilder<SearchCriteria> {
    @Override
    public SqlTuple apply(Tenant tenant, SearchCriteria criteria) {
      // Build dynamic SQL based on criteria
      return ImmutableSqlTuple.builder()
        .value("SELECT * FROM {batch_consumer} WHERE " + buildWhereClause(criteria))
        .props(buildParams(criteria))
        .build();
    }
  }

  Key Technical Decisions

  1. All implementations in .spi: Keeps public API clean
  2. Anonymous inner classes for queries: Avoids cluttering package
  3. Immutables for containers: Type-safe builders for World and PersistenceUnit
  4. Unchecked cast for mappers: Unavoidable due to type erasure, suppressed warnings
  5. Case-insensitive table replacement: Handles {TABLE}, {table}, {Table} uniformly
  6. Multi vs Uni via annotation: Explicit control over streaming vs collection
  7. Optional vs exception via annotation: Clear intent for required vs optional data
  8. SQL builder receives Tenant: Full tenant context for dynamic queries

  Development Workflow

  1. Modify annotations in thena-sql-client
  2. Compile annotations: cd thena-sql-client && mvn clean install -DskipTests
  3. Modify processors in thena-sql-client-annot-proc
  4. Compile processor: cd thena-sql-client-annot-proc && mvn clean compile
  5. Test with actual table definitions
  6. Verify generated code in target/generated-sources

  Communication Rule: DR. HOUSE Pattern

  - Rule 0: DO NOT LIE
  - Rule 1: Java 21
  - Rule 2: Annotation processor using JavaPoet
  - NO git/maven commands unless explicitly allowed
  - Direct, concise answers - no preamble or postamble
  - DO NOT ADD COMMENTS unless asked