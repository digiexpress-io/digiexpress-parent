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

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import io.resys.thena.api.entities.Tenant;
import io.resys.thena.processor.model.TableModel.RegistryModel;
import io.resys.thena.spi.TenantDataSource;
import io.resys.thena.spi.TenantDataSource.TxScope;

public class DbInterfaceSqlCodeGenerator {
  
  public JavaFile generate(RegistryModel registry) {
    final var className = registry.getName() + "Db";
    final var transactionInterfaceName = "Transaction";
    
    final var interfaceBuilder = TypeSpec.interfaceBuilder(className)
      .addModifiers(Modifier.PUBLIC)
      .addSuperinterface(ClassName.get(TenantDataSource.class));
    
    // Add withTenant methods
    interfaceBuilder.addMethod(MethodSpec.methodBuilder("withTenant")
      .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
      .returns(ParameterizedTypeName.get(
        ClassName.get("io.smallrye.mutiny", "Uni"),
        ClassName.get(registry.getPackageName(), className)
      ))
      .build());
    
    interfaceBuilder.addMethod(MethodSpec.methodBuilder("withTenant")
      .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
      .addParameter(String.class, "tenantId")
      .returns(ParameterizedTypeName.get(
        ClassName.get("io.smallrye.mutiny", "Uni"),
        ClassName.get(registry.getPackageName(), className)
      ))
      .build());
    
    interfaceBuilder.addMethod(MethodSpec.methodBuilder("withTenant")
      .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
      .addParameter(ClassName.get(Tenant.class), "tenant")
      .returns(ClassName.get(registry.getPackageName(), className))
      .build());
    
    // Add query method
    interfaceBuilder.addMethod(MethodSpec.methodBuilder("query")
      .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
      .returns(ClassName.get(registry.getPackageName(), registry.getName() + "DbQuery"))
      .build());
    
    // Add builder method
    interfaceBuilder.addMethod(MethodSpec.methodBuilder("builder")
      .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
      .returns(ClassName.get(registry.getPackageName(), registry.getName() + "DbBuilder"))
      .build());
    
    // Add withTransaction method
    final var typeVarR = TypeVariableName.get("R");
    interfaceBuilder.addMethod(MethodSpec.methodBuilder("withTransaction")
      .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
      .addTypeVariable(typeVarR)
      .addParameter(ClassName.get(TxScope.class), "scope")
      .addParameter(ClassName.bestGuess(transactionInterfaceName), "callback")
      .returns(ParameterizedTypeName.get(
        ClassName.get("io.smallrye.mutiny", "Uni"),
        typeVarR
      ))
      .build());
    
    // Add nested Transaction functional interface
    final var transactionInterface = TypeSpec.interfaceBuilder(transactionInterfaceName)
      .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
      .addAnnotation(FunctionalInterface.class)
      .addTypeVariable(typeVarR);
    
    transactionInterface.addMethod(MethodSpec.methodBuilder("apply")
      .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
      .addParameter(ClassName.get(registry.getPackageName(), className), "currentState")
      .returns(ParameterizedTypeName.get(
        ClassName.get("io.smallrye.mutiny", "Uni"),
        typeVarR
      ))
      .build());
    
    interfaceBuilder.addType(transactionInterface.build());
    
    return JavaFile.builder(registry.getPackageName(), interfaceBuilder.build())
      .indent("  ")
      .build();
  }
}
