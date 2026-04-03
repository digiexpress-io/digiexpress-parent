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

import java.util.stream.Stream;

import io.resys.limaone.ast.AST_Parser;
import io.resys.limaone.ast.Flow_AST;
import io.resys.limaone.ast.Simple_AST;
import io.resys.limaone.model.Flow;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.ModelWorld;
import io.resys.limaone.program.Program.ProgramStatus;
import io.resys.limaone.spi.program.FlowProgramImpl;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class Compiler_Flow implements CompilableUnit {
  private final AST_Parser parser;
  @SuppressWarnings("unused")
  private final ModelWorld world;
  private final Model<Flow> flow;
  
  @Override
  public ArtifactLink compile(NewArtifact resolution) {
    
    final Flow_AST ast = parser.parseFlow()
        .syntax(flow.getBody().getFlowValue())
        .onDependency(dep -> resolution.requireDependnecy(dep))
        .parse();
    resolution.ast(ast).id(flow.getId()).name(ast.getName()).build();
    
    return new ArtifactLink() {
      @Override
      public Simple_AST getAst() {
        return ast;
      }
      @Override
      public RuntimeLink accept(Artifact artifact) {
        return (runtime) -> {
          final var extraErrors = new Compiler_FlowDepsValidator(artifact, ast).walk();
          return new FlowProgramImpl(
              runtime,
              flow.getId(), ast, 
              artifact.getErrors().isEmpty() ? artifact.getProgramStatus() : ProgramStatus.ERROR, 
              Stream.concat(artifact.getErrors().stream(), extraErrors.stream()).toList(), 
              artifact.getAssociations());
        };
      }
    };
  }
}
