package io.resys.limaone.authoring;

import java.time.OffsetDateTime;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.limaone.model.Model;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;


@JsonSerialize(as = ImmutableBundle.class)
@JsonDeserialize(as = ImmutableBundle.class)
@Value.Immutable
public interface Bundle {
  String getId();
  String getName();
  @Nullable String getExternalId();
  @Nullable String getCockpitId();
  String getCreatedBy();
  OffsetDateTime getCreatedAt();
  OffsetDateTime getStartsAt();

  String getDescription();
  @Nullable JsonObject getErrors();
  BundleStatus getStatus();
  
  @Nullable Boolean getExternal();

  // Null when user has requested sources to be not loaded on api level
  @Nullable Model.ModelWorld getSources();


  
  enum BundleStatus {
    BUILDING, READY, ERROR, DEPLOYED
  }
}