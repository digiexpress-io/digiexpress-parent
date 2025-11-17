package io.resys.thena.ledger.client.entities;

/*-
 * #%L
 * thena-ledger-client
 * %%
 * Copyright (C) 2015 - 2025 Copyright 2022 ReSys OÜ
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;
import jakarta.annotation.Nullable;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable
@JsonSerialize(as = ImmutableProjection.class)
@JsonDeserialize(as = ImmutableProjection.class)
public interface Projection extends LedgerEntity {

  String getId();
  String getLedgerId();
  String getExternalId();
  String getType();
  Optional<String> getSubType();
  Optional<String> getDescription();
  LocalDate getTargetDate();
  LocalDate getStartDate();
  LocalDate getEndDate();
  BigDecimal getAmount();
  String getCreatedCommit();

  // Transitive data from joins
  @Value.Auxiliary
  @Nullable ProjectionTransitives getTransitives();

  @Override
  default LedgerDocType getDocType() {
    return LedgerDocType.PROJECTION;
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableProjectionTransitives.class)
  @JsonDeserialize(as = ImmutableProjectionTransitives.class)
  interface ProjectionTransitives {
    OffsetDateTime getCreatedAt();
  }

}