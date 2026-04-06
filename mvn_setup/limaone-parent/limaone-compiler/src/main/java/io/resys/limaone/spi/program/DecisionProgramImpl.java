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
import java.util.function.Consumer;

import com.google.common.collect.Streams;

import io.resys.limaone.ast.DecisionTable_AST;
import io.resys.limaone.model.DecisionTable.HitPolicy;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.model.ModelError;
import io.resys.limaone.model.Parameter;
import io.resys.limaone.program.DecisionProgram;
import io.resys.limaone.program.ProgramInput;
import io.resys.limaone.spi.program.input.DefaultProgramInput;



public class DecisionProgramImpl implements DecisionProgram {


  private static final long serialVersionUID = 6616773813732711822L;
  private final DecisionTable_AST ast;
  private final String id;
  private final ProgramStatus status; 
  private final List<DecisionRow> rows;
  private final List<Parameter> headers;
  private final List<ModelError> errors;
  private final List<ProgramAssociation> associations;
  
  public DecisionProgramImpl(
      io.resys.limaone.program.Runtime runtime,
      String id,
      DecisionTable_AST ast, 
      ProgramStatus status,
      List<DecisionRow> rows,
      List<ModelError> errors,
      List<ProgramAssociation> associations) {
    super();
    this.id = id;
    this.ast = ast;
    this.status = status;
    this.rows = Collections.unmodifiableList(rows);
    this.errors = Collections.unmodifiableList(errors);
    this.associations = Collections.unmodifiableList(associations);
    this.headers = Streams
        .concat(ast.getHeaders().getAcceptDefs().stream(), ast.getHeaders().getReturnDefs().stream())
        .toList();
  }
  @Override
  public DecisionTable_AST getAst() {
    return ast;
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
  public BodyType getType() {
    return ast.getBodyType();
  }
  @Override
  public ProgramStatus getStatus() {
    return status;
  }
  @Override
  public List<ModelError> getErrors() {
    return errors;
  }
  @Override
  public List<Parameter> getHeaders() {
    return headers;
  }
  @Override
  public List<ProgramAssociation> getAssociations() {
    return associations;
  }
  @Override
  public List<DecisionRow> getRows() {
    return rows;
  }
  @Override
  public HitPolicy getHitPolicy() {
    return ast.getHitPolicy();
  }
  @Override
  public DecisionExecutor run(Map<String, Serializable> input) {
    return run(DefaultProgramInput.of(input));
  }
  @Override
  public String encodePrettily() {
    return DecisionProgramPrettyEncoder.encodePrettily(ast);
  }
  @Override
  public List<String> getLocales() {
    return Collections.emptyList();
  }
  @Override
  public DecisionExecutor run(ProgramInput input) {
    final var result = DecisionProgramExecutor.run(this, input);
    
    return new DecisionExecutor() {
      @Override
      public DecisionResult andGetBody() {
        return result;
      }
      @Override
      public Map<String, Serializable> andGet() {
        return DecisionProgramExecutor.get(result);
      }
      @Override
      public List<Map<String, Serializable>> andFind() {
        return DecisionProgramExecutor.find(result);
      }
      @Override
      public DecisionExecutor callback(Consumer<DecisionTable_AST> callback) {
        callback.accept(ast);
        return this;
      }
    };
  }

}
