package io.resys.limaone.spi.ast;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import io.resys.limaone.ast.AST_Parser.ArticleWorkflowParser;
import io.resys.limaone.ast.AST_Parser.Dependency_AST;
import io.resys.limaone.ast.ArticleWorkflow_AST;
import io.resys.limaone.model.ArticleWorkflow;
import io.resys.limaone.model.Model;
import io.resys.limaone.spi.LocalCache;
import io.resys.limaone.spi.LocalCache.ArticleWorkflow_AST_CacheKey;
import io.resys.limaone.spi.ast.AST_ParserImpl.AST_ParserProps;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class ArticleWorkflowParserImpl implements ArticleWorkflowParser {
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
    return new ArticleWorkflowVisitor(props, model).accept();
  }  
}
