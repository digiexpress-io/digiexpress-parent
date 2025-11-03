package io.resys.thena.contract.client.spi;

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

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.google.common.collect.ComparisonChain;

import io.resys.thena.api.entities.Tenant;
import io.resys.thena.contract.client.api.ContractQueryActions;
import io.resys.thena.contract.client.tables.ContractDb;
import io.resys.thena.jackson.QuarkusJacksonJsonCodec;
import io.vertx.core.json.JsonObject;

public class ContractPrinter {
  private final ContractQueryActions queryActions;

  public ContractPrinter(ContractDb db) {
    super();
    this.queryActions = new ContractQueryActionsImpl(db, db.getDataSource().getTenant().getId());
  }

  public String print(Tenant repo) {
    return internalPrinting(repo, false, null);
  }

  public String printWithStaticIds(Tenant repo, Map<String, String> replacements) {
    return internalPrinting(repo, true, replacements);
  }

  public String internalPrinting(Tenant repo, boolean isStatic, final Map<String, String> collector) {
    final Map<String, String> wipes = new HashMap<>();
    final Map<String, String> replacements = collector != null ? collector : new HashMap<>();
    
    final Function<String, String> ID = (id) -> {
      if (!isStatic) {
        return id;
      }
      if (id == null) {
        return null;
      }

      if (replacements.containsKey(id)) {
        return replacements.get(id);
      }
      final var next = String.valueOf(replacements.size() + 1);
      replacements.put(id, next);
      return next;
    };

    final Function<Object, String> TR = (input) -> {
      if (input == null) {
        return "null";
      }
      if (!isStatic) {
        return "null";
      }
      final var id = JsonObject.mapFrom(input).encode();
      if (wipes.containsKey(id)) {
        return wipes.get(id);
      }
      wipes.put(id, "null");
      return "null";
    };

    final Function<OffsetDateTime, String> DATES = (input) -> {
      if (input == null) {
        return null;
      }
      try {
        final var id = QuarkusJacksonJsonCodec.mapper().writeValueAsString(input);
        if (!isStatic) {
          return id.toString();
        }

        if (replacements.containsKey(id)) {
          return replacements.get(id);
        }

        final var next = "\"OffsetDateTime.now()\"";
        replacements.put(id, next);
        return next;
      } catch (Exception e) {
        throw new RuntimeException(e.getMessage(), e);
      }
    };

    StringBuilder result = new StringBuilder();

    result
        .append(System.lineSeparator())
        .append("Repo").append(System.lineSeparator())
        .append("  - id: ").append(ID.apply(repo.getId()))
        .append(", rev: ").append(ID.apply(repo.getRev())).append(System.lineSeparator())
        .append("    name: ").append(repo.getName())
        .append(", prefix: ").append(ID.apply(repo.getPrefix()))
        .append(", type: ").append(repo.getType()).append(System.lineSeparator());

    queryActions.contractQuery().findAll()
        .onItem()
        .transform(items -> {
          
          // Pre-process all dates and IDs for static replacement
          for (final var item : items.getObjects()) {
            final var contract = item.getContract();
            ID.apply(contract.getId());
            ID.apply(contract.getCommitId());
            DATES.apply(contract.getTransitives().getCreatedAt());
            DATES.apply(contract.getTransitives().getUpdatedAt());
            
            // Process all related entities
            for (final var party : item.getParties()) {
              ID.apply(party.getId());
              ID.apply(party.getCommitId());
              DATES.apply(party.getTransitives().getCreatedAt());
            }
            
            for (final var coverage : item.getCoverages()) {
              ID.apply(coverage.getId());
              ID.apply(coverage.getCommitId());
              DATES.apply(coverage.getTransitives().getCreatedAt());
            }
            
            for (final var reference : item.getReferences()) {
              ID.apply(reference.getId());
              ID.apply(reference.getCommitId());
              DATES.apply(reference.getTransitives().getCreatedAt());
            }
            
            for (final var note : item.getNotes()) {
              ID.apply(note.getId());
              ID.apply(note.getCommitId());
              DATES.apply(note.getTransitives().getCreatedAt());
            }
            
            for (final var capability : item.getCapabilities()) {
              ID.apply(capability.getId());
              ID.apply(capability.getCommitId());
              DATES.apply(capability.getTransitives().getCreatedAt());
            }
            
            for (final var invPlan : item.getInvPlans()) {
              ID.apply(invPlan.getId());
              ID.apply(invPlan.getCommitId());
              DATES.apply(invPlan.getTransitives().getCreatedAt());
            }
            
            for (final var paymentPlan : item.getPaymentPlans()) {
              ID.apply(paymentPlan.getId());
              ID.apply(paymentPlan.getCommitId());
              DATES.apply(paymentPlan.getTransitives().getCreatedAt());
            }
            
            for (final var allocList : item.getInvPlanAllocations().values()) {
              for (final var alloc : allocList) {
                ID.apply(alloc.getId());
                ID.apply(alloc.getCommitId());
                DATES.apply(alloc.getTransitives().getCreatedAt());
              }
            }
          }

          // Sort and print contracts
          for (final var item : items.getObjects().stream()
              .sorted((a, b) -> ComparisonChain.start()
                  .compare(ID.apply(a.getContract().getId()), ID.apply(b.getContract().getId()))
                  .result())
              .toList()) {

            final var contract = item.getContract();
            result.append("Contract: ").append(ID.apply(contract.getId())).append(System.lineSeparator());

            // Print parties
            for (final var data : item.getParties().stream()
                .sorted((a, b) -> ComparisonChain.start()
                    .compare(ID.apply(a.getCommitId()), ID.apply(b.getCommitId()))
                    .compare(a.getPartyType() + "", b.getPartyType() + "")
                    .compare(a.getExternalId(), b.getExternalId())
                    .result())
                .toList()) {

              result.append("  - ").append(ID.apply(data.getId())).append("::").append(data.getDocType()).append(System.lineSeparator());
            }
            
            // Print coverages
            for (final var data : item.getCoverages().stream()
                .sorted((a, b) -> ComparisonChain.start()
                    .compare(ID.apply(a.getCommitId()), ID.apply(b.getCommitId()))
                    .compare(a.getCoverageType() + "", b.getCoverageType() + "")
                    .compare(a.getInsuredId(), b.getInsuredId())
                    .result())
                .toList()) {

              result.append("  - ").append(ID.apply(data.getId())).append("::").append(data.getDocType()).append(System.lineSeparator());
            }
            
            // Print references
            for (final var data : item.getReferences().stream()
                .sorted((a, b) -> ComparisonChain.start()
                    .compare(ID.apply(a.getCommitId()), ID.apply(b.getCommitId()))
                    .compare(a.getReferenceType() + "", b.getReferenceType() + "")
                    .compare(a.getTransitives().getCreatedAt(), b.getTransitives().getCreatedAt())
                    .result())
                .toList()) {

              result.append("  - ").append(ID.apply(data.getId())).append("::").append(data.getDocType()).append(System.lineSeparator());
            }
            
            // Print notes
            for (final var data : item.getNotes().stream()
                .sorted((a, b) -> ComparisonChain.start()
                    .compare(ID.apply(a.getCommitId()), ID.apply(b.getCommitId()))
                    .compare(a.getNoteType(), b.getNoteType())
                    .compare(a.getTransitives().getCreatedAt(), b.getTransitives().getCreatedAt())
                    .result())
                .toList()) {

              result.append("  - ").append(ID.apply(data.getId())).append("::").append(data.getDocType()).append(System.lineSeparator());
            }
            
            // Print capabilities
            for (final var data : item.getCapabilities().stream()
                .sorted((a, b) -> ComparisonChain.start()
                    .compare(ID.apply(a.getCommitId()), ID.apply(b.getCommitId()))
                    .compare(a.getCapabilityType() + "", b.getCapabilityType() + "")
                    .result())
                .toList()) {

              result.append("  - ").append(ID.apply(data.getId())).append("::").append(data.getDocType()).append(System.lineSeparator());
            }
            
            // Print investment plans
            for (final var data : item.getInvPlans().stream()
                .sorted((a, b) -> ComparisonChain.start()
                    .compare(ID.apply(a.getCommitId()), ID.apply(b.getCommitId()))
                    .compare(a.getInvPlanName(), b.getInvPlanName())
                    .result())
                .toList()) {

              result.append("  - ").append(ID.apply(data.getId())).append("::").append(data.getDocType()).append(System.lineSeparator());
            }
            
            // Print payment plans
            for (final var data : item.getPaymentPlans().stream()
                .sorted((a, b) -> ComparisonChain.start()
                    .compare(ID.apply(a.getCommitId()), ID.apply(b.getCommitId()))
                    .compare(a.getPaymentPlanAmount(), b.getPaymentPlanAmount())
                    .result())
                .toList()) {

              result.append("  - ").append(ID.apply(data.getId())).append("::").append(data.getDocType()).append(System.lineSeparator());
            }
            
            // Print investment plan allocations
            for (final var allocList : item.getInvPlanAllocations().values()) {
              for (final var data : allocList.stream()
                  .sorted((a, b) -> ComparisonChain.start()
                      .compare(a.getInvPlanId(), b.getInvPlanId())
                      .compare(ID.apply(a.getCommitId()), ID.apply(b.getCommitId()))
                      .compare(a.getInvPlanAllocCode(), b.getInvPlanAllocCode())
                      .result())
                  .toList()) {

                result.append("  - ").append(ID.apply(data.getId())).append("::").append(data.getDocType()).append(System.lineSeparator());
              }
            }
          }

          return items;
        })
        .await().indefinitely();

    return result.toString();
  }
}