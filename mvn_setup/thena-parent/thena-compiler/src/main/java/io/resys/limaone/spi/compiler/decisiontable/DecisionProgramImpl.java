package io.resys.limaone.spi.compiler.decisiontable;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Streams;

import io.resys.limaone.ast.DecisionTable_AST;
import io.resys.limaone.model.DecisionTable.HitPolicy;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.model.Parameter;
import io.resys.limaone.program.DecisionProgram;
import io.resys.limaone.program.Runtime;



public class DecisionProgramImpl implements DecisionProgram {


  private static final long serialVersionUID = 6616773813732711822L;
  private final DecisionTable_AST ast;
  private final ProgramStatus status; 
  private final List<DecisionRow> rows;
  private final List<Parameter> headers;
  private final List<ProgramMessage> errors;
  private final List<ProgramAssociation> associations;
  
  public DecisionProgramImpl(
      DecisionTable_AST ast, 
      ProgramStatus status,
      List<DecisionRow> rows,
      List<ProgramMessage> errors,
      List<ProgramAssociation> associations) {
    super();
    this.ast = ast;
    this.status = status;
    this.rows = Collections.unmodifiableList(rows);
    this.errors = Collections.unmodifiableList(errors);
    this.associations = Collections.unmodifiableList(associations);
    this.headers = Streams
        .concat(ast.getHeaders().getAcceptDefs().stream(), ast.getHeaders().getReturnDefs().stream())
        .toList();
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
  public List<ProgramMessage> getErrors() {
    return errors;
  }
  @Override
  public List<Parameter> getHeaders() {
    return headers;
  }
  @Override
  public List<ProgramAssociation> getAssociations() {
    return associations;
  }
  @Override
  public List<DecisionRow> getRows() {
    return rows;
  }
  @Override
  public HitPolicy getHitPolicy() {
    return ast.getHitPolicy();
  }

  @Override
  public DecisionExecutor run(ProgramInput input, Runtime runtime) {
    
    return null;
  }
}
