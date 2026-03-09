package io.resys.limaone.persistence;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import io.resys.limaone.authoring.ImmutableNewArticlePageProps;
import io.resys.limaone.authoring.ImmutableNewArticlePageProps.Builder;
import io.resys.limaone.authoring.NewArticlePage;
import io.resys.limaone.model.ArticlePage;
import io.resys.limaone.model.ImmutableArticlePage;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.model.Model.ModelWorld;
import io.resys.limaone.persistence.AuthoringImpl.AuthoringConfig;
import io.smallrye.mutiny.Uni;


public class NewArticlePageImpl extends AuthoringTemplate<NewArticlePageImpl, Model<ArticlePage>> implements NewArticlePage {

  private NewArticlePageProps props;

  public NewArticlePageImpl(AuthoringConfig config) {
    super(config);
  }

  @Override
  public NewArticlePage props(NewArticlePageProps props) {
    this.props = props;
    return this;
  }

  @Override
  public NewArticlePage props(Consumer<Builder> props) {
    final var builder = ImmutableNewArticlePageProps.builder();
    props.accept(builder);
    return props(builder.build());
  }

  @Override
  public Uni<Model<ArticlePage>> build() {
    return config.getPersistence().worldBuilder()
      .createdAt(getCreatedAt())
      .author(getAuthor())
      .docs(BodyType.LOCALE, BodyType.ARTICLE, BodyType.ARTICLE_PAGE)
      .build(nextWorld -> {
        final var body = internalBuild(nextWorld.getCurrentWorld());
        return nextWorld.newModel(body.getLocale(), body);
      });
  }
  
  private ArticlePage internalBuild(ModelWorld world) {
    Objects.requireNonNull(props, () -> "props must be defined");

    final var localeRef = props.getLocale();
    final var locale = AuthoringMapper.resolveLocale(localeRef, world);
    
    final var articleRef = props.getArticleId();
    final var article = world.getArticles().containsKey(articleRef) ? 
      Optional.of(world.getArticles().get(articleRef)) : 
      world.getArticles().values().stream().filter(l -> l.getBody().getName().equalsIgnoreCase(articleRef)).findFirst();

    final var page = ImmutableArticlePage.builder()
      .devMode(props.getDevMode())
      .article(article.map(e -> e.getId()).orElse(articleRef))
      .locale(locale.map(e -> e.getId()).orElse(localeRef))
      .content(Optional.ofNullable(props.getContent()).orElse(""));

    if(locale.isEmpty()) {
      final var msg = "Locale with id: '" + localeRef + "' does not exist in: '" + String.join(",", world.getLocales().keySet()) + "'!";
      throw new AuthoringException(props, msg);          
    }
    if(article.isEmpty()) {
      throw new AuthoringException(props, "Article with id: '" + articleRef + "' does not exist in: '" + String.join(",", world.getArticles().keySet()) + "'!");          
    }

    final var duplicate = world.getArticlePages().values().stream()
      .filter(p -> p.getBody().getArticle().equals(props.getArticleId()))
      .filter(p -> p.getBody().getLocale().equals(props.getLocale()))
      .findFirst();
    
    if(duplicate.isPresent()) {
      final var msg = "Page locale with id: '" + locale.get().getId() + "' already exists!";
      throw new AuthoringException(props, msg);
    }
    return page.build();
  }
}