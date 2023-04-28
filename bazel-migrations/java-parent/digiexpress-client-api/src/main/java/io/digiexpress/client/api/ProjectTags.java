package io.digiexpress.client.api;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.digiexpress.client.api.ClientEntity.ConfigType;

@Value.Immutable @JsonSerialize(as = ImmutableProjectTags.class) @JsonDeserialize(as = ImmutableProjectTags.class)
public interface ProjectTags extends Serializable {
  ProjectDialobTags getDialob();
  ProjectHdesTags getHdes();
  ProjectStencilTags getStencil();

  interface ProjectTag extends Serializable {
    ConfigType getType();
    Map<String, ProjectTagName> getById();
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableProjectDialobTags.class) @JsonDeserialize(as = ImmutableProjectDialobTags.class)
  interface ProjectDialobTags extends ProjectTag {}
  
  @Value.Immutable @JsonSerialize(as = ImmutableProjectHdesTags.class) @JsonDeserialize(as = ImmutableProjectHdesTags.class)
  interface ProjectHdesTags extends ProjectTag {}
  
  @Value.Immutable @JsonSerialize(as = ImmutableProjectStencilTags.class) @JsonDeserialize(as = ImmutableProjectStencilTags.class)
  interface ProjectStencilTags extends ProjectTag {}
  
  
  @Value.Immutable @JsonSerialize(as = ImmutableProjectTagName.class) @JsonDeserialize(as = ImmutableProjectTagName.class)
  interface ProjectTagName extends Serializable {
    String getId();
    String getValue();
    @Nullable String getParentId();
    LocalDateTime getCreated();    
  }
}