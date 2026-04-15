package io.resys.limaone.model;

import java.util.List;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.limaone.model.Model.Body;
import io.resys.limaone.model.Model.BodyType;
import jakarta.annotation.Nullable;

@Value.Immutable
@JsonSerialize(as = ImmutablePrinoutResource.class)
@JsonDeserialize(as = ImmutablePrinoutResource.class)
public interface PrinoutResource extends Body {
  String getId();
  String getExternalLocation();
  String getResourceName();
  String getContentType();
  List<String> getTemplateIds();
  @Nullable String getContent();
  
  default BodyType getBodyType() { return BodyType.PRINTOUT_RESOURCE; };

}
