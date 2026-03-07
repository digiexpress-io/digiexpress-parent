package io.resys.limaone.persistence;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import io.resys.limaone.authoring.ImmutableNewArticleWorkflowProps;
import io.resys.limaone.authoring.ImmutableNewArticleWorkflowProps.Builder;
import io.resys.limaone.authoring.NewArticleWorkflow;
import io.resys.limaone.model.ArticleWorkflow;
import io.resys.limaone.model.ImmutableArticleWorkflow;
import io.resys.limaone.model.ImmutableLocaleLabel;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.model.Model.ModelWorld;
import io.resys.limaone.persistence.AuthoringImpl.AuthoringConfig;
import io.smallrye.mutiny.Uni;



public class NewArticleWorkflowImpl extends AuthoringTemplate<NewArticleWorkflowImpl, Model<ArticleWorkflow>> implements NewArticleWorkflow {

  private NewArticleWorkflowProps props;

  public NewArticleWorkflowImpl(AuthoringConfig config) {
    super(config);
  }
  
  @Override
  public NewArticleWorkflow props(NewArticleWorkflowProps props) {
    this.props = props;
    return this;
  }

  @Override
  public NewArticleWorkflow props(Consumer<Builder> props) {
    final var builder = ImmutableNewArticleWorkflowProps.builder();
    props.accept(builder);
    return props(builder.build());
  }

  @Override
  public Uni<Model<ArticleWorkflow>> build() {
    return config.getPersistence().worldBuilder()
      .docs(BodyType.LOCALE, BodyType.ARTICLE)
      .lock().build(nextWorld -> {
        final var body = internalBuild(nextWorld.getCurrentWorld());
        return nextWorld.newModel(body);
      });
  }
  
  private ArticleWorkflow internalBuild(ModelWorld world) {
    Objects.requireNonNull(props, () -> "props must be defined");

    final var workflow = ImmutableArticleWorkflow.builder()
        .disabled(props.getDisabled())
        .devMode(props.getDevMode())
        .anon(Boolean.TRUE.equals(props.getAnon()))
        .assignable(Boolean.TRUE.equals(props.getAssignable()) ? true : null)
        .value(props.getValue())
        .startDate(props.getStartDate())
        .endDate(props.getEndDate())
        .formName(props.getFormName())
        .formTag(props.getFormTag())
        .formId(props.getFormId())
        .flowName(props.getFlowName());

    final var articles = new ArrayList<String>();
    for(final var articleRef : props.getArticles()) {
      final var article = world.getArticles().containsKey(articleRef) ? 
          Optional.of(world.getArticles().get(articleRef)) : 
            world.getArticles().values().stream().filter(l -> l.getBody().getName().equalsIgnoreCase(articleRef)).findFirst();

      if(article.isEmpty()) {
        final var msg = "Article with id: '" + articleRef + "' does not exist in: '" + String.join(",", world.getArticles().keySet()) + "'!";
        throw new AuthoringException(props, msg);          
      }
      articles.add(article.get().getId());
    }
    workflow.articles(articles);
    
    for(final var label : props.getLabels()) {
      final var localeRef = label.getLocale();
      final var locale = AuthoringMapper.resolveLocale(localeRef, world);
          
      workflow.addLabels(ImmutableLocaleLabel.builder()
          .locale(locale.map(e -> e.getId()).orElse(localeRef))
          .labelValue(label.getLabelValue())
          .build());

      if(locale.isEmpty()) {
        final var locales = String.join(",", world.getLocales().keySet());
        final var msg = "Locale with id: '" + label.getLocale() + "' does not exist in: '" + locales + "'!";
        throw new AuthoringException(props, msg);
      }
    }
    return workflow.build();
  }
}
