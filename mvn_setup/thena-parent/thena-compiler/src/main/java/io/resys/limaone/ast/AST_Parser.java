package io.resys.limaone.ast;

import java.util.List;

import io.resys.limaone.model.DecisionTable.DecisionStatement;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.ModelWorld;

public interface AST_Parser {

  ArticleParser parseArticles();
  FlowParser parseFlow();
  FlowTaskParser parseFlowTask();
  DecsionTableParser parseDecisionTable();

  interface ArticleParser {
    ArticleParser world(ModelWorld world);
    Article_AST parse();
  }
  
  interface FlowParser {
    FlowParser id(String id);
    FlowParser syntax(String syntax);
    Flow_AST parse();
  }
  
  interface DecsionTableParser {
    DecsionTableParser id(String id);
    DecsionTableParser nodes(List<DecisionStatement> nodes);
    DecsionTableParser syntax(String syntax);
    DecisionTable_AST parse();
  }
  
  interface FlowTaskParser {
    FlowTaskParser id(String id);
    FlowTaskParser syntax(String syntax);
    FlowTask_AST parse();
  }

  interface RequireDependency {
    String getDependencyId();
    String getDependencyName();
    Model.BodyType getType();
  }
}