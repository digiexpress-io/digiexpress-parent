package io.resys.limaone.spi.program;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.resys.limaone.ast.Flow_AST;
import io.resys.limaone.ast.Flow_AST.FlowInputNode;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.model.ModelError;
import io.resys.limaone.model.Parameter;
import io.resys.limaone.model.Parameter.Direction;
import io.resys.limaone.model.Parameter.ValueType;
import io.resys.limaone.program.FlowProgram;
import io.resys.limaone.program.ProgramInput;
import io.resys.limaone.program.Runtime;
import io.resys.limaone.spi.ast.flow.AstFlowNodesFactory;
import io.resys.limaone.spi.parameter.Parameter_Factory;

public class FlowProgramImpl implements FlowProgram {

  private static final long serialVersionUID = -4209510801206880302L;
  private final Flow_AST ast;
  
  private final String startStepId;
  private final Map<String, FlowProgramStep> steps;
  
  private final ProgramStatus status; 
  private final List<Parameter> headers;
  private final List<ModelError> errors;
  private final List<ProgramAssociation> associations;
  
  
  public FlowProgramImpl(
      Flow_AST ast, 
      String startStepId,
      Map<String, FlowProgramStep> steps,
      ProgramStatus status,
      List<ModelError> errors,
      List<ProgramAssociation> associations) {
    super();
    this.ast = ast;
    this.status = status;
    this.startStepId = startStepId;
    this.steps = Collections.unmodifiableMap(steps);
    this.errors = Collections.unmodifiableList(errors);
    this.associations = Collections.unmodifiableList(associations);
    this.headers = Collections.unmodifiableList(getHeaders(ast));
  }
  
  private List<Parameter> getHeaders(Flow_AST ast) {
    final Map<String, FlowInputNode> inputs = ast.getRoot().getInputs();

    int index = 0;
    final List<Parameter> result = new ArrayList<>();
    for (Map.Entry<String, FlowInputNode> entry : inputs.entrySet()) {
      if (entry.getValue().getType() == null) {
        continue;
      }
      try {
        ValueType valueType = ValueType.valueOf(entry.getValue().getType().getValue());
        boolean required = AstFlowNodesFactory.getBooleanValue(entry.getValue().getRequired());
        result.add(Parameter_Factory.newParam()
            .id(entry.getValue().getStart() + "")
            .order(index++)
            .name(entry.getKey()).valueType(valueType).direction(Direction.IN).required(required)
            .values(AstFlowNodesFactory.getStringValue(entry.getValue().getDebugValue()))
            .build());
        
      } catch (Exception e) {
        final String msg = String.format("Failed to convert data type from: %s, error: %s", entry.getValue().getType().getValue(), e.getMessage());
        throw new ProgramException(msg, e);
      }
    }
    return result;
  }
  
  @Override
  public String getId() {
    return ast.getId();
  }

  @Override
  public BodyType getType() {
    return ast.getBodyType();
  }

  @Override
  public ProgramStatus getStatus() {
    return status;
  }

  @Override
  public List<Parameter> getHeaders() {
    return headers;
  }
  @Override
  public List<ModelError> getErrors() {
    return errors;
  }

  @Override
  public List<ProgramAssociation> getAssociations() {
    return associations;
  }

  @Override
  public String getStartStepId() {
    return startStepId;
  }

  @Override
  public Map<String, FlowProgramStep> getSteps() {
    return steps;
  }
  @Override
  public String getName() {
    return ast.getName();
  }
  @Override
  public FlowExecutor run(ProgramInput input, Runtime runtime) {
    // TODO Auto-generated method stub
    return null;
  }
  @Override
  public FlowExecutor run(Map<String, Serializable> input) {
    // TODO Auto-generated method stub
    return null;
  }
}
