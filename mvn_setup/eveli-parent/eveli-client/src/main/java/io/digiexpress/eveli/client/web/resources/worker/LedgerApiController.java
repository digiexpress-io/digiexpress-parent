package io.digiexpress.eveli.client.web.resources.worker;

/*-
 * #%L
 * eveli-client
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

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.resys.thena.ledger.client.api.LedgerClient;
import io.resys.thena.ledger.client.api.ThenaLedgerContainers.LedgerContainer;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;



@RequiredArgsConstructor
@RestController
@RequestMapping("/worker/rest/api/ledgers")
@Slf4j
public class LedgerApiController {

  private final LedgerClient ledgerClient;
  
  @GetMapping("/all")
  public Multi<LedgerContainer> all() {
    return ledgerClient.withTenant().find().ledgerQuery().findAll()
        .onItem().transformToMulti(env -> Multi.createFrom().items(env.getObjects().stream()));
  }

  @GetMapping("/{ledgerId}")
  public Uni<LedgerContainer> getOnLedger(@PathVariable("ledgerId") String id) {
    return ledgerClient.withTenant().find().ledgerQuery().getOne(id)
        .onItem().transform(env -> env.getObjects());
  }

}
