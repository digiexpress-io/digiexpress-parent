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
  
  protected final StencilComposer client;
  private final ObjectMapper objectMapper;
  
  @Override
  public Uni<Entity<Article>> createArticle(ImmutableCreateArticle body) {
    return getClient().onItem().transformToUni(composer -> composer.create().article(body));
  }
  @Override
  public Uni<Entity<Article>> updateArticle(ImmutableArticleMutator body) {
    return getClient().onItem().transformToUni(composer -> composer.update().article(body));
  }
  @Override
  public Uni<Entity<Article>> deleteArticle(String id) {
    return getClient().onItem().transformToUni(composer -> composer.delete().article(id));
  }
  @Override
  public Uni<SiteState> createMigration(String json) {
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
  @Override
  public Uni<SiteState> createSites() {
    return getClient().onItem().transformToUni(composer -> composer.create().repo());
  }
  @Override
  public Uni<SiteState> getSites() {
    return getClient().onItem().transformToUni(composer -> composer.query().head());
  }
  @Override
  public Uni<Entity<Link>> createLink(ImmutableCreateLink body) {
    return getClient().onItem().transformToUni(composer -> composer.create().link(body));
  }
  @Override
  public Uni<Entity<Link>> updateLink(ImmutableLinkMutator body) {
    return getClient().onItem().transformToUni(composer -> composer.update().link(body));
  }
  @Override
  public Uni<Entity<Link>> deleteLink(String linkId, String articleId) {
    if(articleId == null || articleId.isEmpty()) {
      return getClient().onItem().transformToUni(composer -> composer.delete().link(linkId));
    } 
    return getClient().onItem().transformToUni(composer -> composer.delete().linkArticlePage(ImmutableLinkArticlePage.builder()
      .articleId(articleId)
      .linkId(linkId)
      .build()));  

  }
  @Override
  public Uni<Entity<Workflow>> createWorkflow(ImmutableCreateWorkflow body) {
    return getClient().onItem().transformToUni(composer -> composer.create().workflow(body));
  }
  @Override
  public Uni<Entity<Workflow>> updateWorkflow(ImmutableWorkflowMutator body) {
    return getClient().onItem().transformToUni(composer -> composer.update().workflow(body));
  }
  @Override
  public Uni<Entity<Workflow>> deleteWorkflow(String linkId, String articleId) {
    if(articleId == null || articleId.isEmpty()) {
      return getClient().onItem().transformToUni(composer -> composer.delete().workflow(linkId));
    } 
    return getClient().onItem().transformToUni(composer -> composer.delete().workflowArticlePage(ImmutableWorkflowArticlePage.builder()
      .articleId(articleId)
      .workflowId(linkId)
      .build()));  
  }
  @Override
  public Uni<Entity<Locale>> createLocale(ImmutableCreateLocale body) {
    return getClient().onItem().transformToUni(composer -> composer.create().locale(body));
  }
  @Override
  public Uni<Entity<Locale>> updateLocale(ImmutableLocaleMutator body) {
    return getClient().onItem().transformToUni(composer -> composer.update().locale(body));
  }
  @Override
  public Uni<Entity<Locale>> deleteLocale(String id) {
    return getClient().onItem().transformToUni(composer -> composer.delete().locale(id));
  }
  @Override
  public Uni<Entity<Page>> createPage(ImmutableCreatePage body) {
    return getClient().onItem().transformToUni(composer -> composer.create().page(body));
  }
  @Override
  public Uni<List<Entity<Page>>> updatePage(List<ImmutablePageMutator> body) {
    return getClient().onItem().transformToUni(composer -> composer.update().pages(new ArrayList<>(body)));
  }
  @Override
  public Uni<Entity<Page>> deletePage(String id) {
    return getClient().onItem().transformToUni(composer -> composer.delete().page(id));
  }
  @Override
  public Uni<Entity<Template>> createTemplate(ImmutableCreateTemplate body) {
    return getClient().onItem().transformToUni(composer -> composer.create().template(body));
  }
  @Override
  public Uni<Entity<Template>> updateTemplate(ImmutableTemplateMutator body) {
    return getClient().onItem().transformToUni(composer -> composer.update().template(body));
  }
  @Override
  public Uni<Entity<Template>> deleteTemplate(String id) {
    return getClient().onItem().transformToUni(composer -> composer.delete().template(id));
  }
  @Override
  public Uni<Entity<Release>> createRelease(ImmutableCreateRelease body) {
    return getClient().onItem().transformToUni(composer -> composer.create().release(body));
  }
  @Override
  public Uni<SiteState> getRelease(String id) {
    return getClient().onItem().transformToUni(composer -> composer.query().release(id));
  }
  @Override
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
