package io.resys.limaone.persistence;

import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Consumer;

import io.resys.limaone.authoring.ImmutableNewArticleLinkProps;
import io.resys.limaone.authoring.ImmutableNewArticleLinkProps.Builder;
import io.resys.limaone.authoring.NewArticleLink;
import io.resys.limaone.model.ArticleLink;
import io.resys.limaone.model.ImmutableArticleLink;
import io.resys.limaone.model.ImmutableLocaleLabel;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.model.Model.ModelWorld;
import io.resys.limaone.persistence.AuthoringImpl.AuthoringConfig;
import io.smallrye.mutiny.Uni;



public class NewArticleLinkImpl extends AuthoringTemplate<NewArticleLinkImpl, Model<ArticleLink>>  implements NewArticleLink {

  private NewArticleLinkProps props;
  
  public NewArticleLinkImpl(AuthoringConfig config) {
    super(config);
  }
  
  @Override
  public NewArticleLink props(NewArticleLinkProps props) {
    this.props = props;
    return this;
  }

  @Override
  public NewArticleLink props(Consumer<Builder> props) {
    final var builder = ImmutableNewArticleLinkProps.builder();
    props.accept(builder);
    return props(builder.build());
  }

  @Override
  public Uni<Model<ArticleLink>> build() {
    return config.getPersistence().worldBuilder()
      .docs(BodyType.LOCALE, BodyType.ARTICLE, BodyType.ARTICLE_LINK)
      .createdAt(getCreatedAt())
      .author(getAuthor())
      .build(nextWorld -> {
        final var body = internalBuild(nextWorld.getCurrentWorld());
        return nextWorld.newModel(body.getValue(), body);
      });
  }
  
  private ArticleLink internalBuild(ModelWorld world) {
    Objects.requireNonNull(props, () -> "props must be defined");

    final var link = ImmutableArticleLink.builder()
      .devMode(props.getDevMode())
      .contentType(props.getType())
      .value(props.getValue());
    
    final var articles = new ArrayList<String>();
    for(final var articleRef : props.getArticles()) {
      final var article = world.findOneArticle(articleRef);

      if(article.isEmpty()) {
        throw new AuthoringException(
            props, 
            "Article with id: '" + articleRef + "' does not exist in: '" + String.join(",", world.getArticles().keySet()) + "'!");          
      }
      articles.add(article.get().getId());
    }
    link.articles(articles);
    
    for(final var label : props.getLabels()) {
      
      final var localeRef = label.getLocale();
      final var locale = world.findOneLocale(localeRef);
      
      link.addLabels(ImmutableLocaleLabel.builder()
          .locale(locale.map(e -> e.getId()).orElse(localeRef))
          .labelValue(label.getLabelValue())
          .build());
      
      if(locale.isEmpty()) {
        throw new AuthoringException(
            props, 
            "Locale with id: '" + label.getLocale() + "' does not exist in: '" + String.join(",", world.getLocales().keySet()) + "'!");          
      }
    }
    return link.build();
  }
}