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
@JsonSerialize(as = ImmutableUnitPrice.class)
@JsonDeserialize(as = ImmutableUnitPrice.class)
public interface UnitPrice extends LedgerEntity {

  String getId();
  String getExternalId();
  String getUnitType();
  Optional<String> getUnitSubType();
  Optional<String> getUnitDescription();
  LocalDate getUnitDate();
  BigDecimal getUnitValue();
  String getCreatedCommitId();

  // Transitive data from joins
  @Value.Auxiliary
  @Nullable UnitPriceTransitives getTransitives();

  @Override
  default LedgerDocType getDocType() {
    return LedgerDocType.UNIT_PRICE;
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableUnitPriceTransitives.class)
  @JsonDeserialize(as = ImmutableUnitPriceTransitives.class)
  interface UnitPriceTransitives {
    OffsetDateTime getCreatedAt();
  }

}