package io.resys.limaone.spi.compiler;

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

import java.util.List;
import java.util.Optional;

import org.immutables.value.Value;

import io.resys.limaone.ast.AST_Parser.Dependency_AST;
import io.resys.limaone.ast.Simple_AST;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.ModelError;
import io.resys.limaone.program.Compiler.BundleBuilder;
import io.resys.limaone.program.Program;
import io.resys.limaone.program.Program.ProgramAssociation;
import io.resys.limaone.program.Program.ProgramStatus;
import io.smallrye.mutiny.Uni;

public interface CompilableUnit {
  ArtifactLink compile(NewArtifact resolution);
  
  interface ArtifactLink {
    Simple_AST getAst();
    RuntimeLink accept(Artifact artifact);
  }

  @FunctionalInterface
  interface RuntimeLink {
    Program accept(io.resys.limaone.program.Runtime runtime);
  }
  
  interface Bundler {
    NewArtifact newArtifact();
    Uni<BundleBuilder> build(List<ArtifactLink> openProgram);
  }
  
  interface NewArtifact {
    NewArtifact id(String id);
    NewArtifact name(String name);
    NewArtifact ast(Simple_AST ast);
    NewArtifact requireDependnecy(Dependency_AST ast);
    NewArtifact requireDependnecy(Dependency_AST ast, Validator validator);
    void build();
  }
  
  interface Validator {
    ValidatorResult validate(Optional<Simple_AST> dependency);
  }
  
  @Value.Immutable
  interface ValidatorResult {
    List<ModelError> getMessages();
    ProgramStatus getProgramStatus();
  }
  
  
  @Value.Immutable
  interface Artifact {
    String getArtifactId(); // who am I
    String getArtifactName(); // who am I
    
    Model.BodyType getArtifactType();
    
    List<Dependency_AST> getChildDeps(); // who do I depend on aka children
    List<Dependency_AST> getParentDeps(); // who depends on me
    
    List<ModelError> getErrors();
    List<ProgramAssociation> getAssociations(); 
    ProgramStatus getProgramStatus();
  }
}
