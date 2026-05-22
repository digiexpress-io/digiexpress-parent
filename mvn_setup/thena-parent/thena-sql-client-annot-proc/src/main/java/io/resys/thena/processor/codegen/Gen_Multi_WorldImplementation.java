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
import java.util.Map;

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import io.resys.thena.processor.model.Metamodel;
import io.resys.thena.processor.model.RegistryMetamodel;
import io.resys.thena.processor.model.TableMetamodel;
import io.resys.thena.processor.model.TableMetamodel.SqlMethodType;
import io.resys.thena.processor.spi.MultiTableCodeGenerator;
import io.resys.thena.processor.support.NamingUtils;

public class Gen_Multi_WorldImplementation implements MultiTableCodeGenerator {
  
  public JavaFile generate(RegistryMetamodel registry, List<TableMetamodel> tables, Metamodel metamodel) {
    final var className = "Immutable" + registry.getWorldName();
    
    final var classBuilder = TypeSpec.classBuilder(className)
      .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
      .addSuperinterface(ClassName.get(registry.getPackageName(), 
        registry.getName() + "DbQuery." + registry.getWorldName()));
    
    // Add fields for each table
    for (final var table : tables) {
      final var entityType = findEntityTypeForTable(table);
      if (entityType != null) {
        final var fieldName = NamingUtils.toCamelCase(table.getTableName());
        final var fieldType = ParameterizedTypeName.get(
          ClassName.get(Map.class),
          ClassName.get(String.class),
          entityType
        );
        classBuilder.addField(FieldSpec.builder(fieldType, fieldName)
          .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
          .build());
      }
    }
    
    // Generate private constructor
    classBuilder.addMethod(generateConstructor(tables));
    
    // Generate getters
    for (final var table : tables) {
      final var entityType = findEntityTypeForTable(table);
      if (entityType != null) {
        classBuilder.addMethod(generateGetter(table, entityType));
      }
    }
    
    // Generate static builder class
    classBuilder.addType(generateBuilderClass(registry, tables));
    
    // Generate static builder() method
    classBuilder.addMethod(MethodSpec.methodBuilder("builder")
      .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
      .returns(ClassName.bestGuess("Builder"))
      .addStatement("return new Builder()")
      .build());
    
    return JavaFile.builder(registry.getPackageName(), classBuilder.build())
      .indent("  ")
      .build();
  }
  
  private MethodSpec generateConstructor(List<TableMetamodel> tables) {
    final var builder = MethodSpec.constructorBuilder()
      .addModifiers(Modifier.PRIVATE);
    
    for (final var table : tables) {
      final var entityType = findEntityTypeForTable(table);
      if (entityType != null) {
        final var fieldName = NamingUtils.toCamelCase(table.getTableName());
        final var paramType = ParameterizedTypeName.get(
          ClassName.get(Map.class),
          ClassName.get(String.class),
          entityType
        );
        builder.addParameter(paramType, fieldName);
        builder.addStatement("this.$L = $L", fieldName, fieldName);
      }
    }
    
    return builder.build();
  }
  
  private MethodSpec generateGetter(TableMetamodel table, ClassName entityType) {
    final var getterName = "get" + NamingUtils.toPascalCase(table.getTableName());
    final var fieldName = NamingUtils.toCamelCase(table.getTableName());
    final var returnType = ParameterizedTypeName.get(
      ClassName.get(Map.class),
      ClassName.get(String.class),
      entityType
    );
    
    return MethodSpec.methodBuilder(getterName)
      .addModifiers(Modifier.PUBLIC)
      .addAnnotation(Override.class)
      .returns(returnType)
      .addStatement("return $L", fieldName)
      .build();
  }
  
  private TypeSpec generateBuilderClass(RegistryMetamodel registry, List<TableMetamodel> tables) {
    final var builderClass = TypeSpec.classBuilder("Builder")
      .addModifiers(Modifier.PUBLIC, Modifier.STATIC);
    
    final var immutableMapBuilder = ClassName.get("com.google.common.collect", "ImmutableMap", "Builder");
    
    // Add fields for each table
    for (final var table : tables) {
      final var entityType = findEntityTypeForTable(table);
      if (entityType != null) {
        final var fieldName = NamingUtils.toCamelCase(table.getTableName());
        final var fieldType = ParameterizedTypeName.get(
          immutableMapBuilder,
          ClassName.get(String.class),
          entityType
        );
        builderClass.addField(FieldSpec.builder(fieldType, fieldName)
          .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
          .initializer("$T.builder()", ClassName.get("com.google.common.collect", "ImmutableMap"))
          .build());
      }
    }
    
    // Generate from() method
    builderClass.addMethod(generateFromMethod(registry, tables));
    
    // Generate setter methods for each table
    for (final var table : tables) {
      final var entityType = findEntityTypeForTable(table);
      if (entityType != null) {
        builderClass.addMethod(generateBuilderSetter(table, entityType));
        builderClass.addMethod(generateBuilderPut(table, entityType));
        builderClass.addMethod(generateBuilderPutAll(table, entityType));
      }
    }
    
    // Generate build() method
    builderClass.addMethod(generateBuildMethod(registry, tables));
    
    return builderClass.build();
  }
  
  private MethodSpec generateFromMethod(RegistryMetamodel registry, List<TableMetamodel> tables) {
    final var worldInterfaceName = registry.getName() + "DbQuery." + registry.getWorldName();
    
    final var method = MethodSpec.methodBuilder("from")
      .addModifiers(Modifier.PUBLIC)
      .addParameter(ClassName.get(registry.getPackageName(), worldInterfaceName), "source")
      .returns(ClassName.bestGuess("Builder"));
    
    for (final var table : tables) {
      final var entityType = findEntityTypeForTable(table);
      if (entityType != null) {
        final var fieldName = NamingUtils.toCamelCase(table.getTableName());
        final var getterName = "get" + NamingUtils.toPascalCase(table.getTableName());
        method.addStatement("this.$L.putAll(source.$L())", fieldName, getterName);
      }
    }
    
    method.addStatement("return this");
    
    return method.build();
  }
  
  private MethodSpec generateBuilderSetter(TableMetamodel table, ClassName entityType) {
    final var setterName = NamingUtils.toCamelCase(table.getTableName());
    final var fieldName = NamingUtils.toCamelCase(table.getTableName());
    final var paramType = ParameterizedTypeName.get(
      ClassName.get(Map.class),
      ClassName.get(String.class),
      entityType
    );
    
    return MethodSpec.methodBuilder(setterName)
      .addModifiers(Modifier.PUBLIC)
      .addParameter(paramType, "value")
      .returns(ClassName.bestGuess("Builder"))
      .addStatement("this.$L.putAll(value)", fieldName)
      .addStatement("return this")
      .build();
  }
  
  private MethodSpec generateBuilderPut(TableMetamodel table, ClassName entityType) {
    // Convert plural table name to singular for method name
    final var tableName = table.getTableName();
    final var singularName = tableName.endsWith("ies") ? tableName.substring(0, tableName.length() - 3) + "y" :
                            tableName.endsWith("es") ? tableName.substring(0, tableName.length() - 2) :
                            tableName.endsWith("s") ? tableName.substring(0, tableName.length() - 1) : 
                            tableName;
    final var methodName = "put" + NamingUtils.toPascalCase(singularName);
    final var fieldName = NamingUtils.toCamelCase(table.getTableName());
    
    return MethodSpec.methodBuilder(methodName)
      .addModifiers(Modifier.PUBLIC)
      .addParameter(ClassName.get(String.class), "key")
      .addParameter(entityType, "value")
      .returns(ClassName.bestGuess("Builder"))
      .addStatement("this.$L.put(key, value)", fieldName)
      .addStatement("return this")
      .build();
  }
  
  private MethodSpec generateBuilderPutAll(TableMetamodel table, ClassName entityType) {
    final var methodName = "putAll" + NamingUtils.toPascalCase(table.getTableName());
    final var fieldName = NamingUtils.toCamelCase(table.getTableName());
    final var paramType = ParameterizedTypeName.get(
      ClassName.get(Map.class),
      ClassName.get(String.class),
      entityType
    );
    
    return MethodSpec.methodBuilder(methodName)
      .addModifiers(Modifier.PUBLIC)
      .addParameter(paramType, "values")
      .returns(ClassName.bestGuess("Builder"))
      .addStatement("this.$L.putAll(values)", fieldName)
      .addStatement("return this")
      .build();
  }
  
  private MethodSpec generateBuildMethod(RegistryMetamodel registry, List<TableMetamodel> tables) {
    final var className = "Immutable" + registry.getWorldName();
    final var method = MethodSpec.methodBuilder("build")
      .addModifiers(Modifier.PUBLIC)
      .returns(ClassName.bestGuess(className));
    
    final var params = new StringBuilder();
    boolean first = true;
    for (final var table : tables) {
      final var entityType = findEntityTypeForTable(table);
      if (entityType != null) {
        if (!first) params.append(", ");
        params.append(NamingUtils.toCamelCase(table.getTableName())).append(".build()");
        first = false;
      }
    }
    
    method.addStatement("return new $L($L)", className, params.toString());
    
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
}