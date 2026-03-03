package io.resys.limaone.spi.program;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.resys.limaone.ast.Flow_AST;
import io.resys.limaone.ast.Flow_AST.AnyStatement;
import io.resys.limaone.ast.Flow_AST.BodyStatement;
import io.resys.limaone.ast.Flow_AST.CaseStatement;
import io.resys.limaone.ast.Flow_AST.DecisionTableStatement;
import io.resys.limaone.ast.Flow_AST.EmptyBodyStatement;
import io.resys.limaone.ast.Flow_AST.EndStatement;
import io.resys.limaone.ast.Flow_AST.FlowTaskStatement;
import io.resys.limaone.ast.Flow_AST.InputsStatement;
import io.resys.limaone.ast.Flow_AST.ManyTasksStatement;
import io.resys.limaone.ast.Flow_AST.MappingStatement;
import io.resys.limaone.ast.Flow_AST.OneTaskStatement;
import io.resys.limaone.ast.Flow_AST.PointerStatement;
import io.resys.limaone.ast.Flow_AST.ReturnsStatement;
import io.resys.limaone.ast.Flow_AST.StartStatement;
import io.resys.limaone.ast.Flow_AST.SwitchStatement;
import io.resys.limaone.program.FlowProgram.FlowExecutionStatus;
import io.resys.limaone.program.FlowProgram.FlowResult;
import io.resys.limaone.program.ImmutableFlowResult;
import io.resys.limaone.program.ProgramInput;
import io.resys.limaone.program.Runtime;
import io.resys.limaone.spi.program.assignment.AssignmentContext;
import io.resys.limaone.spi.program.expression.OperationContext.ExternalContext;
import io.resys.limaone.spi.program.stack.FlowStack;
import lombok.Getter;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

/**
 * Default walker that traverses all nodes in the AST tree.
 * Override specific visit methods to add custom behavior.
 */
@Slf4j
public class FlowProgramExecutor {

  private final io.resys.limaone.program.Runtime runtime;
  private final AssignmentContext assignment;
  private final FlowStack stack = new FlowStack();
  private final ExecutorResult REACHED_END = new ExecutorResult() {};
  private final ExecutorProps NO_PROPS = new ExecutorProps() {};

  
  public FlowProgramExecutor(Runtime runtime, ProgramInput input) {
    super();
    this.runtime = runtime;
    this.assignment = new AssignmentContext(runtime, input); 
  }

  interface ExecutorResult {}
  interface ExecutorProps {}

  @Value
  private static class CaseStatementProps implements ExecutorProps {
    BodyStatement body;
    ExternalContext context;
    Map<String, Serializable> accepts;    
  }
  @Value
  private static class CaseStatementResult implements ExecutorResult {
    boolean isMatch;
  }
  @Value
  private static class MappingResults implements ExecutorResult {
    List<Map<String, Serializable>> values;
  }
  
  
  /**
   * Walk the entire Flow AST starting from the root statement
   */
  public FlowResult walk(Flow_AST flow, ExecutorProps ExecutorProps) {
    FlowExecutionStatus status = FlowExecutionStatus.COMPLETED;
    try {
      visit(flow.getStatement(), NO_PROPS);
    } catch(Exception exception) {
      stack.newFrame(exception);
      status = FlowExecutionStatus.ERROR;
    }
    
    final var stack = this.stack.close();
    
    return ImmutableFlowResult.builder()
      .logs(stack.getLogs())
      .lastLogs(stack.getLastLogs())
      .stepId(stack.getLastStepId())
      .status(status)
      .accepts(assignment.getInitalizers().entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().getRaw())))
      .isReturnsCollection(stack.isReturnsCollection())
      .returns(stack.getReturns())
      .shortHistory(stack.getShortHistory())
      .build();
  }
  

  public ExecutorResult visitInputsStatement(InputsStatement statement, ExecutorProps ExecutorProps) {
    this.assignment.initalizers(statement);
    
    // Continue to next statement
    return visit(statement.getNext(), NO_PROPS);
  }
  

  public ExecutorResult visitManyTasksStatement(ManyTasksStatement statement, ExecutorProps ExecutorProps) {
    this.assignment.initalizers(statement);
    
    // Container for multiple tasks - continue to next
    return visit(statement.getNext(), NO_PROPS);
  }
  

  public ExecutorResult visitOneTaskStatement(OneTaskStatement statement, ExecutorProps ExecutorProps) {
    // Visit task body
    visit(statement.getBody(), NO_PROPS);
    
    // Visit next statement
    return visit(statement.getThen(), NO_PROPS);
  }
  

  public ExecutorResult visitEmptyBodyStatement(EmptyBodyStatement statement, ExecutorProps ExecutorProps) {
    return REACHED_END;
  }
  

  public ExecutorResult visitDecisionTableStatement(DecisionTableStatement statement, ExecutorProps ExecutorProps) {
    // Process decision table execution
    final var dt = runtime.getBundle().queryDecisions()
        .name(statement.getDecisionTableName())
        .getOne();
    
    final var sets = visitMappingStatement(statement.getMapping(), NO_PROPS).getValues();
    for(final var inputs : sets) {
      try {
        final LocalDateTime start = LocalDateTime.now();
        final var result = dt.run(assignment.withInputs(inputs), runtime).andGetBody();
        assignment.assignFromTask(statement, result, sets.size());
        stack.newFrame(statement, inputs, result, start);
      } catch(Exception e) {
        throw new StatementException(e.getMessage(), statement, inputs, e);
      }
    }
    return REACHED_END;
  }
  

  public ExecutorResult visitFlowTaskStatement(FlowTaskStatement statement, ExecutorProps ExecutorProps) {
    // Process service/groovy task execution
    final var ft = runtime.getBundle().queryFlowTasks()
        .name(statement.getFlowTaskName())
        .getOne();
    
    final var sets = visitMappingStatement(statement.getMapping(), NO_PROPS).getValues();
    for(final var inputs : sets) {
      try {
        final LocalDateTime start = LocalDateTime.now();
        final var result = ft.run(assignment.withInputs(inputs), runtime).andGetBody();
        assignment.assignFromTask(statement, result, sets.size());
        stack.newFrame(statement, inputs, result, start);
        
      } catch(Exception e) {
        throw new StatementException(e.getMessage(), statement, inputs, e);
      }
    }
    
    return REACHED_END;
  }
  

  public ExecutorResult visitReturnsStatement(ReturnsStatement statement, ExecutorProps ExecutorProps) {
    
    // Visit return mapping
    final var sets = visitMappingStatement(statement.getMapping(), NO_PROPS).getValues();;
    for(final var inputs : sets) {
      try {
        final LocalDateTime start = LocalDateTime.now();
        assignment.assignFromTask(statement, inputs, sets.size());
        stack.newFrame(statement, inputs, start);
      } catch(Exception e) {
        throw new StatementException(e.getMessage(), statement, inputs, e);
      }
    }
    return REACHED_END;
  }
  

  public ExecutorResult visitSwitchStatement(SwitchStatement statement, ExecutorProps ExecutorProps) {
    
    for(final var mappingEntry : visitMappingStatement(statement.getMapping(), NO_PROPS).getValues()) {
      final var context = new ExternalContext() {
        public Object apply(String name) {
          if(mappingEntry.containsKey(name)) {
            return mappingEntry.get(name);
          }
          return mappingEntry.entrySet().stream()
            .filter(e -> e.getKey().startsWith(name + "."))
            .collect(Collectors.toMap(e -> e.getKey().substring(name.length() + 1), e -> e.getValue()));
        }
      };
      
      boolean isAtleastOneMatch = false;
      for(final var whenThen : statement.getCases()) {
        final var propsResult = visitCaseStatement(whenThen, new CaseStatementProps(statement, context, mappingEntry));
        if(propsResult.isMatch) {
          isAtleastOneMatch = true;
          break;
        }
      }
      
      if(!isAtleastOneMatch) {
        log.debug("Flow switch: '" + statement.getMapping().getTaskId() + "' does not match any expressions!");
      }
    }
    return REACHED_END;
  }
  

  public CaseStatementResult visitCaseStatement(CaseStatement statement, CaseStatementProps props) {

    
    final LocalDateTime start = LocalDateTime.now();
    final var condition = statement.getWhen().run(props.getContext());
    stack.newFrame(props.getBody(), props.getAccepts(), condition, start);

    if(Boolean.TRUE.equals(condition.getValue())) {
      visit(statement.getThen(), null);
      return new CaseStatementResult(true);
    }
    
    // Visit next statement for this case
    return new CaseStatementResult(false);
  }
  

  public ExecutorResult visitStartStatement(StartStatement statement, ExecutorProps ExecutorProps) {
    // Flow entry point
    return visit(statement.getFirstTask(), NO_PROPS);
  }
  

  public ExecutorResult visitEndStatement(EndStatement statement, ExecutorProps ExecutorProps) {
    // Flow termination
    return REACHED_END;
  }
  

  public ExecutorResult visitPointerStatement(PointerStatement statement, ExecutorProps ExecutorProps) {
    // Reference to another task
    return visit(statement.getTask(), NO_PROPS);
  }
  

  public MappingResults visitMappingStatement(MappingStatement statement, ExecutorProps props) {
    return new MappingResults(assignment.assignTo(statement.getTaskId(), statement));
  }
  

  
  public ExecutorResult visit(AnyStatement statement, ExecutorProps props) {
    switch (statement.getType()) {
      case FLOW_INPUTS:
        return visitInputsStatement((InputsStatement) statement, props);
      case FLOW_TASKS:
        return visitManyTasksStatement((ManyTasksStatement) statement, props);
      case FLOW_TASK:
        return visitOneTaskStatement((OneTaskStatement) statement, props);
      case BODY_EMPTY:
        return visitEmptyBodyStatement((EmptyBodyStatement) statement, props);
      case BODY_DECISION_TABLE:
        return visitDecisionTableStatement((DecisionTableStatement) statement, props);
      case BODY_FLOW_TASK:
        return visitFlowTaskStatement((FlowTaskStatement) statement, props);
      case BODY_RETURNS:
        return visitReturnsStatement((ReturnsStatement) statement, props);
      case BODY_SWITCH:
        return visitSwitchStatement((SwitchStatement) statement, props);
      case BODY_SWITCH_CASE:
        return visitCaseStatement((CaseStatement) statement, (CaseStatementProps) props);
      case NEXT_IS_START:
        return visitStartStatement((StartStatement) statement, props);
      case NEXT_IS_END:
        return visitEndStatement((EndStatement) statement, props);
      case NEXT_IS_POINTER:
        return visitPointerStatement((PointerStatement) statement, props);
      case MAPPING:
        return visitMappingStatement((MappingStatement) statement, props);
      default:
        throw new IllegalArgumentException("Unknown statement type: " + statement.getType());
    }
  }
  
  @Getter
  public static class StatementException extends RuntimeException {
    private static final long serialVersionUID = -7154685569622201632L;
    private final AnyStatement statement;
    private final Map<String, Serializable> props;
    
    public StatementException(String message, AnyStatement statement) {
      super(message);
      this.statement = statement;
      this.props = null;
    }
    public StatementException(String message, AnyStatement statement, Throwable cause) {
      super(message, cause);
      this.statement = statement;
      this.props = null;
    }
    
    public StatementException(String message, AnyStatement statement, Map<String, Serializable> props, Throwable cause) {
      super(message, cause);
      this.statement = statement;
      this.props = props;
    }
  }
}