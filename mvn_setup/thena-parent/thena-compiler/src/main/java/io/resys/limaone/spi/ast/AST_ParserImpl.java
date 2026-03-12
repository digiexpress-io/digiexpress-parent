package io.resys.limaone.spi.ast;

import java.util.Optional;

import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.ObjectMapper;

import groovy.lang.GroovyClassLoader;
import io.resys.limaone.ast.AST_Parser;
import io.resys.limaone.spi.compiler.groovy.GroovyCompilationCustomizer;
import io.resys.limaone.spi.dialob.FormDb;
import io.resys.limaone.spi.dialob.FormDb_Empty;
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
    // TODO Auto-generated method stub
    return null;
  }
  @Override
  public ArticleWorkflowParser parseArticleWorkflow() {
    return new ArticleWorkflowParserImpl(props);
  }
  @Value.Immutable
  public interface AST_ParserProps {
    boolean isDev();
    GroovyClassLoader getGroovy();
    ObjectMapper getYaml();
    FormDb getFormDb();
  }
  
  
  public static Builder builder() {
    return new Builder();
  }
  
  
  public static class Builder {
    private boolean dev;
    private FormDb formDb;

    public Builder formDb(FormDb formDb) {
      this.formDb = formDb;
      return this;
    }
    public Builder dev(boolean dev) {
      this.dev = dev;
      return this;
    }

    public ImmutableAST_ParserProps props() {
      final CompilerConfiguration groovyConfig = new CompilerConfiguration();
      groovyConfig.setTargetBytecode(CompilerConfiguration.JDK21);
      groovyConfig.addCompilationCustomizers(new GroovyCompilationCustomizer());
      groovyConfig.addCompilationCustomizers(new ASTTransformationCustomizer(groovy.transform.CompileStatic.class));
      final var groovy = new GroovyClassLoader(Thread.currentThread().getContextClassLoader(), groovyConfig);
      return ImmutableAST_ParserProps.builder()
          .groovy(groovy)
          .isDev(dev)
          .yaml(new YamlMapper().unwrap())
          .formDb(Optional.ofNullable(formDb).orElseGet(() -> new FormDb_Empty()))
          .build();
    }
    
    public AST_ParserImpl build() {
      return new AST_ParserImpl(props());
    }
  }
}
