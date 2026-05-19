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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import io.resys.thena.api.envelope.BatchStatus;
import io.resys.thena.api.envelope.Message;
import io.resys.thena.processor.model.Metamodel;
import io.resys.thena.processor.model.RegistryMetamodel;
import io.resys.thena.processor.model.TableMetamodel;
import io.resys.thena.processor.model.TableMetamodel.SqlMethod;
import io.resys.thena.processor.model.TableMetamodel.SqlMethodType;
import io.resys.thena.processor.spi.MultiTableCodeGenerator;
import io.resys.thena.processor.support.NamingUtils;

public class Gen_Multi_PersistenceUnitImplementation implements MultiTableCodeGenerator {
  
  public JavaFile generate(RegistryMetamodel registry, List<TableMetamodel> tables, Metamodel metamodel) {
    final var className = "ImmutablePersistenceUnit";
    final var interfaceName = registry.getName() + "DbBuilder.PersistenceUnit";
    
    final var classBuilder = TypeSpec.classBuilder(className)
      .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
      .addSuperinterface(ClassName.get(registry.getPackageName(), interfaceName));
    
    // Add standard fields
    classBuilder.addField(FieldSpec.builder(
        ParameterizedTypeName.get(ClassName.get(List.class), ClassName.get(String.class)),
        "commitMessages",
        Modifier.PRIVATE, Modifier.FINAL
      ).build());
    
    classBuilder.addField(FieldSpec.builder(
        ParameterizedTypeName.get(ClassName.get(List.class), ClassName.get(String.class)),
        "commitAuthors",
        Modifier.PRIVATE, Modifier.FINAL
      ).build());
    
    classBuilder.addField(FieldSpec.builder(
        String.class,
        "tenantId",
        Modifier.PRIVATE, Modifier.FINAL
      ).build());
    
    classBuilder.addField(FieldSpec.builder(
        ClassName.get(BatchStatus.class),
        "status",
        Modifier.PRIVATE, Modifier.FINAL
      ).build());
    
    classBuilder.addField(FieldSpec.builder(
        String.class,
        "log",
        Modifier.PRIVATE, Modifier.FINAL
      ).build());
    
    classBuilder.addField(FieldSpec.builder(
        ParameterizedTypeName.get(ClassName.get(List.class), ClassName.get(Message.class)),
        "commitLogs",
        Modifier.PRIVATE, Modifier.FINAL
      ).build());
    
    // Extract and add operation fields
    final var operations = extractOperations(tables);
    for (final var entry : operations.entrySet()) {
      final var fieldName = NamingUtils.toCamelCase(entry.getKey());
      final var entityType = entry.getValue();
      classBuilder.addField(FieldSpec.builder(
          ParameterizedTypeName.get(ClassName.get(List.class), entityType),
          fieldName,
          Modifier.PRIVATE, Modifier.FINAL
        ).build());
    }
    
    // Generate private constructor
    classBuilder.addMethod(generateConstructor(operations));
    
    // Generate getters
    classBuilder.addMethod(generateSimpleGetter("commitMessages", 
      ParameterizedTypeName.get(ClassName.get(List.class), ClassName.get(String.class))));
    classBuilder.addMethod(generateSimpleGetter("commitAuthors", 
      ParameterizedTypeName.get(ClassName.get(List.class), ClassName.get(String.class))));
    classBuilder.addMethod(generateSimpleGetter("tenantId", ClassName.get(String.class)));
    classBuilder.addMethod(generateSimpleGetter("status", ClassName.get(BatchStatus.class)));
    classBuilder.addMethod(generateSimpleGetter("log", ClassName.get(String.class)));
    classBuilder.addMethod(generateSimpleGetter("commitLogs", 
      ParameterizedTypeName.get(ClassName.get(List.class), ClassName.get(Message.class))));
    
    for (final var entry : operations.entrySet()) {
      final var fieldName = entry.getKey();
      final var entityType = entry.getValue();
      classBuilder.addMethod(generateOperationGetter(fieldName, entityType));
    }
    
    // Generate merge methods (override from interface)
    classBuilder.addMethod(generateMergeListMethod(className, interfaceName));
    classBuilder.addMethod(generateMergeSingleMethod(className, interfaceName));
    classBuilder.addMethod(generateMergeBuilderMethod(className, operations));
    
    // Generate Builder class
    classBuilder.addType(generateBuilderClass(className, interfaceName, operations));
    
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
  
  private Map<String, TypeName> extractOperations(List<TableMetamodel> tables) {
    final var operations = new HashMap<String, TypeName>();
    
    for (final var table : tables) {
      for (final var method : table.getSqlMethods()) {
        if (method.getType() == SqlMethodType.INSERT_ALL || 
            method.getType() == SqlMethodType.UPDATE_ALL || 
            method.getType() == SqlMethodType.DELETE_ALL) {
          
          final var fieldName = buildOperationFieldName(table, method.getType());
          if (fieldName != null && !operations.containsKey(fieldName)) {
            final var entityType = extractEntityType(method);
            if (entityType != null) {
              operations.put(fieldName, entityType);
            }
          }
        }
      }
    }
    
    return operations;
  }
  
  private String buildOperationFieldName(TableMetamodel table, SqlMethodType type) {
    final var baseName = NamingUtils.toPascalCase(table.getTableName());
    
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
  
  private MethodSpec generateConstructor(Map<String, TypeName> operations) {
    final var builder = MethodSpec.constructorBuilder()
      .addModifiers(Modifier.PRIVATE);
    
    // Add standard parameters
    builder.addParameter(ParameterizedTypeName.get(ClassName.get(List.class), ClassName.get(String.class)), "commitMessages");
    builder.addParameter(ParameterizedTypeName.get(ClassName.get(List.class), ClassName.get(String.class)), "commitAuthors");
    builder.addParameter(String.class, "tenantId");
    builder.addParameter(ClassName.get(BatchStatus.class), "status");
    builder.addParameter(String.class, "log");
    builder.addParameter(ParameterizedTypeName.get(ClassName.get(List.class), ClassName.get(Message.class)), "commitLogs");
    
    builder.addStatement("this.commitMessages = commitMessages");
    builder.addStatement("this.commitAuthors = commitAuthors");
    builder.addStatement("this.tenantId = tenantId");
    builder.addStatement("this.status = status");
    builder.addStatement("this.log = log");
    builder.addStatement("this.commitLogs = commitLogs");
    
    // Add operation parameters
    for (final var entry : operations.entrySet()) {
      final var fieldName = NamingUtils.toCamelCase(entry.getKey());
      final var entityType = entry.getValue();
      builder.addParameter(ParameterizedTypeName.get(ClassName.get(List.class), entityType), fieldName);
      builder.addStatement("this.$L = $L", fieldName, fieldName);
    }
    
    return builder.build();
  }
  
  private MethodSpec generateSimpleGetter(String fieldName, TypeName returnType) {
    final var getterName = "get" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
    
    return MethodSpec.methodBuilder(getterName)
      .addModifiers(Modifier.PUBLIC)
      .addAnnotation(Override.class)
      .returns(returnType)
      .addStatement("return $L", fieldName)
      .build();
  }
  
  private MethodSpec generateOperationGetter(String fieldName, TypeName entityType) {
    final var getterName = "get" + fieldName;
    final var camelFieldName = NamingUtils.toCamelCase(fieldName);
    
    return MethodSpec.methodBuilder(getterName)
      .addModifiers(Modifier.PUBLIC)
      .addAnnotation(Override.class)
      .returns(ParameterizedTypeName.get(ClassName.get(List.class), entityType))
      .addStatement("return $L", camelFieldName)
      .build();
  }
  
  private MethodSpec generateMergeListMethod(String className, String interfaceName) {
    return MethodSpec.methodBuilder("merge")
      .addModifiers(Modifier.PUBLIC)
      .addAnnotation(Override.class)
      .addParameter(ParameterizedTypeName.get(
        ClassName.get(List.class),
        ClassName.bestGuess(interfaceName)
      ), "src")
      .returns(ClassName.bestGuess(interfaceName))
      .addStatement("final var builder = $L.builder().from(this)", className)
      .addStatement("src.forEach(entry -> entry.merge(builder))")
      .addStatement("return builder.build()")
      .build();
  }
  
  private MethodSpec generateMergeSingleMethod(String className, String interfaceName) {
    return MethodSpec.methodBuilder("merge")
      .addModifiers(Modifier.PUBLIC)
      .addAnnotation(Override.class)
      .addParameter(ClassName.bestGuess(interfaceName), "src")
      .returns(ClassName.bestGuess(interfaceName))
      .addStatement("return merge($L.builder().from(src)).build()", className)
      .build();
  }
  
  private MethodSpec generateMergeBuilderMethod(String className, Map<String, TypeName> operations) {
    final var builderType = ClassName.bestGuess(className + ".Builder");
    
    final var method = MethodSpec.methodBuilder("merge")
      .addModifiers(Modifier.PUBLIC)
      .addAnnotation(Override.class)
      .addParameter(builderType, "target")
      .returns(builderType);
    
    final var code = CodeBlock.builder()
      .add("return target\n")
      .indent();
    
    for (final var fieldName : operations.keySet()) {
      code.add(".addAll$L(this.get$L())\n", fieldName, fieldName);
    }
    
    code.add(".addAllCommitLogs(this.getCommitLogs())")
      .add(".addAllCommitMessages(this.getCommitMessages())\n")
      .add(".addAllCommitAuthors(this.getCommitAuthors());");
    
    code.unindent();
    
    method.addCode(code.build());
    return method.build();
  }
  
  private TypeSpec generateBuilderClass(String className, String interfaceName, Map<String, TypeName> operations) {
    final var builderClass = TypeSpec.classBuilder("Builder")
      .addModifiers(Modifier.PUBLIC, Modifier.STATIC);
    
    final var immutableListBuilder = ClassName.get("com.google.common.collect", "ImmutableList", "Builder");
    
    // Add standard fields
    builderClass.addField(FieldSpec.builder(
        ParameterizedTypeName.get(immutableListBuilder, ClassName.get(String.class)),
        "commitMessages",
        Modifier.PRIVATE, Modifier.FINAL
      ).initializer("$T.builder()", ClassName.get("com.google.common.collect", "ImmutableList")).build());
    
    builderClass.addField(FieldSpec.builder(
        ParameterizedTypeName.get(immutableListBuilder, ClassName.get(String.class)),
        "commitAuthors",
        Modifier.PRIVATE, Modifier.FINAL
      ).initializer("$T.builder()", ClassName.get("com.google.common.collect", "ImmutableList")).build());
    
    builderClass.addField(FieldSpec.builder(
        String.class,
        "tenantId",
        Modifier.PRIVATE
      ).build());
    
    builderClass.addField(FieldSpec.builder(
        ClassName.get(BatchStatus.class),
        "status",
        Modifier.PRIVATE
      ).build());
    
    builderClass.addField(FieldSpec.builder(
        String.class,
        "log",
        Modifier.PRIVATE
      ).build());
    
    builderClass.addField(FieldSpec.builder(
        ParameterizedTypeName.get(immutableListBuilder, ClassName.get(Message.class)),
        "commitLogs",
        Modifier.PRIVATE, Modifier.FINAL
      ).initializer("$T.builder()", ClassName.get("com.google.common.collect", "ImmutableList")).build());
    
    // Add operation fields
    for (final var entry : operations.entrySet()) {
      final var fieldName = NamingUtils.toCamelCase(entry.getKey());
      final var entityType = entry.getValue();
      builderClass.addField(FieldSpec.builder(
          ParameterizedTypeName.get(immutableListBuilder, entityType),
          fieldName,
          Modifier.PRIVATE, Modifier.FINAL
        ).initializer("$T.builder()", ClassName.get("com.google.common.collect", "ImmutableList")).build());
    }
    
    // Generate from() method
    builderClass.addMethod(generateFromMethod(interfaceName, operations));
    
    // Generate setter methods
    builderClass.addMethod(generateListSetter("commitMessages", ClassName.get(String.class)));
    builderClass.addMethod(generateListSetter("commitAuthors", ClassName.get(String.class)));
    builderClass.addMethod(generateSimpleSetter("tenantId", ClassName.get(String.class)));
    builderClass.addMethod(generateSimpleSetter("status", ClassName.get(BatchStatus.class)));
    builderClass.addMethod(generateSimpleSetter("log", ClassName.get(String.class)));
    builderClass.addMethod(generateListSetter("commitLogs", ClassName.get(Message.class)));
    
    builderClass.addMethod(generateAddAllMethod("commitMessages", ClassName.get(String.class)));
    builderClass.addMethod(generateAddAllMethod("commitAuthors", ClassName.get(String.class)));
    builderClass.addMethod(generateAddAllMethod("commitLogs", ClassName.get(Message.class)));
    
    // Generate operation setters
    for (final var entry : operations.entrySet()) {
      final var fieldName = entry.getKey();
      final var entityType = entry.getValue();
      builderClass.addMethod(generateOperationSetter(fieldName, entityType));
      builderClass.addMethod(generateOperationAddAll(fieldName, entityType));
    }
    
    // Generate build() method
    builderClass.addMethod(generateBuildMethod(className, operations));
    
    return builderClass.build();
  }
  
  private MethodSpec generateFromMethod(String interfaceName, Map<String, TypeName> operations) {
    final var method = MethodSpec.methodBuilder("from")
      .addModifiers(Modifier.PUBLIC)
      .addParameter(ClassName.bestGuess(interfaceName), "source")
      .returns(ClassName.bestGuess("Builder"));
    
    method.addStatement("this.commitMessages.addAll(source.getCommitMessages())");
    method.addStatement("this.commitAuthors.addAll(source.getCommitAuthors())");
    method.addStatement("this.tenantId = source.getTenantId()");
    method.addStatement("this.status = source.getStatus()");
    method.addStatement("this.log = source.getLog()");
    method.addStatement("this.commitLogs.addAll(source.getCommitLogs())");
    
    for (final var fieldName : operations.keySet()) {
      final var camelFieldName = NamingUtils.toCamelCase(fieldName);
      method.addStatement("this.$L.addAll(source.get$L())", camelFieldName, fieldName);
    }
    
    method.addStatement("return this");
    
    return method.build();
  }
  
  private MethodSpec generateSimpleSetter(String fieldName, TypeName type) {
    return MethodSpec.methodBuilder(fieldName)
      .addModifiers(Modifier.PUBLIC)
      .addParameter(type, fieldName)
      .returns(ClassName.bestGuess("Builder"))
      .addStatement("this.$L = $L", fieldName, fieldName)
      .addStatement("return this")
      .build();
  }
  
  private MethodSpec generateListSetter(String fieldName, TypeName elementType) {
    return MethodSpec.methodBuilder(fieldName)
      .addModifiers(Modifier.PUBLIC)
      .addParameter(ParameterizedTypeName.get(ClassName.get(List.class), elementType), fieldName)
      .returns(ClassName.bestGuess("Builder"))
      .addStatement("this.$L.addAll($L)", fieldName, fieldName)
      .addStatement("return this")
      .build();
  }
  
  private MethodSpec generateAddAllMethod(String fieldName, TypeName elementType) {
    final var methodName = "addAll" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
    
    return MethodSpec.methodBuilder(methodName)
      .addModifiers(Modifier.PUBLIC)
      .addParameter(ParameterizedTypeName.get(ClassName.get(List.class), elementType), "values")
      .returns(ClassName.bestGuess("Builder"))
      .addStatement("this.$L.addAll(values)", fieldName)
      .addStatement("return this")
      .build();
  }
  
  private MethodSpec generateOperationSetter(String fieldName, TypeName entityType) {
    final var camelFieldName = NamingUtils.toCamelCase(fieldName);
    
    return MethodSpec.methodBuilder(camelFieldName)
      .addModifiers(Modifier.PUBLIC)
      .addParameter(ParameterizedTypeName.get(ClassName.get(List.class), entityType), camelFieldName)
      .returns(ClassName.bestGuess("Builder"))
      .addStatement("this.$L.addAll($L)", camelFieldName, camelFieldName)
      .addStatement("return this")
      .build();
  }
  
  private MethodSpec generateOperationAddAll(String fieldName, TypeName entityType) {
    final var methodName = "addAll" + fieldName;
    final var camelFieldName = NamingUtils.toCamelCase(fieldName);
    
    return MethodSpec.methodBuilder(methodName)
      .addModifiers(Modifier.PUBLIC)
      .addParameter(ParameterizedTypeName.get(ClassName.get(List.class), entityType), "values")
      .returns(ClassName.bestGuess("Builder"))
      .addStatement("this.$L.addAll(values)", camelFieldName)
      .addStatement("return this")
      .build();
  }
  
  private MethodSpec generateBuildMethod(String className, Map<String, TypeName> operations) {
    final var method = MethodSpec.methodBuilder("build")
      .addModifiers(Modifier.PUBLIC)
      .returns(ClassName.bestGuess(className));
    
    final var params = new StringBuilder();
    params.append("commitMessages.build(), commitAuthors.build(), tenantId, status, log, commitLogs.build()");
    
    for (final var fieldName : operations.keySet()) {
      params.append(", ").append(NamingUtils.toCamelCase(fieldName)).append(".build()");
    }
    
    method.addStatement("return new $L($L)", className, params.toString());
    
    return method.build();
  }
}