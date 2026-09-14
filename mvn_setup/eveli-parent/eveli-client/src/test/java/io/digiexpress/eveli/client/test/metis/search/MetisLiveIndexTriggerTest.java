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

import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.ObjectProvider;

import io.digiexpress.eveli.client.config.EveliAutoConfigAssets.EveliEditEnvir;
import io.digiexpress.eveli.client.config.EveliPropsMetisSearch;
import io.digiexpress.eveli.client.spi.metis.search.MetisLiveIndexTrigger;
import io.resys.limaone.authoring.Authoring;
import io.resys.limaone.model.Deployment;
import io.resys.limaone.model.Deployment.BundleStatus;
import io.resys.limaone.model.ImmutableDeployment;
import io.resys.limaone.model.ImmutableModel;
import io.resys.limaone.model.ImmutableModelWorld;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.BodyType;
import io.resys.metis.search.api.ImmutableMetisSearchIndexStatus;
import io.resys.metis.search.api.MetisSearchClient;
import io.resys.metis.search.api.MetisSearchIndexStatus.JobState;
import io.smallrye.mutiny.Uni;

public class MetisLiveIndexTriggerTest {

  @Test
  void startsAJobWhenTheLivePublicationIsNotYetIndexed() {
    final var search = Mockito.mock(MetisSearchClient.class);
    Mockito.when(search.isPublicationIndexed("pub-live")).thenReturn(false);
    Mockito.when(search.isReindexInFlight()).thenReturn(false);
    Mockito.when(search.startReindex(false, false, "pub-live"))
        .thenReturn(Uni.createFrom().item(accepted()));

    trigger(search, world("pub-live", OffsetDateTime.now().minusHours(1)))
        .startIfLivePublicationChanged();

    Mockito.verify(search).startReindex(false, false, "pub-live");
  }

  @Test
  void skipsWhenAReindexIsAlreadyInFlight() {
    final var search = Mockito.mock(MetisSearchClient.class);
    Mockito.when(search.isPublicationIndexed("pub-live")).thenReturn(false);
    Mockito.when(search.isReindexInFlight()).thenReturn(true);

    trigger(search, world("pub-live", OffsetDateTime.now().minusHours(1)))
        .startIfLivePublicationChanged();

    Mockito.verify(search, Mockito.never()).startReindex(
        Mockito.anyBoolean(), Mockito.anyBoolean(), Mockito.any());
  }

  @Test
  void skipsWhenTheLivePublicationIsAlreadyIndexed() {
    final var search = Mockito.mock(MetisSearchClient.class);
    Mockito.when(search.isPublicationIndexed("pub-live")).thenReturn(true);

    trigger(search, world("pub-live", OffsetDateTime.now().minusHours(1)))
        .startIfLivePublicationChanged();

    Mockito.verify(search, Mockito.never()).startReindex(
        Mockito.anyBoolean(), Mockito.anyBoolean(), Mockito.any());
  }

  @Test
  void skipsAPublicationThatIsNotLiveYet() {
    final var search = Mockito.mock(MetisSearchClient.class);

    trigger(search, world("pub-future", OffsetDateTime.now().plusHours(1)))
        .startIfLivePublicationChanged();

    Mockito.verify(search, Mockito.never()).isPublicationIndexed(Mockito.any());
    Mockito.verify(search, Mockito.never()).startReindex(
        Mockito.anyBoolean(), Mockito.anyBoolean(), Mockito.any());
  }

  @Test
  void skipsWhenReindexOnDeploymentIsOff() {
    final var search = Mockito.mock(MetisSearchClient.class);
    final var props = new EveliPropsMetisSearch();
    props.setReindexOnDeployment(false);

    trigger(search, world("pub-live", OffsetDateTime.now().minusHours(1)), props)
        .startIfLivePublicationChanged();

    Mockito.verify(search, Mockito.never()).isPublicationIndexed(Mockito.any());
    Mockito.verify(search, Mockito.never()).startReindex(
        Mockito.anyBoolean(), Mockito.anyBoolean(), Mockito.any());
  }

  @Test
  void aManualStartStampsTheLivePublicationId() {
    final var search = Mockito.mock(MetisSearchClient.class);
    Mockito.when(search.startReindex(false, false, "pub-live"))
        .thenReturn(Uni.createFrom().item(accepted()));

    final var status = trigger(search, world("pub-live", OffsetDateTime.now().minusHours(1)))
        .startNow(false, false)
        .await().indefinitely();

    Assertions.assertTrue(status.getAccepted());
    Mockito.verify(search).startReindex(false, false, "pub-live");
  }

  private static MetisLiveIndexTrigger trigger(MetisSearchClient search, ImmutableModelWorld world) {
    final var props = new EveliPropsMetisSearch();
    props.setReindexOnDeployment(true);
    return trigger(search, world, props);
  }

  @SuppressWarnings("unchecked")
  private static MetisLiveIndexTrigger trigger(
      MetisSearchClient search, ImmutableModelWorld world, EveliPropsMetisSearch props) {
    final var authoring = Mockito.mock(Authoring.class, Mockito.RETURNS_DEEP_STUBS);
    Mockito.when(authoring.worldQuery().docs(BodyType.DEPLOYMENT).findAll())
        .thenReturn(Uni.createFrom().item(world));
    final var envir = new EveliEditEnvir(null, null, authoring, null);
    final ObjectProvider<EveliEditEnvir> provider = Mockito.mock(ObjectProvider.class);
    Mockito.when(provider.getIfAvailable()).thenReturn(envir);
    return new MetisLiveIndexTrigger(search, props, provider, Runnable::run);
  }

  private static ImmutableModelWorld world(String id, OffsetDateTime startsAt) {
    final Model<Deployment> publication = ImmutableModel.<Deployment>builder()
        .id(id)
        .bodyHash(id)
        .bodyType(BodyType.DEPLOYMENT)
        .body(ImmutableDeployment.builder()
            .fromCommitId(UUID.randomUUID())
            .name(id)
            .createdBy("test")
            .createdAt(startsAt)
            .startsAt(startsAt)
            .description("")
            .status(BundleStatus.UNKNOWN)
            .build())
        .build();
    return ImmutableModelWorld.builder().name("test").putDeployments(id, publication).build();
  }

  private static ImmutableMetisSearchIndexStatus accepted() {
    return ImmutableMetisSearchIndexStatus.builder()
        .accepted(true)
        .state(JobState.RUNNING)
        .jobId(1L)
        .build();
  }
}
