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
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

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
  
  private static final ClassName JSON_CREATOR = ClassName.get("com.fasterxml.jackson.annotation", "JsonCreator");
  private static final ClassName JSON_PROPERTY = ClassName.get("com.fasterxml.jackson.annotation", "JsonProperty");

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
      final var fieldName = uncapitalize(entry.getKey());
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

    // Generate addAllToInserts(World) method
    classBuilder.addMethod(generateAddAllToInsertsMethod(registry, className, interfaceName, tables));

    // Generate wither methods (override single property, not additive)
    for (final var wither : generateWitherMethods(className, interfaceName, operations)) {
      classBuilder.addMethod(wither);
    }

    // Generate split(maxSize) method + generic chunking helper
    final var orderedFieldNames = orderedOperationFieldNames(tables, operations);
    classBuilder.addMethod(generateSplitMethod(className, interfaceName, orderedFieldNames));
    classBuilder.addMethod(generateChunkListHelperMethod());

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

  private ParameterSpec jsonParam(TypeName type, String name) {
    return ParameterSpec.builder(type, name)
      .addAnnotation(AnnotationSpec.builder(JSON_PROPERTY).addMember("value", "$S", name).build())
      .build();
  }

  private MethodSpec generateConstructor(Map<String, TypeName> operations) {
    final var builder = MethodSpec.constructorBuilder()
      .addModifiers(Modifier.PUBLIC)
      .addAnnotation(JSON_CREATOR);

    // Add standard parameters
    builder.addParameter(jsonParam(ParameterizedTypeName.get(ClassName.get(List.class), ClassName.get(String.class)), "commitMessages"));
    builder.addParameter(jsonParam(ParameterizedTypeName.get(ClassName.get(List.class), ClassName.get(String.class)), "commitAuthors"));
    builder.addParameter(jsonParam(ClassName.get(String.class), "tenantId"));
    builder.addParameter(jsonParam(ClassName.get(BatchStatus.class), "status"));
    builder.addParameter(jsonParam(ClassName.get(String.class), "log"));
    builder.addParameter(jsonParam(ParameterizedTypeName.get(ClassName.get(List.class), ClassName.get(Message.class)), "commitLogs"));

    builder.addStatement("this.commitMessages = commitMessages");
    builder.addStatement("this.commitAuthors = commitAuthors");
    builder.addStatement("this.tenantId = tenantId");
    builder.addStatement("this.status = status");
    builder.addStatement("this.log = log");
    builder.addStatement("this.commitLogs = commitLogs");

    // Add operation parameters
    for (final var entry : operations.entrySet()) {
      final var fieldName = uncapitalize(entry.getKey());
      final var entityType = entry.getValue();
      builder.addParameter(jsonParam(ParameterizedTypeName.get(ClassName.get(List.class), entityType), fieldName));
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
    final var camelFieldName = uncapitalize(fieldName);
    
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
      .returns(ClassName.bestGuess(className))
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
      .returns(ClassName.bestGuess(className))
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
      final var fieldName = uncapitalize(entry.getKey());
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
    
    // Generate add single item methods
    builderClass.addMethod(generateAddMethod("commitMessage", "commitMessages", ClassName.get(String.class)));
    builderClass.addMethod(generateAddMethod("commitAuthor", "commitAuthors", ClassName.get(String.class)));
    builderClass.addMethod(generateAddMethod("commitLog", "commitLogs", ClassName.get(Message.class)));
    
    // Generate addAll methods
    builderClass.addMethod(generateAddAllMethod("commitMessages", ClassName.get(String.class)));
    builderClass.addMethod(generateAddAllMethod("commitAuthors", ClassName.get(String.class)));
    builderClass.addMethod(generateAddAllMethod("commitLogs", ClassName.get(Message.class)));
    
    // Generate operation setters
    for (final var entry : operations.entrySet()) {
      final var fieldName = entry.getKey();
      final var entityType = entry.getValue();
      builderClass.addMethod(generateOperationSetter(fieldName, entityType));
      builderClass.addMethod(generateOperationAddSingle(fieldName, entityType));
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
      final var camelFieldName = uncapitalize(fieldName);
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
  
  private MethodSpec generateAddMethod(String singularName, String fieldName, TypeName elementType) {
    final var methodName = "add" + Character.toUpperCase(singularName.charAt(0)) + singularName.substring(1);
    
    return MethodSpec.methodBuilder(methodName)
      .addModifiers(Modifier.PUBLIC)
      .addParameter(elementType, "value")
      .returns(ClassName.bestGuess("Builder"))
      .addStatement("this.$L.add(value)", fieldName)
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
    final var camelFieldName = uncapitalize(fieldName);
    
    return MethodSpec.methodBuilder(camelFieldName)
      .addModifiers(Modifier.PUBLIC)
      .addParameter(ParameterizedTypeName.get(ClassName.get(List.class), entityType), camelFieldName)
      .returns(ClassName.bestGuess("Builder"))
      .addStatement("this.$L.addAll($L)", camelFieldName, camelFieldName)
      .addStatement("return this")
      .build();
  }
  
  private MethodSpec generateOperationAddSingle(String fieldName, TypeName entityType) {
    // Use exact field name like Immutables does
    final var methodName = "add" + fieldName;
    final var camelFieldName = uncapitalize(fieldName);
    
    return MethodSpec.methodBuilder(methodName)
      .addModifiers(Modifier.PUBLIC)
      .addParameter(entityType, "value")
      .returns(ClassName.bestGuess("Builder"))
      .addStatement("this.$L.add(value)", camelFieldName)
      .addStatement("return this")
      .build();
  }
  
  private MethodSpec generateOperationAddAll(String fieldName, TypeName entityType) {
    final var methodName = "addAll" + fieldName;
    final var camelFieldName = uncapitalize(fieldName);
    
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
      params.append(", ").append(uncapitalize(fieldName)).append(".build()");
    }
    
    method.addStatement("return new $L($L)", className, params.toString());
    
    return method.build();
  }
  
  private MethodSpec generateAddAllToInsertsMethod(
      RegistryMetamodel registry,
      String className,
      String interfaceName,
      List<TableMetamodel> tables) {

    final var worldType = ClassName.get(registry.getPackageName(), registry.getName() + "DbQuery", registry.getWorldName());

    final var method = MethodSpec.methodBuilder("addAllToInserts")
      .addModifiers(Modifier.PUBLIC)
      .addParameter(worldType, "world")
      .returns(ClassName.bestGuess(className + ".Builder"));

    method.addStatement("final var builder = $T.builder().from(this)", ClassName.bestGuess(className));

    for (final var table : tables) {
      final var insertAllMethod = table.getSqlMethods().stream()
        .filter(m -> m.getType() == SqlMethodType.INSERT_ALL)
        .findFirst()
        .orElse(null);
      if (insertAllMethod == null) {
        continue;
      }

      final var insertEntityType = extractEntityType(insertAllMethod);
      final var worldEntityType = findEntityTypeForTable(table);
      if (insertEntityType == null || worldEntityType == null || !insertEntityType.equals(worldEntityType)) {
        continue;
      }

      final var fieldName = buildOperationFieldName(table, SqlMethodType.INSERT_ALL);
      final var worldGetter = "get" + NamingUtils.toPascalCase(table.getTableName());
      method.addStatement("builder.addAll$L(world.$L().values().stream().toList())", fieldName, worldGetter);
    }

    method.addStatement("return builder");

    return method.build();
  }

  private record PropertySpecInfo(String pascalName, TypeName type, String getter) {}

  private List<PropertySpecInfo> buildPropertySpecs(Map<String, TypeName> operations) {
    final var specs = new ArrayList<PropertySpecInfo>();
    specs.add(new PropertySpecInfo("CommitMessages", ParameterizedTypeName.get(ClassName.get(List.class), ClassName.get(String.class)), "getCommitMessages"));
    specs.add(new PropertySpecInfo("CommitAuthors", ParameterizedTypeName.get(ClassName.get(List.class), ClassName.get(String.class)), "getCommitAuthors"));
    specs.add(new PropertySpecInfo("TenantId", ClassName.get(String.class), "getTenantId"));
    specs.add(new PropertySpecInfo("Status", ClassName.get(BatchStatus.class), "getStatus"));
    specs.add(new PropertySpecInfo("Log", ClassName.get(String.class), "getLog"));
    specs.add(new PropertySpecInfo("CommitLogs", ParameterizedTypeName.get(ClassName.get(List.class), ClassName.get(Message.class)), "getCommitLogs"));

    for (final var entry : operations.entrySet()) {
      final var key = entry.getKey();
      specs.add(new PropertySpecInfo(key, ParameterizedTypeName.get(ClassName.get(List.class), entry.getValue()), "get" + key));
    }

    return specs;
  }

  private List<MethodSpec> generateWitherMethods(String className, String interfaceName, Map<String, TypeName> operations) {
    final var specs = buildPropertySpecs(operations);
    final var methods = new ArrayList<MethodSpec>();

    for (final var target : specs) {
      final var method = MethodSpec.methodBuilder("with" + target.pascalName())
        .addModifiers(Modifier.PUBLIC)
        .addParameter(target.type(), "value")
        .returns(ClassName.bestGuess(interfaceName));

      final var args = new StringBuilder();
      var first = true;
      for (final var spec : specs) {
        if (!first) {
          args.append(", ");
        }
        args.append(spec == target ? "value" : ("this." + spec.getter() + "()"));
        first = false;
      }

      method.addStatement("return new $T($L)", ClassName.bestGuess(className), args.toString());
      methods.add(method.build());
    }

    return methods;
  }

  // Delete-phase fields (in table order), then insert-phase, then update-phase - matches the
  // phase/table ordering that Gen_Multi_BuilderImplementation.generatePersistMethod relies on.
  private List<String> orderedOperationFieldNames(List<TableMetamodel> tables, Map<String, TypeName> operations) {
    final var tablesByOrder = tables.stream()
      .sorted((a, b) -> Integer.compare(a.getOrder(), b.getOrder()))
      .toList();

    final var deletes = new ArrayList<String>();
    final var inserts = new ArrayList<String>();
    final var updates = new ArrayList<String>();
    final var seen = new HashSet<String>();

    for (final var table : tablesByOrder) {
      for (final var method : table.getSqlMethods()) {
        final var fieldName = buildOperationFieldName(table, method.getType());
        if (fieldName == null || !operations.containsKey(fieldName) || !seen.add(fieldName)) {
          continue;
        }

        switch (method.getType()) {
          case DELETE_ALL -> deletes.add(fieldName);
          case INSERT_ALL -> inserts.add(fieldName);
          case UPDATE_ALL -> updates.add(fieldName);
          default -> { /* skip */ }
        }
      }
    }

    final var ordered = new ArrayList<String>();
    ordered.addAll(deletes);
    ordered.addAll(inserts);
    ordered.addAll(updates);
    return ordered;
  }

  private MethodSpec generateSplitMethod(String className, String interfaceName, List<String> orderedFieldNames) {
    final var method = MethodSpec.methodBuilder("split")
      .addModifiers(Modifier.PUBLIC)
      .addAnnotation(Override.class)
      .addParameter(TypeName.INT, "maxSize")
      .returns(ParameterizedTypeName.get(ClassName.get(List.class), ClassName.bestGuess(interfaceName)));

    final var totalCountExpr = new StringBuilder("0");
    for (final var fieldName : orderedFieldNames) {
      totalCountExpr.append(" + this.").append(uncapitalize(fieldName)).append(".size()");
    }
    method.addStatement("final var totalCount = $L", totalCountExpr.toString());

    method.beginControlFlow("if (totalCount <= maxSize)");
    method.addStatement("return $T.of(this)", List.class);
    method.endControlFlow();

    method.addCode("\n");
    method.addStatement("final var numChunks = (totalCount + maxSize - 1) / maxSize");
    method.addStatement("final var builders = new $T<$T>(numChunks)", ArrayList.class, ClassName.bestGuess("Builder"));
    method.beginControlFlow("for (int i = 0; i < numChunks; i++)");
    method.addStatement("builders.add($L.builder())", className);
    method.endControlFlow();

    method.addCode("\n");
    method.addStatement("int cursor = 0");

    for (final var fieldName : orderedFieldNames) {
      final var camel = uncapitalize(fieldName);
      method.addCode("\n");
      method.addStatement("final var $LChunks = chunkList(this.$L, cursor, maxSize, numChunks)", camel, camel);
      method.beginControlFlow("for (int i = 0; i < numChunks; i++)");
      method.addStatement("builders.get(i).addAll$L($LChunks.get(i))", fieldName, camel);
      method.endControlFlow();
      method.addStatement("cursor += this.$L.size()", camel);
    }

    method.addCode("\n");
    method.addStatement("final var result = new $T<$T>(numChunks)", ArrayList.class, ClassName.bestGuess(interfaceName));
    method.beginControlFlow("for (int i = 0; i < numChunks; i++)");
    method.addStatement(
      "final var unit = builders.get(i)\n" +
      "  .tenantId(this.tenantId)\n" +
      "  .status(this.status)\n" +
      "  .log(this.log)");
    method.beginControlFlow("if (i == numChunks - 1)");
    method.addStatement(
      "unit\n" +
      "  .addAllCommitMessages(this.commitMessages)\n" +
      "  .addAllCommitAuthors(this.commitAuthors)\n" +
      "  .addAllCommitLogs(this.commitLogs)");
    method.endControlFlow();
    method.addStatement("result.add(unit.build())");
    method.endControlFlow();
    method.addStatement("return result");

    return method.build();
  }

  // Slices `data` into `numChunks` sublists aligned to maxSize-sized windows over the raw,
  // caller-tracked `cursor` position - the same window every field in the ordered sequence shares.
  private MethodSpec generateChunkListHelperMethod() {
    final var typeVar = TypeVariableName.get("T");
    final var listOfT = ParameterizedTypeName.get(ClassName.get(List.class), typeVar);
    final var listOfListOfT = ParameterizedTypeName.get(ClassName.get(List.class), listOfT);

    return MethodSpec.methodBuilder("chunkList")
      .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
      .addTypeVariable(typeVar)
      .addParameter(listOfT, "data")
      .addParameter(TypeName.INT, "cursor")
      .addParameter(TypeName.INT, "maxSize")
      .addParameter(TypeName.INT, "numChunks")
      .returns(listOfListOfT)
      .addStatement("final $T result = new $T<>(numChunks)", listOfListOfT, ArrayList.class)
      .beginControlFlow("for (int i = 0; i < numChunks; i++)")
      .addStatement("final var chunkStart = i * maxSize")
      .addStatement("final var fromLocal = $T.max(0, chunkStart - cursor)", Math.class)
      .addStatement("final var toLocal = $T.min(data.size(), chunkStart + maxSize - cursor)", Math.class)
      .addStatement("result.add(fromLocal < toLocal ? data.subList(fromLocal, toLocal) : $T.of())", List.class)
      .endControlFlow()
      .addStatement("return result")
      .build();
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

  private String uncapitalize(String str) {
    if (str == null || str.isEmpty()) {
      return str;
    }
    return Character.toLowerCase(str.charAt(0)) + str.substring(1);
  }
}