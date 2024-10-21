package io.digiexpress.eveli.client.api;

import java.util.List;
import java.util.Optional;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.digiexpress.eveli.client.api.AssetTagCommands.AssetTag;
import io.digiexpress.eveli.client.api.WorkflowCommands.WorkflowTag;
import jakarta.annotation.Nullable;


public interface AssetReleaseCommands extends AssetTagCommands<AssetReleaseCommands.AssetReleaseTag> {
  AssetReleaseTag createTag(AssetReleaseTagInit object);
  
  List<AssetTag> getWrenchTags();
  List<? extends AssetTag> getWorkflowTags();
  List<AssetTag> getContentTags();
  Optional<ReleaseAssets> getAssetRelease(String releaseName);
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableAssetReleaseTag.class)
  @JsonDeserialize(as = ImmutableAssetReleaseTag.class)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public interface AssetReleaseTag extends AssetTag {
    String getContentTag();
    String getWrenchTag();
    String getWorkflowTag();
  }
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableReleaseAssets.class)
  @JsonDeserialize(as = ImmutableReleaseAssets.class)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public interface ReleaseAssets {
    AssetReleaseTag getAssetRelease();
    WorkflowTag workflowRelease();
    @Nullable
    JsonNode contentRelease();
    @Nullable
    JsonNode wrenchRelease();
    @Nullable
    JsonNode dialobReleaseForms();
  }
  
  /*
   * Null values for tags indicate that new tag should be created.
   */
  @Value.Immutable
  @JsonSerialize(as = ImmutableAssetReleaseTagInit.class)
  @JsonDeserialize(as = ImmutableAssetReleaseTagInit.class)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public interface AssetReleaseTagInit extends AssetTagInit {
    @Nullable
    String getContentTag();
    @Nullable
    String getWrenchTag();
    @Nullable
    String getWorkflowTag();
  }
}
