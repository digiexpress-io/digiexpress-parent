package io.resys.limaone.spi.compiler.decisiontable;

import java.util.List;

import io.resys.limaone.model.DecisionTable.HitPolicy;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.model.Parameter;
import io.resys.limaone.program.DecisionProgram;
import io.resys.limaone.program.Runtime;

public class DecisionProgramImpl implements DecisionProgram {

  @Override
  public String getId() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public BodyType getType() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ProgramStatus getStatus() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<ProgramMessage> getWarnings() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<ProgramMessage> getErrors() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<Parameter> getHeaders() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<ProgramAssociation> getAssociations() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<DecisionRow> getRows() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public HitPolicy getHitPolicy() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public DecisionExecutor run(ProgramInput input, Runtime runtime) {
    // TODO Auto-generated method stub
    return null;
  }


}
