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
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.mutable.MutableObject;

import io.resys.limaone.ast.ArticleWorkflow_AST;
import io.resys.limaone.ast.ArticleWorkflow_AST.AnonStatement;
import io.resys.limaone.ast.ArticleWorkflow_AST.AnyStatement;
import io.resys.limaone.ast.ArticleWorkflow_AST.AwaitFormStatement;
import io.resys.limaone.ast.ArticleWorkflow_AST.CreateFormStatement;
import io.resys.limaone.ast.ArticleWorkflow_AST.CreateTaskStatement;
import io.resys.limaone.ast.ArticleWorkflow_AST.DevelopmentStatement;
import io.resys.limaone.ast.ArticleWorkflow_AST.DisabledStatement;
import io.resys.limaone.ast.ArticleWorkflow_AST.EndStatement;
import io.resys.limaone.ast.ArticleWorkflow_AST.InputsStatement;
import io.resys.limaone.ast.ArticleWorkflow_AST.LimitedTimeStatement;
import io.resys.limaone.ast.ArticleWorkflow_AST.UserRolesStatement;
import io.resys.limaone.model.ImmutableModelError;
import io.resys.limaone.model.ModelError;
import io.resys.limaone.program.FlowProgram.FlowResult;
import io.resys.limaone.program.ImmutableWorkflowFlowResult;
import io.resys.limaone.program.ProgramInput;
import io.resys.limaone.program.ProgramInput.ParticipantForm;
import io.resys.limaone.program.WorkflowProgram.WorkflowExecutionStatus;
import io.resys.limaone.program.WorkflowProgram.WorkflowFlowResult;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RequiredArgsConstructor
public class WorkflowFlowExecutor {
  private final io.resys.limaone.program.Runtime runtime;
  private final ParticipantForm form;
  private final ProgramInput programInput;
  private final MutableObject<FlowResult> flowResult = new MutableObject<>();
  private final List<ModelError> errors = new ArrayList<>();
  
  private final ExecutorResult REACHED_END = new ExecutorResult() {};
  private final ExecutorProps NO_PROPS = new ExecutorProps() {};
  
  private String workflowName;
  
  interface ExecutorResult {}
  interface ExecutorProps {}
  
  
  public WorkflowFlowResult walk(ArticleWorkflow_AST ast) {
    try {
      workflowName = ast.getName();
      visit(ast.getStatement(), NO_PROPS);
    } catch(Exception exception) {
      final var msg = "Msg: " + exception.getMessage() + System.lineSeparator() + JsonObject.mapFrom(ast).encodePrettily();
      this.errors.add(ImmutableModelError.builder().exception(exception).msg(msg).build());
    }
    
    for(final var error : errors) {
      log.error(error.getMsg(), error.getException());
    }
    
    return ImmutableWorkflowFlowResult.builder()
        .errors(errors)
        .status(errors.isEmpty() ? WorkflowExecutionStatus.COMPLETED : WorkflowExecutionStatus.ERROR)
        .flow(Optional.ofNullable(flowResult.get()))
        .build();
  }
  
  public ExecutorResult visitAnonStatement(AnonStatement statement, ExecutorProps props) {
    // Anonymous statement - continue to next
    return visit(statement.getNext(), NO_PROPS);
  }
  
  public ExecutorResult visitDisabledStatement(DisabledStatement statement, ExecutorProps props) {
    // Disabled statement - skip to next
    return visit(statement.getNext(), NO_PROPS);
  }
  
  public ExecutorResult visitDevelopmentStatement(DevelopmentStatement statement, ExecutorProps props) {
    // Development statement - continue to next
    return visit(statement.getNext(), NO_PROPS);
  }
  
  public ExecutorResult visitInputsStatement(InputsStatement statement, ExecutorProps props) {
    // Process workflow inputs
    return visit(statement.getNext(), NO_PROPS);
  }
  
  public ExecutorResult visitLimitedTimeStatement(LimitedTimeStatement statement, ExecutorProps props) {
    // Check time constraints
    final OffsetDateTime now = OffsetDateTime.now();
    if (now.isBefore(statement.getStartDate()) || now.isAfter(statement.getEndDate())) {
      errors.add(ImmutableModelError.builder().msg("Workflow execution is outside allowed time window").build());
      return REACHED_END;
    }
    return visit(statement.getNext(), NO_PROPS);
  }
  
  public ExecutorResult visitUserRolesStatement(UserRolesStatement statement, ExecutorProps props) {
    return visit(statement.getNext(), NO_PROPS);
  }
  
  public ExecutorResult visitCreateFormStatement(CreateFormStatement statement, ExecutorProps props) {
    return visit(statement.getNext(), NO_PROPS);
  }
  
  public ExecutorResult visitCreateTaskStatement(CreateTaskStatement statement, ExecutorProps props) {
    // Create task using flow name
    final var flow = runtime.getBundle().queryFlows()
        .name(statement.getFlowName())
        .getOne();
    
    
    if(!flow.getErrors().isEmpty()) {
      this.errors.addAll(flow.getErrors());
      return REACHED_END;
    }
    
    // load the questionnaire right up

    // add more data from current session
    final Map<String, Serializable> additionalInputs = Map.of(
        "questionnaireId", form.getQuestionnaireId(),
        "workflowName", workflowName);    
    final var result = flow.run(programInput.withInputs(additionalInputs)).andGetBody();
    flowResult.setValue(result);
    
    return visit(statement.getNext(), NO_PROPS);
  }
  
  public ExecutorResult visitAwaitFormStatement(AwaitFormStatement statement, ExecutorProps props) {
    return visit(statement.getNext(), NO_PROPS);
  }
  
  public ExecutorResult visitEndStatement(EndStatement statement, ExecutorProps props) {
    // Workflow termination
    return REACHED_END;
  }
  
  public ExecutorResult visit(AnyStatement statement, ExecutorProps props) {
    switch (statement.getType()) {
      case ANON:
        return visitAnonStatement((AnonStatement) statement, props);
      case DISABLED:
        return visitDisabledStatement((DisabledStatement) statement, props);
      case DEVELOPMENT:
        return visitDevelopmentStatement((DevelopmentStatement) statement, props);
      case INPUTS:
        return visitInputsStatement((InputsStatement) statement, props);
      case LIMITED_TIME:
        return visitLimitedTimeStatement((LimitedTimeStatement) statement, props);
      case USER_ROLES:
        return visitUserRolesStatement((UserRolesStatement) statement, props);
      case CREATE_FORM:
        return visitCreateFormStatement((CreateFormStatement) statement, props);
      case CREATE_TASK:
        return visitCreateTaskStatement((CreateTaskStatement) statement, props);
      case AWAIT_FORM:
        return visitAwaitFormStatement((AwaitFormStatement) statement, props);
      case END:
        return visitEndStatement((EndStatement) statement, props);
      default:
        throw new IllegalArgumentException("Unknown statement type: " + statement.getType());
    }
  }
}
