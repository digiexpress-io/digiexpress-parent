package io.resys.limaone.persistence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
import io.resys.limaone.persistence.ModelWorldDb.NextWorld;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;


public class ModifyArticlePageImpl extends AuthoringTemplate<ModifyArticlePageImpl, Model<ArticlePage>> implements ModifyArticlePage {

  private final List<ModifyArticlePageProps> allProps = new ArrayList<>();

  public ModifyArticlePageImpl(AuthoringConfig config) {
    super(config);
  }
  
  @Override
  public ModifyArticlePage props(ModifyArticlePageProps props) {
    this.allProps.add(props);
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
    RepoAssert.isTrue(allProps.size() == 1, () -> "expect: 1 page changes but actual: " + allProps.size());
    final var props = allProps.get(0);
    
    return config.getPersistence().worldBuilder()
      .createdAt(getCreatedAt())
      .author(getAuthor())
      .docs(BodyType.LOCALE, BodyType.ARTICLE, BodyType.ARTICLE_PAGE)
      .build(nextWorld -> {
        final var body = internalBuild(props, nextWorld);
        return nextWorld.mergeModel(props.getPageId(), props.getPageId(), body, props.getAssetDescription());
      });
  }


  @Override
  public ModifyArticlePage props(List<ModifyArticlePageProps> props) {
    this.allProps.addAll(props);
    return this;
  }

  @Override
  public Uni<List<Model<ArticlePage>>> buildAll() {
    RepoAssert.isTrue(allProps.size() > 0, () -> "expect at least: 1 page changes but actual: " + allProps.size());
    return config.getPersistence().worldBuilder()
        .createdAt(getCreatedAt())
        .author(getAuthor())
        .docs(BodyType.LOCALE, BodyType.ARTICLE, BodyType.ARTICLE_PAGE)
        .build(start -> {
          
          final var result = new ArrayList<Model<ArticlePage>>();
          var nextWorld = start;
          for(final var props : this.allProps) {
            final var body = internalBuild(props, nextWorld);
            final var model = nextWorld.mergeModel(props.getPageId(), props.getPageId(), body, props.getAssetDescription());
            result.add(model);
          }
          
          return Collections.unmodifiableList(result);
        });
  }

  @Override
  public List<Model<ArticlePage>> buildAllSync() {
    return this.buildAll()
        .runSubscriptionOn(config.getEnvir().getWorkerPool())
        .await().atMost(config.getEnvir().getWorkerPoolMaxTimeout());
  }
  

  private ArticlePage internalBuild(ModifyArticlePageProps props, NextWorld nextWorld) {
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
