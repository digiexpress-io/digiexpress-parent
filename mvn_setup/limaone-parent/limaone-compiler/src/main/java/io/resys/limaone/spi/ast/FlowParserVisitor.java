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

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.math.NumberUtils;

import com.google.common.hash.Hashing;
import com.ibm.icu.math.BigDecimal;

import io.resys.limaone.ast.AST_Parser.Dependency_AST;
import io.resys.limaone.ast.Flow_AST;
import io.resys.limaone.ast.Flow_AST.BodyStatement;
import io.resys.limaone.ast.Flow_AST.CaseStatement;
import io.resys.limaone.ast.Flow_AST.NextStatement;
import io.resys.limaone.ast.Flow_AST.OneTaskStatement;
import io.resys.limaone.ast.Flow_CST.YamlFlow;
import io.resys.limaone.ast.Flow_CST.YamlInput;
import io.resys.limaone.ast.Flow_CST.YamlSwitch;
import io.resys.limaone.ast.Flow_CST.YamlTask;
import io.resys.limaone.ast.Flow_CST.YamlTaskBody;
import io.resys.limaone.ast.ImmutableDependency_AST;
import io.resys.limaone.ast.ImmutableFlow_AST;
import io.resys.limaone.ast.ImmutableHeaders_AST;
import io.resys.limaone.ast.Yaml_CST.Yaml;
import io.resys.limaone.model.ImmutableModelError;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.model.ModelError;
import io.resys.limaone.model.Parameter;
import io.resys.limaone.model.Parameter.Direction;
import io.resys.limaone.model.Parameter.ValueType;
import io.resys.limaone.spi.ast.AST_ParserImpl.AST_ParserProps;
import io.resys.limaone.spi.ast.FlowStatementFactory.ImmutableCaseStatement;
import io.resys.limaone.spi.ast.FlowStatementFactory.ImmutableDecisionTableStatement;
import io.resys.limaone.spi.ast.FlowStatementFactory.ImmutableEmptyBodyStatement;
import io.resys.limaone.spi.ast.FlowStatementFactory.ImmutableEndStatement;
import io.resys.limaone.spi.ast.FlowStatementFactory.ImmutableFlowTaskStatement;
import io.resys.limaone.spi.ast.FlowStatementFactory.ImmutableInputsStatement;
import io.resys.limaone.spi.ast.FlowStatementFactory.ImmutableManyTasksStatement;
import io.resys.limaone.spi.ast.FlowStatementFactory.ImmutableMappingStatement;
import io.resys.limaone.spi.ast.FlowStatementFactory.ImmutableOneTaskStatement;
import io.resys.limaone.spi.ast.FlowStatementFactory.ImmutablePointerStatement;
import io.resys.limaone.spi.ast.FlowStatementFactory.ImmutableReturnsStatement;
import io.resys.limaone.spi.ast.FlowStatementFactory.ImmutableSwitchStatement;
import io.resys.limaone.spi.compiler.Compiler_Expression;
import io.resys.limaone.spi.parameter.Parameter_Factory;
import io.resys.limaone.yaml.YamlMapper;
import io.smallrye.mutiny.tuples.Tuple2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@RequiredArgsConstructor
@Slf4j
public class FlowParserVisitor {

  private final AST_ParserProps props;
  private final String src;
  private final String hash;

  private final Map<String, OneTaskStatement> steps = new HashMap<>();
  private final Map<String, YamlTask> tasksById = new HashMap<>();
  private final List<ModelError> errors = new ArrayList<>();
  private final List<Dependency_AST> dependencies = new ArrayList<>();
    

  public Flow_AST accept() {
    Objects.requireNonNull(src, () -> "src must be defined!");
    Objects.requireNonNull(hash, () -> "hash must be defined!");
    Objects.requireNonNull(props, () -> "props must be defined!");
    
    final var joined = this.src;
    final var hash = Hashing.murmur3_128().hashString(joined, StandardCharsets.UTF_8).toString();
    

    final var parsed = new CST_YamlParser<MutableYamlFlow>(props, new MutableYamlFlow()).parseCST(joined);
    final var cst = parsed.getItem1();
    final var cstExtraErrors = new CST_YamlFlowValidator(cst).validate();

    final Yaml id = cst.getId();
    final var firstTask = visitTasksById(cst);
    final var next = firstTask == null ? ImmutableEndStatement.getInstance() : new ImmutablePointerStatement(visitTask(firstTask));
    final var headers = headers(cst);
    
                
    return ImmutableFlow_AST.builder()
        .bodyType(Model.BodyType.FLOW)
        .hash(hash)
        .dependencies(dependencies)
        .statement(new ImmutableInputsStatement(headers.getAcceptDefs(), new ImmutableManyTasksStatement(next, steps)))
        .addAllErrors(parsed.getItem2())
        .addAllErrors(errors)
        .addAllErrors(cstExtraErrors)
        .name(id == null ? "": id.getValue())
        .parseTree(cst)
        .headers(headers)
        .build();
  }
  
  private ImmutableHeaders_AST headers(YamlFlow data) {
    Map<String, YamlInput> inputs = data.getInputs();

    int index = 0;
    Collection<Parameter> result = new ArrayList<>();
    for (Map.Entry<String, YamlInput> entry : inputs.entrySet()) {
      if (entry.getValue().getType() == null) {
        continue;
      }
      
      final var required = YamlMapper.getBooleanValue(entry.getValue().getRequired());
      try {
        ValueType valueType = ValueType.valueOf(entry.getValue().getType().getValue());
        
        result.add(Parameter_Factory.newParam()
            .id(entry.getValue().getStart() + "")
            .order(index++)
            .name(entry.getKey())
            .valueType(valueType)
            .direction(Direction.IN)
            .required(required)
            .values(YamlMapper.getStringValue(entry.getValue().getDebugValue()))
            .build());
        
      } catch (Exception e) {
        final String msg = String.format("Failed to convert data type from: %s, error: %s", entry.getValue().getType().getValue(), e.getMessage());
        log.error(msg);
        result.add(Parameter_Factory.newParam()
            .id(entry.getValue().getStart() + "")
            .order(index++)
            .name(entry.getKey())
            .valueType(ValueType.STRING) // fake it 
            .direction(Direction.IN)
            .required(required)
            .values(YamlMapper.getStringValue(entry.getValue().getDebugValue()))
            .build());
      }
    }
    return ImmutableHeaders_AST.builder().acceptDefs(result).build();
  }

  private YamlTask visitTasksById(YamlFlow data) {
    YamlTask firstTask = null;
    for(final var task : data.getTasks().values()) {
      tasksById.put(YamlMapper.getStringValue(task.getId()), task);
      if(task.getOrder() == 0) {
        firstTask = task;
      }
    }
    return firstTask;
  }

  private OneTaskStatement visitTask(YamlTask task) {
    final String taskId = YamlMapper.getStringValue(task.getId());
    if(steps.containsKey(taskId)) {
      return steps.get(taskId);
    }
    steps.put(taskId, null);
    
    final var body = visitStepBody(task);
    final var pointer = visitStepPointer(task);
    
    final var step = new ImmutableOneTaskStatement(taskId, body, pointer);
    steps.put(step.getId(), step);
    return step;
  }

  private BodyStatement visitStepBody(YamlTask task) {
    final var taskId = YamlMapper.getStringValue(task.getId());
    if(task.getDecisionTable() == null && task.getService() == null && task.getReturns() == null) {
      return new ImmutableEmptyBodyStatement(taskId);
    }
    
    final var collection = task.getReturns() != null ? 
        YamlMapper.getBooleanValue(task.getReturns().getCollection()) : 
        YamlMapper.getBooleanValue(task.getRef().getCollection());
    
    final var ref =  task.getReturns() != null ? 
        "" : 
        YamlMapper.getStringValue(task.getRef().getRef());
    

    final ImmutableMappingStatement inputsStmnt;
    if(task.getRef() != null) {

      // use reference input
      inputsStmnt = visitStepInputs(task.getRef(), taskId);
      

    } else {

      // use returns input mapping
      inputsStmnt = visitStepInputs(task.getReturns(), taskId);
    }
    
    if(task.getDecisionTable() != null) {
      
      dependencies.add(ImmutableDependency_AST.builder()
          .dependencyId(ref)
          .type(BodyType.DECISION_TABLE)
          .build());
      
      return new ImmutableDecisionTableStatement(ref, collection, inputsStmnt, taskId);
    } else if(task.getService() != null) {
      
      dependencies.add(ImmutableDependency_AST.builder()
          .dependencyId(ref)
          .type(BodyType.FLOW_TASK)
          .build());
      
      return new ImmutableFlowTaskStatement(ref, collection, inputsStmnt, taskId);
    } else {
      return new ImmutableReturnsStatement(collection, inputsStmnt, taskId);
    }
  }
  
  private ImmutableMappingStatement visitStepInputs(YamlTaskBody yaml, String taskId) {
    final var inputs = new HashMap<String, String>();
    final var literals = new HashMap<String, Serializable>();
    final var deconstruct = new ArrayList<String>();
    
    if (yaml.getObjectInput() != null) {
      deconstruct.add(yaml.getObjectInput());        
    }
    
    for (Map.Entry<String, Yaml> entry : yaml.getInputs().entrySet()) {
      if(entry.getKey().equals(yaml.getObjectInput())) {
        continue;
      }
      
      try {
        final var syntax = entry.getValue().getSyntax().trim();
        final var keyword = entry.getKey();
        final var fragment = syntax
            .substring(syntax.indexOf(keyword) + keyword.length()+1)
            .trim();
        
       // string literal
        if(fragment.startsWith("\"") && fragment.endsWith("\"")) {
          literals.put(entry.getKey(), YamlMapper.getStringValue(entry.getValue()));
          inputs.put(entry.getKey(), ImmutableMappingStatement.VALUE_IS_LITERAL);
          continue;
          
        // boolean literal
        } else if(fragment.toLowerCase().equals("true") || fragment.toLowerCase().equals("false")) {
          literals.put(entry.getKey(), Boolean.parseBoolean(fragment.toLowerCase()));
          inputs.put(entry.getKey(), ImmutableMappingStatement.VALUE_IS_LITERAL);
          continue;
          
        // integer literal
        } else if(NumberUtils.isDigits(fragment)) {
          literals.put(entry.getKey(), Integer.parseInt(fragment));
          inputs.put(entry.getKey(), ImmutableMappingStatement.VALUE_IS_LITERAL);
          continue;
        
        // big decimal literal
        } else if(NumberUtils.isParsable(fragment)) {
          literals.put(entry.getKey(), new BigDecimal(fragment));
          inputs.put(entry.getKey(), ImmutableMappingStatement.VALUE_IS_LITERAL);
          continue;
        }
      } catch(Exception e) {
        log.trace("Failed to parse mapping props for: {}", entry.getValue());
      }

      inputs.put(entry.getKey(), YamlMapper.getStringValue(entry.getValue()));
    }
    
    final var inputsStmnt = new ImmutableMappingStatement(inputs, deconstruct, taskId, literals);
    return inputsStmnt;
  }

  private NextStatement visitStepPointer(YamlTask task) {
    if(task.getSwitch().isEmpty()) {
      final var thenId = YamlMapper.getStringValue(task.getThen());
      if(thenId != null && !thenId.equalsIgnoreCase(MutableYamlFlow.VALUE_END)) {
        return new ImmutablePointerStatement(visitTask(tasksById.get(thenId))); 
      }
      return ImmutableEndStatement.getInstance();

    }
    
    final var inputMappings = new HashMap<String, String>();
    final var cases = task.getSwitch().values().stream()
      .sorted((o1, o2) -> Integer.compare(o1.getOrder(), o2.getOrder()))
      .map(d -> {
        final var switchTuple = visitSwitchNode(d);
        final var condition = switchTuple.getItem2();
        final var stepId = switchTuple.getItem1();
        
        if(!stepId.equalsIgnoreCase(MutableYamlFlow.VALUE_END)) {
          visitTask(tasksById.get(stepId));
        }
        condition.getWhen().getConstants().forEach(e -> inputMappings.put(e, e));
        return condition;
      }).toList();
    
    final var taskId = YamlMapper.getStringValue(task.getId());
    return new ImmutableSwitchStatement(cases, new ImmutableMappingStatement(inputMappings, Collections.emptyList(), taskId, null));
  }
  

  
  private Tuple2<String, CaseStatement> visitSwitchNode(YamlSwitch decision) {
    
    final var decisionId = decision.getKeyword();
    final var when = YamlMapper.getStringValue(decision.getWhen());
    final var thenValue = YamlMapper.getStringValue(decision.getThen());    
    try {
      final var isTrue = when == null || when.isEmpty();
      final var expression = isTrue ? 
          Compiler_Expression.build("true", ValueType.FLOW_CONTEXT) :
          Compiler_Expression.build(when, ValueType.FLOW_CONTEXT);      

      final String stepId;
      final NextStatement next;
      if(MutableYamlFlow.VALUE_END.equalsIgnoreCase(thenValue)) {
        stepId = MutableYamlFlow.VALUE_END;
        next = ImmutableEndStatement.getInstance();
      } else {
        stepId = MutableYamlFlow.VALUE_NEXT.equalsIgnoreCase(thenValue) ? 
            tasksById.values().stream()
              .sorted((a, b) -> Integer.compare(a.getStart(), b.getStart()))
              .filter(e -> e.getOrder() > decision.getOrder())
              .findFirst()
              .map(task -> YamlMapper.getStringValue(task.getId()))
              .orElse(MutableYamlFlow.VALUE_END)
            : thenValue;

        next = MutableYamlFlow.VALUE_END.equalsIgnoreCase(stepId) ? 
            ImmutableEndStatement.getInstance() : 
            new ImmutablePointerStatement(visitTask(Objects.requireNonNull(tasksById.get(stepId), 
                () -> "Can't find task by id: " + stepId)));
      } 
      
      return Tuple2.of(stepId, new ImmutableCaseStatement(expression, next));
    } catch(Exception e) {
      final var message = "Failed to evaluate expression: \"" + when + "\" in flow decision: " + decisionId + "!" + System.lineSeparator() + e.getMessage();
      this.errors.add(ImmutableModelError.builder()
          .line(decision.getStart())
          .msg(message)
          .exception(e)
          .build());
      
      return Tuple2.of(MutableYamlFlow.VALUE_END, new ImmutableCaseStatement(Compiler_Expression.build("true", ValueType.FLOW_CONTEXT), ImmutableEndStatement.getInstance())); 
    } 
  }

}
