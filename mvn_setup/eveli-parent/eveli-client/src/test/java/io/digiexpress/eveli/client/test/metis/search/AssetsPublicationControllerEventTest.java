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

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.function.Consumer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.server.ResponseStatusException;

import io.digiexpress.eveli.client.api.ContentDeployedEvent;
import io.digiexpress.eveli.client.web.resources.assets.AssetsPublicationController;
import io.digiexpress.eveli.client.web.resources.assets.ImmutableCreatePublication;
import io.resys.limaone.authoring.Authoring;
import io.resys.limaone.authoring.NewDeployment;
import io.resys.limaone.model.Deployment;
import io.resys.limaone.model.Deployment.BundleStatus;
import io.resys.limaone.model.ImmutableDeployment;
import io.resys.limaone.model.ImmutableModel;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.BodyType;
import io.smallrye.mutiny.Uni;

public class AssetsPublicationControllerEventTest {

  @Test
  void anImmediatePublicationPublishesContentDeployedEvent() {
    final var publisher = Mockito.mock(ApplicationEventPublisher.class);
    controller(publisher, false)
        .createOnePublication(ImmutableCreatePublication.builder().name("now").build())
        .await().indefinitely();

    Mockito.verify(publisher).publishEvent(new ContentDeployedEvent("assets-publication"));
  }

  @Test
  void aScheduledPublicationDoesNotPublishUntilItIsLive() {
    final var publisher = Mockito.mock(ApplicationEventPublisher.class);
    controller(publisher, false)
        .createOnePublication(ImmutableCreatePublication.builder()
            .name("later")
            .liveDate(LocalDateTime.now().plusDays(1))
            .build())
        .await().indefinitely();

    Mockito.verify(publisher, Mockito.never()).publishEvent(Mockito.any());
  }

  @Test
  void aReadOnlyControllerDoesNotPublish() {
    final var publisher = Mockito.mock(ApplicationEventPublisher.class);
    Assertions.assertThrows(ResponseStatusException.class, () ->
        controller(publisher, true)
            .createOnePublication(ImmutableCreatePublication.builder().name("now").build()));
    Mockito.verifyNoInteractions(publisher);
  }

  @SuppressWarnings("unchecked")
  private static AssetsPublicationController controller(
      ApplicationEventPublisher publisher, boolean readOnly) {
    final var authoring = Mockito.mock(Authoring.class, Mockito.RETURNS_DEEP_STUBS);
    final var deployment = Mockito.mock(NewDeployment.class);
    Mockito.when(authoring.newModel().newDeployment()).thenReturn(deployment);
    Mockito.when(deployment.props(Mockito.any(Consumer.class))).thenReturn(deployment);
    Mockito.when(deployment.build()).thenReturn(Uni.createFrom().item(publication()));
    return new AssetsPublicationController(authoring, readOnly, publisher);
  }

  private static Model<Deployment> publication() {
    final var now = OffsetDateTime.now();
    return ImmutableModel.<Deployment>builder()
        .id("pub-1")
        .bodyHash("hash")
        .bodyType(BodyType.DEPLOYMENT)
        .body(ImmutableDeployment.builder()
            .fromCommitId(UUID.randomUUID())
            .name("now")
            .createdBy("test")
            .createdAt(now)
            .startsAt(now)
            .description("")
            .status(BundleStatus.UNKNOWN)
            .build())
        .build();
  }
}
