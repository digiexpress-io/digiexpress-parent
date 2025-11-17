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

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import jakarta.annotation.Nullable;

@Value.Immutable
@JsonSerialize(as = ImmutableSettlement.class)
@JsonDeserialize(as = ImmutableSettlement.class)
public interface Settlement extends LedgerEntity {

  String getId();
  String getLedgerId();
  String getExternalId();
  String getType();
  Optional<String> getSubType();
  Optional<String> getDescription();
  LocalDate getDate();
  BigDecimal getAmount();
  String getCreatedCommit();

  // Transitive data from joins
  @Value.Auxiliary
  @Nullable SettlementTransitives getTransitives();

  @Override
  default LedgerDocType getDocType() {
    return LedgerDocType.SETTLEMENT;
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableSettlementTransitives.class)
  @JsonDeserialize(as = ImmutableSettlementTransitives.class)
  interface SettlementTransitives {
    OffsetDateTime getCreatedAt();
  }
}