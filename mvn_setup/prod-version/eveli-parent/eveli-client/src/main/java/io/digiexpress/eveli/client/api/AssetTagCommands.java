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
