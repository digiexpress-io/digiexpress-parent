package io.resys.limaone.persistence;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import io.resys.limaone.authoring.DeleteArticleWorkflow;
import io.resys.limaone.authoring.ImmutableDeleteArticleWorkflowProps;
import io.resys.limaone.authoring.ImmutableDeleteArticleWorkflowProps.Builder;
import io.resys.limaone.model.ArticleWorkflow;
import io.resys.limaone.model.ImmutableArticleWorkflow;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.model.Model.ModelWorld;
import io.resys.limaone.persistence.AuthoringImpl.AuthoringConfig;
import io.resys.limaone.persistence.WorldPersistence.NextWorld;
import io.smallrye.mutiny.Uni;


public class DeleteArticleWorkflowImpl extends AuthoringTemplate<DeleteArticleWorkflowImpl, Model<ArticleWorkflow>> implements DeleteArticleWorkflow {

  private DeleteArticleWorkflowProps props;

  public DeleteArticleWorkflowImpl(AuthoringConfig config) {
    super(config);
  }
  
  @Override
  public DeleteArticleWorkflow props(DeleteArticleWorkflowProps props) {
    this.props = props;
    return this;
  }

  @Override
  public DeleteArticleWorkflow props(Consumer<Builder> props) {
    final var builder = ImmutableDeleteArticleWorkflowProps.builder();
    props.accept(builder);
    return props(builder.build());
  }

  @Override
  public Uni<Model<ArticleWorkflow>> build() {
    return config.getPersistence().worldBuilder()
      .createdAt(getCreatedAt())
      .author(getAuthor())
      .docs(BodyType.ARTICLE, BodyType.ARTICLE_WORKFLOW)
      .build(nextWorld -> {
        final var raw = internalBuild(nextWorld);
        if(raw.isEmpty()) {
          final Model<ArticleWorkflow> noChanges = nextWorld.getCurrentWorld().getArticleWorkflows().get(props.getWorkflowId());
          return noChanges;
        }
        final var body = raw.get();
        return nextWorld.mergeModel(props.getWorkflowId(), body.getValue(), body);
      });
  }
  
  private Optional<ArticleWorkflow> internalBuild(NextWorld nextWorld) {
    Objects.requireNonNull(props, () -> "props must be defined");
    final ModelWorld world = nextWorld.getCurrentWorld();
    
    final var start = world.getArticleWorkflows().get(props.getWorkflowId());
    if(start == null) {
      throw new AuthoringException(props, "Article workflow with id: '" + props.getWorkflowId() + "' not found!");
    }
    
    // Validate article exists
    final var article = world.findOneArticle(props.getArticleId());
    if(article.isEmpty()) {
      final var articles = String.join(",", world.getArticles().keySet());
      throw new AuthoringException(props, "Article with id: '" + props.getArticleId() + "' does not exist in: '" + articles + "'!");
    }
    
    final var newArticles = start.getBody()
        .getArticles().stream().filter(a -> !a.equals(props.getArticleId()))
        .collect(Collectors.toList());
    
    if(newArticles.size() == start.getBody().getArticles().size()) {
      return Optional.empty();
    }
    
    return Optional.ofNullable(ImmutableArticleWorkflow.builder().from(start.getBody())
        .articles(newArticles)
        .build());
  }
}