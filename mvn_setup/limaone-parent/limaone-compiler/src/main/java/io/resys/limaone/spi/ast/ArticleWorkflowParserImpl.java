package io.resys.limaone.spi.ast;

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
