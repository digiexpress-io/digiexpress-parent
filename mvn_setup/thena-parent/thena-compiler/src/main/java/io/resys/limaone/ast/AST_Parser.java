package io.resys.limaone.ast;

import java.util.List;
import java.util.Optional;

import io.resys.limaone.model.DecisionTable.DecisionTableNode;
import io.resys.limaone.model.Model;

public interface AST_Parser {

  ArticleParser parseArticles();
  FlowParser parseFlow();
  FlowTaskParser parseFlowTask();
  DecsionTableParser parseDecisionTable();


  
  interface ArticleParser {
    ArticleParser deps(DependencyResolution deps);
    Article_AST parse();
  }
  
  interface FlowParser {
    FlowParser id(String id);
    FlowParser deps(DependencyResolution deps);
    FlowParser syntax(String syntax);
    Flow_AST parse();
  }
  
  interface DecsionTableParser {
    DecsionTableParser id(String id);
    DecsionTableParser nodes(List<DecisionTableNode> nodes);
    DecisionTable_AST parse();
  }
  
  interface FlowTaskParser {
    FlowTaskParser id(String id);
    FlowTaskParser syntax(String syntax);
    FlowTaskParser deps(DependencyResolution deps);
    FlowTask_AST parse();
  }
    
  interface DependencyResolution {
    Optional<Simple_AST> findOne(RequireDependency require);
  }

  interface RequireDependency {
    String getDependencyId();
    String getDependencyName();
    Model.BodyType getType();
  }
}