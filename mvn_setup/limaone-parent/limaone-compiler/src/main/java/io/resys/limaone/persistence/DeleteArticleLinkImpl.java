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
import io.resys.limaone.persistence.ModelWorldDb.NextWorld;
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
        return nextWorld.mergeModel(props.getLinkId(), body.getValue(), body, null);
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
