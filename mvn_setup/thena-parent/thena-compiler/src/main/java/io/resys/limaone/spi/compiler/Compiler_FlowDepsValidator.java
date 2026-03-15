package io.resys.limaone.spi.compiler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.resys.limaone.ast.AST_Parser.Dependency_AST;
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
import io.resys.limaone.model.ImmutableModelError;
import io.resys.limaone.model.ModelError;
import io.resys.limaone.model.Parameter;
import io.resys.limaone.spi.compiler.CompilableUnit.Artifact;
import lombok.RequiredArgsConstructor;
import lombok.Value;


@RequiredArgsConstructor
public class Compiler_FlowDepsValidator {
  private final Artifact artifact;
  private final Flow_AST ast;
  private final Map<String, Dependency_AST> childrenByName = new HashMap<>();
    
  private final ValidatorResult REACHED_END = new ValidatorResult() {};
  private final ValidatorProps NO_PROPS = new ValidatorProps() {};
  private final Map<String, Parameter> flowInputs = new HashMap<>();
  private final Map<String, OneTaskStatement> tasks = new HashMap<>();
  private final Map<String, Map<String, Parameter>> acceptDefs = new HashMap<>();
  private final Map<String, Map<String, Parameter>> returnDefs = new HashMap<>();
  private final List<DepToValidate> toValidate = new ArrayList<>();
  
  private final List<ModelError> errors = new ArrayList<>();
  

  interface ValidatorResult {}
  interface ValidatorProps {}
  
  @Value
  public static class MappingValidatorProps implements ValidatorProps {
    Optional<String> dependencyId; 
    StatementType parent;
  }

  /**
   * Walk the entire Flow AST starting from the root statement
   */
  public List<ModelError> walk() {
    artifact.getChildDeps().forEach(dep -> childrenByName.put(dep.getDependencyId(), dep));
    visit(ast.getStatement(), NO_PROPS);
    toValidate.forEach(this::visitDepToValidate);
    return errors;
  }
  

  public ValidatorResult visitInputsStatement(InputsStatement statement, ValidatorProps ValidatorProps) {
    statement.getParameters().forEach(p -> flowInputs.put(p.getName(), p));
    
    // Continue to next statement
    return visit(statement.getNext(), NO_PROPS);
  }
  

  public ValidatorResult visitManyTasksStatement(ManyTasksStatement statement, ValidatorProps ValidatorProps) {
    
    for (final var task : statement.getTasks().values()) {
      tasks.put(task.getId(), task); 
    }
    
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
    visitMappingStatement(statement.getMapping(), new MappingValidatorProps(
        Optional.of(statement.getDecisionTableName()),
        StatementType.BODY_DECISION_TABLE
    ));
    return REACHED_END;
  }
  

  public ValidatorResult visitFlowTaskStatement(FlowTaskStatement statement, ValidatorProps ValidatorProps) {
    visitMappingStatement(statement.getMapping(), new MappingValidatorProps(
        Optional.of(statement.getFlowTaskName()),
        StatementType.BODY_FLOW_TASK
    ));
    return REACHED_END;
  }
  

  public ValidatorResult visitReturnsStatement(ReturnsStatement statement, ValidatorProps ValidatorProps) {
    visitMappingStatement(statement.getMapping(), new MappingValidatorProps(
        Optional.empty(),
        StatementType.BODY_RETURNS
    ));
    return REACHED_END;
  }
  

  public ValidatorResult visitSwitchStatement(SwitchStatement statement, ValidatorProps ValidatorProps) {
    visitMappingStatement(statement.getMapping(), new MappingValidatorProps(
        Optional.empty(),
        StatementType.BODY_SWITCH
    ));
    return REACHED_END;
  }
  

  public ValidatorResult visitCaseStatement(CaseStatement statement, ValidatorProps props) {
    return REACHED_END;
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
  

  public ValidatorResult visitMappingStatement(MappingStatement statement, MappingValidatorProps props) {
    final var taskId = statement.getTaskId();
    acceptDefs.put(taskId, new HashMap<>());
    returnDefs.put(taskId, new HashMap<>());
    
    
    if(props.getDependencyId().isPresent()) {
      final var childDep = this.childrenByName.get(props.getDependencyId().get());
      if(childDep == null) {
        errors.add(ImmutableModelError.builder()
            .msg("Task: '" + statement.getTaskId() + "' has ref: " + props.getDependencyId().get() + " to unknown asset!")
            .build());
        return REACHED_END;
      }
      
      if(childDep.getArtifactAst().isEmpty()) {
        errors.add(ImmutableModelError.builder()
            .msg("Task: '" + statement.getTaskId() + "' has ref: " + childDep.getDependencyId() + " to unknown asset!")
            .build());
        return REACHED_END;
      }
      
      final var headers = childDep.getArtifactAst().get().getHeaders();
      headers.getAcceptDefs().forEach(e -> acceptDefs.get(taskId).put(e.getName(), e));
      headers.getReturnDefs().forEach(e -> returnDefs.get(taskId).put(e.getName(), e));
    }
    this.toValidate.add(new DepToValidate(statement, props.getParent()));
    return REACHED_END;
  }
  
  public ValidatorResult visitDepToValidate(DepToValidate depToValidate) {
    final var statement = depToValidate.getStatement();
    for(final var mapping : statement.getAssignments().entrySet()) {
      final var fromPath = mapping.getValue().split("\\.");
      final var to = mapping.getKey();
      final var fromTask = fromPath[0];
      final var from = fromPath.length > 1 ? fromPath[1] : fromPath[0];
      final var toTask = depToValidate.getStatement().getTaskId();
      final var toParam = acceptDefs.get(toTask).get(to);
      
      final Parameter fromParam;
      if(flowInputs.containsKey(from)) {
        fromParam = flowInputs.get(from);
      } else if(tasks.containsKey(fromTask)) {
        fromParam = Optional.ofNullable(returnDefs.get(fromTask)).map(e -> e.get(from)).orElse(null);
      } else {
        fromParam = null;
      }
      
      visitMappingProp(Optional.ofNullable(fromParam), Optional.ofNullable(toParam), statement, depToValidate, to, from);
    }
    
    return REACHED_END;
  }
  
  public void visitMappingProp(
      Optional<Parameter> from, Optional<Parameter> to, 
      MappingStatement statement, DepToValidate props, 
      String toName, String fromName) {
    
    if(from.isPresent() && to.isPresent() && 
      !to.get().getValueType().equals(from.get().getValueType())) {
      
      errors.add(ImmutableModelError.builder()
          .msg("Task: '" + statement.getTaskId() + "' has type mismatch " + 
              "@" + to.get().getName() + ": " + to.get().getValueType().name().toLowerCase() + 
              " <> " +
              "@" + from.get().getName() + ": " + from.get().getValueType().name().toLowerCase())
          .build());
      
      return;
    }
    
    if(to.isEmpty()) {
      errors.add(ImmutableModelError.builder()
          .msg("Task: '" + statement.getTaskId() + "' has unknown parameter @to: " + toName)
          .build());
      return;
    }
    if(from.isEmpty()) {
      errors.add(ImmutableModelError.builder()
          .msg("Task: '" + statement.getTaskId() + "' has unknown parameter @from: " + fromName)
          .build());
      return;
    }
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
        return visitMappingStatement((MappingStatement) statement, (MappingValidatorProps) props);
      default:
        throw new IllegalArgumentException("Unknown statement type: " + statement.getType());
    }
  }
  
  @Value
  private static class DepToValidate {
    MappingStatement statement; 
    StatementType parent;
  }
}
