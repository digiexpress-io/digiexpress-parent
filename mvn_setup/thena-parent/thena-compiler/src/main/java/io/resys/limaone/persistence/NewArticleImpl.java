package io.resys.limaone.persistence;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import io.resys.limaone.authoring.ImmutableNewArticleProps;
import io.resys.limaone.authoring.ImmutableNewArticleProps.Builder;
import io.resys.limaone.authoring.NewArticle;
import io.resys.limaone.model.Article;
import io.resys.limaone.model.ImmutableArticle;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.model.Model.ModelWorld;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class NewArticleImpl implements NewArticle {
  private final WorldPersistence persistence;
  private NewArticleProps props;
  
  @Override
  public NewArticle props(NewArticleProps props) {
    this.props = props;
    return this;
  }

  @Override
  public NewArticle props(Consumer<Builder> props) {
    final var builder = ImmutableNewArticleProps.builder();
    props.accept(builder);
    return props(builder.build());
  }

  @Override
  public Uni<Model<Article>> build() {
    return persistence.worldBuilder()
      .docs(BodyType.ARTICLE)
      .lock().build(nextWorld -> {
        final var body = internalBuild(nextWorld.getCurrentWorld());
        return nextWorld.newModel(body);
      });
  }
  
  private Article internalBuild(ModelWorld world) {
    Objects.requireNonNull(props, () -> "props must be defined");

    final var article = ImmutableArticle.builder()
        .authOnly(props.getAuthOnly())
        .devMode(props.getDevMode())
        .name(props.getName())
        .parentId(props.getParentId())
        .order(Optional.ofNullable(props.getOrder()).orElse(0));
    
    final var duplicate = world.getArticles().values().stream()
        .filter(p -> p.getBody().getName().equals(props.getName()))
        .findFirst();
    
    if(duplicate.isPresent()) {
      final var msg = "Article: '" + props.getName() + "' already exists!";
      throw new AuthoringException(props, msg);
    }

    if(props.getParentId() != null && !world.getArticles().containsKey(props.getParentId())) {
      final var msg = "Article: '" + props.getName() + "', parent: '" + props.getParentId() + "' does not exist!";
      throw new AuthoringException(props, msg);
    }
    
    return article.build();
  }
}