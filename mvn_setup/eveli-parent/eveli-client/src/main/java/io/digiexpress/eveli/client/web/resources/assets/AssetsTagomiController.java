package io.digiexpress.eveli.client.web.resources.assets;

/*-
 * #%L
 * eveli-client
 * %%
 * Copyright (C) 2015 - 2025 Copyright 2022 ReSys OÜ
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
import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.digiexpress.eveli.envir.api.EveliEnvirClient;
import io.digiexpress.tagomi.api.TagomiComposer;
import io.digiexpress.tagomi.api.commands.ImmutableCreateLocale;
import io.digiexpress.tagomi.api.commands.ImmutableCreateResource;
import io.digiexpress.tagomi.api.commands.ImmutableCreateService;
import io.digiexpress.tagomi.api.commands.ImmutableCreateTag;
import io.digiexpress.tagomi.api.commands.ImmutableCreateTemplate;
import io.digiexpress.tagomi.api.commands.ImmutableLocaleMutator;
import io.digiexpress.tagomi.api.commands.ImmutableResourceMutator;
import io.digiexpress.tagomi.api.commands.ImmutableResourceOnTemplate;
import io.digiexpress.tagomi.api.commands.ImmutableServiceMutator;
import io.digiexpress.tagomi.api.commands.ImmutableTemplateMutator;
import io.digiexpress.tagomi.api.entities.TagomiContainer;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@RestController
@RequestMapping("/worker/rest/api/assets/tagomi")
@Slf4j
@RequiredArgsConstructor
public class AssetsTagomiController {
  
  private final TagomiComposer client;
  private final EveliEnvirClient envir;
  
  @GetMapping("/")
  public Uni<TagomiContainer> root() {
    return getClient().onItem().transformToUni(composer -> composer.unwrap().stateQuery().getState());
  }
  @GetMapping("/sites")
  public Uni<TagomiContainer> getSites() {
    return getClient().onItem().transformToUni(composer -> composer.unwrap().stateQuery().getState());
  }
  
  @PostMapping("/resources") 
  public Uni<TagomiContainer.Resource> createResource(@RequestBody ImmutableCreateResource body) {
    return getClient().onItem().transformToUni(composer -> composer.create().resource(body))
        .onItem().invoke(() -> envir.invalidateCache());
  }
  @PutMapping("/resources") 
  public Uni<TagomiContainer.Resource> updateResource(@RequestBody ImmutableResourceMutator body) {
    return getClient().onItem().transformToUni(composer -> composer.update().resource(body))
        .onItem().invoke(() -> envir.invalidateCache());
  }
  @DeleteMapping("/resources/{id}") 
  public Uni<TagomiContainer.Resource> deleteResource(@PathVariable("id") String linkId, @RequestParam(name = "articleId", required = false) String articleId) {
    if(articleId == null || articleId.isEmpty()) {
      return getClient().onItem().transformToUni(composer -> composer.delete().resource(linkId))
          .onItem().invoke(() -> envir.invalidateCache());
    } 
    return getClient().onItem().transformToUni(composer -> composer.delete().resourceOnTemplate(ImmutableResourceOnTemplate.builder()
      .templateId(articleId)
      .resourceId(linkId)
      .build()))
      .onItem().invoke(() -> envir.invalidateCache());  

  }
  
  
  @PostMapping("/services") 
  public Uni<TagomiContainer.Service> createService(@RequestBody ImmutableCreateService body) {
    return getClient().onItem().transformToUni(composer -> composer.create().service(body))
        .onItem().invoke(() -> envir.invalidateCache());
  }
  @PutMapping("/services") 
  public Uni<TagomiContainer.Service> updateService(@RequestBody ImmutableServiceMutator body) {
    return getClient().onItem().transformToUni(composer -> composer.update().service(body))
        .onItem().invoke(() -> envir.invalidateCache());
  }
  @DeleteMapping("/services/{id}") 
  public Uni<TagomiContainer.Service> deleteService(@PathVariable("id") String serviceId) {
    return getClient().onItem().transformToUni(composer -> composer.delete().service(serviceId)).onItem().invoke(() -> envir.invalidateCache());
  }
  
  
  @PostMapping("/locales") 
  public Uni<TagomiContainer.Locale> createLocale(@RequestBody ImmutableCreateLocale body) {
    return getClient().onItem().transformToUni(composer -> composer.create().locale(body))
        .onItem().invoke(() -> envir.invalidateCache());
  }
  @PutMapping("/locales") 
  public Uni<TagomiContainer.Locale> updateLocale(@RequestBody ImmutableLocaleMutator body) {
    return getClient().onItem().transformToUni(composer -> composer.update().locale(body))
        .onItem().invoke(() -> envir.invalidateCache());
  }
  @DeleteMapping("/locales/{id}") 
  public Uni<TagomiContainer.Locale> deleteLocale(@PathVariable("id") String id) {
    return getClient().onItem().transformToUni(composer -> composer.delete().locale(id))
        .onItem().invoke(() -> envir.invalidateCache());
  }
  
  
  @PostMapping("/templates") 
  public Uni<TagomiContainer.Template> createTemplate(@RequestBody ImmutableCreateTemplate body) {
    return getClient().onItem().transformToUni(composer -> composer.create().template(body))
        .onItem().invoke(() -> envir.invalidateCache());
  }
  @PutMapping("/templates") 
  public Uni<List<TagomiContainer.Template>> updateTemplate(@RequestBody List<ImmutableTemplateMutator> body) {
    return getClient().onItem().transformToUni(composer -> composer.update().templates(new ArrayList<>(body)))
        .onItem().invoke(() -> envir.invalidateCache());
  }
  @DeleteMapping("/templates/{id}") 
  public Uni<TagomiContainer.Template> deleteTemplate(@PathVariable("id") String id) {
    return getClient().onItem().transformToUni(composer -> composer.delete().template(id))
        .onItem().invoke(() -> envir.invalidateCache());
  }
  
  
  @PostMapping("/tags") 
  public Uni<TagomiContainer.Tag> createTag(@RequestBody ImmutableCreateTag body) {
    return getClient().onItem().transformToUni(composer -> composer.create().tag(body))
        .onItem().invoke(() -> envir.invalidateCache());
  }
  @DeleteMapping("/tags/{id}") 
  public Uni<TagomiContainer.Tag> deleteTag(@PathVariable("id") String id) {
    return getClient().onItem().transformToUni(composer -> composer.delete().tag(id))
        .onItem().invoke(() -> envir.invalidateCache());
  }
  
  protected Uni<TagomiComposer> getClient() {
    return Uni.createFrom().item(this.client);
  }

}
