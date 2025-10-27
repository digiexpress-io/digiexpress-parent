package io.resys.thena.processor.codegen;

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

/**
 * Generates the main registry factory that serves as the entry point for database operations.
 * 
 * <p>This generator creates the central registry class that provides factory methods for 
 * creating transactions, managing database connections, and orchestrating schema operations.
 * It's the primary dependency injection target and main API entry point for the domain.
 * 
 * <h3>Generated Code Example:</h3>
 * <pre>{@code
 * @Component
 * @Singleton
 * public class ContractRegistry {
 *   
 *   private final io.vertx.mutiny.sqlclient.Pool sqlClient;
 *   private final String worldName;
 *   
 *   public ContractRegistry(io.vertx.mutiny.sqlclient.Pool sqlClient) {
 *     this.sqlClient = sqlClient;
 *     this.worldName = "ContractWorld";
 *   }
 *   
 *   // Transaction factory methods
 *   public ContractSaveTransaction createTransaction() {
 *     return new ContractSaveTransactionImpl(sqlClient);
 *   }
 *   
 *   public ContractSaveTransaction createTransaction(String tenantId) {
 *     return new ContractSaveTransactionImpl(sqlClient).withTenantId(tenantId);
 *   }
 *   
 *   // Schema management
 *   public Uni<Void> createTables() {
 *     List<String> ddlStatements = List.of(
 *       // DDL for all tables in dependency order
 *       ContractTable.CREATE_TABLE,
 *       PartyTable.CREATE_TABLE,
 *       CoverageTable.CREATE_TABLE
 *     );
 *     
 *     return executeStatements(ddlStatements);
 *   }
 *   
 *   public Uni<Void> dropTables() {
 *     List<String> dropStatements = List.of(
 *       // DROP statements in reverse dependency order
 *       CoverageTable.DROP_TABLE,
 *       PartyTable.DROP_TABLE,
 *       ContractTable.DROP_TABLE
 *     );
 *     
 *     return executeStatements(dropStatements);
 *   }
 *   
 *   // Utility methods
 *   public String getWorldName() {
 *     return worldName;
 *   }
 *   
 *   public ContractTableNames getTableNames() {
 *     return new ContractTableNames();
 *   }
 * }
 * }</pre>
 * 
 * <h3>Key Features:</h3>
 * <ul>
 * <li>Central factory for creating database transactions</li>
 * <li>Manages SQL connection pool and database resources</li>
 * <li>Provides schema management operations (create/drop tables)</li>
 * <li>Supports multi-tenancy with tenant-scoped transactions</li>
 * <li>Dependency injection ready with proper annotations</li>
 * <li>Handles table creation order based on foreign key dependencies</li>
 * <li>Provides access to table name constants and metadata</li>
 * </ul>
 * 
 * <h3>Usage Pattern:</h3>
 * <pre>{@code
 * @Inject ContractRegistry registry;
 * 
 * // Create transaction for specific tenant
 * ContractSaveTransaction tx = registry.createTransaction("tenant123");
 * 
 * // Perform database operations
 * return tx.queryBuilder()
 *   .contracts()
 *   .findById("contract456")
 *   .flatMap(contract -> {
 *     // Business logic
 *     return tx.commit();
 *   });
 * 
 * // Schema management
 * Uni<Void> schemaSetup = registry.createTables();
 * Uni<Void> schemaTeardown = registry.dropTables();
 * }</pre>
 */

import java.util.List;

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import io.resys.thena.datasource.ThenaSqlDataSource;
import io.resys.thena.processor.model.MultiTableCodeGenerator;
import io.resys.thena.processor.model.TableModel;
import io.resys.thena.processor.model.TableModel.RegistryModel;
import io.resys.thena.processor.support.NamingUtils;

public class Gen_Multi_RegistryFactory implements MultiTableCodeGenerator {
  
  public JavaFile generate(RegistryModel registry, List<TableModel> tables) {
    final var className = registry.getRegistryClassName();
    
    final var classBuilder = TypeSpec.classBuilder(className)
      .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
      .addAnnotation(ClassName.get("lombok", "Value"));
    
    // Add fields
    classBuilder.addField(FieldSpec.builder(
      ClassName.bestGuess(registry.getTableClassName()),
      "tables"
    ).build());
    
    classBuilder.addField(FieldSpec.builder(
      ClassName.get(ThenaSqlDataSource.class),
      "dataSource"
    ).build());
    
    // Add factory method for each table
    for (final var table : tables) {
      classBuilder.addMethod(generateFactoryMethod(table));
    }
    
    return JavaFile.builder(registry.getPackageName() + ".spi", classBuilder.build())
      .indent("  ")
      .build();
  }
  
  private MethodSpec generateFactoryMethod(TableModel table) {
    final var interfaceName = table.getInterfaceName();
    final var interfaceClassName = ClassName.get(table.getPackageName(), table.getInterfaceName());
    final var methodName = NamingUtils.pluralize(table.getTableName());
    final var implName = table.getImplClassName();
    
    return MethodSpec.methodBuilder(methodName)
      .addModifiers(Modifier.PUBLIC)
      .returns(ClassName.bestGuess(implName))
      .addStatement("return new $L(tables, dataSource)", implName)
      .build();
  }
}