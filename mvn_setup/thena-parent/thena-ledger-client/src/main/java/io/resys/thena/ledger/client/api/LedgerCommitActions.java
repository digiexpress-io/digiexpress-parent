package io.resys.thena.ledger.client.api;

/*-
 * #%L
 * thena-Ledger-client
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

import java.util.List;
import java.util.function.Consumer;

import org.immutables.value.Value;

import io.resys.thena.api.entities.CommitResultStatus;
import io.resys.thena.api.envelope.Message;
import io.resys.thena.api.envelope.ThenaEnvelope;
import io.resys.thena.ledger.client.api.ThenaLedgerContainers.LedgerContainer;
import io.resys.thena.ledger.client.api.ThenaLedgerMergeObject.MergeLedger;
import io.resys.thena.ledger.client.api.ThenaLedgerNewObject.NewLedger;
import io.resys.thena.ledger.client.api.ThenaLedgerNewObject.NewUnitPrice;
import io.resys.thena.ledger.client.entities.UnitPrice;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.Nullable;

public interface LedgerCommitActions {
  
  CreateOneLedger createOneLedger();
  ModifyOneLedger modifyOneLedger();
  CreateManyUnitPrices createManyUnitPrices();
  
  interface CreateManyUnitPrices {
    CreateManyUnitPrices commitAuthor(String author);
    CreateManyUnitPrices commitMessage(String message);
    CreateManyUnitPrices addUnitPrice(Consumer<NewUnitPrice> addUnitPrice);
    CreateManyUnitPrices onNewUnitPrices(Consumer<List<UnitPrice>> handleNewState);
    Uni<ManyUnitPricesEnvelope> build();
  }
  
  interface CreateOneLedger {
    CreateOneLedger commitAuthor(String author);
    CreateOneLedger commitMessage(String message);
    CreateOneLedger ledger(Consumer<NewLedger> addLedger);
    CreateOneLedger onNewLedger(Consumer<LedgerContainer> handleNewState);
    Uni<OneLedgerEnvelope> build();
  }
  
  interface ModifyOneLedger {
    ModifyOneLedger commitAuthor(String author);
    ModifyOneLedger commitMessage(String message);
    ModifyOneLedger ledgerId(String LedgerId);
    ModifyOneLedger modifyLedger(Consumer<MergeLedger> modifyLedger);
    Uni<OneLedgerEnvelope> build();
  }
  
  
  interface ModifyManyLedgers {
    ModifyManyLedgers commitAuthor(String author);
    ModifyManyLedgers commitMessage(String message);
    ModifyManyLedgers modifyMission(String missionId, Consumer<MergeLedger> mergeMission);
    
    Uni<ManyLedgersEnvelope> build();
  }
  
  interface CreateManyLedger {
    CreateManyLedger commitAuthor(String author);
    CreateManyLedger commitMessage(String message);
    CreateManyLedger addMission(Consumer<NewLedger> addMission);
    Uni<ManyLedgersEnvelope> build();
  }
  
  @Value.Immutable
  interface ManyLedgersEnvelope extends ThenaEnvelope {
    String getRepoId();
    CommitResultStatus getStatus();
    List<Message> getMessages();
    @Nullable String getLog();
    @Nullable List<LedgerContainer> getLedgers();
  }
  
  
  @Value.Immutable
  interface OneLedgerEnvelope extends ThenaEnvelope {
    String getRepoId();
    CommitResultStatus getStatus();
    List<Message> getMessages();
    
    @Nullable LedgerContainer getLedger();

  }
  
  
  
  @Value.Immutable
  interface ManyUnitPricesEnvelope extends ThenaEnvelope {
    String getRepoId();
    CommitResultStatus getStatus();
    List<Message> getMessages();
    @Nullable String getLog();
    @Nullable List<UnitPrice> getUnitPrices();
  }
  
}
