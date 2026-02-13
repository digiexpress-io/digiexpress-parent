package io.digiexpress.tagomi.spi.commands;

import java.util.ArrayList;
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

import java.util.Optional;
import java.util.stream.Collectors;

import io.digiexpress.tagomi.api.TagomiImageStorage;
import io.digiexpress.tagomi.api.TagomiImageStorage.OperationStatus;
import io.digiexpress.tagomi.api.TagomiStore;
import io.digiexpress.tagomi.api.commands.TagomiCreateCommands;
import io.digiexpress.tagomi.api.entities.ImmutableLocale;
import io.digiexpress.tagomi.api.entities.ImmutableLocaleAndLabel;
import io.digiexpress.tagomi.api.entities.ImmutableResource;
import io.digiexpress.tagomi.api.entities.ImmutableService;
import io.digiexpress.tagomi.api.entities.ImmutableTag;
import io.digiexpress.tagomi.api.entities.ImmutableTemplate;
import io.digiexpress.tagomi.api.entities.TagomiContainer;
import io.digiexpress.tagomi.spi.support.ConstraintException;
import io.digiexpress.tagomi.spi.support.StoreException;
import io.digiexpress.tagomi.spi.support.StoreException.StoreExceptionMsg;
import io.resys.thena.support.OidUtils;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class TagomiCreateCommandsImpl implements TagomiCreateCommands {
  private final TagomiStore store;
  private final TagomiImageStorage imageStorage;
  
  @Override
  public Uni<TagomiContainer.Tag> tag(CreateTag init) {
    
    
    final Uni<TagomiContainer.Tag> command = store.stateQuery().getState()
        .onItem().transform(state -> {
          final var gid = OidUtils.gen();
          final var release = ImmutableTag.builder()
              .id(gid)
              .name(init.getTagName())
              .commitId(Optional.ofNullable(state.getCommitId()).orElse(state.getCommitId()))
              .note(init.getNote())
              .build();
          return assertUniqueId(release, state);
        })
        .onItem().transformToUni(request -> store.upsertBuilder().create(request));
    
    
    if(init.getCommitId() == null) {
      return command;
    } 
    
    // validate commit id
    return store.stateQuery().getStateByCommitId(init.getCommitId())
        .onItem().transformToUni((ignore) -> command);
  }
  @Override
  public Uni<TagomiContainer.Locale> locale(CreateLocale init) {
    return store.stateQuery().getState()
        .onItem().transform(state -> {
          final var gid = OidUtils.gen();
          final var locale = ImmutableLocale.builder()
              .id(gid)
              .localeCode(init.getLocaleCode())
              .enabled(true)
              .getDefault(Boolean.FALSE)
              .build();

          final var duplicate = state.getLocales().values().stream()
              .filter(p -> p.getLocaleCode().equals(init.getLocaleCode()))
              .findFirst();

          if(duplicate.isPresent()) {
            throw new ConstraintException(locale, "Locale: '" + init.getLocaleCode() + "' already exists!");
          }
          return assertUniqueId(locale, state);
        })
        .onItem().transformToUni(request -> store.upsertBuilder().create(request));
  }
  @Override
  public Uni<TagomiContainer.Template> template(CreateTemplate init) {
    return store.stateQuery().getState().onItem().transform(state -> {
      final var gid = OidUtils.gen();
      final var localeRef = init.getLocale();
      final var locale = resolveLocale(localeRef, state);
      
      final var articleRef = init.getServiceId();
      final var article = state.getServices().containsKey(articleRef) ? 
          Optional.of(state.getServices().get(articleRef)) : 
          state.getServices().values().stream().filter(l -> l.getServiceName().equalsIgnoreCase(articleRef)).findFirst();

      final var templateIds = new ArrayList<String>();
      if(init.getTemplateIds() != null) {
        for(final var depTemplateId : init.getTemplateIds()) {
          if(!state.getTemplates().containsKey(depTemplateId)) {
            throw new ConstraintException(
                "Template with id: '" + depTemplateId + "' does not exist in: '" + String.join(",", state.getTemplates().keySet()) + "'!");
          }
          templateIds.add(depTemplateId);
        }
      }

      final var page = ImmutableTemplate.builder()
          .id(gid)
          .serviceId(article.map(e -> e.getId()).orElse(articleRef))
          .content(Optional.ofNullable(init.getContent()).orElse(""))
          .localeId(locale.map(e -> e.getId()).orElse(localeRef))
          .templateIds(templateIds)
          .build();

      if(locale.isEmpty()) {
        throw new ConstraintException(page, "Locale with id: '" + localeRef + "' does not exist in: '" + String.join(",", state.getLocales().keySet()) + "'!");          
      }
      if(article.isEmpty()) {
        throw new ConstraintException(page, "Article with id: '" + articleRef + "' does not exist in: '" + String.join(",", state.getServices().keySet()) + "'!");          
      }

      final var duplicate = state.getTemplates().values().stream()
          .filter(p -> p.getServiceId().equals(init.getServiceId()))
          .filter(p -> p.getLocaleId().equals(init.getLocale()))
          .findFirst();
      
      if(duplicate.isPresent()) {
        throw new ConstraintException(page, "Page locale with id: '" + locale.get().getId() + "' already exists!");
      }
      return assertUniqueId(page, state);
    })
    .onItem().transformToUni(request -> store.upsertBuilder().create(request));
  }
  @Override
  public Uni<TagomiContainer.Resource> resource(CreateResource init) {
    final var gid = OidUtils.gen();

    if (init.getUploadBody() == null) {
      throw new ConstraintException("uploadBody must be provided for resource: " + init.getResourceName());
    }

    if ("text/*".equals(init.getContentType())) {
      return createScriptResource(gid, init);
    } 
    
    if ("image/*".equals(init.getContentType())) {
      return createImageResource(gid, init);
    }
    
    throw new ConstraintException("Unsupported content type: " + init.getContentType());
  }
  
  private Uni<TagomiContainer.Resource> createScriptResource(String gid, CreateResource init) {
    return store.stateQuery().getState()
      .onItem().transform(state -> {
        final var templateIds = validateAndCollectTemplateIds(init.getTemplateIds(), state);

        final var resource = ImmutableResource.builder()
          .id(gid)
          .externalLocation("")
          .resourceName(init.getResourceName())
          .contentType(init.getContentType())
          .content(init.getUploadBody())
          .templateIds(templateIds)
          .build();
        return assertUniqueId(resource, state);
      })
      .onItem().transformToUni(request -> store.upsertBuilder().create(request));
  }
  
  private Uni<TagomiContainer.Resource> createImageResource(String gid, CreateResource init) {
    final byte[] imageBytes = Base64.getDecoder().decode(init.getUploadBody());

    return imageStorage.write(imageBytes)
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
      .onItem().transformToUni(image -> store.stateQuery().getState()
        .onItem().transform(state -> {
          final var templateIds = validateAndCollectTemplateIds(init.getTemplateIds(), state);
          
          final var resource = ImmutableResource.builder()
              .id(gid)
              .externalLocation(image.getId())
              .resourceName(init.getResourceName())
              .contentType(init.getContentType())
              .content(init.getUploadBody())
              .templateIds(templateIds)
              .build();
          return assertUniqueId(resource, state);
        }))
      .onItem().transformToUni(request -> store.upsertBuilder().create(request));
  }
  
  private ArrayList<String> validateAndCollectTemplateIds(java.util.List<String> templateIds, TagomiContainer state) {
    final var validatedIds = new ArrayList<String>();
    for(final var templateId : templateIds) {
      final var template = state.getTemplates().get(templateId);
      if(template == null) {
        throw new ConstraintException(
            "Template with id: '" + templateId + "' does not exist in: '" + String.join(",", state.getTemplates().keySet()) + "'!");
      }
      validatedIds.add(template.getId());
    }
    return validatedIds;
  }

  @Override
  public Uni<TagomiContainer.Service> service(CreateService init) {
    return store.stateQuery().getState()
        .onItem().transform(state -> {
          final var gid = OidUtils.gen();
          final var workflow = ImmutableService.builder()
              .id(gid)
              .serviceName(init.getServiceName())
              .orchestratorName(init.getOrchestratorName());

          final var duplicate = state.getServices().values().stream()
              .filter(p -> p.getServiceName().equals(init.getServiceName()))
              .findFirst();
          
          if(duplicate.isPresent()) {
            throw new ConstraintException(workflow.build(), "Service: '" + init.getServiceName() + "' already exists!");
          }
                    
          for(final var label : init.getLabels()) {        

            final var localeRef = label.getLocale();
            final var locale = resolveLocale(localeRef, state);
                
            workflow.addLabels(ImmutableLocaleAndLabel.builder()
                .locale(locale.map(e -> e.getId()).orElse(localeRef))
                .labelValue(label.getLabelValue())
                .build());

            if(locale.isEmpty()) {
              throw new ConstraintException(
                  workflow.build(), 
                  "Locale with id: '" + label.getLocale() + "' does not exist in: '" + String.join(",", state.getLocales().keySet()) + "'!");          
            }
          }
          return assertUniqueId(workflow.build(), state);
        })
        .onItem().transformToUni(request -> store.upsertBuilder().create(request));
  }

  
  public static Optional<TagomiContainer.Locale> resolveLocale(String idOrValue, TagomiContainer state) {
    final var localeRef = idOrValue;
    final var locale = state.getLocales().containsKey(localeRef) ? 
        Optional.of(state.getLocales().get(localeRef)) : 
        state.getLocales().values().stream().filter(l -> l.getLocaleCode().equalsIgnoreCase(localeRef)).findFirst();
     return locale;
  }
  
  private static <T extends TagomiContainer.IsTagomiObject> T assertUniqueId(T entity, TagomiContainer state) {
    if( state.getTags().containsKey(entity.getId()) ||
        state.getLocales().containsKey(entity.getId()) ||
        state.getTemplates().containsKey(entity.getId()) ||
        state.getResources().containsKey(entity.getId()) ||
        state.getServices().containsKey(entity.getId()) ||
        state.getTemplates().containsKey(entity.getId())) {
      
      throw new ConstraintException(entity, "Entity with id: '" + entity.getId() + "' already exist!");  
    }
    
    return entity;
  }
}
