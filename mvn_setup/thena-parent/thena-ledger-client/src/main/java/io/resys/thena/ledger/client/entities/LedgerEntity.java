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

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import jakarta.annotation.Nullable;

public interface LedgerEntity {
  
  String getId();
  
  @JsonIgnore
  LedgerDocType getDocType();

  enum LedgerRelationType {
    LEDGER,
    MONEY_REQUEST,
    PAYMENT,
    SETTLEMENT,
    BLACK_BOOK,
    PROJECTION
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableLedgerOneOfRelations.class)
  @JsonDeserialize(as = ImmutableLedgerOneOfRelations.class)
  interface LedgerOneOfRelations {
    @Nullable String getLedgerId();
    @Nullable String getMoneyRequestId();
    @Nullable String getPaymentId();
    @Nullable String getSettlementId();
    @Nullable String getBlackBookId();
    @Nullable String getProjectionId();
    LedgerRelationType getRelationType();
    
    @JsonIgnore 
    default String getTargetId() {
      switch (getRelationType()) {
        case LEDGER: return getLedgerId();
        case MONEY_REQUEST: return getMoneyRequestId();
        case PAYMENT: return getPaymentId();
        case SETTLEMENT: return getSettlementId();
        case BLACK_BOOK: return getBlackBookId();
        case PROJECTION: return getProjectionId();
        default: throw new IllegalArgumentException("Unexpected value: " + getRelationType());
      }
    }
  }
}