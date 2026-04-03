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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.resys.limaone.ast.Flow_AST;
import io.resys.limaone.ast.Flow_CST.YamlInput;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.model.ModelError;
import io.resys.limaone.model.Parameter;
import io.resys.limaone.model.Parameter.Direction;
import io.resys.limaone.model.Parameter.ValueType;
import io.resys.limaone.program.FlowProgram;
import io.resys.limaone.program.ProgramInput;
import io.resys.limaone.spi.parameter.Parameter_Factory;
import io.resys.limaone.spi.program.input.DefaultProgramInput;
import io.resys.limaone.yaml.YamlMapper;

public class FlowProgramImpl implements FlowProgram {

  private static final long serialVersionUID = -4209510801206880302L;
  private final String id;
  private final Flow_AST ast;
  private final io.resys.limaone.program.Runtime runtime;
  private final ProgramStatus status; 
  private final List<Parameter> headers;
  private final List<ModelError> errors;
  private final List<ProgramAssociation> associations;
  
  public FlowProgramImpl(
      io.resys.limaone.program.Runtime runtime,
      String id,
      Flow_AST ast, 
      ProgramStatus status,
      List<ModelError> errors,
      List<ProgramAssociation> associations) {
    
    this.id = id;
    this.ast = ast;
    this.status = status;
    this.errors = Collections.unmodifiableList(errors);
    this.associations = Collections.unmodifiableList(associations);
    this.headers = Collections.unmodifiableList(getHeaders(ast));
    this.runtime = runtime;
    
  }
  
  private List<Parameter> getHeaders(Flow_AST ast) {
    final Map<String, YamlInput> inputs = ast.getParseTree().getInputs();

    int index = 0;
    final List<Parameter> result = new ArrayList<>();
    for (final Map.Entry<String, YamlInput> entry : inputs.entrySet()) {
      if (entry.getValue().getType() == null) {
        continue;
      }
      try {
        final ValueType valueType = ValueType.valueOf(entry.getValue().getType().getValue());
        final boolean required = YamlMapper.getBooleanValue(entry.getValue().getRequired());
        result.add(Parameter_Factory.newParam()
            .id(entry.getValue().getStart() + "")
            .order(index++)
            .name(entry.getKey()).valueType(valueType).direction(Direction.IN).required(required)
            .values(YamlMapper.getStringValue(entry.getValue().getDebugValue()))
            .build());
        
      } catch (Exception e) {
        final String msg = String.format("Failed to convert data type from: %s, error: %s", entry.getValue().getType().getValue(), e.getMessage());
        throw new ProgramException(msg, e);
      }
    }
    return result;
  }
  
  @Override
  public String getId() {
    return id;
  }

  @Override
  public Flow_AST getAst() {
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
  public String getName() {
    return ast.getName();
  }
  
  private String getPrettyErrors() {
    final var result = new StringBuilder();
    for(final var error : this.errors) {       
      result
        .append(System.lineSeparator())
        .append("@line ").append(error.getLine())
        .append(": ").append(error.getMsg());
    }
    return result.toString();
  }
  
  private RuntimeException generateError(String msg) {
    final var start = new RuntimeException(msg);
    Exception prev = start;
    for(final var error : this.errors) {  
      if(error.getException() == null) {
        continue;
      }
      prev.initCause(error.getException());
      prev = error.getException();
    }
    return start;
  }

  @Override
  public FlowExecutor run(ProgramInput input) {
    if(!this.errors.isEmpty()) {
      final var msg = "Flow with id: '" + this.getId() + "' and name: '" + this.getName() + "' can't be executed because of errors: " +  getPrettyErrors();
      throw generateError(msg);
    }
    
    final var stack = new FlowProgramExecutor(runtime, input).walk(ast, null);
    return new FlowExecutor() {
      @Override
      public FlowResultLog andGetTask(String task) {
        return stack.getLogs().stream()
            .filter(t -> t.getStepId().equals(task)).findFirst().orElse(null);
      }
      @Override
      public FlowResult andGetBody() {
        return stack;
      }
      @Override
      public String andEncodePrettily() {
        return FlowProgramExecutionPrettyPrint.toVerticalAsciiTable(stack, ast);
      }
    };
  }
  @Override
  public FlowExecutor run(Map<String, Serializable> input) {
    return run(DefaultProgramInput.of(input));
  }
  @Override
  public List<String> getLocales() {
    return Collections.emptyList();
  }
}
