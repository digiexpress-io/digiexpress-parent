package io.resys.thena.processor.codegen;

/*-
 * #%L
 * thena-sql-client-annot-proc
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
import com.squareup.javapoet.WildcardTypeName;

import io.resys.thena.api.envelope.BatchStatus;
import io.resys.thena.api.envelope.ImmutableMessage;
import io.resys.thena.datasource.ThenaSqlClient;
import io.resys.thena.datasource.ThenaSqlClient.SqlTupleList;
import io.resys.thena.datasource.ThenaSqlDataSource;
import io.resys.thena.processor.model.Metamodel;
import io.resys.thena.processor.model.RegistryMetamodel;
import io.resys.thena.processor.model.TableMetamodel;
import io.resys.thena.processor.model.TableMetamodel.SqlMethod;
import io.resys.thena.processor.model.TableMetamodel.SqlMethodType;
import io.resys.thena.processor.spi.MultiTableCodeGenerator;
import io.resys.thena.processor.support.NamingUtils;
import io.smallrye.mutiny.Uni;

public class Gen_Multi_BuilderImplementation implements MultiTableCodeGenerator {
  
  public JavaFile generate(RegistryMetamodel registry, List<TableMetamodel> tables, Metamodel metamodel) {
    final var className = registry.getName() + "DbBuilderImpl";
    final var builderInterfaceName = registry.getName() + "DbBuilder";
    final var persistenceUnitName = builderInterfaceName + ".PersistenceUnit";
    final var registryClassName = registry.getRegistryClassName();
    final var operations = extractOperations(tables);
    
    final var classBuilder = TypeSpec.classBuilder(className)
      .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
      .addSuperinterface(ClassName.get(registry.getPackageName(), builderInterfaceName));
    
    classBuilder.addField(generateLoggerField(registry));
    classBuilder.addField(generateMetricsLoggerField(registry));

    classBuilder.addField(FieldSpec.builder(
      ClassName.get(ThenaSqlClient.class),
      "tx",
      Modifier.PRIVATE, Modifier.FINAL
    ).build());
    
    classBuilder.addField(FieldSpec.builder(
      ClassName.get(ThenaSqlDataSource.class),
      "dataSource",
      Modifier.PRIVATE, Modifier.FINAL
    ).build());
    
    classBuilder.addField(FieldSpec.builder(
      ClassName.bestGuess(registryClassName),
      "registry",
      Modifier.PRIVATE, Modifier.FINAL
    ).build());
    
    classBuilder.addField(FieldSpec.builder(
      StringBuilder.class,
      "txLog",
      Modifier.PRIVATE, Modifier.FINAL
    ).build());
    
    classBuilder.addField(FieldSpec.builder(
      ClassName.get(registry.getPackageName(), "ImmutablePersistenceUnit", "Builder"),
      "init",
      Modifier.PRIVATE, Modifier.FINAL
    ).initializer("ImmutablePersistenceUnit.builder()").build());
    
    classBuilder.addMethod(generateConstructor(registry, registryClassName));
    classBuilder.addMethod(generateFromMethod(registry, builderInterfaceName));
    classBuilder.addMethod(generatePersistMethod(registry, builderInterfaceName, persistenceUnitName, operations, tables));
    
    for (final var entry : operations.entrySet()) {
      final var operation = entry.getKey();
      final var table = entry.getValue().table;
      final var entityType = entry.getValue().entityType;
      
      classBuilder.addMethod(generateVisitMethod(
        registry, 
        persistenceUnitName, 
        operation, 
        table, 
        entityType
      ));
    }
    
    classBuilder.addMethod(generateVisitExecutionMethod(registry, persistenceUnitName));
    classBuilder.addMethod(generateBuildSqlMessageMethod());
    classBuilder.addMethod(generateVisitTxLogMethod());
    classBuilder.addMethod(generateVisitSuccessMethod(registry, persistenceUnitName));
    classBuilder.addMethod(generateVisitErrorMethod(registry, persistenceUnitName));
    
    return JavaFile.builder(registry.getPackageName() + ".spi", classBuilder.build())
      .indent("  ")
      .build();
  }
  
  private FieldSpec generateLoggerField(RegistryMetamodel registry) {
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
  
  private FieldSpec generateMetricsLoggerField(RegistryMetamodel registry) {
    final var loggerTopic = registry.getPackageName() + "." +
                            registry.getName().toLowerCase() + ".metrics";

    return FieldSpec.builder(
      ClassName.get("org.slf4j", "Logger"),
      "metricsLog",
      Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL
    )
    .initializer("$T.getLogger($S)",
      ClassName.get("org.slf4j", "LoggerFactory"),
      loggerTopic
    )
    .build();
  }

  private MethodSpec generateConstructor(RegistryMetamodel registry, String registryClassName) {
    return MethodSpec.constructorBuilder()
      .addModifiers(Modifier.PUBLIC)
      .addParameter(ClassName.get(ThenaSqlDataSource.class), "dataSource")
      .addStatement("final var names = $T.defaults().toRepo(dataSource.getTenant())", 
        ClassName.bestGuess(registry.getTableClassName()))
      .addStatement("this.registry = new $T(names, dataSource)", 
        ClassName.bestGuess(registryClassName))
      .addStatement("this.dataSource = dataSource")
      .addStatement("this.tx = dataSource.getClient()")
      .addStatement("this.txLog = new $T()", StringBuilder.class)
      .build();
  }
  
  private MethodSpec generateFromMethod(RegistryMetamodel registry, String builderInterfaceName) {
    return MethodSpec.methodBuilder("from")
      .addAnnotation(Override.class)
      .addModifiers(Modifier.PUBLIC)
      .addParameter(ClassName.bestGuess(builderInterfaceName + ".PersistenceUnit"), "unit")
      .returns(ClassName.bestGuess(builderInterfaceName))
      .addStatement("init.from(unit)")
      .addStatement("return this")
      .build();
  }
  
  private MethodSpec generatePersistMethod(
      RegistryMetamodel registry, 
      String builderInterfaceName,
      String persistenceUnitName,
      Map<String, OperationInfo> operations,
      List<TableMetamodel> tables) {
    
    final var method = MethodSpec.methodBuilder("persist")
      .addAnnotation(Override.class)
      .addModifiers(Modifier.PUBLIC)
      .returns(ParameterizedTypeName.get(
        ClassName.get(Uni.class),
        ClassName.bestGuess(persistenceUnitName)
      ));
    
    method.addStatement("final var entries = init.build()");
    method.addCode("\n");
    method.addCode("return $T.combine().all()\n", ClassName.get(Uni.class));
    method.addCode("  .unis(\n");
    
    // Group operations by type: DELETE -> INSERT -> UPDATE
    final var deleteOps = new ArrayList<String>();
    final var insertOps = new ArrayList<String>();  
    final var updateOps = new ArrayList<String>();
    
    // Sort tables by order to maintain referential integrity
    final var tablesByOrder = tables.stream()
        .sorted((a, b) -> Integer.compare(a.getOrder(), b.getOrder()))
        .toList();
    
    // Group operations by type and sort by table order
    for (final var table : tablesByOrder) {
      for (final var sqlMethod : table.getSqlMethods()) {
        final var fieldName = buildOperationFieldName(table, sqlMethod.getType());
        if (fieldName != null && operations.containsKey(fieldName)) {
          final var visitCall = "visit" + NamingUtils.capitalize(fieldName) + "(entries)";
          
          switch (sqlMethod.getType()) {
            case DELETE_ALL -> deleteOps.add(visitCall);
            case INSERT_ALL -> insertOps.add(visitCall);
            case UPDATE_ALL -> updateOps.add(visitCall);
            default -> { /* skip */ }
          }
        }
      }
    }
    
    
    int orderNumber = 1;
    
    // 1. DELETES
    if (!deleteOps.isEmpty()) {
      method.addCode("    // === DELETE OPERATIONS ===\n");
      for (final var deleteOp : deleteOps) {
        if(orderNumber > 1) {
          method.addCode("    ,");  
        } else {
          method.addCode("     ");
        }
        method.addCode(deleteOp + " // " + (orderNumber++) + "\n");
      }
      method.addCode("\n\n");
      
    }
    
    // 2. INSERTS  
    if (!insertOps.isEmpty()) {
      method.addCode("    // === INSERT OPERATIONS ===\n");
      for (final var insertOp : insertOps) {
        if(orderNumber > 1) {
          method.addCode("    ,");  
        } else {
          method.addCode("     ");
        }
        method.addCode(insertOp + " // " + (orderNumber++) + "\n");
      }
      method.addCode("\n\n");
      
    }
    
    // 3. UPDATES
    if (!updateOps.isEmpty()) {
      method.addCode("    // === UPDATE OPERATIONS ===\n");
      for (final var updateOp : updateOps) {
        if(orderNumber > 1) {
          method.addCode("    ,");  
        } else {
          method.addCode("     ");
        }
        method.addCode(updateOp + " // " + (orderNumber++) + "\n");
      }
      method.addCode("\n\n");
    }
    
    
    method.addCode("\n  )\n");
    
    
    method.addCode("  .with($T.class, (items) -> visitSuccess(entries, items))\n", 
      ClassName.bestGuess(persistenceUnitName));
    
    method.addCode("  .onFailure($LException.class)\n", 
      registry.getName() + "Builder");
    method.addStatement("  .call(this::visitError)");
    
    return method.build();
  }
  
  private MethodSpec generateVisitMethod(
      RegistryMetamodel registry,
      String persistenceUnitName,
      String operationName,
      TableMetamodel table,
      TypeName entityType) {
    
    final var methodName = "visit" + NamingUtils.capitalize(operationName);
    final var getterName = "get" + NamingUtils.capitalize(operationName);
    
    final var sqlMethod = table.getSqlMethods().stream()
      .filter(m -> buildOperationFieldName(table, m.getType()) != null)
      .filter(m -> buildOperationFieldName(table, m.getType()).equals(operationName))
      .findFirst()
      .orElseThrow(() -> new IllegalStateException("No SQL method found for operation: " + operationName));
    
    final var method = MethodSpec.methodBuilder(methodName)
      .addModifiers(Modifier.PRIVATE)
      .addParameter(ClassName.bestGuess(persistenceUnitName), "entries")
      .returns(ParameterizedTypeName.get(
        ClassName.get(Uni.class),
        ClassName.bestGuess(persistenceUnitName)
      ));
    
    method.addStatement("final var data = entries.$L()", getterName);
    method.addStatement("final var sql = registry.$L().$L(data)", 
        NamingUtils.pluralize(table.getTableName()),
      sqlMethod.getMethodName());
    method.addStatement("return visitExecution(sql, $T.class)", entityType);
    
    return method.build();
  }
  
  private MethodSpec generateVisitExecutionMethod(RegistryMetamodel registry, String persistenceUnitName) {
    final var method = MethodSpec.methodBuilder("visitExecution")
      .addModifiers(Modifier.PRIVATE)
      .addParameter(ClassName.get(SqlTupleList.class), "sql")
      .addParameter(ParameterizedTypeName.get(
        ClassName.get(Class.class),
        WildcardTypeName.subtypeOf(Object.class)
      ), "type")
      .returns(ParameterizedTypeName.get(
        ClassName.get(Uni.class),
        ClassName.bestGuess(persistenceUnitName)
      ));
    
    method.addStatement("visitTxLog(sql, type)");
    method.addCode("\n");
    method.addStatement("final var metricsEnabled = metricsLog.isDebugEnabled()");
    method.addStatement("final var metricsMessage = metricsEnabled ? buildSqlMessage(sql, type) : null");
    method.addStatement("final var metricsStart = metricsEnabled ? $T.nanoTime() : 0L", System.class);
    method.addCode("\n");
    method.addStatement("final var container = Immutable$L.builder()\n" +
      "  .tenantId(this.dataSource.getTenant().getId())\n" +
      "  .status($T.OK)\n" +
      "  .log(\"\")\n" +
      "  .build()",
      "PersistenceUnit",
      ClassName.get(BatchStatus.class));

    method.addCode("\n");
    method.addCode("return $T.apply(tx, sql)\n",
      ClassName.get("io.resys.thena.storesql.support", "Execute"));
    method.addCode("  .onItem().transform(row -> {\n");
    method.addCode("    final var rowCount = row == null ? 0 : row.rowCount();\n");
    method.addCode("    if (metricsEnabled) {\n");
    method.addCode("      final var tookMillis = ($T.nanoTime() - metricsStart) / 1_000_000;\n", System.class);
    method.addCode("      metricsLog.debug(\"{} | took={}ms rows={}\", metricsMessage, tookMillis, rowCount);\n");
    method.addCode("    }\n");
    method.addCode("    final var text = \"Inserted \" + rowCount + \" \" + type.getSimpleName() + \" entries\";\n");
    method.addCode("    final var updatedMessages = new $T<>(container.getCommitLogs());\n", ArrayList.class);
    method.addCode("    updatedMessages.add($T.builder().text(text).build());\n",
      ClassName.get(ImmutableMessage.class));
    method.addCode("    return ($T) Immutable$L.builder().from(container)\n", ClassName.bestGuess(persistenceUnitName), "PersistenceUnit");
    method.addCode("      .commitLogs(updatedMessages)\n");
    method.addCode("      .build();\n");
    method.addCode("  })\n");
    method.addCode("  .onFailure().transform(t -> {\n");
    method.addCode("    if (metricsEnabled) {\n");
    method.addCode("      final var tookMillis = ($T.nanoTime() - metricsStart) / 1_000_000;\n", System.class);
    method.addCode("      metricsLog.debug(\"{} | took={}ms FAILED\", metricsMessage, tookMillis);\n");
    method.addCode("    }\n");
    method.addCode("    final var text = \"Failed to insert \" + sql.getProps().size() + \" \" + type.getSimpleName() + \" entries\";\n");
    method.addCode("    return new $LException(container, text, txLog.toString(), t);\n",
      registry.getName() + "Builder");
    method.addStatement("  })");

    return method.build();
  }

  private MethodSpec generateBuildSqlMessageMethod() {
    return MethodSpec.methodBuilder("buildSqlMessage")
      .addModifiers(Modifier.PRIVATE)
      .addParameter(ClassName.get(SqlTupleList.class), "sql")
      .addParameter(ParameterizedTypeName.get(
        ClassName.get(Class.class),
        WildcardTypeName.subtypeOf(Object.class)
      ), "type")
      .returns(String.class)
      .addStatement(
        "return \"processing \" + sql.getProps().size() + \" entries of type: '\" + type.getSimpleName() + \"'\"\n" +
        "  + sql.getPropsDeepString() + \" \" + sql.getValue()")
      .build();
  }
  
  private MethodSpec generateVisitTxLogMethod() {
    final var method = MethodSpec.methodBuilder("visitTxLog")
      .addModifiers(Modifier.PRIVATE)
      .addParameter(ClassName.get(SqlTupleList.class), "sql")
      .addParameter(ParameterizedTypeName.get(
        ClassName.get(Class.class),
        WildcardTypeName.subtypeOf(Object.class)
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
  
  private MethodSpec generateVisitSuccessMethod(RegistryMetamodel registry, String persistenceUnitName) {
    final var method = MethodSpec.methodBuilder("visitSuccess")
      .addModifiers(Modifier.PRIVATE)
      .addParameter(ClassName.bestGuess(persistenceUnitName), "inputContainer")
      .addParameter(ParameterizedTypeName.get(
        ClassName.get(List.class),
        ClassName.bestGuess(persistenceUnitName)
      ), "items")
      .returns(ClassName.bestGuess(persistenceUnitName));
    
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
  
  private MethodSpec generateVisitErrorMethod(RegistryMetamodel registry, String persistenceUnitName) {
    final var method = MethodSpec.methodBuilder("visitError")
      .addModifiers(Modifier.PRIVATE)
      .addParameter(Throwable.class, "ex")
      .returns(ParameterizedTypeName.get(
        ClassName.get(Uni.class),
        ClassName.get(Void.class)
      ));
    
    method.addStatement("final var msg = $T.lineSeparator() + \"--- TX LOG\" + $T.lineSeparator() + txLog", System.class, System.class);
    method.addStatement("log.error(\"Failed to persist because of: {},\\r\\n{}\", ex.getMessage(), msg, ex)");
    
    method.addCode("\n");
    method.addStatement("return tx.rollback()");
    
    return method.build();
  }
  

  
  private Map<String, OperationInfo> extractOperations(List<TableMetamodel> tables) {
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
  
  private String buildOperationFieldName(TableMetamodel table, SqlMethodType type) {
    final var baseName = NamingUtils.toCamelCase(table.getTableName());
    
    return switch (type) {
      case INSERT_ALL -> baseName + "Inserts";
      case UPDATE_ALL -> baseName + "Updates";
      case DELETE_ALL -> baseName + "Deletes";
      default -> null;
    };
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
  


  
  private static class OperationInfo {
    final TableMetamodel table;
    final TypeName entityType;
    @SuppressWarnings("unused")
    final SqlMethodType methodType;
    
    OperationInfo(TableMetamodel table, TypeName entityType, SqlMethodType methodType) {
      this.table = table;
      this.entityType = entityType;
      this.methodType = methodType;
    }
  }
}
