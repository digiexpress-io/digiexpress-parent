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
import java.util.Collections;
import java.util.Objects;
import java.util.function.Consumer;

import io.resys.limaone.authoring.ImmutableModifyArticleLinkProps;
import io.resys.limaone.authoring.ImmutableModifyArticleLinkProps.Builder;
import io.resys.limaone.authoring.ModifyArticleLink;
import io.resys.limaone.model.ArticleLink;
import io.resys.limaone.model.ImmutableArticleLink;
import io.resys.limaone.model.ImmutableLocaleLabel;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.model.Model.ModelWorld;
import io.resys.limaone.persistence.AuthoringImpl.AuthoringConfig;
import io.resys.limaone.persistence.ModelWorldDb.NextWorld;
import io.smallrye.mutiny.Uni;


public class ModifyArticleLinkImpl extends AuthoringTemplate<ModifyArticleLinkImpl, Model<ArticleLink>> implements ModifyArticleLink {

  private ModifyArticleLinkProps props;

  public ModifyArticleLinkImpl(AuthoringConfig config) {
    super(config);
  }
  
  @Override
  public ModifyArticleLink props(ModifyArticleLinkProps props) {
    this.props = props;
    return this;
  }

  @Override
  public ModifyArticleLink props(Consumer<Builder> props) {
    final var builder = ImmutableModifyArticleLinkProps.builder();
    props.accept(builder);
    return props(builder.build());
  }

  @Override
  public Uni<Model<ArticleLink>> build() {
    return config.getPersistence().worldBuilder()
      .createdAt(getCreatedAt())
      .author(getAuthor())
      .docs(BodyType.LOCALE, BodyType.ARTICLE, BodyType.ARTICLE_LINK)
      .build(nextWorld -> {
        final var body = internalBuild(nextWorld);
        return nextWorld.mergeModel(props.getLinkId(), body.getValue(), body);
      });
  }
  
  private ArticleLink internalBuild(NextWorld nextWorld) {
    Objects.requireNonNull(props, () -> "props must be defined");
    final ModelWorld world = nextWorld.getCurrentWorld();
    
    final var start = world.getArticleLinks().get(props.getLinkId());
    if(start == null) {
      throw new AuthoringException(props, "Article link with id: '" + props.getLinkId() + "' not found!");
    }
    
    final var link = ImmutableArticleLink.builder()
      .from(start.getBody())
      .devMode(props.getDevMode())
      .contentType(props.getType())
      .value(props.getValue());
    
    // Handle articles if provided
    if(props.getArticles() != null) {
      final var articles = new ArrayList<String>();
      for(final var articleRef : props.getArticles()) {
        final var article = world.findOneArticle(articleRef);

        if(article.isEmpty()) {
          throw new AuthoringException(props, 
              "Article with id: '" + articleRef + "' does not exist in: '" + String.join(",", world.getArticles().keySet()) + "'!");          
        }
        articles.add(article.get().getId());
      }
      link.articles(articles);
    }
    
    // Handle labels if provided
    if(props.getLabels() != null) {
      link.labels(Collections.emptyList());
      for(final var label : props.getLabels()) {
        final var localeRef = label.getLocale();
        final var locale = world.findOneLocale(localeRef);
            
        link.addLabels(ImmutableLocaleLabel.builder()
            .locale(locale.map(e -> e.getId()).orElse(localeRef))
            .labelValue(label.getLabelValue())
            .build());

        if(locale.isEmpty()) {
          throw new AuthoringException(props, 
              "Locale with id: '" + label.getLocale() + "' does not exist in: '" + String.join(",", world.getLocales().keySet()) + "'!");          
        }
      }
    }
    
    return link.build();
  }
}
