package io.resys.thena.processor;

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

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;

import com.google.auto.service.AutoService;

import io.resys.thena.api.annotations.TenantSql;
import io.resys.thena.processor.codegen.Gen_Multi_BuilderImplementation;
import io.resys.thena.processor.codegen.Gen_Multi_BuilderInterface;
import io.resys.thena.processor.codegen.Gen_Multi_InternalTenantQuery;
import io.resys.thena.processor.codegen.Gen_Multi_QueryImplementation;
import io.resys.thena.processor.codegen.Gen_Multi_QueryInterface;
import io.resys.thena.processor.codegen.Gen_Multi_RegistryFactory;
import io.resys.thena.processor.codegen.Gen_Multi_TableNames;
import io.resys.thena.processor.codegen.Gen_Registry_DatabaseImplementation;
import io.resys.thena.processor.codegen.Gen_Registry_DatabaseInterface;
import io.resys.thena.processor.codegen.Gen_Registry_Exception;
import io.resys.thena.processor.codegen.Gen_Table_SqlImplementation;
import io.resys.thena.processor.model.Metamodel;
import io.resys.thena.processor.model.RegistryMetamodel;
import io.resys.thena.processor.model.TableMetamodel;
import io.resys.thena.processor.spi.MultiTableCodeGenerator;
import io.resys.thena.processor.spi.RegistryCodeGenerator;
import io.resys.thena.processor.spi.TableCodeGenerator;


@AutoService(Processor.class)
public class SqlAnnotationProcessor extends AbstractProcessor {

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latest();
  }
  
  @Override
  public Set<String> getSupportedAnnotationTypes() {
    return new HashSet<>(Arrays.asList(
        io.resys.thena.api.annotations.TenantSql.Table.class.getCanonicalName(),
        io.resys.thena.api.annotations.TenantSql.Registry.class.getCanonicalName()
    ));
  }
  
  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    
    // Build centralized metadata model
    final var metaModel = Metamodel.builder(processingEnv)
      .addAllRegistries(roundEnv.getElementsAnnotatedWith(TenantSql.Registry.class))
      .addAllTables(roundEnv.getElementsAnnotatedWith(TenantSql.Table.class))
      .build();
    
    // Process individual table generators
    for (final var table : metaModel.getTables()) {
      final var registry = metaModel.findRegistryForTable(table);
      if (registry != null) {
        try {
          processTableInterface(table, registry, metaModel);
        } catch (Exception e) {
          processingEnv.getMessager().printMessage(
            javax.tools.Diagnostic.Kind.ERROR,
            "Failed to process table " + table.getTableName() + ": " + e.getMessage()
          );
          e.printStackTrace();
        }
      }
    }
    
    // Process registry generators
    for (final var registry : metaModel.getRegistries()) {
      final var tables = metaModel.getTablesForRegistry(registry);
      try {
        processRegistry(registry, tables);
      } catch (Exception e) {
        processingEnv.getMessager().printMessage(
          javax.tools.Diagnostic.Kind.ERROR,
          "Failed to process registry " + registry.getName() + ": " + e.getMessage(),
          registry.getElement()
        );
        e.printStackTrace();
      }
    }
    
    return true; // We claim this annotation
  }
  
  private void processRegistry(RegistryMetamodel registryConfig, List<TableMetamodel> tableModels) throws IOException {
    // Generate registry if we have config
    if (tableModels.isEmpty()) {
      return;
    }
    
    
    // Registry-only generators (single RegistryModel parameter)
    final var registryGenerators = List.of(
      new Gen_Registry_DatabaseInterface(),
      new Gen_Registry_DatabaseImplementation(),
      new Gen_Registry_Exception()
    );
    
    // Multi-table generators (RegistryModel + List<TableModel> parameters)
    final var multiTableGenerators = List.of(
      new Gen_Multi_TableNames(),
      new Gen_Multi_RegistryFactory(),
      new Gen_Multi_QueryInterface(),
      new Gen_Multi_BuilderInterface(),
      new Gen_Multi_InternalTenantQuery(),
      new Gen_Multi_BuilderImplementation(),
      new Gen_Multi_QueryImplementation()
    );
    
    // Process registry-only generators
    for (RegistryCodeGenerator generator : registryGenerators) {
      final var javaFile = generator.generate(registryConfig);
      javaFile.writeTo(processingEnv.getFiler());
      
      processingEnv.getMessager().printMessage(
        javax.tools.Diagnostic.Kind.NOTE,
        "Generated: " + javaFile.typeSpec.name + " using " + generator.getClass().getSimpleName()
      );
    }
    
    // Process multi-table generators
    for (MultiTableCodeGenerator generator : multiTableGenerators) {
      final var javaFile = generator.generate(registryConfig, tableModels);
      javaFile.writeTo(processingEnv.getFiler());
      
      processingEnv.getMessager().printMessage(
        javax.tools.Diagnostic.Kind.NOTE,
        "Generated: " + javaFile.typeSpec.name + " using " + generator.getClass().getSimpleName() + 
        " with " + tableModels.size() + " tables"
      );
    }
  }
  
  
  
  

  private void processTableInterface(TableMetamodel model, RegistryMetamodel registry, Metamodel metaModel) throws IOException {
    
    final TableCodeGenerator codeGenerator = new Gen_Table_SqlImplementation(metaModel);
    final var javaFile = codeGenerator.generate(model, registry);
    javaFile.writeTo(processingEnv.getFiler());
    
    // Log success
    processingEnv.getMessager().printMessage(
      javax.tools.Diagnostic.Kind.NOTE,
      "Generated: " + javaFile.typeSpec.name + " using " + codeGenerator.getClass().getSimpleName()
    );
  }
  
}
