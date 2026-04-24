package io.resys.limaone.spi.ast;

import java.io.Serializable;

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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.resys.limaone.ast.Flow_AST.BodyStatement;
import io.resys.limaone.ast.Flow_AST.CaseStatement;
import io.resys.limaone.ast.Flow_AST.DecisionTableStatement;
import io.resys.limaone.ast.Flow_AST.EmptyBodyStatement;
import io.resys.limaone.ast.Flow_AST.EndStatement;
import io.resys.limaone.ast.Flow_AST.FlowTaskStatement;
import io.resys.limaone.ast.Flow_AST.FormStatement;
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

public class FlowStatementFactory {

  @Getter
  public static class ImmutableEndStatement implements EndStatement {
    private static final long serialVersionUID = -8010199437759830713L;
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
    private static final long serialVersionUID = 1108990577752137814L;
    private final OneTaskStatement firstTask;
    
    public ImmutableStartStatement(OneTaskStatement firstTask) {
      super();
      this.firstTask = firstTask;
    }
  }

  @Getter
  public static class ImmutableOneTaskStatement implements OneTaskStatement {
    private static final long serialVersionUID = -4820409328592900687L;
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
    private static final long serialVersionUID = -8026683217423486066L;
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
    private static final long serialVersionUID = -1896036205391174027L;
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
    private static final long serialVersionUID = -7715022665761301866L;
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
    private static final long serialVersionUID = -6614886981666233092L;
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
    private static final long serialVersionUID = 9674604747977970L;
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
    private static final long serialVersionUID = 1166458809369928434L;
    private final List<Parameter> parameters;
    private final ManyTasksStatement next;
    
    public ImmutableInputsStatement(List<Parameter> parameters, ManyTasksStatement next) {
      super();
      this.parameters = Collections.unmodifiableList(parameters);
      this.next = next;
    }
  }

  @Getter
  public static class ImmutableFormStatement implements FormStatement {
    private static final long serialVersionUID = 4827361509283746150L;
    private final String formRefInputName;
    private final String returnsCode;
    private final Class<?> compiledClass;
    private final Map<String, String> outputFields;
    private final String taskId;
    
    public ImmutableFormStatement(String formRefInputName, String returnsCode, Class<?> compiledClass, Map<String, String> outputFields, String taskId) {
      super();
      this.formRefInputName = formRefInputName;
      this.returnsCode = returnsCode;
      this.compiledClass = compiledClass;
      this.outputFields = Collections.unmodifiableMap(outputFields);
      this.taskId = taskId;
    }
  }

  @Getter
  public static class ImmutableEmptyBodyStatement implements EmptyBodyStatement {
    private static final long serialVersionUID = -6532077245354447132L;
    private final String taskId;
    
    public ImmutableEmptyBodyStatement(String taskId) {
      super();
      this.taskId = taskId;
    }
  }

  @Getter
  public static class ImmutableSwitchStatement implements SwitchStatement {
    private static final long serialVersionUID = 1630622880720461598L;
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
    private static final long serialVersionUID = 5563386829409749385L;
    private final Map<String, String> assignments;
    private final List<String> deconstructors; 
    private final boolean deconstructing;
    private final String taskId;
    private final Map<String, Serializable> literalValue;
    public static final String VALUE_IS_LITERAL = "__get_from_literal"; 
    
    public ImmutableMappingStatement(
        Map<String, String> assignments, 
        List<String> deconstructors, 
        String taskId,
        Map<String, Serializable> literalValue) {
      super();
      this.assignments = Collections.unmodifiableMap(assignments);
      this.deconstructing = !deconstructors.isEmpty();
      this.deconstructors = deconstructors;
      this.taskId = taskId;
      this.literalValue = literalValue == null ? Collections.emptyMap() : Collections.unmodifiableMap(literalValue);
    }
    @Override
    public boolean isLiteral() {
      return literalValue.isEmpty();
    }
  }

  @Getter
  public static class ImmutablePointerStatement implements PointerStatement {
    private static final long serialVersionUID = 1402373499412189858L;
    private final OneTaskStatement task;
    
    public ImmutablePointerStatement(OneTaskStatement task) {
      super();
      this.task = task;
    }
  }
}
