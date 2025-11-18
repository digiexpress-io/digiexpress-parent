package io.resys.thena.ledger.client.api;

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
import java.util.function.Consumer;

import io.resys.thena.ledger.client.api.ThenaLedgerContainers.LedgerContainer;
import io.resys.thena.ledger.client.api.ThenaLedgerNewObject.NewBlackBook;
import io.resys.thena.ledger.client.api.ThenaLedgerNewObject.NewLedgerEvent;
import io.resys.thena.ledger.client.api.ThenaLedgerNewObject.NewMoneyRequest;
import io.resys.thena.ledger.client.api.ThenaLedgerNewObject.NewPayment;
import io.resys.thena.ledger.client.api.ThenaLedgerNewObject.NewProjection;
import io.resys.thena.ledger.client.api.ThenaLedgerNewObject.NewSettlement;
import io.resys.thena.ledger.client.api.ThenaLedgerNewObject.NewUnitPrice;
import io.resys.thena.ledger.client.entities.MoneyRequest.MoneyRequestFrequency;
import io.resys.thena.ledger.client.entities.MoneyRequest.MoneyRequestStatus;
import jakarta.annotation.Nullable;

// Generic interfaces for update operations and adding new entities
public interface ThenaLedgerMergeObject {

  interface MergeLedger {
    // State access
    MergeLedger onCurrentState(Consumer<LedgerContainer> handleCurrentState);
    LedgerContainer getCurrentState();
    
    // Update mutable ledger fields
    MergeLedger externalId(String externalId);
    MergeLedger name(String name);
    MergeLedger description(@Nullable String description);
    
    // Add new entities to existing ledger (all types can be added)
    MergeLedger addMoneyRequest(Consumer<NewMoneyRequest> moneyRequest);
    MergeLedger addPayment(Consumer<NewPayment> payment);
    MergeLedger addSettlement(Consumer<NewSettlement> settlement);
    MergeLedger addBlackBook(Consumer<NewBlackBook> blackBook);
    MergeLedger addProjection(Consumer<NewProjection> projection);
    MergeLedger addUnitPrice(Consumer<NewUnitPrice> unitPrice);
    MergeLedger addLedgerEvent(Consumer<NewLedgerEvent> ledgerEvent);
    
    // Modify existing mutable entities only
    MergeLedger modifyMoneyRequest(String moneyRequestId, Consumer<MergeMoneyRequest> moneyRequest);
    
    
    void build();
  }
  
  // Merge interface only for truly mutable entities
  interface MergeMoneyRequest {
    MergeMoneyRequest externalId(String externalId);
    MergeMoneyRequest type(String type);
    MergeMoneyRequest subType(@Nullable String subType);
    MergeMoneyRequest status(MoneyRequestStatus status);  // Key mutable field: OPEN -> CLOSED -> CANCELLED
    MergeMoneyRequest frequency(MoneyRequestFrequency frequency);
    MergeMoneyRequest description(@Nullable String description);
    MergeMoneyRequest dueDate(LocalDate dueDate);
    MergeMoneyRequest amount(BigDecimal amount);
    void build();
  }
}