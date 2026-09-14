package io.digiexpress.eveli.client.test.metis.search;

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

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import io.digiexpress.eveli.client.spi.assets.ContentDeployedEvent;
import io.digiexpress.eveli.client.config.EveliPropsMetisSearch;
import io.digiexpress.eveli.client.spi.metis.search.MetisLiveIndexTrigger;
import io.digiexpress.eveli.client.spi.metis.search.MetisReindexListener;
import io.resys.metis.search.api.ImmutableMetisSearchIndexStatus;
import io.resys.metis.search.api.MetisSearchClient;
import io.resys.metis.search.api.MetisSearchIndexStatus.JobState;
import io.smallrye.mutiny.Uni;

public class MetisReindexListenerTest {

  @Test
  void aContentDeployedEventStartsAReindexWhenEnabled() {
    final var trigger = Mockito.mock(MetisLiveIndexTrigger.class);
    final var props = new EveliPropsMetisSearch();
    props.setReindexOnDeployment(true);

    listener(search(), props, trigger)
        .onContentDeployed(new ContentDeployedEvent("assets-publication"));

    Mockito.verify(trigger).startIfLivePublicationChanged();
  }

  @Test
  void aContentDeployedEventIsIgnoredWhenReindexOnDeploymentIsOff() {
    final var trigger = Mockito.mock(MetisLiveIndexTrigger.class);
    final var props = new EveliPropsMetisSearch();
    props.setReindexOnDeployment(false);

    listener(search(), props, trigger)
        .onContentDeployed(new ContentDeployedEvent("assets-publication"));

    Mockito.verifyNoInteractions(trigger);
  }

  @Test
  void startupReindexesAnEmptyIndex() {
    final var search = search();
    Mockito.when(search.getIndexStatus()).thenReturn(Uni.createFrom().item(
        ImmutableMetisSearchIndexStatus.builder().state(JobState.NONE).indexedDocuments(0).build()));
    final var trigger = Mockito.mock(MetisLiveIndexTrigger.class);
    Mockito.when(trigger.startNow(false, false)).thenReturn(Uni.createFrom().item(
        ImmutableMetisSearchIndexStatus.builder().accepted(true).state(JobState.RUNNING).jobId(1L).build()));
    final var props = new EveliPropsMetisSearch();
    props.setAutoReindexOnStartup(true);

    listener(search, props, trigger).onApplicationReady();

    Mockito.verify(trigger, Mockito.timeout(1_000)).startNow(false, false);
    Mockito.verify(search, Mockito.never()).startReindex(Mockito.anyBoolean());
  }

  @Test
  void startupSkipsWhenTheIndexAlreadyHasDocuments() {
    final var search = search();
    Mockito.when(search.getIndexStatus()).thenReturn(Uni.createFrom().item(
        ImmutableMetisSearchIndexStatus.builder().state(JobState.COMPLETED).indexedDocuments(12).build()));
    final var trigger = Mockito.mock(MetisLiveIndexTrigger.class);
    final var props = new EveliPropsMetisSearch();
    props.setAutoReindexOnStartup(true);

    listener(search, props, trigger).onApplicationReady();

    Mockito.verify(trigger, Mockito.after(200).never()).startNow(Mockito.anyBoolean(), Mockito.anyBoolean());
    Mockito.verify(search, Mockito.never()).startReindex(Mockito.anyBoolean());
    Mockito.verify(search, Mockito.never()).startReindex(
        Mockito.anyBoolean(), Mockito.anyBoolean(), Mockito.any());
  }

  private static MetisReindexListener listener(
      MetisSearchClient search, EveliPropsMetisSearch props, MetisLiveIndexTrigger trigger) {
    return new MetisReindexListener(search, props, trigger);
  }

  private static MetisSearchClient search() {
    return Mockito.mock(MetisSearchClient.class);
  }
}
