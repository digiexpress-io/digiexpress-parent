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

import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.ObjectMapper;

import groovy.lang.GroovyClassLoader;
import io.resys.limaone.ast.AST_Parser;
import io.resys.limaone.spi.groovy.Add_Executor;
import io.resys.limaone.yaml.YamlMapper;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class AST_ParserImpl implements AST_Parser {

  private final AST_ParserProps props;

  @Override
  public ArticleParser parseArticles() {
    return new ArticleParserImpl(props);
  }
  @Override
  public FlowParser parseFlow() {
    return new FlowParserImpl(props);
  }
  @Override
  public FlowTaskParser parseFlowTask() {
    return new FlowTaskParserImpl(props);
  }
  @Override
  public DecsionTableParser parseDecisionTable() {
    return new DecsionTableParserImpl(props);
  }
  @Override
  public DialobFormParser parseDialobForm() {
    return new DialobFormParserImpl(props);
  }
  @Override
  public ArticleWorkflowParser parseArticleWorkflow() {
    return new ArticleWorkflowParserImpl(props);
  }
  @Override
  public CsvParser parseCsv() {
    return new CsvParserImpl();
  }
  @Value.Immutable
  public interface AST_ParserProps {
    boolean isDev();
    GroovyClassLoader getGroovy();
    ObjectMapper getYaml();
  }
  
  
  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private boolean dev;
    
    public Builder dev(boolean dev) {
      this.dev = dev;
      return this;
    }

    public ImmutableAST_ParserProps props() {
      final CompilerConfiguration groovyConfig = new CompilerConfiguration();
      groovyConfig.setTargetBytecode(CompilerConfiguration.JDK21);
      groovyConfig.addCompilationCustomizers(new Add_Executor());
      groovyConfig.addCompilationCustomizers(new ASTTransformationCustomizer(groovy.transform.CompileStatic.class));
      final var groovy = new GroovyClassLoader(Thread.currentThread().getContextClassLoader(), groovyConfig);
      return ImmutableAST_ParserProps.builder()
          .groovy(groovy)
          .isDev(dev)
          .yaml(new YamlMapper().unwrap())
          .build();
    }
    
    public AST_ParserImpl build() {
      return new AST_ParserImpl(props());
    }
  }
}
