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

import java.util.concurrent.TimeUnit;

import org.springframework.scheduling.annotation.Scheduled;

import io.digiexpress.eveli.client.config.EveliPropsMetisSearch;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class MetisLivePublicationReconciler {

  private final MetisLiveIndexTrigger trigger;
  private final EveliPropsMetisSearch props;

  @Scheduled(
      fixedDelayString = "${eveli.metis.search.live-publication-check-seconds:60}",
      initialDelay = 15,
      timeUnit = TimeUnit.SECONDS)
  public void checkLivePublication() {
    if (!Boolean.TRUE.equals(props.getReindexOnDeployment())) {
      return;
    }
    trigger.startIfLivePublicationChanged();
  }
}
