package io.resys.thena.ledger.client.api;

/*-
 * #%L
 * thena-contract-client
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

import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelopeList;
import io.resys.thena.ledger.client.api.ThenaLedgerContainers.LedgerContainer;
import io.resys.thena.ledger.client.entities.LedgerDocType;
import io.smallrye.mutiny.Uni;

public interface LedgerQueryActions {

  LedgerQuery ledgerQuery();
  
  interface LedgerQuery {
    
    LedgerQuery lockForUpdate();
    
    // optimization, exclude explicitly doc-s that we don't need 
    LedgerQuery excludeDocs(LedgerDocType... docs);
    
    
    LedgerQuery addAllFundId(List<String> fundNameOrId); // query exciting fund related data, by default wont be included
    
    LedgerQuery addLedgerId(String id);
    LedgerQuery addAllLedgerId(List<String> ids); // include only data for given contract
    
    Uni<QueryEnvelope<LedgerContainer>> getOne(String id);
    Uni<QueryEnvelope<LedgerContainer>> findOne();
    Uni<QueryEnvelopeList<LedgerContainer>> findAll();
  }
  
}
