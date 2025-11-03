package io.resys.thena.api.annotations;

/*-
 * #%L
 * thena-sql-client
 * %%
 * Copyright (C) 2015 - 2025 Copyright 2022 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.resys.thena.api.entities.Tenant;
import io.resys.thena.datasource.ThenaSqlClient.SqlTuple;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.Tuple;

/**
 * Annotations for generating tenant-aware SQL registry implementations.
 * Table names in SQL use {table_name} placeholders that get replaced with tenant-prefixed names at runtime.
 */
public @interface TenantSql {
  
  
  /**
   * Marks a package or interface as a registry holder for all @TenantSql.Table definitions.
   * Generates a TableNames class with getters for all tables and tenant prefix handling.
   */
  @Target({ElementType.PACKAGE, ElementType.TYPE})
  @Retention(RetentionPolicy.SOURCE)
  @interface Registry {
    /**
     * Name of the generated registry class.
     * Example: "GrimTableNames" generates GrimTableNames.java
     */
    String name();
    
    /**
     * Unique enum for given tenant, will be used as a classified in tenant table
     */
    Tenant.StructureType tenantType();
    
    /**
     * Optional: List of table names that should NOT be tenant-prefixed.
     * These tables remain constant across all tenants.
     * Example: {"process", "process_id_seq"}
     */
    String[] nonTenantTables() default {};
    
    /**
     * Optional: Package where the registry class should be generated.
     * If not specified, uses the package of the annotated element.
     */
    String packageName() default "";
    
    /**
     * Optional: Name of the World container interface.
     * If not specified, uses "World" as default.
     * Example: "GrimMissionContainer"
     */
    String worldName() default "World";
  }
  
  
  /**
   * Marks an interface as a tenant-aware SQL table registry.
   * Generates implementation with tenant prefix injection for all SQL operations.
   */
  @Target(ElementType.TYPE)
  @Retention(RetentionPolicy.SOURCE)
  @interface Table {
    /**
     * Clean table name without any tenant prefix or suffix.
     * Example: "grim_mission" (not "tenant_1_grim_mission")
     */
    String name();
    
    /**
     * Processing order for table creation/deletion.
     * Lower numbers processed first. Default is 0.
     * Use for dependency ordering (e.g., foreign key constraints).
     */
    int order() default 0;
    
    /**
     * DDL SQL for creating the table and indexes.
     * Use {table_name} placeholders for tenant injection.
     * Example: "CREATE TABLE {grim_mission} (id VARCHAR(40) PRIMARY KEY);"
     */
    String ddl() default "";
    
    /**
     * SQL for creating foreign key constraints.
     * Use {table_name} placeholders for tenant injection.
     * Example: "ALTER TABLE {grim_mission} ADD CONSTRAINT fk FOREIGN KEY (id) REFERENCES {other_table}(id);"
     */
    String constraints() default "";
    
    /**
     * SQL for dropping the table.
     * Use {table_name} placeholders for tenant injection.
     * Example: "DROP TABLE {grim_mission};"
     */
    String drop() default "";
  }
  
  /**
   * Marks a method as a SQL query that returns a single result.
   * Generates execution code with RowMapper for result transformation.
   */
  @Target(ElementType.METHOD)
  @Retention(RetentionPolicy.SOURCE)
  @interface Find {
    /**
     * SQL query string with {table_name} placeholders for tenant injection.
     * Use positional parameters ($1, $2, etc.) for method parameters.
     */
    String sql();
    
    /**
     * Mapper that transforms database Row to domain object.
     */
    Class<? extends RowMapper<?>> rowMapper();
    
    /**
     * Whether the result is optional (may return null/empty).
     * Default is true.
     */
    boolean optional() default true;
    
    /**
     * Optional SQL builder for custom SqlTuple construction.
     * Only valid when method has exactly one parameter.
     * Builder receives table names and the method parameter to construct SqlTuple.
     */
    Class<? extends SqlBuilder<?>> sqlBuilder() default DefaultSqlBuilder.class;
  }
  
  /**
   * Marks a method as a SQL query that returns multiple results.
   * Generates execution code with RowMapper for result transformation.
   */
  @Target(ElementType.METHOD)
  @Retention(RetentionPolicy.SOURCE)
  @interface FindAll {
    /**
     * SQL query string with {table_name} placeholders for tenant injection.
     * Use positional parameters ($1, $2, etc.) for method parameters.
     */
    String sql();
    
    /**
     * Mapper that transforms database Row to domain object.
     */
    Class<? extends RowMapper<?>> rowMapper();
    
    /**
     * Wrapper type for the result.
     * UNI wraps result as Uni<List<Entity>>
     * MULTI wraps result as Multi<Entity>
     */
    WrapperType wrapper() default WrapperType.UNI;
    
    /**
     * Optional SQL builder for custom SqlTuple construction.
     * Only valid when method has exactly one parameter.
     * Builder receives table names and the method parameter to construct SqlTuple.
     */
    Class<? extends SqlBuilder<?>> sqlBuilder() default DefaultSqlBuilder.class;
  }
  
  enum WrapperType {
    UNI,
    MULTI
  }
  
  /**
   * Marks a method as a single-row INSERT operation.
   * Returns SqlTuple for single object insertion.
   */
  @Target(ElementType.METHOD)
  @Retention(RetentionPolicy.SOURCE)
  @interface Insert {
    /**
     * INSERT SQL with {table_name} placeholders and positional parameters.
     */
    String sql();
    
    /**
     * Mapper that transforms domain object to Tuple for insertion.
     */
    Class<? extends PropsMapper<?>> propsMapper();
  }
  
  /**
   * Marks a method as a batch INSERT operation.
   * Returns SqlTupleList for inserting a collection of objects.
   */
  @Target(ElementType.METHOD)
  @Retention(RetentionPolicy.SOURCE)
  @interface InsertAll {
    /**
     * INSERT SQL with {table_name} placeholders and positional parameters.
     * Same SQL used for each object in the collection.
     */
    String sql();
    
    /**
     * Mapper that transforms each domain object to Tuple.
     * Applied to each element in the collection.
     */
    Class<? extends PropsMapper<?>> propsMapper();
  }
  
  /**
   * Marks a method as a single-row UPDATE operation.
   * Returns SqlTuple for single object update.
   */
  @Target(ElementType.METHOD)
  @Retention(RetentionPolicy.SOURCE)
  @interface Update {
    /**
     * UPDATE SQL with {table_name} placeholders and positional parameters.
     */
    String sql();
    
    /**
     * Mapper that transforms domain object to Tuple for update.
     */
    Class<? extends PropsMapper<?>> propsMapper();
  }
  
  /**
   * Marks a method as a batch UPDATE operation.
   * Returns SqlTupleList for updating a collection of objects.
   */
  @Target(ElementType.METHOD)
  @Retention(RetentionPolicy.SOURCE)
  @interface UpdateAll {
    /**
     * UPDATE SQL with {table_name} placeholders and positional parameters.
     * Same SQL used for each object in the collection.
     */
    String sql();
    
    /**
     * Mapper that transforms each domain object to Tuple.
     * Applied to each element in the collection.
     */
    Class<? extends PropsMapper<?>> propsMapper();
  }
  
  /**
   * Marks a method as a single-row DELETE operation.
   * Returns SqlTuple for single deletion.
   */
  @Target(ElementType.METHOD)
  @Retention(RetentionPolicy.SOURCE)
  @interface Delete {
    /**
     * DELETE SQL with {table_name} placeholders and positional parameters.
     */
    String sql();
    
    /**
     * Mapper that transforms deletion criteria to Tuple.
     */
    Class<? extends PropsMapper<?>> propsMapper();
  }
  
  /**
   * Marks a method as a batch DELETE operation.
   * Returns SqlTupleList for deleting multiple rows.
   */
  @Target(ElementType.METHOD)
  @Retention(RetentionPolicy.SOURCE)
  @interface DeleteAll {
    /**
     * DELETE SQL with {table_name} placeholders and positional parameters.
     * Same SQL used for each element in the collection.
     */
    String sql();
    
    /**
     * Mapper that transforms each deletion criteria to Tuple.
     * Applied to each element in the collection.
     */
    Class<? extends PropsMapper<?>> propsMapper();
  }
  
  /**
   * Maps database Row to domain object.
   * Used in @Query operations to transform query results.
   */
  @FunctionalInterface
  interface RowMapper<T> {
    T apply(Row row);
  }
  
  /**
   * Maps domain object to Tuple for SQL parameters.
   * Used in @Insert, @Update, @Delete operations to prepare statement parameters.
   */
  @FunctionalInterface
  interface PropsMapper<T> {
    Tuple apply(T object);
  }
  
  /**
   * Builds custom SqlTuple for query operations.
   * Used in @Find and @FindAll when custom SQL construction is needed.
   */
  @FunctionalInterface
  interface SqlBuilder<T> {
    SqlTuple apply(Tenant tenant, T parameter);
  }
  
  /**
   * Marker class for default SQL builder (no custom builder).
   */
  final class DefaultSqlBuilder implements SqlBuilder<Object> {
    @Override
    public SqlTuple apply(Tenant tenant, Object parameter) {
      throw new UnsupportedOperationException("Default SQL builder should not be called");
    }
  }

}
