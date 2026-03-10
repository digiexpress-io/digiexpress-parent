package io.resys.limaone.persistence;

import java.util.Objects;
import java.util.function.Consumer;

import io.resys.limaone.authoring.ImmutableModifyArticlePageProps;
import io.resys.limaone.authoring.ImmutableModifyArticlePageProps.Builder;
import io.resys.limaone.authoring.ModifyArticlePage;
import io.resys.limaone.model.ArticlePage;
import io.resys.limaone.model.ImmutableArticlePage;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.model.Model.ModelWorld;
import io.resys.limaone.persistence.AuthoringImpl.AuthoringConfig;
import io.resys.limaone.persistence.WorldPersistence.NextWorld;
import io.smallrye.mutiny.Uni;


public class ModifyArticlePageImpl extends AuthoringTemplate<ModifyArticlePageImpl, Model<ArticlePage>> implements ModifyArticlePage {

  private ModifyArticlePageProps props;

  public ModifyArticlePageImpl(AuthoringConfig config) {
    super(config);
  }
  
  @Override
  public ModifyArticlePage props(ModifyArticlePageProps props) {
    this.props = props;
    return this;
  }

  @Override
  public ModifyArticlePage props(Consumer<Builder> props) {
    final var builder = ImmutableModifyArticlePageProps.builder();
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
        final var body = internalBuild(nextWorld);
        return nextWorld.mergeModel(props.getPageId(), body.getLocale(), body);
      });
  }
  
  private ArticlePage internalBuild(NextWorld nextWorld) {
    Objects.requireNonNull(props, () -> "props must be defined");
    final ModelWorld world = nextWorld.getCurrentWorld();
    
    final var start = world.getArticlePages().get(props.getPageId());
    if(start == null) {
      throw new AuthoringException(props, "Article page with id: '" + props.getPageId() + "' not found!");
    }
    
    // Validate locale exists
    final var localeRef = props.getLocale();
    final var locale = world.findOneLocale(localeRef);
    if(locale.isEmpty()) {
      final var locales = String.join(",", world.getLocales().keySet());
      throw new AuthoringException(props, "Locale with id: '" + localeRef + "' does not exist in: '" + locales + "'!");
    }
    return ImmutableArticlePage.builder()
      .from(start.getBody())
      .content(props.getContent())
      .locale(locale.get().getId())
      .devMode(props.getDevMode())
      .build();
  }
}