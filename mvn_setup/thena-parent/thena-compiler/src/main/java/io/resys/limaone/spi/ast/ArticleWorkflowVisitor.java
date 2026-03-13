package io.resys.limaone.spi.ast;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import io.resys.limaone.ast.AST_Parser.Dependency_AST;
import io.resys.limaone.ast.ArticleWorkflow_AST;
import io.resys.limaone.ast.ArticleWorkflow_AST.AnyStatement;
import io.resys.limaone.ast.ImmutableArticleWorkflow_AST;
import io.resys.limaone.ast.ImmutableDependency_AST;
import io.resys.limaone.ast.ImmutableHeaders_AST;
import io.resys.limaone.model.ArticleWorkflow;
import io.resys.limaone.model.ImmutableModelError;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.model.ModelError;
import io.resys.limaone.spi.ast.AST_ParserImpl.AST_ParserProps;
import io.resys.limaone.spi.ast.WkStatementFactory.ImmutableEndStatement;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class ArticleWorkflowVisitor {
  private final String DT_AUTH_NAME = "ProcessAuthorizationDT";
  @SuppressWarnings("unused")
  private final AST_ParserProps props;
  private final Model<ArticleWorkflow> model;
  
  private final List<ModelError> errors = new ArrayList<>();
  private final List<Dependency_AST> dependencies = new ArrayList<>();
  

  public ArticleWorkflow_AST accept() {
    Objects.requireNonNull(model, () -> "workflow must be defined");

    final var workflow = model.getBody();
    final var errors = new ArrayList<ModelError>();
    final var dependencies = new ArrayList<Dependency_AST>();
    final var statement = visitDisabled(workflow);
    
    return ImmutableArticleWorkflow_AST.builder()
        .addAllErrors(errors)
        .bodyType(BodyType.ARTICLE_WORKFLOW)
        .headers(ImmutableHeaders_AST.builder().build())
        .name(workflow.getValue())
        .hash(model.getBodyHash())
        .dependencies(dependencies)
        .statement(statement)
        .build();
  }
  
  private AnyStatement visitDisabled(ArticleWorkflow workflow) {
    if(Boolean.TRUE.equals(workflow.getDisabled())) {
      return new WkStatementFactory.ImmutableDisabledStatement(ImmutableEndStatement.getInstance());
    }
    return visitDev(workflow);
  }

  private AnyStatement visitDev(ArticleWorkflow workflow) {
    if(Boolean.TRUE.equals(workflow.getDevMode())) {
      return new WkStatementFactory.ImmutableDevelopmentStatement(visitLimitedTime(workflow));
    }
    
    return visitLimitedTime(workflow);
  }
  
  private AnyStatement visitLimitedTime(ArticleWorkflow workflow) {
    if(workflow.getStartDate()  == null && workflow.getEndDate() == null) {
      return visitAnon(workflow);
    }
    return new WkStatementFactory.ImmutableLimitedTimeStatement(
        Optional.ofNullable(workflow.getStartDate()).orElse(OffsetDateTime.now()),
        Optional.ofNullable(workflow.getEndDate()).orElse(OffsetDateTime.MAX),
        visitAnon(workflow));
  }
  
  
  private AnyStatement visitAnon(ArticleWorkflow workflow) {
    return new WkStatementFactory.ImmutableAnonStatement(visitInputs(workflow), Boolean.TRUE.equals(workflow.getAnon()));
  }
  
  private AnyStatement visitInputs(ArticleWorkflow workflow) {
    return new WkStatementFactory.ImmutableInputsStatement(visitUserRoles(workflow));
  }

  private AnyStatement visitUserRoles(ArticleWorkflow workflow) {
    dependencies.add(ImmutableDependency_AST.builder().dependencyId(DT_AUTH_NAME).type(BodyType.DECISION_TABLE).build());
    return new WkStatementFactory.ImmutableUserRolesStatement(DT_AUTH_NAME, workflow.getValue(), visitForm(workflow));
  }
  
  private AnyStatement visitForm(ArticleWorkflow workflow) {
    if(StringUtils.isBlank(workflow.getFormName()) || StringUtils.isBlank(workflow.getFormTag())) {
      errors.add(ImmutableModelError.builder().msg("Form name and tag must be defined!").build());
      return WkStatementFactory.ImmutableEndStatement.getInstance();
    }
    
    final var dependencyId = DialobFormParserImpl.getFormDep(workflow.getFormName(), workflow.getFormTag());
    dependencies.add(ImmutableDependency_AST.builder().dependencyId(dependencyId).type(BodyType.DIALOB_FORM).build());
    
    return new WkStatementFactory.ImmutableCreateFormStatement(
      dependencyId, 
      workflow.getFormName(), 
      workflow.getFormTag(),
      new WkStatementFactory.ImmutableAwaitFormStatement(visitFlow(workflow))
    );
  }

  private AnyStatement visitFlow(ArticleWorkflow workflow) {
    if(StringUtils.isBlank(workflow.getFlowName())) {
      errors.add(ImmutableModelError.builder().msg("Flow name must be defined!").build());
      return WkStatementFactory.ImmutableEndStatement.getInstance();
    }
    dependencies.add(ImmutableDependency_AST.builder().dependencyId(workflow.getFlowName()).type(BodyType.FLOW).build());
    
    return new WkStatementFactory.ImmutableCreateTaskStatement(
        workflow.getFlowName(),
        WkStatementFactory.ImmutableEndStatement.getInstance());
  }
}
