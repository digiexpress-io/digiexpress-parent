package io.resys.avatar.client.api;
import java.time.Instant;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import jakarta.annotation.Nullable;

@Value.Immutable @JsonSerialize(as = ImmutableAvatar.class) @JsonDeserialize(as = ImmutableAvatar.class)
public interface Avatar  {

  String getId();
  @Nullable String getVersion();
  String getExternalId();
  String getAvatarType();
  
  @Nullable Instant getCreated();
  @Nullable Instant getUpdated();

  String getColorCode();
  String getLetterCode();
  String getDisplayName();

}
