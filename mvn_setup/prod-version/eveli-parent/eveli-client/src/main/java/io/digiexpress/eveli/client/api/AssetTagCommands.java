package io.digiexpress.eveli.client.api;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import jakarta.annotation.Nullable;

public interface AssetTagCommands<T>{
  
  T createTag(AssetTagInit init);
  List<T> findAll();
  Optional<T> getByName(String name);
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableAssetTagInit.class)
  @JsonDeserialize(as = ImmutableAssetTagInit.class)
  @JsonIgnoreProperties(ignoreUnknown = true)
  interface AssetTagInit {
    String getName();
    String getDescription();
    @Nullable
    String getUser();
  }
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableAssetTag.class)
  @JsonDeserialize(as = ImmutableAssetTag.class)
  @JsonIgnoreProperties(ignoreUnknown = true)
  interface AssetTag {
    String getId();
    String getName();
    String getDescription();
    // some models don't store user info for tag
    @Nullable
    String getUser();
    LocalDateTime getCreated();
  }
  
}
