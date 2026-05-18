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

import com.squareup.javapoet.AnnotationSpec;
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

public class Gen_Multi_BuilderInterface implements MultiTableCodeGenerator {
  
  public JavaFile generate(RegistryMetamodel registry, List<TableMetamodel> tables, Metamodel metamodel) {
    final var className = registry.getName() + "DbBuilder";
    final var persistenceUnitName = "PersistenceUnit";
    
    final var interfaceBuilder = TypeSpec.interfaceBuilder(className)
      .addModifiers(Modifier.PUBLIC);
    
    interfaceBuilder.addMethod(MethodSpec.methodBuilder("from")
      .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
      .addParameter(ClassName.bestGuess(persistenceUnitName), "unit")
      .returns(ClassName.bestGuess(className))
      .build());
    
    interfaceBuilder.addMethod(MethodSpec.methodBuilder("persist")
      .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
      .returns(ParameterizedTypeName.get(
        ClassName.get("io.smallrye.mutiny", "Uni"),
        ClassName.bestGuess(persistenceUnitName)
      ))
      .build());
    
    interfaceBuilder.addType(generateExceptionClass(registry, persistenceUnitName));
    
    final var operations = extractOperations(tables);
    interfaceBuilder.addType(generatePersistenceUnitInterface(persistenceUnitName, operations));
    
    return JavaFile.builder(registry.getPackageName(), interfaceBuilder.build())
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
  
  private TypeSpec generatePersistenceUnitInterface(String interfaceName, Map<String, TypeName> operations) {
    final var persistenceUnit = TypeSpec.interfaceBuilder(interfaceName)
      .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
      .addAnnotation(AnnotationSpec.builder(ClassName.get("org.immutables.value", "Value", "Immutable")).build());
    
    persistenceUnit.addMethod(MethodSpec.methodBuilder("getCommitMessages")
      .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
      .returns(ParameterizedTypeName.get(ClassName.get(List.class), ClassName.get(String.class)))
      .build());
    
    persistenceUnit.addMethod(MethodSpec.methodBuilder("getCommitAuthors")
      .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
      .returns(ParameterizedTypeName.get(ClassName.get(List.class), ClassName.get(String.class)))
      .build());
    
    for (final var entry : operations.entrySet()) {
      final var fieldName = entry.getKey();
      final var entityType = entry.getValue();
      
      persistenceUnit.addMethod(MethodSpec.methodBuilder("get" + fieldName)
        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
        .returns(ParameterizedTypeName.get(ClassName.get(List.class), entityType))
        .build());
    }
    
    persistenceUnit.addMethod(MethodSpec.methodBuilder("getTenantId")
      .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
      .returns(String.class)
      .build());
    
    persistenceUnit.addMethod(MethodSpec.methodBuilder("getStatus")
      .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
      .returns(ClassName.get(BatchStatus.class))
      .build());
    
    persistenceUnit.addMethod(MethodSpec.methodBuilder("getLog")
      .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
      .returns(String.class)
      .build());
    
    persistenceUnit.addMethod(MethodSpec.methodBuilder("getCommitLogs")
      .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
      .returns(ParameterizedTypeName.get(
        ClassName.get(List.class),
        ClassName.get(Message.class)
      ))
      .build());
    
    persistenceUnit.addMethod(generateMergeListMethod(interfaceName));
    persistenceUnit.addMethod(generateMergeSingleMethod(interfaceName));
    persistenceUnit.addMethod(generateMergeBuilderMethod(interfaceName, operations));
    
    return persistenceUnit.build();
  }
  
  private MethodSpec generateMergeListMethod(String interfaceName) {
    return MethodSpec.methodBuilder("merge")
      .addModifiers(Modifier.PUBLIC, Modifier.DEFAULT)
      .addParameter(ParameterizedTypeName.get(
        ClassName.get(List.class),
        ClassName.bestGuess(interfaceName)
      ), "src")
      .returns(ClassName.bestGuess(interfaceName))
      .addStatement("final var builder = Immutable$L.builder().from(this)", interfaceName)
      .addStatement("src.forEach(entry -> entry.merge(builder))")
      .addStatement("return builder.build()")
      .build();
  }
  
  private MethodSpec generateMergeSingleMethod(String interfaceName) {
    return MethodSpec.methodBuilder("merge")
      .addModifiers(Modifier.PUBLIC, Modifier.DEFAULT)
      .addParameter(ClassName.bestGuess(interfaceName), "src")
      .returns(ClassName.bestGuess(interfaceName))
      .addStatement("return merge(Immutable$L.builder().from(src)).build()", interfaceName)
      .build();
  }
  
  private MethodSpec generateMergeBuilderMethod(String interfaceName, Map<String, TypeName> operations) {
    final var builderType = ClassName.bestGuess("Immutable" + interfaceName + ".Builder");
    
    final var method = MethodSpec.methodBuilder("merge")
      .addModifiers(Modifier.PUBLIC, Modifier.DEFAULT)
      .addParameter(builderType, "target")
      .returns(builderType);
    
    final var code = CodeBlock.builder()
      .add("return target\n")
      .indent();
    
    for (final var fieldName : operations.keySet()) {
      code.add(".addAll$L(this.get$L())\n", fieldName, fieldName);
    }
    
    code.add(".addAllCommitLogs(this.getCommitLogs())");
    code.add(".addAllCommitMessages(this.getCommitMessages())\n");
    code.add(".addAllCommitAuthors(this.getCommitAuthors());");
    
    code.unindent();
    
    method.addCode(code.build());
    return method.build();
  }

  
  private TypeSpec generateExceptionClass(RegistryMetamodel registry, String persistenceUnitName) {
    final var exceptionClassName = registry.getExceptionClassName();
    
    final var exceptionClass = TypeSpec.classBuilder(exceptionClassName)
      .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
      .superclass(RuntimeException.class);
    
    exceptionClass.addField(FieldSpec.builder(
        long.class,
        "serialVersionUID",
        Modifier.PRIVATE, Modifier.FINAL, Modifier.STATIC
      ).initializer("1l").build());
      
    exceptionClass.addField(FieldSpec.builder(
      ClassName.bestGuess(persistenceUnitName),
      "container",
      Modifier.PRIVATE, Modifier.FINAL
    ).build());
    
    exceptionClass.addField(FieldSpec.builder(
      String.class,
      "txLog",
      Modifier.PRIVATE, Modifier.FINAL
    ).build());
      
    
    exceptionClass.addMethod(MethodSpec.constructorBuilder()
      .addModifiers(Modifier.PUBLIC)
      .addParameter(ClassName.bestGuess(persistenceUnitName), "container")
      .addParameter(String.class, "message")
      .addParameter(String.class, "txLog")
      .addParameter(Throwable.class, "cause")
      .addStatement("super(message, cause)")
      .addStatement("this.txLog = txLog")
      .addStatement("this.container = container")
      .build());
    
    exceptionClass.addMethod(MethodSpec.methodBuilder("getContainer")
      .addModifiers(Modifier.PUBLIC)
      .returns(ClassName.bestGuess(persistenceUnitName))
      .addStatement("return container")
      .build());
    
    exceptionClass.addMethod(MethodSpec.methodBuilder("getTxLog")
      .addModifiers(Modifier.PUBLIC)
      .returns(String.class)
      .addStatement("return txLog")
      .build());
    return exceptionClass.build();
  }
}
