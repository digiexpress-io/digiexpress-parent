package io.resys.limaone.spi.ast;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import com.google.common.hash.Hashing;

import io.resys.limaone.ast.AST_Parser;
import io.resys.limaone.ast.AST_Parser.FlowParser;
import io.resys.limaone.ast.Flow_AST;
import io.resys.limaone.ast.Flow_AST.BodyStatement;
import io.resys.limaone.ast.Flow_AST.CaseStatement;
import io.resys.limaone.ast.Flow_AST.NextStatement;
import io.resys.limaone.ast.Flow_AST.OneTaskStatement;
import io.resys.limaone.ast.Flow_CST.YamlInput;
import io.resys.limaone.ast.Flow_CST.YamlFlow;
import io.resys.limaone.ast.Flow_CST.YamlSwitch;
import io.resys.limaone.ast.Flow_CST.YamlTask;
import io.resys.limaone.ast.ImmutableFlow_AST;
import io.resys.limaone.ast.ImmutableHeaders_AST;
import io.resys.limaone.ast.Yaml_CST.Yaml;
import io.resys.limaone.model.ImmutableModelError;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.ModelError;
import io.resys.limaone.model.Parameter;
import io.resys.limaone.model.Parameter.Direction;
import io.resys.limaone.model.Parameter.ValueType;
import io.resys.limaone.spi.LocalCache;
import io.resys.limaone.spi.LocalCache.Flow_AST_CacheKey;
import io.resys.limaone.spi.ast.AST_ParserImpl.AST_ParserProps;
import io.resys.limaone.spi.ast.flow.ImmutableCaseStatement;
import io.resys.limaone.spi.ast.flow.ImmutableDecisionTableStatement;
import io.resys.limaone.spi.ast.flow.ImmutableEmptyBodyStatement;
import io.resys.limaone.spi.ast.flow.ImmutableEndStatement;
import io.resys.limaone.spi.ast.flow.ImmutableFlowTaskStatement;
import io.resys.limaone.spi.ast.flow.ImmutableInputsStatement;
import io.resys.limaone.spi.ast.flow.ImmutableManyTasksStatement;
import io.resys.limaone.spi.ast.flow.ImmutableMappingStatement;
import io.resys.limaone.spi.ast.flow.ImmutableOneTaskStatement;
import io.resys.limaone.spi.ast.flow.ImmutablePointerStatement;
import io.resys.limaone.spi.ast.flow.ImmutableReturnsStatement;
import io.resys.limaone.spi.ast.flow.ImmutableSwitchStatement;
import io.resys.limaone.spi.ast.flow.MutableYamlFlow;
import io.resys.limaone.spi.ast.flow.YamlMapper;
import io.resys.limaone.spi.compiler.Compiler_Expression;
import io.resys.limaone.spi.parameter.Parameter_Factory;
import io.smallrye.mutiny.tuples.Tuple2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@RequiredArgsConstructor
@Slf4j
public class FlowParserImpl implements AST_Parser.FlowParser {

  private final AST_ParserProps props;
  private final List<String> src = new ArrayList<>();

  private final Map<String, OneTaskStatement> steps = new HashMap<>();
  private final Map<String, YamlTask> tasksById = new HashMap<>();
  private final List<ModelError> errors = new ArrayList<>();
  
  private String id;
  
  @Override
  public FlowParserImpl syntax(String src) {
    if (src == null) {
      return this;
    }
    this.src.add(src);
    return this;
  }
  @Override
  public FlowParser id(String id) {
    this.id = id;
    return this;
  }
  @Override
  public Flow_AST parse() {
    Objects.requireNonNull(id, () -> "id must be defined!");
    
    final var joined = String.join(System.lineSeparator(), this.src);
    final var hash = Hashing.murmur3_128().hashString(joined, StandardCharsets.UTF_8).toString();
    final var cacheKey = new Flow_AST_CacheKey(hash);
    final Function<Flow_AST_CacheKey, Flow_AST> mappingFunction = (k) -> {
      
      final var cst = new FlowParserCST(props).parseCST(joined);
      final Yaml id = cst.getItem1().getId();
      final var firstTask = visitTasksById(cst.getItem1());
      final var next = firstTask == null ? ImmutableEndStatement.getInstance() : new ImmutablePointerStatement(visitTask(firstTask));
      final var headers = headers(cst.getItem1());
      
                  
      return ImmutableFlow_AST.builder()
          .id(this.id)
          .bodyType(Model.BodyType.FLOW)
          .hash(hash)
          .statement(new ImmutableInputsStatement(headers.getAcceptDefs(), new ImmutableManyTasksStatement(next, steps)))
          .errors(cst.getItem2())
          .addAllErrors(errors)
          .name(id == null ? "": id.getValue())
          .parseTree(cst.getItem1())
          .headers(headers)
          .build();
    };
    return LocalCache.computeIfAbsent(cacheKey, mappingFunction);

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
    
    final var collection = task.getReturns() != null ? YamlMapper.getBooleanValue(task.getReturns().getCollection()) : YamlMapper.getBooleanValue(task.getRef().getCollection());
    final var ref =  task.getReturns() != null ? "" : YamlMapper.getStringValue(task.getRef().getRef());
    
    final var inputs = new HashMap<String, String>();
    final var deconstruct = new ArrayList<String>();
    
    // use reference input
    if(task.getRef() != null) {
      if (task.getRef().getObjectInput() != null) {
        deconstruct.add(task.getRef().getObjectInput());
      }
      for (Map.Entry<String, Yaml> entry : task.getRef().getInputs().entrySet()) {
        if(entry.getKey().equals(task.getRef().getObjectInput())) {
          continue;
        }
        inputs.put(entry.getKey(), YamlMapper.getStringValue(entry.getValue()));
      }

      // use returns input mapping
    } else {
      if (task.getReturns().getObjectInput() != null) {
        deconstruct.add(task.getReturns().getObjectInput());        
      }
      
      for (Map.Entry<String, Yaml> entry : task.getReturns().getInputs().entrySet()) {
        if(entry.getKey().equals(task.getReturns().getObjectInput())) {
          continue;
        }
        inputs.put(entry.getKey(), YamlMapper.getStringValue(entry.getValue()));
      }
    }
    
    final var inputsStmnt = new ImmutableMappingStatement(inputs, deconstruct, taskId);
    if(task.getDecisionTable() != null) {
      return new ImmutableDecisionTableStatement(ref, collection, inputsStmnt, taskId);
    } else if(task.getService() != null) {
      return new ImmutableFlowTaskStatement(ref, collection, inputsStmnt, taskId);
    } else {
      return new ImmutableReturnsStatement(collection, inputsStmnt, taskId);
    }
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
    return new ImmutableSwitchStatement(cases, new ImmutableMappingStatement(inputMappings, Collections.emptyList(), taskId));
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
