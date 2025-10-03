package io.digiexpress.tagomi.spi.commands;

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

import java.util.stream.Collectors;

import io.digiexpress.tagomi.api.TagomiStore;
import io.digiexpress.tagomi.api.commands.TagomiDeleteCommands;
import io.digiexpress.tagomi.api.entities.ImmutableResource;
import io.digiexpress.tagomi.api.entities.TagomiContainer;
import io.digiexpress.tagomi.api.entities.TagomiContainer.Locale;
import io.digiexpress.tagomi.api.entities.TagomiContainer.Resource;
import io.digiexpress.tagomi.api.entities.TagomiContainer.Tag;
import io.digiexpress.tagomi.api.entities.TagomiContainer.TagomiDocType;
import io.digiexpress.tagomi.api.entities.TagomiContainer.Template;
import io.digiexpress.tagomi.api.entities.TagomiEntityContainer;
import io.digiexpress.tagomi.spi.visitors.ServiceDeleteVisitor;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;



@RequiredArgsConstructor
public class TagomiDeleteCommandsImpl implements TagomiDeleteCommands {
  private final TagomiStore client;

  @Override
  public Uni<TagomiContainer.Locale> locale(String localeId) {
    // Get the locale
    final var query = client.stateQuery().getEntityState(localeId, TagomiDocType.LOCALE);
    
    // Delete the locale
    return query.onItem().transformToUni(state -> {
      final Locale locale = (Locale) state.getEntity();
      return client.upsertBuilder().delete(state.getEntity()).onItem().transform(ignore -> locale);
    });
  }
  
  @Override
  public Uni<Template> template(String templateId) {
    // Get the page
    final var query = client.stateQuery().getEntityState(templateId, TagomiDocType.TEMPLATE);
    
    // Delete the template
    return query.onItem().transformToUni(state -> {
      final Template template = (Template) state.getEntity();
      return client.upsertBuilder().delete(state.getEntity()).onItem().transform(ignore -> template);
    });
  }

  @Override
  public Uni<Resource> resource(String linkId) {
    // Get the link
    final var query = client.stateQuery().getEntityState(linkId, TagomiDocType.RESOURCE);
    
    // Delete the link
    return query.onItem().transformToUni(state -> {
      final Resource resource = (Resource) state.getEntity();
      return client.upsertBuilder().delete(state.getEntity()).onItem().transform(ignore -> resource);
    });
  }
  @Override
  public Uni<Tag> tag(String releaseId) {
    // Get the link
    final var query = client.stateQuery().getEntityState(releaseId, TagomiDocType.TAG);
    
    // Delete the link
    return query.onItem().transformToUni(state -> {
      final Tag tag = (Tag) state.getEntity();
      return client.upsertBuilder().delete(state.getEntity()).onItem().transform(ignore -> tag);
    });
  }
  @Override
  public Uni<TagomiContainer.Service> service(String articleId) {
    // Delete the article
    return new ServiceDeleteVisitor(client, articleId).visit();
  
  }
  @Override
  public Uni<Resource> resourceOnTemplate(ResourceOnTemplate resourceOnTemplate) {
    
    // Get the link
    final Uni<TagomiEntityContainer> query = client.stateQuery()
        .getEntityState(resourceOnTemplate.getResourceId(), TagomiDocType.RESOURCE);
    
    return query.onItem().transformToUni(state -> {
      final Resource start = (Resource) state.getEntity();
      
      var newArticles = start
          .getTemplateIds().stream().filter(a -> !a.equals(resourceOnTemplate.getTemplateId()))
          .collect(Collectors.toList());
      
      if(newArticles.size() == start.getTemplateIds().size()) {
        return Uni.createFrom().item(start);
      }
      
      final var end = ImmutableResource.builder()
        .id(start.getId())
        .from(start)
        .templateIds(newArticles)
        .build();
      
      return client.upsertBuilder().save(end);
    });
  }

}
