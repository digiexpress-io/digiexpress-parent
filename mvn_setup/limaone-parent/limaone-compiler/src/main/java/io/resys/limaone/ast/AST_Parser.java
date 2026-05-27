package io.resys.limaone.ast;

/*-
 * #%L
 * limaone-compiler
 * %%
 * Copyright (C) 2015 - 2026 Copyright 2022 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.immutables.value.Value;

import io.resys.limaone.model.ArticleWorkflow;
import io.resys.limaone.model.DecisionTable.DecisionStatement;
import io.resys.limaone.model.DialobForm;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.ModelWorld;
import io.resys.limaone.model.Parameter;

public interface AST_Parser {

  ArticleParser parseArticles();
  ArticleWorkflowParser parseArticleWorkflow();
  
  FlowParser parseFlow();
  FlowTaskParser parseFlowTask();
  DecsionTableParser parseDecisionTable();
  DialobFormParser parseDialobForm();
  
  CsvParser parseCsv();
  
  interface CsvParser {
    CsvParser csv(String syntax);
    CsvParser castTo(List<Parameter> parameter);
    CSV_AST parse();
  }
  
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
     
    // just extract nodes from what the DT would be built
    List<DecisionStatement> parseNodes();
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
