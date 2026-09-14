package io.resys.metis.spi;

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

import java.util.ArrayList;

import io.resys.metis.api.ImmutableMetisCapabilityStatus;
import io.resys.metis.api.ImmutableMetisStatus;
import io.resys.metis.api.MetisClient;
import io.resys.metis.api.MetisConfig;
import io.resys.metis.api.MetisStatus;
import io.resys.metis.api.MetisStatus.CapabilityState;
import io.resys.metis.api.MetisStatus.MetisCapabilityStatus;
import io.resys.metis.search.api.MetisSearchClient;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class MetisClientImpl implements MetisClient {

  public static final String CAPABILITY_SEARCH = "site-search";

  private final MetisConfig config;
  private final MetisSearchClient search;

  @Override
  public MetisSearchClient search() {
    if (search == null) {
      throw new IllegalStateException(
          "Metis site search is not enabled, set eveli.metis.search.enabled");
    }
    return search;
  }

  @Override
  public boolean isSearchEnabled() {
    return search != null;
  }

  @Override
  public Uni<MetisStatus> getStatus() {
    return Uni.createFrom().item(this::buildStatus)
        .runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
  }

  private MetisStatus buildStatus() {
    final var capabilities = new ArrayList<MetisCapabilityStatus>();
    capabilities.add(searchStatus());

    return ImmutableMetisStatus.builder()
        .enabled(true)
        .provider(config.getProvider())
        .chatModel(config.getChatModelId())
        .embeddingModel(config.getEmbeddingModelId())
        .capabilities(capabilities)
        .build();
  }

  private MetisCapabilityStatus searchStatus() {
    final var status = ImmutableMetisCapabilityStatus.builder().id(CAPABILITY_SEARCH);
    if (search == null) {
      return status.enabled(false).state(CapabilityState.DISABLED).build();
    }
    try {
      final var indexed = search.countIndexedDocuments();
      if (!search.isIndexReadyForPortal()) {
        return status.enabled(true).state(CapabilityState.NOT_READY)
            .detail("Portal is serving keyword search until a reindex completes").build();
      }
      if (indexed == 0) {
        return status.enabled(true).state(CapabilityState.NOT_READY)
            .detail("The index is empty, no reindex has completed yet").build();
      }
      return status.enabled(true).state(CapabilityState.READY)
          .detail(indexed + " documents indexed").build();
    } catch (RuntimeException e) {
      log.warn("Metis could not read the site search state, because of: {}", e.toString(), e);
      return status.enabled(true).state(CapabilityState.ERROR).detail(e.toString()).build();
    }
  }
}
