package io.digiexpress.eveli.client.spi.assets;

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
import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;

import io.resys.limaone.model.Deployment;
import io.resys.limaone.model.Model;

/**
 * A publication is live when it has no start time, or the start time is not in the future.
 * Among live publications the latest {@code startsAt} wins, then the later {@code createdAt}.
 */
public final class LivePublications {

  private LivePublications() {}

  public static boolean isLive(OffsetDateTime liveDate, OffsetDateTime now) {
    return liveDate == null || !liveDate.isAfter(now);
  }

  public static Optional<Model<Deployment>> resolve(
      Collection<Model<Deployment>> deployments, OffsetDateTime now) {
    if (deployments == null || deployments.isEmpty()) {
      return Optional.empty();
    }
    final var byStartsAt = Comparator.comparing(
        (Model<Deployment> model) -> model.getBody().getStartsAt(),
        Comparator.nullsFirst(OffsetDateTime::compareTo));
    final var byCreatedAt = Comparator.comparing(
        (Model<Deployment> model) -> model.getBody().getCreatedAt(),
        Comparator.nullsFirst(OffsetDateTime::compareTo));
    return deployments.stream()
        .filter(model -> isLive(model.getBody().getStartsAt(), now))
        .max(byStartsAt.thenComparing(byCreatedAt));
  }
}
