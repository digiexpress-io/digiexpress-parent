package io.resys.thena.processor.codegen;

import java.util.Arrays;

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
 * Generates the concrete SQL implementation class for individual table operations.
 * 
 * <p>This is the core generator that transforms a table interface (annotated with 
 * @TenantSql.Table) into a complete SQL implementation class. It generates all the 
 * SQL operations, row mapping, and provides type-safe database access for a single table.
 * 
 * <h3>Generated Code Example:</h3>
 * <pre>{@code
 * @Component
 * public class ContractTableImpl implements ContractTable {
 *   
 *   private final io.vertx.mutiny.sqlclient.Pool client;
 *   private final String tenantId;
 *   
 *   public ContractTableImpl(io.vertx.mutiny.sqlclient.Pool client, String tenantId) {
 *     this.client = client;
 *     this.tenantId = tenantId;
 *   }
 *   
 *   @Override
 *   public Uni<Contract> findById(String id) {
 *     return client.preparedQuery("""
 *         SELECT id, party_id, policy_number, created_commit_id, commit_id 
 *         FROM contract 
 *         WHERE id = $1 AND tenant_id = $2
 *         """)
 *         .execute(Tuple.of(id, tenantId))
 *         .map(this::mapContract);
 *   }
 *   
 *   @Override
 *   public Uni<List<Contract>> findByPartyId(String partyId) {
 *     return client.preparedQuery("""
 *         SELECT id, party_id, policy_number, created_commit_id, commit_id 
 *         FROM contract 
 *         WHERE party_id = $1 AND tenant_id = $2
 *         """)
 *         .execute(Tuple.of(partyId, tenantId))
 *         .map(rowSet -> StreamSupport.stream(rowSet.spliterator(), false)
 *             .map(this::mapContract)
 *             .collect(Collectors.toList()));
 *   }
 *   
 *   @Override
 *   public Uni<Contract> insertOne(Contract contract) {
 *     return client.preparedQuery("""
 *         INSERT INTO contract (id, party_id, policy_number, tenant_id, created_commit_id, commit_id) 
 *         VALUES ($1, $2, $3, $4, $5, $6)
 *         """)
 *         .execute(Tuple.of(contract.getId(), contract.getPartyId(), 
 *                          contract.getPolicyNumber(), tenantId,
 *                          contract.getCreatedCommitId(), contract.getCommitId()))
 *         .map(rows -> contract);
 *   }
 *   
 *   private Contract mapContract(Row row) {
 *     return ImmutableContract.builder()
 *         .id(row.getString("id"))
 *         .partyId(row.getString("party_id"))
 *         .policyNumber(row.getString("policy_number"))
 *         .createdCommitId(row.getString("created_commit_id"))
 *         .commitId(row.getString("commit_id"))
 *         .build();
 *   }
 * }
 * }</pre>
 * 
 * <h3>Key Features:</h3>
 * <ul>
 * <li>Implements all methods defined in the table interface</li>
 * <li>Generates type-safe SQL queries with proper parameter binding</li>
 * <li>Creates row mappers that transform SQL results to domain objects</li>
 * <li>Handles multi-tenancy with automatic tenant_id filtering</li>
 * <li>Supports reactive patterns with Uni return types</li>
 * <li>Includes CRUD operations (Create, Read, Update, Delete)</li>
 * <li>Supports batch operations for bulk data processing</li>
 * <li>Proper SQL injection prevention through prepared statements</li>
 * </ul>
 * 
 * <h3>Usage Pattern:</h3>
 * <pre>{@code
 * @Inject ContractTable contractTable;
 * 
 * // Find by ID
 * Uni<Contract> contract = contractTable.findById("contract123");
 * 
 * // Insert new contract
 * Contract newContract = ImmutableContract.builder()
 *     .id("new123")
 *     .partyId("party456")
 *     .policyNumber("POL789")
 *     .build();
 * Uni<Contract> inserted = contractTable.insertOne(newContract);
 * 
 * // Custom queries
 * Uni<List<Contract>> activeContracts = contractTable.findByPartyId("party456");
 * }</pre>
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
import io.resys.thena.processor.model.Metamodel;
import io.resys.thena.processor.model.RegistryMetamodel;
import io.resys.thena.processor.model.TableMetamodel;
import io.resys.thena.processor.model.TableMetamodel.MethodParameter;
import io.resys.thena.processor.model.TableMetamodel.SqlMethod;
import io.resys.thena.processor.model.TableMetamodel.SqlPropsType;
import io.resys.thena.processor.spi.TableCodeGenerator;
import io.resys.thena.processor.support.NamingUtils;
import lombok.RequiredArgsConstructor;



@RequiredArgsConstructor
public class Gen_Table_SqlImplementation implements TableCodeGenerator {
  
  private final Metamodel metamodel;

  public JavaFile generate(TableMetamodel model, RegistryMetamodel registry) {
    final var classBuilder = TypeSpec.classBuilder(model.getImplClassName())
      .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
      .addSuperinterface(ClassName.get(model.getPackageName(), model.getInterfaceName()))
      .addAnnotation(ClassName.get("lombok.extern.slf4j", "Slf4j"))
      .addAnnotation(ClassName.get("lombok", "Value"))
      .addAnnotation(ClassName.get("lombok", "AllArgsConstructor"));
    
    // Add fields
    addFields(classBuilder, registry);
    
    // Add constructor
    addConstructor(classBuilder, registry);
    
    // Generate all methods
    for (final var method : model.getSqlMethods()) {
      classBuilder.addMethod(generateMethod(method, model, registry));
    }
    
    return JavaFile.builder(model.getPackageName() + ".spi", classBuilder.build())
      .indent("  ")
      .build();
  }
  
  private void addFields(TypeSpec.Builder classBuilder, RegistryMetamodel registry) {
    classBuilder.addField(FieldSpec.builder(
      ClassName.get(registry.getPackageName() + ".spi", registry.getTableClassName()),
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
  
  private void addConstructor(TypeSpec.Builder classBuilder, RegistryMetamodel registry) {
    final var constructor = MethodSpec.constructorBuilder()
      .addModifiers(Modifier.PUBLIC)
      .addParameter(ClassName.bestGuess(registry.getTableClassName()), "tables")
      .addParameter(ClassName.get(ThenaSqlDataSource.class), "dataSource")
      .addStatement("this.tables = tables")
      .addStatement("this.dataSource = dataSource")
      .addStatement("this.errorHandler = dataSource.getErrorHandler()")
      .build();
    
    classBuilder.addMethod(constructor);
  }
  
  private MethodSpec generateMethod(SqlMethod method, TableMetamodel model, RegistryMetamodel registry) {
    return switch (method.getType()) {
      case SELECT -> generateQueryMethod(method, model, registry);
      case SELECT_ALL -> generateQueryMethod(method, model, registry);
      case INSERT -> generateSingleInsertMethod(method, model);
      case INSERT_ALL -> generateBatchMethod(method, model);
      case UPDATE -> generateSingleUpdateMethod(method, model);
      case UPDATE_ALL -> generateBatchMethod(method, model);
      case DELETE -> generateSingleDeleteMethod(method, model);
      case DELETE_ALL -> generateBatchMethod(method, model);
      case CREATE_TABLE, CREATE_CONSTRAINTS, DROP_TABLE -> generateLifecycleMethod(method, model);
    };
  }
  
  private MethodSpec generateLifecycleMethod(SqlMethod method, TableMetamodel model) {
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
  
  private MethodSpec generateQueryMethod(SqlMethod method, TableMetamodel model, RegistryMetamodel registry) {
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
      generateSqlReturnBody(builder, method, model, registry);
    }
    
    return builder.build();
  }
  
  private MethodSpec generateSingleInsertMethod(SqlMethod method, TableMetamodel model) {
    return generateSinglePropsMethod(method, model);
  }
  
  private MethodSpec generateSingleUpdateMethod(SqlMethod method, TableMetamodel model) {
    return generateSinglePropsMethod(method, model);
  }
  
  private MethodSpec generateSingleDeleteMethod(SqlMethod method, TableMetamodel model) {
    return generateSinglePropsMethod(method, model);
  }
  
  private MethodSpec generateSinglePropsMethod(SqlMethod method, TableMetamodel model) {
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
  
  private MethodSpec generateBatchMethod(SqlMethod method, TableMetamodel model) {
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
  
  private void generateSqlReturnBody(MethodSpec.Builder builder, SqlMethod method, TableMetamodel model, RegistryMetamodel registry) {
    if (method.getSqlBuilderClassName() != null && method.getParameters().size() == 1) {
      final var param = method.getParameters().get(0);
      
      builder.addStatement("final var sqlBuilder = new $T()", 
        ClassName.bestGuess(method.getSqlBuilderClassName()));
      builder.addStatement("final var baseSql = sqlBuilder.apply(dataSource.getTenant(), $L, $L)", 
        resolveSqlPlaceholders(method.getSqlTemplate(), method.getTableNames()), param.getName());
      builder.addCode("\n");
      builder.addStatement("var sqlValue = baseSql.getValue()");
      
      // Replace ALL tables from registry (sorted by order) for sqlBuilder methods
      final var tablesOrderedByOrder = metamodel.getTablesForRegistry(registry) 
        .stream()
        .sorted((a, b) -> Integer.compare(a.getOrder(), b.getOrder()))
        .collect(java.util.stream.Collectors.toList());
      
      for (final var table : tablesOrderedByOrder) {
        final var tableName = table.getTableName();
        final var getterName = NamingUtils.toCamelCaseCapitalized(tableName);
        builder.addStatement("sqlValue = sqlValue.replaceAll(\"(?i)\\\\{$L\\\\}\", tables.get$L())",
          tableName, getterName);
      }
      
      builder.addCode("\n");
      builder.addStatement("return $T.builder().value(sqlValue).props(baseSql.getProps()).rowMapper(new $T()).build()",
        ClassName.get(ImmutableSqlTuple.class),
        ClassName.bestGuess(method.getMapperClassName()));
    } else {
      final var resolvedSql = resolveSqlPlaceholders(method.getSqlTemplate(), method.getTableNames());
      
      if (method.getPropsType() == SqlPropsType.SQL) {
        builder.addStatement("return $T.builder().value($L).rowMapper(new $T()).build()",
          ClassName.get(ImmutableSql.class),
          resolvedSql,
          ClassName.bestGuess(method.getMapperClassName()));
      } else {
        final var propsArgs = method.getParameters().stream()
          .map(MethodParameter::getName)
          .collect(Collectors.joining(", "));
        

        
        builder.addStatement("return $T.builder().value($L).props($T.from($T.asList($L))).rowMapper(new $T()).build()",
          ClassName.get(ImmutableSqlTuple.class),
          resolvedSql,
          ClassName.get(io.vertx.mutiny.sqlclient.Tuple.class),
          ClassName.get(Arrays.class),
          propsArgs,
          ClassName.bestGuess(method.getMapperClassName()));
      }
    }
  }
  
  private void generateExecutionBody(MethodSpec.Builder builder, SqlMethod method, TableMetamodel model) {
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
    // Preserve line breaks where -- comments exist, collapse other whitespace
    final var normalizedSql = sql
        .replaceAll("--[^\n]*", "$0\n")  // Ensure -- comments end with newline
        .replaceAll("[ \t]+", " ")        // Collapse horizontal whitespace only
        .replaceAll("\n+", "\n")          // Collapse multiple newlines to single
        .trim()
        .replace("\"", "\\\"")
        .replace("\n", "\\n");            // Escape newlines for Java string literal

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
        final var getterName = NamingUtils.toCamelCaseCapitalized(tableName);
        current = String.join("\" + tables.get" + getterName + "() + \"", parts);
      }
    }

    result.append(current).append("\"");
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