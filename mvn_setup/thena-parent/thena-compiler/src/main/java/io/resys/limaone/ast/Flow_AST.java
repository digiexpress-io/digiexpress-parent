package io.resys.limaone.ast;

/*-
 * #%L
 * wrench-assets-flow
 * %%
 * Copyright (C) 2016 - 2018 Copyright 2016 ReSys OÜ
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
import java.util.List;
import java.util.Map;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.limaone.ast.Flow_CST.YamlParseTree;
import io.resys.limaone.model.Parameter;
import io.resys.limaone.program.ExpressionProgram;
import jakarta.annotation.Nullable;


@Value.Immutable
@JsonSerialize(as = ImmutableFlow_AST.class)
@JsonDeserialize(as = ImmutableFlow_AST.class)
public interface Flow_AST extends Simple_AST, Serializable {
  YamlParseTree getParseTree();
  AnyStatement getStatement();


  //root marker
  interface AnyStatement {
    StatementType getType();
  }

  // markers
  interface BodyStatement {}
  interface NextStatement {}


  interface InputsStatement {
    List<Parameter> getParameters();
    default StatementType getType() { return StatementType.FLOW_INPUTS; }
  }

  interface ManyTasksStatement extends AnyStatement {
    NextStatement getNext();
    default StatementType getType() { return StatementType.FLOW_TASKS; }
  }

  interface OneTaskStatement extends AnyStatement {
    String getId();
    BodyStatement getBody();
    NextStatement getThen();
    default StatementType getType() { return StatementType.FLOW_TASK; }
  }

  interface EmptyBodyStatement extends BodyStatement, AnyStatement {
    default StatementType getType() { return StatementType.BODY_EMPTY; }
  }  
  interface DecisionTableStatement extends BodyStatement, AnyStatement {
    boolean isCollection();
    MappingStatement getMapping();
    default StatementType getType() { return StatementType.BODY_DECISION_TABLE; }
  }
  interface FlowTaskStatement extends BodyStatement, AnyStatement {
    boolean isCollection();
    MappingStatement getMapping();
    default StatementType getType() { return StatementType.BODY_FLOW_TASK; }
  }
  interface ReturnsStatement extends BodyStatement, AnyStatement {
    boolean isCollection();
    MappingStatement getMapping();
    default StatementType getType() { return StatementType.BODY_RETURNS; }
  }


  interface SwitchStatement extends BodyStatement, AnyStatement {
    List<CaseStatement> getCases();
    default StatementType getType() { return StatementType.BODY_SWITCH; }
  }
  interface CaseStatement extends AnyStatement {
    @Nullable ExpressionProgram getWhen();
    NextStatement getThen();    
    default StatementType getType() { return StatementType.BODY_SWITCH_CASE; }
  }

  interface StartStatement extends AnyStatement, NextStatement {
    OneTaskStatement getFirstTask();
    default StatementType getType() { return StatementType.NEXT_IS_START; }
  }

  interface EndStatement extends AnyStatement, NextStatement {
    default StatementType getType() { return StatementType.NEXT_IS_END; }
  }

  interface PointerStatement extends AnyStatement, NextStatement {
    OneTaskStatement getTask();

    default StatementType getType() { return StatementType.NEXT_IS_POINTER; }
  }
  interface MappingStatement extends AnyStatement {
    //  to------from
    Map<String, String> getAssignments();

    default StatementType getType() { return StatementType.MAPPING; }
  }


  enum StatementType {
    FLOW_INPUTS,
    FLOW_TASKS,
    FLOW_TASK,

    BODY_EMPTY,
    BODY_FLOW_TASK, 
    BODY_DECISION_TABLE, 
    BODY_SWITCH,
    BODY_SWITCH_CASE,
    BODY_RETURNS,

    MAPPING,

    NEXT_IS_START, 
    NEXT_IS_END, 
    NEXT_IS_POINTER
  }

}