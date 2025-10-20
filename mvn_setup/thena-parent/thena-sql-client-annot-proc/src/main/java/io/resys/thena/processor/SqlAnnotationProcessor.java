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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;

import io.resys.thena.api.annotations.TenantSql;
import io.resys.thena.processor.codegen.DbBuilderInterfaceSqlCodeGenerator;
import io.resys.thena.processor.codegen.DbImplSqlCodeGenerator;
import io.resys.thena.processor.codegen.DbInterfaceSqlCodeGenerator;
import io.resys.thena.processor.codegen.DbInternalTenantQuerySqlCodeGenerator;
import io.resys.thena.processor.codegen.DbQueryInterfaceSqlCodeGenerator;
import io.resys.thena.processor.codegen.RegistryFactorySqlCodeGenerator;
import io.resys.thena.processor.codegen.TableNameSqlCodeGenerator;
import io.resys.thena.processor.codegen.TableSqlCodeGenerator;
import io.resys.thena.processor.codegen.TransactionBuilderSqlCodeGenerator;
import io.resys.thena.processor.codegen.TransactionSaveSqlCodeGenerator;
import io.resys.thena.processor.model.ModelExtractor;
import io.resys.thena.processor.model.TableModel;
import io.resys.thena.processor.model.TableModel.RegistryModel;



@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class SqlAnnotationProcessor extends AbstractProcessor {

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    return new HashSet<>(Arrays.asList(
        io.resys.thena.api.annotations.TenantSql.Table.class.getCanonicalName(),
        io.resys.thena.api.annotations.TenantSql.Registry.class.getCanonicalName()
    ));
  }
  
  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    
    // Step 0: Find all @TenantSql.Registry interfaces
    final var registryModels = new ArrayList<RegistryModel>();
    for (Element element : roundEnv.getElementsAnnotatedWith(TenantSql.Registry.class)) {
      if (element.getKind() == ElementKind.PACKAGE) {
        final var packageElement = (PackageElement) element;
        final var registryConfig = extractRegistryConfig(packageElement);
        if(registryConfig != null) {
          registryModels.add(registryConfig);
        }
      }
    }
    
    
    // Step 1: Find all @TenantSql.Table interfaces
    final var tableModels = new ArrayList<TableModel>();
    for (Element element : roundEnv.getElementsAnnotatedWith(TenantSql.Table.class)) {
      
      processingEnv.getMessager().printMessage(
          javax.tools.Diagnostic.Kind.NOTE,
          "Analysing SQL TABLE: " + element.getSimpleName()
        );
      
      // Step 2: Validate
      if (element.getKind() != ElementKind.INTERFACE) {
        // Log error and continue
        
        processingEnv.getMessager().printMessage(
            javax.tools.Diagnostic.Kind.ERROR,
            "Skipping expecting interface but got class: " + element.getSimpleName()
          );
        continue;
      }
      
      // Step 3: Process tables
      try {
        final var modelExtractor = new ModelExtractor(processingEnv);
        final var model = modelExtractor.extract((TypeElement) element);
        final var registry = findRegistryForTable(model, registryModels);
        
        if(registry == null) {
          processingEnv.getMessager().printMessage(
              javax.tools.Diagnostic.Kind.ERROR,
              "Failed to process @TenantSql.Table: " + model.getTableName() + " is skipped because there is no registry!",
              element
            );
        }
        
        processTableInterface(model, registry);
        tableModels.add(model);
        
      } catch (Exception e) {
        processingEnv.getMessager().printMessage(
            javax.tools.Diagnostic.Kind.ERROR,
            "Failed to process @TenantSql.Table: " + e.getMessage(),
            element
          );
          e.printStackTrace(); // Also print stack trace to console for debugging
      }
    }
    
    
    // Step 4: Process factories
    for (final var registryConfig : registryModels) {
      final var tables = filterTablesForRegistry(registryConfig, tableModels);
      try {
        processRegistry(registryConfig, tables);
      } catch (Exception e) {
        processingEnv.getMessager().printMessage(
            javax.tools.Diagnostic.Kind.ERROR,
            "Failed to process @TenantSql.Registry: " + e.getMessage(),
            registryConfig.getElement()
          );
          e.printStackTrace(); // Also print stack trace to console for debugging
      }      
    }
    return true; // We claim this annotation
  }
  
  private void processRegistry(RegistryModel registryConfig, List<TableModel> tableModels) throws IOException {
    // Step 3: Generate registry if we have config
    if (tableModels.isEmpty()) {
      return;
    }
    
    final var filteredTables = filterTablesForRegistry(registryConfig, tableModels);
    

    new TableNameSqlCodeGenerator()
      .generate(registryConfig, filteredTables)
      .writeTo(processingEnv.getFiler());
      
    processingEnv.getMessager().printMessage(
      javax.tools.Diagnostic.Kind.NOTE,
      "Generated names: " + registryConfig.getTableClassName() + " with " + 
      filteredTables.size() + " tables"
    );
  
    new RegistryFactorySqlCodeGenerator()
        .generate(registryConfig, tableModels)
        .writeTo(processingEnv.getFiler());
    
    processingEnv.getMessager().printMessage(
      javax.tools.Diagnostic.Kind.NOTE,
      "Generated registry: " + registryConfig.getRegistryClassName() + " with " + 
      filteredTables.size() + " tables"
    );
  
    
    new TransactionBuilderSqlCodeGenerator()
      .generate(registryConfig, tableModels)
      .writeTo(processingEnv.getFiler());
    
    processingEnv.getMessager().printMessage(
        javax.tools.Diagnostic.Kind.NOTE,
        "Generated container: " + registryConfig.getTransactionContainerClassName() + " with " + 
        filteredTables.size() + " tables");
  
  
    new TransactionSaveSqlCodeGenerator()
      .generate(registryConfig, tableModels)
      .writeTo(processingEnv.getFiler());
    
    processingEnv.getMessager().printMessage(
        javax.tools.Diagnostic.Kind.NOTE,
        "Generated save: " + registryConfig.getTransactionSaveClassName() + " with " + 
        filteredTables.size() + " tables");
    
    
    new DbInterfaceSqlCodeGenerator()
      .generate(registryConfig)
      .writeTo(processingEnv.getFiler());
  
    processingEnv.getMessager().printMessage(
        javax.tools.Diagnostic.Kind.NOTE,
        "Generated db interface: " + registryConfig.getTransactionSaveClassName() + " with " + 
        filteredTables.size() + " tables");
    
    
    new DbQueryInterfaceSqlCodeGenerator()
      .generate(registryConfig, filteredTables)
      .writeTo(processingEnv.getFiler());
  
    processingEnv.getMessager().printMessage(
        javax.tools.Diagnostic.Kind.NOTE,
        "Generated db query interface: " + registryConfig.getName() + "DbQuery with " + 
        filteredTables.size() + " tables");
    
    
    new DbBuilderInterfaceSqlCodeGenerator()
      .generate(registryConfig, filteredTables)
      .writeTo(processingEnv.getFiler());
  
    processingEnv.getMessager().printMessage(
        javax.tools.Diagnostic.Kind.NOTE,
        "Generated db builder interface: " + registryConfig.getName() + "DbBuilder with " + 
        filteredTables.size() + " tables");
    
    
    new DbImplSqlCodeGenerator()
      .generate(registryConfig)
      .writeTo(processingEnv.getFiler());
  
    processingEnv.getMessager().printMessage(
        javax.tools.Diagnostic.Kind.NOTE,
        "Generated db impl: " + registryConfig.getName() + "DbImpl");
    
    
    new DbInternalTenantQuerySqlCodeGenerator()
      .generate(registryConfig, filteredTables)
      .writeTo(processingEnv.getFiler());
  
    processingEnv.getMessager().printMessage(
        javax.tools.Diagnostic.Kind.NOTE,
        "Generated internal tenant query: " + registryConfig.getName() + "DbInternalTenantQuery");
    
  }
  
  
  private List<TableModel> filterTablesForRegistry(RegistryModel config, List<TableModel> allTables) {
    final var registryPackage = config.getPackageName();
    
    return allTables.stream()
      .filter(table -> {
        final var tablePackage = table.getPackageName();
        // Include if same package or child package
        return tablePackage.equals(registryPackage) || 
               tablePackage.startsWith(registryPackage + ".");
      })
      .collect(Collectors.toList());
  }
  
  private RegistryModel extractRegistryConfig(Element element) {
    if (element.getKind() != ElementKind.PACKAGE) {
      processingEnv.getMessager().printMessage(
        javax.tools.Diagnostic.Kind.ERROR,
        "@TenantSql.Registry can only be applied to packages (via package-info.java)",
        element
      );
      return null;
    }
    
    final var packageElement = (PackageElement) element;
    final var annotation = element.getAnnotation(TenantSql.Registry.class);
    final var packageName = packageElement.getQualifiedName().toString();
    
    if(packageName == null) {
      return null;
    }
    
    final var worldName = annotation.worldName() != null && !annotation.worldName().isEmpty() 
      ? annotation.worldName() 
      : "World";
    
    return RegistryModel.builder()
      .name(annotation.name())
      .tableClassName(annotation.name() + "TableNames")
      .registryClassName(annotation.name() + "Registry")
      .transactionContainerClassName(annotation.name() + "TransactionContainer")
      .transactionSaveClassName(annotation.name() + "SaveTransaction")
      .internalTenantQueryClassName(annotation.name() + "DbInternalTenantQuery")
      .worldName(worldName)
      .packageName(packageName)
      .element(element)
      .nonTenantTables(List.of(annotation.nonTenantTables()))
      .build();
  }
  

  private void processTableInterface(TableModel model, RegistryModel registry) throws IOException {
    
    final var codeGenerator = new TableSqlCodeGenerator();
    final var javaFile = codeGenerator.generate(model, registry);
    javaFile.writeTo(processingEnv.getFiler());
    
    
    
    // Log success
    processingEnv.getMessager().printMessage(
      javax.tools.Diagnostic.Kind.NOTE,
      "Generated implementation: " + model.getImplClassName()
    );
  }
  
  private RegistryModel findRegistryForTable(TableModel table, List<RegistryModel> registries) {
    final var tablePackage = table.getPackageName();
    
    // Find registry where table is in same package or child package
    return registries.stream()
      .filter(registry -> {
        final var registryPackage = registry.getPackageName();
        return tablePackage.equals(registryPackage) || 
               tablePackage.startsWith(registryPackage + ".");
      })
      .findFirst()
      .orElse(null);
  }
}
