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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import io.resys.thena.api.entities.Tenant;
import io.resys.thena.processor.model.Metamodel;
import io.resys.thena.processor.model.RegistryMetamodel;
import io.resys.thena.processor.model.TableMetamodel;
import io.resys.thena.processor.spi.MultiTableCodeGenerator;

public class Gen_Multi_TableNames implements MultiTableCodeGenerator {
  
  
  public JavaFile generate(RegistryMetamodel registry, List<TableMetamodel> registryTables, Metamodel metamodel) {
    
    
    final var tables = Stream.concat(registryTables.stream(), getExtendedTablesFromQueries(registryTables, metamodel).stream()).toList();
    
    final var className = registry.getTableClassName();
    
    final var classBuilder = TypeSpec.classBuilder(className)
      .addModifiers(Modifier.PUBLIC, Modifier.FINAL);
    
    // Add DEFAULTS static field
    classBuilder.addField(FieldSpec.builder(
      ClassName.bestGuess(className),
      "DEFAULTS",
      Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL
    ).initializer("defaults()").build());
    
    // Add closed flag field
    classBuilder.addField(FieldSpec.builder(boolean.class, "closed")
      .addModifiers(Modifier.PRIVATE)
      .initializer("false")
      .build());
    
    // Add fields
    classBuilder.addField(FieldSpec.builder(String.class, "prefix")
      .addModifiers(Modifier.PRIVATE)
      .build());
    
    for (final var table : tables) {
      final var fieldName = uncapitalize(table.getTableName());
      classBuilder.addField(FieldSpec.builder(String.class, fieldName)
        .addModifiers(Modifier.PRIVATE)
        .build());
    }
    
    // Generate no-arg constructor
    classBuilder.addMethod(MethodSpec.constructorBuilder()
      .addModifiers(Modifier.PUBLIC)
      .build());
    
    // Generate isOpen() guard method
    classBuilder.addMethod(generateIsOpenMethod());
    
    // Generate close() method
    classBuilder.addMethod(generateCloseMethod(className));
    
    // Generate with methods for chaining
    classBuilder.addMethod(generateWithMethod("prefix", String.class, className));
    for (final var table : tables) {
      final var fieldName = uncapitalize(table.getTableName());
      classBuilder.addMethod(generateWithMethod(fieldName, String.class, className));
    }
    
    // Generate getters
    classBuilder.addMethod(generateGetter("prefix", String.class));
    for (final var table : tables) {
      final var fieldName = uncapitalize(table.getTableName());
      final var getterName = "get" + capitalize(table.getTableName());
      classBuilder.addMethod(generateGetterWithName(fieldName, getterName, String.class));
    }
    
    // Add manual Builder inner class
    classBuilder.addType(generateBuilderClass(className, tables));
    
    // Add static builder() method
    classBuilder.addMethod(MethodSpec.methodBuilder("builder")
      .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
      .returns(ClassName.bestGuess("Builder"))
      .addStatement("return new Builder()")
      .build());
    
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
  
  private MethodSpec generateToRepoTenantMethod(RegistryMetamodel registry, String className) {
    return MethodSpec.methodBuilder("toRepo")
      .addModifiers(Modifier.PUBLIC)
      .addParameter(ClassName.get(Tenant.class), "repo")
      .returns(ClassName.bestGuess(className))
      .addStatement("final var prefix = repo.getPrefix()")
      .addStatement("return toRepo(prefix)")
      .build();
  }
  
  private MethodSpec generateIsOpenMethod() {
    return MethodSpec.methodBuilder("isOpen")
      .addModifiers(Modifier.PRIVATE)
      .returns(void.class)
      .beginControlFlow("if (closed)")
      .addStatement("throw new $T($S)", IllegalArgumentException.class, "TableNames instance is closed and cannot be modified")
      .endControlFlow()
      .build();
  }
  
  private MethodSpec generateCloseMethod(String className) {
    return MethodSpec.methodBuilder("close")
      .addModifiers(Modifier.PUBLIC)
      .returns(ClassName.bestGuess(className))
      .addStatement("this.closed = true")
      .addStatement("return this")
      .build();
  }
  
  private MethodSpec generateWithMethod(String fieldName, Class<?> type, String className) {
    final var methodName = "with" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
    return MethodSpec.methodBuilder(methodName)
      .addModifiers(Modifier.PUBLIC)
      .addParameter(type, fieldName)
      .returns(ClassName.bestGuess(className))
      .addStatement("isOpen()")
      .addStatement("this.$L = $L", fieldName, fieldName)
      .addStatement("return this")
      .build();
  }
  
  private MethodSpec generateToRepoStringMethod(
      RegistryMetamodel registry, 
      List<TableMetamodel> tables, 
      String className) {
    
    final var builder = MethodSpec.methodBuilder("toRepo")
      .addModifiers(Modifier.PUBLIC)
      .addParameter(String.class, "prefix")
      .returns(ClassName.bestGuess(className));
    
    // Create new instance and chain with methods
    builder.addCode("final var result = new $L()\n", className);
    builder.addCode("  .withPrefix(prefix)\n");
    
    // Add each table
    for (final var table : tables) {
      final var tableName = table.getTableName();
      final var getterName = "get" + capitalize(tableName);
      final var withMethod = "with" + capitalize(uncapitalize(tableName));
      
      if (registry.getNonTenantTables().contains(tableName)) {
        // Non-tenant table - no prefix
        builder.addCode("  .$L(DEFAULTS.$L())\n", withMethod, getterName);
      } else {
        // Tenant-aware table - add prefix
        builder.addCode("  .$L(prefix + DEFAULTS.$L())\n", withMethod, getterName);
      }
    }
    
    builder.addStatement("  .close()");
    builder.addStatement("return result");
    
    return builder.build();
  }
  
  private MethodSpec generateDefaultsMethod(
      RegistryMetamodel registry,
      List<TableMetamodel> tables,
      String className) {
    
    final var builder = MethodSpec.methodBuilder("defaults")
      .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
      .returns(ClassName.bestGuess(className));
    
    // Create new instance and chain with methods
    builder.addCode("final var result = new $L()\n", className);
    builder.addCode("  .withPrefix(\"\")\n");
    
    // Add each table with clean name
    for (final var table : tables) {
      final var tableName = table.getTableName();
      final var withMethod = "with" + capitalize(uncapitalize(tableName));
      builder.addCode("  .$L($S)\n", withMethod, tableName);
    }
    
    builder.addStatement("  .close()");
    builder.addStatement("return result");
    
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
  
  
  private MethodSpec generateGetter(String fieldName, Class<?> type) {
    final var getterName = "get" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
    return generateGetterWithName(fieldName, getterName, type);
  }
  
  private MethodSpec generateGetterWithName(String fieldName, String getterName, Class<?> type) {
    return MethodSpec.methodBuilder(getterName)
      .addModifiers(Modifier.PUBLIC)
      .returns(type)
      .addStatement("return $L", fieldName)
      .build();
  }
  
  private TypeSpec generateBuilderClass(String className, List<TableMetamodel> tables) {
    final var builderClass = TypeSpec.classBuilder("Builder")
      .addModifiers(Modifier.PUBLIC, Modifier.STATIC);
    
    // Add fields
    builderClass.addField(FieldSpec.builder(String.class, "prefix")
      .addModifiers(Modifier.PRIVATE)
      .build());
    
    for (final var table : tables) {
      final var fieldName = uncapitalize(table.getTableName());
      builderClass.addField(FieldSpec.builder(String.class, fieldName)
        .addModifiers(Modifier.PRIVATE)
        .build());
    }
    
    // Add setter methods
    builderClass.addMethod(generateBuilderSetter("prefix"));
    
    for (final var table : tables) {
      final var fieldName = uncapitalize(table.getTableName());
      builderClass.addMethod(generateBuilderSetter(fieldName));
    }
    
    // Add build() method
    builderClass.addMethod(generateBuildMethod(className, tables));
    
    return builderClass.build();
  }
  
  private MethodSpec generateBuilderSetter(String fieldName) {
    return MethodSpec.methodBuilder(fieldName)
      .addModifiers(Modifier.PUBLIC)
      .addParameter(String.class, fieldName)
      .returns(ClassName.bestGuess("Builder"))
      .addStatement("this.$L = $L", fieldName, fieldName)
      .addStatement("return this")
      .build();
  }
  
  private MethodSpec generateBuildMethod(String className, List<TableMetamodel> tables) {
    final var builder = MethodSpec.methodBuilder("build")
      .addModifiers(Modifier.PUBLIC)
      .returns(ClassName.bestGuess(className));
    
    // Create new instance and chain with methods
    builder.addCode("final var result = new $L()\n", className);
    builder.addCode("  .withPrefix(prefix)\n");
    
    // Add each table field
    for (final var table : tables) {
      final var fieldName = uncapitalize(table.getTableName());
      final var withMethod = "with" + capitalize(fieldName);
      builder.addCode("  .$L($L)\n", withMethod, fieldName);
    }
    
    builder.addStatement("  .close()");
    builder.addStatement("return result");
    
    return builder.build();
  }
  

  public List<TableMetamodel> getExtendedTablesFromQueries(List<TableMetamodel> registryTables, Metamodel metamodel) {
    
    final var allTables = metamodel.getTables().stream().collect(Collectors.toMap(e -> e.getTableName(), e -> e));
    
    final var registryNames = registryTables.stream().map(e -> e.getTableName()).toList();
    
    final var externalTables = registryTables.stream()
      .flatMap(e -> e.getSqlMethods().stream())
      .flatMap(e -> e.getTableNames().stream())
      .filter(e -> !registryNames.contains(e))
      .collect(Collectors.toSet())
      .stream()
      .map(e -> allTables.get(e))
      .filter(e -> e != null)
      .sorted((a, b) -> a.getTableName().compareTo(b.getTableName())).toList();
    
    return externalTables;
  }
}