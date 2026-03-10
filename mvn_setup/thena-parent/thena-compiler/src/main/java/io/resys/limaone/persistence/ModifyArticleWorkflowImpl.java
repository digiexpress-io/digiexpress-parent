package io.resys.limaone.persistence;

import java.util.Collections;
import java.util.Objects;
import java.util.function.Consumer;

import io.resys.limaone.authoring.ImmutableModifyArticleWorkflowProps;
import io.resys.limaone.authoring.ImmutableModifyArticleWorkflowProps.Builder;
import io.resys.limaone.authoring.ModifyArticleWorkflow;
import io.resys.limaone.model.ArticleWorkflow;
import io.resys.limaone.model.ImmutableArticleWorkflow;
import io.resys.limaone.model.ImmutableLocaleLabel;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.model.Model.ModelWorld;
import io.resys.limaone.persistence.AuthoringImpl.AuthoringConfig;
import io.resys.limaone.persistence.WorldPersistence.NextWorld;
import io.smallrye.mutiny.Uni;


public class ModifyArticleWorkflowImpl extends AuthoringTemplate<ModifyArticleWorkflowImpl, Model<ArticleWorkflow>> implements ModifyArticleWorkflow {

  private ModifyArticleWorkflowProps props;

  public ModifyArticleWorkflowImpl(AuthoringConfig config) {
    super(config);
  }
  
  @Override
  public ModifyArticleWorkflow props(ModifyArticleWorkflowProps props) {
    this.props = props;
    return this;
  }

  @Override
  public ModifyArticleWorkflow props(Consumer<Builder> props) {
    final var builder = ImmutableModifyArticleWorkflowProps.builder();
    props.accept(builder);
    return props(builder.build());
  }

  @Override
  public Uni<Model<ArticleWorkflow>> build() {
    return config.getPersistence().worldBuilder()
      .createdAt(getCreatedAt())
      .author(getAuthor())
      .docs(BodyType.LOCALE, BodyType.ARTICLE, BodyType.ARTICLE_WORKFLOW)
      .build(nextWorld -> {
        final var body = internalBuild(nextWorld);
        return nextWorld.mergeModel(props.getWorkflowId(), body.getValue(), body);
      });
  }
  
  private ArticleWorkflow internalBuild(NextWorld nextWorld) {
    Objects.requireNonNull(props, () -> "props must be defined");
    final ModelWorld world = nextWorld.getCurrentWorld();
    
    final var start = world.getArticleWorkflows().get(props.getWorkflowId());
    if(start == null) {
      throw new AuthoringException(props, "Article workflow with id: '" + props.getWorkflowId() + "' not found!");
    }
    
    final var workflow = ImmutableArticleWorkflow.builder()
      .from(start.getBody())
      
      .devMode(props.getDevMode())
      .anon(Boolean.TRUE.equals(props.getAnon()))
      .assignable(Boolean.TRUE.equals(props.getAssignable()) ? true : null)
      .disabled(Boolean.TRUE.equals(props.getDisabled()) ? true : null)
      .value(props.getValue())
      .startDate(props.getStartDate())
      .endDate(props.getEndDate())
      
      .formName(props.getFormName())
      .formTag(props.getFormTag())
      .formId(props.getFormId());
    
    // Handle articles if provided
    if(props.getArticles() != null) {
      workflow.articles(Collections.emptyList());
      
      for(final var articleRef : props.getArticles()) {
        final var article = world.findOneArticle(articleRef);

        if(article.isEmpty()) {
          final var msg = "Article with id: '" + articleRef + "' does not exist in: '" + String.join(",", world.getArticles().keySet()) + "'!";
          throw new AuthoringException(props, msg);          
        }
        workflow.addArticles(article.get().getId());
      }
      
    }
    
    // Handle labels if provided
    if(props.getLabels() != null) {
      workflow.labels(Collections.emptyList());
      for(final var label : props.getLabels()) {
        final var localeRef = label.getLocale();
        final var locale = world.findOneLocale(localeRef);
            
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
    }
    
    return workflow.build();
  }
}