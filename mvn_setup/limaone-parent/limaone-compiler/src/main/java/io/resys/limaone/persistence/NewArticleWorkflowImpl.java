package io.resys.limaone.persistence;

/*-
 * #%L
 * limaone-compiler
 * %%
 * Copyright (C) 2015 - 2026 Copyright 2022 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.ArrayList;
import java.util.Objects;
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
      .createdAt(getCreatedAt())
      .author(getAuthor())
      .build(nextWorld -> {
        final var body = internalBuild(nextWorld.getCurrentWorld());
        return nextWorld.newModel(body.getValue(), body, props.getAssetDescription(), props.getAssetLabels());
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
      final var article = world.findOneArticle(articleRef);

      if(article.isEmpty()) {
        final var msg = "Article with id: '" + articleRef + "' does not exist in: '" + String.join(",", world.getArticles().keySet()) + "'!";
        throw new AuthoringException(props, msg);          
      }
      articles.add(article.get().getId());
    }
    workflow.articles(articles);
    
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
    return workflow.build();
  }
}
