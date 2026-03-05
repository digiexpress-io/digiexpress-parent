package io.resys.limaone.spi.ast;

import io.resys.limaone.ast.Flow_AST.BodyStatement;
import io.resys.limaone.ast.Flow_AST.CaseStatement;
import io.resys.limaone.ast.Flow_AST.DecisionTableStatement;
import io.resys.limaone.ast.Flow_AST.EmptyBodyStatement;
import io.resys.limaone.ast.Flow_AST.EndStatement;
import io.resys.limaone.ast.Flow_AST.FlowTaskStatement;
import io.resys.limaone.ast.Flow_AST.InputsStatement;
import io.resys.limaone.ast.Flow_AST.ManyTasksStatement;
import io.resys.limaone.ast.Flow_AST.MappingStatement;
import io.resys.limaone.ast.Flow_AST.NextStatement;
import io.resys.limaone.ast.Flow_AST.OneTaskStatement;
import io.resys.limaone.ast.Flow_AST.PointerStatement;
import io.resys.limaone.ast.Flow_AST.ReturnsStatement;
import io.resys.limaone.ast.Flow_AST.StartStatement;
import io.resys.limaone.ast.Flow_AST.SwitchStatement;
import io.resys.limaone.model.Parameter;
import io.resys.limaone.program.ExpressionProgram;
import jakarta.annotation.Nullable;
import lombok.Getter;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class FlowStatementFactory {

  @Getter
  public static class ImmutableEndStatement implements EndStatement {
    private static final ImmutableEndStatement INSTANCE = new ImmutableEndStatement();
    
    private ImmutableEndStatement() {
      super();
    }
    
    public static ImmutableEndStatement getInstance() {
      return INSTANCE;
    }
  }

  @Getter
  public static class ImmutableStartStatement implements StartStatement {
    private final OneTaskStatement firstTask;
    
    public ImmutableStartStatement(OneTaskStatement firstTask) {
      super();
      this.firstTask = firstTask;
    }
  }

  @Getter
  public static class ImmutableOneTaskStatement implements OneTaskStatement {
    private final String id;
    private final BodyStatement body;
    private final NextStatement then;
    
    public ImmutableOneTaskStatement(String id, BodyStatement body, NextStatement then) {
      super();
      this.id = id;
      this.body = body;
      this.then = then;
    }
  }

  @Getter
  public static class ImmutableDecisionTableStatement implements DecisionTableStatement {
    private final boolean collection;
    private final MappingStatement mapping;
    private final String decisionTableName;
    private final String taskId;
    
    public ImmutableDecisionTableStatement(String decisionTableName, boolean collection, MappingStatement mapping, String taskId) {
      super();
      this.collection = collection;
      this.mapping = mapping;
      this.decisionTableName = decisionTableName;
      this.taskId = taskId;
    }
  }

  @Getter
  public static class ImmutableManyTasksStatement implements ManyTasksStatement {
    private final NextStatement next;
    private final Map<String, OneTaskStatement> tasks;
    
    public ImmutableManyTasksStatement(NextStatement next, Map<String, OneTaskStatement> tasks) {
      super();
      this.next = next;
      this.tasks = Collections.unmodifiableMap(tasks);
    }
  }

  @Getter
  public static class ImmutableFlowTaskStatement implements FlowTaskStatement {
    private final boolean collection;
    private final MappingStatement mapping;
    private final String flowTaskName;
    private final String taskId;
    
    public ImmutableFlowTaskStatement(String flowTaskName, boolean collection, MappingStatement mapping, String taskId) {
      super();
      this.collection = collection;
      this.mapping = mapping;
      this.flowTaskName = flowTaskName;
      this.taskId = taskId;
    }
  }

  @Getter
  public static class ImmutableCaseStatement implements CaseStatement {
    @Nullable
    private final ExpressionProgram when;
    private final NextStatement then;
    
    public ImmutableCaseStatement(@Nullable ExpressionProgram when, NextStatement then) {
      super();
      this.when = when;
      this.then = then;
    }
  }

  @Getter
  public static class ImmutableReturnsStatement implements ReturnsStatement {
    private final boolean collection;
    private final MappingStatement mapping;
    private final String taskId;
    
    public ImmutableReturnsStatement(boolean collection, MappingStatement mapping, String taskId) {
      super();
      this.collection = collection;
      this.mapping = mapping;
      this.taskId = taskId;
    }
  }

  @Getter
  public static class ImmutableInputsStatement implements InputsStatement {
    private final List<Parameter> parameters;
    private final ManyTasksStatement next;
    
    public ImmutableInputsStatement(List<Parameter> parameters, ManyTasksStatement next) {
      super();
      this.parameters = Collections.unmodifiableList(parameters);
      this.next = next;
    }
  }

  @Getter
  public static class ImmutableEmptyBodyStatement implements EmptyBodyStatement {
    private final String taskId;
    
    public ImmutableEmptyBodyStatement(String taskId) {
      super();
      this.taskId = taskId;
    }
  }

  @Getter
  public static class ImmutableSwitchStatement implements SwitchStatement {
    private final List<CaseStatement> cases;
    private final MappingStatement mapping;
    
    public ImmutableSwitchStatement(List<CaseStatement> cases, MappingStatement mapping) {
      super();
      this.cases = Collections.unmodifiableList(cases);
      this.mapping = mapping;
    }

    @Override
    public String getTaskId() {
      return mapping.getTaskId();
    }
  }

  @Getter
  public static class ImmutableMappingStatement implements MappingStatement {
    private final Map<String, String> assignments;
    private final List<String> deconstructors; 
    private final boolean deconstructing;
    private final String taskId;
    
    public ImmutableMappingStatement(Map<String, String> assignments, List<String> deconstructors, String taskId) {
      super();
      this.assignments = Collections.unmodifiableMap(assignments);
      this.deconstructing = !deconstructors.isEmpty();
      this.deconstructors = deconstructors;
      this.taskId = taskId;
    }
  }

  @Getter
  public static class ImmutablePointerStatement implements PointerStatement {
    private final OneTaskStatement task;
    
    public ImmutablePointerStatement(OneTaskStatement task) {
      super();
      this.task = task;
    }
  }
}
