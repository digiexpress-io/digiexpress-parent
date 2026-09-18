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
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.digiexpress.eveli.client.spi.assets.LivePublications;
import io.resys.limaone.model.Deployment;
import io.resys.limaone.model.Deployment.BundleStatus;
import io.resys.limaone.model.ImmutableDeployment;
import io.resys.limaone.model.ImmutableModel;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.BodyType;

public class LivePublicationsTest {

  private static final OffsetDateTime NOW = OffsetDateTime.parse("2026-08-31T12:00:00Z");

  @Test
  void aNullOrPastLiveDateIsAlreadyLive() {
    Assertions.assertTrue(LivePublications.isLive(null, NOW));
    Assertions.assertTrue(LivePublications.isLive(NOW, NOW));
    Assertions.assertTrue(LivePublications.isLive(NOW.minusSeconds(1), NOW));
  }

  @Test
  void aFutureLiveDateIsNotLive() {
    Assertions.assertFalse(LivePublications.isLive(NOW.plusSeconds(1), NOW));
  }

  @Test
  void aFuturePublicationIsIgnoredUntilItsLiveDate() {
    final var live = publication("a", NOW.minusHours(1), NOW.minusHours(2));
    final var scheduled = publication("b", NOW.plusHours(1), NOW.minusMinutes(1));

    final var resolved = LivePublications.resolve(List.of(live, scheduled), NOW).orElseThrow();
    Assertions.assertEquals("a", resolved.getId());
  }

  @Test
  void aLaterStartsAtOvertakesThePreviousLivePublication() {
    final var previous = publication("a", NOW.minusHours(2), NOW.minusHours(3));
    final var current = publication("b", NOW.minusMinutes(1), NOW.minusHours(1));

    final var resolved = LivePublications.resolve(List.of(previous, current), NOW).orElseThrow();
    Assertions.assertEquals("b", resolved.getId());
  }

  @Test
  void theLaterCreatedAtWinsWhenStartsAtIsEqual() {
    final var first = publication("a", NOW.minusHours(1), NOW.minusHours(2));
    final var second = publication("b", NOW.minusHours(1), NOW.minusMinutes(1));

    final var resolved = LivePublications.resolve(List.of(first, second), NOW).orElseThrow();
    Assertions.assertEquals("b", resolved.getId());
  }

  @Test
  void anEmptyListHasNoLivePublication() {
    Assertions.assertTrue(LivePublications.resolve(List.of(), NOW).isEmpty());
  }

  private static Model<Deployment> publication(String id, OffsetDateTime startsAt, OffsetDateTime createdAt) {
    return ImmutableModel.<Deployment>builder()
        .id(id)
        .bodyHash(id)
        .bodyType(BodyType.DEPLOYMENT)
        .body(ImmutableDeployment.builder()
            .fromCommitId(UUID.randomUUID())
            .name(id)
            .createdBy("test")
            .createdAt(createdAt)
            .startsAt(startsAt)
            .description("")
            .status(BundleStatus.UNKNOWN)
            .build())
        .build();
  }
}
