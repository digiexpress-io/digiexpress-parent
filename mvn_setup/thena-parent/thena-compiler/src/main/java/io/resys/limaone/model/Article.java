package io.resys.limaone.model;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.limaone.model.Model.Body;
import io.resys.limaone.model.Model.BodyType;
import jakarta.annotation.Nullable;

@Value.Immutable
@JsonSerialize(as = ImmutableArticle.class)
@JsonDeserialize(as = ImmutableArticle.class)
public interface Article extends Body {
  
  String getName();
  Integer getOrder();
  
  @Nullable String getParentId();
  
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @Nullable Boolean getDevMode();
  
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @Nullable Boolean getAuthOnly();
    
  default BodyType getBodyType() {
    return BodyType.ARTICLE;
  }
}
