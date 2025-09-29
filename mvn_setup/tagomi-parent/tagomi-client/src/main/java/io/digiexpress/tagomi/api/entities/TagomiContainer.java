package io.digiexpress.tagomi.api.entities;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import jakarta.annotation.Nullable;



public interface TagomiContainer {
  String getTagName();
  
  @Nullable String getCommitId(); //only when available
  @Nullable LocalDateTime getCommitAt(); //only when available

  Map<String, Service> getReleases();
  Map<String, Article> getLocales();
  Map<String, Template> getPages();
  Map<String, Resource> getLinks();
  Map<String, Tag> getArticles();
  
  interface IsTagomiObject { String getId(); TagomiDocType getDocType(); }
  
  enum TagomiDocType {
    ARTICLE,  // main grouping for locale based templates
    TEMPLATE, // holds markup for pdf in specific locale
    RESOURCE, // some static asset like image to be embedded in printout
    SERVICE,  // final product, that links article for syntax and data for input
  }
  
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableService.class)
  @JsonDeserialize(as = ImmutableService.class)
  interface Service extends IsTagomiObject {
    String getId();
    String getServiceName(); // human readable name, what IS this PDF
    String getOrchestratorName(); // external name/id that will be called to resolve data

    @Override default public TagomiDocType getDocType() { return TagomiDocType.SERVICE; };

  }
  
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableArticle.class)
  @JsonDeserialize(as = ImmutableArticle.class)
  interface Article extends IsTagomiObject {
    String getId();
    String getArticleName();
    
    @Override default public TagomiDocType getDocType() { return TagomiDocType.ARTICLE; };
  }  
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableTemplate.class)
  @JsonDeserialize(as = ImmutableTemplate.class)
  interface Template extends IsTagomiObject {
    String getId();
    
    String getLocale();
    String getContent();

    String getArticleId();
    List<String> getResourceIds(); // id-s to ResourceLink
    
    @Override default public TagomiDocType getDocType() { return TagomiDocType.TEMPLATE; };
  }
  
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableResource.class)
  @JsonDeserialize(as = ImmutableResource.class)
  interface Resource extends IsTagomiObject {
    String getId();
    String getExternalLocation();
    String getResourceName();
    
    @Override default public TagomiDocType getDocType() { return TagomiDocType.RESOURCE; };

  }

  
  @Value.Immutable
  @JsonSerialize(as = ImmutableTag.class)
  @JsonDeserialize(as = ImmutableTag.class)
  interface Tag {
    String getId();
    String getExternalLocation();
    String getResourceName();
  }

}
