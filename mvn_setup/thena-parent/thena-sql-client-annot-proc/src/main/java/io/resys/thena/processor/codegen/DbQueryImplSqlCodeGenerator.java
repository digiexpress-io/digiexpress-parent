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
import io.resys.thena.processor.model.TableModel;
import io.resys.thena.processor.model.TableModel.RegistryModel;
import io.resys.thena.processor.model.TableModel.SqlMethodType;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.RowSet;

public class DbQueryImplSqlCodeGenerator {
  
  public JavaFile generate(RegistryModel registry, List<TableModel> tables) {
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
    classBuilder.addMethod(generateFindAllMethod(registry));
    
    for (final var table : tables) {
      classBuilder.addMethod(generateQueryTableMethod(registry, table));
    }
    
    return JavaFile.builder(packageName, classBuilder.build())
      .indent("  ")
      .build();
  }
  
  private MethodSpec generateConstructor(RegistryModel registry) {
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
  
  private MethodSpec generateFindAllMethod(RegistryModel registry) {
    return MethodSpec.methodBuilder("findAll")
      .addAnnotation(Override.class)
      .addModifiers(Modifier.PUBLIC)
      .returns(ParameterizedTypeName.get(
        ClassName.get(Uni.class),
        ClassName.bestGuess(registry.getWorldName())
      ))
      .addComment("TODO Auto-generated method stub")
      .addStatement("return null")
      .build();
  }
  
  private MethodSpec generateQueryTableMethod(RegistryModel registry, TableModel table) {
    final var methodName = "query" + toPascalCase(table.getTableName());
    final var nestedInterfaceName = toPascalCase(table.getTableName()) + "Query";
    final var registryGetter = RegistryFactorySqlCodeGenerator.pluralize(table.getTableName());
    
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
      RegistryModel registry, 
      TableModel table,
      TableModel.SqlMethod sqlMethod,
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
        className, "query" + toPascalCase(table.getTableName()), sqlMethod.getMethodName());
      code.indent();
      code.add("\"\",\n");
    } else {
      code.add("log.debug(\"$L.$L.$L query, with props: {} \\r\\n{}\",\n",
        className, "query" + toPascalCase(table.getTableName()), sqlMethod.getMethodName());
      code.indent();
      code.add("sql.getProps().deepToString(),\n");
    }
    code.add("sql.getValue());\n");
    code.unindent();
    code.unindent();
    code.add("}\n\n");
    
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
      code.add(".transformToMulti(($T<$T> rowset) -> $T.createFrom().iterable(rowset))\n",
        RowSet.class, entityType, Multi.class);
    } else {
      code.add(".transformToUni(($T<$T> rowset) -> $T.createFrom().iterable(rowset).collect().asList())\n",
        RowSet.class, entityType, Multi.class);
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
  
  private String toPascalCase(String snakeCase) {
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
}
