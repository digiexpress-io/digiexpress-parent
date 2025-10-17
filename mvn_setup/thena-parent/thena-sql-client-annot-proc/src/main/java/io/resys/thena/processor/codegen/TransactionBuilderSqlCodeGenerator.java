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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.Modifier;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import io.resys.thena.api.entities.BatchLog;
import io.resys.thena.api.entities.BatchStatus;
import io.resys.thena.processor.model.TableModel;
import io.resys.thena.processor.model.TableModel.RegistryModel;
import io.resys.thena.processor.model.TableModel.SqlMethod;
import io.resys.thena.processor.model.TableModel.SqlMethodType;

public class TransactionBuilderSqlCodeGenerator {
  
  public JavaFile generate(RegistryModel registry, List<TableModel> tables) {
    final var className = registry.getTransactionContainerClassName();
    final var operations = extractOperations(tables);
    
    final var classBuilder = TypeSpec.classBuilder(className)
      .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
      .addAnnotation(ClassName.get("lombok", "Value"))
      .addAnnotation(com.squareup.javapoet.AnnotationSpec.builder(ClassName.get("lombok", "Builder"))
        .addMember("toBuilder", "$L", true)
        .build());
    
    // Add metadata fields first
    addMetadataFields(classBuilder);
    
    // Add operation fields
    for (final var entry : operations.entrySet()) {
      final var fieldName = entry.getKey();
      final var entityType = entry.getValue();
      
      classBuilder.addField(FieldSpec.builder(
        ParameterizedTypeName.get(ClassName.get(List.class), entityType),
        fieldName
      ).addAnnotation(ClassName.bestGuess("lombok.Builder.Default"))
       .initializer("new $T<>()", ArrayList.class)
       .build());
    }
    
    
    // Add merge method
    classBuilder.addMethod(generateMergeMethod(registry, className, operations));
    
    // Add helper method for merge
    classBuilder.addMethod(generateMergeListsHelper(operations));
    
    return JavaFile.builder(registry.getPackageName(), classBuilder.build())
      .indent("  ")
      .build();
  }
  
  
  private Map<String, TypeName> extractOperations(List<TableModel> tables) {
    final var operations = new HashMap<String, TypeName>();
    
    for (final var table : tables) {
      for (final var method : table.getSqlMethods()) {
        final var fieldName = buildOperationFieldName(table, method.getType());
        if (fieldName != null && !operations.containsKey(fieldName)) {
          final var entityType = extractEntityType(method);
          if (entityType != null) {
            operations.put(fieldName, entityType);
          }
        }
      }
    }
    
    return operations;
  }
  
  private String buildOperationFieldName(TableModel table, SqlMethodType type) {
    final var baseName = toCamelCase(table.getTableName());
    
    return switch (type) {
      case INSERT, INSERT_ALL -> baseName + "Inserts";
      case UPDATE, UPDATE_ALL -> baseName + "Updates";
      case DELETE, DELETE_ALL -> baseName + "Deletes";
      default -> null;
    };
  }
  
  private TypeName extractEntityType(SqlMethod method) {
    // Get entity type from first parameter
    if (method.getParameters().isEmpty()) {
      return null;
    }
    
    final var firstParam = method.getParameters().get(0);
    final var paramType = firstParam.getType();
    
    // If it's a Collection<T>, extract T
    if (paramType instanceof ParameterizedTypeName) {
      final var parameterized = (ParameterizedTypeName) paramType;
      if (!parameterized.typeArguments.isEmpty()) {
        return parameterized.typeArguments.get(0);
      }
    }
    
    // Otherwise return the type as-is
    return paramType;
  }
  
  private void addMetadataFields(TypeSpec.Builder classBuilder) {
    classBuilder.addField(FieldSpec.builder(String.class, "tenantId").build());
    classBuilder.addField(FieldSpec.builder(
      ClassName.get(BatchStatus.class),
      "status"
    ).build());
    classBuilder.addField(FieldSpec.builder(String.class, "log").build());
    classBuilder.addField(FieldSpec.builder(
      ParameterizedTypeName.get(
        ClassName.get(List.class),
        ClassName.get(BatchLog.class)
      ),
      "messages"
    ).addAnnotation(ClassName.bestGuess("lombok.Builder.Default"))
     .initializer("new $T<>()", ArrayList.class)
     .build());
  }
  
  
  
  
  private MethodSpec generateMergeMethod(RegistryModel registry, String className, Map<String, TypeName> operations) {
    final var method = MethodSpec.methodBuilder("merge")
      .addModifiers(Modifier.PUBLIC)
      .addParameter(ClassName.get(registry.getPackageName(), className), "other")
      .returns(ClassName.get(registry.getPackageName(), className));
    
    method.addStatement("final var mergedMessages = new $T<$T>()", 
      ArrayList.class,
      ClassName.get(BatchLog.class));
    method.addStatement("mergedMessages.addAll(this.messages)");
    method.addStatement("mergedMessages.addAll(other.messages)");
    
    method.addCode("\nreturn new $L(\n", className);
    method.addCode("  other.tenantId != null ? other.tenantId : this.tenantId,\n");
    method.addCode("  other.status != null ? other.status : this.status,\n");
    method.addCode("  other.log != null ? other.log : this.log,\n");
    method.addCode("  mergedMessages");
    
    for (final var fieldName : operations.keySet()) {
      method.addCode(",\n  mergeLists(this.$L, other.$L)", fieldName, fieldName);
    }
    
    method.addStatement("\n)");
    
    return method.build();
  }
  
  private MethodSpec generateMergeListsHelper(Map<String, TypeName> operations) {
    return MethodSpec.methodBuilder("mergeLists")
      .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
      .addTypeVariable(com.squareup.javapoet.TypeVariableName.get("T"))
      .addParameter(ParameterizedTypeName.get(
        ClassName.get(List.class),
        com.squareup.javapoet.TypeVariableName.get("T")
      ), "list1")
      .addParameter(ParameterizedTypeName.get(
        ClassName.get(List.class),
        com.squareup.javapoet.TypeVariableName.get("T")
      ), "list2")
      .returns(ParameterizedTypeName.get(
        ClassName.get(List.class),
        com.squareup.javapoet.TypeVariableName.get("T")
      ))
      .addStatement("final var merged = new $T<T>()", ArrayList.class)
      .addStatement("merged.addAll(list1)")
      .addStatement("merged.addAll(list2)")
      .addStatement("return merged")
      .build();
  }
  
  private String capitalize(String str) {
    if (str == null || str.isEmpty()) {
      return str;
    }
    return Character.toUpperCase(str.charAt(0)) + str.substring(1);
  }
  
  private String toCamelCase(String snakeCase) {
    final var parts = snakeCase.split("_");
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