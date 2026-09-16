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

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import io.digiexpress.eveli.client.spi.assets.ContentDeployedEvent;
import io.digiexpress.eveli.client.config.EveliPropsMetisSearch;
import io.resys.metis.search.api.MetisSearchClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class MetisReindexListener {

  private final MetisSearchClient search;
  private final EveliPropsMetisSearch props;
  private final MetisLiveIndexTrigger trigger;

  @EventListener(ApplicationReadyEvent.class)
  public void onApplicationReady() {
    if (!Boolean.TRUE.equals(props.getAutoReindexOnStartup())) {
      return;
    }
    search.index().getIndexStatus().subscribe().with(indexStatus -> {
      if (indexStatus.getIndexedDocuments() > 0) {
        log.info("Metis index already holds {} document(s), skipping the startup reindex",
            indexStatus.getIndexedDocuments());
        return;
      }
      log.info("Metis index is empty, starting the first reindex in the background");
      trigger.startNow(false, false).subscribe().with(jobStatus -> {
        if (!jobStatus.getAccepted()) {
          log.info("Metis skipped the reindex, job: {} is already running", jobStatus.getJobId());
        }
      }, error -> log.error("Metis failed to start a reindex, because of: {}", error.getMessage(), error));
    }, error -> log.error("Metis could not read the index status on startup, because of: {}",
        error.getMessage(), error));
  }

  @EventListener
  public void onContentDeployed(ContentDeployedEvent event) {
    if (!Boolean.TRUE.equals(props.getReindexOnDeployment())) {
      return;
    }
    log.info("Metis checking the live publication after content change from: {}", event.source());
    trigger.startIfLivePublicationChanged();
  }
}
