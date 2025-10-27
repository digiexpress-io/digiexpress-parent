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
import io.resys.thena.processor.support.NamingUtils;

public class RegistryFactorySqlCodeGenerator {
  
  public JavaFile generate(RegistryModel registry, List<TableModel> tables) {
    final var className = registry.getRegistryClassName();
    
    final var classBuilder = TypeSpec.classBuilder(className)
      .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
      .addAnnotation(ClassName.get("lombok", "Value"));
    
    // Add fields
    classBuilder.addField(FieldSpec.builder(
      ClassName.bestGuess(registry.getTableClassName()),
      "tables"
    ).build());
    
    classBuilder.addField(FieldSpec.builder(
      ClassName.get(ThenaSqlDataSource.class),
      "dataSource"
    ).build());
    
    // Add factory method for each table
    for (final var table : tables) {
      classBuilder.addMethod(generateFactoryMethod(table));
    }
    
    return JavaFile.builder(registry.getPackageName() + ".spi", classBuilder.build())
      .indent("  ")
      .build();
  }
  
  private MethodSpec generateFactoryMethod(TableModel table) {
    final var interfaceName = table.getInterfaceName();
    final var interfaceClassName = ClassName.get(table.getPackageName(), table.getInterfaceName());
    final var methodName = NamingUtils.pluralize(table.getTableName());
    final var implName = table.getImplClassName();
    
    return MethodSpec.methodBuilder(methodName)
      .addModifiers(Modifier.PUBLIC)
      .returns(ClassName.bestGuess(implName))
      .addStatement("return new $L(tables, dataSource)", implName)
      .build();
  }
}