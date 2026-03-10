package io.resys.limaone.persistence;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

import org.immutables.value.Value;

import io.resys.limaone.ast.AST_Parser;
import io.resys.limaone.authoring.Authoring;
import io.resys.limaone.authoring.DeleteAny;
import io.resys.limaone.authoring.DeleteArticleLink;
import io.resys.limaone.authoring.DeleteArticleWorkflow;
import io.resys.limaone.authoring.ModifyArticle;
import io.resys.limaone.authoring.ModifyArticleLink;
import io.resys.limaone.authoring.ModifyArticlePage;
import io.resys.limaone.authoring.ModifyArticleTemplate;
import io.resys.limaone.authoring.ModifyArticleWorkflow;
import io.resys.limaone.authoring.ModifyDecisionTable;
import io.resys.limaone.authoring.ModifyDeployment;
import io.resys.limaone.authoring.ModifyFlow;
import io.resys.limaone.authoring.ModifyFlowTask;
import io.resys.limaone.authoring.ModifyLocale;
import io.resys.limaone.authoring.NewArticle;
import io.resys.limaone.authoring.NewArticleLink;
import io.resys.limaone.authoring.NewArticlePage;
import io.resys.limaone.authoring.NewArticleTemplate;
import io.resys.limaone.authoring.NewArticleWorkflow;
import io.resys.limaone.authoring.NewDecisionTable;
import io.resys.limaone.authoring.NewDeployment;
import io.resys.limaone.authoring.NewFlow;
import io.resys.limaone.authoring.NewFlowTask;
import io.resys.limaone.authoring.NewLocale;
import io.resys.limaone.spi.ast.AST_ParserImpl;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class AuthoringImpl implements Authoring {

  private final AuthoringConfig config;
  
  @Override
  public ModifyModel modifyModel() {
    return new ModifyModel() {
      @Override public ModifyLocale modifyLocale() { return new ModifyLocaleImpl(config); }
      @Override public ModifyFlowTask modifyFlowTask() { return new ModifyFlowTaskImpl(config); }
      @Override public ModifyFlow modifyFlow() { return new ModifyFlowImpl(config); }
      @Override public ModifyDecisionTable modifyDecisionTable() { return new ModifyDecisionTableImpl(config); }
      @Override public ModifyArticleWorkflow modifyArticleWorkflow() { return new ModifyArticleWorkflowImpl(config); }
      @Override public ModifyArticleTemplate modifyArticleTemplate() { return new ModifyArticleTemplateImpl(config); }
      @Override public ModifyArticlePage modifyArticlePage() { return new ModifyArticlePageImpl(config); }
      @Override public ModifyArticleLink modifyArticleLink() { return new ModifyArticleLinkImpl(config); }
      @Override public ModifyArticle modifyArticle() { return new ModifyArticleImpl(config); }
      @Override public ModifyDeployment modifyDeployment() { return new ModifyDeploymentImpl(config); }
    };
  }

  @Override
  public DeleteModel deleteModel() {
    return new DeleteModel() {
      @Override public DeleteArticleLink deleteArticleLink() { return new DeleteArticleLinkImpl(config); }
      @Override public DeleteArticleWorkflow deleteArticleWorkflow() { return new DeleteArticleWorkflowImpl(config); }
      @Override public DeleteAny deleteAny() { return new DeleteAnyImpl(config); }
    };
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
      @Override public NewDeployment newDeployment() { return new NewDeploymentImpl(config); }
    };
  }

  
  public static AuthoringBuilder builder() {
    return new AuthoringBuilder();
  }
  public static class AuthoringBuilder {
    private WorldPersistence persistence;
    private ScheduledExecutorService workerPool;
    private AST_Parser astParser;
    private Supplier<String> author = () -> "unknown";
    
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
    public AuthoringBuilder author(Supplier<String> author) {
      this.author = author;
      return this;
    }
    public AuthoringImpl build() {
      Objects.requireNonNull(persistence, () -> "persistence must be defined");

      final var astParser = this.astParser == null ? AST_ParserImpl.builder().dev(true).build() : this.astParser;
      final var workerPool = this.workerPool == null ? Infrastructure.getDefaultWorkerPool() : this.workerPool;
      final var workerTimeout = Duration.ofMinutes(15);
                  
      
      return new AuthoringImpl(ImmutableAuthoringConfig.builder()
          .workerTimeout(workerTimeout)
          .workerPool(workerPool)
          .astParser(astParser)
          .persistence(persistence)
          .author(() -> Uni.createFrom()
            .item(() -> author.get())
            .runSubscriptionOn(workerPool)
            .await().atMost(workerTimeout)
          )
          .build());
    }
  }

  @Value.Immutable
  public interface AuthoringConfig {
    WorldPersistence getPersistence();
    ScheduledExecutorService getWorkerPool();
    Duration getWorkerTimeout();
    AST_Parser getAstParser();
    Supplier<String> getAuthor();
  }
}
