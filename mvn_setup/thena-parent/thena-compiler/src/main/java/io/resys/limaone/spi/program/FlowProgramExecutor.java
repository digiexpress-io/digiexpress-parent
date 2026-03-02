package io.resys.limaone.spi.program;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import io.resys.limaone.ast.Flow_AST;
import io.resys.limaone.ast.Flow_AST.AnyStatement;
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
import io.resys.limaone.program.DecisionProgram.DecisionResult;
import io.resys.limaone.program.FlowProgram.FlowExecutionStatus;
import io.resys.limaone.program.FlowProgram.FlowResult;
import io.resys.limaone.program.FlowProgram.FlowResultLog;
import io.resys.limaone.program.FlowTaskProgram.FlowTaskResult;
import io.resys.limaone.program.FlowProgram;
import io.resys.limaone.program.ImmutableFlowResult;
import io.resys.limaone.program.ImmutableFlowResultLog;
import io.resys.limaone.program.ProgramInput;
import io.resys.limaone.program.Runtime;
import io.resys.limaone.spi.program.assignment.AssignmentContext;
import io.resys.limaone.spi.program.expression.OperationFlowContext.FlowTaskExpressionContext;
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
  private final AtomicInteger sequence = new AtomicInteger(0);
  private final LocalDateTime start = LocalDateTime.now();
  private final Map<String, FlowResultLog> stack = new HashMap<>();
  private final StringBuilder shortHistory = new StringBuilder();
  
  public FlowProgramExecutor(Runtime runtime, ProgramInput input) {
    super();
    this.runtime = runtime;
    this.assignment = new AssignmentContext(runtime, input); 
  }

  interface ExecutorResult {}
  interface ExecutorProps {}

  @Value
  private static class MappingResults implements ExecutorResult {
    List<Map<String, Serializable>> values;
  }
  
  
  /**
   * Walk the entire Flow AST starting from the root statement
   */
  public FlowResult walk(Flow_AST flow, ExecutorProps ExecutorProps) {
    visit(flow.getStatement(), null);
    final var isArray = false;
    final FlowProgram.FlowResultLog last = null;
    
    return ImmutableFlowResult.builder()
      .logs(stack.values())
      .lastLogs(stack.values())
      .stepId("")
      .status(FlowExecutionStatus.COMPLETED)
      .accepts(assignment.getInitalizers().entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().getRaw())))
      .isReturnsCollection(isArray)
      .returns(Map.<String, Serializable>of("", ""))
      .shortHistory(shortHistory.toString())
      .build();
  }
  

  public ExecutorResult visitInputsStatement(InputsStatement statement, ExecutorProps ExecutorProps) {
    this.assignment.initalizers(statement);
    
    // Continue to next statement
    return visit(statement.getNext(), null);
  }
  

  public ExecutorResult visitManyTasksStatement(ManyTasksStatement statement, ExecutorProps ExecutorProps) {
    this.assignment.initalizers(statement);
    
    // Container for multiple tasks - continue to next
    return visit(statement.getNext(), null);
  }
  

  public ExecutorResult visitOneTaskStatement(OneTaskStatement statement, ExecutorProps ExecutorProps) {
    // Process individual task
    final var taskId = statement.getId();
    
    // Visit task body
    visit(statement.getBody(), null);
    
    // Visit next statement
    return visit(statement.getThen(), null);
  }
  

  public ExecutorResult visitEmptyBodyStatement(EmptyBodyStatement statement, ExecutorProps ExecutorProps) {
    newFlowResult((newResult -> newResult
        .stepId(statement.getTaskId())
        .status(FlowExecutionStatus.COMPLETED)
        .isReturnsCollection(false)
    ));
    return null;
  }
  

  public ExecutorResult visitDecisionTableStatement(DecisionTableStatement statement, ExecutorProps ExecutorProps) {
    // Process decision table execution
    final var dt = runtime.getBundle().queryDecisions()
        .name(statement.getDecisionTableName())
        .getOne();
    
    final var results = new ArrayList<DecisionResult>(); 
    for(final var inputs : visitMappingStatement(statement.getMapping(), null).getValues()) {
      try {
        
        final var result = dt.run(assignment.withInputs(inputs), runtime).andGetBody();
        results.add(result);
        
      } catch(Exception e) {
        throw new StatementException(e.getMessage(), statement, e);
      }
    }
    
    // TODO MEREGE
    return null;
  }
  

  public ExecutorResult visitFlowTaskStatement(FlowTaskStatement statement, ExecutorProps ExecutorProps) {
    // Process service/groovy task execution
    final var ft = runtime.getBundle().queryFlowTasks()
        .name(statement.getFlowTaskName())
        .getOne();
    
    final var results = new ArrayList<FlowTaskResult>(); 
    for(final var inputs : visitMappingStatement(statement.getMapping(), null).getValues()) {
      try {
        
        final var result = ft.run(assignment.withInputs(inputs), runtime).andGetBody();
        results.add(result);
        
      } catch(Exception e) {
        throw new StatementException(e.getMessage(), statement, e);
      }
    }
    
    // TODO MEREGE
    return null;
  }
  

  public ExecutorResult visitReturnsStatement(ReturnsStatement statement, ExecutorProps ExecutorProps) {
    // Process direct return
    boolean isCollection = statement.isCollection();
    
    // Visit return mapping
    final var inputs = visitMappingStatement(statement.getMapping(), null);
    
    try {


    } catch(Exception e) {
      throw new StatementException(e.getMessage(), statement, e);
    }
    // TODO MEREGE    
    return null;
  }
  

  public ExecutorResult visitSwitchStatement(SwitchStatement statement, ExecutorProps ExecutorProps) {
    
    for(final var mappingEntry : visitMappingStatement(statement.getMapping(), null).getValues()) {
      final var expressionContext = new FlowTaskExpressionContext() {
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
        final var condition = whenThen.getWhen().run(expressionContext);
        
        newFlowResult(newFlowResult -> newFlowResult
            .stepId(statement.getMapping().getTaskId())
            .status(FlowExecutionStatus.COMPLETED)
            .accepts(mappingEntry)
            .returns(Map.of("isMatch", (Boolean) condition.getValue())));
        
        if(Boolean.TRUE.equals(condition.getValue())) {
          isAtleastOneMatch = true;
          this.visit(whenThen.getThen(), null);
          break;
        }     
      }
      
      if(!isAtleastOneMatch) {
        log.debug("Flow switch: '" + statement.getMapping().getTaskId() + "' does not match any expressions!");
      }
    }

    return null;
  }
  

  public ExecutorResult visitCaseStatement(CaseStatement statement, ExecutorProps ExecutorProps) {
    // Process switch case condition
    if (statement.getWhen() != null) {
      // Access expression: statement.getWhen().getConstants(), etc.
    }
    
    // Visit next statement for this case
    return visit(statement.getThen(), null);
  }
  

  public ExecutorResult visitStartStatement(StartStatement statement, ExecutorProps ExecutorProps) {
    // Flow entry point
    return visit(statement.getFirstTask(), null);
  }
  

  public ExecutorResult visitEndStatement(EndStatement statement, ExecutorProps ExecutorProps) {
    // Flow termination
    return null;
  }
  

  public ExecutorResult visitPointerStatement(PointerStatement statement, ExecutorProps ExecutorProps) {
    // Reference to another task
    return visit(statement.getTask(), null);
  }
  

  public MappingResults visitMappingStatement(MappingStatement statement, ExecutorProps props) {
    return new MappingResults(assignment.assignTo(statement.getTaskId(), statement));
  }
  
  private void newFlowResult(Consumer<ImmutableFlowResultLog.Builder> callback) {
    final var builder = ImmutableFlowResultLog.builder()
        .id(sequence.incrementAndGet())
        .start(start)
        .isReturnsCollection(false)
        .end(LocalDateTime.now());
    
    callback.accept(builder);
    final var frame = builder.build();
    stack.put(frame.getStepId(), frame);
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
        return visitCaseStatement((CaseStatement) statement, props);
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
    
    public StatementException(String message, AnyStatement statement) {
      super(message);
      this.statement = statement;
    }
    public StatementException(String message, AnyStatement statement, Throwable cause) {
      super(message, cause);
      this.statement = statement;
    }
  }
}