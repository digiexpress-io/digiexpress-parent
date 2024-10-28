package io.digiexpress.eveli.client.web.resources;

/*-
 * #%L
 * eveli-client
 * %%
 * Copyright (C) 2015 - 2024 Copyright 2022 ReSys OÜ
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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.smallrye.mutiny.Uni;
import io.thestencil.client.api.ImmutableArticleMutator;
import io.thestencil.client.api.ImmutableCreateArticle;
import io.thestencil.client.api.ImmutableCreateLink;
import io.thestencil.client.api.ImmutableCreateLocale;
import io.thestencil.client.api.ImmutableCreatePage;
import io.thestencil.client.api.ImmutableCreateRelease;
import io.thestencil.client.api.ImmutableCreateTemplate;
import io.thestencil.client.api.ImmutableCreateWorkflow;
import io.thestencil.client.api.ImmutableLinkArticlePage;
import io.thestencil.client.api.ImmutableLinkMutator;
import io.thestencil.client.api.ImmutableLocaleMutator;
import io.thestencil.client.api.ImmutablePageMutator;
import io.thestencil.client.api.ImmutableSiteState;
import io.thestencil.client.api.ImmutableTemplateMutator;
import io.thestencil.client.api.ImmutableWorkflowArticlePage;
import io.thestencil.client.api.ImmutableWorkflowMutator;
import io.thestencil.client.api.MigrationBuilder.Sites;
import io.thestencil.client.api.StencilClient.Article;
import io.thestencil.client.api.StencilClient.Entity;
import io.thestencil.client.api.StencilClient.Link;
import io.thestencil.client.api.StencilClient.Locale;
import io.thestencil.client.api.StencilClient.Page;
import io.thestencil.client.api.StencilClient.Release;
import io.thestencil.client.api.StencilClient.Template;
import io.thestencil.client.api.StencilClient.Workflow;
import io.thestencil.client.api.StencilComposer;
import io.thestencil.client.api.StencilComposer.SiteState;
import io.thestencil.client.spi.beans.SitesBean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/stencil")
@Slf4j
@RequiredArgsConstructor
public class AssetsStencilController {
  
  private final StencilComposer client;
  private final ObjectMapper objectMapper;
  
  @PostMapping("articles")
  public Uni<Entity<Article>> createArticle(@RequestBody ImmutableCreateArticle body) {
    return getClient().onItem().transformToUni(composer -> composer.create().article(body));
  }
  @PutMapping("articles") 
  public Uni<Entity<Article>> updateArticle(@RequestBody ImmutableArticleMutator body) {
    return getClient().onItem().transformToUni(composer -> composer.update().article(body));
  }
  @DeleteMapping("articles/{id}") 
  public Uni<Entity<Article>> deleteArticle(String id) {
    return getClient().onItem().transformToUni(composer -> composer.delete().article(id));
  }
  @PostMapping("migrations") 
  public Uni<SiteState> createMigration(@RequestBody String json) {
    byte[] body = json.getBytes(StandardCharsets.UTF_8);
  
    final var sites = parseSites(body);
    if(sites != null) {
      return getClient().onItem().transformToUni(composer -> composer.migration().importData(sites));
    }
    
    final var release = parseSiteState(body);
    if(release != null) {
      return getClient().onItem().transformToUni(composer -> composer.migration().importData(release));
    }
    return Uni.createFrom().nullItem();
  }
  @PostMapping("sites") 
  public Uni<SiteState> createSites() {
    return getClient().onItem().transformToUni(composer -> composer.create().repo());
  }
  @GetMapping("sites")
  public Uni<SiteState> getSites() {
    return getClient().onItem().transformToUni(composer -> composer.query().head());
  }
  @PostMapping("links") 
  public Uni<Entity<Link>> createLink(@RequestBody ImmutableCreateLink body) {
    return getClient().onItem().transformToUni(composer -> composer.create().link(body));
  }
  @PutMapping("links") 
  public Uni<Entity<Link>> updateLink(@RequestBody ImmutableLinkMutator body) {
    return getClient().onItem().transformToUni(composer -> composer.update().link(body));
  }
  @DeleteMapping("links/{id}") 
  public Uni<Entity<Link>> deleteLink(String linkId, String articleId) {
    if(articleId == null || articleId.isEmpty()) {
      return getClient().onItem().transformToUni(composer -> composer.delete().link(linkId));
    } 
    return getClient().onItem().transformToUni(composer -> composer.delete().linkArticlePage(ImmutableLinkArticlePage.builder()
      .articleId(articleId)
      .linkId(linkId)
      .build()));  

  }
  @PostMapping("workflows") 
  public Uni<Entity<Workflow>> createWorkflow(@RequestBody ImmutableCreateWorkflow body) {
    return getClient().onItem().transformToUni(composer -> composer.create().workflow(body));
  }
  @PutMapping("workflows") 
  public Uni<Entity<Workflow>> updateWorkflow(@RequestBody ImmutableWorkflowMutator body) {
    return getClient().onItem().transformToUni(composer -> composer.update().workflow(body));
  }
  @DeleteMapping("workflows/{id}") 
  public Uni<Entity<Workflow>> deleteWorkflow(String linkId, String articleId) {
    if(articleId == null || articleId.isEmpty()) {
      return getClient().onItem().transformToUni(composer -> composer.delete().workflow(linkId));
    } 
    return getClient().onItem().transformToUni(composer -> composer.delete().workflowArticlePage(ImmutableWorkflowArticlePage.builder()
      .articleId(articleId)
      .workflowId(linkId)
      .build()));  
  }
  @PostMapping("locales") 
  public Uni<Entity<Locale>> createLocale(@RequestBody ImmutableCreateLocale body) {
    return getClient().onItem().transformToUni(composer -> composer.create().locale(body));
  }
  @PutMapping("locales") 
  public Uni<Entity<Locale>> updateLocale(@RequestBody ImmutableLocaleMutator body) {
    return getClient().onItem().transformToUni(composer -> composer.update().locale(body));
  }
  @DeleteMapping("locales/{id}") 
  public Uni<Entity<Locale>> deleteLocale(String id) {
    return getClient().onItem().transformToUni(composer -> composer.delete().locale(id));
  }
  @PostMapping("pages") 
  public Uni<Entity<Page>> createPage(@RequestBody ImmutableCreatePage body) {
    return getClient().onItem().transformToUni(composer -> composer.create().page(body));
  }
  @PutMapping("pages") 
  public Uni<List<Entity<Page>>> updatePage(@RequestBody List<ImmutablePageMutator> body) {
    return getClient().onItem().transformToUni(composer -> composer.update().pages(new ArrayList<>(body)));
  }
  @DeleteMapping("pages/{id}") 
  public Uni<Entity<Page>> deletePage(String id) {
    return getClient().onItem().transformToUni(composer -> composer.delete().page(id));
  }
  @PostMapping("templates") 
  public Uni<Entity<Template>> createTemplate(@RequestBody ImmutableCreateTemplate body) {
    return getClient().onItem().transformToUni(composer -> composer.create().template(body));
  }
  @PutMapping("templates") 
  public Uni<Entity<Template>> updateTemplate(@RequestBody ImmutableTemplateMutator body) {
    return getClient().onItem().transformToUni(composer -> composer.update().template(body));
  }
  @DeleteMapping("templates/{id}") 
  public Uni<Entity<Template>> deleteTemplate(String id) {
    return getClient().onItem().transformToUni(composer -> composer.delete().template(id));
  }
  @PostMapping("releases") 
  public Uni<Entity<Release>> createRelease(@RequestBody ImmutableCreateRelease body) {
    return getClient().onItem().transformToUni(composer -> composer.create().release(body));
  }
  @GetMapping("releases/{id}") 
  public Uni<SiteState> getRelease(String id) {
    return getClient().onItem().transformToUni(composer -> composer.query().release(id));
  }
  @DeleteMapping("releases/{id}") 
  public Uni<Entity<Release>> deleteRelease(String id) {
    return getClient().onItem().transformToUni(composer -> composer.delete().release(id));
  }

  protected Uni<StencilComposer> getClient() {
    return Uni.createFrom().item(this.client);
  }
  
  private SiteState parseSiteState(byte[] body) {
    try {
      return objectMapper.readValue(body, ImmutableSiteState.class);
    } catch(IOException ex1) {
      log.error("failed to parse site for migration, " + ex1.getMessage(), ex1);
    }
    return null;
  }
  
  private Sites parseSites(byte[] body) {    
    Sites site = null;
    try {
      site = objectMapper.readValue(body, SitesBean.class);
      
      if(site == null || site.getSites() == null  || site.getSites().isEmpty()) {
        final var md = client
            .markdown().json(new String(body, StandardCharsets.UTF_8), true)
            .build();

        site = client
            .sites().imagePath("/images").created(1l)
            .source(md)
            .build();
      }

      return site;
    } catch(IOException ex1) {
      log.error("failed to parse site for migration, " + ex1.getMessage(), ex1);
    }
    return null;
  }
  
}
