package io.resys.metis.api;

/*-
 * #%L
 * metis-client
 * %%
 * Copyright (C) 2015 - 2026 Copyright 2022 ReSys OÜ
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

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import jakarta.annotation.Nullable;

@Value.Immutable
@JsonSerialize(as = ImmutableMetisStatus.class)
@JsonDeserialize(as = ImmutableMetisStatus.class)
public interface MetisStatus {

  boolean getEnabled();

  String getProvider();

  String getChatModel();

  String getEmbeddingModel();

  List<MetisCapabilityStatus> getCapabilities();

  enum CapabilityState {
    READY, NOT_READY, DISABLED, ERROR
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableMetisCapabilityStatus.class)
  @JsonDeserialize(as = ImmutableMetisCapabilityStatus.class)
  interface MetisCapabilityStatus {

    String getId();

    boolean getEnabled();

    CapabilityState getState();

    @Nullable
    String getDetail();
  }
}
