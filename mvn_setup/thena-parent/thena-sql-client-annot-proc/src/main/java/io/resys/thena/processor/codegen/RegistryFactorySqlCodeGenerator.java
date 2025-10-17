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

import java.util.List;
import javax.lang.model.element.Modifier;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import io.resys.thena.datasource.ThenaSqlDataSource;
import io.resys.thena.processor.model.TableModel;
import io.resys.thena.processor.model.TableModel.RegistryModel;

public class RegistryFactorySqlCodeGenerator {
  
  public JavaFile generate(RegistryModel registry, List<TableModel> tables) {
    final var className = registry.getRegistryClassName();
    
    final var classBuilder = TypeSpec.classBuilder(className)
      .addModifiers(Modifier.PUBLIC, Modifier.FINAL);
    
    // Add private final fields
    classBuilder.addField(FieldSpec.builder(
      ClassName.get(registry.getPackageName(), registry.getTableClassName()),
      "tables",
      Modifier.PRIVATE, Modifier.FINAL
    ).build());
    
    classBuilder.addField(FieldSpec.builder(
      ClassName.get(ThenaSqlDataSource.class),
      "dataSource",
      Modifier.PRIVATE, Modifier.FINAL
    ).build());
    
    // Add constructor
    classBuilder.addMethod(generateConstructor(registry));
    
    // Add factory method for each table
    for (final var table : tables) {
      classBuilder.addMethod(generateFactoryMethod(table));
    }
    
    return JavaFile.builder(registry.getPackageName(), classBuilder.build())
      .indent("  ")
      .build();
  }
  
  private MethodSpec generateConstructor(RegistryModel registry) {
    return MethodSpec.constructorBuilder()
      .addModifiers(Modifier.PUBLIC)
      .addParameter(ClassName.get(registry.getPackageName(), registry.getTableClassName()), "tables")
      .addParameter(ClassName.get(ThenaSqlDataSource.class), "dataSource")
      .addStatement("this.tables = tables")
      .addStatement("this.dataSource = dataSource")
      .build();
  }
  
  private MethodSpec generateFactoryMethod(TableModel table) {
    final var methodName = pluralize(table.getTableName());
    final var interfaceName = table.getInterfaceName();
    final var implName = table.getImplClassName();
    final var interfaceClassName = ClassName.get(table.getPackageName(), table.getInterfaceName());

    
    return MethodSpec.methodBuilder(methodName)
      .addModifiers(Modifier.PUBLIC)
      .returns(ClassName.get(table.getPackageName(), interfaceName))
      .returns(interfaceClassName)  // Use ClassName with full package
      .addStatement("return new $L(tables, dataSource)", implName)
      .build();
  }
  
  private String pluralize(String tableName) {
    // Convert snake_case to camelCase and attempt simple pluralization
    // grim_mission -> missions
    // grim_commit -> commits
    
    final var parts = tableName.split("_");
    final var result = new StringBuilder();
    
    for (int i = 0; i < parts.length; i++) {
      final var part = parts[i];
      if (!part.isEmpty()) {
        if (i == 0) {
          result.append(part);
        } else {
          result.append(Character.toUpperCase(part.charAt(0)))
                .append(part.substring(1));
        }
      }
    }
    
    // Simple pluralization: add 's' if doesn't end in 's'
    final var camelCase = result.toString();
    if (!camelCase.endsWith("s")) {
      return camelCase + "s";
    }
    return camelCase;
  }
}