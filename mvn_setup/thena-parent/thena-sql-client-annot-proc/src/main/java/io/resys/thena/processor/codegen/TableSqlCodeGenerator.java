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

import java.util.stream.Collectors;

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import io.resys.thena.datasource.ImmutableSql;
import io.resys.thena.datasource.ImmutableSqlTuple;
import io.resys.thena.datasource.ImmutableSqlTupleList;
import io.resys.thena.datasource.ThenaSqlClient.Sql;
import io.resys.thena.datasource.ThenaSqlClient.SqlTuple;
import io.resys.thena.datasource.ThenaSqlClient.SqlTupleList;
import io.resys.thena.datasource.ThenaSqlDataSource;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler;
import io.resys.thena.processor.model.TableModel;
import io.resys.thena.processor.model.TableModel.MethodParameter;
import io.resys.thena.processor.model.TableModel.RegistryModel;
import io.resys.thena.processor.model.TableModel.SqlMethod;
import io.resys.thena.processor.model.TableModel.SqlPropsType;
import lombok.extern.slf4j.Slf4j;



public class TableSqlCodeGenerator {
  
  public JavaFile generate(TableModel model, RegistryModel registry) {
    final var classBuilder = TypeSpec.classBuilder(model.getImplClassName())
      .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
      .addSuperinterface(ClassName.get(model.getPackageName(), model.getInterfaceName()))
      .addAnnotation(Slf4j.class)
      .addAnnotation(ClassName.get("lombok", "Value"))
      .addAnnotation(ClassName.get("lombok", "AllArgsConstructor"));
    
    // Add fields
    addFields(classBuilder, registry);
    
    // Add constructor
    addConstructor(classBuilder, registry);
    
    // Generate all methods
    for (final var method : model.getSqlMethods()) {
      classBuilder.addMethod(generateMethod(method, model));
    }
    
    return JavaFile.builder(model.getPackageName(), classBuilder.build())
      .indent("  ")
      .build();
  }
  
  private void addFields(TypeSpec.Builder classBuilder, RegistryModel registry) {
    classBuilder.addField(FieldSpec.builder(
      ClassName.get(registry.getPackageName(), registry.getTableClassName()),
      "tables"
    ).build());
    
    classBuilder.addField(FieldSpec.builder(
      ClassName.get(ThenaSqlDataSource.class),
      "dataSource"
    ).build());
    
    classBuilder.addField(FieldSpec.builder(
      ClassName.get(ThenaSqlDataSourceErrorHandler.class),
      "errorHandler"
    ).build());
  }
  
  private void addConstructor(TypeSpec.Builder classBuilder, RegistryModel registry) {
    final var constructor = MethodSpec.constructorBuilder()
      .addModifiers(Modifier.PUBLIC)
      .addParameter(ClassName.get(registry.getPackageName(), registry.getTableClassName()), "tables")
      .addParameter(ClassName.get(ThenaSqlDataSource.class), "dataSource")
      .addStatement("this.tables = tables")
      .addStatement("this.dataSource = dataSource")
      .addStatement("this.errorHandler = dataSource.getErrorHandler()")
      .build();
    
    classBuilder.addMethod(constructor);
  }
  
  private MethodSpec generateMethod(SqlMethod method, TableModel model) {
    return switch (method.getType()) {
      case QUERY -> generateQueryMethod(method, model);
      case INSERT -> generateSingleInsertMethod(method, model);
      case INSERT_ALL -> generateBatchMethod(method, model);
      case UPDATE -> generateSingleUpdateMethod(method, model);
      case UPDATE_ALL -> generateBatchMethod(method, model);
      case DELETE -> generateSingleDeleteMethod(method, model);
      case DELETE_ALL -> generateBatchMethod(method, model);
      case CREATE_TABLE, CREATE_CONSTRAINTS, DROP_TABLE -> generateLifecycleMethod(method, model);
    };
  }
  
  private MethodSpec generateLifecycleMethod(SqlMethod method, TableModel model) {
    final var builder = MethodSpec.methodBuilder(method.getMethodName())
      .addModifiers(Modifier.PUBLIC)
      .returns(ClassName.get(Sql.class));
    
    // Build SQL with table name resolution
    final var resolvedSql = resolveSqlPlaceholders(method.getSqlTemplate(), method.getTableNames());
    
    builder.addStatement("return $T.builder().value($L).build()",
      ClassName.get(ImmutableSql.class),
      resolvedSql);
    
    return builder.build();
  }
  
  private MethodSpec generateQueryMethod(SqlMethod method, TableModel model) {
    final var builder = MethodSpec.methodBuilder(method.getMethodName())
      .addAnnotation(Override.class)
      .addModifiers(Modifier.PUBLIC);
    
    // Add parameters
    for (final var param : method.getParameters()) {
      builder.addParameter(param.getType(), param.getName());
    }
    
    // Determine full return type
    if (method.getWrapperType() != null) {
      builder.returns(ParameterizedTypeName.get(
        (ClassName) method.getWrapperType(), 
        method.getReturnType()
      ));
      generateExecutionBody(builder, method, model);
    } else {
      // Direct SQL return
      builder.returns(getDirectReturnType(method.getPropsType()));
      generateSqlReturnBody(builder, method, model);
    }
    
    return builder.build();
  }
  
  private MethodSpec generateSingleInsertMethod(SqlMethod method, TableModel model) {
    return generateSinglePropsMethod(method, model);
  }
  
  private MethodSpec generateSingleUpdateMethod(SqlMethod method, TableModel model) {
    return generateSinglePropsMethod(method, model);
  }
  
  private MethodSpec generateSingleDeleteMethod(SqlMethod method, TableModel model) {
    return generateSinglePropsMethod(method, model);
  }
  
  private MethodSpec generateSinglePropsMethod(SqlMethod method, TableModel model) {
    final var builder = MethodSpec.methodBuilder(method.getMethodName())
      .addAnnotation(Override.class)
      .addModifiers(Modifier.PUBLIC)
      .returns(ClassName.get(SqlTuple.class));
    
    // Add parameters
    for (final var param : method.getParameters()) {
      builder.addParameter(param.getType(), param.getName());
    }
    
    final var singleParam = method.getParameters().get(0).getName();
    final var resolvedSql = resolveSqlPlaceholders(method.getSqlTemplate(), method.getTableNames());
    
    builder.addStatement("return $T.builder()\n" +
      "  .value($L)\n" +
      "  .props(new $T().apply($L))\n" +
      "  .build()",
      ClassName.get(ImmutableSqlTuple.class),
      resolvedSql,
      ClassName.bestGuess(method.getMapperClassName()),
      singleParam);
    
    return builder.build();
  }
  
  private MethodSpec generateBatchMethod(SqlMethod method, TableModel model) {
    final var builder = MethodSpec.methodBuilder(method.getMethodName())
      .addAnnotation(Override.class)
      .addModifiers(Modifier.PUBLIC)
      .returns(ClassName.get(SqlTupleList.class));
    
    // Add parameters
    for (final var param : method.getParameters()) {
      builder.addParameter(param.getType(), param.getName());
    }
    
    final var collectionParam = method.getParameters().get(0).getName();
    final var resolvedSql = resolveSqlPlaceholders(method.getSqlTemplate(), method.getTableNames());
    
    builder.addStatement("final var mapper = new $T()", ClassName.bestGuess(method.getMapperClassName()));
    builder.addStatement("return $T.builder()\n" +
      "  .value($L)\n" +
      "  .props($L.stream()\n" +
      "    .map(mapper::apply)\n" +
      "    .collect($T.toList()))\n" +
      "  .build()",
      ClassName.get(ImmutableSqlTupleList.class),
      resolvedSql,
      collectionParam,
      ClassName.get("java.util.stream", "Collectors"));
    
    return builder.build();
  }
  
  private void generateSqlReturnBody(MethodSpec.Builder builder, SqlMethod method, TableModel model) {
    final var resolvedSql = resolveSqlPlaceholders(method.getSqlTemplate(), method.getTableNames());
    
    if (method.getPropsType() == SqlPropsType.SQL) {
      builder.addStatement("return $T.builder().value($L).build()",
        ClassName.get(ImmutableSql.class),
        resolvedSql);
    } else {
      final var propsArgs = method.getParameters().stream()
        .map(MethodParameter::getName)
        .collect(Collectors.joining(", "));
      
      builder.addStatement("return $T.builder().value($L).props($T.of($L)).build()",
        ClassName.get(ImmutableSqlTuple.class),
        resolvedSql,
        ClassName.get("io.vertx.mutiny.sqlclient", "Tuple"),
        propsArgs);
    }
  }
  
  private void generateExecutionBody(MethodSpec.Builder builder, SqlMethod method, TableModel model) {
    final var resolvedSql = resolveSqlPlaceholders(method.getSqlTemplate(), method.getTableNames());
    
    // Layer 1: Build SQL
    if (method.getPropsType() == SqlPropsType.SQL) {
      builder.addStatement("final var sql = $T.builder().value($L).build()",
        ClassName.get(ImmutableSql.class),
        resolvedSql);
    } else {
      final var propsArgs = method.getParameters().stream()
        .map(MethodParameter::getName)
        .collect(Collectors.joining(", "));
      
      builder.addStatement("final var sql = $T.builder().value($L).props($T.of($L)).build()",
        ClassName.get(ImmutableSqlTuple.class),
        resolvedSql,
        ClassName.get("io.vertx.mutiny.sqlclient", "Tuple"),
        propsArgs);
    }
    
    // Layer 2: Logging
    builder.beginControlFlow("if(log.isDebugEnabled())")
      .addStatement("log.debug($S, sql.getPropsDeepString(), sql.getValue())",
        "Query " + method.getMethodName() + " with props: {} \\r\\n{}")
      .endControlFlow();
    
    // Layer 3: Execute with error handling
    builder.addCode("return dataSource.getClient().preparedQuery(sql.getValue())\n")
      .addCode("  .mapping(new $T())\n", ClassName.bestGuess(method.getMapperClassName()))
      .addCode("  .execute(sql.getProps())\n")
      .addCode("  .onItem().transformToMulti($T::toMulti)\n", 
        ClassName.get("io.vertx.mutiny.sqlclient", "RowSet"))
      .addCode("  .collect().asList()\n")
      .addStatement("  .onFailure().invoke(e -> errorHandler.deadEnd(sql.failed(e, $S)))",
        "Can't execute " + method.getMethodName());
  }
  
  private String resolveSqlPlaceholders(String sql, java.util.List<String> tableNames) {
    // Normalize SQL - replace newlines and excess whitespace
    var normalizedSql = sql.replaceAll("\\s+", " ").trim();
    
    if (tableNames.isEmpty()) {
      return "\"" + normalizedSql + "\"";
    }
    
    // Build the SQL string with table name replacements
    final var result = new StringBuilder("\"");
    var current = normalizedSql;
    
    for (final var tableName : tableNames) {
      final var placeholder = "{" + tableName + "}";
      final var parts = current.split(java.util.regex.Pattern.quote(placeholder), -1);
      
      if (parts.length > 1) {
        final var getterName = toCamelCaseCapitalized(tableName);
        current = String.join("\" + tables.get" + getterName + "() + \"", parts);
      }
    }
    
    result.append(current).append("\"");
    return result.toString();
  }
  
  private String toCamelCaseCapitalized(String snakeCase) {
    final var parts = snakeCase.split("_");
    final var result = new StringBuilder();
    for (final var part : parts) {
      if (!part.isEmpty()) {
        result.append(Character.toUpperCase(part.charAt(0)))
              .append(part.substring(1));
      }
    }
    return result.toString();
  }
  
  private ClassName getDirectReturnType(SqlPropsType propsType) {
    return switch (propsType) {
      case SQL -> ClassName.get(Sql.class);
      case SQL_TUPLE -> ClassName.get(SqlTuple.class);
      case SQL_TUPLE_LIST -> ClassName.get(SqlTupleList.class);
    };
  }
}