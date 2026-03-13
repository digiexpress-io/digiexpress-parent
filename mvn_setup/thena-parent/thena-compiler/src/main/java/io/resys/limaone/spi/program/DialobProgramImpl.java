package io.resys.limaone.spi.program;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.resys.limaone.ast.DialobForm_AST;
import io.resys.limaone.model.DialobForm;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.model.ModelError;
import io.resys.limaone.model.Parameter;
import io.resys.limaone.program.DialobProgram;
import io.resys.limaone.program.ProgramInput;
import io.resys.limaone.program.Runtime;
import io.resys.limaone.program.WorkflowProgram;
import io.resys.limaone.spi.dialob.FormDb;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class DialobProgramImpl implements DialobProgram {
  private final FormDb formDb;
  private final Model<DialobForm> target;
  private final DialobForm_AST ast;
  private final ProgramStatus status;
  private final List<ModelError> errors;
  private final List<ProgramAssociation> associations;
  
  @Override
  public String getId() {
    return target.getId();
  }

  @Override
  public String getName() {
    return ast.getName();
  }

  @Override
  public BodyType getType() {
    return BodyType.DIALOB_FORM;
  }
  
  @Override
  public ProgramStatus getStatus() {
    return status;
  }

  @Override
  public List<Parameter> getHeaders() {
    return Collections.emptyList();
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
  public DialobExecutor run(ProgramInput input, Runtime runtime) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public DialobExecutor run(Map<String, Serializable> input) {
    // TODO Auto-generated method stub
    return null;
  }

}
