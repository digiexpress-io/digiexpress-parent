package io.thestencil.client.api;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.thestencil.client.api.MigrationBuilder.Sites;
import io.thestencil.client.api.StencilComposer.SiteState;

public interface StencilClient {
  MarkdownBuilder markdown();
  SitesBuilder sites();
  
  interface SitesBuilder {
    SitesBuilder imagePath(String imagePath);
    SitesBuilder created(long created);
    SitesBuilder source(Markdowns markdowns);
    Sites build();
  }
  
  interface MarkdownBuilder {
    MarkdownBuilder json(String jsonOfSiteState, boolean dev);
    MarkdownBuilder json(SiteState jsonOfSiteState, boolean dev);
    MarkdownBuilder md(String path, byte[] value);
    Markdowns build();
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableEntity.class)
  @JsonDeserialize(as = ImmutableEntity.class)
  interface Entity<T extends EntityBody> extends Serializable {
    String getId();
    EntityType getType();
    T getBody();
  }

  enum EntityType {
    LOCALE, LINK, ARTICLE, WORKFLOW, RELEASE, PAGE, TEMPLATE
  }

  interface EntityBody extends Serializable {
  }
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableArticle.class)
  @JsonDeserialize(as = ImmutableArticle.class)
  interface Article extends EntityBody {
    @Nullable
    String getParentId();
    @Nullable
    @JsonInclude(JsonInclude.Include.NON_NULL)
    Boolean getDevMode();
    String getName();
    Integer getOrder();
  }
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableTemplate.class)
  @JsonDeserialize(as = ImmutableTemplate.class)
  interface Template extends EntityBody {
    String getName();
    String getDescription();
    String getContent();
    String getType();
  }
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableLocale.class)
  @JsonDeserialize(as = ImmutableLocale.class)
  interface Locale extends EntityBody {
    String getValue();
    Boolean getEnabled();
  }
  
  @Value.Immutable
  @JsonSerialize(as = ImmutablePage.class)
  @JsonDeserialize(as = ImmutablePage.class)
  interface Page extends EntityBody {
    String getArticle();
    @Nullable
    @JsonInclude(JsonInclude.Include.NON_NULL)
    Boolean getDevMode();
    String getLocale();
    String getContent();
  }
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableWorkflow.class)
  @JsonDeserialize(as = ImmutableWorkflow.class)
  interface Workflow extends EntityBody {
    String getValue(); // pointer to actual workflow
    @Nullable
    @JsonInclude(JsonInclude.Include.NON_NULL) 
    Boolean getDevMode();
    List<String> getArticles();
    List<LocaleLabel> getLabels();
  }
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableLink.class)
  @JsonDeserialize(as = ImmutableLink.class)
  interface Link extends EntityBody {
    String getValue();
    @Nullable
    @JsonInclude(JsonInclude.Include.NON_NULL)
    Boolean getDevMode();
    String getContentType();
    List<String> getArticles();
    List<LocaleLabel> getLabels();
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableLocaleLabel.class)
  @JsonDeserialize(as = ImmutableLocaleLabel.class)
  interface LocaleLabel extends Serializable {
    String getLocale();     // locale id
    String getLabelValue(); // translation in locale
  }
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableRelease.class)
  @JsonDeserialize(as = ImmutableRelease.class)
  interface Release extends EntityBody {
    String getParentCommit();
    String getName();
    String getNote();
    LocalDateTime getCreated();
    
    List<LocaleReleaseItem> getLocales();
    List<ArticleReleaseItem> getArticles();
    List<LinkReleaseItem> getLinks();
    List<WorkflowReleaseItem> getWorkflows();
    List<PageReleaseItem> getPages();
    List<TemplateReleaseItem> getTemplates();
  }
  
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableLinkReleaseItem.class)
  @JsonDeserialize(as = ImmutableLinkReleaseItem.class)
  interface LinkReleaseItem extends ReleaseItem {
    String getValue();
    String getContentType();
    List<String> getArticles();
    List<LocaleLabel> getLabels();    
  }
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableWorkflowReleaseItem.class)
  @JsonDeserialize(as = ImmutableWorkflowReleaseItem.class)
  interface WorkflowReleaseItem extends ReleaseItem {
    String getValue(); // pointer to actual workflow
    List<String> getArticles();
    List<LocaleLabel> getLabels();
  }
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableLocaleReleaseItem.class)
  @JsonDeserialize(as = ImmutableLocaleReleaseItem.class)
  interface LocaleReleaseItem extends ReleaseItem {
    String getValue(); // language code
  }
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableArticleReleaseItem.class)
  @JsonDeserialize(as = ImmutableArticleReleaseItem.class)
  interface ArticleReleaseItem extends ReleaseItem {
    String getName();
    @Nullable
    String getParentId();
  }
  
  @Value.Immutable
  @JsonSerialize(as = ImmutablePageReleaseItem.class)
  @JsonDeserialize(as = ImmutablePageReleaseItem.class)
  interface PageReleaseItem extends ReleaseItem {
    String getLocale();
    String getH1();
  }
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableTemplateReleaseItem.class)
  @JsonDeserialize(as = ImmutableTemplateReleaseItem.class)
  interface TemplateReleaseItem extends ReleaseItem {
    String getName();
    String getContent();
    String getType();
  }
  
  interface ReleaseItem extends Serializable {
    String getId();
    String getHash();
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableVersionInfo.class)
  @JsonDeserialize(as = ImmutableVersionInfo.class)
  interface VersionInfo {
    String getVersion();
    String getDate();
  }
}
