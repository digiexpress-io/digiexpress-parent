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

import java.util.Optional;

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import io.resys.thena.api.actions.TenantActions.TenantOperationStatus;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.api.entities.Tenant.StructureType;
import io.resys.thena.datasource.TenantCacheImpl;
import io.resys.thena.datasource.TenantContext;
import io.resys.thena.datasource.ThenaDataSource;
import io.resys.thena.datasource.ThenaSqlDataSource;
import io.resys.thena.datasource.ThenaSqlDataSource.TenantCache;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler;
import io.resys.thena.datasource.ThenaSqlDataSourceImpl;
import io.resys.thena.datasource.vertx.ThenaSqlPoolVertx;
import io.resys.thena.processor.model.RegistryMetamodel;
import io.resys.thena.processor.spi.RegistryCodeGenerator;
import io.resys.thena.spi.InternalAliasQueryImpl;
import io.resys.thena.spi.InternalMemberQueryImpl;
import io.resys.thena.spi.TenantActionsImpl;
import io.resys.thena.spi.TenantDataSource.InternalAliasQuery;
import io.resys.thena.spi.TenantDataSource.InternalMemberQuery;
import io.resys.thena.spi.TenantDataSource.InternalTenantQuery;
import io.resys.thena.spi.TenantDataSource.TxScope;
import io.resys.thena.spi.TenantException;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;

public class Gen_Registry_DatabaseImplementation implements RegistryCodeGenerator {
  
  public JavaFile generate(RegistryMetamodel registry) {
    final var className = registry.getName() + "DbImpl";
    final var interfaceName = registry.getName() + "Db";
    final var packageName = registry.getPackageName() + ".spi";
    
    final var classBuilder = TypeSpec.classBuilder(className)
      .addModifiers(Modifier.PUBLIC)
      .addSuperinterface(ClassName.get(registry.getPackageName(), interfaceName))
      .addAnnotation(AnnotationSpec.builder(ClassName.get("lombok", "RequiredArgsConstructor")).build())
      .addAnnotation(AnnotationSpec.builder(ClassName.get("lombok.extern.slf4j", "Slf4j")).build());
    
    classBuilder.addField(FieldSpec.builder(
        ClassName.get(ThenaSqlDataSource.class),
        "dataSource",
        Modifier.PRIVATE, Modifier.FINAL
      ).build());
    
    classBuilder.addMethod(generateGetDataSource());
    
    classBuilder.addMethod(generateTenant(registry));
    classBuilder.addMethod(generateAlias(registry));
    classBuilder.addMethod(generateMember(registry));
    
    if(registry.isTenantDisabled()) {
      // do nothing
      
    } else {
      classBuilder.addMethod(generateWithTenantString(registry, interfaceName));
      classBuilder.addMethod(generateWithTenantObject(registry, className, interfaceName));
      classBuilder.addMethod(generateWithTenantDefault(registry, interfaceName));
      classBuilder.addMethod(generateTenantNotFound());
      classBuilder.addMethod(generateCreateIfNot(registry, interfaceName));      
    }
    

    classBuilder.addMethod(generateWithTransaction(registry, className, interfaceName));
    classBuilder.addMethod(generateQuery(registry));
    classBuilder.addMethod(generateBuilder(registry));
    
    
    classBuilder.addMethod(generateStaticCreateWithParams(className));
    classBuilder.addMethod(generateStaticCreate());
    classBuilder.addType(generateBuilderClass(className, registry));
    
    return JavaFile.builder(packageName, classBuilder.build())
      .indent("  ")
      .build();
  }
  
  private MethodSpec generateGetDataSource() {
    return MethodSpec.methodBuilder("getDataSource")
      .addAnnotation(Override.class)
      .addModifiers(Modifier.PUBLIC)
      .returns(ClassName.get(ThenaDataSource.class))
      .addStatement("return dataSource")
      .build();
  }
  
  private MethodSpec generateTenant(RegistryMetamodel registry) {
    final var method = MethodSpec.methodBuilder("tenant")
      .addAnnotation(Override.class)
      .addModifiers(Modifier.PUBLIC)
      .returns(ClassName.get(InternalTenantQuery.class));
      
    if(registry.isTenantDisabled()) {
      method.addStatement("throw new $T(\"Tenant is disabled\")", RuntimeException.class);
    } else {
      method.addStatement("return new $T(dataSource)", ClassName.bestGuess(registry.getInternalTenantQueryClassName()));
    }
    
    return method.build();
  }
  
  private MethodSpec generateAlias(RegistryMetamodel registry) {
    final var method = MethodSpec.methodBuilder("alias")
      .addAnnotation(Override.class)
      .addModifiers(Modifier.PUBLIC)
      .returns(ClassName.get(InternalAliasQuery.class));
    
    if(registry.isTenantDisabled()) {
      method.addStatement("throw new $T(\"Tenant is disabled\")", RuntimeException.class);
    } else {
      method.addStatement("return new $T(dataSource)", InternalAliasQueryImpl.class);
    }
    
    return method.build();
  }
  
  private MethodSpec generateMember(RegistryMetamodel registry) {
    final var method = MethodSpec.methodBuilder("member")
      .addAnnotation(Override.class)
      .addModifiers(Modifier.PUBLIC)
      .returns(ClassName.get(InternalMemberQuery.class));

    if(registry.isTenantDisabled()) {
      method.addStatement("throw new $T(\"Tenant is disabled\")", RuntimeException.class);
    } else {
      method.addStatement("return new $T(dataSource)", InternalMemberQueryImpl.class);
    }
    return method.build();
  }
  private MethodSpec generateWithTenantString(RegistryMetamodel registry, String interfaceName) {
    return MethodSpec.methodBuilder("withTenant")
      .addAnnotation(Override.class)
      .addModifiers(Modifier.PUBLIC)
      .addParameter(String.class, "tenantId")
      .returns(ParameterizedTypeName.get(
        ClassName.get(Uni.class),
        ClassName.get(registry.getPackageName(), interfaceName)
      ))
      .addCode(CodeBlock.builder()
        .add("return tenant().getByNameOrId(tenantId).onItem().transformToUni(tenant -> {\n")
        .indent()
        .add("if(tenant == null) {\n")
        .indent()
        .add("return tenantNotFound(tenantId);\n")
        .unindent()
        .add("}\n")
        .add("return $T.createFrom().item(withTenant(tenant));\n", ClassName.get(Uni.class))
        .unindent()
        .add("});\n")
        .build())
      .build();
  }
  
  private MethodSpec generateWithTenantObject(RegistryMetamodel registry, String className, String interfaceName) {
    return MethodSpec.methodBuilder("withTenant")
      .addAnnotation(Override.class)
      .addModifiers(Modifier.PUBLIC)
      .addParameter(ClassName.get(Tenant.class), "tenant")
      .returns(ClassName.get(registry.getPackageName(), interfaceName))
      .addStatement("return new $T(dataSource.withTenant(tenant))", ClassName.bestGuess(className))
      .build();
  }
  
  private MethodSpec generateWithTenantDefault(RegistryMetamodel registry, String interfaceName) {
    return MethodSpec.methodBuilder("withTenant")
      .addAnnotation(Override.class)
      .addModifiers(Modifier.PUBLIC)
      .returns(ParameterizedTypeName.get(
        ClassName.get(Uni.class),
        ClassName.get(registry.getPackageName(), interfaceName)
      ))
      .addCode(CodeBlock.builder()
        .add("if(this.dataSource.isTenantLoaded()) {\n")
        .indent()
        .add("return $T.createFrom().item(this);\n", ClassName.get(Uni.class))
        .unindent()
        .add("}\n")
        .add("return this.withTenant(this.dataSource.getTenant().getName());\n")
        .build())
      .build();
  }
  
  private MethodSpec generateWithTransaction(
      RegistryMetamodel registry, String className, String interfaceName
  ) {
    
    
    final CodeBlock main;
    
    if(registry.isTenantDisabled()) {
      main = CodeBlock.builder()
          .add("final var source = this.dataSource;\n")
          .add("return source.getPool().withTransaction(conn -> callback.apply(new $T(source.withTx(conn))));\n", ClassName.bestGuess(className))
          .build();     
    } else {
      main = CodeBlock.builder()
        .add("return withTenant(scope.getTenantId()).onItem().transformToUni(state -> {\n")
        .indent()
        .add("final var source = ($T) state.getDataSource();\n", ClassName.get(ThenaSqlDataSource.class))
        .add("return source.getPool().withTransaction(conn -> callback.apply(new $T(source.withTx(conn))));\n", ClassName.bestGuess(className))
        .unindent()
        .add("});\n")
        .build();      
    }
    
    
    final var typeVarR = TypeVariableName.get("R");
    return MethodSpec.methodBuilder("withTransaction")
      .addAnnotation(Override.class)
      .addModifiers(Modifier.PUBLIC)
      .addTypeVariable(typeVarR)
      .addParameter(ClassName.get(TxScope.class), "scope")
      .addParameter(ParameterizedTypeName.get(
        ClassName.get(registry.getPackageName(), interfaceName, "Transaction"),
        typeVarR
      ), "callback")
      .returns(ParameterizedTypeName.get(
        ClassName.get(Uni.class),
        typeVarR
      ))
      .addCode(main)
      .build();
  }
  
  private MethodSpec generateQuery(RegistryMetamodel registry) {
    return MethodSpec.methodBuilder("query")
      .addAnnotation(Override.class)
      .addModifiers(Modifier.PUBLIC)
      .returns(ClassName.get(registry.getPackageName(), registry.getName() + "DbQuery"))
      .addStatement("return new $T(dataSource)", 
        ClassName.get(registry.getPackageName() + ".spi", registry.getName() + "DbQueryImpl"))
      .build();
  }
  
  private MethodSpec generateBuilder(RegistryMetamodel registry) {
    return MethodSpec.methodBuilder("builder")
      .addAnnotation(Override.class)
      .addModifiers(Modifier.PUBLIC)
      .returns(ClassName.get(registry.getPackageName(), registry.getName() + "DbBuilder"))
      .addStatement("return new $T(dataSource)", 
        ClassName.get(registry.getPackageName() + ".spi", registry.getName() + "DbBuilderImpl"))
      .build();
  }
  
  private MethodSpec generateCreateIfNot(RegistryMetamodel registry, String interfaceName) {
    return MethodSpec.methodBuilder("createIfNot")
      .addModifiers(Modifier.PUBLIC)
      .returns(ParameterizedTypeName.get(
        ClassName.get(Uni.class),
        ClassName.get(registry.getPackageName(), interfaceName)
      ))
      .addCode(CodeBlock.builder()
        .add("return tenant().findByNameOrId(this.dataSource.getTenant().getName())\n")
        .indent()
        .add(".onItem().transformToUni(repo -> {\n")
        .indent()
        .add("if(repo.isEmpty()) {\n")
        .indent()
        .add("return new $T(this, $T.$L)\n", 
          ClassName.get(TenantActionsImpl.class),
          ClassName.get(StructureType.class),
          registry.getTenantType())
        .indent()
        .add(".createOneTenant()\n")
        .add(".name(this.dataSource.getTenant().getName())\n")
        .add(".build().onItem().transform(commit -> {\n")
        .indent()
        .add("if(commit.getStatus() != $T.OK) {\n", ClassName.get(TenantOperationStatus.class))
        .indent()
        .add("final var msg = $T.join(\",\", commit.getMessages().stream().map(e -> e.getText()).toList());\n", String.class)
        .add("final var ex = commit.getMessages().stream().map(e -> e.getException()).filter(e -> e != null).toList();\n")
        .add("throw new $T(\"Failed to create tenant: \" + msg, ex);\n", TenantException.class)
        .unindent()
        .add("}\n")
        .add("return withTenant(commit.getRepo());\n")
        .unindent()
        .add("});\n")
        .unindent()
        .unindent()
        .add("}\n")
        .add("return $T.createFrom().item(withTenant(repo.get()));\n", ClassName.get(Uni.class))
        .unindent()
        .add("});\n")
        .unindent()
        .build())
      .build();
  }
  
  private MethodSpec generateTenantNotFound() {
    return MethodSpec.methodBuilder("tenantNotFound")
      .addModifiers(Modifier.PRIVATE)
      .addTypeVariable(TypeVariableName.get("T"))
      .addParameter(String.class, "tenantId")
      .returns(ParameterizedTypeName.get(
        ClassName.get(Uni.class),
        TypeVariableName.get("T")
      ))
      .addCode(CodeBlock.builder()
        .add("return tenant().findAll().collect().asList().onItem().transform(tenants -> {\n")
        .indent()
        .add("final var text = new $T()\n", StringBuilder.class)
        .indent()
        .add(".append(\"Tenant with name: '\").append(tenantId).append(\"' does not exist!\")\n")
        .add(".append(\" known tenants: '\").append($T.join(\",\", tenants.stream().map(r -> r.getName()).toList())).append(\"'\")\n", String.class)
        .add(".toString();\n")
        .unindent()
        .add("log.error(text);\n")
        .add("throw new $T(text);\n", RuntimeException.class)
        .unindent()
        .add("});\n")
        .build())
      .build();
  }
  
  private MethodSpec generateStaticCreateWithParams(String className) {
    return MethodSpec.methodBuilder("create")
      .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
      .addParameter(ClassName.get(TenantContext.class), "names")
      .addParameter(ClassName.get("io.vertx.mutiny.sqlclient", "Pool"), "client")
      .addParameter(ClassName.get(TenantCache.class), "tenantCache")
      .addParameter(ClassName.get(ThenaSqlDataSourceErrorHandler.class), "errorHandler")
      
      .returns(ClassName.bestGuess(className))
      .addStatement("final var pool = new $T(client)", ClassName.get(ThenaSqlPoolVertx.class))
      .addCode(CodeBlock.builder()
        .add("final var dataSource = new $T(\n", ClassName.get(ThenaSqlDataSourceImpl.class))
        .indent()
        .add("\"\", names, pool, errorHandler,\n")
        .add("$T.empty(),\n", Optional.class)
        .add("tenantCache\n")
        .unindent()
        .add(");\n")
        .build())
      .addStatement("return new $T(dataSource)", ClassName.bestGuess(className))
      .build();
  }
  
  private MethodSpec generateStaticCreate() {
    return MethodSpec.methodBuilder("create")
      .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
      .returns(ClassName.bestGuess("Builder"))
      .addStatement("return new Builder()")
      .build();
  }
  
  private TypeSpec generateBuilderClass(String className, RegistryMetamodel registry) {
    final var builder = TypeSpec.classBuilder("Builder")
      .addModifiers(Modifier.PUBLIC, Modifier.STATIC);
    
    builder.addField(ClassName.get("io.vertx.mutiny.sqlclient", "Pool"), "client", Modifier.PRIVATE);
    
    builder.addField(
      FieldSpec
        .builder(registry.isTenantDisabled() ? Tenant.class : String.class, "db", Modifier.PRIVATE)
        .initializer("$T.unknown()", ClassName.get(Tenant.class) )
        .build()
    );
    
    builder.addField(ClassName.get(ThenaSqlDataSourceErrorHandler.class), "errorHandler", Modifier.PRIVATE);
    builder.addField(ClassName.get(TenantCache.class), "tenantCache", Modifier.PRIVATE);
    
    builder.addMethod(MethodSpec.methodBuilder("errorHandler")
      .addModifiers(Modifier.PUBLIC)
      .addParameter(ClassName.get(ThenaSqlDataSourceErrorHandler.class), "errorHandler")
      .returns(ClassName.bestGuess("Builder"))
      .addStatement("this.errorHandler = errorHandler")
      .addStatement("return this")
      .build());
    
    builder.addMethod(MethodSpec.methodBuilder("tenant")
        .addModifiers(Modifier.PUBLIC)
        .addParameter(registry.isTenantDisabled() ? Tenant.class : String.class, "db")
        .returns(ClassName.bestGuess("Builder"))
        .addStatement("this.db = db")
        .addStatement("return this")
        .build());
    
    builder.addMethod(MethodSpec.methodBuilder("tenantCache")
      .addModifiers(Modifier.PUBLIC)
      .addParameter(ClassName.get(TenantCache.class), "tenantCache")
      .returns(ClassName.bestGuess("Builder"))
      .addStatement("this.tenantCache = tenantCache")
      .addStatement("return this")
      .build());
    
    builder.addMethod(MethodSpec.methodBuilder("client")
      .addModifiers(Modifier.PUBLIC)
      .addParameter(ClassName.get("io.vertx.mutiny.sqlclient", "Pool"), "client")
      .returns(ClassName.bestGuess("Builder"))
      .addStatement("this.client = client")
      .addStatement("return this")
      .build());
    
    // main build method
    final var buildMethod = MethodSpec.methodBuilder("build")
      .addModifiers(Modifier.PUBLIC)
      .returns(ClassName.bestGuess(className))
      .addStatement("$T.notNull(client, () -> \"client must be defined!\")", ClassName.get(RepoAssert.class))
      .addStatement("$T.notNull(errorHandler, () -> \"errorHandler must be defined!\")", ClassName.get(RepoAssert.class))
      .addCode("\n")
      .addStatement("final var tenantCache = this.tenantCache == null ? new $T() : this.tenantCache", ClassName.get(TenantCacheImpl.class))
      .addStatement("final var pool = new $T(client)", ClassName.get(ThenaSqlPoolVertx.class))
      .addCode("\n");
    
    
    if(registry.isTenantDisabled() ) {
      buildMethod
        .addStatement("final var db = this.db")
        .addStatement("final var ctx = $T.defaults(\"\")", ClassName.get(TenantContext.class));
    } else {
      buildMethod
        .addStatement("$T.notNull(db, () -> \"db must be defined!\")", ClassName.get(RepoAssert.class))
        .addStatement("final var ctx = $T.defaults(db)", ClassName.get(TenantContext.class));      
    }
    
    
    buildMethod
        .addCode(CodeBlock.builder()
          .add("final var dataSource = new $T(\n", ClassName.get(ThenaSqlDataSourceImpl.class))
          .indent()
          .add("db, ctx, pool, errorHandler,\n")
          .add("$T.empty(),\n", Optional.class)
          .add("tenantCache\n")
          .unindent()
          .add(");\n")
          .build())
        .addStatement("return new $T(dataSource)", ClassName.bestGuess(className));
    
    builder.addMethod(buildMethod.build());
    
    return builder.build();
  }
}
