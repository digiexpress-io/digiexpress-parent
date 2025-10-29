package io.resys.thena.processor.model;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;

import io.resys.thena.api.annotations.TenantSql;
import io.resys.thena.processor.support.NamingUtils;
import lombok.Value;

@Value
public class Metamodel {
  List<RegistryMetamodel> registries;
  List<TableMetamodel> tables;
  
  public static MetaModelBuilder builder(ProcessingEnvironment processingEnv) {
    return new MetaModelBuilder(processingEnv);
  }
  
  /**
   * Get tables that belong to a specific registry
   */
  public List<TableMetamodel> getTablesForRegistry(RegistryMetamodel registry) {
    final var registryPackage = registry.getPackageName();
    
    return tables.stream()
      .filter(table -> {
        final var tablePackage = table.getPackageName();
        // Include if same package or child package
        return tablePackage.equals(registryPackage) || 
               tablePackage.startsWith(registryPackage + ".");
      })
      .collect(Collectors.toList());
  }
  
  /**
   * Find the registry that owns a specific table
   */
  public RegistryMetamodel findRegistryForTable(TableMetamodel table) {
    final var tablePackage = table.getPackageName();
    
    return registries.stream()
      .filter(registry -> {
        final var registryPackage = registry.getPackageName();
        return tablePackage.equals(registryPackage) || 
               tablePackage.startsWith(registryPackage + ".");
      })
      .findFirst()
      .orElse(null);
  }
  
  public static class MetaModelBuilder {
    private final ProcessingEnvironment processingEnv;
    private final List<RegistryMetamodel> registries = new ArrayList<>();
    private final List<TableMetamodel> tables = new ArrayList<>();
    
    public MetaModelBuilder(ProcessingEnvironment processingEnv) {
      this.processingEnv = processingEnv;
    }
    
    public MetaModelBuilder addAllRegistries(Set<? extends Element> registryElements) {
      for (Element element : registryElements) {
        try {
          final var registry = extractRegistryConfig(element);
          if (registry != null) {
            registries.add(registry);
            
            processingEnv.getMessager().printMessage(
              javax.tools.Diagnostic.Kind.NOTE,
              "Extracted registry: " + registry.getName() + " from package " + registry.getPackageName()
            );
          }
        } catch (Exception e) {
          processingEnv.getMessager().printMessage(
            javax.tools.Diagnostic.Kind.ERROR,
            "Failed to extract registry from " + element + ": " + e.getMessage(),
            element
          );
        }
      }
      return this;
    }
    
    public MetaModelBuilder addAllTables(Set<? extends Element> tableElements) {
      for (Element element : tableElements) {
        try {
          final var table = extractTableModel(element);
          if (table != null) {
            tables.add(table);
            
            processingEnv.getMessager().printMessage(
              javax.tools.Diagnostic.Kind.NOTE,
              "Extracted table: " + table.getTableName() + " from interface " + table.getInterfaceName()
            );
          }
        } catch (Exception e) {
          processingEnv.getMessager().printMessage(
            javax.tools.Diagnostic.Kind.ERROR,
            "Failed to extract table from " + element + ": " + e.getMessage(),
            element
          );
        }
      }
      return this;
    }
    
    public Metamodel build() {
      // Validate that all tables have corresponding registries
      for (TableMetamodel table : tables) {
        final var registry = findRegistryForTable(table);
        if (registry == null) {
          processingEnv.getMessager().printMessage(
            javax.tools.Diagnostic.Kind.ERROR,
            "Table " + table.getTableName() + " has no corresponding registry in package hierarchy"
          );
        }
      }
      
      return new Metamodel(registries, tables);
    }
    
    private RegistryMetamodel extractRegistryConfig(Element element) {
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
      
      if (packageName == null) {
        return null;
      }
      
      final var worldName = annotation.worldName() != null && !annotation.worldName().isEmpty() 
        ? annotation.worldName() 
        : "World";
      
      final var domainName = NamingUtils.toCamelCaseCapitalized(annotation.name().toLowerCase());
      
      return RegistryMetamodel.builder()
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
    
    private TableMetamodel extractTableModel(Element element) {
      // Validate element type
      if (element.getKind() != ElementKind.INTERFACE) {
        processingEnv.getMessager().printMessage(
          javax.tools.Diagnostic.Kind.ERROR,
          "Skipping expecting interface but got " + element.getKind() + ": " + element.getSimpleName(),
          element
        );
        return null;
      }
      
      // Extract table model using existing ModelExtractor
      final var modelExtractor = new AnnotationParser(processingEnv);
      return modelExtractor.extract((TypeElement) element);
    }
    
    private RegistryMetamodel findRegistryForTable(TableMetamodel table) {
      final var tablePackage = table.getPackageName();
      
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
}