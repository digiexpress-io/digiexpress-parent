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
  Uni<TagomiContainer.Locale> locale(CreateLocale init);
  Uni<TagomiContainer.Template> template(CreateTemplate init);
  Uni<TagomiContainer.Resource> resource(CreateResource init);
  Uni<TagomiContainer.Service> service(CreateService init);  
  Uni<TagomiContainer.Tag> tag(CreateTag init);
  
  interface Command extends Serializable {}

  @Value.Immutable
  @JsonSerialize(as = ImmutableCreateLocale.class)
  @JsonDeserialize(as = ImmutableCreateLocale.class)
  interface CreateLocale extends Command {
    @Nullable String getId();
    String getLocaleCode();
  }
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableCreateTemplate.class)
  @JsonDeserialize(as = ImmutableCreateTemplate.class)
  interface CreateTemplate extends Command {
    @Nullable String getId();
    String getServiceId();
    String getLocale();
    @Nullable String getContent();
  }
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableCreateResource.class)
  @JsonDeserialize(as = ImmutableCreateResource.class)
  interface CreateResource extends Command {
    @Nullable String getId();
    String getResourceName();
    String getContentType();
    byte[] getUploadBody(); // some static asset...
    List<String> getTemplateIds();
  }
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableCreateService.class)
  @JsonDeserialize(as = ImmutableCreateService.class)
  interface CreateService extends Command {
    @Nullable String getId();
    List<LocaleAndLabel> getLabels();
    
    String getServiceName();
    String getOrchestratorName();
  }

  
  @Value.Immutable
  @JsonSerialize(as = ImmutableCreateTag.class)
  @JsonDeserialize(as = ImmutableCreateTag.class)
  interface CreateTag extends Command {
    @Nullable String getId();
    String getTagName();
    String getNote();
    @Nullable String getCommitId();
  }
}
