package io.resys.limaone.spi.ast;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;

import io.resys.limaone.ast.AST_Parser.ArticleWorkflowParser;
import io.resys.limaone.ast.AST_Parser.Dependency_AST;
import io.resys.limaone.ast.ArticleWorkflow_AST;
import io.resys.limaone.ast.ImmutableArticleWorkflow_AST;
import io.resys.limaone.ast.ImmutableDependency_AST;
import io.resys.limaone.ast.ImmutableHeaders_AST;
import io.resys.limaone.model.ArticleWorkflow;
import io.resys.limaone.model.ImmutableModelError;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.model.ModelError;
import io.resys.limaone.spi.LocalCache;
import io.resys.limaone.spi.LocalCache.ArticleWorkflow_AST_CacheKey;
import io.resys.limaone.spi.ast.AST_ParserImpl.AST_ParserProps;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class ArticleWorkflowParserImpl implements ArticleWorkflowParser {
  private final String DT_AUTH_NAME = "ProcessAuthorizationDT";
  private final AST_ParserProps props;
  private Consumer<Dependency_AST> onDependency;
  private Model<ArticleWorkflow> model;
  
  
  @Override
  public ArticleWorkflowParser onDependency(Consumer<Dependency_AST> onDependency) {
    this.onDependency = onDependency;
    return this;
  }

  @Override
  public ArticleWorkflowParser model(Model<ArticleWorkflow> workflow) {
    this.model = Objects.requireNonNull(workflow, () -> "workflow must be defined");
    return this;
  }

  @Override
  public ArticleWorkflow_AST parse() {
    Objects.requireNonNull(model, () -> "workflow must be defined");

    final var hash = model.getBodyHash();
    final var cacheKey = new ArticleWorkflow_AST_CacheKey(hash);
    
    final Function<ArticleWorkflow_AST_CacheKey, ArticleWorkflow_AST> mappingFunction = (k) -> createAst(model);
    final var ast = LocalCache.computeIfAbsent(cacheKey, mappingFunction);
    
    if(onDependency != null) {
      ast.getDependencies().forEach(onDependency);
    }
    return ast;
  }

  
  private ArticleWorkflow_AST createAst(Model<ArticleWorkflow> model) {
    final var workflow = model.getBody();
    final List<ModelError> errors = new ArrayList<>();
    final var dependencies = new ArrayList<Dependency_AST>();
    
    // DT authorization 
    dependencies.add(ImmutableDependency_AST.builder().dependencyId(DT_AUTH_NAME).type(BodyType.DECISION_TABLE).build());
    
    // Flow dependency
    if(StringUtils.isBlank(workflow.getFlowName())) {
      errors.add(ImmutableModelError.builder().msg("Flow name must be defined!").build());
    } else {
      dependencies.add(ImmutableDependency_AST.builder().dependencyId(workflow.getFlowName()).type(BodyType.FLOW).build());
    }

    // Form dependency
    if(StringUtils.isBlank(workflow.getFormName()) || StringUtils.isBlank(workflow.getFormTag())) {
      errors.add(ImmutableModelError.builder().msg("Form name and tag must be defined!").build());
    } else {
      final var dependencyId = DialobFormParserImpl.getFormDep(workflow.getFormName(), workflow.getFormTag());
      dependencies.add(ImmutableDependency_AST.builder().dependencyId(dependencyId).type(BodyType.DIALOB_FORM).build());
    }
    
    return ImmutableArticleWorkflow_AST.builder()
        .addAllErrors(errors)
        .bodyType(BodyType.ARTICLE_WORKFLOW)
        .headers(ImmutableHeaders_AST.builder().build())
        .name(workflow.getValue())
        .hash(model.getBodyHash())
        .dependencies(dependencies)
        .build();
  }
}
