package io.thestencil.client.rest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

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

@Slf4j
@RequiredArgsConstructor
public class StencilRestApiTemplate implements StencilRestApi {
  
  private final StencilComposer client;
  private final ObjectMapper objectMapper;
  
  @Override
  public Uni<Entity<Article>> createArticle(String projectId, ImmutableCreateArticle body) {
    return client.withRepo(projectId).create().article(body);
  }
  @Override
  public Uni<Entity<Article>> updateArticle(String projectId, ImmutableArticleMutator body) {
    return client.withRepo(projectId).update().article(body);
  }
  @Override
  public Uni<Entity<Article>> deleteArticle(String projectId, String id) {
    return client.withRepo(projectId).delete().article(id);
  }
  @Override
  public Uni<SiteState> createMigration(String projectId, String json) {
    byte[] body = json.getBytes(StandardCharsets.UTF_8);
  
    final var sites = parseSites(body);
    if(sites != null) {
      return client.withRepo(projectId).migration().importData(sites);
    }
    
    final var release = parseSiteState(body);
    if(release != null) {
      return client.withRepo(projectId).migration().importData(release);
    }
    return Uni.createFrom().nullItem();
  }
  @Override
  public Uni<SiteState> createSites(String projectId) {
    return client.withRepo(projectId).create().repo();
  }
  @Override
  public Uni<SiteState> getSites(String projectId) {
    return client.withRepo(projectId).query().head();
  }
  @Override
  public Uni<Entity<Link>> createLink(String projectId, ImmutableCreateLink body) {
    return client.withRepo(projectId).create().link(body);
  }
  @Override
  public Uni<Entity<Link>> updateLink(String projectId, ImmutableLinkMutator body) {
    return client.withRepo(projectId).update().link(body);
  }
  @Override
  public Uni<Entity<Link>> deleteLink(String projectId, String linkId, String articleId) {
    if(articleId == null || articleId.isEmpty()) {
      return client.withRepo(projectId).delete().link(linkId);
    } 
    return client.withRepo(projectId).delete().linkArticlePage(ImmutableLinkArticlePage.builder()
      .articleId(articleId)
      .linkId(linkId)
      .build());  

  }
  @Override
  public Uni<Entity<Workflow>> createWorkflow(String projectId, ImmutableCreateWorkflow body) {
    return client.withRepo(projectId).create().workflow(body);
  }
  @Override
  public Uni<Entity<Workflow>> updateWorkflow(String projectId, ImmutableWorkflowMutator body) {
    return client.withRepo(projectId).update().workflow(body);
  }
  @Override
  public Uni<Entity<Workflow>> deleteWorkflow(String projectId, String linkId, String articleId) {
    if(articleId == null || articleId.isEmpty()) {
      return client.withRepo(projectId).delete().workflow(linkId);
    } 
    return client.withRepo(projectId).delete().workflowArticlePage(ImmutableWorkflowArticlePage.builder()
      .articleId(articleId)
      .workflowId(linkId)
      .build());  
  }
  @Override
  public Uni<Entity<Locale>> createLocale(String projectId, ImmutableCreateLocale body) {
    return client.withRepo(projectId).create().locale(body);
  }
  @Override
  public Uni<Entity<Locale>> updateLocale(String projectId, ImmutableLocaleMutator body) {
    return client.withRepo(projectId).update().locale(body);
  }
  @Override
  public Uni<Entity<Locale>> deleteLocale(String projectId, String id) {
    return client.withRepo(projectId).delete().locale(id);
  }
  @Override
  public Uni<Entity<Page>> createPage(String projectId, ImmutableCreatePage body) {
    return client.withRepo(projectId).create().page(body);
  }
  @Override
  public Uni<List<Entity<Page>>> updatePage(String projectId, List<ImmutablePageMutator> body) {
    return client.withRepo(projectId).update().pages(new ArrayList<>(body));
  }
  @Override
  public Uni<Entity<Page>> deletePage(String projectId, String id) {
    return client.withRepo(projectId).delete().page(id);
  }
  @Override
  public Uni<Entity<Template>> createTemplate(String projectId, ImmutableCreateTemplate body) {
    return client.withRepo(projectId).create().template(body);
  }
  @Override
  public Uni<Entity<Template>> updateTemplate(String projectId, ImmutableTemplateMutator body) {
    return client.withRepo(projectId).update().template(body);
  }
  @Override
  public Uni<Entity<Template>> deleteTemplate(String projectId, String id) {
    return client.withRepo(projectId).delete().template(id);
  }
  @Override
  public Uni<Entity<Release>> createRelease(String projectId, ImmutableCreateRelease body) {
    return client.withRepo(projectId).create().release(body);
  }
  @Override
  public Uni<SiteState> getRelease(String projectId, String id) {
    return client.withRepo(projectId).query().release(id);
  }
  @Override
  public Uni<Entity<Release>> deleteRelease(String projectId, String id) {
    return client.withRepo(projectId).delete().release(id);
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
