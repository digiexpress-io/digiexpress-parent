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
import java.util.Optional;

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import io.resys.thena.processor.model.MultiTableCodeGenerator;
import io.resys.thena.processor.model.TableModel;
import io.resys.thena.processor.model.TableModel.RegistryModel;
import io.resys.thena.processor.model.TableModel.SqlMethodType;
import io.resys.thena.processor.support.NamingUtils;

public class Gen_Multi_QueryInterface implements MultiTableCodeGenerator {
  
  public JavaFile generate(RegistryModel registry, List<TableModel> tables) {
    final var className = registry.getName() + "DbQuery";
    
    final var interfaceBuilder = TypeSpec.interfaceBuilder(className)
      .addModifiers(Modifier.PUBLIC);
    
    // Add findAll method
    interfaceBuilder.addMethod(MethodSpec.methodBuilder("findAll")
      .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
      .returns(ParameterizedTypeName.get(
        ClassName.get("io.smallrye.mutiny", "Uni"),
        ClassName.bestGuess(registry.getWorldName())
      ))
      .build());
    
    // Add query method for each table
    for (final var table : tables) {
      final var queryMethodName = "query" + NamingUtils.toPascalCase(table.getTableName());
      final var nestedInterfaceName = NamingUtils.toPascalCase(table.getTableName()) + "Query";
      
      interfaceBuilder.addMethod(MethodSpec.methodBuilder(queryMethodName)
        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
        .returns(ClassName.bestGuess(nestedInterfaceName))
        .build());
      
      // Generate nested query interface for this table
      interfaceBuilder.addType(generateTableQueryInterface(table, nestedInterfaceName));
    }
    
    // Generate World interface
    interfaceBuilder.addType(generateWorldInterface(registry, tables));
    
    return JavaFile.builder(registry.getPackageName(), interfaceBuilder.build())
      .indent("  ")
      .build();
  }
  
  private TypeSpec generateTableQueryInterface(TableModel table, String interfaceName) {
    final var nestedInterface = TypeSpec.interfaceBuilder(interfaceName)
      .addModifiers(Modifier.PUBLIC, Modifier.STATIC);
    
    // Add method for each SELECT query in the table
    for (final var method : table.getSqlMethods()) {
      if (method.getType() == SqlMethodType.SELECT || method.getType() == SqlMethodType.SELECT_ALL) {
        final var methodBuilder = MethodSpec.methodBuilder(method.getMethodName())
          .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);
        
        // Add parameters
        for (final var param : method.getParameters()) {
          methodBuilder.addParameter(param.getType(), param.getName());
        }
        
        // Determine return type based on SELECT vs SELECT_ALL
        final var entityType = Optional.ofNullable(method.getReturnType()).orElse(ClassName.get(Object.class));
        if (method.getType() == SqlMethodType.SELECT && method.isOptional()) {
          // Single optional result: Uni<Optional<Entity>>
          methodBuilder.returns(ParameterizedTypeName.get(
            ClassName.get("io.smallrye.mutiny", "Uni"),
            ParameterizedTypeName.get(
              ClassName.get("java.util", "Optional"),
              entityType
            )
          ));
        } else if (method.getType() == SqlMethodType.SELECT && !method.isOptional()) {
          // Single required result: Uni<Entity>
          methodBuilder.returns(ParameterizedTypeName.get(
            ClassName.get("io.smallrye.mutiny", "Uni"),
            entityType
          ));
        } else if (method.getType() == SqlMethodType.SELECT_ALL && method.isMultiWrapper()) {
          // Multiple results with Multi: Multi<Entity>
          methodBuilder.returns(ParameterizedTypeName.get(
            ClassName.get("io.smallrye.mutiny", "Multi"),
            entityType
          ));
        } else {
          // Multiple results with Uni: Uni<List<Entity>>
          methodBuilder.returns(ParameterizedTypeName.get(
            ClassName.get("io.smallrye.mutiny", "Uni"),
            ParameterizedTypeName.get(
              ClassName.get("java.util", "List"),
              entityType
            )
          ));
        }
        
        nestedInterface.addMethod(methodBuilder.build());
      }
    }
    
    return nestedInterface.build();
  }
  
  private TypeSpec generateWorldInterface(RegistryModel registry, List<TableModel> tables) {
    final var worldInterface = TypeSpec.interfaceBuilder(registry.getWorldName())
      .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
      .addAnnotation(AnnotationSpec.builder(ClassName.get("org.immutables.value", "Value", "Immutable")).build());
    
    for (final var table : tables) {
      final var entityType = findEntityTypeForTable(table);
      if (entityType != null) {
        final var getterName = "get" + NamingUtils.toPascalCase(table.getTableName());
        final var returnType = ParameterizedTypeName.get(
          ClassName.get(Map.class),
          ClassName.get(String.class),
          entityType
        );
        
        worldInterface.addMethod(MethodSpec.methodBuilder(getterName)
          .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
          .returns(returnType)
          .build());
      }
    }
    
    return worldInterface.build();
  }
  
  private ClassName findEntityTypeForTable(TableModel table) {
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
