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

import java.util.Comparator;
import java.util.List;

import javax.lang.model.element.Modifier;

import org.apache.commons.lang3.StringUtils;

import com.github.vertical_blank.sqlformatter.SqlFormatter;
import com.github.vertical_blank.sqlformatter.languages.Dialect;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import io.resys.thena.api.LogConstants;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.api.entities.Tenant.StructureType;
import io.resys.thena.datasource.ThenaSqlDataSource;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler.SqlFailed;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler.SqlSchemaFailed;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler.SqlTupleFailed;
import io.resys.thena.processor.model.RegistryMetamodel;
import io.resys.thena.processor.model.TableMetamodel;
import io.resys.thena.processor.spi.MultiTableCodeGenerator;
import io.resys.thena.processor.support.NamingUtils;
import io.resys.thena.spi.InternalTenantQueryImpl;
import io.resys.thena.spi.TenantDataSource.InternalTenantQuery;
import io.resys.thena.spi.TenantRegistrySqlImpl;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;

public class Gen_Multi_InternalTenantQuery implements MultiTableCodeGenerator {
  
  public JavaFile generate(RegistryMetamodel registry, List<TableMetamodel> tables) {
    final var className = registry.getInternalTenantQueryClassName();
    final var packageName = registry.getPackageName() + ".spi";
    
    final var sortedTables = tables.stream()
      .sorted(Comparator.comparingInt(TableMetamodel::getOrder))
      .toList();
    
    final var classBuilder = TypeSpec.classBuilder(className)
      .addModifiers(Modifier.PUBLIC)
      .superclass(ClassName.get(InternalTenantQueryImpl.class))
      .addSuperinterface(ClassName.get(InternalTenantQuery.class))
      .addAnnotation(AnnotationSpec.builder(ClassName.get("lombok.extern.slf4j", "Slf4j"))
        .addMember("topic", "$S", LogConstants.SHOW_SQL)
        .build());
    
    classBuilder.addMethod(generateConstructor());
    classBuilder.addMethod(generateInsert(registry, sortedTables));
    classBuilder.addMethod(generateDelete(registry, sortedTables));
    
    return JavaFile.builder(packageName, classBuilder.build())
      .indent("  ")
      .build();
  }
  
  private MethodSpec generateConstructor() {
    return MethodSpec.constructorBuilder()
      .addModifiers(Modifier.PUBLIC)
      .addParameter(ClassName.get(ThenaSqlDataSource.class), "dataSource")
      .addStatement("super(dataSource)")
      .build();
  }
  
  private MethodSpec generateInsert(RegistryMetamodel registry, List<TableMetamodel> sortedTables) {
    return MethodSpec.methodBuilder("insert")
      .addAnnotation(Override.class)
      .addModifiers(Modifier.PUBLIC)
      .addParameter(ClassName.get(Tenant.class), "newRepo")
      .returns(ParameterizedTypeName.get(
        ClassName.get(Uni.class),
        ClassName.get(Tenant.class)
      ))
      .addCode(generateInsertBody(registry, sortedTables))
      .build();
  }
  
  private CodeBlock generateInsertBody(RegistryMetamodel registry, List<TableMetamodel> sortedTables) {
    final var code = CodeBlock.builder();
    //     final var registry = new Batch2Registry(Batch2TableNames.defaults().toRepo(newRepo), next);
    
    code.add("final var names = $T.defaults().toRepo(newRepo);\n", ClassName.bestGuess(registry.getTableClassName()));
    code.add("final var next = dataSource.withTenant(newRepo);\n");
    code.add("final var registry = new $T(names, next);\n", 
      ClassName.get(registry.getPackageName() + ".spi", registry.getRegistryClassName()));
    
    code.add("final var pool = next.getPool();\n\n");
    
    code.add("return pool.withTransaction(tx -> {\n");
    code.indent();
    
    if(!registry.isTenantDisabled()) {
      code.add("final var sqlQuery = new $T(next.getTenantContext());\n", 
          ClassName.get(TenantRegistrySqlImpl.class));
      code.add("final var tenantInsert = sqlQuery.insertOne(newRepo);\n");
    }
    
    code.add("final var tablesCreate = new $T();\n", StringBuilder.class);
    
    if(!registry.isTenantDisabled()) {
    
      code.add("$T.isTrue(newRepo.getType() == $T.$L, () -> \"Tenant type must be $L\");\n\n",
        ClassName.get(RepoAssert.class),
        ClassName.get(StructureType.class),
        registry.getTenantType(),
        registry.getTenantType());
    }
    
    code.add("tablesCreate\n");
    code.indent();
    for (final var table : sortedTables) {
      final var getter = NamingUtils.pluralize(table.getTableName());
      code.add(".append(registry.$L().createTable().getValue())\n", getter);
    }
    code.add("\n");
    for (final var table : sortedTables) {
      if(StringUtils.isEmpty(table.getConstraintsSql().trim())) {
        continue;
      }
      final var getter = NamingUtils.pluralize(table.getTableName());
      code.add(".append(registry.$L().createConstraints().getValue())\n", getter);
    }
    code.add(".toString();\n");
    code.unindent();
    
    code.add("\n");
    code.add("if(log.isDebugEnabled()) {\n");
    code.indent();
    code.add("log.debug(new $T(\"Creating schema: \")\n", StringBuilder.class);
    code.indent();
    code.add(".append($T.lineSeparator())\n", System.class);
    
    code.add(".append($T.of($T.PostgreSql).format(tablesCreate.toString()))\n", 
        SqlFormatter.class, Dialect.class);
    
    code.add(".toString());\n");
    code.unindent();
    code.unindent();
    code.add("}\n\n");
    
    if(!registry.isTenantDisabled()) {
      code.add("final $T<Void> create = getClient().query(sqlQuery.createTable().getValue()).execute()\n", 
        ClassName.get(Uni.class));
      code.indent();
      code.add(".onItem().transformToUni(data -> $T.createFrom().voidItem())\n", 
        ClassName.get(Uni.class));
      code.add(".onFailure().invoke(e -> next.getErrorHandler().deadEnd(new $T(\"Can't create table 'TENANT'!\", sqlQuery.createTable(), e)));\n",
        ClassName.get(SqlFailed.class));
      code.unindent();
      
      code.add("\n");
    
      code.add("final $T<Void> insert = tx.preparedQuery(tenantInsert.getValue()).execute(tenantInsert.getProps())\n",
        ClassName.get(Uni.class));
      code.indent();
      code.add(".onItem().transformToUni(rowSet -> $T.createFrom().voidItem())\n",
        ClassName.get(Uni.class));
      code.add(".onFailure().invoke(e -> next.getErrorHandler().deadEnd(new $T(\"Can't insert into 'TENANT'!\", tenantInsert, e)));\n",
        ClassName.get(SqlTupleFailed.class));
      code.unindent();
    }
    
    code.add("final $T<Void> nested = tx.query(tablesCreate.toString()).execute()\n",
      ClassName.get(Uni.class));
    code.indent();
    code.add(".onItem().transformToUni(rowSet -> $T.createFrom().voidItem())\n",
      ClassName.get(Uni.class));
    code.add(".onFailure().invoke(e -> next.getErrorHandler().deadEnd(new $T(\"Can't create tables!\", tablesCreate.toString(), e)));\n",
      ClassName.get(SqlSchemaFailed.class));
    code.unindent();
    
    code.add("\n");
    
    if(registry.isTenantDisabled()) {
      code.add("return nested\n");
      code.indent();      
    } else {
      code.add("return create\n");
      code.indent();
      code.add(".onItem().transformToUni((junk) -> insert)\n");
      code.add(".onItem().transformToUni((junk) -> nested)\n");

    }
    code.add(".onItem().transform(junk -> newRepo)\n");
    code.add(".onItem().invoke(newTenant -> this.dataSource.getTenantCache().setTenant(newTenant));\n");
    code.unindent();
    
    code.unindent();
    code.add("});\n");
    
    return code.build();
  }
  
  private MethodSpec generateDelete(RegistryMetamodel registry, List<TableMetamodel> sortedTables) {
    return MethodSpec.methodBuilder("delete")
      .addAnnotation(Override.class)
      .addModifiers(Modifier.PUBLIC)
      .addParameter(ClassName.get(Tenant.class), "newRepo")
      .returns(ParameterizedTypeName.get(
        ClassName.get(Uni.class),
        ClassName.get(Tenant.class)
      ))
      .addCode(generateDeleteBody(registry, sortedTables))
      .build();
  }
  
  private CodeBlock generateDeleteBody(RegistryMetamodel registry, List<TableMetamodel> sortedTables) {
    final var code = CodeBlock.builder();
    
    code.add("final var names = $T.defaults().toRepo(newRepo);\n", ClassName.bestGuess(registry.getTableClassName()));
    code.add("final var next = dataSource.withTenant(newRepo);\n");
    code.add("final var registry = new $T(names, next);\n", 
      ClassName.get(registry.getPackageName() + ".spi", registry.getRegistryClassName()));
    code.add("final var pool = next.getPool();\n\n");
    
    code.add("return pool.withTransaction(tx -> {\n");
    code.indent();
    
    code.add("final var tablesDrop = new $T();\n", StringBuilder.class);
    
    if(!registry.isTenantDisabled()) {
      code.add("$T.isTrue(newRepo.getType() == $T.$L, () -> \"Tenant type must be $L\");\n\n",
        ClassName.get(RepoAssert.class),
        ClassName.get(StructureType.class),
        registry.getTenantType(),
        registry.getTenantType());
      code.add("final var sqlQuery = new $T(next.getTenantContext());\n", 
          ClassName.get(TenantRegistrySqlImpl.class));
      code.add("final var tenantDelete = sqlQuery.deleteOne(newRepo);\n");
    }
    
    code.add("tablesDrop\n");
    code.indent();
    final var reversedTables = sortedTables.reversed();
    for (final var table : reversedTables) {
      final var getter = NamingUtils.pluralize(table.getTableName());
      code.add(".append(registry.$L().dropTable().getValue())\n", getter);
    }
    code.add(".toString();\n");
    code.unindent();
    
    code.add("\n");
    code.add("if(log.isDebugEnabled()) {\n");
    code.indent();
    
    if(!registry.isTenantDisabled()) {
      code.add("log.debug(\"Delete tenant by name query, with props: {} \\r\\n{}\",\n");
      code.indent();
      code.add("tenantDelete.getProps().deepToString(),\n");
      code.add("tenantDelete.getValue());\n");
      code.unindent();
    }
    
    code.add("\n");
    code.add("log.debug(new $T(\"Drop schema: \")\n", StringBuilder.class);
    code.indent();
    code.add(".append($T.lineSeparator())\n", System.class);
    code.add(".append(tablesDrop.toString())\n");
    code.add(".toString());\n");
    code.unindent();
    code.unindent();
    code.add("}\n\n");
    
    
    if(!registry.isTenantDisabled()) {
      code.add("final $T<Void> insert = tx.preparedQuery(tenantDelete.getValue()).execute(tenantDelete.getProps())\n",
        ClassName.get(Uni.class));
      code.indent();
      code.add(".onItem().transformToUni(rowSet -> $T.createFrom().voidItem())\n",
        ClassName.get(Uni.class));
      code.add(".onFailure().invoke(e -> next.getErrorHandler().deadEnd(new $T(\"Can't delete from 'TENANT'!\", tenantDelete, e)));\n",
        ClassName.get(SqlTupleFailed.class));
      code.unindent();
    }
    
    code.add("final $T<Void> nested = tx.query(tablesDrop.toString()).execute()\n",
      ClassName.get(Uni.class));
    code.indent();
    code.add(".onItem().transformToUni(rowSet -> $T.createFrom().voidItem())\n",
      ClassName.get(Uni.class));
    code.add(".onFailure().invoke(e -> next.getErrorHandler().deadEnd(new $T(\"Can't drop tables!\", tablesDrop.toString(), e)));\n",
      ClassName.get(SqlSchemaFailed.class));
    code.unindent();
    
    code.add("\n");
    
    if(registry.isTenantDisabled()) {
      code.add("return nested\n");
      code.indent();
    } else {
      code.add("return insert\n");
      code.indent();
      code.add(".onItem().transformToUni(junk -> nested)\n");      
    }
    
    code.add(".onItem().transform(junk -> newRepo)\n");
    code.add(".onItem().invoke(() -> this.dataSource.getTenantCache().invalidateAll());\n");
    code.unindent();
    
    code.unindent();
    code.add("});\n");
    
    return code.build();
  }
}
