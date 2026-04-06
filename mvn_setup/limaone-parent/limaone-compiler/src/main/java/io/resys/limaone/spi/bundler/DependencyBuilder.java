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

import java.util.Objects;
import java.util.Optional;

import io.resys.limaone.ast.ImmutableDependency_AST;
import io.resys.limaone.model.ImmutableModelError;
import io.resys.limaone.model.Model;
import io.resys.limaone.spi.compiler.CompilableUnit.Validator;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DependencyBuilder {
  private final ArtifactBuilder artifactBuilder;
  
  private String id;
  private Model.BodyType bodyType;
  private @Nullable Validator validator;

  // Fluent setters
  public DependencyBuilder id(String id) {
    this.id = Objects.requireNonNull(id, () -> "id must be defined");
    return this;
  }
  public DependencyBuilder bodyType(Model.BodyType bodyType) {
    this.bodyType = Objects.requireNonNull(bodyType, () -> "bodyType must be defined");
    return this;
  }
  public DependencyBuilder validator(Validator validator) {
    this.validator = Objects.requireNonNull(validator, () -> "validator must be defined");
    return this;
  }
  public void init() {
    Objects.requireNonNull(id, () -> "id must be defined");
    Objects.requireNonNull(bodyType, () -> "bodyType must be defined");
  }
  public @Nullable Validator getValidator() {
    return validator;
  }
  public String getId() {
    return id;
  }
  public Model.BodyType getBodyType() {
    return bodyType;
  }
  
  public void close(Optional<ArtifactBuilder> ref) {
    if(validator != null) {
      // turn ref to depen
      validator.validate(ref.map(r -> r.getAst()));
    }
    
    if(ref.isEmpty()) {
      artifactBuilder.addError(ImmutableModelError.builder()
          .msg("@missing '" + bodyType.name() + "': '" + id + "'")
          .build());
    } else {
      artifactBuilder.addChildDep(ImmutableDependency_AST.builder()
          .artifactAst(ref.get().getAst())
          .dependencyId(ref.get().getAst().getName())
          .type(bodyType)
          .build());
      ref.get().addParent(artifactBuilder);
    }
  }
}
