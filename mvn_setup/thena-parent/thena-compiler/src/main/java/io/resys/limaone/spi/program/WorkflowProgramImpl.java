package io.resys.limaone.spi.program;

import java.util.Collections;
import java.util.List;

import io.resys.limaone.ast.ArticleWorkflow_AST;
import io.resys.limaone.model.ArticleWorkflow;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.model.ModelError;
import io.resys.limaone.model.Parameter;
import io.resys.limaone.program.Runtime;
import io.resys.limaone.program.WorkflowProgram;
import io.resys.limaone.spi.dialob.FormDb;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class WorkflowProgramImpl implements WorkflowProgram {
  private final FormDb formDb;
  private final Model<ArticleWorkflow> target;
  private final ArticleWorkflow_AST ast;
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
    return BodyType.ARTICLE_WORKFLOW;
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
  public WorkflowExecutor run(Runtime runtime, WorkflowIdentityProps identity, WorkflowInputProps programInput) {
    return new WorkflowExecutor() {
      @Override
      public WorkflowInstanceResult andGetForm() {
        return new WorkflowInstanceExecutor(runtime, identity, programInput).walk(ast);
      }
      @Override
      public WorkflowResult andGetFlow() {
        // TODO Auto-generated method stub
        return null;
      }
    };
  }
}