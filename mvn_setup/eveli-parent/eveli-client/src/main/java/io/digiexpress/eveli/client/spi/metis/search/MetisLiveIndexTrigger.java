package io.digiexpress.eveli.client.spi.metis.search;

/*-
 * #%L
 * eveli-client
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

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.concurrent.Executor;

import org.springframework.beans.factory.ObjectProvider;

import io.digiexpress.eveli.client.config.EveliAutoConfigAssets.EveliEditEnvir;
import io.digiexpress.eveli.client.config.EveliPropsMetisSearch;
import io.digiexpress.eveli.client.spi.assets.LivePublications;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.model.Model.ModelWorld;
import io.resys.metis.search.api.MetisSearchClient;
import io.resys.metis.search.api.MetisSearchIndexStatus;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class MetisLiveIndexTrigger {

  private final MetisSearchClient search;
  private final EveliPropsMetisSearch props;
  private final ObjectProvider<EveliEditEnvir> editEnvir;
  /** Worker pool: awaiting Thena SQL on the Vert.x loop deadlocks. */
  private final Executor worker;

  public void startIfLivePublicationChanged() {
    if (!Boolean.TRUE.equals(props.getReindexOnDeployment())) {
      return;
    }
    onWorker(resolveLivePublicationId()).subscribe().with(liveId -> {
      if (liveId.isEmpty()) {
        return;
      }
      final var id = liveId.get();
      if (search.isPublicationIndexed(id)) {
        return;
      }
      if (search.isReindexInFlight()) {
        log.debug("Metis waiting for the in-flight reindex before indexing live publication: {}", id);
        return;
      }
      startReindex(false, false, id);
    }, error -> log.error("Metis could not resolve the live publication, because of: {}",
        error.toString(), error));
  }

  public Uni<MetisSearchIndexStatus> startNow(boolean force, boolean replace) {
    return onWorker(resolveLivePublicationId())
        .onItem().transformToUni(liveId -> search.startReindex(force, replace, liveId.orElse(null)))
        .onFailure().recoverWithUni(error -> {
          log.error("Metis could not resolve the live publication for a manual reindex, because of: {}",
              error.toString(), error);
          return search.startReindex(force, replace, null);
        });
  }

  private Uni<Optional<String>> resolveLivePublicationId() {
    final var envir = editEnvir.getIfAvailable();
    if (envir == null) {
      return Uni.createFrom().item(Optional.empty());
    }
    return envir.getAuthoring().worldQuery()
        .docs(BodyType.DEPLOYMENT)
        .findAll()
        .onItem().transform(this::livePublicationId);
  }

  private Optional<String> livePublicationId(ModelWorld world) {
    return LivePublications.resolve(world.getDeployments().values(), OffsetDateTime.now())
        .map(Model::getId);
  }

  private <T> Uni<T> onWorker(Uni<T> uni) {
    return uni.emitOn(worker);
  }

  private void startReindex(boolean force, boolean replace, String publicationId) {
    search.startReindex(force, replace, publicationId).subscribe().with(status -> {
      if (status.getAccepted()) {
        log.info("Metis starting an incremental reindex for live publication: {}", publicationId);
        return;
      }
      log.debug("Metis skipped the reindex, job: {} is already running", status.getJobId());
    }, error -> log.error("Metis failed to start a reindex, because of: {}", error.toString(), error));
  }
}
