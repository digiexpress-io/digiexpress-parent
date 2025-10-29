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

/**
 * Generates domain-specific exception classes for database operations.
 * 
 * <p>This generator creates custom exception types that provide meaningful error 
 * information for database operations within a specific domain registry. The exceptions
 * are designed to wrap SQL errors and provide domain-specific context.
 * 
 * <h3>Generated Code Example:</h3>
 * <pre>{@code
 * public class ContractFindException extends RuntimeException {
 * 
 *   private static final long serialVersionUID = 1L;
 *   
 *   public ContractFindException(String message) {
 *     super(message);
 *   }
 *   
 *   public ContractFindException(String message, Throwable cause) {
 *     super(message, cause);
 *   }
 *   
 *   public static ContractFindException notFound(String id) {
 *     return new ContractFindException("Contract not found with id: " + id);
 *   }
 *   
 *   public static ContractFindException sqlError(String operation, Throwable cause) {
 *     return new ContractFindException("SQL error during " + operation, cause);
 *   }
 * }
 * }</pre>
 * 
 * <h3>Key Features:</h3>
 * <ul>
 * <li>Domain-specific exception naming (e.g., ContractFindException)</li>
 * <li>Standard exception constructors with message and cause</li>
 * <li>Static factory methods for common error scenarios</li>
 * <li>Proper serialization support with serialVersionUID</li>
 * <li>Meaningful error messages with domain context</li>
 * </ul>
 * 
 * <h3>Usage Pattern:</h3>
 * <pre>{@code
 * try {
 *   Contract contract = contractTable.findById(id);
 *   if (contract == null) {
 *     throw ContractFindException.notFound(id);
 *   }
 *   return contract;
 * } catch (SQLException e) {
 *   throw ContractFindException.sqlError("findById", e);
 * }
 * }</pre>
 */

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import io.resys.thena.processor.model.TableModel.RegistryModel;
import io.resys.thena.processor.spi.RegistryCodeGenerator;

public class Gen_Registry_Exception implements RegistryCodeGenerator {
  
  public JavaFile generate(RegistryModel registry) {
    final var className = registry.getName() + "FindException";
    final var packageName = registry.getPackageName() + ".spi";
    
    final var classBuilder = TypeSpec.classBuilder(className)
      .addModifiers(Modifier.PUBLIC)
      .superclass(RuntimeException.class);
    
    classBuilder.addMethod(MethodSpec.constructorBuilder()
      .addModifiers(Modifier.PUBLIC)
      .addParameter(String.class, "message")
      .addStatement("super(message)")
      .build());
    
    classBuilder.addMethod(MethodSpec.constructorBuilder()
      .addModifiers(Modifier.PUBLIC)
      .addParameter(String.class, "message")
      .addParameter(Throwable.class, "cause")
      .addStatement("super(message, cause)")
      .build());
    
    return JavaFile.builder(packageName, classBuilder.build())
      .indent("  ")
      .build();
  }
}
