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

public class TableNameSqlCodeGenerator {
  
  public JavaFile generate(RegistryModel registry, List<TableModel> tables) {
    final var className = registry.getTableClassName();
    
    final var classBuilder = TypeSpec.classBuilder(className)
      .addModifiers(Modifier.PUBLIC, Modifier.FINAL);
    
    // Add DEFAULTS static field
    classBuilder.addField(FieldSpec.builder(
      ClassName.get(registry.getPackageName(), className),
      "DEFAULTS",
      Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL
    ).initializer("defaults()").build());
    
    // Add private final fields
    classBuilder.addField(FieldSpec.builder(String.class, "prefix", Modifier.PRIVATE, Modifier.FINAL).build());
    
    for (final var table : tables) {
      final var fieldName = uncapitalize(table.getTableName());
      classBuilder.addField(FieldSpec.builder(String.class, fieldName, Modifier.PRIVATE, Modifier.FINAL).build());
    }
    
    // Add private constructor
    classBuilder.addMethod(generateConstructor(registry, tables));
    
    // Add prefix getter
    classBuilder.addMethod(MethodSpec.methodBuilder("getPrefix")
      .addModifiers(Modifier.PUBLIC)
      .returns(String.class)
      .addStatement("return prefix")
      .build());
    
    // Add getter for each table
    for (final var table : tables) {
      final var fieldName = uncapitalize(table.getTableName());
      final var getterName = "get" + capitalize(table.getTableName());
      classBuilder.addMethod(MethodSpec.methodBuilder(getterName)
        .addModifiers(Modifier.PUBLIC)
        .returns(String.class)
        .addStatement("return $L", fieldName)
        .build());
    }
    
    // Add builder() static method
    classBuilder.addMethod(MethodSpec.methodBuilder("builder")
      .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
      .returns(ClassName.get(registry.getPackageName(), className, "Builder"))
      .addStatement("return new Builder()")
      .build());
    
    // Add nested Builder class
    classBuilder.addType(generateBuilderClass(registry, tables, className));
    
    // Add toRepo(Tenant) method
    classBuilder.addMethod(generateToRepoTenantMethod(registry, className));
    
    // Add toRepo(String) method
    classBuilder.addMethod(generateToRepoStringMethod(registry, tables, className));
    
    // Add defaults() static method
    classBuilder.addMethod(generateDefaultsMethod(registry, tables, className));
    
    return JavaFile.builder(registry.getPackageName(), classBuilder.build())
      .indent("  ")
      .build();
  }
  
  private MethodSpec generateConstructor(RegistryModel registry, List<TableModel> tables) {
    final var constructor = MethodSpec.constructorBuilder()
      .addModifiers(Modifier.PRIVATE)
      .addParameter(String.class, "prefix");
    
    constructor.addStatement("this.prefix = prefix");
    
    for (final var table : tables) {
      final var fieldName = uncapitalize(table.getTableName());
      constructor.addParameter(String.class, fieldName);
      constructor.addStatement("this.$L = $L", fieldName, fieldName);
    }
    
    return constructor.build();
  }
  
  private TypeSpec generateBuilderClass(RegistryModel registry, List<TableModel> tables, String parentClassName) {
    final var builderClass = TypeSpec.classBuilder("Builder")
      .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL);
    
    // Add fields
    builderClass.addField(FieldSpec.builder(String.class, "prefix", Modifier.PRIVATE).build());
    
    for (final var table : tables) {
      final var fieldName = uncapitalize(table.getTableName());
      builderClass.addField(FieldSpec.builder(String.class, fieldName, Modifier.PRIVATE).build());
    }
    
    // Add prefix setter
    builderClass.addMethod(MethodSpec.methodBuilder("prefix")
      .addModifiers(Modifier.PUBLIC)
      .addParameter(String.class, "prefix")
      .returns(ClassName.get(registry.getPackageName(), parentClassName, "Builder"))
      .addStatement("this.prefix = prefix")
      .addStatement("return this")
      .build());
    
    // Add setter for each table
    for (final var table : tables) {
      final var fieldName = uncapitalize(table.getTableName());
      builderClass.addMethod(MethodSpec.methodBuilder(fieldName)
        .addModifiers(Modifier.PUBLIC)
        .addParameter(String.class, fieldName)
        .returns(ClassName.get(registry.getPackageName(), parentClassName, "Builder"))
        .addStatement("this.$L = $L", fieldName, fieldName)
        .addStatement("return this")
        .build());
    }
    
    // Add build() method
    final var buildMethod = MethodSpec.methodBuilder("build")
      .addModifiers(Modifier.PUBLIC)
      .returns(ClassName.get(registry.getPackageName(), parentClassName));
    
    // Build constructor call
    buildMethod.addCode("return new $L(\n", parentClassName);
    buildMethod.addCode("  prefix");
    for (final var table : tables) {
      final var fieldName = uncapitalize(table.getTableName());
      buildMethod.addCode(",\n  $L", fieldName);
    }
    buildMethod.addStatement("\n)");
    
    builderClass.addMethod(buildMethod.build());
    
    return builderClass.build();
  }
  
  private MethodSpec generateToRepoTenantMethod(RegistryModel registry, String className) {
    return MethodSpec.methodBuilder("toRepo")
      .addModifiers(Modifier.PUBLIC)
      .addParameter(ClassName.get(Tenant.class), "repo")
      .returns(ClassName.get(registry.getPackageName(), className))
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
      .returns(ClassName.get(registry.getPackageName(), className));
    
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
      .returns(ClassName.get(registry.getPackageName(), className));
    
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