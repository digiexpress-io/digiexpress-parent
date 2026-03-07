package io.resys.limaone.persistence;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;

import org.immutables.value.Value;

import io.resys.limaone.ast.AST_Parser;
import io.resys.limaone.authoring.Authoring;
import io.resys.limaone.authoring.NewArticle;
import io.resys.limaone.authoring.NewArticleLink;
import io.resys.limaone.authoring.NewArticlePage;
import io.resys.limaone.authoring.NewArticleTemplate;
import io.resys.limaone.authoring.NewArticleWorkflow;
import io.resys.limaone.authoring.NewDecisionTable;
import io.resys.limaone.authoring.NewFlow;
import io.resys.limaone.authoring.NewFlowTask;
import io.resys.limaone.authoring.NewLocale;
import io.resys.limaone.spi.ast.AST_ParserImpl;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class AuthoringImpl implements Authoring {

  private final AuthoringConfig config;
  
  @Override
  public ModifyModel modifyModel() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public DeleteModel deleteModel() {
    // TODO Auto-generated method stub
    return null;
  }
  
  @Override
  public NewModel newModel() {
    return new NewModel() {
      @Override public NewLocale newLocale() { return new NewLocaleImpl(config); }
      @Override public NewFlowTask newFlowTask() { return new NewFlowTaskImpl(config); }
      @Override public NewFlow newFlow() { return new NewFlowImpl(config); }
      @Override public NewDecisionTable newDecisionTable() { return new NewDecisionTableImpl(config); }
      @Override public NewArticleWorkflow newArticleWorkflow() { return new NewArticleWorkflowImpl(config); }
      @Override public NewArticleTemplate newArticleTemplate() { return new NewArticleTemplateImpl(config); }
      @Override public NewArticlePage newArticlePage() { return new NewArticlePageImpl(config); }
      @Override public NewArticleLink newArticleLink() { return new NewArticleLinkImpl(config); }
      @Override public NewArticle newArticle() { return new NewArticleImpl(config); }
    };
  }

  
  public static AuthoringBuilder builder() {
    return new AuthoringBuilder();
  }
  public static class AuthoringBuilder {
    private WorldPersistence persistence;
    private ScheduledExecutorService workerPool;
    private AST_Parser astParser;
    
    public AuthoringBuilder persistence(WorldPersistence persistence) {
      this.persistence = persistence;
      return this;
    }    
    public AuthoringBuilder workerPool(ScheduledExecutorService workerPool) {
      this.workerPool = workerPool;
      return this;
    }
    public AuthoringBuilder astParser(AST_Parser astParser) {
      this.astParser = astParser;
      return this;
    }
    
    public AuthoringImpl build() {
      Objects.requireNonNull(persistence, () -> "persistence must be defined");
      
      final var workerPool = this.workerPool == null ? Infrastructure.getDefaultWorkerPool() : this.workerPool;
      final var astParser = this.astParser == null ? AST_ParserImpl.builder().dev(true).build() : this.astParser;

      return new AuthoringImpl(ImmutableAuthoringConfig.builder()
          .workerTimeout(Duration.ofMinutes(15))
          .workerPool(workerPool)
          .astParser(astParser)
          .persistence(persistence)
          .build());
    }
  }

  @Value.Immutable
  public interface AuthoringConfig {
    WorldPersistence getPersistence();
    ScheduledExecutorService getWorkerPool();
    Duration getWorkerTimeout();
    AST_Parser getAstParser();
  }
}
