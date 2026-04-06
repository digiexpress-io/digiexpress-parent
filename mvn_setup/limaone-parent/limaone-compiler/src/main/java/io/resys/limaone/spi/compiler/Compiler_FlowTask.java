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

import java.util.ArrayList;

import org.apache.commons.lang3.Validate;

import io.resys.limaone.ast.AST_Parser;
import io.resys.limaone.ast.FlowTask_AST;
import io.resys.limaone.ast.Simple_AST;
import io.resys.limaone.model.FlowTask;
import io.resys.limaone.model.FlowTask.FlowTaskExecutable;
import io.resys.limaone.model.ImmutableModelError;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.ModelWorld;
import io.resys.limaone.program.Program.ProgramStatus;
import io.resys.limaone.spi.program.FlowTaskProgramImpl;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class Compiler_FlowTask implements CompilableUnit {
  private final AST_Parser parser;
  @SuppressWarnings("unused")
  private final ModelWorld world;
  private final Model<FlowTask> target;
  
  @Override
  public ArtifactLink compile(NewArtifact resolution) {
    final FlowTask_AST ast = parser.parseFlowTask().syntax(target.getBody().getTaskValue()).parse();
    // register to bundler
    resolution.ast(ast).name(ast.getName()).id(target.getId()).build();

    return new ArtifactLink() {
      @Override
      public Simple_AST getAst() {
        return ast;
      }
      @Override
      public RuntimeLink accept(Artifact artifact) {
        return (runtime) -> {
          try {
            final var constructors = ast.getBeanType().getConstructors();
            Validate.isTrue(constructors.length == 1, () -> "There can be only one constructor for flow task, expected: 1 actual: " + constructors.length);
            
            final var errors = new ArrayList<>(artifact.getErrors());
            errors.addAll(ast.getErrors());
            
            final var beanInstance = (FlowTaskExecutable) constructors[0].newInstance();
            final var flowTask = new FlowTaskProgramImpl(
                runtime,
                target.getId(),
                ast, 
                beanInstance, 
                errors.isEmpty() ? ProgramStatus.UP : ProgramStatus.ERROR, 
                errors, 
                artifact.getAssociations());
            return flowTask;
          } catch(Exception e) {
            final var errors = new ArrayList<>(artifact.getErrors());
            errors.add(ImmutableModelError.builder()
                .exception(e)
                .id("flow-task-failed")
                .msg(e.getMessage())
                .build());
            return new FlowTaskProgramImpl(
                runtime,
                target.getId(),
                ast, 
                null, 
                ProgramStatus.ERROR, 
                errors, 
                artifact.getAssociations());
          }
        };
      }
    };
  }
}
