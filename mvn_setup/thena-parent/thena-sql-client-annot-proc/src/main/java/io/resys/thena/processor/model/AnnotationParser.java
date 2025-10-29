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
import java.util.regex.Pattern;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import io.resys.thena.api.annotations.TenantSql;
import io.resys.thena.processor.model.TableMetamodel.MethodParameter;
import io.resys.thena.processor.model.TableMetamodel.SqlMethod;
import io.resys.thena.processor.model.TableMetamodel.SqlMethodType;
import io.resys.thena.processor.model.TableMetamodel.SqlPropsType;

public class AnnotationParser {
  private static final Pattern TABLE_NAME_PATTERN = Pattern.compile("\\{([a-zA-Z_][a-zA-Z0-9_]*)\\}");
  
  private final ProcessingEnvironment processingEnv;
  
  public AnnotationParser(ProcessingEnvironment processingEnv) {
    this.processingEnv = processingEnv;
  }
  
  public TableMetamodel extract(TypeElement interfaceElement) {
    // Step 1: Extract @TenantSql.Table annotation
    final var tableAnnotation = interfaceElement.getAnnotation(TenantSql.Table.class);
    
    // Step 2: Build basic table metadata
    final var interfaceName = interfaceElement.getSimpleName().toString();
    final var packageName = processingEnv.getElementUtils()
      .getPackageOf(interfaceElement)
      .getQualifiedName()
      .toString();
    final var implClassName = interfaceName + "Impl";
    final var tableName = tableAnnotation.name();
    
    // Step 3: Process all methods
    final var sqlMethods = new ArrayList<SqlMethod>();
    
    for (final var enclosed : interfaceElement.getEnclosedElements()) {
      if (enclosed.getKind() == ElementKind.METHOD) {
        final var method = (ExecutableElement) enclosed;
        
        // Check for all annotation types
        if (method.getAnnotation(TenantSql.Find.class) != null) {
          sqlMethods.add(extractFindMethod(method));
        } else if (method.getAnnotation(TenantSql.FindAll.class) != null) {
          sqlMethods.add(extractFindAllMethod(method));
        } else if (method.getAnnotation(TenantSql.Insert.class) != null) {
          sqlMethods.add(extractInsertMethod(method));
        } else if (method.getAnnotation(TenantSql.InsertAll.class) != null) {
          sqlMethods.add(extractInsertAllMethod(method));
        } else if (method.getAnnotation(TenantSql.Update.class) != null) {
          sqlMethods.add(extractUpdateMethod(method));
        } else if (method.getAnnotation(TenantSql.UpdateAll.class) != null) {
          sqlMethods.add(extractUpdateAllMethod(method));
        } else if (method.getAnnotation(TenantSql.Delete.class) != null) {
          sqlMethods.add(extractDeleteMethod(method));
        } else if (method.getAnnotation(TenantSql.DeleteAll.class) != null) {
          sqlMethods.add(extractDeleteAllMethod(method));
        }
      }
    }
    
    // Step 4: Generate lifecycle methods
    sqlMethods.addAll(generateLifecycleMethods(tableAnnotation));
    
    // Step 5: Build and return TableModel
    return TableMetamodel.builder()
      .interfaceName(interfaceName)
      .packageName(packageName)
      .implClassName(implClassName)
      .tableName(tableName)
      .order(tableAnnotation.order())
      .ddlSql(tableAnnotation.ddl())
      .constraintsSql(tableAnnotation.constraints())
      .dropSql(tableAnnotation.drop())
      .sqlMethods(sqlMethods)
      .build();
  }
  
  private SqlMethod extractFindMethod(ExecutableElement method) {
    final var annotation = method.getAnnotation(TenantSql.Find.class);
    final var sql = annotation.sql();
    
    return SqlMethod.builder()
      .type(SqlMethodType.SELECT)
      .methodName(method.getSimpleName().toString())
      .sqlTemplate(sql)
      .resolvedSql(sql)
      .mapperClassName(extractRowMapperClassName(annotation))
      .parameters(extractParameters(method))
      .returnType(extractEntityTypeFromMapper(annotation))
      .wrapperType(extractWrapperType(method.getReturnType()))
      .propsType(determinePropsType(method))
      .tableNames(extractTableNames(sql))
      .optional(annotation.optional())
      .sqlBuilderClassName(extractSqlBuilderClassName(annotation))
      .build();
  }
  
  private SqlMethod extractFindAllMethod(ExecutableElement method) {
    final var annotation = method.getAnnotation(TenantSql.FindAll.class);
    final var sql = annotation.sql();
    final var isMultiWrapper = annotation.wrapper() == TenantSql.WrapperType.MULTI;
    
    return SqlMethod.builder()
      .type(SqlMethodType.SELECT_ALL)
      .methodName(method.getSimpleName().toString())
      .sqlTemplate(sql)
      .resolvedSql(sql)
      .mapperClassName(extractRowMapperClassName(annotation))
      .parameters(extractParameters(method))
      .returnType(extractEntityTypeFromMapper(annotation))
      .wrapperType(extractWrapperType(method.getReturnType()))
      .propsType(determinePropsType(method))
      .tableNames(extractTableNames(sql))
      .multiWrapper(isMultiWrapper)
      .sqlBuilderClassName(extractSqlBuilderClassName(annotation))
      .build();
  }
  

  private String extractRowMapperClassName(TenantSql.Find mapperClass) {
    try {
      return mapperClass.rowMapper().getName();
    } catch (javax.lang.model.type.MirroredTypeException e) {
      return e.getTypeMirror().toString();
    }
  }
  
  private String extractRowMapperClassName(TenantSql.FindAll mapperClass) {
    try {
      return mapperClass.rowMapper().getName();
    } catch (javax.lang.model.type.MirroredTypeException e) {
      return e.getTypeMirror().toString();
    }
  }
  
  private TypeName extractEntityTypeFromMapper(TenantSql.Find annotation) {
    try {
      annotation.rowMapper();
      return null;
    } catch (javax.lang.model.type.MirroredTypeException e) {
      return extractGenericFromRowMapper(e.getTypeMirror());
    }
  }
  
  private TypeName extractEntityTypeFromMapper(TenantSql.FindAll annotation) {
    try {
      annotation.rowMapper();
      return null;
    } catch (javax.lang.model.type.MirroredTypeException e) {
      return extractGenericFromRowMapper(e.getTypeMirror());
    }
  }
  
  private TypeName extractGenericFromRowMapper(TypeMirror mapperType) {
    if (mapperType instanceof DeclaredType) {
      
      // Get superinterfaces to find RowMapper<T>
      for (final var interfaceType : processingEnv.getTypeUtils().directSupertypes(mapperType)) {
        if (interfaceType instanceof DeclaredType) {
          final var interfaceDeclared = (DeclaredType) interfaceType;
          final var typeArgs = interfaceDeclared.getTypeArguments();
          
          if (!typeArgs.isEmpty()) {
            // Found RowMapper<T>, return T
            return TypeName.get(typeArgs.get(0));
          }
        }
      }
    }
    return null;
  }
  
  private SqlMethod extractInsertMethod(ExecutableElement method) {
    final var annotation = method.getAnnotation(TenantSql.Insert.class);
    final var sql = annotation.sql();
    
    return SqlMethod.builder()
      .type(SqlMethodType.INSERT)
      .methodName(method.getSimpleName().toString())
      .sqlTemplate(sql)
      .resolvedSql(sql)
      .mapperClassName(extractPropsMapperClassName(annotation))
      .parameters(extractParameters(method))
      .returnType(null)
      .wrapperType(null)
      .propsType(SqlPropsType.SQL_TUPLE)
      .tableNames(extractTableNames(sql))
      .build();
  }
  
  private String extractPropsMapperClassName(TenantSql.Insert annotation) {
    try {
      return annotation.propsMapper().getName();
    } catch (javax.lang.model.type.MirroredTypeException e) {
      return e.getTypeMirror().toString();
    }
  }
  
  private SqlMethod extractInsertAllMethod(ExecutableElement method) {
    final var annotation = method.getAnnotation(TenantSql.InsertAll.class);
    final var sql = annotation.sql();
    
    return SqlMethod.builder()
      .type(SqlMethodType.INSERT_ALL)
      .methodName(method.getSimpleName().toString())
      .sqlTemplate(sql)
      .resolvedSql(sql)
      .mapperClassName(extractPropsMapperClassName(annotation))
      .parameters(extractParameters(method))
      .returnType(null)
      .wrapperType(null)
      .propsType(SqlPropsType.SQL_TUPLE_LIST)
      .tableNames(extractTableNames(sql))
      .build();
  }
  
  private String extractPropsMapperClassName(TenantSql.InsertAll annotation) {
    try {
      annotation.propsMapper();
      return null;
    } catch (javax.lang.model.type.MirroredTypeException e) {
      return e.getTypeMirror().toString();
    }
  }
  
  private SqlMethod extractUpdateMethod(ExecutableElement method) {
    final var annotation = method.getAnnotation(TenantSql.Update.class);
    final var sql = annotation.sql();
    
    return SqlMethod.builder()
      .type(SqlMethodType.UPDATE)
      .methodName(method.getSimpleName().toString())
      .sqlTemplate(sql)
      .resolvedSql(sql)
      .mapperClassName(extractPropsMapperClassName(annotation))
      .parameters(extractParameters(method))
      .returnType(null)
      .wrapperType(null)
      .propsType(SqlPropsType.SQL_TUPLE)
      .tableNames(extractTableNames(sql))
      .build();
  }

  private String extractPropsMapperClassName(TenantSql.Update annotation) {
    try {
      annotation.propsMapper();
      return null;
    } catch (javax.lang.model.type.MirroredTypeException e) {
      return e.getTypeMirror().toString();
    }
  }
  
  private SqlMethod extractUpdateAllMethod(ExecutableElement method) {
    final var annotation = method.getAnnotation(TenantSql.UpdateAll.class);
    final var sql = annotation.sql();
    
    return SqlMethod.builder()
      .type(SqlMethodType.UPDATE_ALL)
      .methodName(method.getSimpleName().toString())
      .sqlTemplate(sql)
      .resolvedSql(sql)
      .mapperClassName(extractPropsMapperClassName(annotation))
      .parameters(extractParameters(method))
      .returnType(null)
      .wrapperType(null)
      .propsType(SqlPropsType.SQL_TUPLE_LIST)
      .tableNames(extractTableNames(sql))
      .build();
  }
  
  private String extractPropsMapperClassName(TenantSql.UpdateAll annotation) {
    try {
      annotation.propsMapper();
      return null;
    } catch (javax.lang.model.type.MirroredTypeException e) {
      return e.getTypeMirror().toString();
    }
  }
  
  private SqlMethod extractDeleteMethod(ExecutableElement method) {
    final var annotation = method.getAnnotation(TenantSql.Delete.class);
    final var sql = annotation.sql();
    
    return SqlMethod.builder()
      .type(SqlMethodType.DELETE)
      .methodName(method.getSimpleName().toString())
      .sqlTemplate(sql)
      .resolvedSql(sql)
      .mapperClassName(extractPropsMapperClassName(annotation))
      .parameters(extractParameters(method))
      .returnType(null)
      .wrapperType(null)
      .propsType(SqlPropsType.SQL_TUPLE)
      .tableNames(extractTableNames(sql))
      .build();
  }
  
  private String extractPropsMapperClassName(TenantSql.Delete annotation) {
    try {
      annotation.propsMapper();
      return null;
    } catch (javax.lang.model.type.MirroredTypeException e) {
      return e.getTypeMirror().toString();
    }
  }
  
  private SqlMethod extractDeleteAllMethod(ExecutableElement method) {
    final var annotation = method.getAnnotation(TenantSql.DeleteAll.class);
    final var sql = annotation.sql();
    
    return SqlMethod.builder()
      .type(SqlMethodType.DELETE_ALL)
      .methodName(method.getSimpleName().toString())
      .sqlTemplate(sql)
      .resolvedSql(sql)
      .mapperClassName(extractPropsMapperClassName(annotation))
      .parameters(extractParameters(method))
      .returnType(null)
      .wrapperType(null)
      .propsType(SqlPropsType.SQL_TUPLE_LIST)
      .tableNames(extractTableNames(sql))
      .build();
  }
  
  private String extractPropsMapperClassName(TenantSql.DeleteAll annotation) {
    try {
      annotation.propsMapper();
      return null;
    } catch (javax.lang.model.type.MirroredTypeException e) {
      return e.getTypeMirror().toString();
    }
  }
  
  private List<SqlMethod> generateLifecycleMethods(TenantSql.Table annotation) {
    final var methods = new ArrayList<SqlMethod>();
    
    // CREATE_TABLE
    if (!annotation.ddl().isEmpty()) {
      methods.add(SqlMethod.builder()
        .type(SqlMethodType.CREATE_TABLE)
        .methodName("createTable")
        .sqlTemplate(annotation.ddl())
        .resolvedSql(annotation.ddl())
        .mapperClassName(null)
        .parameters(List.of())
        .returnType(null)
        .wrapperType(null)
        .propsType(SqlPropsType.SQL)
        .tableNames(extractTableNames(annotation.ddl()))
        .build());
    }
    
    // CREATE_CONSTRAINTS
    if (!annotation.constraints().isEmpty()) {
      methods.add(SqlMethod.builder()
        .type(SqlMethodType.CREATE_CONSTRAINTS)
        .methodName("createConstraints")
        .sqlTemplate(annotation.constraints())
        .resolvedSql(annotation.constraints())
        .mapperClassName(null)
        .parameters(List.of())
        .returnType(null)
        .wrapperType(null)
        .propsType(SqlPropsType.SQL)
        .tableNames(extractTableNames(annotation.constraints()))
        .build());
    }
    
    // DROP_TABLE
    if (!annotation.drop().isEmpty()) {
      methods.add(SqlMethod.builder()
        .type(SqlMethodType.DROP_TABLE)
        .methodName("dropTable")
        .sqlTemplate(annotation.drop())
        .resolvedSql(annotation.drop())
        .mapperClassName(null)
        .parameters(List.of())
        .returnType(null)
        .wrapperType(null)
        .propsType(SqlPropsType.SQL)
        .tableNames(extractTableNames(annotation.drop()))
        .build());
    }
    
    return methods;
  }
  
  private List<MethodParameter> extractParameters(ExecutableElement method) {
    final var parameters = new ArrayList<MethodParameter>();
    int position = 0;
    
    for (final var param : method.getParameters()) {
      parameters.add(MethodParameter.builder()
        .name(param.getSimpleName().toString())
        .type(TypeName.get(param.asType()))
        .position(position++)
        .build());
    }
    
    return parameters;
  }
  
  private TypeName extractWrapperType(TypeMirror returnType) {
    if (returnType instanceof DeclaredType) {
      final var declaredType = (DeclaredType) returnType;
      final var typeElement = (TypeElement) declaredType.asElement();
      final var qualifiedName = typeElement.getQualifiedName().toString();
      
      // Check if it's Uni or Multi
      if (qualifiedName.equals("io.smallrye.mutiny.Uni")) {
        return ClassName.get("io.smallrye.mutiny", "Uni");
      } else if (qualifiedName.equals("io.smallrye.mutiny.Multi")) {
        return ClassName.get("io.smallrye.mutiny", "Multi");
      }
    }
    
    return null;
  }
  
  private SqlPropsType determinePropsType(ExecutableElement method) {
    final List<? extends VariableElement> params = method.getParameters();
    
    // No parameters -> SQL
    if (params.isEmpty()) {
      return SqlPropsType.SQL;
    }
    
    // Check if first parameter is a Collection
    final var firstParamType = params.get(0).asType();
    if (firstParamType instanceof DeclaredType) {
      DeclaredType declaredType = (DeclaredType) firstParamType;
      TypeElement typeElement = (TypeElement) declaredType.asElement();
      String qualifiedName = typeElement.getQualifiedName().toString();
      
      if (qualifiedName.equals("java.util.Collection") || 
          qualifiedName.equals("java.util.List") ||
          qualifiedName.equals("java.util.Set")) {
        return SqlPropsType.SQL_TUPLE_LIST;
      }
    }
    
    // Has parameters but not Collection -> SQL_TUPLE
    return SqlPropsType.SQL_TUPLE;
  }
  
  private List<String> extractTableNames(String sql) {
    final var tableNames = new ArrayList<String>();
    final var matcher = TABLE_NAME_PATTERN.matcher(sql);
    
    while (matcher.find()) {
      final var tableName = matcher.group(1);
      if (!tableNames.contains(tableName)) {
        tableNames.add(tableName);
      }
    }
    
    return tableNames;
  }
  
  private String extractSqlBuilderClassName(TenantSql.Find annotation) {
    try {
      final var builderClass = annotation.sqlBuilder();
      if (builderClass.getName().equals("io.resys.thena.api.annotations.TenantSql$DefaultSqlBuilder")) {
        return null;
      }
      return builderClass.getName();
    } catch (javax.lang.model.type.MirroredTypeException e) {
      final var typeMirror = e.getTypeMirror().toString();
      if (typeMirror.equals("io.resys.thena.api.annotations.TenantSql.DefaultSqlBuilder")) {
        return null;
      }
      return typeMirror;
    }
  }
  
  private String extractSqlBuilderClassName(TenantSql.FindAll annotation) {
    try {
      final var builderClass = annotation.sqlBuilder();
      if (builderClass.getName().equals("io.resys.thena.api.annotations.TenantSql$DefaultSqlBuilder")) {
        return null;
      }
      return builderClass.getName();
    } catch (javax.lang.model.type.MirroredTypeException e) {
      final var typeMirror = e.getTypeMirror().toString();
      if (typeMirror.equals("io.resys.thena.api.annotations.TenantSql.DefaultSqlBuilder")) {
        return null;
      }
      return typeMirror;
    }
  }
}