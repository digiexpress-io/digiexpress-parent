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

import io.resys.thena.api.entities.BatchStatus;
import io.resys.thena.api.entities.ImmutableBatchLog;
import io.resys.thena.datasource.ThenaSqlClient;
import io.resys.thena.datasource.ThenaSqlClient.SqlTupleList;
import io.resys.thena.datasource.ThenaSqlDataSource;
import io.resys.thena.processor.model.MultiTableCodeGenerator;
import io.resys.thena.processor.model.TableModel;
import io.resys.thena.processor.model.TableModel.RegistryModel;
import io.resys.thena.processor.model.TableModel.SqlMethod;
import io.resys.thena.processor.model.TableModel.SqlMethodType;
import io.resys.thena.processor.support.NamingUtils;
import io.smallrye.mutiny.Uni;

public class Gen_Multi_BuilderImplementation implements MultiTableCodeGenerator {
  
  public JavaFile generate(RegistryModel registry, List<TableModel> tables) {
    final var className = registry.getName() + "DbBuilderImpl";
    final var builderInterfaceName = registry.getName() + "DbBuilder";
    final var persistenceUnitName = builderInterfaceName + ".PersistenceUnit";
    final var registryClassName = registry.getRegistryClassName();
    final var operations = extractOperations(tables);
    
    final var classBuilder = TypeSpec.classBuilder(className)
      .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
      .addSuperinterface(ClassName.get(registry.getPackageName(), builderInterfaceName));
    
    classBuilder.addField(generateLoggerField(registry));
    
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
    classBuilder.addMethod(generateVisitTxLogMethod());
    classBuilder.addMethod(generateVisitSuccessMethod(registry, persistenceUnitName));
    classBuilder.addMethod(generateVisitErrorMethod(registry, persistenceUnitName));
    
    classBuilder.addType(generateExceptionClass(registry, persistenceUnitName));
    
    return JavaFile.builder(registry.getPackageName() + ".spi", classBuilder.build())
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
  
  private MethodSpec generateConstructor(RegistryModel registry, String registryClassName) {
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
  
  private MethodSpec generateFromMethod(RegistryModel registry, String builderInterfaceName) {
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
      RegistryModel registry, 
      String builderInterfaceName,
      String persistenceUnitName,
      Map<String, OperationInfo> operations,
      List<TableModel> tables) {
    
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
    
    final var visitCalls = new ArrayList<String>();
    for (final var operationName : operations.keySet()) {
      visitCalls.add("visit" + NamingUtils.capitalize(operationName) + "(entries)");
    }
    
    method.addCode("    " + String.join(",\n    ", visitCalls));
    method.addCode("\n  )\n");
    
    method.addCode("  .with($T.class, (items) -> visitSuccess(entries, items))\n", 
      ClassName.bestGuess(persistenceUnitName));
    
    method.addCode("  .onFailure($LException.class)\n", 
      registry.getName() + "Builder");
    method.addStatement("  .recoverWithUni(this::visitError)");
    
    return method.build();
  }
  
  private MethodSpec generateVisitMethod(
      RegistryModel registry,
      String persistenceUnitName,
      String operationName,
      TableModel table,
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
  
  private MethodSpec generateVisitExecutionMethod(RegistryModel registry, String persistenceUnitName) {
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
    method.addCode("    final var text = \"Inserted \" + (row == null ? 0 : row.rowCount()) + \" \" + type.getSimpleName() + \" entries\";\n");
    method.addCode("    final var updatedMessages = new $T<>(container.getCommitLogs());\n", ArrayList.class);
    method.addCode("    updatedMessages.add($T.builder().text(text).build());\n", 
      ClassName.get(ImmutableBatchLog.class));
    method.addCode("    return ($T) Immutable$L.builder().from(container)\n", ClassName.bestGuess(persistenceUnitName), "PersistenceUnit");
    method.addCode("      .commitLogs(updatedMessages)\n");
    method.addCode("      .build();\n");
    method.addCode("  })\n");
    method.addCode("  .onFailure().transform(t -> {\n");
    method.addCode("    final var text = \"Failed to insert \" + sql.getProps().size() + \" \" + type.getSimpleName() + \" entries\";\n");
    method.addCode("    return new $LException(container, text, t);\n",
      registry.getName() + "Builder");
    method.addStatement("  })");
    
    return method.build();
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
  
  private MethodSpec generateVisitSuccessMethod(RegistryModel registry, String persistenceUnitName) {
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
  
  private MethodSpec generateVisitErrorMethod(RegistryModel registry, String persistenceUnitName) {
    final var exceptionClassName = registry.getName() + "BuilderException";
    
    final var method = MethodSpec.methodBuilder("visitError")
      .addModifiers(Modifier.PRIVATE)
      .addParameter(Throwable.class, "ex")
      .returns(ParameterizedTypeName.get(
        ClassName.get(Uni.class),
        ClassName.bestGuess(persistenceUnitName)
      ));
    
    method.addStatement("final var msg = $T.lineSeparator() + \"--- TX LOG\" + $T.lineSeparator() + txLog",
      System.class, System.class);
    method.addStatement("final var builderError = ($L) ex", exceptionClassName);
    method.addStatement("log.error(\"Failed to persist because of: {},\\r\\n{}\", ex.getMessage(), msg, ex)");
    
    method.addCode("\n");
    method.addStatement("return tx.rollback().onItem().transform(junk -> \n" +
      "  Immutable$L.builder().from(builderError.getContainer())\n" +
      "    .log(msg)\n" +
      "    .build()\n" +
      ")",
      "PersistenceUnit");
    
    return method.build();
  }
  
  private TypeSpec generateExceptionClass(RegistryModel registry, String persistenceUnitName) {
    final var exceptionClassName = registry.getName() + "BuilderException";
    
    final var exceptionClass = TypeSpec.classBuilder(exceptionClassName)
      .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
      .superclass(RuntimeException.class);
    
    exceptionClass.addField(FieldSpec.builder(
      ClassName.bestGuess(persistenceUnitName),
      "container",
      Modifier.PRIVATE, Modifier.FINAL
    ).build());
    
    exceptionClass.addMethod(MethodSpec.constructorBuilder()
      .addModifiers(Modifier.PUBLIC)
      .addParameter(ClassName.bestGuess(persistenceUnitName), "container")
      .addParameter(String.class, "message")
      .addParameter(Throwable.class, "cause")
      .addStatement("super(message, cause)")
      .addStatement("this.container = container")
      .build());
    
    exceptionClass.addMethod(MethodSpec.methodBuilder("getContainer")
      .addModifiers(Modifier.PUBLIC)
      .returns(ClassName.bestGuess(persistenceUnitName))
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
    final TableModel table;
    final TypeName entityType;
    @SuppressWarnings("unused")
    final SqlMethodType methodType;
    
    OperationInfo(TableModel table, TypeName entityType, SqlMethodType methodType) {
      this.table = table;
      this.entityType = entityType;
      this.methodType = methodType;
    }
  }
}
