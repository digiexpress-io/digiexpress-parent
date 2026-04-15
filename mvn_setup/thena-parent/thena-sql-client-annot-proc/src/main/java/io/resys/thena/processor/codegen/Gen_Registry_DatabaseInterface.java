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

/**
 * Generates the main database transaction interface for a registry.
 * 
 * <p>This generator creates the primary interface that provides transaction-based 
 * access to the database operations. It includes methods for creating queries, 
 * managing transactions, and provides generic repository-style access patterns.
 * 
 * <h3>Generated Code Example:</h3>
 * <pre>{@code
 * public interface ContractSaveTransaction extends TransactionSave {
 *   
 *   ContractDbQuery queryBuilder();
 *   ContractDbBuilder insertBuilder();
 *   ContractDbBuilder updateBuilder();
 *   ContractDbBuilder deleteBuilder();
 *   
 *   ContractSaveTransaction withTenantId(String tenantId);
 *   
 *   // Commit/rollback methods
 *   Uni<Void> commit();
 *   Uni<Void> rollback();
 * }
 * }</pre>
 * 
 * <h3>Key Features:</h3>
 * <ul>
 * <li>Extends base TransactionSave interface</li>
 * <li>Provides builder pattern access to query/insert/update/delete operations</li>
 * <li>Supports multi-tenancy with tenantId methods</li>
 * <li>Includes transaction lifecycle management (commit/rollback)</li>
 * <li>Type-safe access to domain-specific query builders</li>
 * </ul>
 * 
 * <h3>Usage Pattern:</h3>
 * <pre>{@code
 * ContractSaveTransaction tx = registry.createTransaction();
 * tx.queryBuilder()
 *   .contracts()
 *   .findByPartyId("party123")
 *   .execute()
 *   .flatMap(contracts -> {
 *     // Process contracts
 *     return tx.commit();
 *   });
 * }</pre>
 */

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import io.resys.thena.api.entities.Tenant;
import io.resys.thena.processor.model.RegistryMetamodel;
import io.resys.thena.processor.spi.RegistryCodeGenerator;
import io.resys.thena.spi.TenantDataSource;
import io.resys.thena.spi.TenantDataSource.TxScope;

public class Gen_Registry_DatabaseInterface implements RegistryCodeGenerator {
  
  public JavaFile generate(RegistryMetamodel registry) {
    final var className = registry.getName() + "Db";
    final var transactionInterfaceName = "Transaction";
    
    final var interfaceBuilder = TypeSpec.interfaceBuilder(className)
      .addModifiers(Modifier.PUBLIC)
      .addSuperinterface(ClassName.get(TenantDataSource.class));
    
    if(!registry.isTenantDisabled()) {
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
    }
    
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
      .addParameter(ParameterizedTypeName.get(
        ClassName.bestGuess(transactionInterfaceName),
        typeVarR
      ), "callback")
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
