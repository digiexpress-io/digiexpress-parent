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
import java.util.List;

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import io.resys.thena.api.LogConstants;
import io.resys.thena.api.annotations.TenantSql;
import io.resys.thena.datasource.ThenaSqlDataSource;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler.SqlFailed;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler.SqlTupleFailed;
import io.resys.thena.processor.model.Metamodel;
import io.resys.thena.processor.model.RegistryMetamodel;
import io.resys.thena.processor.model.TableMetamodel;
import io.resys.thena.processor.model.TableMetamodel.SqlMethodType;
import io.resys.thena.processor.spi.MultiTableCodeGenerator;
import io.resys.thena.processor.support.NamingUtils;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.RowSet;

public class Gen_Multi_QueryImplementation implements MultiTableCodeGenerator {
  
  public JavaFile generate(RegistryMetamodel registry, List<TableMetamodel> tables, Metamodel metamodel) {
    final var className = registry.getName() + "DbQueryImpl";
    final var interfaceName = registry.getName() + "DbQuery";
    final var packageName = registry.getPackageName() + ".spi";
    
    final var classBuilder = TypeSpec.classBuilder(className)
      .addModifiers(Modifier.PUBLIC)
      .addSuperinterface(ClassName.get(registry.getPackageName(), interfaceName))
      .addAnnotation(AnnotationSpec.builder(ClassName.get("lombok.extern.slf4j", "Slf4j"))
        .addMember("topic", "$T.SHOW_SQL", ClassName.get(LogConstants.class))
        .build())
      .addAnnotation(ClassName.get("lombok", "RequiredArgsConstructor"));
    
    classBuilder.addField(generateMetricsLoggerField(registry));

    classBuilder.addField(FieldSpec.builder(
      ClassName.get(ThenaSqlDataSource.class),
      "dataSource",
      Modifier.PRIVATE, Modifier.FINAL
    ).build());
    
    classBuilder.addField(FieldSpec.builder(
      ClassName.bestGuess(registry.getRegistryClassName()),
      "registry",
      Modifier.PRIVATE, Modifier.FINAL
    ).build());
    
    classBuilder.addField(FieldSpec.builder(
      ClassName.get(ThenaSqlDataSourceErrorHandler.class),
      "errorHandler",
      Modifier.PRIVATE, Modifier.FINAL
    ).build());
    
    classBuilder.addMethod(generateConstructor(registry));
    classBuilder.addMethod(generateFindAllMethod(registry, tables));
    
    for (final var table : tables) {
      classBuilder.addMethod(generateQueryTableMethod(registry, table));
    }
    
    return JavaFile.builder(packageName, classBuilder.build())
      .indent("  ")
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

  private MethodSpec generateConstructor(RegistryMetamodel registry) {
    return MethodSpec.constructorBuilder()
      .addModifiers(Modifier.PUBLIC)
      .addParameter(ClassName.get(ThenaSqlDataSource.class), "dataSource")
      .addStatement("this.dataSource = dataSource")
      .addStatement("final var names = $T.defaults().toRepo(dataSource.getTenant())", 
        ClassName.bestGuess(registry.getTableClassName()))
      .addStatement("this.registry = new $T(names, dataSource)", 
        ClassName.bestGuess(registry.getRegistryClassName()))
      .addStatement("this.errorHandler = dataSource.getErrorHandler()")
      .build();
  }
  
  private MethodSpec generateFindAllMethod(RegistryMetamodel registry, List<TableMetamodel> tables) {
    final var method = MethodSpec.methodBuilder("findAll")
      .addAnnotation(Override.class)
      .addAnnotation(AnnotationSpec.builder(SuppressWarnings.class).addMember("value", "$S", "unchecked").build())
      .addModifiers(Modifier.PUBLIC)
      
      .returns(ParameterizedTypeName.get(
        ClassName.get(Uni.class),
        ClassName.bestGuess(registry.getWorldName())
      ));
    
    final var tablesWithFindAll = new ArrayList<TableMetamodel>();
    for (final var table : tables) {
      if (findEntityTypeForTable(table) != null) {
        tablesWithFindAll.add(table);
      }
    }
    
    if (tablesWithFindAll.isEmpty()) {
      method.addStatement("return $T.createFrom().item($T.builder().build())", 
        Uni.class, 
        ClassName.get(registry.getPackageName(), "Immutable" + registry.getWorldName()));
      return method.build();
    }
    
    method.addCode("return $T.combine().all()\n", Uni.class);
    method.addCode("$>.unis(\n");
    method.addCode("$>");
    
    for (int i = 0; i < tablesWithFindAll.size(); i++) {
      final var table = tablesWithFindAll.get(i);
      final var methodName = "query" + NamingUtils.toPascalCase(table.getTableName());
      final var findAllMethod = findNoArgFindAllMethod(table);
      
      if (i > 0) {
        method.addCode(",\n");
      }
      
      if(findAllMethod.isMultiWrapper()) {
        method.addCode("$L().$L().collect().asList()", methodName, findAllMethod.getMethodName());
      } else {
        method.addCode("$L().$L()", methodName, findAllMethod.getMethodName());  
      }
      
    }
    
    method.addCode("\n$<");
    method.addCode(")\n$<");
    method.addCode(".with(sets -> {\n");
    method.addCode("$>final var builder = $T.builder();\n", 
      ClassName.get(registry.getPackageName(), "Immutable" + registry.getWorldName()));
    
    for (int i = 0; i < tablesWithFindAll.size(); i++) {
      final var table = tablesWithFindAll.get(i);
      final var entityType = findEntityTypeForTable(table);
      
      method.addCode("final $T<$T> item_$L = ($T<$T>) sets.get($L);\n",
        List.class, entityType,
        i,
        List.class, entityType,
        i);
    }
    
    method.addCode("\n");
    
    for (int i = 0; i < tablesWithFindAll.size(); i++) {
      final var table = tablesWithFindAll.get(i);
      final var builderMethodName = NamingUtils.lowerCamelCase(NamingUtils.toPascalCase(table.getTableName()));
      
      method.addCode("builder.$L(item_$L\n", builderMethodName, i);
      method.addCode("$>.stream()\n");
      method.addCode(".collect($T.toMap(\n", ClassName.get("java.util.stream", "Collectors"));
      method.addCode("$>e -> e.getId().toString(),\n");
      method.addCode("e -> e\n$<");
      method.addCode(")));$<\n");
      
      if (i < tablesWithFindAll.size() - 1) {
        method.addCode("\n");
      }
    }
    
    method.addCode("\nreturn builder.build();\n$<");
    method.addCode("});\n");
    
    return method.build();
  }
  
  private ClassName findEntityTypeForTable(TableMetamodel table) {
    for (final var method : table.getSqlMethods()) {
      if (method.getType() == SqlMethodType.SELECT_ALL && method.getParameters().isEmpty()) {
        if (method.getReturnType() != null) {
          return (ClassName) method.getReturnType();
        }
      }
    }
    return null;
  }
  
  private TableMetamodel.SqlMethod findNoArgFindAllMethod(TableMetamodel table) {
    for (final var method : table.getSqlMethods()) {
      if (method.getType() == SqlMethodType.SELECT_ALL && method.getParameters().isEmpty()) {
        return method;
      }
    }
    return null;
  }
  
  private MethodSpec generateQueryTableMethod(RegistryMetamodel registry, TableMetamodel table) {
    final var methodName = "query" + NamingUtils.toPascalCase(table.getTableName());
    final var nestedInterfaceName = NamingUtils.toPascalCase(table.getTableName()) + "Query";
    final var registryGetter = NamingUtils.pluralize(table.getTableName());
    
    final var method = MethodSpec.methodBuilder(methodName)
      .addAnnotation(Override.class)
      .addModifiers(Modifier.PUBLIC)
      .returns(ClassName.bestGuess(nestedInterfaceName));
    
    method.addCode("return new $L() {\n", nestedInterfaceName);
    method.addCode("$>");
    
    for (final var sqlMethod : table.getSqlMethods()) {
      if (sqlMethod.getType() == SqlMethodType.SELECT || sqlMethod.getType() == SqlMethodType.SELECT_ALL) {
        method.addCode("\n");
        method.addCode(generateQueryMethodImpl(registry, table, sqlMethod, registryGetter));
      }
    }
    
    method.addCode("$<");
    method.addStatement("}");
    
    return method.build();
  }
  
  private CodeBlock generateQueryMethodImpl(
      RegistryMetamodel registry, 
      TableMetamodel table,
      TableMetamodel.SqlMethod sqlMethod,
      String registryGetter) {
    
    final var code = CodeBlock.builder();
    final var className = registry.getName() + "DbQueryImpl";
    final var entityType = sqlMethod.getReturnType();
    
    code.add("@$T\n", Override.class);
    code.add("public ");
    
    if (sqlMethod.getType() == SqlMethodType.SELECT && sqlMethod.isOptional()) {
      code.add("$T<$T<$T>> ", Uni.class, java.util.Optional.class, entityType);
    } else if (sqlMethod.getType() == SqlMethodType.SELECT && !sqlMethod.isOptional()) {
      code.add("$T<$T> ", Uni.class, entityType);
    } else if (sqlMethod.getType() == SqlMethodType.SELECT_ALL && sqlMethod.isMultiWrapper()) {
      code.add("$T<$T> ", Multi.class, entityType);
    } else {
      code.add("$T<$T<$T>> ", Uni.class, List.class, entityType);
    }
    
    code.add("$L(", sqlMethod.getMethodName());
    
    for (int i = 0; i < sqlMethod.getParameters().size(); i++) {
      final var param = sqlMethod.getParameters().get(i);
      if (i > 0) code.add(", ");
      code.add("$T $L", param.getType(), param.getName());
    }
    
    code.add(") {\n");
    code.indent();
    
    code.add("final var sql = registry.$L().$L(", registryGetter, sqlMethod.getMethodName());
    for (int i = 0; i < sqlMethod.getParameters().size(); i++) {
      if (i > 0) code.add(", ");
      code.add("$L", sqlMethod.getParameters().get(i).getName());
    }
    code.add(");\n");
    
    code.add("\n");
    code.add("if(log.isDebugEnabled()) {\n");
    code.indent();
    if (sqlMethod.getParameters().isEmpty()) {
      code.add("log.debug(\"$L.$L.$L query, with props: {} \\r\\n{}\",\n", 
        className, "query" + NamingUtils.toPascalCase(table.getTableName()), sqlMethod.getMethodName());
      code.indent();
      code.add("\"\",\n");
    } else {
      code.add("log.debug(\"$L.$L.$L query, with props: {} \\r\\n{}\",\n",
        className, "query" + NamingUtils.toPascalCase(table.getTableName()), sqlMethod.getMethodName());
      code.indent();
      code.add("sql.getProps().deepToString(),\n");
    }
    code.add("sql.getValue());\n");
    code.unindent();
    code.unindent();
    code.add("}\n\n");

    final var logLabel = className + "." + "query" + NamingUtils.toPascalCase(table.getTableName()) + "." + sqlMethod.getMethodName();
    final var propsExpr = sqlMethod.getParameters().isEmpty() ? "\"\"" : "sql.getProps().deepToString()";

    code.add("final var metricsEnabled = metricsLog.isDebugEnabled();\n");
    code.add("final var metricsStart = metricsEnabled ? $T.nanoTime() : 0L;\n", System.class);
    code.add("final var metricsMessage = metricsEnabled ? ($S + \" query, props=\" + $L + \" sql=\" + sql.getValue()) : null;\n",
      logLabel, propsExpr);
    code.add("\n");

    code.add("@$T(\"unchecked\")\n", SuppressWarnings.class);
    code.add("final $T<$T> mapper = ($T<$T>) sql.getRowMapper();\n",
      ClassName.get(TenantSql.RowMapper.class),
      entityType,
      ClassName.get(TenantSql.RowMapper.class),
      entityType);
    code.add("\n");
    code.add("return dataSource.getClient().preparedQuery(sql.getValue())\n");
    code.indent();
    code.add(".mapping(mapper::apply)\n");
    
    if (sqlMethod.getParameters().isEmpty()) {
      code.add(".execute()\n");
    } else {
      code.add(".execute(sql.getProps())\n");
    }
    
    code.add(".onItem()\n");
    
    if (sqlMethod.getType() == SqlMethodType.SELECT && sqlMethod.isOptional()) {
      code.add(".transform(($T<$T> rowset) -> {\n", RowSet.class, entityType);
      code.indent();
      code.add("if (metricsEnabled) {\n");
      code.indent();
      code.add("final var tookMillis = ($T.nanoTime() - metricsStart) / 1_000_000;\n", System.class);
      code.add("metricsLog.debug(\"{} | took={}ms rows={}\", metricsMessage, tookMillis, rowset.rowCount());\n");
      code.unindent();
      code.add("}\n");
      code.add("final var iterator = rowset.iterator();\n");
      code.add("if(iterator.hasNext()) {\n");
      code.indent();
      code.add("return $T.of(iterator.next());\n", java.util.Optional.class);
      code.unindent();
      code.add("}\n");
      code.add("return $T.<$T>empty();\n", java.util.Optional.class, entityType);
      code.unindent();
      code.add("})\n");
    } else if (sqlMethod.getType() == SqlMethodType.SELECT && !sqlMethod.isOptional()) {
      code.add(".transform(($T<$T> rowset) -> {\n", RowSet.class, entityType);
      code.indent();
      code.add("if (metricsEnabled) {\n");
      code.indent();
      code.add("final var tookMillis = ($T.nanoTime() - metricsStart) / 1_000_000;\n", System.class);
      code.add("metricsLog.debug(\"{} | took={}ms rows={}\", metricsMessage, tookMillis, rowset.rowCount());\n");
      code.unindent();
      code.add("}\n");
      code.add("final var iterator = rowset.iterator();\n");
      code.add("if(iterator.hasNext()) {\n");
      code.indent();
      code.add("return iterator.next();\n");
      code.unindent();
      code.add("}\n");
      code.add("throw new $T(\"$L not found!\");\n", 
        ClassName.get(registry.getPackageName() + ".spi", registry.getName() + "FindException"),
        entityType.toString());
      code.unindent();
      code.add("})\n");
    } else if (sqlMethod.getType() == SqlMethodType.SELECT_ALL && sqlMethod.isMultiWrapper()) {
      code.add(".transformToMulti(($T<$T> rowset) -> {\n", RowSet.class, entityType);
      code.indent();
      code.add("if (metricsEnabled) {\n");
      code.indent();
      code.add("final var tookMillis = ($T.nanoTime() - metricsStart) / 1_000_000;\n", System.class);
      code.add("metricsLog.debug(\"{} | took={}ms rows={}\", metricsMessage, tookMillis, rowset.rowCount());\n");
      code.unindent();
      code.add("}\n");
      code.add("return $T.createFrom().iterable(rowset);\n", Multi.class);
      code.unindent();
      code.add("})\n");
    } else {
      code.add(".transformToUni(($T<$T> rowset) -> {\n", RowSet.class, entityType);
      code.indent();
      code.add("if (metricsEnabled) {\n");
      code.indent();
      code.add("final var tookMillis = ($T.nanoTime() - metricsStart) / 1_000_000;\n", System.class);
      code.add("metricsLog.debug(\"{} | took={}ms rows={}\", metricsMessage, tookMillis, rowset.rowCount());\n");
      code.unindent();
      code.add("}\n");
      code.add("return $T.createFrom().iterable(rowset).collect().asList();\n", Multi.class);
      code.unindent();
      code.add("})\n");
    }
    
    if (sqlMethod.getParameters().isEmpty()) {
      code.add(".onFailure().invoke(e -> errorHandler.deadEnd(new $T(\"Can't find '$L's!\", sql, e)));\n",
        SqlFailed.class, table.getTableName().toUpperCase());
    } else {
      code.add(".onFailure().invoke(e -> errorHandler.deadEnd(new $T(\"Can't find '$L's!\", sql, e)));\n",
        SqlTupleFailed.class, table.getTableName().toUpperCase());
    }
    
    code.unindent();
    code.unindent();
    code.add("}\n");
    
    return code.build();
  }
  

  

}
