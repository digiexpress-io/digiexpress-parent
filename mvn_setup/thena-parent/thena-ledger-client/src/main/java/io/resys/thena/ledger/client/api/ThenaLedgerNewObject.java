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
import io.resys.thena.ledger.client.entities.MoneyRequest.MoneyRequestFrequency;
import io.resys.thena.ledger.client.entities.MoneyRequest.MoneyRequestStatus;
import io.resys.thena.ledger.client.entities.Payment;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;

public interface ThenaLedgerNewObject {

  interface NewLedger {
    NewLedger externalId(String externalId);
    NewLedger name(String name);
    NewLedger description(@Nullable String description);
    
    // nested builders for related entities
    NewLedger addMoneyRequest(Consumer<NewMoneyRequest> moneyRequest);
    NewLedger addPayment(Consumer<NewPayment> payment);
    NewLedger addSettlement(Consumer<NewSettlement> settlement);
    NewLedger addBlackBook(Consumer<NewBlackBook> blackBook);
    NewLedger addProjection(Consumer<NewProjection> projection);
    NewLedger addUnitPrice(Consumer<NewUnitPrice> unitPrice);
    NewLedger addLedgerEvent(Consumer<NewLedgerEvent> ledgerEvent);
    
    // state handling
    NewLedger onNewState(Consumer<LedgerContainer> handleNewState);
    void build();
  }
  
  // support interface for money request creation
  interface NewMoneyRequest {
    NewMoneyRequest externalId(String externalId);
    NewMoneyRequest type(String type);
    NewMoneyRequest subType(@Nullable String subType);
    NewMoneyRequest status(MoneyRequestStatus status);
    NewMoneyRequest frequency(MoneyRequestFrequency frequency);
    NewMoneyRequest description(@Nullable String description);
    NewMoneyRequest dueDate(LocalDate dueDate);
    NewMoneyRequest amount(BigDecimal amount);
    void build();
  }
  
  // support interface for payment creation
  interface NewPayment {
    NewPayment externalId(String externalId);
    NewPayment type(String type);
    NewPayment subType(@Nullable String subType);
    NewPayment description(@Nullable String description);
    NewPayment date(LocalDate date);
    NewPayment amount(BigDecimal amount);
    NewPayment body(@Nullable JsonObject body);
    NewPayment onNewState(Consumer<Payment> payment);
    Payment build();
  }
  
  // support interface for settlement creation
  interface NewSettlement {
    NewSettlement externalId(String externalId);
    NewSettlement type(String type);
    NewSettlement subType(@Nullable String subType);
    NewSettlement description(@Nullable String description);
    NewSettlement date(LocalDate date);
    NewSettlement amount(BigDecimal amount);
    
    // nested settlement payments
    NewSettlement addSettlementPayment(Consumer<NewSettlementPayment> settlementPayment);
    void build();
  }
  
  // support interface for settlement payment creation
  interface NewSettlementPayment {
    NewSettlementPayment paymentId(String paymentId);
    NewSettlementPayment allocationAmount(BigDecimal allocationAmount);
    void build();
  }
  
  // support interface for black book creation
  interface NewBlackBook {
    NewBlackBook externalId(String externalId);
    NewBlackBook type(String type);
    NewBlackBook subType(@Nullable String subType);
    NewBlackBook description(@Nullable String description);
    NewBlackBook date(LocalDate date);
    NewBlackBook amount(BigDecimal amount);

    // nested black book details
    NewBlackBook addBlackBookDetail(Consumer<NewBlackBookDetail> blackBookDetail);
    void build();
  }
  
  // support interface for black book detail creation
  interface NewBlackBookDetail {
    NewBlackBookDetail externalId(String externalId);
    NewBlackBookDetail type(String type);
    NewBlackBookDetail subType(@Nullable String subType);
    NewBlackBookDetail description(@Nullable String description);
    NewBlackBookDetail targetId(@Nullable String targetId);
    NewBlackBookDetail startDate(LocalDate startDate);
    NewBlackBookDetail endDate(LocalDate endDate);
    NewBlackBookDetail amount(BigDecimal amount);
    NewBlackBookDetail formula(@Nullable String formula);
    NewBlackBookDetail body(@Nullable JsonObject body);
    void build();
  }
  
  // support interface for projection creation
  interface NewProjection {
    NewProjection externalId(String externalId);
    NewProjection type(String type);
    NewProjection subType(@Nullable String subType);
    NewProjection description(@Nullable String description);
    NewProjection targetDate(LocalDate targetDate);
    NewProjection startDate(LocalDate startDate);
    NewProjection endDate(LocalDate endDate);
    NewProjection amount(BigDecimal amount);
    
    // nested projection details
    NewProjection addProjectionDetail(Consumer<NewProjectionDetail> projectionDetail);
    void build();
  }
  
  // support interface for projection detail creation
  interface NewProjectionDetail {
    NewProjectionDetail externalId(String externalId);
    NewProjectionDetail type(String type);
    NewProjectionDetail subType(@Nullable String subType);
    NewProjectionDetail description(@Nullable String description);
    NewProjectionDetail targetId(@Nullable String targetId);
    NewProjectionDetail startDate(LocalDate startDate);
    NewProjectionDetail endDate(LocalDate endDate);
    NewProjectionDetail amount(BigDecimal amount);
    NewProjectionDetail formula(@Nullable String formula);
    NewProjectionDetail body(@Nullable JsonObject body);
    void build();
  }
  
  // support interface for unit price creation
  interface NewUnitPrice {
    NewUnitPrice externalId(String externalId);
    NewUnitPrice type(String type);
    NewUnitPrice subType(@Nullable String subType);
    NewUnitPrice description(@Nullable String description);
    NewUnitPrice date(LocalDate date);
    NewUnitPrice value(BigDecimal value);
    void build();
  }
  
  // support interface for ledger event creation
  interface NewLedgerEvent {
    NewLedgerEvent externalId(String externalId);
    NewLedgerEvent type(String type);
    NewLedgerEvent subType(@Nullable String subType);
    NewLedgerEvent description(@Nullable String description);
    NewLedgerEvent date(LocalDate date);
    NewLedgerEvent body(@Nullable JsonObject body);
    void build();
  }
  
  // support interface for commit viewer creation (similar to Contract)
  interface NewLedgerCommitViewer {
    NewLedgerCommitViewer userId(String userId);
    NewLedgerCommitViewer usedFor(String usedFor);
    NewLedgerCommitViewer commitId(String commitId);
    NewLedgerCommitViewer currentTxCommit(); // ongoing tx commit
    NewLedgerCommitViewer currentTreeCommit(); // whatever is last tree updated commit 
    NewLedgerCommitViewer skipViewer(); // cancel out of viewer, skips the object 
    String getCurrentTreeCommit();
    void build(); 
  }
}