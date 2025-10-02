package io.digiexpress.tagomi.api.commands;



import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.digiexpress.tagomi.api.entities.TagomiContainer;
import io.smallrye.mutiny.Uni;

public interface TagomiDeleteCommands {

  Uni<TagomiContainer.Locale> locale(String localeId);
  Uni<TagomiContainer.Resource> resource(String linkId);
  Uni<TagomiContainer.Resource> resourceOnTemplate(ResourceOnTemplate linkArticlePage);
  Uni<TagomiContainer.Template> template(String templateId);
  Uni<TagomiContainer.Service> service(String workflowId);
  Uni<TagomiContainer.Tag> tag(String tagId);
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableResourceOnTemplate.class)
  @JsonDeserialize(as = ImmutableResourceOnTemplate.class)
  interface ResourceOnTemplate {
    String getResourceId(); 
    String getTemplateId();
  }
}
