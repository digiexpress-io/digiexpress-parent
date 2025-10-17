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

import io.resys.thena.api.entities.BatchStatus;
import io.resys.thena.api.entities.ImmutableBatchLog;
import io.resys.thena.datasource.ThenaSqlClient.SqlTupleList;
import io.resys.thena.datasource.ThenaSqlDataSource;
import io.resys.thena.processor.model.TableModel;
import io.resys.thena.processor.model.TableModel.RegistryModel;
import io.resys.thena.processor.model.TableModel.SqlMethod;
import io.resys.thena.processor.model.TableModel.SqlMethodType;

public class TransactionSaveSqlCodeGenerator {
  
  public JavaFile generate(RegistryModel registry, List<TableModel> tables) {
    final var className = registry.getTransactionSaveClassName();
    final var containerClassName = registry.getTransactionContainerClassName();
    final var registryClassName = registry.getRegistryClassName();
    final var operations = extractOperations(tables);
    
    final var classBuilder = TypeSpec.classBuilder(className)
      .addModifiers(Modifier.PUBLIC, Modifier.FINAL);
    
    // Add logger
    classBuilder.addField(generateLoggerField(registry));
    
    // Add fields
    classBuilder.addField(FieldSpec.builder(
      ClassName.get("io.resys.thena.datasource", "ThenaSqlClient"),
      "tx",
      Modifier.PRIVATE, Modifier.FINAL
    ).build());
    
    classBuilder.addField(FieldSpec.builder(
      ClassName.get("io.resys.thena.datasource", "ThenaSqlDataSource"),
      "dataSource",
      Modifier.PRIVATE, Modifier.FINAL
    ).build());
    
    classBuilder.addField(FieldSpec.builder(
      ClassName.get(registry.getPackageName(), registryClassName),
      "registry",
      Modifier.PRIVATE, Modifier.FINAL
    ).build());
    
    classBuilder.addField(FieldSpec.builder(
      StringBuilder.class,
      "txLog",
      Modifier.PRIVATE, Modifier.FINAL
    ).initializer("new $T()", StringBuilder.class).build());
    
    // Add constructor
    classBuilder.addMethod(generateConstructor(registry, className, registryClassName));
    
    // Add persist method
    classBuilder.addMethod(generatePersistMethod(registry, containerClassName, operations, tables));
    
    // Add visit methods for each operation
    for (final var entry : operations.entrySet()) {
      final var operation = entry.getKey();
      final var table = entry.getValue().table;
      final var entityType = entry.getValue().entityType;
      
      classBuilder.addMethod(generateVisitMethod(
        registry, 
        containerClassName, 
        operation, 
        table, 
        entityType
      ));
    }
    
    // Add helper methods
    classBuilder.addMethod(generateVisitExecutionMethod(registry, containerClassName));
    classBuilder.addMethod(generateVisitTxLogMethod());
    classBuilder.addMethod(generateVisitSuccessMethod(registry, containerClassName));
    classBuilder.addMethod(generateVisitErrorMethod(registry, containerClassName));
    
    // Add exception class
    classBuilder.addType(generateExceptionClass(registry, containerClassName));
    
    return JavaFile.builder(registry.getPackageName(), classBuilder.build())
      .indent("  ")
      .build();
  }
  
  private FieldSpec generateLoggerField(RegistryModel registry) {
    final var loggerTopic = registry.getPackageName() + "." + 
                            registry.getName().toLowerCase() + ".show_sql";
    
    return FieldSpec.builder(
      ClassName.get("org.slf4j", "Logger"),
      "log",
      Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL
    )
    .initializer("$T.getLogger($S)", 
      ClassName.get("org.slf4j", "LoggerFactory"),
      loggerTopic
    )
    .build();
  }
  
  private MethodSpec generateConstructor(RegistryModel registry, String className, String registryClassName) {
    return MethodSpec.constructorBuilder()
      .addModifiers(Modifier.PUBLIC)
      .addParameter(ClassName.get(ThenaSqlDataSource.class), "dataSource")
      .addStatement("this.dataSource = dataSource")
      .addStatement("this.tx = dataSource.getClient()")
      .addStatement("this.registry = new $L(dataSource.getRegistry(), dataSource)", 
        registryClassName)
      .build();
  }
  
  private MethodSpec generatePersistMethod(
      RegistryModel registry, 
      String containerClassName,
      Map<String, OperationInfo> operations,
      List<TableModel> tables) {
    
    final var method = MethodSpec.methodBuilder("persist")
      .addModifiers(Modifier.PUBLIC)
      .addParameter(ClassName.get(registry.getPackageName(), containerClassName), "entries")
      .returns(ParameterizedTypeName.get(
        ClassName.get("io.smallrye.mutiny", "Uni"),
        ClassName.get(registry.getPackageName(), containerClassName)
      ));
    
    // Build Uni.combine().all().unis(...)
    method.addCode("return $T.combine().all()\n", ClassName.get("io.smallrye.mutiny", "Uni"));
    method.addCode("  .unis(\n");
    
    final var visitCalls = new ArrayList<String>();
    for (final var operationName : operations.keySet()) {
      visitCalls.add("visit" + capitalize(operationName) + "(entries)");
    }
    
    method.addCode("    " + String.join(",\n    ", visitCalls));
    method.addCode("\n  )\n");
    
    // Add with() combinator
    method.addCode("  .with($T.class, (items) -> visitSuccess(entries, items))\n", 
      ClassName.get(registry.getPackageName(), containerClassName));
    
    // Add error handler
    method.addCode("  .onFailure($LException.class)\n", 
      registry.getName() + "Transaction");
    method.addStatement("  .recoverWithUni(this::visitError)");
    
    return method.build();
  }
  
  private MethodSpec generateVisitMethod(
      RegistryModel registry,
      String containerClassName,
      String operationName,
      TableModel table,
      TypeName entityType) {
    
    final var methodName = "visit" + capitalize(operationName);
    final var getterName = "get" + capitalize(operationName);
    final var registryMethod = determineRegistryMethod(table, operationName);
    
    final var method = MethodSpec.methodBuilder(methodName)
      .addModifiers(Modifier.PRIVATE)
      .addParameter(ClassName.get(registry.getPackageName(), containerClassName), "entries")
      .returns(ParameterizedTypeName.get(
        ClassName.get("io.smallrye.mutiny", "Uni"),
        ClassName.get(registry.getPackageName(), containerClassName)
      ));
    
    method.addStatement("final var data = entries.$L()", getterName);
    method.addStatement("final var sql = registry.$L().$L(data)", 
      pluralize(table.getTableName()),
      registryMethod);
    method.addStatement("return visitExecution(sql, $T.class)", entityType);
    
    return method.build();
  }
  
  private MethodSpec generateVisitExecutionMethod(RegistryModel registry, String containerClassName) {
    final var method = MethodSpec.methodBuilder("visitExecution")
      .addModifiers(Modifier.PRIVATE)
      .addParameter(ClassName.get(SqlTupleList.class), "sql")
      .addParameter(ParameterizedTypeName.get(
        ClassName.get(Class.class),
        ClassName.get("?", "")
      ), "type")
      .returns(ParameterizedTypeName.get(
        ClassName.get("io.smallrye.mutiny", "Uni"),
        ClassName.get(registry.getPackageName(), containerClassName)
      ));
    
    method.addStatement("visitTxLog(sql, type)");
    method.addCode("\n");
    method.addStatement("final var container = $T.builder()\n" +
      "  .tenantId(this.dataSource.getTenant().getId())\n" +
      "  .status($T.OK)\n" +
      "  .log(\"\")\n" +
      "  .build()",
      ClassName.get(registry.getPackageName(), containerClassName),
      ClassName.get(BatchStatus.class));
    
    method.addCode("\n");
    method.addCode("return $T.apply(tx, sql)\n", 
      ClassName.get("io.resys.thena.storesql.support", "Execute"));
    method.addCode("  .onItem().transform(row -> {\n");
    method.addCode("    final var text = \"Inserted \" + (row == null ? 0 : row.rowCount()) + \" \" + type.getSimpleName() + \" entries\";\n");
    method.addCode("    return $T.builder()\n" +
      "      .from(container)\n" +
      "      .addMessage($T.builder().text(text).build())\n" +
      "      .build();\n",
      ClassName.get(registry.getPackageName(), containerClassName),
      ClassName.get(ImmutableBatchLog.class));
    method.addCode("  })\n");
    method.addCode("  .onFailure().transform(t -> {\n");
    method.addCode("    final var text = \"Failed to insert \" + sql.getProps().size() + \" \" + type.getSimpleName() + \" entries\";\n");
    method.addCode("    return new $LException(container, text, t);\n",
      registry.getName() + "Transaction");
    method.addStatement("  })");
    
    return method.build();
  }
  
  private MethodSpec generateVisitTxLogMethod() {
    final var method = MethodSpec.methodBuilder("visitTxLog")
      .addModifiers(Modifier.PRIVATE)
      .addParameter(ClassName.get("io.resys.thena.datasource.ThenaSqlClient", "SqlTupleList"), "sql")
      .addParameter(ParameterizedTypeName.get(
        ClassName.get(Class.class),
        ClassName.get("?", "")
      ), "type");
    
    method.beginControlFlow("if(sql.getProps().isEmpty())");
    method.addStatement("return");
    method.endControlFlow();
    
    method.addCode("\n");
    method.addStatement("this.txLog\n" +
      "  .append($T.lineSeparator())\n" +
      "  .append(\"--- processing \").append(sql.getProps().size()).append(\" entries of type: '\").append(type.getSimpleName()).append(\"'\")\n" +
      "  .append(sql.getPropsDeepString()).append($T.lineSeparator())\n" +
      "  .append(sql.getValue()).append($T.lineSeparator())",
      System.class, System.class, System.class);
    
    return method.build();
  }
  
  private MethodSpec generateVisitSuccessMethod(RegistryModel registry, String containerClassName) {
    final var method = MethodSpec.methodBuilder("visitSuccess")
      .addModifiers(Modifier.PRIVATE)
      .addParameter(ClassName.get(registry.getPackageName(), containerClassName), "inputContainer")
      .addParameter(ParameterizedTypeName.get(
        ClassName.get(List.class),
        ClassName.get(registry.getPackageName(), containerClassName)
      ), "items")
      .returns(ClassName.get(registry.getPackageName(), containerClassName));
    
    method.addStatement("final var msg = $T.lineSeparator() + \"--- TX LOG\" + $T.lineSeparator() + txLog",
      System.class, System.class);
    
    method.beginControlFlow("if(log.isDebugEnabled())");
    method.addStatement("log.debug(msg)");
    method.endControlFlow();
    
    method.addCode("\n");
    method.addStatement("return inputContainer.merge(items.stream()\n" +
      "  .reduce((a, b) -> a.merge(b))\n" +
      "  .orElse(inputContainer))");
    
    return method.build();
  }
  
  private MethodSpec generateVisitErrorMethod(RegistryModel registry, String containerClassName) {
    final var exceptionClassName = registry.getName() + "TransactionException";
    
    final var method = MethodSpec.methodBuilder("visitError")
      .addModifiers(Modifier.PRIVATE)
      .addParameter(Throwable.class, "ex")
      .returns(ParameterizedTypeName.get(
        ClassName.get("io.smallrye.mutiny", "Uni"),
        ClassName.get(registry.getPackageName(), containerClassName)
      ));
    
    method.addStatement("final var msg = $T.lineSeparator() + \"--- TX LOG\" + $T.lineSeparator() + txLog",
      System.class, System.class);
    method.addStatement("final var batchError = ($L) ex", exceptionClassName);
    method.addStatement("log.error(\"Failed to save transaction because of: {},\\r\\n{}\", ex.getMessage(), msg, ex)");
    
    method.addCode("\n");
    method.addStatement("return tx.rollback().onItem().transform(junk -> \n" +
      "  $T.builder()\n" +
      "    .from(batchError.getContainer())\n" +
      "    .log(msg)\n" +
      "    .build()\n" +
      ")",
      ClassName.get(registry.getPackageName(), containerClassName));
    
    return method.build();
  }
  
  private TypeSpec generateExceptionClass(RegistryModel registry, String containerClassName) {
    final var exceptionClassName = registry.getName() + "TransactionException";
    
    final var exceptionClass = TypeSpec.classBuilder(exceptionClassName)
      .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
      .superclass(RuntimeException.class);
    
    exceptionClass.addField(FieldSpec.builder(
      ClassName.get(registry.getPackageName(), containerClassName),
      "container",
      Modifier.PRIVATE, Modifier.FINAL
    ).build());
    
    exceptionClass.addMethod(MethodSpec.constructorBuilder()
      .addModifiers(Modifier.PUBLIC)
      .addParameter(ClassName.get(registry.getPackageName(), containerClassName), "container")
      .addParameter(String.class, "message")
      .addParameter(Throwable.class, "cause")
      .addStatement("super(message, cause)")
      .addStatement("this.container = container")
      .build());
    
    exceptionClass.addMethod(MethodSpec.methodBuilder("getContainer")
      .addModifiers(Modifier.PUBLIC)
      .returns(ClassName.get(registry.getPackageName(), containerClassName))
      .addStatement("return container")
      .build());
    
    return exceptionClass.build();
  }
  
  private Map<String, OperationInfo> extractOperations(List<TableModel> tables) {
    final var operations = new HashMap<String, OperationInfo>();
    
    for (final var table : tables) {
      for (final var method : table.getSqlMethods()) {
        final var fieldName = buildOperationFieldName(table, method.getType());
        if (fieldName != null && !operations.containsKey(fieldName)) {
          final var entityType = extractEntityType(method);
          if (entityType != null) {
            operations.put(fieldName, new OperationInfo(table, entityType, method.getType()));
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
  
  private String determineRegistryMethod(TableModel table, String operationName) {
    if (operationName.endsWith("Inserts")) {
      return "insertAll";
    } else if (operationName.endsWith("Updates")) {
      return "updateAll";
    } else if (operationName.endsWith("Deletes")) {
      return "deleteAll";
    }
    return "unknown";
  }
  
  private TypeName extractEntityType(SqlMethod method) {
    if (method.getParameters().isEmpty()) {
      return null;
    }
    
    final var firstParam = method.getParameters().get(0);
    final var paramType = firstParam.getType();
    
    if (paramType instanceof ParameterizedTypeName) {
      final var parameterized = (ParameterizedTypeName) paramType;
      if (!parameterized.typeArguments.isEmpty()) {
        return parameterized.typeArguments.get(0);
      }
    }
    
    return paramType;
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
  
  private String pluralize(String tableName) {
    final var camelCase = toCamelCase(tableName);
    if (!camelCase.endsWith("s")) {
      return camelCase + "s";
    }
    return camelCase;
  }
  
  private static class OperationInfo {
    final TableModel table;
    final TypeName entityType;
    final SqlMethodType methodType;
    
    OperationInfo(TableModel table, TypeName entityType, SqlMethodType methodType) {
      this.table = table;
      this.entityType = entityType;
      this.methodType = methodType;
    }
  }
}