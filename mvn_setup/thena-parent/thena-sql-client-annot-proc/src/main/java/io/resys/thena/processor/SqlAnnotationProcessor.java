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
import io.resys.thena.processor.codegen.Gen_Multi_BuilderImplementation;
import io.resys.thena.processor.codegen.Gen_Multi_BuilderInterface;
import io.resys.thena.processor.codegen.Gen_Registry_DatabaseImplementation;
import io.resys.thena.processor.codegen.Gen_Registry_DatabaseInterface;
import io.resys.thena.processor.codegen.Gen_Multi_InternalTenantQuery;
import io.resys.thena.processor.codegen.Gen_Registry_Exception;
import io.resys.thena.processor.codegen.Gen_Multi_QueryImplementation;
import io.resys.thena.processor.codegen.Gen_Multi_QueryInterface;
import io.resys.thena.processor.codegen.Gen_Multi_RegistryFactory;
import io.resys.thena.processor.codegen.Gen_Multi_TableNames;
import io.resys.thena.processor.codegen.Gen_Table_SqlImplementation;
import io.resys.thena.processor.model.MultiTableCodeGenerator;
import io.resys.thena.processor.model.RegistryCodeGenerator;
import io.resys.thena.processor.model.TableCodeGenerator;
import io.resys.thena.processor.model.ModelExtractor;
import io.resys.thena.processor.model.TableModel;
import io.resys.thena.processor.model.TableModel.RegistryModel;
import io.resys.thena.processor.support.NamingUtils;



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
      final var javaFile = generator.generate(registryConfig, filteredTables);
      javaFile.writeTo(processingEnv.getFiler());
      
      processingEnv.getMessager().printMessage(
        javax.tools.Diagnostic.Kind.NOTE,
        "Generated: " + javaFile.typeSpec.name + " using " + generator.getClass().getSimpleName() + 
        " with " + filteredTables.size() + " tables"
      );
    }
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
    
    final var domainName = NamingUtils.toCamelCaseCapitalized(annotation.name().toLowerCase());
    
    return RegistryModel.builder()
      .name(domainName)
      .tableClassName(domainName + "TableNames")
      .registryClassName(domainName + "Registry")
      .transactionContainerClassName(domainName + "TransactionContainer")
      .transactionSaveClassName(domainName + "SaveTransaction")
      .internalTenantQueryClassName(domainName + "DbInternalTenantQuery")
      .worldName(worldName)
      .packageName(packageName)
      .element(element)
      .nonTenantTables(List.of(annotation.nonTenantTables()))
      .build();
  }
  

  private void processTableInterface(TableModel model, RegistryModel registry) throws IOException {
    
    final TableCodeGenerator codeGenerator = new Gen_Table_SqlImplementation();
    final var javaFile = codeGenerator.generate(model, registry);
    javaFile.writeTo(processingEnv.getFiler());
    
    // Log success
    processingEnv.getMessager().printMessage(
      javax.tools.Diagnostic.Kind.NOTE,
      "Generated: " + javaFile.typeSpec.name + " using " + codeGenerator.getClass().getSimpleName()
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
