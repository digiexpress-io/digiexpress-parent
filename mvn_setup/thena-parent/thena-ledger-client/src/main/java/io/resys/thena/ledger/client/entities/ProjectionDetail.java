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

import io.vertx.core.json.JsonObject;

@Value.Immutable
@JsonSerialize(as = ImmutableProjectionDetail.class)
@JsonDeserialize(as = ImmutableProjectionDetail.class)
public interface ProjectionDetail extends LedgerEntity {

  String getId();
  String getProjectionId();
  String getExternalId();
  String getDetailType();
  Optional<String> getDetailSubType();
  Optional<String> getDetailDescription();
  Optional<String> getTargetId();
  LocalDate getDetailStartDate();
  LocalDate getDetailEndDate();
  BigDecimal getDetailAmount();
  Optional<String> getDetailFormula();
  Optional<JsonObject> getDetailBody();
  String getCreatedCommitId();

  // Transitive data from joins
  @Value.Auxiliary
  @Nullable ProjectionDetailTransitives getTransitives();

  @Override
  default LedgerDocType getDocType() {
    return LedgerDocType.PROJECTION_DETAIL;
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableProjectionDetailTransitives.class)
  @JsonDeserialize(as = ImmutableProjectionDetailTransitives.class)
  interface ProjectionDetailTransitives {
    OffsetDateTime getCreatedAt();
  }

}