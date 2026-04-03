package io.resys.limaone.spi.bundler;

/*-
 * #%L
 * limaone-compiler
 * %%
 * Copyright (C) 2015 - 2026 Copyright 2022 ReSys OÜ
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
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import io.resys.limaone.ast.AST_Parser.Dependency_AST;
import io.resys.limaone.ast.ImmutableDependency_AST;
import io.resys.limaone.ast.Simple_AST;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.ModelError;
import io.resys.limaone.program.ImmutableProgramAssociation;
import io.resys.limaone.program.Program.ProgramStatus;
import io.resys.limaone.spi.compiler.CompilableUnit.Artifact;
import io.resys.limaone.spi.compiler.ImmutableArtifact;

public class ArtifactBuilder {
  private String artifactId;
  private String artifactName;
  private Model.BodyType artifactType;

  private final List<Dependency_AST> childDeps = new ArrayList<>();
  private final List<Dependency_AST> parentDeps = new ArrayList<>();
  private final List<ModelError> errors = new ArrayList<>();
  

  private Simple_AST ast;
  @SuppressWarnings("unused")
  private ProgramStatus programStatus;

  public ArtifactBuilder ast(Simple_AST ast) {
    this.ast = Objects.requireNonNull(ast, () -> "ast must be defined");
    return this;
  }
  // Fluent setters
  public ArtifactBuilder artifactId(String artifactId) {
    this.artifactId = Objects.requireNonNull(artifactId, () -> "artifactId must be defined");
    return this;
  }

  public ArtifactBuilder artifactName(String artifactName) {
    this.artifactName = Objects.requireNonNull(artifactName, () -> "artifactName must be defined");
    return this;
  }

  public ArtifactBuilder artifactType(Model.BodyType artifactType) {
    this.artifactType = Objects.requireNonNull(artifactType, () -> "artifactType must be defined");
    return this;
  }

  // Mutable list operations
  public ArtifactBuilder addChildDep(Dependency_AST dep) {
    this.childDeps.add(dep);
    return this;
  }

  public ArtifactBuilder addError(ModelError error) {
    this.errors.add(error);
    return this;
  }

  public ArtifactBuilder clearErrors() {
    this.errors.clear();
    return this;
  }

  // Direct list access for complex operations
  public List<ModelError> getErrors() {
    return errors; // Mutable access
  }

  public List<Dependency_AST> getChildDeps() {
    return childDeps; // Mutable access
  }

  public ArtifactBuilder programStatus(ProgramStatus status) {
    this.programStatus = status;
    return this;
  }
  
  public void init() {
    Objects.requireNonNull(ast, () -> "ast must be defined");
    Objects.requireNonNull(artifactId, () -> "artifactId must be defined");
    Objects.requireNonNull(artifactName, () -> "artifactName must be defined");
    Objects.requireNonNull(artifactType, () -> "artifactType must be defined");
  }

  public Artifact build() {
    final var builder = ImmutableArtifact.builder();
    
    for(final var child : childDeps) {
      if(child.getArtifactAst().isEmpty()) {
        continue;
      }
      final var refType = child.getArtifactAst().map(e -> e.getBodyType());
      final var errors = child.getArtifactAst().map(ast -> ast.getErrors()).orElse(Collections.emptyList());
      final var refStatus = errors.isEmpty() ? ProgramStatus.UP : ProgramStatus.ERROR;
      
      builder.addAssociations(ImmutableProgramAssociation.builder()
          .id(child.getDependencyId())
          .owner(true)
          .refType(refType.get())
          .refStatus(refStatus)
          .ref(child.getArtifactAst().get().getName())
          .build()
      );
    }
    
    for(final var child : parentDeps) {
      if(child.getArtifactAst().isEmpty()) {
        continue;
      }
      final var refType = child.getArtifactAst().map(e -> e.getBodyType());
      final var errors = child.getArtifactAst().map(ast -> ast.getErrors()).orElse(Collections.emptyList());
      final var refStatus = errors.isEmpty() ? ProgramStatus.UP : ProgramStatus.ERROR;
      
      builder.addAssociations(ImmutableProgramAssociation.builder()
          .id(child.getDependencyId())
          .owner(false)
          .refType(refType.get())
          .refStatus(refStatus)
          .ref(child.getArtifactAst().get().getName())
          .build()
      );
    }
    
    return builder
        .artifactId(artifactId)
        .artifactName(artifactName)
        .artifactType(artifactType)
        .childDeps(childDeps)
        .parentDeps(parentDeps)
        .addAllErrors(errors)
        .addAllErrors(ast.getErrors())
        .programStatus(errors.isEmpty() ? ProgramStatus.UP : ProgramStatus.ERROR)
        .build();
  }

  public String getArtifactId() {
    return artifactId;
  }
  public String getArtifactName() {
    return artifactName;
  }
  public Model.BodyType getArtifactType() {
    return artifactType;
  }
  public Simple_AST getAst() {
    return ast;
  }
  
  public void addParent(ArtifactBuilder parent) {
    this.parentDeps.add(ImmutableDependency_AST.builder()
        .artifactAst(parent.getAst())
        .type(parent.getAst().getBodyType())
        .dependencyId(parent.getArtifactId())
        .build());
  }
}
