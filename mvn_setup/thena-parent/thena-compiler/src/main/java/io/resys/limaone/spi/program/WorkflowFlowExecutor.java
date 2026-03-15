package io.resys.limaone.spi.program;

import java.io.Serializable;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import groovy.util.logging.Slf4j;
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
import io.resys.limaone.program.ImmutableWorkflowFormResult;
import io.resys.limaone.program.ProgramInput;
import io.resys.limaone.program.WorkflowProgram.WorkflowExecutionStatus;
import io.resys.limaone.program.WorkflowProgram.WorkflowFlowResult;
import io.resys.limaone.program.WorkflowProgram.WorkflowForm;
import io.resys.limaone.spi.dialob.FormDb;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;


@Slf4j
@RequiredArgsConstructor
public class WorkflowFlowExecutor {
  private final io.resys.limaone.program.Runtime runtime;
  private final FormDb formDb;
  private final ScheduledExecutorService workerPool;
  private final Duration maxTimeout;
  private final ProgramInput programInput;
  private final WorkflowForm form;
  
  private final List<ModelError> errors = new ArrayList<>();
  
  private final ExecutorResult REACHED_END = new ExecutorResult() {};
  private final ExecutorProps NO_PROPS = new ExecutorProps() {};
  
  interface ExecutorResult {}
  interface ExecutorProps {}
  
  
  public WorkflowFlowResult walk(ArticleWorkflow_AST ast) {
    try {
      visit(ast.getStatement(), NO_PROPS);
    } catch(Exception exception) {
      final var msg = "Msg: " + exception.getMessage() + System.lineSeparator() + JsonObject.mapFrom(ast).encodePrettily();
      this.errors.add(ImmutableModelError.builder().exception(exception).msg(msg).build());
    }
    return ImmutableWorkflowFormResult.builder()
        .errors(errors)
        .status(errors.isEmpty() ? WorkflowExecutionStatus.COMPLETED : WorkflowExecutionStatus.ERROR)
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
    // TODO:: validate against form version
    return visit(statement.getNext(), NO_PROPS);
  }
  
  public ExecutorResult visitCreateTaskStatement(CreateTaskStatement statement, ExecutorProps props) {
    // Create task using flow name
    final var flow = runtime.getBundle().queryFlows()
        .name(statement.getFlowName())
        .getOne();
    
    // load the questionnaire right up
    ds
    final var result = flow.run(programInput.withInputs(Map.of(
      "questionnaireId", form.getFormSessionId(),
      "workflowName", form.getWorkflowName())
    ), runtime);
    
    // Execute the flow as a task
    // Implementation depends on your task management system
    
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
