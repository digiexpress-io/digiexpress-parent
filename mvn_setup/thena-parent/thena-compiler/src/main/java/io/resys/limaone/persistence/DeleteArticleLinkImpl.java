package io.resys.limaone.persistence;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import io.resys.limaone.authoring.DeleteArticleLink;
import io.resys.limaone.authoring.ImmutableDeleteArticleLinkProps;
import io.resys.limaone.authoring.ImmutableDeleteArticleLinkProps.Builder;
import io.resys.limaone.model.ArticleLink;
import io.resys.limaone.model.ImmutableArticleLink;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.model.Model.ModelWorld;
import io.resys.limaone.persistence.AuthoringImpl.AuthoringConfig;
import io.resys.limaone.persistence.WorldPersistence.NextWorld;
import io.smallrye.mutiny.Uni;


public class DeleteArticleLinkImpl extends AuthoringTemplate<DeleteArticleLinkImpl, Model<ArticleLink>> implements DeleteArticleLink {

  private DeleteArticleLinkProps props;

  public DeleteArticleLinkImpl(AuthoringConfig config) {
    super(config);
  }
  
  @Override
  public DeleteArticleLink props(DeleteArticleLinkProps props) {
    this.props = props;
    return this;
  }

  @Override
  public DeleteArticleLink props(Consumer<Builder> props) {
    final var builder = ImmutableDeleteArticleLinkProps.builder();
    props.accept(builder);
    return props(builder.build());
  }

  @Override
  public Uni<Model<ArticleLink>> build() {
    return config.getPersistence().worldBuilder()
      .createdAt(getCreatedAt())
      .author(getAuthor())
      .docs(BodyType.ARTICLE, BodyType.ARTICLE_LINK)
      .build(nextWorld -> {
        final var raw = internalBuild(nextWorld);
        if(raw.isEmpty()) {
          final Model<ArticleLink> noChanges = nextWorld.getCurrentWorld().getArticleLinks().get(props.getLinkId());
          return noChanges;
        }
        final var body = raw.get();
        return nextWorld.mergeModel(props.getLinkId(), body.getValue(), body);
      });
  }
  
  private Optional<ArticleLink> internalBuild(NextWorld nextWorld) {
    Objects.requireNonNull(props, () -> "props must be defined");
    final ModelWorld world = nextWorld.getCurrentWorld();
    
    final var start = world.getArticleLinks().get(props.getLinkId());
    if(start == null) {
      throw new AuthoringException(props, "Article link with id: '" + props.getLinkId() + "' not found!");
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
    
    return Optional.ofNullable(ImmutableArticleLink.builder().from(start.getBody())
        .articles(newArticles)
        .build());
  }
}