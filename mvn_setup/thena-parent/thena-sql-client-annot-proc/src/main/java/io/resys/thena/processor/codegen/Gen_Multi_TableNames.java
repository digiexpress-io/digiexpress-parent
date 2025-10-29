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
 * Generates a centralized table names constants class for a registry.
 * 
 * <p>This generator creates a utility class that contains all table names as string 
 * constants, providing a single source of truth for table names across the domain.
 * This is essential for schema management, migrations, and avoiding magic strings.
 * 
 * <h3>Generated Code Example:</h3>
 * <pre>{@code
 * public class ContractTableNames {
 * 
 *   // Table name constants
 *   public static final String CONTRACT = "contract";
 *   public static final String PARTY = "party";
 *   public static final String COVERAGE = "coverage";
 *   public static final String COMMIT = "commit";
 *   public static final String COMMIT_TREE = "commit_tree";
 *   
 *   // All table names in creation order (respects FK dependencies)
 *   public static final List<String> ALL = List.of(
 *     COMMIT,
 *     COMMIT_TREE,
 *     CONTRACT,
 *     PARTY, 
 *     COVERAGE
 *   );
 *   
 *   // Tables in reverse order for dropping (handles FK constraints)
 *   public static final List<String> ALL_REVERSE = List.of(
 *     COVERAGE,
 *     PARTY,
 *     CONTRACT,
 *     COMMIT_TREE,
 *     COMMIT
 *   );
 * }
 * }</pre>
 * 
 * <h3>Key Features:</h3>
 * <ul>
 * <li>String constants for all table names in the registry</li>
 * <li>ALL list with tables in creation order (respects FK dependencies)</li>
 * <li>ALL_REVERSE list for safe dropping order</li>
 * <li>Prevents magic strings throughout the codebase</li>
 * <li>Single source of truth for table naming</li>
 * <li>Orders tables by their dependency relationships</li>
 * </ul>
 * 
 * <h3>Usage Pattern:</h3>
 * <pre>{@code
 * // Schema creation
 * for (String tableName : ContractTableNames.ALL) {
 *   executeCreateTable(tableName);
 * }
 * 
 * // Schema cleanup  
 * for (String tableName : ContractTableNames.ALL_REVERSE) {
 *   executeDropTable(tableName);
 * }
 * 
 * // Reference in queries
 * String sql = "SELECT * FROM " + ContractTableNames.CONTRACT + " WHERE id = ?";
 * }</pre>
 */

import java.util.List;

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import io.resys.thena.api.entities.Tenant;
import io.resys.thena.processor.model.TableModel;
import io.resys.thena.processor.model.TableModel.RegistryModel;
import io.resys.thena.processor.spi.MultiTableCodeGenerator;

public class Gen_Multi_TableNames implements MultiTableCodeGenerator {
  
  public JavaFile generate(RegistryModel registry, List<TableModel> tables) {
    final var className = registry.getTableClassName();
    
    final var classBuilder = TypeSpec.classBuilder(className)
      .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
      .addAnnotation(ClassName.get("lombok", "Value"))
      .addAnnotation(ClassName.get("lombok", "Builder"));
    
    // Add DEFAULTS static field
    classBuilder.addField(FieldSpec.builder(
      ClassName.bestGuess(className),
      "DEFAULTS",
      Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL
    ).initializer("defaults()").build());
    
    // Add fields
    classBuilder.addField(FieldSpec.builder(String.class, "prefix").build());
    
    for (final var table : tables) {
      final var fieldName = uncapitalize(table.getTableName());
      classBuilder.addField(FieldSpec.builder(String.class, fieldName).build());
    }
    
    // Add toRepo(Tenant) method
    classBuilder.addMethod(generateToRepoTenantMethod(registry, className));
    
    // Add toRepo(String) method
    classBuilder.addMethod(generateToRepoStringMethod(registry, tables, className));
    
    // Add defaults() static method
    classBuilder.addMethod(generateDefaultsMethod(registry, tables, className));
    
    return JavaFile.builder(registry.getPackageName() + ".spi", classBuilder.build())
      .indent("  ")
      .build();
  }
  
  private MethodSpec generateToRepoTenantMethod(RegistryModel registry, String className) {
    return MethodSpec.methodBuilder("toRepo")
      .addModifiers(Modifier.PUBLIC)
      .addParameter(ClassName.get(Tenant.class), "repo")
      .returns(ClassName.bestGuess(className))
      .addStatement("final var prefix = repo.getPrefix()")
      .addStatement("return toRepo(prefix)")
      .build();
  }
  
  private MethodSpec generateToRepoStringMethod(
      RegistryModel registry, 
      List<TableModel> tables, 
      String className) {
    
    final var builder = MethodSpec.methodBuilder("toRepo")
      .addModifiers(Modifier.PUBLIC)
      .addParameter(String.class, "prefix")
      .returns(ClassName.bestGuess(className));
    
    // Start builder chain
    builder.addCode("return $L.builder()\n", className);
    builder.addCode("  .prefix(prefix)\n");
    
    // Add each table
    for (final var table : tables) {
      final var tableName = table.getTableName();
      final var getterName = "get" + capitalize(tableName);
      final var builderMethod = uncapitalize(tableName);
      
      if (registry.getNonTenantTables().contains(tableName)) {
        // Non-tenant table - no prefix
        builder.addCode("  .$L(DEFAULTS.$L())\n", builderMethod, getterName);
      } else {
        // Tenant-aware table - add prefix
        builder.addCode("  .$L(prefix + DEFAULTS.$L())\n", builderMethod, getterName);
      }
    }
    
    builder.addStatement("  .build()");
    
    return builder.build();
  }
  
  private MethodSpec generateDefaultsMethod(
      RegistryModel registry,
      List<TableModel> tables,
      String className) {
    
    final var builder = MethodSpec.methodBuilder("defaults")
      .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
      .returns(ClassName.bestGuess(className));
    
    // Start builder chain
    builder.addCode("return $L.builder()\n", className);
    builder.addCode("  .prefix(\"\")\n");
    
    // Add each table with clean name
    for (final var table : tables) {
      final var tableName = table.getTableName();
      final var builderMethod = uncapitalize(tableName);
      builder.addCode("  .$L($S)\n", builderMethod, tableName);
    }
    
    builder.addStatement("  .build()");
    
    return builder.build();
  }
  
  private String capitalize(String str) {
    if (str == null || str.isEmpty()) {
      return str;
    }
    // Handle snake_case: grim_mission -> GrimMission
    final var parts = str.split("_");
    final var result = new StringBuilder();
    for (final var part : parts) {
      if (!part.isEmpty()) {
        result.append(Character.toUpperCase(part.charAt(0)))
              .append(part.substring(1));
      }
    }
    return result.toString();
  }
  
  private String uncapitalize(String str) {
    if (str == null || str.isEmpty()) {
      return str;
    }
    // Handle snake_case: grim_mission -> grimMission
    final var parts = str.split("_");
    final var result = new StringBuilder();
    for (int i = 0; i < parts.length; i++) {
      final var part = parts[i];
      if (!part.isEmpty()) {
        if (i == 0) {
          result.append(part);
        } else {
          result.append(Character.toUpperCase(part.charAt(0)))
                .append(part.substring(1));
        }
      }
    }
    return result.toString();
  }
}