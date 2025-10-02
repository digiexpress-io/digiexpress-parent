package io.digiexpress.tagomi.api.entities;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import jakarta.annotation.Nullable;


@Value.Immutable
@JsonSerialize(as = ImmutableTagomiContainer.class)
@JsonDeserialize(as = ImmutableTagomiContainer.class)
public interface TagomiContainer {
  String getTagName();
  
  @Nullable String getCommitId(); //only when available
  @Nullable LocalDateTime getCommitAt(); //only when available

  Map<String, Tag> getTags();
  Map<String, Locale> getLocales();
  Map<String, Template> getTemplates();
  Map<String, Resource> getResources();
  Map<String, Service> getServices();
  
  
  
  @JsonTypeInfo(
      use = JsonTypeInfo.Id.NAME,
      include = JsonTypeInfo.As.PROPERTY,
      property = "docType")
  @JsonSubTypes({
    @Type(value = ImmutableTemplate.class, name = "TEMPLATE"), 
    @Type(value = ImmutableResource.class, name = "RESOURCE"), 
    @Type(value = ImmutableService.class, name = "SERVICE"), 
    @Type(value = ImmutableLocale.class, name = "LOCALE"), 
    @Type(value = ImmutableTag.class, name = "TAG")
  })
  interface IsTagomiObject { 
    String getId(); 
    TagomiDocType getDocType(); 
  }
  
  enum TagomiDocType {
    TEMPLATE, // holds markup for pdf in specific locale
    RESOURCE, // some static asset like image to be embedded in printout
    SERVICE,  // final product, that links article for syntax and data for input, main grouping for locale based templates
    LOCALE,   // locale code and enabled/disabled flag
    TAG       // very small meta object for holding some commit data
  }
  
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableService.class)
  @JsonDeserialize(as = ImmutableService.class)
  interface Service extends IsTagomiObject {
    String getId();
    String getServiceName(); // human readable name, what IS this PDF
    String getOrchestratorName(); // external name/id that will be called to resolve data/ most likely wrench flow name
    
    List<LocaleAndLabel> getLabels(); // localized labels, human readable names
    @Override default public TagomiDocType getDocType() { return TagomiDocType.SERVICE; };

  }
  

  @Value.Immutable
  @JsonSerialize(as = ImmutableTemplate.class)
  @JsonDeserialize(as = ImmutableTemplate.class)
  interface Template extends IsTagomiObject {
    String getId();
    String getContent(); // the markdown definition
    
    String getLocaleId();
    String getServiceId();
    
    @Override default public TagomiDocType getDocType() { return TagomiDocType.TEMPLATE; };
  }
  
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableResource.class)
  @JsonDeserialize(as = ImmutableResource.class)
  interface Resource extends IsTagomiObject {
    String getId();
    String getExternalLocation();
    String getResourceName();
    List<String> getTemplateIds();
    @Override default public TagomiDocType getDocType() { return TagomiDocType.RESOURCE; };

  }

  

  @Value.Immutable
  @JsonSerialize(as = ImmutableLocale.class)
  @JsonDeserialize(as = ImmutableLocale.class)
  interface Locale extends IsTagomiObject {
    String getId();
    
    String getLocaleCode();
    Boolean getEnabled();
    Boolean getDefault();

    @Override default public TagomiDocType getDocType() { return TagomiDocType.LOCALE; };
  }
  
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableTag.class)
  @JsonDeserialize(as = ImmutableTag.class)
  interface Tag extends IsTagomiObject {
    String getCommitId();
    String getName();
    
    @Override default public TagomiDocType getDocType() { return TagomiDocType.TAG; };
  }

  
  @Value.Immutable
  @JsonSerialize(as = ImmutableLocaleAndLabel.class)
  @JsonDeserialize(as = ImmutableLocaleAndLabel.class)
  interface LocaleAndLabel extends Serializable {
    String getLocale();     // locale id
    String getLabelValue(); // translation in locale
  }
}
