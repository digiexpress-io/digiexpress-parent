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



import java.util.List;
import java.util.Map;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.thena.api.envelope.ThenaContainer;
import io.resys.thena.ledger.client.entities.BlackBook;
import io.resys.thena.ledger.client.entities.BlackBookDetail;
import io.resys.thena.ledger.client.entities.Ledger;
import io.resys.thena.ledger.client.entities.LedgerEvent;
import io.resys.thena.ledger.client.entities.MoneyRequest;
import io.resys.thena.ledger.client.entities.Payment;
import io.resys.thena.ledger.client.entities.Projection;
import io.resys.thena.ledger.client.entities.ProjectionDetail;
import io.resys.thena.ledger.client.entities.Settlement;
import io.resys.thena.ledger.client.entities.SettlementPayment;
import io.resys.thena.ledger.client.entities.UnitPrice;

/**
 * Container objects that aggregate related contract entities together,
 * similar to GrimMissionContainer in the Grim domain.
 */
public interface ThenaLedgerContainers {

  @Value.Immutable
  @JsonSerialize(as = ImmutableLedgerContainer.class)
  @JsonDeserialize(as = ImmutableLedgerContainer.class)
  interface LedgerContainer extends ThenaContainer {
    Ledger getLedger();
    
    List<LedgerEvent> getLedgerEvents();
    List<MoneyRequest> getMoneyRequests();
    List<Payment> getPayments();
    List<UnitPrice> getUnitPrices();
    
    List<BlackBook> getBlackBooks();
    // bb id -> details
    Map<String, List<BlackBookDetail>> getBlackBookDetails();
    
    List<Projection> getProjections(); 
    // projection id -> details
    Map<String, List<ProjectionDetail>> getProjectionDetails();
    
    List<Settlement> getSettlements();
    // settlement id -> details
    Map<String, List<SettlementPayment>> getSettlementPayments();

  }
}
