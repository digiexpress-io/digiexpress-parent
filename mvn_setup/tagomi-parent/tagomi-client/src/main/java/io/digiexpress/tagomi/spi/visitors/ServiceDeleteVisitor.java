package io.digiexpress.tagomi.spi.visitors;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

import java.util.Optional;
import java.util.stream.Collectors;

import io.digiexpress.tagomi.api.ImmutableBatchCommand;
import io.digiexpress.tagomi.api.TagomiStore;
import io.digiexpress.tagomi.api.entities.ImmutableResource;
import io.digiexpress.tagomi.api.entities.TagomiContainer;
import io.digiexpress.tagomi.api.entities.TagomiContainer.Resource;
import io.digiexpress.tagomi.api.entities.TagomiContainer.Service;
import io.digiexpress.tagomi.api.entities.TagomiContainer.Template;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;



@RequiredArgsConstructor
public class ServiceDeleteVisitor {
  private final TagomiStore client;
  private final String articleId;
  
  
  public Uni<Service> visit() {
    return client.stateQuery().getState().onItem()
        .transformToUni(state -> visitObjects(state));
  }
  
  private Uni<Service> visitObjects(TagomiContainer state) {
    final var start = visitArticleId(state, articleId);
    final var updateCommand = ImmutableBatchCommand.builder();
    final var templateIds = new ArrayList<String>();
    
    for(final var page : state.getTemplates().values()) {
      visitPage(page).ifPresent(changeEntity -> {
        templateIds.add(changeEntity.getId());
        updateCommand.addToBeDeleted(changeEntity);
      });
    }
    
    for(final var link : state.getResources().values()) {
      visitLink(link, templateIds).ifPresent(changeEntity -> updateCommand.addToBeSaved(changeEntity));
    }
    
    updateCommand.addToBeDeleted(start);
    
    return client.upsertBuilder()
        .batch(updateCommand.build())
        .onItem().transform(updated -> start);
  }


  public Optional<Resource> visitLink(Resource start, List<String> templateIds) {
    var newArticles = start
        .getTemplateIds().stream().filter(a -> !templateIds.contains(a))
        .collect(Collectors.toList());
    
    if(newArticles.size() == start.getTemplateIds().size()) {
      return Optional.empty();
    }
    
    return Optional.of(ImmutableResource.builder().from(start)
        .templateIds(newArticles)
        .build());
  }
  
  public Optional<Template> visitPage(Template page) {
    if(page.getServiceId().equals(articleId)) {
      return Optional.of(page);
    }
    return Optional.empty();
  }

  private Service visitArticleId(TagomiContainer state, String articleId) {
    final var article = state.getServices().get(articleId);
    Objects.requireNonNull(article != null, () -> "Can't find service with id: '" + articleId + "'");
    return article;
  }
}
