package io.resys.limaone.spi.program;

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

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Streams;

import io.resys.limaone.ast.FlowTask_AST;
import io.resys.limaone.model.FlowTask.FlowTaskExecutable;
import io.resys.limaone.model.FlowTask.FlowTaskPropType;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.model.ModelError;
import io.resys.limaone.model.Parameter;
import io.resys.limaone.program.FlowTaskProgram;
import io.resys.limaone.program.ImmutableFlowTaskResult;
import io.resys.limaone.program.ProgramInput;
import io.resys.limaone.spi.program.input.DefaultProgramInput;
import io.resys.limaone.spi.program.input.RuntimeProgramInput;
import io.smallrye.mutiny.Uni;


public class FlowTaskProgramImpl implements FlowTaskProgram {
  private static final long serialVersionUID = 5909985495850322300L;

  private final io.resys.limaone.program.Runtime runtime;
  private final String id;
  private final FlowTask_AST ast;
  private final FlowTaskExecutable executable;
  
  private final ProgramStatus status; 
  private final List<Parameter> headers;
  private final List<ModelError> errors;
  private final List<ProgramAssociation> associations;
  
  public FlowTaskProgramImpl(
      io.resys.limaone.program.Runtime runtime,
      String id,
      FlowTask_AST ast,
      FlowTaskExecutable executable,
      ProgramStatus status,
      List<ModelError> errors,
      List<ProgramAssociation> associations) {
    super();
    this.runtime = runtime;
    this.id = id;
    this.ast = ast;
    this.executable = executable;
    this.status = status;
    this.errors = Collections.unmodifiableList(errors);
    this.associations = Collections.unmodifiableList(associations);
    this.headers = Streams
        .concat(ast.getHeaders().getAcceptDefs().stream(), ast.getHeaders().getReturnDefs().stream())
        .toList();
  }

  @Override
  public String getId() {
    return id;
  }
  @Override
  public String getName() {
    return ast.getName();
  }
  @Override
  public FlowTask_AST getAst() {
    return ast;
  }
  @Override
  public BodyType getType() {
    return ast.getBodyType();
  }
  @Override
  public ProgramStatus getStatus() {
    return status;
  }
  @Override
  public List<Parameter> getHeaders() {
    return headers;
  }
  @Override
  public List<ModelError> getErrors() {
    return errors;
  }
  @Override
  public List<ProgramAssociation> getAssociations() {
    return associations;
  }
  @Override
  public FlowTaskPropType getExecutorType() {
    return ast.getExecutorType();
  }
  @Override
  public FlowTaskExecutable getBean() {
    return executable;
  }
  @Override
  public Parameter getTypeDef0() {
    return ast.getTypeDef0();
  }
  @Override
  public Parameter getTypeDef1() {
    return ast.getTypeDef1();
  }
  @Override
  public FlowTaskExecutor run(ProgramInput input) {
    return new FlowTaskExecutor() {
      @Override
      public FlowTaskResult andGetBody() {
        return execute(new RuntimeProgramInput(runtime, input));
      }
    };
  }
  @Override
  public FlowTaskExecutor run(Map<String, Serializable> input) {
    return run(DefaultProgramInput.of(input));
  }
  @SuppressWarnings({"rawtypes", "unchecked"})
  private FlowTaskResult execute(ProgramInput input) {
    if(this.status != ProgramStatus.UP) {
      final var errors = String.join("\n", this.errors.stream().map(e -> e.getMsg()).toList());
      throw new ProgramException(
          "Can't run program: " + this.getName() + " because of errors:\n" + errors);
    }
    switch (this.getExecutorType()) {
      case TYPE_0: {
        final var exec = (ServiceExecutorType0) executable;
        final var value = exec.execute();
        return ImmutableFlowTaskResult.builder().value(value).build();
      }
      case TYPE_1: {
        final var param1 = input.getValue(ast.getTypeDef0());
        final var exec = (ServiceExecutorType1) executable;
        final var value = exec.execute(param1);
        return ImmutableFlowTaskResult.builder().value(value).build();
      }
      case TYPE_2: {
        final var param1 = input.getValue(ast.getTypeDef0());
        final var param2 = input.getValue(ast.getTypeDef1());
        final var exec = (ServiceExecutorType2) executable;
        
        final var value = Uni.createFrom()
            .item(() -> exec.execute(param1, param2))
            .runSubscriptionOn(runtime.getProperties().getWorkerPool())
            .await().atMost(runtime.getProperties().getWorkerPoolMaxTimeout());
        
        return ImmutableFlowTaskResult.builder().value(value).build();
      }
      default: throw new ProgramException("Can't find/call execute method!");
    }    
  }
  @Override
  public List<String> getLocales() {
    return Collections.emptyList();
  }
}
