package io.thestencil.client.spi.builders;

import java.util.List;

/*-
 * #%L
 * stencil-persistence
 * %%
 * Copyright (C) 2021 Copyright 2021 ReSys OÜ
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

import java.util.Map;
import java.util.stream.Collectors;

import io.resys.thena.docdb.api.actions.ObjectsActions.ObjectsStatus;
import io.resys.thena.docdb.api.models.Objects.Blob;
import io.resys.thena.docdb.api.models.Objects.Tree;
import io.smallrye.mutiny.Uni;
import io.thestencil.client.api.ImmutableSiteState;
import io.thestencil.client.api.StencilConfig;
import io.thestencil.client.api.StencilClient.Article;
import io.thestencil.client.api.StencilClient.Entity;
import io.thestencil.client.api.StencilClient.EntityBody;
import io.thestencil.client.api.StencilClient.EntityType;
import io.thestencil.client.api.StencilClient.Link;
import io.thestencil.client.api.StencilClient.Locale;
import io.thestencil.client.api.StencilClient.Page;
import io.thestencil.client.api.StencilClient.Release;
import io.thestencil.client.api.StencilClient.Template;
import io.thestencil.client.api.StencilClient.Workflow;
import io.thestencil.client.api.StencilComposer.SiteContentType;
import io.thestencil.client.api.StencilComposer.SiteState;
import io.thestencil.client.api.StencilStore.QueryBuilder;
import io.thestencil.client.api.StencilConfig.EntityState;
import io.thestencil.client.spi.PersistenceCommands;
import io.thestencil.client.spi.exceptions.QueryException;
import io.thestencil.client.spi.exceptions.RefException;

public class QueryBuilderImpl extends PersistenceCommands implements QueryBuilder {
  
  public QueryBuilderImpl(StencilConfig config) {
    super(config);
  }

  @Override
  public Uni<SiteState> head() {
    final var siteName = config.getRepoName() + ":" + config.getHeadName();
    return config.getClient().repo().query().id(config.getRepoName()).get().onItem()
      .transformToUni(repo -> {
        if(repo == null) {
         return Uni.createFrom().item(ImmutableSiteState.builder()
              .name(siteName)
              .contentType(SiteContentType.NOT_CREATED)
              .build()); 
        }
      
        return config.getClient()
            .objects().refState()
            .repo(config.getRepoName())
            .ref(config.getHeadName())
            .blobs(true)
            .build().onItem()
            .transform(state -> {
              if(state.getStatus() == ObjectsStatus.ERROR) {
                throw new RefException(siteName, state);
              }

              // Nothing present
              if(state.getObjects() == null) {
                return ImmutableSiteState.builder()
                    .name(siteName)
                    .contentType(SiteContentType.EMPTY)
                    .build();
              }
              
              final var commit = state.getObjects().getCommit();
              final var tree = state.getObjects().getTree();
              final var blobs = state.getObjects().getBlobs();
              final var builder = mapTree(tree, blobs, config);
              return builder
                  .commit(commit.getId())
                  .name(siteName)
                  .contentType(SiteContentType.OK)
                  .build();
            });
      });
  }
  
  @SuppressWarnings("unchecked")
  public static ImmutableSiteState.Builder mapTree(Tree tree, Map<String, Blob> blobs, StencilConfig config) {
    final var builder = ImmutableSiteState.builder();
    for(final var treeValue : tree.getValues().values()) {
      final var blob = blobs.get(treeValue.getBlob());
      final var entity = config.getDeserializer().fromString(blob.getValue());
      final var id = entity.getId();
      
      switch (entity.getType()) {
      case ARTICLE:
        builder.putArticles(id, (Entity<Article>) entity);
        break;
      case LINK:
        builder.putLinks(id, (Entity<Link>) entity);
        break;
      case LOCALE:
        builder.putLocales(id, (Entity<Locale>) entity);
        break;
      case PAGE:
        builder.putPages(id, (Entity<Page>) entity);
        break;
      case RELEASE:
        builder.putReleases(id, (Entity<Release>) entity);
        break;
      case WORKFLOW:
        builder.putWorkflows(id, (Entity<Workflow>) entity);
        break;
      case TEMPLATE:
        builder.putTemplates(id, (Entity<Template>) entity);
        break;
      default: throw new RuntimeException("Don't know how to convert entity: " + entity.toString() + "!");
      }
    }
    return builder;
  }

  @Override
  public Uni<SiteState> release(String releaseId) {
    // Get the page
    final Uni<EntityState<Release>> query = get(releaseId, EntityType.RELEASE);
    
    return query.onItem().transformToUni(this::getCommitState);
  }
  
  private Uni<SiteState> getCommitState(EntityState<Release> release) {
    return config.getClient().objects().commitState()
    .repo(config.getRepoName())
    .anyId(release.getEntity().getBody().getParentCommit())
    .blobs(true)
    .build().onItem()
    .transform(state -> {
      if(state.getStatus() == ObjectsStatus.ERROR) {
        throw new QueryException("Can't find release commit: '" + release.getEntity().getBody().getParentCommit() + "'!", EntityType.RELEASE, state);
      }
      
      final var tree = state.getObjects().getTree();
      final var blobs = state.getObjects().getBlobs();
      final var builder = mapTree(tree, blobs, config).putReleases(release.getEntity().getId(), release.getEntity());
      return builder.name(config.getRepoName() + ":" + config.getHeadName() + ":" + release.getEntity().getBody().getName()).contentType(SiteContentType.RELEASE).build();
    });
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends EntityBody> Uni<List<Entity<T>>> head(List<String> ids, EntityType type) {
    return config.getClient()
    .objects().blobState()
    .repo(config.getRepoName())
    .anyId(config.getHeadName())
    .blobNames(ids)
    .list().onItem()
    .transform(state -> {
      
      if(state.getStatus() != ObjectsStatus.OK) {
        throw new QueryException(String.join(",", ids), type, state);  
      }
      
      return state.getObjects().getBlob().stream()
        .map(blob -> (Entity<T>) config.getDeserializer().fromString(type, blob.getValue()))
        .collect(Collectors.toList());
      
    });
  }
}
