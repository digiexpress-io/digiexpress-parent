package io.resys.metis.search.api;

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

import java.time.OffsetDateTime;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import jakarta.annotation.Nullable;

@Value.Immutable
@JsonSerialize(as = ImmutableMetisSearchIndexStatus.class)
@JsonDeserialize(as = ImmutableMetisSearchIndexStatus.class)
public interface MetisSearchIndexStatus {

  @Value.Default
  default boolean getAccepted() {
    return true;
  }

  @Nullable
  Long getJobId();

  JobState getState();

  @Nullable
  Integer getTotalCount();

  @Value.Default
  default int getProcessedCount() {
    return 0;
  }

  @Value.Default
  default int getSkippedCount() {
    return 0;
  }

  @Nullable
  OffsetDateTime getStartedAt();

  @Nullable
  OffsetDateTime getFinishedAt();

  @Nullable
  Long getDurationMs();

  @Nullable
  String getError();

  @Nullable
  String getEmbeddingModel();

  @Nullable
  String getPublicationId();

  @Nullable
  String getBundleHash();

  @Value.Default
  default long getIndexedDocuments() {
    return 0;
  }

  enum JobState {
    NONE, RUNNING, CANCELLING, CANCELLED, COMPLETED, FAILED
  }
}
