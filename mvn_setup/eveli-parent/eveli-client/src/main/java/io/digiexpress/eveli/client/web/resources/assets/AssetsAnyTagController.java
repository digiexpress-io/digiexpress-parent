package io.digiexpress.eveli.client.web.resources.assets;

/*-
 * #%L
 * eveli-client
 * %%
 * Copyright (C) 2015 - 2025 Copyright 2022 ReSys OÜ
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

import java.time.LocalDateTime;
import java.util.List;

import jakarta.annotation.Nullable;

import org.immutables.value.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.hdes.client.api.HdesClient;
import io.smallrye.mutiny.Uni;
import io.thestencil.client.api.StencilClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;



@RestController
@RequiredArgsConstructor
@RequestMapping("/worker/rest/api/assets/any-tags")
@Slf4j
public class AssetsAnyTagController {
  private final StencilClient stencilClient;
  private final HdesClient hdesClient;
  
  
  enum AssetTagType {
    WRENCH, STENCIL
  }  
  @Value.Immutable
  @JsonSerialize(as = ImmutableAnyAssetTag.class)
  @JsonDeserialize(as = ImmutableAnyAssetTag.class)
  @JsonIgnoreProperties(ignoreUnknown = true)
  interface AnyAssetTag {
    String getId();
    String getName();
    String getDescription();
    LocalDateTime getCreated();
    AssetTagType getType();
    
    // some models don't store user info for tag
    @Nullable String getUser();
  }
  
  
  @GetMapping("/wrench-tags")
  public Uni<List<AnyAssetTag>> findAllWrenchTags() {
    return hdesClient.store().query().get().onItem().transform(state ->
      state.getTags().values().stream()
          .map(release -> hdesClient.ast().commands(release.getBody()).tag())
          .map(release ->  (AnyAssetTag) ImmutableAnyAssetTag.builder()
          .created(release.getCreated())
          .type(AssetTagType.WRENCH)
          .description(release.getDescription())
          .id("no-release-id")
          .name(release.getName())

          .user("not-available")
          .build())
          .toList()
    );
  
  }

  @GetMapping("/stencil-tags")
  public Uni<List<AnyAssetTag>> findAllContentTags() {
    return stencilClient.getStore().query().head().onItem().transform(state -> 
      state.getReleases().values().stream()
          .map(release -> (AnyAssetTag) ImmutableAnyAssetTag.builder()
          .created(release.getBody().getCreated())
          .type(AssetTagType.STENCIL)
          .description(release.getBody().getNote())
          .id(release.getId())
          .name(release.getBody().getName())

          .user("not-available")
          .build())
          .toList()
    );
  }
}
