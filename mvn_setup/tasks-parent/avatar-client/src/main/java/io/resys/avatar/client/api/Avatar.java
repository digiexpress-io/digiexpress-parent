package io.resys.avatar.client.api;
import java.time.Instant;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.userprofile.client.api.ImmutableAvatar;

@Value.Immutable @JsonSerialize(as = ImmutableAvatar.class) @JsonDeserialize(as = ImmutableAvatar.class)
public interface Avatar  {

  String getId();
  String getVersion();
  String getExternalId();
  
  Instant getCreated();
  Instant getUpdated();

  String getColorCode();
  String getLetterCode();
  String getDisplayName();

}
