package io.resys.limaone.spi.compiler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import io.resys.limaone.ast.AST_Parser;
import io.resys.limaone.ast.Flow_AST;
import io.resys.limaone.ast.Flow_CST.Yaml;
import io.resys.limaone.ast.Flow_CST.YamlSwitch;
import io.resys.limaone.ast.Flow_CST.YamlTask;
import io.resys.limaone.ast.Simple_AST;
import io.resys.limaone.model.Flow;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.ModelWorld;
import io.resys.limaone.model.Parameter.ValueType;
import io.resys.limaone.program.FlowProgram.FlowProgramStep;
import io.resys.limaone.program.FlowProgram.FlowProgramStepBody;
import io.resys.limaone.program.FlowProgram.FlowProgramStepConditionalThenPointer;
import io.resys.limaone.program.FlowProgram.FlowProgramStepPointer;
import io.resys.limaone.program.FlowProgram.FlowProgramStepPointerType;
import io.resys.limaone.program.FlowProgram.FlowProgramStepRefType;
import io.resys.limaone.program.ImmutableFlowProgramStep;
import io.resys.limaone.program.ImmutableFlowProgramStepBody;
import io.resys.limaone.program.ImmutableFlowProgramStepConditionalThenPointer;
import io.resys.limaone.program.ImmutableFlowProgramStepThenPointer;
import io.resys.limaone.program.ImmutableFlowProgramStepWhenThenPointer;
import io.resys.limaone.program.Program;
import io.resys.limaone.spi.ast.flow.YamlMapper;
import io.resys.limaone.spi.program.FlowProgramExecutor;
import io.resys.limaone.spi.program.FlowProgramImpl;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class Compiler_Flow implements CompilableUnit {
  private final AST_Parser parser;
  @SuppressWarnings("unused")
  private final ModelWorld world;
  private final Model<Flow> flow;

  public static final String OBJECT_INPUT_FLAG = "OBJECT_INPUT";

  private final Map<String, FlowProgramStep> steps = new HashMap<>();
  private final Map<String, YamlTask> tasksById = new HashMap<>();
  private NewArtifact resolution;
  
  @Override
  public OpenProgram compile(NewArtifact resolution) {
    final Flow_AST ast = parser.parseFlow().id(flow.getId()).syntax(flow.getBody().getFlowValue()).parse();
    
    this.resolution = resolution;
    final var firstTask = visitTasksById(ast);
    final var firstStep = firstTask == null ? FlowProgramExecutor.END_STEP: visitTask(firstTask);
    resolution.ast(ast).name(ast.getName()).id(flow.getId()).build();
    
    return new OpenProgram() {
      @Override
      public String getId() {
        return ast.getId();
      }
      @Override
      public Simple_AST getAst() {
        return ast;
      }
      @Override
      public Program close(Artifact artifact) {
        return new FlowProgramImpl(
            ast, firstStep.getId(), steps, 
            artifact.getProgramStatus(), 
            artifact.getErrors(), artifact.getAssociations());
      }
    };
  }


  private YamlTask visitTasksById(Flow_AST ast) {
    YamlTask firstTask = null;
    final var data = ast.getParseTree();
    for(final var task : data.getTasks().values()) {
      tasksById.put(YamlMapper.getStringValue(task.getId()), task);
      if(task.getOrder() == 0) {
        firstTask = task;
      }
    }
    return firstTask;
  }

  private FlowProgramStep visitTask(YamlTask task) {
    String taskId = YamlMapper.getStringValue(task.getId());
    if(steps.containsKey(taskId)) {
      return steps.get(taskId);
    }
    steps.put(taskId, null);
    
    final var body = visitStepBody(task);
    final var pointer = visitStepPointer(task);
    
    final var step = ImmutableFlowProgramStep.builder()
        .id(taskId)
        .body(body)
        .pointer(pointer)
        .build();
    
    steps.put(step.getId(), step);
    return step;
  }

  public FlowProgramStepBody visitStepBody(YamlTask task) {
    if(task.getDecisionTable() == null && task.getService() == null && task.getReturns() == null) {
      return null;
    }

    final var collection = task.getReturns() != null ? YamlMapper.getBooleanValue(task.getReturns().getCollection()) : YamlMapper.getBooleanValue(task.getRef().getCollection());
    final var ref =  task.getReturns() != null ? "" : YamlMapper.getStringValue(task.getRef().getRef());
    
    final var inputs = new HashMap<String, String>();
    
    // use reference input
    if(task.getRef() != null) {
      if (task.getRef().getObjectInput() != null) {
        inputs.put(OBJECT_INPUT_FLAG, task.getRef().getObjectInput());
      } else {
        for (Map.Entry<String, Yaml> entry : task.getRef().getInputs().entrySet()) {
          inputs.put(entry.getKey(), YamlMapper.getStringValue(entry.getValue()));
        }
      }
      // use returns input mapping
    } else {
      if (task.getReturns().getObjectInput() != null) {
        inputs.put(OBJECT_INPUT_FLAG, task.getRef().getObjectInput());
      } else {
        for (Map.Entry<String, Yaml> entry : task.getReturns().getInputs().entrySet()) {
          inputs.put(entry.getKey(), YamlMapper.getStringValue(entry.getValue()));
        }
      }
    }
    
    final FlowProgramStepRefType refType;
    if(task.getDecisionTable() != null) {
      refType = FlowProgramStepRefType.DT;
    } else if(task.getService() != null) {
      refType = FlowProgramStepRefType.SERVICE;
    } else {
      refType = FlowProgramStepRefType.RETURNS;
    }
    return ImmutableFlowProgramStepBody.builder()
        .ref(ref)
        .refType(refType)
        .collection(collection)
        .inputMapping(inputs)
        .build();
  }
  

  private FlowProgramStepPointer visitStepPointer(YamlTask task) {
    if(!task.getSwitch().isEmpty()) {
      final var pointer = ImmutableFlowProgramStepWhenThenPointer.builder().type(FlowProgramStepPointerType.SWITCH);
      final var decisions = new ArrayList<YamlSwitch>(task.getSwitch().values());
      Collections.sort(decisions, (o1, o2) -> Integer.compare(o1.getOrder(), o2.getOrder()));
      decisions.forEach(d -> {
        
        final var condition = visitSwitchNode(d);
        if(!condition.getStepId().equals(FlowProgramExecutor.END_STEP.getId())) {
          visitTask(tasksById.get(condition.getStepId()));
        }
        pointer.addConditions(condition);
        
        
      });
      return pointer.build();
    }
    
    final var thenId = YamlMapper.getStringValue(task.getThen());
    if(thenId != null && !thenId.equals(FlowProgramExecutor.END_STEP.getId())) {
      visitTask(tasksById.get(thenId));
      return ImmutableFlowProgramStepThenPointer.builder()
          .type(FlowProgramStepPointerType.THEN)
          .stepId(thenId)
          .build();
    }
    
    return FlowProgramExecutor.END_STEP_POINTER;
  }
  
  private FlowProgramStepConditionalThenPointer visitSwitchNode(YamlSwitch decision) {
    final var condition = ImmutableFlowProgramStepConditionalThenPointer.builder();
    final var decisionId = decision.getKeyword();
    final var when = YamlMapper.getStringValue(decision.getWhen());
    final var thenValue = YamlMapper.getStringValue(decision.getThen());    
    try {
      final var isTrue = when == null || when.isEmpty();
      final var expression = isTrue ? 
          Compiler_Expression.build("true", ValueType.FLOW_CONTEXT) :
          Compiler_Expression.build(when, ValueType.FLOW_CONTEXT);
      
      condition.expression(expression).stepId(thenValue);
    } catch(Exception e) {
      final var message = "Failed to evaluate expression: \"" + when + "\" in flow decision: " + decisionId + "!" + System.lineSeparator() + e.getMessage();
      throw new CompilerException(message, e);
    } 
    return condition.build();
  }
}
