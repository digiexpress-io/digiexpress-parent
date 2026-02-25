package io.resys.limaone.ast;

import java.util.List;
import java.util.Optional;

import io.resys.limaone.ast.attribute.AST;
import io.resys.limaone.ast.decisiontable.DecisionTable_AST;
import io.resys.limaone.ast.flow.Flow_AST;
import io.resys.limaone.ast.flowtask.FlowTask_AST;
import io.resys.limaone.model.DecisionTable.DecisionTableNode;
import io.resys.limaone.model.Model;

public interface AST_Parser {

  ArticleParser parseArticles();
  FlowParser parseFlow();
  FlowTaskParser parseFlowTask();
  DecsionTableParser parseDecisionTable();


  
  interface ArticleParser {
    ArticleParser deps(DependencyResolution deps);
  }
  
  interface FlowParser {
    FlowParser deps(DependencyResolution deps);
    FlowParser syntax(String syntax);
    Flow_AST parse();
  }
  
  interface DecsionTableParser {
    DecsionTableParser nodes(List<DecisionTableNode> nodes);
    DecisionTable_AST parse();
  }
  
  interface FlowTaskParser {
    FlowTaskParser deps(DependencyResolution deps);
    FlowTaskParser syntax(String syntax);
    FlowTask_AST parse();
  }
    
  interface DependencyResolution {
    Optional<AST> findOne(RequireDependency require);
  }

  interface RequireDependency {
    String getDependencyId();
    String getDependencyName();
    Model.BodyType getType();
  }
}