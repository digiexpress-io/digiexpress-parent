package io.resys.limaone.ast;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.immutables.value.Value;

import io.resys.limaone.model.ArticleWorkflow;
import io.resys.limaone.model.DecisionTable.DecisionStatement;
import io.resys.limaone.model.DialobForm;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.ModelWorld;

public interface AST_Parser {

  ArticleParser parseArticles();
  ArticleWorkflowParser parseArticleWorkflow();
  
  FlowParser parseFlow();
  FlowTaskParser parseFlowTask();
  DecsionTableParser parseDecisionTable();
  DialobFormParser parseDialobForm();
  
  interface ArticleWorkflowParser {
    ArticleWorkflowParser onDependency(Consumer<Dependency_AST> dependency);
    ArticleWorkflowParser model(Model<ArticleWorkflow> workflow);
    ArticleWorkflow_AST parse();
  }
  
  interface DialobFormParser {
    DialobFormParser model(Model<DialobForm> workflow);
    DialobForm_AST parse();
  }
  
  interface ArticleParser {
    ArticleParser world(ModelWorld world);
    Article_AST parse();
  }
  
  interface FlowParser {
    FlowParser syntax(String syntax);
    FlowParser onDependency(Consumer<Dependency_AST> dependency);
    Flow_AST parse();
  }
  
  interface DecsionTableParser {
    DecsionTableParser nodes(List<DecisionStatement> nodes);
    DecsionTableParser syntax(String syntax);
    DecisionTable_AST parse();
  }
  
  interface FlowTaskParser {
    FlowTaskParser syntax(String syntax);
    FlowTask_AST parse();
  }

  @Value.Immutable
  interface Dependency_AST {
    String getDependencyId();
    Model.BodyType getType();
    Optional<Simple_AST> getArtifactAst();
  }
}