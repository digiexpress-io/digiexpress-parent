package io.digiexpress.tagomi.spi.commands;
import java.util.Base64;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import io.digiexpress.tagomi.api.TagomiImageStorage;
import io.digiexpress.tagomi.api.TagomiImageStorage.Image;
import io.digiexpress.tagomi.api.TagomiImageStorage.OperationStatus;
import io.digiexpress.tagomi.api.TagomiStore;
import io.digiexpress.tagomi.api.commands.TagomiUpdateCommands;
import io.digiexpress.tagomi.api.entities.ImmutableLocale;
import io.digiexpress.tagomi.api.entities.ImmutableResource;
import io.digiexpress.tagomi.api.entities.ImmutableService;
import io.digiexpress.tagomi.api.entities.ImmutableTemplate;
import io.digiexpress.tagomi.api.entities.TagomiContainer;
import io.digiexpress.tagomi.api.entities.TagomiContainer.IsTagomiObject;
import io.digiexpress.tagomi.api.entities.TagomiContainer.Locale;
import io.digiexpress.tagomi.api.entities.TagomiContainer.Resource;
import io.digiexpress.tagomi.api.entities.TagomiContainer.Service;
import io.digiexpress.tagomi.api.entities.TagomiContainer.Template;
import io.digiexpress.tagomi.spi.support.ConstraintException;
import io.digiexpress.tagomi.spi.support.StoreException;
import io.digiexpress.tagomi.spi.support.StoreException.StoreExceptionMsg;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import lombok.RequiredArgsConstructor;




@RequiredArgsConstructor
public class TagomiUpdateCommandsImpl implements TagomiUpdateCommands {

  private final TagomiStore client;
  private final TagomiImageStorage imageStorage;
  
  @Override
  public Uni<Locale> locale(LocaleMutator changes) {
    return client.stateQuery().getState().onItem()
        .transformToUni(state -> client.upsertBuilder().save(changeLocale(state, changes)));
  }
  
  private Locale changeLocale(TagomiContainer site, LocaleMutator changes) {
    final var start = site.getLocales().get(changes.getLocaleId());

    final var duplicate = site.getLocales().values().stream()
        .filter(p -> !p.getId().equals(changes.getLocaleId()))
        .filter(p -> p.getLocaleCode().equals(changes.getValue()))
        .findFirst();
    
    if(duplicate.isPresent()) {
      throw new ConstraintException(start, "Locale: '" + changes.getValue() + "' already exists!");
    }
    
    return ImmutableLocale.builder()
        .from(start)
        .localeCode(changes.getValue())
        .enabled(changes.getEnabled())
        .build();
  }

  @Override
  public Uni<Template> template(TemplateMutator changes) {
    return client.stateQuery().getState()
        .onItem().transformToUni(state -> {
          final var tuple = changeTemplate(state, changes);
          return client.upsertBuilder().saveAll(tuple.getItem2())
              .onItem().transform(e -> tuple.getItem1());
        });
  }
  
  private Tuple2<Template, List<IsTagomiObject>> changeTemplate(TagomiContainer site, TemplateMutator changes) {
    final Template start = site.getTemplates().get(changes.getTemplateId());
    final var targetLocale = Optional.ofNullable(changes.getLocale()).orElse(start.getLocaleId());
    
    
    final var locale = site.getLocales().values().stream()
        .filter(p -> 
          p.getLocaleCode().equals(targetLocale) || 
          p.getId().equals(targetLocale)
        ).findFirst();
    
    if(locale.isEmpty()) {
      throw new ConstraintException(start, "Template, locale: '" + changes.getLocale() + "' does not exist!");
    }
    
    final var duplicate = site.getTemplates().values().stream()
        .filter(p -> !p.getId().equals(changes.getTemplateId()))
        .filter(p -> p.getLocaleId().equals(changes.getLocale()))
        .findFirst();
    
    if(duplicate.isPresent()) {
      throw new ConstraintException(start, "Template for locale: '" + changes.getLocale() + "' already exists!");
    }
    
    List<IsTagomiObject> allChanges = new ArrayList<>();
    if(changes.getResourceIds() != null) {
      for(final var link : site.getResources().values()) {
        
        final var isArticleInLink = link.getTemplateIds().contains(changes.getTemplateId());
        final var isLinkInChanges = changes.getResourceIds().contains(link.getId());
        
        // link already defined for article
        if(isArticleInLink &&  isLinkInChanges) {
          continue;
        }
        
        // add link
        if(isLinkInChanges && !isArticleInLink) {
          final var newLink = ImmutableResource.builder()
              .from(link)
              .addTemplateIds(changes.getTemplateId())
              .build(); 
          allChanges.add(newLink);
        }
        
        // remove link
        if(isArticleInLink && !isLinkInChanges) {
          final var articles = new ArrayList<>(link.getTemplateIds());
          articles.remove(changes.getTemplateId());
          
          final var newLink = ImmutableResource.builder().from(link)
              .templateIds(articles)
              .build();
          allChanges.add(newLink);
        }
      }
    }
    
    final var updated = ImmutableTemplate.builder()
      .from(start)
      .content(changes.getContent())
      .build();
    
    allChanges.add(updated);
    
    return Tuple2.of(updated, allChanges);
  }

  @Override
  public Uni<Resource> resource(ResourceMutator changes) {
    return client.stateQuery().getState()
      .onItem().transformToUni(state -> {
        final var existingResource = state.getResources().get(changes.getResourceId());
        final var contentType = changes.getContentType() != null ? changes.getContentType() : existingResource.getContentType();

        if(changes.getUploadBody() == null || "text/*".equals(contentType)) {
          return client.upsertBuilder().save(changeLink(state, changes, null));
        }

        final byte[] bytesToStore = Base64.getDecoder().decode(changes.getUploadBody());

        return imageStorage.write(bytesToStore)
          .onItem().transform(image -> {
            if(image.getOperationStatus() == OperationStatus.OK) {
              return image.getObject();
            }
            throw new StoreException("FAILED_TO_STORE_IMAGE", null,
                StoreExceptionMsg.builder()
                .id("image-store-error")
                .value("Can't save image because of unknown error!")
                .args(image.getOperationLogs().stream().map(message -> message.getText()).collect(Collectors.toList()))
                .build());
          })
          .onItem().transformToUni(image -> client.upsertBuilder().save(changeLink(state, changes, image)));
      });
  }
  private Resource changeLink(TagomiContainer site, ResourceMutator changes, Image image) {
    final var start = site.getResources().get(changes.getResourceId());

    if(start == null) {
      throw new ConstraintException("Can't find resource: '" + changes.getResourceId() + "' to update!");
    }
    
    
    if(changes.getTemplateIds() != null ) {
      for(final var templateId : changes.getTemplateIds()) {
        if(!site.getTemplates().containsKey(templateId)) {
          throw new ConstraintException(start, "Template: '" + templateId + "' does not exist!");          
        }
      }
    }
    if(changes.getResourceName() != null) {
      final var duplicate = site.getResources().values().stream()
          .filter(p -> !p.getId().equals(changes.getResourceId()))
          .filter(p -> p.getResourceName().equals(changes.getResourceName()))
          .findFirst();
      
      if(duplicate.isPresent()) {
        throw new ConstraintException(start, "Resource with name: '" + changes.getResourceName() + "' already exists!");
      }
    }
    
    return ImmutableResource.builder()
        .from(start)
        .contentType(changes.getContentType() == null ? start.getContentType() : changes.getContentType())
        .resourceName(changes.getResourceName() == null ? start.getResourceName() : changes.getResourceName())
        .externalLocation(image == null ? start.getExternalLocation() : image.getId())
        .content(changes.getUploadBody() == null ? start.getContent() : changes.getUploadBody())
        .templateIds(changes.getTemplateIds() == null ? start.getTemplateIds() : new HashSet<>(changes.getTemplateIds()))
        .build();
  }

  @Override
  public Uni<Service> service(ServiceMutator changes) {
    return client.stateQuery().getState()
        .onItem().transformToUni(state -> client.upsertBuilder().save(changeService(state, changes)));
  }
  
  private Service changeService(TagomiContainer site, ServiceMutator changes) {
    final var start = site.getServices().get(changes.getServiceId());
    
    if(start == null) {
      throw new ConstraintException("Can't find service: '" + changes.getServiceId() + "' to update!");
    }
    
    if(changes.getLabels() != null ) {
      for(final var label : changes.getLabels()) {
        final var localeId = label.getLocale();
        if(!site.getLocales().containsKey(localeId)) {
          throw new ConstraintException(start, "Locale with id: '" + localeId + "' does not exist in: '" + String.join(",", site.getLocales().keySet()) + "'!");          
        }
      }
    }
    return ImmutableService.builder()
        .from(start)
        .serviceName(changes.getServiceName())
        .labels(changes.getLabels())
        .orchestratorName(changes.getOrchestratorName())
        .build();
  }

  @Override
  public Uni<List<Template>> templates(List<TemplateMutator> mutators) {
    // Get the page
    final List<String> ids = new ArrayList<>();
    final Map<String, TemplateMutator> changes = new HashMap<>();
    for(var m : mutators) {
      changes.put(m.getTemplateId(), m);
      ids.add(m.getTemplateId());
    }
    
    return client.stateQuery().getState().onItem().transformToUni(state -> {
      final var toBeSaved = mutators.stream()
        .map(mutator -> {
          
          final var start = state.getTemplates().get(mutator.getTemplateId());
          final var targetLocale = Optional.ofNullable(mutator.getLocale()).orElse(start.getLocaleId());
          final var locale = state.getLocales().values().stream()
              .filter(p -> 
                p.getLocaleCode().equals(targetLocale) || 
                p.getId().equals(targetLocale)
              ).findFirst();
          
          if(locale.isEmpty()) {
            throw new ConstraintException(start, "Template, locale: '" + mutator.getLocale() + "' does not exist!");
          }
          

          final var end = ImmutableTemplate.builder()
              .from(start)
              // only content change
              .localeId(mutator.getLocale())
              .content(mutator.getContent())
              .build();
          return end;
        }).collect(Collectors.toList());

      return client.upsertBuilder()
          .saveAll(new ArrayList<>(toBeSaved))
          .onItem().transform(e -> new ArrayList<>(toBeSaved));
    });
  }
}
