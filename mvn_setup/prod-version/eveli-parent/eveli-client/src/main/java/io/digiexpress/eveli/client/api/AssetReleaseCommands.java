package io.digiexpress.eveli.client.api;

/*-
 * #%L
 * eveli-client
 * %%
 * Copyright (C) 2015 - 2024 Copyright 2022 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.List;
import java.util.Optional;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

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
  public interface AssetReleaseTag extends AssetTagCommands.AssetTag {
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
