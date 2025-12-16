package io.digiexpress.eveli.client.web.resources.worker;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.immutables.value.Value;

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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.lp.client.spi.LpClientImpl;
import io.resys.lp.client.spi.formula.feemi_savings.AddPaymentFactory;
import io.resys.thena.contract.client.api.ContractClient;
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
  private final ContractClient contractClient;
  
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

  @PostMapping("/{ledgerId}/payments")
  public Uni<LedgerContainer> addPayment(@RequestBody CreatePaymentCommand command) {
    final var lpClient = new LpClientImpl(contractClient, ledgerClient);
    return lpClient.actions().matchPayment()
      .addHint(command.getContractId())
      .addPayment(newPayment -> {
        newPayment
          .amount(command.getAmount())
          .date(command.getTargetDate())
          .externalId(command.getPaymentId())
          .description(command.getDescription())
          .type("MANUAL")
          .build();
      }).build()
      
      .onItem().transformToUni(ignore -> lpClient.actions().calculateAny()
          .ledgerId(command.getContractId())
          .formula(new AddPaymentFactory())
          .build())
      .onItem().transformToUni(ignore -> ledgerClient.withTenant().find().ledgerQuery().getOne(command.getContractId()))
      .onItem().transform(env -> env.getObjects());
    
  }
  
  @JsonSerialize(as = ImmutableCreatePaymentCommand.class)
  @JsonDeserialize(as = ImmutableCreatePaymentCommand.class)
  @Value.Immutable
  interface CreatePaymentCommand {
    String getContractId();
    BigDecimal getAmount();
    LocalDate getTargetDate();
    String getPaymentId();
    String getDescription();
  }
}
