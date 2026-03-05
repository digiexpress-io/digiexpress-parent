package io.resys.limaone.spi.compiler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
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
import io.resys.limaone.ast.Flow_AST.StatementType;
import io.resys.limaone.ast.Flow_AST.SwitchStatement;
import io.resys.limaone.model.Parameter;
import io.resys.limaone.spi.compiler.CompilableUnit.NewArtifact;
import io.resys.limaone.spi.program.FlowProgramExecutor.StatementException;
import io.resys.limaone.spi.program.expression.OperationContext.ExternalContext;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class Compiler_FlowDepsValidator {
  private final NewArtifact resolution;
  private final Flow_AST ast;
  
    
  private final ValidatorResult REACHED_END = new ValidatorResult() {};
  private final ValidatorProps NO_PROPS = new ValidatorProps() {};

  private final Map<String, Parameter> flowInputs = new HashMap<>();
  

  interface ValidatorResult {}
  interface ValidatorProps {}

  /**
   * Walk the entire Flow AST starting from the root statement
   */
  public ValidatorResult walk(Flow_AST flow, ValidatorProps ValidatorProps) {
    visit(flow.getStatement(), NO_PROPS);
    return REACHED_END;
  }
  

  public ValidatorResult visitInputsStatement(InputsStatement statement, ValidatorProps ValidatorProps) {
    statement.getParameters().forEach(p -> flowInputs.put(p.getName(), p));
    
    // Continue to next statement
    return visit(statement.getNext(), NO_PROPS);
  }
  

  public ValidatorResult visitManyTasksStatement(ManyTasksStatement statement, ValidatorProps ValidatorProps) {
    
    // Container for multiple tasks - continue to next
    return visit(statement.getNext(), NO_PROPS);
  }
  

  public ValidatorResult visitOneTaskStatement(OneTaskStatement statement, ValidatorProps ValidatorProps) {
    // Visit task body
    if(statement.getThen().getType() != StatementType.BODY_SWITCH) {
      visit(statement.getBody(), NO_PROPS);  
    }
    
    // Visit next statement
    return visit(statement.getThen(), NO_PROPS);
  }
  

  public ValidatorResult visitEmptyBodyStatement(EmptyBodyStatement statement, ValidatorProps ValidatorProps) {
    return REACHED_END;
  }
  

  public ValidatorResult visitDecisionTableStatement(DecisionTableStatement statement, ValidatorProps ValidatorProps) {
    // Process decision table execution
    final var dt = runtime.getBundle().queryDecisions()
        .name(statement.getDecisionTableName())
        .getOne();
    
    final var sets = visitMappingStatement(statement.getMapping(), NO_PROPS).getValues();
    for(final var inputs : sets) {
      try {
        final LocalDateTime start = LocalDateTime.now();
        final var result = dt.run(assignment.withInputs(inputs), runtime).andGetBody();
        
        final var newFrame = stack.newFrame(statement, inputs, result, start);
        assignment.assignFromTask(newFrame);
        
      } catch(Exception e) {
        throw new StatementException(e.getMessage(), statement, inputs, e);
      }
    }
    return REACHED_END;
  }
  

  public ValidatorResult visitFlowTaskStatement(FlowTaskStatement statement, ValidatorProps ValidatorProps) {
    // Process service/groovy task execution
    final var ft = runtime.getBundle().queryFlowTasks()
        .name(statement.getFlowTaskName())
        .getOne();
    
    final var sets = visitMappingStatement(statement.getMapping(), NO_PROPS).getValues();
    for(final var inputs : sets) {
      try {
        final LocalDateTime start = LocalDateTime.now();
        final var result = ft.run(assignment.withInputs(inputs), runtime).andGetBody();
        
        final var newFrame = stack.newFrame(statement, inputs, result, start);
        assignment.assignFromTask(newFrame);
        
      } catch(Exception e) {
        throw new StatementException(e.getMessage(), statement, inputs, e);
      }
    }
    
    return REACHED_END;
  }
  

  public ValidatorResult visitReturnsStatement(ReturnsStatement statement, ValidatorProps ValidatorProps) {
    
    // Visit return mapping
    final var sets = visitMappingStatement(statement.getMapping(), NO_PROPS).getValues();
    for(final var inputs : sets) {
      try {
        final LocalDateTime start = LocalDateTime.now();
        final var newFrame = stack.newFrame(statement, inputs, start);
        assignment.assignFromTask(newFrame);
      } catch(Exception e) {
        throw new StatementException(e.getMessage(), statement, inputs, e);
      }
    }
    return REACHED_END;
  }
  

  public ValidatorResult visitSwitchStatement(SwitchStatement statement, ValidatorProps ValidatorProps) {
    
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
  

  public ValidatorResult visitCaseStatement(CaseStatement statement, ValidatorProps props) {

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
  

  public ValidatorResult visitStartStatement(StartStatement statement, ValidatorProps ValidatorProps) {
    // Flow entry point
    return visit(statement.getFirstTask(), NO_PROPS);
  }
  

  public ValidatorResult visitEndStatement(EndStatement statement, ValidatorProps ValidatorProps) {
    // Flow termination
    return REACHED_END;
  }
  

  public ValidatorResult visitPointerStatement(PointerStatement statement, ValidatorProps ValidatorProps) {
    // Reference to another task
    return visit(statement.getTask(), NO_PROPS);
  }
  

  public ValidatorResult visitMappingStatement(MappingStatement statement, ValidatorProps props) {
    return new MappingResults(assignment.mapTo(statement.getTaskId(), statement));
  }
  

  
  public ValidatorResult visit(AnyStatement statement, ValidatorProps props) {
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
}
