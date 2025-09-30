package io.digiexpress.tagomi.api.commands;

import java.io.Serializable;
import java.util.List;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.digiexpress.tagomi.api.entities.TagomiContainer;
import io.digiexpress.tagomi.api.entities.TagomiContainer.LocaleAndLabel;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.Nullable;




public interface TagomiCreateCommands {

  Uni<TagomiContainer.Article> article(CreateArticle init);
  Uni<TagomiContainer.Locale> locale(CreateLocale init);
  Uni<TagomiContainer.Template> page(CreateTemplate init);
  Uni<TagomiContainer.Resource> resource(CreateResource init);
  Uni<TagomiContainer.Service> service(CreateService init);  
  Uni<List<TagomiContainer.IsTagomiObject>> batch(BatchSite batch);
  
  interface Command extends Serializable {}

  @Value.Immutable
  @JsonSerialize(as = ImmutableBatchSite.class)
  @JsonDeserialize(as = ImmutableBatchSite.class)
  interface BatchSite extends Command {
    List<CreateLocale> getLocales();
    List<CreateTemplate> getTemplates();
    List<CreateArticle> getArticles();
    List<CreateService> getServices();
    List<CreateResource> getResources();
  }
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableCreateArticle.class)
  @JsonDeserialize(as = ImmutableCreateArticle.class)
  interface CreateArticle extends Command {
    @Nullable String getId();
    String getName();
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableCreateLocale.class)
  @JsonDeserialize(as = ImmutableCreateLocale.class)
  interface CreateLocale extends Command {
    @Nullable String getId();
    String getLocale();
  }
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableCreateTemplate.class)
  @JsonDeserialize(as = ImmutableCreateTemplate.class)
  interface CreateTemplate extends Command {
    @Nullable String getId();
    String getArticleId();
    String getLocale();
    @Nullable String getContent();
  }
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableCreateResource.class)
  @JsonDeserialize(as = ImmutableCreateResource.class)
  interface CreateResource extends Command {
    @Nullable String getId();
    String getResourceName();
    byte[] getUploadBody(); // some static asset...
    List<String> getArticles();
  }
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableCreateService.class)
  @JsonDeserialize(as = ImmutableCreateService.class)
  interface CreateService extends Command {
    @Nullable String getId();
    List<String> getArticles();
    List<LocaleAndLabel> getLabels();
    
    String getServiceName();
    String getOrchestratorName();
  }

}
